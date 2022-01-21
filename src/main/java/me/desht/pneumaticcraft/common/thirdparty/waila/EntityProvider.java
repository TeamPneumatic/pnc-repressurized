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

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EntityProvider {

    public static class Data implements IServerDataProvider<Entity> {
        @Override
        public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, Entity entity, boolean b) {
            entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .ifPresent(h -> compoundTag.putFloat("Pressure", h.getPressure()));
            entity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
                    .ifPresent(h -> compoundTag.putFloat("Temperature", h.getTemperatureAsInt()));
            if (entity instanceof ISemiBlock s) {
                s.serializeNBT(compoundTag);
            }
        }
    }

    public static class Component implements IEntityComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
            iTooltip.add(entityAccessor.getEntity().getDisplayName().copy().withStyle(ChatFormatting.WHITE));

            if (entityAccessor.getServerData().contains("Pressure")) {
                float pressure = entityAccessor.getServerData().getFloat("Pressure");
                iTooltip.add(new TranslatableComponent("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
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

            String modName = ModNameCache.getModName(Names.MOD_ID);
            iTooltip.add(new TextComponent(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
    }
}
