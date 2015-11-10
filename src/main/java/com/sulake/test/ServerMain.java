package com.sulake.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;

/**
 * @author Johno Crawford (johno@sulake.com)
 */
public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class);

    private Channel serverChannel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public ChannelFuture start(InetSocketAddress address) throws SSLException, CertificateException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        //bootstrap.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        //bootstrap.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey());
        final SslContext sslContext = sslContextBuilder.build();

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            final Charset UTF8 = Charset.forName("UTF-8");

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new SslMessageDecoder(sslContext));
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringDecoder(UTF8));
                pipeline.addLast(new StringEncoder(UTF8));
                pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
                        ctx.writeAndFlush("ACK " + s + "\n");
                        logger.info("Acknowledged: " + s);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        logger.error("Unexpected exception", cause);
                        ctx.close();
                    }
                });
            }
        });

        ChannelFuture future = bootstrap.bind(address);
        future.syncUninterruptibly();
        serverChannel = future.channel();

        logger.info("Server ready for connections!");

        return future;
    }

    public void destroy() {
        try {
            try {
                if (serverChannel != null) {
                    serverChannel.close().sync();
                }
            }
            finally {
                bossGroup.shutdownGracefully().sync();
                workerGroup.shutdownGracefully().sync();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException, SSLException, CertificateException {
        final ServerMain server = new ServerMain();
        ChannelFuture future = server.start(new InetSocketAddress(8888));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.destroy();
            }
        });
        future.channel().closeFuture().syncUninterruptibly();
    }
}
