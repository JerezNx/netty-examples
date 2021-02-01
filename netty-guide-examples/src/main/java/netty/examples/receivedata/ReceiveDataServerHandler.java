package netty.examples.receivedata;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

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
public class ReceiveDataServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当有数据读事件时触发，这里的数据是 ${@link ByteBuf}
     */
    @Override
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
