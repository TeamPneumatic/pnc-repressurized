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

package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class TubeModuleProvider {
    public static class Data implements IServerDataProvider<BlockEntity> {
        @Override
        public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
            if (blockEntity instanceof TileEntityPressureTube) {
                TubeModule module = PressureTubeBlock.getFocusedModule(level, blockEntity.getBlockPos(), serverPlayer);
                if (module != null) {
                    compoundTag.put("module", module.writeToNBT(new CompoundTag()));
                    compoundTag.putByte("side", (byte) module.getDirection().get3DDataValue());
                }
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            TileEntityPressureTube tube = (TileEntityPressureTube) blockAccessor.getBlockEntity();
            CompoundTag tubeTag = blockAccessor.getServerData();
            if (tubeTag.contains("side", Tag.TAG_BYTE)) {
                int side = tubeTag.getByte("side");
                TubeModule module = tube.getModule(Direction.from3DDataValue(side));
                if (module != null) {
                    module.readFromNBT(tubeTag.getCompound("module"));
                    List<net.minecraft.network.chat.Component> l = new ArrayList<>();
                    module.addInfo(l);
                    l.forEach(iTooltip::add);
                }
            }
        }
    }
}
