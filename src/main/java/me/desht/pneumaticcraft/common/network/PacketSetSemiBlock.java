package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketSetSemiBlock extends LocationIntPacket<PacketSetSemiBlock> {

    private String id;

    public PacketSetSemiBlock() {
    }

    public PacketSetSemiBlock(ISemiBlock semiBlock) {
        this(semiBlock.getPos(), semiBlock);
    }

    public PacketSetSemiBlock(BlockPos pos, ISemiBlock semiBlock) {
        super(pos);
        if (semiBlock != null) {
            id = SemiBlockManager.getKeyForSemiBlock(semiBlock);
        } else {
            id = "";
        }
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
    public void handleClientSide(PacketSetSemiBlock message, EntityPlayer player) {
        SemiBlockManager.getInstance(player.world).setSemiBlock(player.world, message.pos, message.id.equals("") ? null : SemiBlockManager.getSemiBlockForKey(message.id));
    }

    @Override
    public void handleServerSide(PacketSetSemiBlock message, EntityPlayer player) {

    }

}
