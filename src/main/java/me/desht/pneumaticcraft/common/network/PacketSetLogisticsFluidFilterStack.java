package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 */
public class PacketSetLogisticsFluidFilterStack extends LocationIntPacket {
    private FluidStack settingStack;
    private int settingIndex;

    public PacketSetLogisticsFluidFilterStack() {
    }

    public PacketSetLogisticsFluidFilterStack(SemiBlockLogistics logistics, FluidStack stack, int index) {
        super(logistics.getPos());
        settingStack = stack;
        settingIndex = index;
    }

    public PacketSetLogisticsFluidFilterStack(PacketBuffer buffer) {
        super(buffer);
        if (buffer.readBoolean())
            settingStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
        settingIndex = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(settingStack != null);
        if (settingStack != null) {
            new PacketBuffer(buf).writeCompoundTag(settingStack.writeToNBT(new CompoundNBT()));
        }
        buf.writeInt(settingIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (pos.equals(BlockPos.ZERO)) {
                if (player.openContainer instanceof ContainerLogistics) {
                    ((ContainerLogistics) player.openContainer).logistics.setFilter(settingIndex, settingStack);
                }
            } else {
                SemiBlockLogistics semiBlock = SemiBlockManager.getInstance(player.world).getSemiBlock(SemiBlockLogistics.class, player.world, pos);
                if (semiBlock != null) {
                    semiBlock.setFilter(settingIndex, settingStack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
