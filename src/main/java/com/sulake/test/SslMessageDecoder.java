package com.sulake.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.List;

/**
 * Manipulates the current pipeline dynamically to enable SSL if detected.
 *
 * @author Johno Crawford (johno@sulake.com)
 */
public class SslMessageDecoder extends ByteToMessageDecoder {

    private final SslContext sslContext;

    public SslMessageDecoder(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        if (SslHandler.isEncrypted(in)) {
            enableSsl(context);
        } else {
            context.pipeline().remove(this);
        }
    }

    private void enableSsl(ChannelHandlerContext context) {
        ChannelPipeline pipeline = context.pipeline();
        pipeline.replace(this, SslHandler.class.getName(), sslContext.newHandler(context.alloc()));
    }

    @Override
    public boolean isSingleDecode() {
        // ByteToMessageDecoder uses this method to optionally break out of the decoding loop after each unit of work.
        // Since we only ever want to decode a single header we always return true to save a bit of work here.
        return true;
    }
}

