package nachos.threads;

import nachos.machine.*;
import java.util.TreeSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */

public class Alarm {
	// Add Alarm testing code to the Alarm class
	public static void alarmTest1() {
	int durations[] = {1000, 10*1000, 100*1000};
	long t0, t1;
	for (int d : durations) {
	    t0 = Machine.timer().getTime();
	    ThreadedKernel.alarm.waitUntil (d);
	    t1 = Machine.timer().getTime();
	    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
	}
    }
    // Implement more test methods here ...
    // Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
    public static void selfTest() {
	alarmTest1();
	// Invoke your other test methods here ...
    }

	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	static class Pair implements Comparable<Pair>{
		private KThread thread;	
		private Long wakeTime;
		
		//Pair Constructor
		public Pair(KThread newThread, Long wt){ 
			this.thread = newThread;
			this.wakeTime = wt;
		}
	
		//For the TreeSet ordering, lowest to highest wakeTime.
		@Override
		public int compareTo(Pair newThread) {
			// TODO Auto-generated method stub
			if(this.wakeTime > newThread.wakeTime) {
				return 1;
			}
			else if(this.wakeTime < newThread.wakeTime) {
				return -1;
			}

			return 0;
		}
	}
	
	//TreeSet global variable.
	TreeSet <Pair> theThreads = new TreeSet<Pair>(); // combine thread and time


	public Alarm() {

		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {

		Machine.interrupt().disable();

		//while loop the threads in the TreeSet
		while(!theThreads.isEmpty()) {
			Pair curr_thread = theThreads.first(); //Fetch the first (lowest wakeTime) on the list

			if(curr_thread.wakeTime < Machine.timer().getTime()) { //Is the wakeTime for that thread < current time
				theThreads.pollFirst(); //Remove it from the TreeSet, curr_thread holds it still.
				curr_thread.thread.ready(); //Ready that thread
			}
			else {
				break; //break the loop, not ready yet.
			}
		}
		Machine.interrupt().enable();
		KThread.yield(); //Yield the current thread.
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {

		Machine.interrupt().disable();
		//Create the pair object with current thread and its respective wake time.
		Pair newPair = new Pair(KThread.currentThread(), Machine.timer().getTime() + x);

		//Initialize with the current thread and wakeTime
		theThreads.add(newPair); //add to list
		
		KThread.sleep(); //put that thread to sleep....its waiting
		Machine.interrupt().enable();


	}
}