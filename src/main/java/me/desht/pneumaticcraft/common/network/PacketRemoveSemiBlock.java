package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Received on: CLIENT
 * Sent by server to remove a semiblock client-side
 */
public class PacketRemoveSemiBlock extends LocationIntPacket {

    private int index;

    public PacketRemoveSemiBlock() {
    }

    public PacketRemoveSemiBlock(ISemiBlock semiBlock, int index) {
        super(semiBlock.getPos());
        this.index = index;
    }

    public PacketRemoveSemiBlock(PacketBuffer buffer) {
        super(buffer);
        this.index = buffer.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            SemiBlockManager manager = SemiBlockManager.getInstance(player.world);
            manager.getSemiBlocks(player.world, pos)
                    .filter(s -> s.getIndex() == index)
                    .collect(Collectors.toList()) //To list is necessary, because the indices will get updated through the removal iterations.
                    .forEach(manager::removeSemiBlock);
        });
        ctx.get().setPacketHandled(true);
    }
}
