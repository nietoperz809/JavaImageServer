package misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NPExecutor implements Executor {
    private final ArrayBlockingQueue<Runnable> workList;
    private final ThreadGroup tg = new ThreadGroup (this.getClass ().getSimpleName ());
    private final AtomicBoolean shuttingDown = new AtomicBoolean (false);

    /**
     * Constructor
     *
     * @param count - number of threads spawned as executors
     * @param capacity - size of the task queue
     */
    public NPExecutor (int count, int capacity) {
        workList = new ArrayBlockingQueue<> (capacity);
        Runnable commonProcedure = () -> {
            try {
                while (true) {
                    workList.take ().run ();
                    if (shuttingDown.get())
                        break;
                    Thread.yield ();
                }
            } catch (Throwable e) {
                //System.out.println (e + " -- " + Thread.currentThread ().getName ());
            }
        };
        while (count-- > 0) {
            new Thread (tg, commonProcedure, "Npe: " + count).start ();
        }
        tg.setDaemon (true);
    }

    /**
     * "Destructor"
     * Kill all threads.
     * First send a group-wide InterruptedException
     * Then fill up the queue with StopExceptions until every thread gets one.
     *
     * @return List of tasks that didn't yet execute.
     */
    public ArrayList<Runnable> dispose () {
        ArrayList<Runnable> list = cancel ();
        shuttingDown.set (true);
        tg.interrupt ();
        do {
            Thread.yield ();
        } while (tg.activeCount () > 0);
        return list;
    }

    /**
     * Get active Threads
     * @return Array of active threads
     */
    public Thread[] getThreads() {
        Thread[] threads = new Thread[tg.activeCount ()];
        tg.enumerate (threads);
        return threads;
    }

    /**
     * Remove all yet unprocessed tasks from the workList.
     * Threads remain active, new tasks can be submitted.
     *
     * @return List of tasks that didn't yet execute.
     */
    public ArrayList<Runnable> cancel () {
        ArrayList<Runnable> list = new ArrayList<> ();
        workList.drainTo (list);
        return list;
    }

    /**
     * Get number of active Threads of this Executor
     * @return Unusually this is the same number given in the constructor
     */
    public int activeThreads() {
        return tg.activeCount ();
    }

    /**
     * Put a new task in the work queue
     *
     * @param r - the new task
     * @throws RejectedExecutionException - if this executor won't accept new tasks
     */
    @Override
    public void execute (Runnable r) throws RejectedExecutionException {
        if (shuttingDown.get ()) {
            throw new RejectedExecutionException ("Executor shutting down");
        } else {
            if (!workList.offer (r))
                throw new RejectedExecutionException ("Rejected by queue");
        }
    }

    /**
     * Forcefully murders a thread
     * @param t - Thread to be killed
     * @throws NoSuchMethodException If hidden method "stop0" doesn't exist anymore
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static void forceDown (Thread t)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = Thread.class.getDeclaredMethod("stop0", Object.class);
        m.setAccessible(true);
        m.invoke(t, new Throwable ("murdered!"));
        m.setAccessible(false);
    }

    /**
     * Shut down all threads the hard and ugly way
     * @return Arraylist of tasks that couldn't be executed
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ArrayList<Runnable> forceDown()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Thread[] threads = getThreads ();
        for (Thread t : threads) {
            forceDown (t);
        }
        return cancel();
    }
}

