package netty.examples.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author LQL
 * @since Create in 2021/2/2 22:31
 */
public class TimeEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        UnixTime time = (UnixTime) msg;
        final ByteBuf encodedData = ctx.alloc().buffer(4);
//        todo 弄明白为毛这边直接写的int，但解码那边是读的 无符号Int
        encodedData.writeInt((int) time.value());
//        需要将原本的promise传过去，netty会用他标记是否成功
        ctx.write(encodedData, promise);
        /**
         * 这边不需要调用 ctx.flush(),因为在{@link ChannelOutboundHandlerAdapter#flush(io.netty.channel.ChannelHandlerContext)}中有，
         * 会由netty进行调用
         */
    }


}
