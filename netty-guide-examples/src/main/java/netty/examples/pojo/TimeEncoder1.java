package netty.examples.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 简写方法，泛型中是上一个handler输出的类型
 *
 * @author LQL
 * @since Create in 2021/2/2 22:38
 */
public class TimeEncoder1 extends MessageToByteEncoder<UnixTime> {
    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) throws Exception {
        out.writeInt((int) msg.value());
    }
}
