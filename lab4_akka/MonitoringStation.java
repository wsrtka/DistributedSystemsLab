package akka.Homework;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MonitoringStation extends AbstractActor {

    private final ActorRef dispatcher;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int requestId = 0;
    private long requestTime;

    public MonitoringStation(ActorRef dispatcher){
        this.dispatcher = dispatcher;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StationRequest.class, request -> {
                    request.queryId = requestId;
                    requestId++;
                    dispatcher.tell(request, getSelf());
                    requestTime = System.currentTimeMillis();
                })
                .match(DispatcherResponse.class, response -> {
                    log.info("Station: " + getSelf().path().name());
                    log.info("Received response after: " + (System.currentTimeMillis() - requestTime) + "ms");
                    log.info("Number of error statuses: " + response.satelliteStatuses.size());
                    for(int key : response.satelliteStatuses.keySet()){
                        log.info(key + "\t" + response.satelliteStatuses.get(key));
                    }
                })
                .match(DBRequest.class, request -> {
                    dispatcher.tell(request, getSelf());
                })
                .match(DBResponse.class, response -> {
                    if(response.errors > 0){
                        log.info("Satellite " + response.satelliteID + " has " + response.errors + " errors.");
                    }
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }
}
