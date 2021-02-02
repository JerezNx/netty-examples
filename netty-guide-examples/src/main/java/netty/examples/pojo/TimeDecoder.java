package netty.examples.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author LQL
 * @since Create in 2021/2/2 21:25
 */
public class TimeDecoder extends ByteToMessageDecoder {

    /**
     * 每当接收到新的数据时，就会调用该方法
     *
     * @param ctx c
     * @param in  其内部维护的一个累积的缓冲buf
     * @param out 其内部维护的编码后的数据包的List
     * @throws Exception e
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        out.add(new UnixTime(in.readUnsignedInt()));
    }

}
