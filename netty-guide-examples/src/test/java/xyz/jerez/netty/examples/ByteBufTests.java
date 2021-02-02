package xyz.jerez.netty.examples;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

/**
 * @author LQL
 * @since Create in 2021/2/2 21:15
 */
public class ByteBufTests {

    @Test
    public void test() {
        final ByteBuf buffer = Unpooled.buffer(4);
        buffer.writeByte(1);
        buffer.writeByte(1);
        buffer.writeByte(1);
        buffer.writeByte(1);
        buffer.writeByte(1);
        System.out.println(buffer.readableBytes());
    }

}
