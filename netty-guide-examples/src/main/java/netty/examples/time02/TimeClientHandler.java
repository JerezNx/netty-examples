package netty.examples.time02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * @author LQL
 * @since Create in 2021/2/2 21:09
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 我们维护一个缓存buf
     */
    private ByteBuf buf;

    /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and it's ready to handle events.
     * 简单说就是当前handler初始化完成后调用
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        这里需要注意，ByteBuf是会自动扩容的，只是指定初始容量
        buf = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buf.release();
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf m = (ByteBuf) msg;
//        将接收到的数据写到我们的缓冲中
        buf.writeBytes(m);
        m.release();
//        我们的数据包约定的是一个4字节的int，所以这边判断数据够没够4字节
        if (buf.readableBytes() >= 4) {
            final long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
