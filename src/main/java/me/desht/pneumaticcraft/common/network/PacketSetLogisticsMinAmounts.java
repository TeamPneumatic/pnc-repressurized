package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 */
public class PacketSetLogisticsMinAmounts extends LocationIntPacket {
    private int minItems;
    private int minFluid;

    public PacketSetLogisticsMinAmounts() {
    }

    public PacketSetLogisticsMinAmounts(SemiBlockRequester logistics, int minItems, int minFluid) {
        super(logistics.getPos());
        this.minItems = minItems;
        this.minFluid = minFluid;
    }

    public PacketSetLogisticsMinAmounts(PacketBuffer buffer) {
        super(buffer);
        this.minItems = buffer.readInt();
        this.minFluid = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(minItems);
        buf.writeInt(minFluid);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (pos.equals(BlockPos.ZERO)) {
                // frame in hand
                if (player.openContainer instanceof ContainerLogistics) {
                    setMinAmounts(((ContainerLogistics) player.openContainer).logistics, minItems, minFluid);
                }
            } else {
                // frame in world
                SemiBlockLogistics semiBlock = SemiBlockManager.getInstance(player.world)
                        .getSemiBlock(SemiBlockLogistics.class, player.world, pos);
                setMinAmounts(semiBlock, minItems, minFluid);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void setMinAmounts(SemiBlockLogistics logistics, int minItems, int minFluid) {
        if (logistics instanceof SemiBlockRequester) {
            ((SemiBlockRequester) logistics).setMinItemOrderSize(minItems);
            ((SemiBlockRequester) logistics).setMinFluidOrderSize(minFluid);
        } else {
            Log.warning("Received PacketSetLogisticsMinAmounts for logistics @ " + logistics.getPos() + " but it is not a SemiBlockRequester!");
        }
    }
}
