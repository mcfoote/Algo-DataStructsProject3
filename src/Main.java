/* 
 * Programmer: 	Mitchell Foote
 * Course: 	   	COSC 311, F'23
 * Project:    	3
 * Due date:   	10-26-23
 * Project Description: A terminal based simulation program, the simulation models a research center that provides remote
 * access to its computing facilities via a bank of modems. These modems are access by dialing via telephone when the modems are all occupied the users are added to
 * a queue the length of which is determined by user input, various metrics such as average wait time are calculated and reported out
 * to the terminal as well as saved to a report text file.
 * Class Description: The following main class handles all functionality other than the data structures and organization handled 
 * by the Event, QueueSLL and QueueCircularArray classes.
 * */


import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//Driver for simulation program
public class Main {
	
	//local variables
	private static int simLength;
	private static double timeBetweenDial;
	private static int avgConnTime;
	private static int numModem;
	private static int sizeWaitQueue;
	
	
	private static int droppedCalls;
	private static double cumulativeWaitingTime;
    private static int totalUser;
    private static double cumulativeBusyTime;
	
    //Poisson Objects
	private static Poisson dialInPoisson;
	private static Poisson connectionPoisson;
	
	//Queue Data structures
	private static QueueSLL<Event> eventQ;
	private static QueueCircularArray<Event> userQ;
	
	//IO objects
	private static Scanner scanner;
	private static File txtFile;
	private static FileWriter fileOut;
	
	private static int nextID = 1000;
	
