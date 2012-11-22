package driver.server;

import com.google.inject.*;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

/**
 *
 */
public class ServerModule extends AbstractModule
{
    final int port;

    public ServerModule(int port)
    {
        this.port = port;
    }

    @Override
    protected void configure()
    {
        bind(SocketAddress.class).toInstance(new InetSocketAddress(port));

        // bind to our handler
        bind(ChannelHandler.class).to(Handler.class);
    }

    @Provides @Inject
    ChannelPipeline providePipeline(ChannelHandler handler)
    {
        // construct empty pipeline
        final ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("chunk-aggregator", new HttpChunkAggregator(1024 * 1024 * 2));
        pipeline.addLast("resource", handler);

        return pipeline;
    }

    @Provides @Inject @Singleton
    ServerBootstrap provideServerBootstrap(ChannelFactory channelFactory, Provider<ChannelPipeline> pipelineProvider)
    {
        final ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

        // adapt a Provider<ChannelPipeline> into a ChannelPipelineFactory
        bootstrap.setPipelineFactory(new ProviderPipelineFactory(pipelineProvider));

        bootstrap.setOption("connectTimeoutMillis", 2000);
        bootstrap.setOption("backlog", 1000);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        return bootstrap;
    }

    @Provides @Inject @Singleton
    ChannelFactory provideChannelFactory()
    {
        // NIO channels
        return new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
        );
    }

    // provider -> factory
    static class ProviderPipelineFactory implements ChannelPipelineFactory
    {
        final Provider<ChannelPipeline> pipeline;

        public ProviderPipelineFactory(Provider<ChannelPipeline> pipeline)
        {
            this.pipeline = pipeline;
        }

        public ChannelPipeline getPipeline() throws Exception
        {
            return pipeline.get();
        }
    }
}