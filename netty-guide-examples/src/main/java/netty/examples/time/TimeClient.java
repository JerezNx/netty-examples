package netty.examples.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author LQL
 * @since Create in 2021/2/1 22:13
 */
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        String host = "localhost";
        int port = 8080;
//        客户端只需要和一个服务端对接，所以只需要一个worker即可
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
//            对标ServerBootstrap,这里只能设置客户端的配置
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
//                    客户端使用 NioSocketChannel
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            接收到消息后的实际处理者
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);
//            connect也是一个异步操作
            final ChannelFuture connectFuture = bootstrap.connect(host, port).sync();
            connectFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
