package sk.upjs.kopr.generator;

import com.google.gson.Gson;
import sk.upjs.kopr.model.HotelMessage;
import sk.upjs.kopr.service.RabbitMQService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

public class MealGenerator {
    private RabbitMQService rabbitMQ;
    private Gson gson;
    private DateTimeFormatter formatter;
    private LocalDateTime simulationTime;

    // Definovanie časov jedál ako nemenných konštánt
    private static final LocalTime BREAKFAST_TIME = LocalTime.of(7, 0); // Raňajky o 7:00
    private static final LocalTime LUNCH_TIME = LocalTime.of(12, 0); // Obed o 12:00
    private static final LocalTime DINNER_TIME = LocalTime.of(18, 0); // Večera o 18:00

    public MealGenerator() throws IOException, TimeoutException {
        this.rabbitMQ = new RabbitMQService();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.simulationTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public void start() {
        System.out.println("MealGenerator spustený (jedlo o 7:00, 12:00, 18:00)");

        while (true) {
            try {
                // Získame aktuálnu hodinu (bez dátumu) zo simulačného času
                LocalTime currentTime = simulationTime.toLocalTime();

                // Kontrolujeme, či nastal presný čas podávania jedla
                if (currentTime.equals(BREAKFAST_TIME)) {
                    serveMeal("BREAKFAST"); // Podávame raňajky
                } else if (currentTime.equals(LUNCH_TIME)) {
                    serveMeal("LUNCH"); // Podávame obed
                } else if (currentTime.equals(DINNER_TIME)) {
                    serveMeal("DINNER"); // Podávame večeru
                }

                // Posunieme čas o 1 hodinu
                simulationTime = simulationTime.plusHours(1);

                // Čakáme 1 sekundu
                Thread.sleep(1000);

            } catch (InterruptedException | IOException e) {
                System.err.println("Chyba v MealGenerator: " + e.getMessage());
                break;
            }
        }
    }

    // Privátna pomocná metóda na odoslanie správy o jedle
    private void serveMeal(String mealType) throws IOException {
        HotelMessage message = new HotelMessage(HotelMessage.MessageType.MEAL_SERVED);
        message.setMealType(mealType); // Nastavíme typ jedla (napr. "BREAKFAST")
        message.setTimestamp(simulationTime.format(formatter));

        String jsonMessage = gson.toJson(message);
        // Odošleme do fronty 'hotel.meal'
        rabbitMQ.sendMessage(RabbitMQService.MEAL_QUEUE, jsonMessage);
    }

    public static void main(String[] args) {
        try {
            MealGenerator generator = new MealGenerator();
            generator.start();
        } catch (Exception e) {
            System.err.println("Nepodarilo sa spustiť MealGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}