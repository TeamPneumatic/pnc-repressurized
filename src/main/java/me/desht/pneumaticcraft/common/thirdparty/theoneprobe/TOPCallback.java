package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.api.tileentity.IPneumaticMachine;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.thirdparty.waila.IInfoForwarder;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.List;

public class TOPCallback /*implements Function<ITheOneProbe, Void>*/ {
    private static final TextFormatting COLOR = TextFormatting.GRAY;

    static int elementPressure;

//    @Override
//    public Void apply(ITheOneProbe theOneProbe) {
//        PneumaticCraftRepressurized.LOGGER.info("Enabled support for The One Probe");
//
//        elementPressure = theOneProbe.registerElementFactory(ElementPressure::new);
//
//        theOneProbe.registerProvider(new IProbeInfoProvider() {
//            @Override
//            public String getID() {
//                return Names.MOD_ID + ":default";
//            }
//
//            @Override
//            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//                if (blockState.getBlock() instanceof ITOPInfoProvider) {
//                    ITOPInfoProvider provider = (ITOPInfoProvider) blockState.getBlock();
//                    provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//                }
//                SemiBlockManager.getInstance(world).getSemiBlocks(world, data.getPos()).forEach(semiBlock -> handleSemiblock(mode, probeInfo, semiBlock));
//            }
//        });
//
//        theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
//            @Override
//            public String getID() {
//                return Names.MOD_ID + ":entity";
//            }
//
//            @Override
//            public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data) {
//                if (entity instanceof IPressurizable) {
//                    String p = PneumaticCraftUtils.roundNumberTo(((IPressurizable) entity).getPressure(ItemStack.EMPTY), 1);
//                    probeInfo.text(COLOR + "Pressure: " + p + " bar");
//                }
//            }
//        });
//        return null;
//    }

    public static void handle(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof IInfoForwarder){
            te = ((IInfoForwarder)te).getInfoTileEntity();
        }

        if (te instanceof IPneumaticMachine) {
            TOPCallback.handlePneumatic(mode, probeInfo, (IPneumaticMachine)te);
        }
        if (te instanceof IHeatExchanger) {
            TOPCallback.handleHeat(mode, probeInfo, (IHeatExchanger) te);
        }
        if (Config.Client.topShowsFluids && te != null) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, data.getSideHit())
                    .ifPresent(handler -> TOPCallback.handleFluidTanks(mode, probeInfo, handler));
        }
        if (te instanceof TileEntityBase) {
            TOPCallback.handleRedstoneMode(mode, probeInfo, (TileEntityBase) te);
        }
        if (te instanceof TileEntityPressureTube) {
            TOPCallback.handlePressureTube(mode, probeInfo, (TileEntityPressureTube) te, data.getSideHit(), player);
        }
    }

    private static void handlePneumatic(ProbeMode mode, IProbeInfo probeInfo, IPneumaticMachine pneumaticMachine) {
        IAirHandler airHandler = pneumaticMachine.getAirHandler(null);
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
            probeInfo.text(COLOR + PneumaticCraftUtils.xlate(te.getRedstoneTabTitle()) + ": " + TextFormatting.RED + PneumaticCraftUtils.xlate(te.getRedstoneButtonText(redstoneMode)));
        }
    }

    private static void handlePressureTube(ProbeMode mode, IProbeInfo probeInfo, TileEntityPressureTube te, Direction face, PlayerEntity player) {
        TubeModule module = BlockPressureTube.getFocusedModule(te.getWorld(), te.getPos(), player);
        if (module != null) {
            List<String> currenttip = new ArrayList<>();
            module.addInfo(currenttip);
            if (!currenttip.isEmpty()) {
                IProbeInfo vert = probeInfo.vertical(new LayoutStyle().borderColor(0xFF4040FF).spacing(3));
                currenttip.forEach(vert::text);
            }
        }
    }

    private static void handleFluidTanks(ProbeMode mode, IProbeInfo probeInfo, IFluidHandler handler) {
        if (mode == ProbeMode.EXTENDED) {
            IFluidTankProperties[] tankProperties = handler.getTankProperties();
            for (int i = 0; i < tankProperties.length; i++) {
                IFluidTankProperties properties = tankProperties[i];
                FluidStack fluidStack = properties.getContents();
                String fluidDesc = fluidStack == null ? PneumaticCraftUtils.xlate("gui.liquid.empty") : fluidStack.amount + "mB " + fluidStack.getLocalizedName();
                probeInfo.text(COLOR + "Tank " + (i + 1) + ": " + TextFormatting.AQUA + fluidDesc);
            }
        }
    }

    public static void handleCamo(ProbeMode mode, IProbeInfo probeInfo, BlockState camo) {
        probeInfo.text(TextFormatting.YELLOW + "[Camo: " + ItemCamoApplicator.getCamoStateDisplayName(camo) + "]");
    }
}
