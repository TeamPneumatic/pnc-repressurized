package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumMap;

@Sharable
public class DescPacketHandler extends SimpleChannelInboundHandler<FMLProxyPacket> {
    public final static String CHANNEL = "PneumaticCraftDesc";
    private final static EnumMap<Side, FMLEmbeddedChannel> channels = NetworkRegistry.INSTANCE.newChannel(DescPacketHandler.CHANNEL, new DescPacketHandler());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception {
        final PacketDescription packet = new PacketDescription();
        packet.fromBytes(msg.payload());
        PneumaticCraftRepressurized.proxy.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                packet.handleClientSide(packet, PneumaticCraftRepressurized.proxy.getPlayer());
            }
        }, false);
    }

    public static FMLProxyPacket getPacket(PacketDescription packet) {
        ByteBuf buf = Unpooled.buffer();
        packet.toBytes(buf);
        return new FMLProxyPacket(new PacketBuffer(buf), CHANNEL);
    }

}
