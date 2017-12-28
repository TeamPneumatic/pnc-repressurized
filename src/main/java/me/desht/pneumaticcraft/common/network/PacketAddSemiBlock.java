package me.desht.pneumaticcraft.common.network;

import org.apache.commons.lang3.Validate;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketAddSemiBlock extends LocationIntPacket<PacketAddSemiBlock> {

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

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, id);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        id = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void handleClientSide(PacketAddSemiBlock message, EntityPlayer player) {
        SemiBlockManager.getInstance(player.world).addSemiBlock(player.world, message.pos, SemiBlockManager.getSemiBlockForKey(message.id));
    }

    @Override
    public void handleServerSide(PacketAddSemiBlock message, EntityPlayer player) {

    }

}
