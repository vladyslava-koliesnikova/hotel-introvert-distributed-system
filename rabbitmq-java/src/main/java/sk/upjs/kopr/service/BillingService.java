package sk.upjs.kopr.service;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import sk.upjs.kopr.model.Guest;
import sk.upjs.kopr.model.HotelMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BillingService {
    private RabbitMQService rabbitMQ;
    private DatabaseService database;
    private Gson gson;
    private DateTimeFormatter formatter;

    public BillingService() throws IOException, TimeoutException {
        this.rabbitMQ = new RabbitMQService();
        this.database = DatabaseService.getInstance();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public void start() {
        listenToQueue(RabbitMQService.CHECKIN_QUEUE, this::handleCheckIn);
        listenToQueue(RabbitMQService.CHECKOUT_QUEUE, this::handleCheckOut);
        listenToQueue(RabbitMQService.MEAL_QUEUE, this::handleMeal);

        System.out.println("BillingService spustený. Očakávam správy... (Manuálne ACK)");
    }

    private void listenToQueue(String queueName, DeliverCallback callback) {
        try {
            // ---- ZMENA 1 ----
            // Zmenili sme 'autoAck' z 'true' na 'false'.
            // RabbitMQ teraz počká na naše manuálne potvrdenie (ACK).
            boolean autoAck = false;
            rabbitMQ.getChannel().basicConsume(queueName, autoAck, callback, consumerTag -> {
            });

        } catch (IOException e) {
            System.err.println("Chyba pri počúvaní fronty " + queueName + ": " + e.getMessage());
        }
    }

    private void handleCheckIn(String consumerTag, com.rabbitmq.client.Delivery delivery) {
        // ---- ZMENA 2: Získame potrebné nástroje na ACK/NACK ----
        // deliveryTag je unikátny identifikátor tejto konkrétnej správy
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        // Získame kanál, cez ktorý budeme posielať odpoveď
        Channel channel = rabbitMQ.getChannel();
        // ----------------------------------------------------

        try {
            // Všetka pôvodná logika je teraz v 'try' bloku
            String message = new String(delivery.getBody());
            HotelMessage hotelMessage = gson.fromJson(message, HotelMessage.class);

            LocalDateTime checkInTime = LocalDateTime.parse(hotelMessage.getTimestamp(), formatter);
            Guest guest = database.checkInGuest(checkInTime);

            if (guest != null) {
                System.out.println("[CHECK-IN] " + checkInTime.format(formatter) +
                        " - " + guest.getName() + " sa ubytoval");
                printFreeRooms();
            } else {
                System.out.println("[CHECK-IN] " + checkInTime.format(formatter) +
                        " - Hotel je plný! Hosť sa nemôže ubytovať");
            }

            // ---- ZMENA 3: Spracovanie bolo ÚSPEŠNÉ ----
            // Manuálne potvrdíme správu (ACK). RabbitMQ ju teraz môže zmazať.
            // 'false' znamená, že potvrdzujeme iba túto jednu správu.
            channel.basicAck(deliveryTag, false);
            // ------------------------------------------

        } catch (Exception e) {
            // ---- ZMENA 4: Spracovanie ZLYHALO ----
            System.err.println("CHYBA pri spracovaní CHECK_IN: " + e.getMessage());
            try {
                // Manuálne zamietneme správu (NACK) a povieme, aby ju VRÁTIL späť do fronty.
                // 'false' = zamietnuť iba túto jednu správu
                // 'true' = vrátiť správu do fronty (requeue)
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                System.err.println("CHYBA pri vrátení správy (NACK) do fronty: " + ioException.getMessage());
            }
            // ---------------------------------------
        }
    }

    private void handleCheckOut(String consumerTag, com.rabbitmq.client.Delivery delivery) {
        // ---- ZMENA 2 (Rovnaká ako vyššie) ----
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        Channel channel = rabbitMQ.getChannel();
        // -------------------------------------

        try {
            // Pôvodná logika
            String message = new String(delivery.getBody());
            HotelMessage hotelMessage = gson.fromJson(message, HotelMessage.class);

            LocalDateTime checkOutTime = LocalDateTime.parse(hotelMessage.getTimestamp(), formatter);
            Guest guest = database.checkOutGuest(hotelMessage.getRoomNumber(), checkOutTime);

            if (guest != null) {
                System.out.println("\n[CHECK-OUT] " + guest.getName() + " odchádza");
                System.out.println("   Príchod: " + guest.getCheckInTime().format(formatter));
                System.out.println("   Odchod: " + checkOutTime.format(formatter));
                System.out.println("   Raňajky: " + guest.getBreakfasts());
                System.out.println("   Obedy: " + guest.getLunches());
                System.out.println("   Večere: " + guest.getDinners());
                System.out.println("   Noci: " + guest.getNights());
                System.out.println("   Celková suma: " + guest.getTotalCost() + " EUR\n");
                printFreeRooms();
            }

            // ---- ZMENA 3 (Rovnaká ako vyššie) ----
            // ÚSPECH: Potvrdíme správu
            channel.basicAck(deliveryTag, false);
            // -------------------------------------

        } catch (Exception e) {
            // ---- ZMENA 4 (Rovnaká ako vyššie) ----
            System.err.println("CHYBA pri spracovaní CHECK_OUT: " + e.getMessage());
            try {
                // ZLYHANIE: Zamietneme a vrátime do fronty
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                System.err.println("CHYBA pri vrátení správy (NACK) do fronty: " + ioException.getMessage());
            }
            // ---------------------------------------
        }
    }

    private void handleMeal(String consumerTag, com.rabbitmq.client.Delivery delivery) {
        // ---- ZMENA 2 (Rovnaká ako vyššie) ----
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        Channel channel = rabbitMQ.getChannel();
        // -------------------------------------

        try {
            // Pôvodná logika
            String message = new String(delivery.getBody());
            HotelMessage hotelMessage = gson.fromJson(message, HotelMessage.class);

            database.serveMealToAll(hotelMessage.getMealType());
            int guestCount = database.getOccupiedRoomsCount();

            String mealName = switch (hotelMessage.getMealType()) {
                case "BREAKFAST" -> "Raňajky";
                case "LUNCH" -> "Obed";
                case "DINNER" -> "Večera";
                default -> "Jedlo";
            };

            System.out.println("[JEDLO] " + hotelMessage.getTimestamp() +
                    " - " + mealName + " (podávané pre " + guestCount + " hostí)");

            // ---- ZMENA 3 (Rovnaká ako vyššie) ----
            // ÚSPECH: Potvrdíme správu
            channel.basicAck(deliveryTag, false);
            // -------------------------------------

        } catch (Exception e) {
            // ---- ZMENA 4 (Rovnaká ako vyššie) ----
            System.err.println("CHYBA pri spracovaní JEDLA: " + e.getMessage());
            try {
                // ZLYHANIE: Zamietneme a vrátime do fronty
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                System.err.println("CHYBA pri vrátení správy (NACK) do fronty: " + ioException.getMessage());
            }
            // ---------------------------------------
        }
    }

    private void printFreeRooms() {
        List<Integer> freeRooms = database.getFreeRooms();
        System.out.println("   Voľné izby: " + freeRooms);
    }

    public static void main(String[] args) {
        try {
            BillingService service = new BillingService();
            service.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Kritická chyba v BillingService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}