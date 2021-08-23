package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone.EnumRedstoneDirection;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public class PacketSyncRedstoneModuleToServer extends LocationIntPacket {
    private final byte side;
    private final byte op;
    private final byte ourColor;
    private final byte otherColor;
    private final int constantVal;
    private final boolean invert;
    private final boolean input;
    private final boolean comparatorInput;

    public PacketSyncRedstoneModuleToServer(ModuleRedstone module) {
        super(module.getTube().getBlockPos());

        this.input = module.getRedstoneDirection() == EnumRedstoneDirection.INPUT;
        this.side = (byte) module.getDirection().ordinal();
        this.op = (byte) module.getOperation().ordinal();
        this.ourColor = (byte) module.getColorChannel();
        this.otherColor = (byte) module.getOtherColor();
        this.constantVal = module.getConstantVal();
        this.invert = module.isInverted();
        this.comparatorInput = module.isComparatorInput();
    }

    PacketSyncRedstoneModuleToServer(PacketBuffer buffer) {
        super(buffer);
        side = buffer.readByte();
        input = buffer.readBoolean();
        ourColor = buffer.readByte();
        if (input) {
            op = 0;
            otherColor = 0;
            constantVal = 0;
            invert = false;
            comparatorInput = buffer.readBoolean();
        } else {
            op = buffer.readByte();
            otherColor = buffer.readByte();
            constantVal = buffer.readVarInt();
            invert = buffer.readBoolean();
            comparatorInput = false;
        }
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeByte(side);
        buf.writeBoolean(input);
        buf.writeByte(ourColor);
        if (input) {
            buf.writeBoolean(comparatorInput);
        } else {
            buf.writeByte(op);
            buf.writeByte(otherColor);
            buf.writeVarInt(constantVal);
            buf.writeBoolean(invert);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (PneumaticCraftUtils.canPlayerReach(player, pos)) {
                PneumaticCraftUtils.getTileEntityAt(player.level, pos, TileEntityPressureTube.class).ifPresent(te -> {
                    TubeModule tm = te.getModule(Direction.from3DDataValue(side));
                    if (tm instanceof ModuleRedstone) {
                        ModuleRedstone mr = (ModuleRedstone) tm;
                        mr.setRedstoneDirection(input ? EnumRedstoneDirection.INPUT : EnumRedstoneDirection.OUTPUT);
                        mr.setColorChannel(ourColor);
                        if (input) {
                            mr.setComparatorInput(comparatorInput);
                        } else {
                            mr.setInverted(invert);
                            mr.setOperation(ModuleRedstone.Operation.values()[op], otherColor, constantVal);
                        }
                        mr.updateNeighbors();
                        mr.setInputLevel(-1);  // force recalc
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
