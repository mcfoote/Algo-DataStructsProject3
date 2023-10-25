public class Event implements Comparable<Event> {
    
    // Enum to represent event types
    public enum EventType {
        DIAL_IN, HANG_UP
    }

    private EventType eventType; // Type of event (DIAL_IN or HANG_UP)
    private int eventTime;       // Simulation time when the event occurs
    private int userID;          // Unique ID of the user associated with the event

    public Event(EventType eventType, int eventTime, int userID) {
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.userID = userID;
    }

    // Getters
    public EventType getEventType() {
        return eventType;
    }

    public int getEventTime() {
        return eventTime;
    }

    public int getUserID() {
        return userID;
    }
    
    //setters
    public void setEventType(EventType eventType) {
    	this.eventType = eventType;
    }
    
    public void setEventTime(int eventTime) {
    	this.eventTime = eventTime;
    }
    
    public void setUserID(int userID) {
    	this.userID = userID;
    }

    // Compare events based on their event times, 
    // useful for maintaining them in order in the priority queue
    @Override
    public int compareTo(Event other) {
        return Integer.compare(this.eventTime, other.eventTime);
    }

    // Useful for debugging
    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", eventTime=" + eventTime +
                ", userID=" + userID +
                '}';
    }
}