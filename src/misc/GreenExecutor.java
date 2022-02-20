package misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class GreenExecutor implements Executor {
    AtomicInteger max = new AtomicInteger ();
    List<Runnable> dlist = Collections.synchronizedList(new ArrayList<Runnable> ());
    public GreenExecutor (int max) {
        this.max.set (max);
    }

    @Override
    public void execute (Runnable command) {
        CompletableFuture.runAsync (() -> {
            if (max.get () == 0) {
                dlist.add (command);
                return;
            }
            max.decrementAndGet ();
            command.run ();
            Thread.yield ();
            max.incrementAndGet();
            while (!dlist.isEmpty ()) {
                dlist.remove (0).run ();
                Thread.yield ();
            }
        });
    }

//    public static void main (String[] args) {
//        GreenExecutor gr = new GreenExecutor (5);
//
//        for (int s=0; s<500; s++) {
//            final int ss = s;
//            gr.execute (() -> System.out.println (ss + " " + Thread.currentThread ().getId ()));
//        }
//        System.out.println ("remaining: "+gr.dlist.size ());
//    }
}
