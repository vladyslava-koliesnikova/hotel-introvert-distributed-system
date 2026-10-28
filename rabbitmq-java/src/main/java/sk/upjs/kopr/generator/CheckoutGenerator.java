package sk.upjs.kopr.generator;

import com.google.gson.Gson;
import sk.upjs.kopr.model.Guest;
import sk.upjs.kopr.model.HotelMessage;
import sk.upjs.kopr.service.DatabaseService;
import sk.upjs.kopr.service.RabbitMQService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class CheckoutGenerator {
    private RabbitMQService rabbitMQ;    // Služba pre komunikáciu s RabbitMQ
    private DatabaseService database;    // Prístup k zdieľanej databáze hostí (Singleton)
    private Gson gson;                    // Knižnica na konverziu objektov na JSON
    private DateTimeFormatter formatter; // Formátovač pre dátum a čas
    private LocalDateTime simulationTime;  // Interný simulačný čas tohto generátora
    private double checkoutProbability; // Pravdepodobnosť odchodu (napr. 0.1 pre 10%)
    private Random random;                // Generátor náhodných čísel

    public CheckoutGenerator(double probability) throws IOException, TimeoutException {
        // Inicializácia pripojenia k RabbitMQ pre tento generátor
        this.rabbitMQ = new RabbitMQService();
        // Získanie JEDINEJ inštancie databázy (Singleton pattern)
        this.database = DatabaseService.getInstance();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Nastavíme počiatočný čas simulácie na polnoc aktuálneho dňa
        this.simulationTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        // Prevod percent (napr. 10) na desatinné číslo (0.1) pre výpočty
        this.checkoutProbability = probability / 100.0;
        this.random = new Random();
    }

    public void start() {
        System.out.println("CheckoutGenerator spustený (pravdepodobnosť odchodu: " +
                (checkoutProbability * 100) + "%)");

        // Nekonečná slučka, ktorá predstavuje plynutie času
        while (true) {
            try {
                // Získame náhodného hosťa z databázy
                // Táto metóda je 'synchronized', takže je bezpečná pre prístup z viacerých vlákien
                Guest guest = database.getRandomGuest();

                if (guest != null) { // Iba ak hotel nie je prázdny
                    // Skontrolujeme, či hosť odchádza
                    double randomValue = random.nextDouble(); // Generuje náhodné číslo od 0.0 do 1.0

                    // Porovnanie náhodnej hodnoty s nastavenou pravdepodobnosťou
                    if (randomValue < checkoutProbability) {
                        // Áno, hosť odchádza
                        HotelMessage message = new HotelMessage(HotelMessage.MessageType.CHECK_OUT);
                        // Do správy vložíme číslo izby, aby BillingService vedel, koho odhlásiť
                        message.setRoomNumber(guest.getRoomNumber());
                        message.setGuestName(guest.getName());
                        // Nastavíme AKTUÁLNY simulačný čas ako čas odchodu
                        message.setTimestamp(simulationTime.format(formatter));

                        // Prevedieme objekt správy na JSON string
                        String jsonMessage = gson.toJson(message);
                        // Odošleme JSON správu do príslušnej fronty v RabbitMQ
                        rabbitMQ.sendMessage(RabbitMQService.CHECKOUT_QUEUE, jsonMessage);
                    }
                }

                // Posunieme simulačný čas o 1 hodinu
                simulationTime = simulationTime.plusHours(1);

                // Zastavíme toto vlákno (generátor) na 1 sekundu reálneho času
                // Tým simulujeme, že 1 sekunda = 1 hodina
                Thread.sleep(1000);

            } catch (InterruptedException | IOException e) {
                System.err.println("Chyba v CheckoutGenerator: " + e.getMessage());
                break; // V prípade chyby prerušíme nekonečnú slučku
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Predvolená pravdepodobnosť odchodu (10%)
            double probability = 10.0;

            // Možnosť načítať pravdepodobnosť z argumentov príkazového riadku
            if (args.length > 0) {
                probability = Double.parseDouble(args[0]);
            }

            CheckoutGenerator generator = new CheckoutGenerator(probability);
            generator.start();
        } catch (Exception e) {
            System.err.println("Nepodarilo sa spustiť CheckoutGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}