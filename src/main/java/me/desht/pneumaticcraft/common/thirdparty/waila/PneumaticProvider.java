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
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.entity.IInfoForwarder;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.LinkedHashSet;
import java.util.Set;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PneumaticProvider {
    private static final ResourceLocation ID = RL("pneumatic");

    public static class DataProvider implements IServerDataProvider<BlockAccessor> {
        @Override
        public ResourceLocation getUid() {
            return ID;
        }

        @Override
        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            BlockEntity beInfo;
            if (blockAccessor.getBlockEntity() instanceof IInfoForwarder forwarder) {
                beInfo = forwarder.getInfoBlockEntity();
                if (beInfo != null) {
                    compoundTag.putInt("infoX", beInfo.getBlockPos().getX());
                    compoundTag.putInt("infoY", beInfo.getBlockPos().getY());
                    compoundTag.putInt("infoZ", beInfo.getBlockPos().getZ());
                }
            } else {
                beInfo = blockAccessor.getBlockEntity();
            }
            if (beInfo != null) {
                Set<IAirHandlerMachine> set = new LinkedHashSet<>();
                IOHelper.getCap(beInfo, PNCCapabilities.AIR_HANDLER_MACHINE, null).ifPresent(set::add);
                for (Direction dir : DirectionUtil.VALUES) {
                    IOHelper.getCap(beInfo, PNCCapabilities.AIR_HANDLER_MACHINE, dir).ifPresent(set::add);
                }
                ListTag l = new ListTag();
                for (IAirHandlerMachine h : set) {
                    ListTag l2 = new ListTag();
                    l2.add(FloatTag.valueOf(h.getPressure()));
                    l2.add(FloatTag.valueOf(h.getDangerPressure()));
                    l.add(l2);
                }
                compoundTag.put("pressure", l);

                if (IOHelper.getCap(beInfo, PNCCapabilities.HEAT_EXCHANGER_BLOCK, null).isPresent()) {
                    compoundTag.put("heatData", TemperatureData.forBlockEntity(beInfo).toNBT());
                }

                IOHelper.getCap(beInfo, Capabilities.FluidHandler.BLOCK, null).ifPresent(h -> {
                    ListTag list = new ListTag();
                    for (int i = 0; i < h.getTanks(); i++) {
                        list.add(h.getFluidInTank(i).save(blockAccessor.getLevel().registryAccess()));
                    }
                    compoundTag.put("tanks", list);
                });
            }
        }
    }

    public static class ComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            CompoundTag tag = blockAccessor.getServerData();
            BlockEntity te = blockAccessor.getBlockEntity();
            if (te instanceof IInfoForwarder) {
                BlockPos infoPos = new BlockPos(tag.getInt("infoX"), tag.getInt("infoY"), tag.getInt("infoZ"));
                te = blockAccessor.getLevel().getBlockEntity(infoPos);
            }
            if (te != null) {
                ListTag l = tag.getList("pressure", Tag.TAG_LIST);
                for (int i = 0; i < l.size(); i++) {
                    ListTag l2 = l.getList(i);
                    String pressureStr = PneumaticCraftUtils.roundNumberTo(l2.getFloat(0), 2);
                    String dangerPressureStr = PneumaticCraftUtils.roundNumberTo(l2.getFloat(1), 1);
                    iTooltip.add(xlate("pneumaticcraft.gui.tooltip.pressureMax", pressureStr, dangerPressureStr));
                }
                handleHeatData(iTooltip, tag);
                if (blockAccessor.getPlayer().isCrouching()) {
                    handleFluidData(iTooltip, tag, blockAccessor.getLevel().registryAccess());
                }
            }
        }

        private void handleFluidData(ITooltip tooltip, CompoundTag tag, HolderLookup.Provider provider) {
            ListTag list = tag.getList("tanks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag subtag = list.getCompound(i);
                FluidStack fluidStack = FluidStack.parseOptional(provider, subtag);
                MutableComponent fluidDesc = fluidStack.isEmpty() ?
                        xlate("pneumaticcraft.gui.misc.empty") :
                        xlate("pneumaticcraft.message.misc.fluidmB", fluidStack.getAmount()).append(" ").append(fluidStack.getHoverName());
                tooltip.add(xlate("pneumaticcraft.waila.tank", i + 1, fluidDesc.copy().withStyle(ChatFormatting.AQUA)));
            }
        }

        private void handleHeatData(ITooltip tooltip, CompoundTag tag) {
            if (tag.contains("heatData")) {
                TemperatureData tempData = TemperatureData.fromNBT(tag.getCompound("heatData"));
                if (tempData.isMultisided()) {
                    for (Direction face : DirectionUtil.VALUES) {
                        if (tempData.hasData(face)) {
                            tooltip.add(HeatUtil.formatHeatString(face, tempData.getTemperatureAsInt(face)));
                        }
                    }
                } else if (tempData.hasData(null)) {
                    tooltip.add(HeatUtil.formatHeatString(tempData.getTemperatureAsInt(null)));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ID;
        }
    }
}
