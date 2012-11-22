package driver;

import com.beust.jcommander.JCommander;
import com.google.inject.*;
import driver.server.Server;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        final Options opt = new Options();

        // parse CLI options
        new JCommander(opt, args);

        final List<Module> modules = new ArrayList<Module>();

        // depending on properties / CLI, load proper modules
        modules.add(opt.getNettyModule());

        // the injector
        final Injector injector = Guice.createInjector(modules);

        // the netty machine
        final Server server = injector.getInstance(Server.class);

        // so we can shutdown cleanly
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.err.println("JVM Shutdown received (e.g., Ctrl-c pressed)");

                server.stopAndWait();

                // shutdown other remote services here
            }
        });

        // start listening
        server.startAndWait();
    }
}