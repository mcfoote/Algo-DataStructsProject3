import java.util.Scanner;


public class Main {
	
	private static int simLength;
	private static double timeBetweenDial;
	private static int avgConnTime;
	private static int numModem;
	private static int sizeWaitQueue;
	
	
	private static int droppedCalls;
	
	private static Poisson dialInPoisson;
	private static Poisson connectionPoisson;
	
	private static QueueSLL<Event> eventQ;
	private static QueueCircularArray<Event> userQ;
	
	private static Scanner scanner;
	
	public static void main(String args[]) {
		
		System.out.println("Programmer:		Mitchell Foote");
		System.out.println("Course:			COSC 311, F'23");
		System.out.println("Project:		3");
		System.out.println("Due date:		10-26-23\n");
		
		//Open Scanner object to take keyboard input.
		scanner = new Scanner(System.in);
		
		simulationManager();
	   


	}
	
	private static void simulationManager() {
		
		while(true) {
			
			inputPrompt();
			
			eventQ = new QueueSLL<>(); 
	        userQ = new QueueCircularArray<>(sizeWaitQueue);
	        
	        droppedCalls = 0;
	        
	        dialInPoisson = new Poisson(timeBetweenDial);
	        connectionPoisson = new Poisson(avgConnTime);

	        
	        double cumulativeWaitingTime = 0;
	        double lastDialInTime = 0;
	        double cumulativeBusyTime = 0;
	        
	        simulationLoop();
		
			double averageWaitTime = (userQ.size() == 0) ? 0 : cumulativeWaitingTime / userQ.size();
	        double modemBusyPercentage = (cumulativeBusyTime / simLength) * 100;

	        System.out.println("Number of dropped calls: " + droppedCalls);
	        System.out.println("Average wait time = " + averageWaitTime);
	        System.out.println("Percentage of time modems were busy = " + modemBusyPercentage);
	        System.out.println("Number of customers left in the waiting queue = " + userQ.size());

	        
	        System.out.print("Run simulation again, yes (or no)? ");
	        String response = scanner.next();
	        if(response.equalsIgnoreCase("no")) {
	            break;
	        }

		}
		 
	}
	
	private static void inputPrompt() {
		
		System.out.print("Enter simulation length: ");
	    simLength = scanner.nextInt();
	    
	    System.out.print("Enter average time between dials: ");
	    timeBetweenDial = scanner.nextDouble();
	    
	    System.out.print("Enter average connection time: ");
	    avgConnTime = scanner.nextInt();
	    
	    System.out.print("Enter number of modems: ");
	    numModem = scanner.nextInt();
	    
	    System.out.print("Enter waiting queue size: ");
	    sizeWaitQueue = scanner.nextInt();
	    
	}
	
	private static void simulationLoop() {
	    // 1. Initialize the simulation with a few dial-in events.
	    for(int i = 0; i < numModem; i++) {
	        eventQ.offer(new Event(Event.EventType.DIAL_IN, i, 1000));
	    }

	    while(!eventQ.empty()) {
	        Event currentEvent = eventQ.poll();

	        switch(currentEvent.getEventType()) {
	            case DIAL_IN:
	                // Check if a modem is available
	                if(numModem > 0) {
	                    numModem--;
	                    // Schedule hang-up event for this user
	                    double hangUpTime = currentEvent.getEventTime() + connectionPoisson.nextInt();
	                    eventQ.offer(new Event(Event.EventType.HANG_UP, (int)hangUpTime, currentEvent.getUserID()));
	                } else if(userQ.size() < sizeWaitQueue) {
	                    userQ.offer(currentEvent);
	                } else {
	                    droppedCalls++;
	                }

	                // Schedule next dial-in event
	                double nextDialInTime = currentEvent.getEventTime() + dialInPoisson.nextInt();
	                if(nextDialInTime < simLength) {
	                    eventQ.offer(new Event(Event.EventType.DIAL_IN, (int)nextDialInTime, currentEvent.getUserID()));
	                }
	                break;

	            case HANG_UP:
	                numModem++;
	                if(!userQ.empty()) {
	                    Event waitingUser = userQ.poll();
	                    double hangUpTime = currentEvent.getEventTime() + connectionPoisson.nextInt();
	                    eventQ.offer(new Event(Event.EventType.HANG_UP, (int)hangUpTime, waitingUser.getUserID()));
	                    numModem--;
	                }
	                break;
	        }
	    }
	}

}
