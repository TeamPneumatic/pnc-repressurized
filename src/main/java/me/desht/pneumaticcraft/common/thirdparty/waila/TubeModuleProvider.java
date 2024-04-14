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

import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class TubeModuleProvider {
    public static final ResourceLocation ID = RL("tube_module");

    public static class DataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            if (blockAccessor.getBlockEntity() instanceof PressureTubeBlockEntity) {
                AbstractTubeModule module = PressureTubeBlock.getFocusedModule(blockAccessor.getLevel(), blockAccessor.getPosition(), blockAccessor.getPlayer());
                if (module != null) {
                    compoundTag.put("module", module.writeToNBT(new CompoundTag()));
                    compoundTag.putByte("side", (byte) module.getDirection().get3DDataValue());
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }

    public static class ComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            PressureTubeBlockEntity tube = (PressureTubeBlockEntity) blockAccessor.getBlockEntity();
            CompoundTag tubeTag = blockAccessor.getServerData();
            if (tubeTag.contains("side", Tag.TAG_BYTE)) {
                int side = tubeTag.getByte("side");
                AbstractTubeModule module = tube.getModule(Direction.from3DDataValue(side));
                if (module != null) {
                    module.readFromNBT(tubeTag.getCompound("module"));
                    List<net.minecraft.network.chat.Component> l = new ArrayList<>();
                    module.addInfo(l);
                    l.forEach(iTooltip::add);
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }
}
