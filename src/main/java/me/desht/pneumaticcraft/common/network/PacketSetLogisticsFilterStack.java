package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;

public class PacketSetLogisticsFilterStack extends LocationIntPacket<PacketSetLogisticsFilterStack> {
    private ItemStack settingStack;
    private int settingIndex;

    public PacketSetLogisticsFilterStack() {
    }

    public PacketSetLogisticsFilterStack(SemiBlockLogistics logistics, @Nonnull ItemStack stack, int index) {
        super(logistics.getPos());
        settingStack = stack;
        settingIndex = index;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeItemStack(buf, settingStack);
        buf.writeInt(settingIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        settingStack = ByteBufUtils.readItemStack(buf);
        settingIndex = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketSetLogisticsFilterStack message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketSetLogisticsFilterStack message, EntityPlayer player) {
        if (message.pos.equals(new BlockPos(0, 0, 0))) {
            if (player.openContainer instanceof ContainerLogistics) {
                ((ContainerLogistics) player.openContainer).logistics.getFilters().setStackInSlot(message.settingIndex, message.settingStack);
            }
        } else {
            ISemiBlock semiBlock = SemiBlockManager.getInstance(player.world).getSemiBlock(player.world, message.pos);
            if (semiBlock instanceof SemiBlockLogistics) {
                ((SemiBlockLogistics) semiBlock).getFilters().setStackInSlot(message.settingIndex, message.settingStack);
            }
        }
    }

}
