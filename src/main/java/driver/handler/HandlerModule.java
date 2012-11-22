package driver.handler;

import com.google.inject.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpServerCodec;

/**
 * This is where you configure your handlers and the pipeline
 */
public class HandlerModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        // bind to our handler
        bind(ChannelHandler.class).to(Handler.class);
    }

    @Provides
    @Inject
    ChannelPipeline providePipeline(ChannelHandler handler)
    {
        // construct empty pipeline
        final ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("chunk-aggregator", new HttpChunkAggregator(1024 * 1024 * 2));
        pipeline.addLast("resource", handler);

        return pipeline;
    }
}
