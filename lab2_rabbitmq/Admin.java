import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Admin {

    private static final String EXCHANGE_NAME = "admin";
    private static final String LOG_QUEUE = "log";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";

    private static final String INFO = ANSI_YELLOW + "[INFO]" + ANSI_RESET;
    private static final String IN = ANSI_BLUE + "[IN]" + ANSI_RESET;
    private static final String OUT = ANSI_GREEN + "[OUT]" + ANSI_RESET;

    public static void main(String[] args) throws IOException, TimeoutException {

        // info
        System.out.println(INFO + "Panel Administracyjny");

        // połączenie
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange administracyjny
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // kolejka na przyjmowanie wiadomości od ekip/dostawców o wysłanych wiadomościach
        // kolejka sama się usuwa, jeśli nikt z niej nie korzysta
        channel.queueDeclare(LOG_QUEUE, false, false, true, null);
        channel.queueBind(LOG_QUEUE, EXCHANGE_NAME, LOG_QUEUE);

        // wątek na odbieranie wiadomości na tej kolejce
        Thread t = new Thread(new Runnable() {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(IN + "Otrzymano wiadomość: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), true);
                }
            };
            @Override
            public void run() {
                while(true){
                    try {
                        channel.basicConsume(LOG_QUEUE, false, consumer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // uruchomienie wątku
        t.start();

        // wysyłanie wiadomości
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            // wpisanie odbiorcy wiadomości
            System.out.println(INFO + "Do kogo chcesz wysłać wiadomość (ekipy/dostawcy/wszyscy): ");
            String receiver = br.readLine();

            // wyjście z pętli
            if ("exit".equals(receiver)) {
                break;
            }

            // wpisanie treści wiadomości
            System.out.println(INFO + "Treść wiadomości: ");
            String msg = br.readLine();

            // wysyłanie wiadomości
            if(receiver.equalsIgnoreCase("wszyscy")){
                channel.basicPublish(EXCHANGE_NAME, "ekipy", null, msg.getBytes(StandardCharsets.UTF_8));
                channel.basicPublish(EXCHANGE_NAME, "dostawcy", null, msg.getBytes(StandardCharsets.UTF_8));
            }
            else{
                channel.basicPublish(EXCHANGE_NAME, receiver, null, msg.getBytes("UTF-8"));
            }

            System.out.println(OUT + "Wysłano: " + msg);
        }

    }
}
