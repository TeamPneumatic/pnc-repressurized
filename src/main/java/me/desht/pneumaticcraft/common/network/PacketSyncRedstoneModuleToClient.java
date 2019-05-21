package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSyncRedstoneModuleToClient extends LocationIntPacket<PacketSyncRedstoneModuleToClient> {
    private ModuleRedstone.EnumRedstoneDirection dir;
    private int outputLevel;
    private int inputLevel;
    private int channel;
    private byte side;

    @SuppressWarnings("unused")
    public PacketSyncRedstoneModuleToClient() {
    }

    public PacketSyncRedstoneModuleToClient(ModuleRedstone module) {
        super(module.getTube().pos());

        this.dir = module.getRedstoneDirection();
        this.outputLevel = module.getRedstoneLevel();
        this.inputLevel = module.getInputLevel();
        this.channel = module.getColorChannel();
        this.side = (byte) module.getDirection().getIndex();
    }

    @Override
    public void handleClientSide(PacketSyncRedstoneModuleToClient message, EntityPlayer player) {
        TileEntityPressureTube te = TileEntityPressureTube.getTube(message.getTileEntity(player.world));
        if (te != null) {
            TubeModule module = te.modules[message.side];
            if (module instanceof ModuleRedstone) {
                ModuleRedstone mr = (ModuleRedstone) module;
                mr.setColorChannel(message.channel);
                mr.setRedstoneDirection(message.dir);
                mr.setOutputLevel(message.outputLevel);
                mr.setInputLevel(message.inputLevel);
            }
        }
    }

    @Override
    public void handleServerSide(PacketSyncRedstoneModuleToClient message, EntityPlayer player) {
        // empty
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeByte(dir.ordinal());
        buf.writeByte(side);
        buf.writeByte(outputLevel);
        buf.writeByte(inputLevel);
        buf.writeByte(channel);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        dir = ModuleRedstone.EnumRedstoneDirection.values()[buf.readByte()];
        side = buf.readByte();
        outputLevel = buf.readByte();
        inputLevel = buf.readByte();
        channel = buf.readByte();
    }
}
