package netty.examples.time01;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
