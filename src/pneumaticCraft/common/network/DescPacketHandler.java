package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.EnumMap;

import pneumaticCraft.PneumaticCraft;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;

@Sharable
public class DescPacketHandler extends SimpleChannelInboundHandler<FMLProxyPacket>{
    public final static String CHANNEL = "PneumaticCraftDesc";
    private final static EnumMap<Side, FMLEmbeddedChannel> channels = NetworkRegistry.INSTANCE.newChannel(DescPacketHandler.CHANNEL, new DescPacketHandler());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception{
        PacketDescription packet = new PacketDescription();
        packet.fromBytes(msg.payload());
        packet.handleClientSide(packet, PneumaticCraft.proxy.getPlayer());
    }

    public static FMLProxyPacket getPacket(PacketDescription packet){
        ByteBuf buf = Unpooled.buffer();
        packet.toBytes(buf);
        return new FMLProxyPacket(buf, CHANNEL);
    }

}
