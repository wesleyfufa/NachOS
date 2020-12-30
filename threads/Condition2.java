package nachos.threads;



import java.util.LinkedList;



import nachos.machine.*;



/**

 * An implementation of condition variables that disables interrupt()s for

 * synchronization.

 *

 * <p>

 * You must implement this.

 *

 * @see	nachos.threads.Condition

 */

public class Condition2 {

	/**

	 * Allocate a new condition variable.

	 *

	 * @param	conditionLock	the lock associated with this condition

	 *				variable. The current thread must hold this

	 *				lock whenever it uses <tt>sleep()</tt>,

	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.

	 */

	public Condition2(Lock conditionLock) {

		this.conditionLock = conditionLock;

	}



	/**

	 * Atomically release the associated lock and go to sleep on this condition

	 * variable until another thread wakes it using <tt>wake()</tt>. The

	 * current thread must hold the associated lock. The thread will

	 * automatically re-acquire the lock before <tt>sleep()</tt> returns.

	 */

	public void sleep() {

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		Machine.interrupt().disable(); //interrupt disabled

		conditionLock.release(); //release the lock since the condition variables allows a thread to sleep with the lock

		//taken from semaphore p

		waitQueue.waitForAccess(KThread.currentThread());

		KThread.sleep(); //current thread to sleep.

		Machine.interrupt().enable(); //interrupt enabled

		conditionLock.acquire(); //Acquire the lock when it is done sleeping.

	}



	/**

	 * Wake up at most one thread sleeping on this condition variable. The

	 * current thread must hold the associated lock.

	 */

	public void wake() {

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		Machine.interrupt().disable();


		//taken from semaphore v

		KThread thread = waitQueue.nextThread();

		if (thread != null){ //Check if there was actually a thread returned

			thread.ready(); //Ready that thread.

		}

		Machine.interrupt().enable();

	}



	/**

	 * Wake up all threads sleeping on this condition variable. The current

	 * thread must hold the associated lock.

	 */

	public void wakeAll() {

		Lib.assertTrue(conditionLock.isHeldByCurrentThread());



		Machine.interrupt().disable();

		// This needed fixing, wasn't waking up properly 
		// waitQueue.nextThread() i believe removes the thread 
		// from the queue so using it here in a while loop then again 
		// on wake() caused threads to be missed. LinkedList has isEmpty()
		// like how Condition.java has it.

		//taken from condition.java
		KThread thread = waitQueue.nextThread(); //Get the next thread

		while (thread != null){ //sanity check before and iterate through each 
			thread.ready(); //Not null, ready it
			thread = waitQueue.nextThread(); //Continue with the loop

		}



		Machine.interrupt().enable();

	}



	// Place Condition2 test code inside of the Condition2 class.



	// Test programs should have exactly the same behavior with the

	// Condition and Condition2 classes.  You can first try a test with

	// Condition, which is already provided for you, and then try it

	// with Condition2, which you are implementing, and compare their

	// behavior.



	// Do not use this test program as your first Condition2 test.

	// First test it with more basic test programs to verify specific

	// functionality.





	public static void cvTest5() {

		final Lock lock = new Lock();

		// final Condition empty = new Condition(lock);

		final Condition2 empty = new Condition2(lock);

		final LinkedList<Integer> list = new LinkedList<Integer>();



		KThread consumer = new KThread( new Runnable () {

			public void run() {

				lock.acquire();

				while(list.isEmpty()){

					empty.sleep();

				}


				Lib.assertTrue(list.size() == 5, "List should have 5 values.");

				while(!list.isEmpty()) {

					// context swith for the fun of it

					KThread.currentThread().yield();

					System.out.println("Removed " + list.removeFirst());

				}

				lock.release();

			}

		});



		KThread producer = new KThread( new Runnable () {

			public void run() {

				lock.acquire();

				for (int i = 0; i < 5; i++) {

					list.add(i);

					System.out.println("Added " + i);

					// context swith for the fun of it

					KThread.currentThread().yield();

				}

				empty.wake();

				lock.release();

			}

		});



		consumer.setName("Consumer");

		producer.setName("Producer");

		consumer.fork();

		producer.fork();



		// We need to wait for the consumer and producer to finish,

		// and the proper way to do so is to join on them.  For this

		// to work, join must be implemented.  If you have not

		// implemented join yet, then comment out the calls to join

		// and instead uncomment the loop with yield; the loop has the

		// same effect, but is a kludgy way to do it.

		consumer.join();

		producer.join();

		//for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }

	}


	private Lock conditionLock;

	private ThreadQueue waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);

}