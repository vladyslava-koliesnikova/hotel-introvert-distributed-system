package sk.upjs.kopr.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Servisná trieda, ktorá zapuzdruje (skrýva) logiku pripojenia k RabbitMQ.
 * Každý generátor a aj BillingService si vytvára vlastnú inštanciu tejto triedy,
 * čo znamená, že každý má vlastné pripojenie (Connection) a kanál (Channel).
 */
public class RabbitMQService {
    // Adresa servera RabbitMQ. 'localhost' znamená, že beží na tom istom stroji.
    private static final String HOST = "localhost";

    // Definovanie názvov front (queues) ako verejných konštánt
    public static final String CHECKIN_QUEUE = "hotel.checkin";
    public static final String CHECKOUT_QUEUE = "hotel.checkout";
    public static final String MEAL_QUEUE = "hotel.meal";

    private Connection connection; // Pripojenie k RabbitMQ serveru
    private Channel channel;       // Kanál v rámci pripojenia, cez ktorý posielame správy

    public RabbitMQService() throws IOException, TimeoutException {
        System.out.println("Pripájam sa k RabbitMQ na " + HOST + "...");

        // Továreň (Factory) na vytváranie pripojení
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername("guest"); // Predvolené prihlasovacie údaje
        factory.setPassword("guest");
        factory.setPort(5672); // Predvolený port pre AMQP protokol

        try {
            // Vytvorenie samotného pripojenia
            connection = factory.newConnection();
            // Vytvorenie kanála v rámci pripojenia
            channel = connection.createChannel();

            System.out.println("Pripojené k RabbitMQ!");
            System.out.println("   Pripojenie: " + connection);

            // Deklarujeme (vytvoríme) fronty. Ak už existujú, tento krok sa preskočí.
            // 'queueDeclare(name, durable, exclusive, autoDelete, arguments)`
            // durable=false: fronta neprežije reštart RabbitMQ servera
            // exclusive=false: fronta nie je viazaná na toto pripojenie
            // autoDelete=false: fronta sa nezmaže, keď sa odpojí posledný spotrebiteľ
            channel.queueDeclare(CHECKIN_QUEUE, false, false, false, null);
            channel.queueDeclare(CHECKOUT_QUEUE, false, false, false, null);
            channel.queueDeclare(MEAL_QUEUE, false, false, false, null);

            System.out.println("Fronty vytvorené!");

        } catch (Exception e) {
            System.err.println("NEPODARILO sa pripojiť k RabbitMQ!");
            System.err.println("   Skontrolujte, či RabbitMQ beží:");
            System.err.println("   1. Otvorte manažment rozhranie v prehliadači: http://localhost:15672");
            System.err.println("   2. Alebo použite príkaz v termináli: rabbitmqctl status");
            throw e; // Hlásime chybu ďalej, aby sa program nespustil nesprávne
        }
    }

    /**
     * Metóda na odoslanie správy (String) do špecifickej fronty.
     *
     * @param queue   Názov cieľovej fronty
     * @param message Správa (zvyčajne vo formáte JSON)
     */
    public void sendMessage(String queue, String message) throws IOException {
        // 'basicPublish(exchange, routingKey, props, body)`
        // Používame "default exchange" (prázdny string ""), čo znamená,
        // že správa ide priamo do fronty, ktorej názov je v 'routingKey' (u nás 'queue').
        channel.basicPublish("", queue, null, message.getBytes());
    }

    /**
     * Getter pre kanál.
     * Používa ho BillingService na spustenie 'basicConsume' (počúvania).
     *
     * @return Aktívny RabbitMQ kanál
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Metóda na korektné zatvorenie pripojenia.
     * (V tejto simulácii sa nevyužíva, keďže beží donekonečna).
     */
    public void close() throws IOException, TimeoutException {
        if (channel != null) channel.close();
        if (connection != null) connection.close();
    }
}