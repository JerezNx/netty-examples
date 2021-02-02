package netty.examples.time03;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * {@link ReplayingDecoder}是继承自{@link ByteToMessageDecoder}的一个抽象类
 * 按其字面意思看不出太多内涵，重播是个什么鬼
 * 阅读官方api文档： https://netty.io/4.1/api/io/netty/handler/codec/ReplayingDecoder.html
 * 其描述使用该类时，我们可以不用判断是否满足解码为一个包的条件，直接解码，便于开发。
 * 其原理如下：拿下面的 readBytes(4)举例，如果数据不够时，就会抛出异常，
 * 而{@link ReplayingDecoder}会捕获该异常，并将buf的读指针回调。
 * 下次来数据后再次尝试，如果够了，就解码成功。
 *
 * 所以，该机制下其必然会带来一下负面影响：
 * 最显然的就是性能问题，一个数据包解码成功可能需要前面好几次的解码失败，如果协议比较复杂，会浪费很多耗时。
 *
 * @author LQL
 * @since Create in 2021/2/2 21:25
 */
public class TimeDecoder extends ReplayingDecoder<Void> {

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
        out.add(in.readBytes(4));
    }

}
