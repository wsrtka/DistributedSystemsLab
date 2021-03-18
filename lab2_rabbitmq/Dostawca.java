import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

public class Dostawca {

    private static final String EXCHANGE_NAME = "zamowienia";
    private static final String ADMIN_EXCHANGE = "admin";
    private static final String DOSTAWCY_QUEUE = "dostawcy";
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

        // info
        System.out.println(INFO + "Dostawca");

        // tabela kosztów sprzętu
        HashMap<String, Integer> cost = new HashMap<>();
        cost.put("tlen", 1);
        cost.put("buty", 2);
        cost.put("plecak", 3);

        // połączenie
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange na zamówienia
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // ustawienie współdzielenia obciążenia
        channel.basicQos(1, true);

        // exchange na wiadomości administratora
        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);

        // kolejka na wiadomości do dostawców
        channel.queueDeclare(DOSTAWCY_QUEUE, false, false, true, null);
        channel.queueBind(DOSTAWCY_QUEUE, ADMIN_EXCHANGE, DOSTAWCY_QUEUE);

        // kolejki na sprzęt, który obsługuje dostawca
        LinkedList<String> queues = new LinkedList<>();

        // kofiguracja dostępnego u dostawcy sprzętu
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.println(CONF + "Podaj klucz (tlen/buty/plecak): ");
            String key = br.readLine();

            // wyjście z pętli
            if(key.equalsIgnoreCase("exit")){
                break;
            }

            // konfiguracja kolejki na sprzęt
            queues.add(key);
            channel.queueDeclare(key, false, false, true, null);
            channel.queueBind(key, EXCHANGE_NAME, key);
        }

        // obsługa przychodzących zleceń
        Consumer consumer = new DefaultConsumer(channel) {

            int orderId = 1;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");

                // podzielenie wiadomości na interesujące nas części
                String[] data = message.split("#");
                System.out.println(INFO + "Otrzymano zamówienie nr " + data[0] + "#" + data[1] + "#" + orderId + " na: " + data[2]);

                orderId++;
                int timeToSleep = 0;

                if(cost.containsKey(data[1])){
                    timeToSleep = cost.get(data[1]);
                }

                // symulacja obsługi zlecenia
                try {
                    Thread.sleep(timeToSleep * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }

                // wysłanie potwierdzenia do klienta
                channel.basicAck(envelope.getDeliveryTag(), false);
                channel.basicPublish(EXCHANGE_NAME, data[0], null, ("Potwierdzenie wykonania zamówienia " + data[0] + "#" + data[1] + "#" + orderId).getBytes(StandardCharsets.UTF_8));
                channel.basicPublish(ADMIN_EXCHANGE, LOG_QUEUE, null, ("Dostawca wysłał potwierdzenie zamówienia " + data[0] + "#" + data[1] + "#" + orderId + " na " + data[2]).getBytes(StandardCharsets.UTF_8));
                System.out.println(OUT + "Wysłano potwierdzenie wykonania zamówienia " + data[0] + "#" + data[1] + "#" + orderId);
            }
        };

        // odbieranie zleceń w osobnych wątkach
        for(String q : queues) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true){
                            channel.basicConsume(q, false, consumer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }

        // wątek na odbieranie wiadomości od administratora
        Thread adm_t = new Thread(new Runnable() {
            Consumer cons = new DefaultConsumer(channel) {
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
                        channel.basicConsume(DOSTAWCY_QUEUE, false, cons);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        adm_t.start();
    }
}
