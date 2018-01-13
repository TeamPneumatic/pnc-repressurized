package me.desht.pneumaticcraft.common.network;

import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketRemoveSemiBlock extends LocationIntPacket<PacketRemoveSemiBlock> {

    private int index;

    public PacketRemoveSemiBlock() {
    }

    public PacketRemoveSemiBlock(ISemiBlock semiBlock, int index) {
        super(semiBlock.getPos());
        this.index = index;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(index);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        index = buf.readByte();
    }

    @Override
    public void handleClientSide(PacketRemoveSemiBlock message, EntityPlayer player) {
        SemiBlockManager manager = SemiBlockManager.getInstance(player.world);
        manager.getSemiBlocks(player.world, message.pos)
              .filter(s -> s.getIndex() == message.index)
              .collect(Collectors.toList()) //To list is necessary, because the indeces will get updated through the removal iterations.
              .forEach(s -> manager.removeSemiBlock(s));
    }

    @Override
    public void handleServerSide(PacketRemoveSemiBlock message, EntityPlayer player) {

    }

}
