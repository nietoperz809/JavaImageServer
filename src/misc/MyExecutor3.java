package misc;

import java.util.concurrent.ArrayBlockingQueue;

public class MyExecutor3 {
    private final ArrayBlockingQueue<Runnable> workList = new ArrayBlockingQueue<> (1000);
    private final ThreadGroup tg = new ThreadGroup("TG-executor.MyExecutor3");

    public MyExecutor3 (int count) {
        Runnable proc = () -> {
            try {
                while (true) {
                    Runnable r = workList.take ();
                    r.run ();
                    Thread.yield ();
                }
            } catch (Exception e) {
                System.out.println ("break");
            }
        };
        for (int s = 0; s < count; s++) {
            new Thread (tg, proc, "Ex3:"+s).start ();
        }
        tg.setDaemon (true);
    }

    public void dispose() {
        tg.interrupt ();
        workList.clear();
        while (tg.activeCount () != 0) {
        workList.offer (() -> {
            int a = 1/0;
            });
        }
    }

    public void execute (Runnable r) {
        workList.add (r);
    }
}

