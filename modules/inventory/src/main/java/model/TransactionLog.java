package model;

public class TransactionLog {

    private int logID;
    private String username;
    private String action;
    private String details;
    private String dateTime;


    public TransactionLog(
            int logID,
            String username,
            String action,
            String details,
            String dateTime
    ) {

        this.logID = logID;
        this.username = username;
        this.action = action;
        this.details = details;
        this.dateTime = dateTime;

    }


    public int getLogID() {
        return logID;
    }


    public String getUsername() {
        return username;
    }


    public String getAction() {
        return action;
    }


    public String getDetails() {
        return details;
    }


    public String getDateTime() {
        return dateTime;
    }

}