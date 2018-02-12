package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketChangeGPSToolCoordinate extends LocationIntPacket<PacketChangeGPSToolCoordinate> {
    private String variable;
    private int metadata;

    public PacketChangeGPSToolCoordinate() {
    }

    public PacketChangeGPSToolCoordinate(BlockPos pos, String variable, int metadata) {
        super(pos);
        this.variable = variable;
        this.metadata = metadata;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, variable);
        buf.writeInt(metadata);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        variable = ByteBufUtils.readUTF8String(buf);
        metadata = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketChangeGPSToolCoordinate message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketChangeGPSToolCoordinate message, EntityPlayer player) {
        ItemStack playerStack = player.getHeldItemMainhand();
        if (playerStack.getItem() == Itemss.GPS_TOOL) {
            ItemGPSTool.setVariable(playerStack, message.variable);
            if (message.pos.getY() >= 0) {
                playerStack.getItem().onItemUse(player, player.world, message.pos, EnumHand.MAIN_HAND, null, 0, 0, 0);
            }
        }else if(playerStack.getItem() == Itemss.GPS_AREA_TOOL){
            ItemGPSAreaTool.setVariable(playerStack, message.variable, message.metadata);
            if(message.pos.getY() >= 0){
                ItemGPSAreaTool.setGPSPosAndNotify(player, message.pos, message.metadata);
            }
        }
    }
}
