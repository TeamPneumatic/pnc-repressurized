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

package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureData;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TOPInfoProvider {
    private static final TextFormatting COLOR = TextFormatting.GRAY;

    static void handleBlock(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity te = world.getBlockEntity(data.getPos());
        if (te instanceof IInfoForwarder) {
            te = ((IInfoForwarder)te).getInfoTileEntity();
        }

        if (te == null) return;

        if (te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).isPresent()) {
            handlePneumatic(mode, probeInfo, te);
        }
        if (te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).isPresent()) {
            handleHeat(mode, probeInfo, te);
        }
        if (ConfigHelper.client().general.topShowsFluids.get()) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, data.getSideHit())
                    .ifPresent(handler -> handleFluidTanks(mode, probeInfo, handler));
        }
        if (te instanceof IRedstoneControl) {
            handleRedstoneMode(mode, probeInfo, (IRedstoneControl<?>) te);
        }
        if (te instanceof TileEntityPressureTube) {
            handlePressureTube(mode, probeInfo, (TileEntityPressureTube) te, data.getSideHit(), player);
        }
        if (te instanceof ICamouflageableTE) {
            handleCamo(mode, probeInfo, ((ICamouflageableTE) te).getCamouflage());
        }
    }

    static void handleSemiblock(PlayerEntity player, ProbeMode mode, IProbeInfo probeInfo, ISemiBlock semiBlock) {
        IProbeInfo vert = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(semiBlock.getColor()));
        IProbeInfo horiz = vert.horizontal();
        NonNullList<ItemStack> drops = semiBlock.getDrops();
        if (!drops.isEmpty()) {
            ItemStack stack = drops.get(0);
            horiz.item(stack);
            horiz.text(stack.getHoverName());
            List<ITextComponent> currenttip = new ArrayList<>();
            semiBlock.addTooltip(currenttip, player, stack.getTag(), player.isShiftKeyDown());
            currenttip.forEach(vert::text);
        }
    }

    private static void handlePneumatic(ProbeMode mode, IProbeInfo probeInfo, TileEntity pneumaticMachine) {
        pneumaticMachine.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY).ifPresent(airHandler -> {
            String pressure = PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 2);
            String dangerPressure = PneumaticCraftUtils.roundNumberTo(airHandler.getDangerPressure(), 1);
            probeInfo.text(xlate("pneumaticcraft.gui.tooltip.maxPressure", dangerPressure).withStyle(COLOR));
            if (mode == ProbeMode.EXTENDED) {
                probeInfo.text(new StringTextComponent("Pressure:").withStyle(COLOR));
                probeInfo.horizontal()
                        .element(new ElementPressure(pneumaticMachine, airHandler))
                        .vertical()
                        .text(StringTextComponent.EMPTY)
                        .text(new StringTextComponent(" " + Symbols.ARROW_LEFT_SHORT + " " + pressure + " bar"));
            } else {
                probeInfo.text(xlate("pneumaticcraft.gui.tooltip.pressure", pressure));
            }
        });
    }

    private static void handleHeat(ProbeMode mode, IProbeInfo probeInfo, TileEntity heatExchanger) {
        TemperatureData tempData = new TemperatureData(heatExchanger);
        if (tempData.isMultisided()) {
            for (Direction face : DirectionUtil.VALUES) {
                if (tempData.hasData(face)) {
                    probeInfo.text(HeatUtil.formatHeatString(face, (int) tempData.getTemperature(face)));
                }
            }
        } else if (tempData.hasData(null)) {
            probeInfo.text(HeatUtil.formatHeatString((int) tempData.getTemperature(null)));
        }
    }

    private static void handleRedstoneMode(ProbeMode mode, IProbeInfo probeInfo, IRedstoneControl<?> redstoneControl) {
        probeInfo.text(redstoneControl.getRedstoneController().getDescription());
    }

    private static void handlePressureTube(ProbeMode mode, IProbeInfo probeInfo, TileEntityPressureTube te, Direction face, PlayerEntity player) {
        TubeModule module = BlockPressureTube.getFocusedModule(te.getLevel(), te.getBlockPos(), player);
        if (module != null) {
            List<ITextComponent> currenttip = new ArrayList<>();
            module.addInfo(currenttip);
            if (!currenttip.isEmpty()) {
                IProbeInfo vert = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(0xFF4040FF));
                currenttip.forEach(vert::text);
            }
        }
    }

    static void handleFluidTanks(ProbeMode mode, IProbeInfo probeInfo, IFluidHandler handler) {
        if (mode == ProbeMode.EXTENDED) {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluidStack = handler.getFluidInTank(i);
                ITextComponent fluidDesc = fluidStack.isEmpty() ?
                        xlate("pneumaticcraft.gui.misc.empty") :
                        new StringTextComponent(fluidStack.getAmount() + "mB ").append(xlate(fluidStack.getTranslationKey()));
                probeInfo.text(xlate("pneumaticcraft.waila.tank", i + 1, fluidDesc.copy().withStyle(TextFormatting.AQUA)));
            }
        }
    }

    private static void handleCamo(ProbeMode mode, IProbeInfo probeInfo, BlockState camo) {
        if (camo != null) {
            probeInfo.text(xlate("pneumaticcraft.waila.camo", ItemCamoApplicator.getCamoStateDisplayName(camo)));
        }
    }

}
