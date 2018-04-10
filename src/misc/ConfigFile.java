package misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A config file is organised as following:
 * keyword :: p1 :: p2 :: p3 ... pn
 * The arguments p1 ... pn are optional
 *
 * The usage is:
 *         ConfigFile cf = new ConfigFile("file_name_on_disk");
 *         cf.setAction("any_string", strings -> Command(strings));
 *         cf.setAction("other_string", strings -> Command(strings));
 *         ...
 *         cf.execute(); // Parse file and execute Commands
 *
 * Commands are functions/methods that take a single String[] as argument
 * 
 */
public class ConfigFile
{
    private final String _path;
    private final ArrayList<Action> _list = new ArrayList<>();

    /**
     * Constructor
     * @param path set file path
     */
    public ConfigFile (String path)
    {
        _path = path;
    }

    /**
     * Action element
     */
    private class Action
    {
        final String name;
        final Consumer<String[]> function;

        Action (String n, Consumer<String[]> r)
        {
            name = n;
            function = r;
        }
    }

    /**
     * Submit a new action command
     * @param name Keyword
     * @param r Handler function
     */
    public void setAction (String name, Consumer<String[]> r)
    {
        _list.add(new Action (name, r));
    }

    /**
     * Parse line of config file and execute command
     * @param line the line
     */
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

    /**
     * Parse whole config file and execute it line by line
     */
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
}
