package sk.upjs.kopr.generator;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import sk.upjs.kopr.model.HotelMessage;
import sk.upjs.kopr.service.KafkaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GuestGenerator {
    private KafkaProducer<String, String> producer;
    private Gson gson;
    private DateTimeFormatter formatter;
    private LocalDateTime simulationTime;

    public GuestGenerator() {
        this.producer = KafkaService.createProducer();
        this.gson = new Gson();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.simulationTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public void start() {
        System.out.println("GuestGenerator spustený...");

        while (true) {
            try {
                HotelMessage message = new HotelMessage(HotelMessage.MessageType.CHECK_IN);
                message.setTimestamp(simulationTime.format(formatter));

                String jsonMessage = gson.toJson(message);

                producer.send(new ProducerRecord<>(KafkaService.TOPIC_CHECKIN, null, jsonMessage));

                simulationTime = simulationTime.plusHours(1);
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Chyba GuestGenerator: " + e.getMessage());
            }
        }
    }
}