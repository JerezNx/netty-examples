package netty.examples.time02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * {@link ByteToMessageDecoder}是继承自{@link io.netty.channel.ChannelInboundHandlerAdapter}的一个抽象类
 * 按其字面意思，是用于将byte按指定规则编码为消息包
 *
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
//            不足以构造一个数据包时直接返回，因为这时候其实已经将新接收的数据和之前累积的数据一起放在byteBuf中了
            return;
        }
//        足够时就编码并添加到list中
//        需要注意的是，此处并不需要一次性处理多个，比如说判断buf中有8个，然后解出2个包
//        因为解码成功后，会继续再次调用 decode 方法，直到没有添加任何包到list中
        out.add(in.readBytes(4));
    }

}
