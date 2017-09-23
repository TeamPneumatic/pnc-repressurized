package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PacketOpenTubeModuleGui extends LocationIntPacket<PacketOpenTubeModuleGui> {
    private int guiID;

    public PacketOpenTubeModuleGui() {
    }

    public PacketOpenTubeModuleGui(int guiID, BlockPos pos) {
        super(pos);
        this.guiID = guiID;

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        guiID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(guiID);
    }

    @Override
    public void handleClientSide(PacketOpenTubeModuleGui message, EntityPlayer player) {
        if (BlockPressureTube.getLookedModule(player.world, message.pos, player) != null) {
            Object o = PneumaticCraftRepressurized.proxy.getClientGuiElement(message.guiID, player, player.world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
            FMLCommonHandler.instance().showGuiScreen(o);
        }
    }

    @Override
    public void handleServerSide(PacketOpenTubeModuleGui message, EntityPlayer player) {
    }

}
