package sk.upjs.kopr.generator;

import com.google.gson.Gson;
import sk.upjs.kopr.model.HotelMessage;
import sk.upjs.kopr.service.RabbitMQService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

public class GuestGenerator {
    private RabbitMQService rabbitMQ;    // Služba pre komunikáciu s RabbitMQ
    private Gson gson;                    // Knižnica na konverziu objektov na JSON
    private DateTimeFormatter formatter; // Formátovač pre dátum a čas
    private LocalDateTime simulationTime;  // Interný simulačný čas tohto generátora

    public GuestGenerator() throws IOException, TimeoutException {
        this.rabbitMQ = new RabbitMQService();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Začiatok simulácie - dnes o 00:00
        this.simulationTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public void start() {
        System.out.println("GuestGenerator spustený (každú sekundu = 1 hodina v hoteli)");

        // Nekonečná slučka simulácie
        while (true) {
            try {
                // Vytvoríme správu o tom, že prichádza NOVÝ ZÁUJEMCA o ubytovanie
                HotelMessage message = new HotelMessage(HotelMessage.MessageType.CHECK_IN);
                // Posielame čas, kedy sa hosť POKÚŠA ubytovať. BillingService rozhodne, či sa ubytuje.
                message.setTimestamp(simulationTime.format(formatter));

                String jsonMessage = gson.toJson(message);
                // Odošleme správu do CHECKIN fronty
                rabbitMQ.sendMessage(RabbitMQService.CHECKIN_QUEUE, jsonMessage);

                // Zvýšime simulačný čas o 1 hodinu
                simulationTime = simulationTime.plusHours(1);

                // Čakáme 1 sekundu reálneho času (1 sekunda = 1 hodina)
                Thread.sleep(1000);

            } catch (InterruptedException | IOException e) {
                System.err.println("Chyba v GuestGenerator: " + e.getMessage());
                break; // Prerušenie slučky v prípade chyby
            }
        }
    }

    public static void main(String[] args) {
        try {
            GuestGenerator generator = new GuestGenerator();
            generator.start();
        } catch (Exception e) {
            System.err.println("Nepodarilo sa spustiť GuestGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}