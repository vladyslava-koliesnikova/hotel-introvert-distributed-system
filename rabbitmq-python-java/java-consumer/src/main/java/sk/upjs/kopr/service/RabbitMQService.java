package sk.upjs.kopr.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class RabbitMQService {
    private static final String HOST = "localhost";

    public static final String CHECKIN_QUEUE = "hotel.checkin";
    public static final String CHECKOUT_QUEUE = "hotel.checkout";
    public static final String MEAL_QUEUE = "hotel.meal";

    private Connection connection;
    private Channel channel;

    public RabbitMQService() throws IOException, TimeoutException {
        System.out.println("Pripájam sa k RabbitMQ na " + HOST);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPort(5672);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            System.out.println("Pripojené k RabbitMQ");
            System.out.println("Pripojenie: " + connection);

            //vytvorime fronty
            //queueDeclare(name, durable, exclusive, autoDelete, arguments)
            //durable=false: fronta neprezije restart RabbitMQ servera
            //exclusive=false: fronta nie je viazana na toto pripojenie
            //autoDelete=false: fronta sa nezmaze, ked sa odpoji posledny spotrebitel
            channel.queueDeclare(CHECKIN_QUEUE, false, false, false, null);
            channel.queueDeclare(CHECKOUT_QUEUE, false, false, false, null);
            channel.queueDeclare(MEAL_QUEUE, false, false, false, null);

            System.out.println("Fronty vytvorené");

        } catch (Exception e) {
            System.err.println("Nepodarilo sa pripojiť k RabbitMQ");
            throw e;
        }
    }


    public void sendMessage(String queue, String message) throws IOException {
        //sprava ide priamo do fronty queue
        channel.basicPublish("", queue, null, message.getBytes());
    }

    public Channel getChannel() {
        return channel;
    }

}