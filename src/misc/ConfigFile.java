package misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ConfigFile
{
    private final String _path;
    private ArrayList<Action> _list = new ArrayList<>();

    public ConfigFile (String path)
    {
        _path = path;
    }

    private class Action
    {
        String name;
        Consumer<String[]> function;

        Action (String n, Consumer<String[]> r)
        {
            name = n;
            function = r;
        }
    }

    public void setAction (String name, Consumer<String[]> r)
    {
        _list.add(new Action (name, r));
    }

    private void handleLine (String line)
    {
        line = line.replaceAll("\\s+","");
        String[] splits = line.split("::");
        for (Action a : _list)
        {
            if (splits[0].equals(a.name))
            {
                String[] args = new String[splits.length-1];
                System.arraycopy(splits, 1, args, 0, args.length);
                a.function.accept(args);
            }
        }
    }

    public void execute()
    {
        try (Stream<String> stream = Files.lines(Paths.get(_path)))
        {
            stream.forEach(this::handleLine);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

//    public static void main (String[] args)
//    {
//        ConfigFile cf = new ConfigFile("serversettings.txt");
//        cf.setAction("hello", strings -> System.out.println(Arrays.toString(strings)));
//        cf.setAction("world", strings -> System.out.println(Arrays.toString(strings)));
//        cf.execute();
//    }
}
