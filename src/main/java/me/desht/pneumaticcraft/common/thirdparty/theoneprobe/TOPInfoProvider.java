package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class TOPInfoProvider {
    private static final TextFormatting COLOR = TextFormatting.GRAY;

    public static void handle(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof IInfoForwarder){
            te = ((IInfoForwarder)te).getInfoTileEntity();
        }

        if (te instanceof IPneumaticMachine) {
            TOPInfoProvider.handlePneumatic(mode, probeInfo, (IPneumaticMachine)te);
        }
        if (te instanceof IHeatExchanger) {
            TOPInfoProvider.handleHeat(mode, probeInfo, (IHeatExchanger) te);
        }
        if (PNCConfig.Client.topShowsFluids && te != null) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, data.getSideHit())
                    .ifPresent(handler -> TOPInfoProvider.handleFluidTanks(mode, probeInfo, handler));
        }
        if (te instanceof TileEntityBase) {
            TOPInfoProvider.handleRedstoneMode(mode, probeInfo, (TileEntityBase) te);
        }
        if (te instanceof TileEntityPressureTube) {
            TOPInfoProvider.handlePressureTube(mode, probeInfo, (TileEntityPressureTube) te, data.getSideHit(), player);
        }
        if (te instanceof ICamouflageableTE) {
            TOPInfoProvider.handleCamo(mode, probeInfo, ((ICamouflageableTE) te).getCamouflage());
        }
    }

    private static void handlePneumatic(ProbeMode mode, IProbeInfo probeInfo, IPneumaticMachine pneumaticMachine) {
        IAirHandlerMachine airHandler = pneumaticMachine.getAirHandler(null);
        probeInfo.text(COLOR + "Max Pressure: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(airHandler.getDangerPressure(), 1) + " bar");
        if (mode == ProbeMode.EXTENDED) {
            probeInfo.text(COLOR + "Pressure:");
            probeInfo.horizontal()
                    .element(new ElementPressure(pneumaticMachine))
                    .vertical()
                    .text("")
                    .text("  \u2b05 " + PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 2) + " bar");
        } else {
            probeInfo.text(COLOR + "Pressure: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 2) + " bar");
        }
    }

    private static void handleHeat(ProbeMode mode, IProbeInfo probeInfo, IHeatExchanger heatExchanger) {
        HeatExchangerManager.TemperatureData tempData = new HeatExchangerManager.TemperatureData(heatExchanger);
        if (tempData.isMultisided()) {
            for (Direction face : Direction.VALUES) {
                if (tempData.hasData(face)) {
                    probeInfo.text(HeatUtil.formatHeatString(face, (int) tempData.getTemperature(face)).getFormattedText());
                }
            }
        } else if (tempData.hasData(null)) {
            probeInfo.text(HeatUtil.formatHeatString((int) tempData.getTemperature(null)).getFormattedText());
        }
    }

    static void handleSemiblock(ProbeMode mode, IProbeInfo probeInfo, ISemiBlock semiBlock) {
        NonNullList<ItemStack> l = NonNullList.create();
        semiBlock.addDrops(l);
        if (l.isEmpty()) return;
        ItemStack stack = l.get(0);
        int color = semiBlock instanceof SemiBlockLogistics ? ((SemiBlockLogistics) semiBlock).getColor() : 0xFF808080;
        IProbeInfo vert = probeInfo.vertical(new LayoutStyle().borderColor(color).spacing(3));
        IProbeInfo horiz = vert.horizontal();
        horiz.item(stack);
        horiz.text(stack.getDisplayName().getFormattedText());
        if (semiBlock instanceof SemiBlockBasic) {
            List<String> currenttip = new ArrayList<>();
            ((SemiBlockBasic) semiBlock).addTooltip(currenttip, stack.getTag(), mode == ProbeMode.EXTENDED);
            currenttip.forEach(vert::text);
        }
    }

    private static void handleRedstoneMode(ProbeMode mode, IProbeInfo probeInfo, TileEntityBase te) {
        if (te instanceof IRedstoneControl) {
            int redstoneMode = ((IRedstoneControl) te).getRedstoneMode();
            probeInfo.text(COLOR + L(te.getRedstoneTabTitle()) + ": " + TextFormatting.RED + L(te.getRedstoneButtonText(redstoneMode)));
        }
    }

    private static void handlePressureTube(ProbeMode mode, IProbeInfo probeInfo, TileEntityPressureTube te, Direction face, PlayerEntity player) {
        TubeModule module = BlockPressureTube.getFocusedModule(te.getWorld(), te.getPos(), player);
        if (module != null) {
            List<ITextComponent> currenttip = new ArrayList<>();
            module.addInfo(currenttip);
            if (!currenttip.isEmpty()) {
                IProbeInfo vert = probeInfo.vertical(new LayoutStyle().borderColor(0xFF4040FF).spacing(3));
                currenttip.stream().map(ITextComponent::getFormattedText).forEach(vert::text);
            }
        }
    }

    private static void handleFluidTanks(ProbeMode mode, IProbeInfo probeInfo, IFluidHandler handler) {
        if (mode == ProbeMode.EXTENDED) {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluidStack = handler.getFluidInTank(i);
                String fluidDesc = fluidStack.isEmpty() ? L("gui.liquid.empty") : fluidStack.getAmount() + "mB " + L(fluidStack.getTranslationKey());
                probeInfo.text(COLOR + "Tank #" + (i + 1) + ": " + TextFormatting.AQUA + fluidDesc);
            }
        }
    }

    public static void handleCamo(ProbeMode mode, IProbeInfo probeInfo, BlockState camo) {
        if (camo != null) {
            probeInfo.text(TextFormatting.YELLOW + "[Camo: " + ItemCamoApplicator.getCamoStateDisplayName(camo).getFormattedText() + "]");
        }
    }

    private static String L(String s) {
        return IProbeInfo.STARTLOC + s + IProbeInfo.ENDLOC;
    }
}
