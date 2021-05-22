package akka.Homework;

import java.util.HashMap;

public class DispatcherResponse {

    public int queryId;
    public HashMap<Integer, SatelliteAPI.Status> satelliteStatuses;
    public double lateResponses;

    public DispatcherResponse(int queryId, HashMap<Integer, SatelliteAPI.Status> satelliteStatuses, double lateResponses){
        this.queryId = queryId;
        this.satelliteStatuses = satelliteStatuses;
        this.lateResponses = lateResponses;
    }

}
