package misc;

public class Dbg {
    private static boolean enabled = false;
    private static long t0 = System.currentTimeMillis();

    public static void print (Object in) {
        if (enabled) {
            long t = (System.currentTimeMillis()-t0)/1000;
            Tools.println(t+": "+in);
        }
    }

}
