package edu.usc.infolab.geo.model;

/**
 * Represents a location update form a moving object.
 *
 * @author Yaguang
 */
public class LocationUpdate {
    public static final int STATUS_EMPTY = 0;
    public static final int STATUS_LOAD = 1;

    private TrjRecord record;
    private String content;
    private int event = 4;
    private int status = 0;


    public String getContent() {
        return content;
    }

    public TrjRecord getRecord() {
        return record;
    }

    public LocationUpdate(TrjRecord record, String content) {
        this.record = record;
        this.content = content;
    }

    public LocationUpdate(TrjRecord record, int status, int event, String content) {
        this.record = record;
        this.content = content;
        this.status = status;
        this.event = event;
    }

    public int getEvent() {
        return event;
    }

    private void setEvent(int event) {
        this.event = event;
    }

    public int getStatus() {
        return status;
    }

    private void setStatus(int status) {
        this.status = status;
    }
}
