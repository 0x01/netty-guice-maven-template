package driver;

import com.beust.jcommander.Parameter;
import com.google.inject.AbstractModule;
import driver.server.ServerModule;

import java.util.Properties;

public class Options
{
    @Parameter(names = {"-p", "--port"}, description = "Listening port")
    private int port;

    @Parameter(names = "--help", help = true)
    private boolean help;

    public Options()
    {
        this(System.getProperties());
    }

    /**
     * You can pass the system properties here, this will then be used as defaults that can be overridden using the CLI.
     *
     * @param properties
     */
    public Options(Properties properties)
    {
        // try to set the defaults for local
        port = Integer.parseInt(properties.getProperty("server.port", "9821"));
    }

    public AbstractModule getNettyModule()
    {
        return new ServerModule(port);
    }
}
