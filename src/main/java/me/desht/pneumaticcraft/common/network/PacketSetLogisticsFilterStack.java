package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 */
public class PacketSetLogisticsFilterStack extends LocationIntPacket {
    private ItemStack settingStack;
    private int settingIndex;

    public PacketSetLogisticsFilterStack() {
    }

    public PacketSetLogisticsFilterStack(SemiBlockLogistics logistics, @Nonnull ItemStack stack, int index) {
        super(logistics.getPos());
        settingStack = stack;
        settingIndex = index;
    }

    public PacketSetLogisticsFilterStack(PacketBuffer buffer) {
        super(buffer);
        settingStack = buffer.readItemStack();
        settingIndex = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeItemStack(settingStack);
        buf.writeInt(settingIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (pos.equals(BlockPos.ZERO)) {
                if (player.openContainer instanceof ContainerLogistics) {
                    ((ContainerLogistics) player.openContainer).logistics.getFilters().setStackInSlot(settingIndex, settingStack);
                }
            } else {
                SemiBlockLogistics semiBlock = SemiBlockManager.getInstance(player.world).getSemiBlock(SemiBlockLogistics.class, player.world, pos);
                if (semiBlock != null) {
                    semiBlock.getFilters().setStackInSlot(settingIndex, settingStack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
