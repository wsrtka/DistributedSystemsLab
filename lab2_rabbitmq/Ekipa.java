import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Ekipa {

    private static final String EXCHANGE_NAME = "zamowienia";
    private static final String ADMIN_EXCHANGE = "admin";
    private static final String EKIPY_QUEUE = "ekipy";
    private static final String LOG_QUEUE = "log";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RED = "\u001B[31m";

    private static final String INFO = ANSI_YELLOW + "[INFO]" + ANSI_RESET;
    private static final String IN = ANSI_BLUE + "[IN]" + ANSI_RESET;
    private static final String ADMIN_IN = ANSI_RED + "[IN]" + ANSI_RESET;
    private static final String OUT = ANSI_GREEN + "[OUT]" + ANSI_RESET;
    private static final String CONF = ANSI_CYAN + "[CONF]" + ANSI_RESET;

    public static void main(String[] argv) throws Exception {

        int msgId = 0;

        // wybór nazwy ekipy
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(CONF + " Podaj nazwę ekipy: ");
        String name = br.readLine();

        // info
        System.out.println(INFO +"Ekipa " + name);

        // połączenie
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange na zamówienia
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // kolejka na potwierdzenia od dostawców
        // kolejka nie jest ekskluzywna dla tego klienta, sama się usuwa, jeśli nie ma używających jej programów
        channel.queueDeclare(name, false, false, true, null);
        channel.queueBind(name, EXCHANGE_NAME, name);

        // wątek obsługujący wiadomości przychodzące na tę kolejkę
        Thread ack_t = new Thread(new Runnable() {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(IN +"Otrzymano wiadomość: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), true);
                }
            };
            @Override
            public void run() {
                while(true){
                    try {
                        channel.basicConsume(name, false, consumer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // uruchomienie wątku
        ack_t.start();

        // exchange odpowiedzialny za wymianę wiadomości administratora
        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);

        // kolejka na wiadomości od administratora
        channel.queueDeclare(EKIPY_QUEUE, false, false, true, null);
        channel.queueBind(EKIPY_QUEUE, ADMIN_EXCHANGE, EKIPY_QUEUE);

        // wątek obsługujący tę kolejkę
        Thread adm_t = new Thread(new Runnable() {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(ADMIN_IN + "Otrzymano wiadomość od administratora: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), true);
                }
            };
            @Override
            public void run() {
                while(true){
                    try {
                        channel.basicConsume(EKIPY_QUEUE, false, consumer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // uruchomienie wątku
        adm_t.start();


        // pętla na wysyłanie wiadomości
        while (true) {

            // treść wiadomości
            System.out.println(INFO + "Podaj nazwę przedmiotu do zamówienia (tlen/buty/plecak): ");
            String key = br.readLine();

            // wyjście z pętli
            if ("exit".equals(key)) {
                break;
            }

            // konstrukacja wiadomości do wysłąnia
            // wiadomość ma postać <nazwa ekipy>#<nr wiadomości>#<przedmiot zamówienia>
            String message = String.join("#", Arrays.asList(name, String.valueOf(msgId), key));
            msgId++;

            // wysłanie wiadomości do dostawców oraz do logów
            channel.basicPublish(EXCHANGE_NAME, key, null, message.getBytes("UTF-8"));
            channel.basicPublish(ADMIN_EXCHANGE, LOG_QUEUE, null, ("Wysłano zlecenie od " + name + " na " + key).getBytes(StandardCharsets.UTF_8));
            System.out.println(OUT + "Wysłano: " + message);
        }
    }
}
