package misc;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static misc.Tools.getPathOfClass;

/**
 * A line of the config file is organised as following:
 * keyword :: p1 :: p2 :: p3 ... pn
 *
 * :: is the delimiter
 * The arguments p1 ... pn are optional
 * Whitespaces are ignored
 * Lines that do not start with a known keyword are ignored
 * Blank lines are also ignored
 *
 * The usage is:
 *         ConfigFile cf = new ConfigFile("file_name_on_disk");
 *         cf.setAction("any_string", strings -> Command(strings));
 *         cf.setAction("other_string", strings -> Command(strings));
 *         ...
 *         cf.execute(); // Parse file and execute commands
 *
 * Commands are functions/methods that take a single String[] as argument
 * 
 */
public class ConfigFile
{
    private final String _filename;
    private String _filepath;
    private final HashMap<String, Consumer<String[]>> _map = new HashMap<>();

    /**
     * Constructor
     * @param fileName name of config file
     */
    public ConfigFile (String fileName)
    {
        _filepath = getPathOfClass (this.getClass()) + fileName;
        _filename = fileName;
    }

    /**
     * Submit a new action command
     * @param name Keyword
     * @param r Handler function
     */
    public void setAction (String name, Consumer<String[]> r)
    {
        _map.put (name, r);
    }

    /**
     * Parse line of config file and execute command
     * @param line the line
     */
    private void handleLine (String line)
    {
        line = line.replaceAll("\\s+","");
        String[] splits = line.split("::");
        Consumer<String[]> func = _map.get(splits[0]);
        if (func != null)
        {
            String[] args = new String[splits.length-1];
            System.arraycopy(splits, 1, args, 0, args.length);
            func.accept(args);
        }
    }

    /**
     * Parse whole config file and execute it line by line
     */
    public void execute() throws Exception {
        Stream<String> stream;
        try {
            System.out.println("trying "+ _filepath);
            stream = Files.lines(Paths.get(_filepath));
        } catch (Exception e) {
            System.out.print("first chance failed ... ");
            System.out.println("trying "+_filename);
            stream = Files.lines(Paths.get(_filename));
            _filepath = _filename;
        }
        stream.forEach(this::handleLine);
    }

    public String getUsedFilePath()
    {
        return _filepath;
    }
}
