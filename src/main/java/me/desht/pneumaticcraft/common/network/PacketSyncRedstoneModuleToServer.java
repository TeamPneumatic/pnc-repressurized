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

import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule.EnumRedstoneDirection;
import me.desht.pneumaticcraft.common.tubemodules.RedstoneModule.Operation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update server-side settings when redstone module GUI is closed
 */
public record PacketSyncRedstoneModuleToServer(ModuleLocator locator, Operation op,
                                               byte ourColor, byte otherColor, int constantVal, boolean invert,
                                               EnumRedstoneDirection redstoneDirection, boolean comparatorInput)
        implements TubeModulePacket<RedstoneModule> {
    public static final ResourceLocation ID = RL("sync_redstone_module_to_server");

    public static PacketSyncRedstoneModuleToServer create(RedstoneModule module) {
        return new PacketSyncRedstoneModuleToServer(
                ModuleLocator.forModule(module),
                module.getOperation(),
                (byte) module.getColorChannel(),
                (byte) module.getOtherColor(),
                module.getConstantVal(),
                module.isInverted(),
                module.getRedstoneDirection(),
                module.isComparatorInput()
        );
    }

    public static PacketSyncRedstoneModuleToServer fromNetwork(FriendlyByteBuf buffer) {
        ModuleLocator loc = ModuleLocator.fromNetwork(buffer);
        EnumRedstoneDirection redstoneDir = buffer.readEnum(EnumRedstoneDirection.class);
        byte ourColor = buffer.readByte();
        if (redstoneDir.isInput()) {
            return new PacketSyncRedstoneModuleToServer(loc, Operation.PASSTHROUGH, ourColor,
                    (byte) 0, 0, false, EnumRedstoneDirection.INPUT, buffer.readBoolean());
        } else {
            return new PacketSyncRedstoneModuleToServer(loc, buffer.readEnum(Operation.class), ourColor,
                    buffer.readByte(), buffer.readVarInt(), buffer.readBoolean(), EnumRedstoneDirection.OUTPUT, false);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        locator.write(buf);
        buf.writeEnum(redstoneDirection);
        buf.writeByte(ourColor);
        if (redstoneDirection.isInput()) {
            buf.writeBoolean(comparatorInput);
        } else {
            buf.writeEnum(op);
            buf.writeByte(otherColor);
            buf.writeVarInt(constantVal);
            buf.writeBoolean(invert);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void onModuleUpdate(RedstoneModule module, Player player) {
        if (PneumaticCraftUtils.canPlayerReach(player, module.getTube().getBlockPos())) {
            module.setRedstoneDirection(redstoneDirection);
            module.setColorChannel(ourColor);
            if (redstoneDirection.isInput()) {
                module.setComparatorInput(comparatorInput);
            } else {
                module.setInverted(invert);
                module.setOperation(op, otherColor, constantVal);
            }
            module.updateNeighbors();
            module.setInputLevel(-1);  // force recalc
        }
    }
}
