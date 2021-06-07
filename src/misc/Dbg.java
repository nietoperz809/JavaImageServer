package misc;

public class Dbg {
    private static boolean enabled = true;

//    public static void print (String in)
//    {
//        if (enabled)
//            System.out.println(in);
//    }

    public static void print (Object in)
    {
        if (enabled)
            System.out.println(in);
    }

}
