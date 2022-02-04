package misc;

import java.util.concurrent.ArrayBlockingQueue;

public class MyExecutor3 extends ThreadGroup {
    private ArrayBlockingQueue<Runnable> workList = new ArrayBlockingQueue<> (1000);
    private Runnable proc = () -> {
        try {
            while (true) {
                Runnable r = workList.take ();
                r.run ();
                Thread.sleep (100);
            }
        } catch (Exception e) {
            return;
        }
    };

    public synchronized void execute (Runnable r) {
        workList.add (r);
    }

    public MyExecutor3 (int count) {
        super ("TG-MyExecutor2");
        for (int s = 0; s < count; s++) {
            new Thread (this, proc).start();
        }
    }

    ///////////////////////////////////////////////////////
    public static void main (String[] args) throws InterruptedException {
        MyExecutor3 ex = new MyExecutor3 (10);

        ex.execute (() -> {
            System.out.println ("hello");
        });

        Thread.sleep (2000);

        ex.interrupt ();
    }
}
