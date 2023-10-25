/* 
 * Programmer: 	Mitchell Foote
 * Course: 	   	COSC 311, F'23
 * Project:    	3
 * Due date:   	10-26-23
 * Project Description: A terminal based simulation program, the simulation models a research center that provides remote
 * access to its computing facilities via a bank of modems. These modems are access by dialing via telephone when the modems are all occupied the users are added to
 * a queue the length of which is determined by user input, various metrics such as average wait time are calculated and reported out
 * to the terminal as well as saved to a report text file.
 * Class Description: The following Event class provides data structures that model the dial in and hang up events simulated by the program.
 * */

public class Event implements Comparable<Event> {
    
    // Enum to represent event types
    public enum EventType {
        DIAL_IN, HANG_UP
    }

    //member variables
    private EventType eventType; // Type of event (DIAL_IN or HANG_UP)
    private int eventTime;       // Simulation time when the event occurs
    private int userID;          // Unique ID of the user associated with the event

    //constructor
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