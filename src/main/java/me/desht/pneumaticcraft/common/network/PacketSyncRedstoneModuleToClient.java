package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to sync up the settings of a redstone module
 */
public class PacketSyncRedstoneModuleToClient extends LocationIntPacket {
    private final ModuleRedstone.EnumRedstoneDirection dir;
    private final int outputLevel;
    private final int inputLevel;
    private final int channel;
    private final byte side;

    public PacketSyncRedstoneModuleToClient(ModuleRedstone module) {
        super(module.getTube().getPos());

        this.dir = module.getRedstoneDirection();
        this.outputLevel = module.getRedstoneLevel();
        this.inputLevel = module.getInputLevel();
        this.channel = module.getColorChannel();
        this.side = (byte) module.getDirection().getIndex();
    }

    PacketSyncRedstoneModuleToClient(PacketBuffer buffer) {
        super(buffer);
        dir = ModuleRedstone.EnumRedstoneDirection.values()[buffer.readByte()];
        side = buffer.readByte();
        outputLevel = buffer.readByte();
        inputLevel = buffer.readByte();
        channel = buffer.readByte();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeByte(dir.ordinal());
        buf.writeByte(side);
        buf.writeByte(outputLevel);
        buf.writeByte(inputLevel);
        buf.writeByte(channel);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntityPressureTube te = TileEntityPressureTube.getTube(ClientUtils.getClientTE(pos));
            if (te != null) {
                TubeModule module = te.getModule(Direction.byIndex(side));
                if (module instanceof ModuleRedstone) {
                    ModuleRedstone mr = (ModuleRedstone) module;
                    mr.setColorChannel(channel);
                    mr.setRedstoneDirection(dir);
                    mr.setOutputLevel(outputLevel);
                    mr.setInputLevel(inputLevel);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
