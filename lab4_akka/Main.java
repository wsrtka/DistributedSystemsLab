package akka.Homework;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.sql.*;
import java.util.Random;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {

        Main.setupDB();

        final File configFile = new File("src/main/resources/akka/homework/dispatcher.conf");
        Config config = ConfigFactory.parseFile(configFile);
        System.out.println("Loaded config: " + config);

        final ActorSystem system = ActorSystem.create("AstraLink", config);

        final ActorRef dispatcher = system.actorOf(Props.create(Dispatcher.class).withDispatcher("my-dispatcher"), "dispatcher");
        final ActorRef[] stations = IntStream
                .rangeClosed(0,2)
                .mapToObj(i -> system.actorOf(Props.create(MonitoringStation.class, dispatcher).withDispatcher("my-dispatcher"), "station" + i))
                .toArray(ActorRef[]::new);

        final Random rand = new Random();

        for(ActorRef station : stations){
            for(int i = 0; i < 2; i++){
                int firstSatId = 100 + rand.nextInt(50);
                station.tell(new StationRequest(firstSatId, 50, 300), null);
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int stationId = rand.nextInt(3);

        for(int i = 100; i < 200; i++){
            stations[stationId].tell(new DBRequest(i), null);
        }

        system.terminate();

    }

    private static void setupDB(){
        String DRIVER = "org.sqlite.JDBC";
        String DB_URL = "jdbc:sqlite:db.sqlite";

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        final SQLiteConfig config = new SQLiteConfig();
        config.setOpenMode(SQLiteOpenMode.FULLMUTEX);

        try(final Connection connection = DriverManager.getConnection(DB_URL, config.toProperties())){
            connection.setAutoCommit(true);

            try(Statement statement = connection.createStatement()){
                statement.execute("CREATE TABLE IF NOT EXISTS satellite_errors (satellite_id INTEGER PRIMARY KEY, errors INTEGER);");
            }

            for(int i = 100; i < 200; i++){
                try(PreparedStatement insert = connection.prepareStatement("INSERT OR REPLACE INTO satellite_errors(satellite_id, errors) VALUES(?, 0)")){
                    insert.setInt(1, i);
                    insert.execute();
                }
            }

            try(Statement pragma = connection.createStatement()){
                pragma.execute("pragma journal_mode=wal");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
