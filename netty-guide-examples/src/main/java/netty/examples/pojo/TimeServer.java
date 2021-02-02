package netty.examples.pojo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author LQL
 * @since Create in 2021/2/1 21:07
 */
public class TimeServer {

    private int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
//        EventLoop即事件循环，可以理解为一个线程while true死循环处理事件
//        EventLoopGroup即线程池，拥有多个线程 不停循环处理事件
//        bossGroup 负责处理 建立连接的事件，一般需要的线程比较少
        EventLoopGroup bossGroup = new NioEventLoopGroup();
//        workerGroup是复制 处理 读写相关逻辑的，一般线程会设置多一些
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
//            ServerBootstrap是一个工具类，帮助设置各类配置
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
//                    不带child的配置就是对bossGroup的设置
//                    带child开头的就是对workerGroup的设置
//                    这里是指bossGroup使用 NioServerSocketChannel 来初始化channel处理连接事件
                    .channel(NioServerSocketChannel.class)
//                    这个是设置 socketChannel 的处理方案
//                    每来一个新的连接，都会 新建一个 channel、pipeline、n个handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            pipeline 是一个双向链表，入站顺序 和 出站顺序相反一个个调用
//                            每个连接都是重新new handler
                            ch.pipeline().addLast(new TimeEncoder(), new TimeServerHandler());
                        }
                    })
//                    可以指定一些参数配置
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//            绑定端口，设置监听
//            bind方法返回的是一个异步future，即该方法执行完后可能还没有绑定结束
//            使用sync可以将其阻塞住，等完成后再往下走
            final ChannelFuture bindFuture = serverBootstrap.bind(port).sync();
//            closeFuture 即获取 channel关闭的回调，sync会阻塞住
//            这里的意思就是阻塞住，直到通道关闭。否则主线程走到这就关闭了
            bindFuture.channel().closeFuture().sync();
        } finally {
//            优雅关闭，哪里优雅暂时也不懂，慢慢来...
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new TimeServer(8080).run();
    }
}
