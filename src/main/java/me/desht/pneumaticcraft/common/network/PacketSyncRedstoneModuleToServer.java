/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule.EnumRedstoneDirection;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public class PacketSyncRedstoneModuleToServer extends LocationIntPacket {
    private final Direction side;
    private final byte op;
    private final byte ourColor;
    private final byte otherColor;
    private final int constantVal;
    private final boolean invert;
    private final boolean input;
    private final boolean comparatorInput;

    public PacketSyncRedstoneModuleToServer(RedstoneModule module) {
        super(module.getTube().getBlockPos());

        this.input = module.getRedstoneDirection() == EnumRedstoneDirection.INPUT;
        this.side = module.getDirection();
        this.op = (byte) module.getOperation().ordinal();
        this.ourColor = (byte) module.getColorChannel();
        this.otherColor = (byte) module.getOtherColor();
        this.constantVal = module.getConstantVal();
        this.invert = module.isInverted();
        this.comparatorInput = module.isComparatorInput();
    }

    PacketSyncRedstoneModuleToServer(FriendlyByteBuf buffer) {
        super(buffer);
        side = buffer.readEnum(Direction.class);
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
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeEnum(side);
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
            Player player = ctx.get().getSender();
            if (PneumaticCraftUtils.canPlayerReach(player, pos)) {
                PneumaticCraftUtils.getTileEntityAt(player.level(), pos, PressureTubeBlockEntity.class).ifPresent(tube -> {
                    if (tube.getModule(side) instanceof RedstoneModule mr) {
                        mr.setRedstoneDirection(input ? EnumRedstoneDirection.INPUT : EnumRedstoneDirection.OUTPUT);
                        mr.setColorChannel(ourColor);
                        if (input) {
                            mr.setComparatorInput(comparatorInput);
                        } else {
                            mr.setInverted(invert);
                            mr.setOperation(RedstoneModule.Operation.values()[op], otherColor, constantVal);
                        }
                        mr.updateNeighbors();
                        mr.updateInputLevel();
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
