package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		mutex = new Lock();
		speakers = new Condition2(mutex); //all conditions need the same lock
		listeners = new Condition2(mutex);
		syncedThreads = new Condition2(mutex);
		currWord = 0;
		active = false;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */
	public void speak(int word) {
		mutex.acquire(); //1. acquire the lock

		//Monitors
		while(active) { //2. Is there an active speaker first? Go to sleep
			speakers.sleep();
		}
		active = true; //3a. No speakers activated, it is active now

		currWord = word; //3b. global variable must hold the current word.
		listeners.wake(); //3c. wake up a waiting listener.
		syncedThreads.sleep(); //4. sleep until a listener wakes it up.
		mutex.release();
	}


	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() {
		mutex.acquire(); //1. acquire the lock

		//Monitors
		while(!active) { //2. Is there a speaker? No? Put listener to sleep and have a speaker wake up a listener
			listeners.sleep();
		}

		int temp_word = currWord; //3a.temporary word in case of context switch.
		active = false; //3b. Word is received and stored locally. Speaker has done its job.

		
		speakers.wake(); //3c. wake up a sleeping speaker for multiple speakers.
		syncedThreads.wake(); //4. wake up the corresponding speaker to finish.
		
		mutex.release();
		
		return temp_word; //return the temporary word.
	}

	private Lock mutex;
	private Condition2 speakers, listeners, syncedThreads;

	private boolean active;
	private int currWord;


}
