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

import me.desht.pneumaticcraft.common.block.entity.IRedstoneControl;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.HashMap;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class RedstoneControlProvider {
    public static final ResourceLocation ID = RL("redstone");

    public static class DataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            if (blockAccessor.getBlockEntity() instanceof IRedstoneControl rc) {
                compoundTag.putInt("redstoneMode", rc.getRedstoneMode());
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
            CompoundTag tag = blockAccessor.getServerData();
            // This is used so that we can split values later easier and have them all in the same layout.
            Map<ComponentProvider, ComponentProvider> values = new HashMap<>();

            if (tag.contains("redstoneMode")) {
                BlockEntity te = blockAccessor.getBlockEntity();
                if (te instanceof IRedstoneControl) {
                    RedstoneController<?> rsController = ((IRedstoneControl<?>) te).getRedstoneController();
                    iTooltip.add(rsController.getDescription());
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }
}
