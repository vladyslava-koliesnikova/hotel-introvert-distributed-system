package sk.upjs.kopr.service;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import sk.upjs.kopr.model.Guest;
import sk.upjs.kopr.model.HotelMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class BillingService {
    private KafkaConsumer<String, String> consumer;
    private DatabaseService database;
    private Gson gson;
    private DateTimeFormatter formatter;

    public BillingService() {
        this.consumer = KafkaService.createConsumer();
        this.database = DatabaseService.getInstance();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public void start() {
        consumer.subscribe(Arrays.asList(
                KafkaService.TOPIC_CHECKIN,
                KafkaService.TOPIC_CHECKOUT,
                KafkaService.TOPIC_MEAL
        ));

        System.out.println("BillingService spustený. Polling...");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
                String topic = record.topic();
                String json = record.value();

                if (topic.equals(KafkaService.TOPIC_CHECKIN)) {
                    handleCheckIn(json);
                } else if (topic.equals(KafkaService.TOPIC_CHECKOUT)) {
                    handleCheckOut(json);
                } else if (topic.equals(KafkaService.TOPIC_MEAL)) {
                    handleMeal(json);
                }
            }
        }
    }

    private void handleCheckIn(String json) {
        try {
            HotelMessage msg = gson.fromJson(json, HotelMessage.class);
            LocalDateTime time = LocalDateTime.parse(msg.getTimestamp(), formatter);
            Guest guest = database.checkInGuest(time);

            if (guest != null) {
                System.out.println("[CHECK-IN] " + time.format(formatter) + " - " + guest.getName() + " sa ubytoval");
                printFreeRooms();
            } else {
                System.out.println("[CHECK-IN] " + time.format(formatter) + " Hotel je plný");
            }
        } catch (Exception e) {
            System.err.println("Chyba CheckIn: " + e.getMessage());
        }
    }

    private void handleCheckOut(String json) {
        try {
            HotelMessage msg = gson.fromJson(json, HotelMessage.class);
            LocalDateTime time = LocalDateTime.parse(msg.getTimestamp(), formatter);
            Guest guest = database.checkOutGuest(msg.getRoomNumber(), time);

            if (guest != null) {
                System.out.println("\n[CHECK-OUT] " + guest.getName() + " odchádza");
                System.out.println("   Príchod: " + guest.getCheckInTime().format(formatter));
                System.out.println("   Odchod: " + time.format(formatter));
                System.out.println("   Raňajky: " + guest.getBreakfasts());
                System.out.println("   Obedy: " + guest.getLunches());
                System.out.println("   Večere: " + guest.getDinners());
                System.out.println("   Noci: " + guest.getNights());
                System.out.println("   Celková suma: " + guest.getTotalCost() + " EUR\n");
                printFreeRooms();
            }
        } catch (Exception e) {
            System.err.println("Chyba CheckOut: " + e.getMessage());
        }
    }

    private void handleMeal(String json) {
        try {
            HotelMessage msg = gson.fromJson(json, HotelMessage.class);
            database.serveMealToAll(msg.getMealType());
            int count = database.getOccupiedRoomsCount();

            String mealName = switch (msg.getMealType()) {
                case "BREAKFAST" -> "Raňajky";
                case "LUNCH" -> "Obed";
                case "DINNER" -> "Večera";
                default -> "Jedlo";
            };

            System.out.println("[JEDLO] " + msg.getTimestamp() + " - " + mealName + " (podávané pre " + count + " hostí)");
        } catch (Exception e) {
            System.err.println("Chyba Meal: " + e.getMessage());
        }
    }

    private void printFreeRooms() {
        System.out.println("   Voľné izby: " + database.getFreeRooms());
    }
}