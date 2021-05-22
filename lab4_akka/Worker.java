package akka.Homework;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Worker(){}

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StationRequest.class, request -> {
                    HashMap<Integer, SatelliteAPI.Status> satelliteStatuses = new HashMap<>();
                    int lateResponses = 0;

                    ExecutorService executor = Executors.newCachedThreadPool();
                    ArrayList<Future<Pair<SatelliteAPI.Status, Integer>>> results = new ArrayList<>();

                    for(int i = request.firstSatId; i < request.firstSatId + request.range; i++){
                        int finalI = i;
                        results.add(executor.submit(new Callable<Pair<SatelliteAPI.Status, Integer>>() {
                            @Override
                            public Pair<SatelliteAPI.Status, Integer> call() throws Exception {
                                long start = System.currentTimeMillis();
                                SatelliteAPI.Status status = SatelliteAPI.getStatus(finalI);
                                long timeElapsed = System.currentTimeMillis() - start;

                                return new Pair<>(status, Math.toIntExact(timeElapsed));
                            }
                        }));
                    }

                    for(int i = request.firstSatId; i < request.firstSatId + request.range; i++){
                        Pair<SatelliteAPI.Status, Integer> p = results.get(i - request.firstSatId).get();

                        if(p.first() != SatelliteAPI.Status.OK){
                            satelliteStatuses.put(i, p.first());
                        }

                        if(p.second() > request.timeout){
                            lateResponses++;
                        }
                    }

                    executor.shutdown();

                    DispatcherResponse response = new DispatcherResponse(request.queryId, satelliteStatuses, lateResponses / request.range);

                    final ActorRef dbLog = getContext().actorOf(Props.create(DBLogger.class).withDispatcher("my-dispatcher"));

                    dbLog.tell(response, null);
                    getSender().tell(response, null);

                    getContext().stop(getSelf());
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }
}
