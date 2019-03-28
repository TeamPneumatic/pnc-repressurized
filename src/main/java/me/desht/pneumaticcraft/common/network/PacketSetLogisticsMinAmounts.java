package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PacketSetLogisticsMinAmounts extends LocationIntPacket<PacketSetLogisticsMinAmounts> {
    private int minItems;
    private int minFluid;

    public PacketSetLogisticsMinAmounts() {
    }

    public PacketSetLogisticsMinAmounts(SemiBlockRequester logistics, int minItems, int minFluid) {
        super(logistics.getPos());
        this.minItems = minItems;
        this.minFluid = minFluid;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(minItems);
        buf.writeInt(minFluid);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        minItems = buf.readInt();
        minFluid = buf.readInt();
    }

    @Override
    public void handleClientSide(PacketSetLogisticsMinAmounts message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketSetLogisticsMinAmounts message, EntityPlayer player) {
        if (message.pos.equals(BlockPos.ORIGIN)) {
            // frame in hand
            if (player.openContainer instanceof ContainerLogistics) {
                setMinAmounts(((ContainerLogistics) player.openContainer).logistics, message.minItems, message.minFluid);
            }
        } else {
            // frame in world
            SemiBlockLogistics semiBlock = SemiBlockManager.getInstance(player.world)
                    .getSemiBlock(SemiBlockLogistics.class, player.world, message.pos);
            setMinAmounts(semiBlock, message.minItems, message.minFluid);
        }
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
