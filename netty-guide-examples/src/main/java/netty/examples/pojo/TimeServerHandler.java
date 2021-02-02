package netty.examples.pojo;

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
        final ChannelFuture writeFuture = ctx.writeAndFlush(new UnixTime());
//       像上面操作完后就关闭连接，可以使用netty提供的静态变量
        writeFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
