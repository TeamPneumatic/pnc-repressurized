package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by the server to sync the addition of a new Semiblock in the world
 */
public class PacketAddSemiBlock extends LocationIntPacket {

    private ResourceLocation id;

    public PacketAddSemiBlock() {
    }

    public PacketAddSemiBlock(ISemiBlock semiBlock) {
        this(semiBlock.getPos(), semiBlock);
    }

    public PacketAddSemiBlock(BlockPos pos, ISemiBlock semiBlock) {
        super(pos);
        Validate.notNull(semiBlock);
        id = semiBlock.getId(); //SemiBlockManager.getKeyForSemiBlock(semiBlock);
    }

    public PacketAddSemiBlock(PacketBuffer buf) {
        super(buf);
        id = buf.readResourceLocation();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeResourceLocation(id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            SemiBlockManager.getInstance(player.world).addSemiBlock(player.world, pos, SemiBlockManager.getSemiBlockForKey(id));
        });
        ctx.get().setPacketHandled(true);
    }

}
