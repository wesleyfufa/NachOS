package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
	static BoatGrader bg;

	public static void selfTest()
	{
		BoatGrader b = new BoatGrader();

		//System.out.println("\n ***Testing Boats with only 2 children***");
		//begin(0, 2, b);
		begin(1, 2, b);
		//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		//  	begin(1, 2, b);

		//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//  	begin(3, 3, b);
	}

	public static void begin( int adults, int children, BoatGrader b )
	{

		

		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here

		total_childs = children;
		total_adults = adults;
		OC = children;
		OA = adults;
		MC = 0;
		MA = 0;

		active = false;

		mutex = new Lock();
		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		//		Runnable r = new Runnable() {
		//			public void run() {
		//				SampleItinerary();
		//			}
		//		};
		//		KThread t = new KThread(r);
		//		t.setName("Sample Boat Thread");
		//		t.fork();
		
		System.out.println("OAHU BEFORE: " + OA + " : " + OC);
		System.out.println("Molokai BEFORE: " + MA + " : " + MC);

		Runnable a = new Runnable() {
			public void run() {
				AdultItinerary();
			}
		};

		Runnable c = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};
		

		KThread aa = new KThread(a);
		aa.setName("Adults Thread");

		
		
		
		KThread cc = new KThread(c);
		cc.setName("Childrens Thread");
		
		
		aa.fork();
		cc.fork();
		//aa.join();

		
		System.out.println("OAHU NOW: " + OA + " : " + OC);
		System.out.println("Molokai NOW: " + MA + " : " + MC);

	}

	static void AdultItinerary()
	{
		/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
		 */

		mutex.acquire();

		active = true;

		while(active) {

//			System.out.println("Adults at Molokai: " + MA);
//			System.out.println("Adults at Oahu: " + OA);
//			System.out.println("Children at Molokai: " + MC);
//			System.out.println("Children at Oahu: " + OC);

			if((OC + OA) > 0 && OA > 0 &&  OC > 0) {
				bg.ChildRowToMolokai(); //Child pilot, adult passenger.
				bg.ChildRideToMolokai();
				MC++; OA--; OC--;

				bg.ChildRowToOahu();
				MC--; OC++;


			}

			if(MA == total_adults) {
				active = false;
			}

		}


		mutex.release();

	}

	static void ChildItinerary()
	{

		//System.out.println("CHILD ITINERARY");

		mutex.acquire();

		active = true;
		while(active) {
//			System.out.println("Children at Molokai: " + MC);
//			System.out.println("Children at Oahu: " + OC);
			if(OC  > 0) {
				bg.ChildRowToMolokai();
				bg.ChildRideToMolokai();
				OC-=2; MC+=2; 
			}
			if(OC > 0) {
				bg.ChildRowToOahu();
				OC++; MC--; 
			}
			if(MC == total_childs) {
				active = false;
			}

		}


		mutex.release();
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}


	 static int  total_childs, total_adults, OC, OA, MC, MA;
	 static boolean active;
	 static Lock mutex;

}
