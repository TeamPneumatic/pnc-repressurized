package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketChangeGPSToolCoordinate extends LocationIntPacket<PacketChangeGPSToolCoordinate> {
    private String variable;

    public PacketChangeGPSToolCoordinate() {
    }

    public PacketChangeGPSToolCoordinate(BlockPos pos, String variable) {
        super(pos);
        this.variable = variable;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, variable);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        variable = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void handleClientSide(PacketChangeGPSToolCoordinate message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketChangeGPSToolCoordinate message, EntityPlayer player) {
        ItemStack playerStack = player.getHeldItemMainhand();
        if (!playerStack.isEmpty() && playerStack.getItem() == Itemss.GPS_TOOL) {
            ItemGPSTool.setVariable(playerStack, message.variable);
            if (message.pos.getY() >= 0) {
                playerStack.getItem().onItemUse(player, player.world, message.pos, EnumHand.MAIN_HAND, null, 0, 0, 0);
            }
        }
    }
}
