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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityProvider {
    private static final ResourceLocation ID = RL("entity");

    public static class DataProvider implements IServerDataProvider<EntityAccessor> {
        @Override
        public ResourceLocation getUid() {
            return ID;
        }

        @Override
        public void appendServerData(CompoundTag compoundTag, EntityAccessor accessor) {
            accessor.getEntity().getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .ifPresent(h -> compoundTag.putFloat("Pressure", h.getPressure()));
            accessor.getEntity().getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                    .ifPresent(h -> compoundTag.putFloat("Temperature", h.getTemperatureAsInt()));
            if (accessor instanceof ISemiBlock s) {
                s.serializeNBT(compoundTag);
            }
        }
    }

    public static class ComponentProvider implements IEntityComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
            if (entityAccessor.getServerData().contains("Pressure")) {
                float pressure = entityAccessor.getServerData().getFloat("Pressure");
                iTooltip.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
            }
            if (entityAccessor.getServerData().contains("Temperature")) {
                iTooltip.add(HeatUtil.formatHeatString(entityAccessor.getServerData().getInt("Temperature")));
            }
            if (entityAccessor.getEntity() instanceof ISemiBlock semiBlock) {
                semiBlock.addTooltip(iTooltip::add, entityAccessor.getPlayer(), entityAccessor.getServerData(), entityAccessor.getPlayer().isShiftKeyDown());
                BlockPos pos = semiBlock.getBlockPos();
                BlockState state = entityAccessor.getLevel().getBlockState(pos);
                iTooltip.add(state.getBlock().getName().withStyle(ChatFormatting.YELLOW));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }
}
