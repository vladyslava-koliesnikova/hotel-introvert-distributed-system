package sk.upjs.kopr.generator;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sk.upjs.kopr.model.Guest;
import sk.upjs.kopr.model.HotelMessage;
import sk.upjs.kopr.service.DatabaseService;
import sk.upjs.kopr.service.KafkaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CheckoutGenerator {
    private KafkaProducer<String, String> producer;
    private DatabaseService database;
    private Gson gson;
    private DateTimeFormatter formatter;
    private LocalDateTime simulationTime;
    private double checkoutProbability;
    private Random random;

    public CheckoutGenerator(double probability) {
        this.producer = KafkaService.createProducer();
        this.database = DatabaseService.getInstance();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.simulationTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        this.checkoutProbability = probability / 100.0;
        this.random = new Random();
    }

    public void start() {
        System.out.println("CheckoutGenerator spustený...");

        while (true) {
            try {
                Guest guest = database.getRandomGuest();

                if (guest != null) {
                    if (random.nextDouble() < checkoutProbability) {
                        HotelMessage message = new HotelMessage(HotelMessage.MessageType.CHECK_OUT);
                        message.setRoomNumber(guest.getRoomNumber());
                        message.setGuestName(guest.getName());
                        message.setTimestamp(simulationTime.format(formatter));

                        String jsonMessage = gson.toJson(message);

                        producer.send(new ProducerRecord<>(KafkaService.TOPIC_CHECKOUT, null, jsonMessage));
                    }
                }
                simulationTime = simulationTime.plusHours(1);
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Chyba CheckoutGenerator: " + e.getMessage());
            }
        }
    }
}