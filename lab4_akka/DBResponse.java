package akka.Homework;

public class DBResponse {

    public int satelliteID;
    public int errors;

    public DBResponse(int satelliteID, int errors){
        this.satelliteID = satelliteID;
        this.errors = errors;
    }

}
