package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketSyncRedstoneModuleToServer extends LocationIntPacket<PacketSyncRedstoneModuleToServer> {
    private byte side;
    private byte op;
    private byte ourColor;
    private byte otherColor;
    private byte constantVal;
    private boolean invert;

    @SuppressWarnings("unused")
    public PacketSyncRedstoneModuleToServer() {
    }

    public PacketSyncRedstoneModuleToServer(ModuleRedstone module) {
        super(module.getTube().pos());

        this.side = (byte) module.getDirection().ordinal();
        this.op = (byte) module.getOperation().ordinal();
        this.ourColor = (byte) module.getColorChannel();
        this.otherColor = (byte) module.getOtherColor();
        this.constantVal = (byte) module.getConstantVal();
        this.invert = module.isInvert();
    }

    @Override
    public void handleClientSide(PacketSyncRedstoneModuleToServer message, EntityPlayer player) {
        // empty
    }

    @Override
    public void handleServerSide(PacketSyncRedstoneModuleToServer message, EntityPlayer player) {
        TileEntity te = player.world.getTileEntity(pos);
        if (te instanceof TileEntityPressureTube) {
            TubeModule m = ((TileEntityPressureTube) te).modules[message.side];
            if (m instanceof ModuleRedstone) {
                ModuleRedstone mr = (ModuleRedstone) m;
                mr.setColorChannel(message.ourColor);
                mr.setInvert(invert);
                mr.setOperation(ModuleRedstone.Operation.values()[message.op], message.otherColor, message.constantVal);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeByte(side);
        buf.writeByte(op);
        buf.writeByte(ourColor);
        buf.writeByte(otherColor);
        buf.writeByte(constantVal);
        buf.writeBoolean(invert);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        side = buf.readByte();
        op = buf.readByte();
        ourColor = buf.readByte();
        otherColor = buf.readByte();
        constantVal = buf.readByte();
        invert = buf.readBoolean();
    }
}
