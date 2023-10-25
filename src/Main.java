import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Main {
	
	private static int simLength;
	private static double timeBetweenDial;
	private static int avgConnTime;
	private static int numModem;
	private static int sizeWaitQueue;
	
	
	private static int droppedCalls;
	private static double cumulativeWaitingTime;
    private static double lastDialInTime;
    private static double cumulativeBusyTime;
	
	private static Poisson dialInPoisson;
	private static Poisson connectionPoisson;
	
	private static QueueSLL<Event> eventQ;
	private static QueueCircularArray<Event> userQ;
	
	private static Scanner scanner;
	private static File txtFile;
	private static FileWriter fileOut;
	
	private static int nextID = 1000;
	
	public static void main(String args[]) {
		
		System.out.println("Programmer:		Mitchell Foote");
		System.out.println("Course:			COSC 311, F'23");
		System.out.println("Project:		3");
		System.out.println("Due date:		10-26-23\n");
		
		//Open Scanner object to take keyboard input.
		scanner = new Scanner(System.in);
		txtFile = new File("report.txt");
		try {
			fileOut = new FileWriter("report.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		simulationManager();
	   


	}
	
	private static void simulationManager() {
		
		while(true) {
			
			//Get input param and write to report file
			inputPrompt();
			
			//create event queue and waiting queue
			eventQ = new QueueSLL<>(); 
	        userQ = new QueueCircularArray<>(sizeWaitQueue);
	        
	        //init event queue with dial-in events
	        dialInPoisson = new Poisson(timeBetweenDial);
	        initEQ();
	        
	        droppedCalls = 0;
	        cumulativeWaitingTime = 0;
	        lastDialInTime = 0;
	        cumulativeBusyTime = 0;
	        
	        connectionPoisson = new Poisson(avgConnTime);

	        
	        simulationLoop();
		
			double averageWaitTime = (userQ.size() == 0) ? 0 : cumulativeWaitingTime / userQ.size();
	        double modemBusyPercentage = (cumulativeBusyTime / (numModem * simLength)) * 100;

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
	    try {
			fileOut.write("Simulation length: " + simLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    System.out.print("Enter average time between dials: ");
	    timeBetweenDial = scanner.nextDouble();
	    try {
			fileOut.write("Average time between Dial-Ins: " + timeBetweenDial);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    System.out.print("Enter average connection time: ");
	    avgConnTime = scanner.nextInt();
	    try {
			fileOut.write("Average connection time: " + avgConnTime);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    System.out.print("Enter number of modems: ");
	    numModem = scanner.nextInt();
	    try {
			fileOut.write("Number of modems: " + numModem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    System.out.print("Enter waiting queue size: ");
	    sizeWaitQueue = scanner.nextInt();
	    try {
			fileOut.write("Size of waiting queue: " + sizeWaitQueue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	private static void initEQ() {
		
		for(int i = 0; i < simLength; i++) {
			
			int numDials = dialInPoisson.nextInt();
			
			for(int j = 0; j < numDials; j++) {
				
				Event newEvent = new Event(Event.EventType.DIAL_IN, i, nextID);
				nextID++;
				eventQ.offer(newEvent);
				
			}
			
		}
		
	}
	
	private static void simulationLoop() {
		
		int availModem = numModem;
		
		for(int i = 0; i < simLength; i++) {
			
			while(eventQ.peek() != null && eventQ.peek().getEventTime() == i) {
				
				Event currEvent = eventQ.poll();
				if(currEvent.getEventType() == Event.EventType.HANG_UP) {
					availModem++;
				} else if(currEvent.getEventType() == Event.EventType.DIAL_IN && userQ.size() < sizeWaitQueue) {
					userQ.offer(currEvent);
				} else {
					System.out.println("Customer " + currEvent.getUserID() + " was denied service at time unit " + i);
					droppedCalls++;
				}
				
			}
			
			while(availModem > 0 && userQ.peek() != null) {
				
				Event currEvent = userQ.poll();
				cumulativeWaitingTime += (i - currEvent.getEventTime());
				int connectionTime = connectionPoisson.nextInt();
				
				Event currEventHangUp = new Event(Event.EventType.HANG_UP, currEvent.getEventTime() + connectionTime,  currEvent.getUserID());
				eventQ.offer(currEventHangUp);
				
				cumulativeBusyTime += connectionTime;
				availModem--;
				
			}
		}

	}

}
