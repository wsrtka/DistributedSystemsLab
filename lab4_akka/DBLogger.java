package akka.Homework;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import java.sql.*;

public class DBLogger extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:db.sqlite";
    private static Connection connection;

    public DBLogger(){
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        final SQLiteConfig config = new SQLiteConfig();
        config.setOpenMode(SQLiteOpenMode.FULLMUTEX);

        try {
            connection = DriverManager.getConnection(DB_URL, config.toProperties());
            connection.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
public Receive createReceive() {
        return receiveBuilder()
                .match(DispatcherResponse.class, request -> {
                    System.out.println("Successfully opened db");
                    for (Integer key : request.satelliteStatuses.keySet()) {
                        try (PreparedStatement update = connection.prepareStatement("UPDATE satellite_errors SET errors = errors + 1 WHERE satellite_id = ?")) {
                            update.setInt(1, key);
                            update.execute();
                        }
                    }

                    getContext().stop(getSelf());
                })
                .match(DBRequest.class, request -> {
                    int errors = 0;
                    try(PreparedStatement select = connection.prepareStatement("SELECT errors FROM satellite_errors WHERE satellite_id = ?")){
                        select.setInt(1, request.satelliteId);
                        try(final ResultSet result = select.executeQuery()){
                            if(result.next()){
                                errors = result.getInt("errors");
                            }
                        }
                    }

                    getSender().tell(new DBResponse(request.satelliteId, errors), null);

                    getContext().stop(getSelf());
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }
}
