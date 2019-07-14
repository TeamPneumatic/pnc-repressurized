package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by the server to sync the addition of a new Semiblock in the world
 */
public class PacketAddSemiBlock extends LocationIntPacket {

    private String id;

    public PacketAddSemiBlock() {
    }

    public PacketAddSemiBlock(ISemiBlock semiBlock) {
        this(semiBlock.getPos(), semiBlock);
    }

    public PacketAddSemiBlock(BlockPos pos, ISemiBlock semiBlock) {
        super(pos);
        Validate.notNull(semiBlock);
        id = SemiBlockManager.getKeyForSemiBlock(semiBlock);
    }

    public PacketAddSemiBlock(PacketBuffer buf) {
        super(buf);
        id = PacketUtil.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        PacketUtil.writeUTF8String(buf, id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            SemiBlockManager.getInstance(player.world).addSemiBlock(player.world, pos, SemiBlockManager.getSemiBlockForKey(id));
        });
        ctx.get().setPacketHandled(true);
    }

}
