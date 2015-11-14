# ssl-server-test

Sample project for debugging SSL handshake with Unity Android client. n.b. Initial message must be at least five bytes for the SSL message decoder.

```
nioEventLoopGroup-3-1, WRITE: TLSv1 Handshake, length = 518
nioEventLoopGroup-3-1, READ: TLSv1 Alert, length = 2
nioEventLoopGroup-3-1, RECV TLSv1 ALERT:  warning, internal_error
SSL -- handshake alert:  internal_error
nioEventLoopGroup-3-1, fatal error: 80: problem unwrapping net record
javax.net.ssl.SSLProtocolException: handshake alert: internal_error
%% Invalidated:  [Session-1, TLS_RSA_WITH_AES_128_CBC_SHA]
nioEventLoopGroup-3-1, SEND TLSv1 ALERT:  fatal, description = internal_error
nioEventLoopGroup-3-1, WRITE: TLSv1 Alert, length = 2
nioEventLoopGroup-3-1, called closeOutbound()
nioEventLoopGroup-3-1, closeOutboundInternal()
nioEventLoopGroup-3-1, called closeInbound()
nioEventLoopGroup-3-1, fatal: engine already closed.  Rethrowing javax.net.ssl.SSLException: Inbound closed before receiving peer's close_notify: possible truncation attack?
2015-11-09 16:47:46,619 ERROR [nioEventLoopGroup-3-1] omitted: unexpected exception 
io.netty.handler.codec.DecoderException: javax.net.ssl.SSLProtocolException: handshake alert: internal_error
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:380)
    at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:244)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:308)
    at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:294)
    at io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:266)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:308)
    at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:294)
    at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:846)
    at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:131)
    at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:511)
    at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:468)
    at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:382)
    at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:354)
    at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:112)
    at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.java:137)
    at java.lang.Thread.run(Thread.java:745)
Caused by: javax.net.ssl.SSLProtocolException: handshake alert: internal_error
    at sun.security.ssl.ServerHandshaker.handshakeAlert(ServerHandshaker.java:1757)
    at sun.security.ssl.SSLEngineImpl.recvAlert(SSLEngineImpl.java:1771)
    at sun.security.ssl.SSLEngineImpl.readRecord(SSLEngineImpl.java:1075)
    at sun.security.ssl.SSLEngineImpl.readNetRecord(SSLEngineImpl.java:901)
    at sun.security.ssl.SSLEngineImpl.unwrap(SSLEngineImpl.java:775)
    at javax.net.ssl.SSLEngine.unwrap(SSLEngine.java:624)
    at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1138)
    at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1028)
    at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:968)
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:349)
    ... 15 more
nioEventLoopGroup-3-1, called closeOutbound()
nioEventLoopGroup-3-1, closeOutboundInternal()
nioEventLoopGroup-3-1, called closeInbound()
nioEventLoopGroup-3-1, closeInboundInternal()
```

Turns out there was an undocumented exception being thrown in the SSL authenticated callback which was being swallowed..

11-11 01:12:50.858 25012 25139 I Unity : NullReferenceException: Object reference not set to an instance of an object
11-11 01:12:50.858 25012 25139 I Unity : at Mono.Security.X509.X509Certificate.get_Hash () [0x00000] in :0
11-11 01:12:50.858 25012 25139 I Unity : at Mono.Security.X509.X509Certificate.VerifySignature (System.Security.Cryptography.RSA rsa) [0x00000] in :0
11-11 01:12:50.858 25012 25139 I Unity : at Mono.Security.X509.X509Certificate.VerifySignature (System.Security.Cryptography.AsymmetricAlgorithm aa) [0x00000] in :
011-11 01:12:50.858 25012 25139 I Unity : at System.Security.Cryptography.X509Certificates.X509Chain.IsSignedWith (System.Security.Cryptography.X509Certificates.X509Certificate2 signed, System.Security.Cryptography.AsymmetricAlgorithm pubkey) [0x00000] in :
0
11-11 01:12:50.858 25012 25139 I Unity : at System.Security.Cryptography.X509Certificates.X509Chain.Process (Int32 n) [0x00000] in :0
11-11 01:12:50.858 25012 25139 I Unity : at System.Security.Cryptography.X509Certificates.X509Chain.ValidateChain (X509ChainStatusFlags flag) [0x00000] in :0
11-11 01:12:50.858 25012 25139 I Unity : at System.Security.Cryptography.X509Certificates.X509Chain.Build (System.Security.Cryptograp

This is caused by "Strip Bytecode" in Unity publish settings and only affects Android. The fix was to add the following in link.xml.

```
<assembly fullname="mscorlib">
       <namespace fullname="System.Security.Cryptography" preserve="all"/>
</assembly>
```
