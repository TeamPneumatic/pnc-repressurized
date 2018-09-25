package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TOPCallback implements Function<ITheOneProbe, Void> {
    static int elementPressure;

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        PneumaticCraftRepressurized.logger.info("Enabled support for The One Probe");

        elementPressure = theOneProbe.registerElementFactory(ElementPressure::new);

        theOneProbe.registerProvider(new IProbeInfoProvider() {
            @Override
            public String getID() {
                return Names.MOD_ID + ":default";
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                if (blockState.getBlock() instanceof ITOPInfoProvider) {
                    ITOPInfoProvider provider = (ITOPInfoProvider) blockState.getBlock();
                    provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                }
                SemiBlockManager.getInstance(world).getSemiBlocks(world, data.getPos()).forEach(semiBlock -> handleSemiblock(mode, probeInfo, semiBlock));
            }
        });

        theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
            @Override
            public String getID() {
                return Names.MOD_ID + ":entity";
            }

            @Override
            public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data) {
                if (entity instanceof IPressurizable) {
                    String p = PneumaticCraftUtils.roundNumberTo(((IPressurizable) entity).getPressure(ItemStack.EMPTY), 1);
                    probeInfo.text("Pressure: " + p + " bar");
                }
            }
        });
        return null;
    }

    public static void handlePneumatic(ProbeMode mode, IProbeInfo probeInfo, IPneumaticMachine pneumaticMachine) {
        IAirHandler airHandler = pneumaticMachine.getAirHandler(null);
        if (mode == ProbeMode.EXTENDED) {
            probeInfo.text("Pressure:");
            probeInfo.horizontal()
                    .element(new ElementPressure(pneumaticMachine))
                    .vertical()
                    .text("")
                    .text("  \u2b05 " + PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 2) + " bar");
        } else {
            probeInfo.text(TextFormatting.GRAY + "Pressure: " + TextFormatting.WHITE + PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 2) + " bar");
        }
    }

    public static void handleHeat(ProbeMode mode, IProbeInfo probeInfo, IHeatExchanger heatExchanger) {
        HeatExchangerManager.TemperatureData tempData = new HeatExchangerManager.TemperatureData(heatExchanger);
        if (tempData.isMultisided()) {
            for (EnumFacing face : EnumFacing.VALUES) {
                if (tempData.hasData(face)) {
                    int tempInt = (int) tempData.getTemperature(face) - 273;
                    probeInfo.text(I18n.translateToLocalFormatted("waila.temperature." + face, tempInt));
                }
            }
        } else if (tempData.hasData(null)) {
            int tempInt = (int) tempData.getTemperature(null) - 273;
            probeInfo.text(I18n.translateToLocalFormatted("waila.temperature", tempInt));
        }
    }

    private static void handleSemiblock(ProbeMode mode, IProbeInfo probeInfo, ISemiBlock semiBlock) {
        List<String> currenttip = new ArrayList<>();
        if (semiBlock instanceof SemiBlockBasic) {
            ((SemiBlockBasic) semiBlock).addWailaTooltip(currenttip, new NBTTagCompound(), mode == ProbeMode.EXTENDED);
        }
        for (String s : currenttip) {
            probeInfo.text(s);
        }
    }

    public static void handleRedstoneMode(ProbeMode mode, IProbeInfo probeInfo, TileEntityBase te) {
        if (te instanceof IRedstoneControl) {
            int redstoneMode = ((IRedstoneControl) te).getRedstoneMode();
            probeInfo.text(TextFormatting.GRAY + I18n.translateToLocalFormatted(te.getRedstoneTabTitle()) + ": " + TextFormatting.RED + I18n.translateToLocalFormatted(te.getRedstoneButtonText(redstoneMode)));
        }
    }

    public static void handlePressureTube(ProbeMode mode, IProbeInfo probeInfo, TileEntityPressureTube te, EnumFacing face) {
        if (face != null) {
            TubeModule module = te.modules[face.ordinal()];
            if (module != null) {
                IProbeInfo vert = probeInfo.vertical(new LayoutStyle().borderColor(0xFF4040FF).spacing(3));
                List<String> currenttip = new ArrayList<>();
                module.addInfo(currenttip);
                for (String s : currenttip) vert.text(s);
            }
        }
    }

    public static void handleFluidTanks(ProbeMode mode, IProbeInfo probeInfo, IFluidHandler handler) {
        int n = 1;
        for (IFluidTankProperties properties : handler.getTankProperties()) {
            FluidStack fluidStack = properties.getContents();
            String fluidDesc = fluidStack == null ? I18n.translateToLocalFormatted("gui.liquid.empty") : fluidStack.amount + "mB " + fluidStack.getLocalizedName();
            probeInfo.text(I18n.translateToLocalFormatted("waila.fluid", n++, fluidDesc));
        }
    }

    public static void handleCamo(ProbeMode mode, IProbeInfo probeInfo, IBlockState camo) {
        probeInfo.text(TextFormatting.YELLOW + "[Camo: " + ItemCamoApplicator.getCamoStateDisplayName(camo) + "]");
    }
}
