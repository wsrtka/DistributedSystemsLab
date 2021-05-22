package akka.Homework;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Dispatcher extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StationRequest.class, request -> {
                    final ActorRef worker = getContext().actorOf(Props.create(Worker.class).withDispatcher("my-dispatcher"));
                    worker.forward(request, getContext());
                })
                .match(DBRequest.class, request -> {
                    final ActorRef dbLog = getContext().actorOf(Props.create(DBLogger.class).withDispatcher("my-dispatcher"));
                    dbLog.forward(request, getContext());
                })
                .matchAny(msg -> log.info("Received unknown message: " + msg))
                .build();
    }
}
