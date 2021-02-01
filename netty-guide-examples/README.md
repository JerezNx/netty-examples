## 一、概述
官方入门教程： https://netty.io/wiki/user-guide-for-4.x.html#wiki-h3-16  

跟着走一遍，对netty有个大概的认识。第一遍最好直接复制代码，跑起来看看效果。后面再手敲一遍。
## 二、入门
### 2.1 最简实现-Discard Server
即服务端接收到数据后什么都不做，自然是最简单。

- 新建一个handler:
```
package netty.examples.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 实现了 {@link ChannelInboundHandlerAdapter},该接口提供了各种方法处理各个事件
 * 我们可以选择需要处理的事件，进行方法重写
 * <p>
 * java各类框架喜欢用抽象的适配器类给各个方法一个空实现，这样我们可以只注重部分方法的重写，类型WebMvcConfigurator
 * 不过后来jdk8接口有默认方法后就不是很需要了。当然这里的适配器还提供了一些代码，暂未细看
 *
 * @author LQL
 * @since Create in 2021/2/1 20:56
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当有数据读事件时触发，这里的数据是 ${@link ByteBuf}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf 是所谓的  reference-counted 对象
//        需要显示的调用 release方法
        ((ByteBuf) msg).release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}

```
- 新建服务端类：
```
package netty.examples.discard;

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
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
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
                            ch.pipeline().addLast(new DiscardServerHandler());
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
        new DiscardServer(8080).run();
    }
}
```

- 启动测试验证
启动后，在handler的`channelRead`中打断点，然后 `telnet localhost 8080`，发送数据，会发现进入断点，即最简实现成功。


### 2.2 实现读取数据
上面虽然断点判断最简实现成功了，但总不得劲，现在修改handler的read方法，读取数据：

```
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf in = (ByteBuf) msg;
        try {
//            buf数组中还有可读的数据
            while (in.isReadable()) {
                System.out.print((char) in.readByte());
                System.out.flush();
            }
//            上面循环意义即将接收到的byte数组每个byte打印，可简化为：
            System.out.println(in.toString(CharsetUtil.US_ASCII));
//            byteBuf可以方便的指定编码转换为 string
//            底层肯定是取到byte数组后，new String
        } finally {
            in.release();
//            也可以用下面的方式：
            ReferenceCountUtil.release(msg);
        }
    }
```
再次启动server，这时候telnet后，服务端控制台输出了接收到的消息。


### 2.3 实现 EchoServer (输出消息)
上面可以服务端可以接收客户端发送的消息了，下面实现服务端往客户端发送消息，修改 handler的 read方法：
```
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        将接收到的消息逐字返回
//        这里不要release，netty在发送完后会帮我们处理
        ctx.write(msg);
//        上面write只是写到了缓存中，需要flush才会正式io发送
        ctx.flush();
}
```
再次启动server，这时候telnet后，telnet每发送消息，就会接收到同样的内容。

### 2.4 实现 TimeServer 
现在实现一个时间服务器，当客户端与服务端建立连接后，服务端把当前时间戳用一个4字节的int数值发送给客户端，发送完成后就断开连接。  
要实现建立连接事件的处理，在handler中重写 channelActive 方法：
```
 /**
     * channelActive 会在连接建立完成后触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        发送一个int数据，需要4个字节
//        ctx.alloc() 返回的是一个ByteBufAllocator，字面意思，就是用来分配byteBuf的
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//        ByteBuf 是netty封装的对象，其内部维护了读写两个独立的指针
//        因此写完数据后不需要flip
        final ChannelFuture writeFuture = ctx.writeAndFlush(time);
//        发送数据返回的是一个future，即该操作是异步的，到这一行该数据很可能还没发送完成
//        所以可以添加一个回调，执行发送完成后的操作
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert future == writeFuture;
//                发送完成后，关闭连接
                ctx.close();
            }
        });
//        像上面操作完后就关闭连接，可以使用netty提供的静态变量
        writeFuture.addListener(ChannelFutureListener.CLOSE);
    }
```
启动服务端，使用linux命令测试：
```
rdate -o <port> -p <host>
```
### 2.5 实现 TimeClient 

- 新建 TimeClientHandler：
```
package netty.examples.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * @author LQL
 * @since Create in 2021/2/1 22:09
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf m = (ByteBuf) msg;
        try {
            final long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

- 新建 TimeClient：
```
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

```

- 启动测试：
先启动server，再启动client，会发现client打印了时间。

### 2.6 解决粘包问题
todo