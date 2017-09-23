package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketSetLogisticsFluidFilterStack extends LocationIntPacket<PacketSetLogisticsFluidFilterStack> {
    private FluidStack settingStack;
    private int settingIndex;

    public PacketSetLogisticsFluidFilterStack() {
    }

    public PacketSetLogisticsFluidFilterStack(SemiBlockLogistics logistics, FluidStack stack, int index) {
        super(logistics.getPos());
        settingStack = stack;
        settingIndex = index;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(settingStack != null);
        if (settingStack != null) {
            ByteBufUtils.writeUTF8String(buf, settingStack.getFluid().getName());
            buf.writeInt(settingStack.amount);
            ByteBufUtils.writeTag(buf, settingStack.tag);
        }
        buf.writeInt(settingIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        if (buf.readBoolean())
            settingStack = new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf));
        settingIndex = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketSetLogisticsFluidFilterStack message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketSetLogisticsFluidFilterStack message, EntityPlayer player) {
        if (message.pos.equals(new BlockPos(0, 0, 0))) {
            if (player.openContainer instanceof ContainerLogistics) {
                ((ContainerLogistics) player.openContainer).logistics.setFilter(message.settingIndex, message.settingStack);
            }
        } else {
            ISemiBlock semiBlock = SemiBlockManager.getInstance(player.world).getSemiBlock(player.world, message.pos);
            if (semiBlock instanceof SemiBlockLogistics) {
                ((SemiBlockLogistics) semiBlock).setFilter(message.settingIndex, message.settingStack);
            }
        }
    }

}
