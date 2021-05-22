package akka.Homework;

public class StationRequest {

    public int queryId = -1;
    public int firstSatId;
    public int range;
    public int timeout;

    public StationRequest(int firstSatId, int range, int timeout){
        this.firstSatId = firstSatId;
        this.range = range;
        this.timeout = timeout;
    }

}