	//Main prints project info and init io components and begins simulation
	public static void main(String args[]) {
		
		System.out.println("Programmer:		Mitchell Foote");
		System.out.println("Course:			COSC 311, F'23");
		System.out.println("Project:		3");
		System.out.println("Due date:		10-26-23\n");
		
		//Open Scanner object to take keyboard input.
		scanner = new Scanner(System.in);
		txtFile = new File("report.txt");
		try {
			fileOut = new FileWriter(txtFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		try {
		    fileOut = new FileWriter(txtFile);

		    // Write the headers
		    fileOut.write("1- Length of simulation\n");
		    fileOut.write("2- Average time between dial-in attempts\n");
		    fileOut.write("3- Average connection time\n");
		    fileOut.write("4- Number of modems in the bank\n");
		    fileOut.write("5- Size of the waiting queue, -1 for an infinite queue\n");
		    fileOut.write("6- Average wait time\n");
		    fileOut.write("7- Percentage of time modems were busy\n");
		    fileOut.write("8- Number of customers left in the waiting queue\n");
		    fileOut.write("1     | 2     | 3 	| 4 	| 5 	| 6 	| 7 	| 8		\n");
		    fileOut.write("--------------------------------------------------------------\n");
		    fileOut.flush();
		    
		} catch (IOException e) {
		    System.out.println(e.getMessage());
		}
	
		//run simulation
		simulationManager();
		
		
		try {
	        if (fileOut != null) {
	            fileOut.close();
	        }
		} catch (IOException e) {
			System.out.println(e.getMessage());
	    }
		


	}
	
	//Manages interface loop init 
	private static void simulationManager() {
		
		while(true) {
			
			droppedCalls = 0;
	        cumulativeWaitingTime = 0;
	        totalUser = 0;
	        cumulativeBusyTime = 0;
	        nextID = 1000;
			//Get input param and write to report file
			inputPrompt();
			
			//create event queue and waiting queue
			eventQ = new QueueSLL<>(); 
	        userQ = new QueueCircularArray<>(sizeWaitQueue);
	        
	        //init event queue with dial-in events
	        dialInPoisson = new Poisson(1/timeBetweenDial);
	        initEQ();
	        
	        connectionPoisson = new Poisson(avgConnTime);

	        
	        simulationLoop();
		
			double averageWaitTime = cumulativeWaitingTime / totalUser;
	        double modemBusyPercentage = (cumulativeBusyTime / (numModem * simLength)) * 100;

	        System.out.println("Number of dropped calls: " + droppedCalls);
	        System.out.println("Average wait time = " + averageWaitTime);
	        System.out.println("Percentage of time modems were busy = " + modemBusyPercentage);
	        System.out.println("Number of customers left in the waiting queue = " + userQ.size());
	        
	        writeOut();

	        
	        while (true) {  
	            System.out.print("Run simulation again, yes (or no)? ");
	            String response = scanner.next();

	            if (response.equalsIgnoreCase("yes")) {
	                break; // break out of the inner loop, to rerun the simulation
	            } else if (response.equalsIgnoreCase("no")) {
	                return; // or break out of the outer loop too, if you want to stop the simulation entirely
	            } else {
	                System.out.println("Invalid response. Please enter 'yes' or 'no'.");
	            }
	        }
		}
		 
	}
	
	//handles user input for simulation parameters
	private static void inputPrompt() {
	    // For the simulation length
	    do {
	        System.out.print("Enter simulation length: ");
	        while (!scanner.hasNextInt()) {
	            System.out.println("That's not a valid number!");
	            scanner.next(); 
	        }
	        simLength = scanner.nextInt();
	    } while (simLength <= 0); 

	    // For the time between dials
	    do {
	        System.out.print("Enter average time between dials: ");
	        while (!scanner.hasNextDouble()) {
	            System.out.println("That's not a valid number!");
	            scanner.next();
	        }
	        timeBetweenDial = scanner.nextDouble();
	    } while (timeBetweenDial < 0.0 && timeBetweenDial <= 1.0);

	    // For the average connection time
	    do {
	        System.out.print("Enter average connection time: ");
	        while (!scanner.hasNextInt()) {
	            System.out.println("That's not a valid number!");
	            scanner.next();
	        }
	        avgConnTime = scanner.nextInt();
	    } while (avgConnTime <= 0);

	    // For the number of modems
	    do {
	        System.out.print("Enter number of modems: ");
	        while (!scanner.hasNextInt()) {
	            System.out.println("That's not a valid number!");
	            scanner.next();
	        }
	        numModem = scanner.nextInt();
	    } while (numModem <= 0);

	    // For the waiting queue size
	    do {
	        System.out.print("Enter waiting queue size: ");
	        while (!scanner.hasNextInt()) {
	            System.out.println("That's not a valid number!");
	            scanner.next();
	        }
	        sizeWaitQueue = scanner.nextInt();
	    } while (sizeWaitQueue < 0 || sizeWaitQueue == -1);  
	}
	
	//Populates event queue with dial in population
	private static void initEQ() {

		for(int i = 0; i <= simLength; i++) {

			int numDials = dialInPoisson.nextInt();
			
			for(int j = 0; j < numDials; j++) {

				Event newEvent = new Event(Event.EventType.DIAL_IN, i, nextID);
				nextID++;
				eventQ.offer(newEvent);
				
			}
			
		}
		
	}
	
	//provides simulation logic and handles events and customers as simulation time progresses
	private static void simulationLoop() {

		int availModem = numModem;
		
		//loop manages simulations time value
		for(int i = 0; i < simLength; i++) {
			
			
			//incoming events are processed and passed to user queue
			while(eventQ.peek() != null && eventQ.peek().getEventTime() == i) { //

				
				Event currEvent = eventQ.poll();

				
				if(currEvent.getEventType() == Event.EventType.HANG_UP) {
					availModem++;
				}  if(currEvent.getEventType() == Event.EventType.DIAL_IN && userQ.size() < sizeWaitQueue || sizeWaitQueue == -1) {
					userQ.offer(currEvent);
					totalUser++;
				} else {
					System.out.println("Customer " + currEvent.getUserID() + " was denied service at time unit " + i);
					droppedCalls++;
				}
				
			}
			
			//user waiting queue processing
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
	
	//write report to text file
	private static void writeOut() {
	    try {
	        double averageWaitTime = cumulativeWaitingTime / totalUser;
	        double modemBusyPercentage = (cumulativeBusyTime / (numModem * simLength)) * 100;

	        fileOut.write(simLength + "	| " +
	                timeBetweenDial + " | " +
	                avgConnTime + " 	| " +
	                numModem + " 	| " +
	                sizeWaitQueue + " 	| " +
	                String.format("%.2f", averageWaitTime) + " | " +
	                String.format("%.2f", modemBusyPercentage) + " | " +
	                userQ.size() + "\n");
	        fileOut.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

}
