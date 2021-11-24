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

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PneumaticProvider {
    public static class Data implements IServerDataProvider<TileEntity> {
        @Override
        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
            TileEntity teInfo;
            if (te instanceof IInfoForwarder) {
                teInfo = ((IInfoForwarder) te).getInfoTileEntity();
                if (teInfo != null) {
                    compoundNBT.putInt("infoX", teInfo.getBlockPos().getX());
                    compoundNBT.putInt("infoY", teInfo.getBlockPos().getY());
                    compoundNBT.putInt("infoZ", teInfo.getBlockPos().getZ());
                }
            } else {
                teInfo = te;
            }
            if (teInfo != null) {
                teInfo.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                        .ifPresent(h -> compoundNBT.putFloat("pressure", h.getPressure()));

                if (teInfo.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).isPresent()) {
                    compoundNBT.put("heatData", new TemperatureData(teInfo).toNBT());
                }

                teInfo.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                        .ifPresent(h -> {
                            ListNBT list = new ListNBT();
                            for (int i = 0; i < h.getTanks(); i++) {
                                list.add(h.getFluidInTank(i).writeToNBT(new CompoundNBT()));
                            }
                            compoundNBT.put("tanks", list);
                        });
            }
        }
    }

    public static class Component implements IComponentProvider {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
            CompoundNBT tag = accessor.getServerData();
            TileEntity te = accessor.getTileEntity();
            if (te instanceof IInfoForwarder){
                BlockPos infoPos = new BlockPos(tag.getInt("infoX"), tag.getInt("infoY"), tag.getInt("infoZ"));
                te = accessor.getWorld().getBlockEntity(infoPos);
            }
            if (te != null) {
                te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(h -> {
                    if (h instanceof MachineAirHandler) {
                        addTipToMachine(tooltip, (MachineAirHandler) h, tag.getFloat("pressure"));
                    }
                });
                handleHeatData(tooltip, tag);
                if (accessor.getPlayer().isCrouching()) {
                    handleFluidData(tooltip, tag);
                }
            }
        }

        private void handleFluidData(List<ITextComponent> tooltip, CompoundNBT tag) {
            ListNBT list = tag.getList("tanks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT subtag = list.getCompound(i);
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(subtag);
                ITextComponent fluidDesc = fluidStack.isEmpty() ?
                        xlate("pneumaticcraft.gui.misc.empty") :
                        xlate("pneumaticcraft.message.misc.fluidmB", fluidStack.getAmount()).append(" ").append(xlate(fluidStack.getTranslationKey()));
                tooltip.add(xlate("pneumaticcraft.waila.tank", i + 1, fluidDesc.copy().withStyle(TextFormatting.AQUA)));
            }
        }

        private void handleHeatData(List<ITextComponent> tooltip, CompoundNBT tag) {
            if (tag.contains("heatData")) {
                TemperatureData tempData = TemperatureData.fromNBT(tag.getCompound("heatData"));
                if (tempData.isMultisided()) {
                    for (Direction face : DirectionUtil.VALUES) {
                        if (tempData.hasData(face)) {
                            tooltip.add(HeatUtil.formatHeatString(face, (int) tempData.getTemperature(face)));
                        }
                    }
                } else if (tempData.hasData(null)) {
                    tooltip.add(HeatUtil.formatHeatString((int) tempData.getTemperature(null)));
                }
            }
        }

        private void addTipToMachine(List<ITextComponent> tooltip, MachineAirHandler airHandler, float pressure) {
            Map<String, String> values = new HashMap<>();

            values.put("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 2));
            values.put("pneumaticcraft.gui.tooltip.maxPressure", PneumaticCraftUtils.roundNumberTo(airHandler.getDangerPressure(), 1));

            for (Map.Entry<String, String> entry : values.entrySet()) {
                tooltip.add(new TranslationTextComponent(entry.getKey(), entry.getValue()));
            }
        }
    }
}
