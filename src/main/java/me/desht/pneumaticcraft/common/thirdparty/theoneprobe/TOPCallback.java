package me.desht.pneumaticcraft.common.thirdparty.theoneprobe;

import mcjty.theoneprobe.api.*;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockBasic;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TOPCallback implements Function<ITheOneProbe, Void> {
    public static ITheOneProbe probe;

    public static int elementPressure;

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        probe = theOneProbe;
        PneumaticCraftRepressurized.logger.info("Enabled support for The One Probe");

        elementPressure = probe.registerElementFactory(ElementPressure::new);

        probe.registerProvider(new IProbeInfoProvider() {
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
                ISemiBlock semiBlock = SemiBlockManager.getInstance(world).getSemiBlock(world, data.getPos());
                if (semiBlock != null) {
                    handleSemiblock(mode, probeInfo, semiBlock);
                }
            }
        });

        probe.registerEntityProvider(new IProbeInfoEntityProvider() {
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

    public static void handlePneumatic(ProbeMode mode, IProbeInfo probeInfo, TileEntityPneumaticBase te) {
        IAirHandler airHandler = te.getAirHandler(null);
        probeInfo.horizontal()
                .element(new ElementPressure(te))
                .vertical()
                .text("")
                .text("  \u2b05 " + PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1) + " bar");
    }

    public static void handleHeat(ProbeMode mode, IProbeInfo probeInfo, IHeatExchanger heatExchanger) {
        HeatExchangerManager.TemperatureData tempData = new HeatExchangerManager.TemperatureData(heatExchanger);
        if (tempData.isMultisided()) {
            for (EnumFacing face : EnumFacing.values()) {
                if (tempData.hasData(face)) {
                    int tempInt = (int) tempData.getTemperature(face) - 273;
                    probeInfo.text(I18n.format("waila.temperature." + face, tempInt));
                }
            }
        } else if (tempData.hasData(null)) {
            int tempInt = (int) tempData.getTemperature(null) - 273;
            probeInfo.text(I18n.format("waila.temperature", tempInt));
        }
    }

    public static void handleSemiblock(ProbeMode mode, IProbeInfo probeInfo, ISemiBlock semiBlock) {
        List<String> currenttip = new ArrayList<>();
        if (semiBlock instanceof SemiBlockBasic) {
            ((SemiBlockBasic) semiBlock).addWailaTooltip(currenttip, null);
        }
        for (String s : currenttip) {
            probeInfo.text(s);
        }
    }

    public static void handleRedstoneMode(ProbeMode mode, IProbeInfo probeInfo, TileEntityBase te) {
        if (te instanceof IRedstoneControl) {
            int redstoneMode = ((IRedstoneControl) te).getRedstoneMode();
            probeInfo.text(TextFormatting.RED + I18n.format(te.getRedstoneString()) + ": " + I18n.format(te.getRedstoneButtonText(redstoneMode)));
        }
    }

    public static void handlePressureTube(ProbeMode mode, IProbeInfo probeInfo, TileEntityPressureTube te, EnumFacing face) {
        if (face != null) {
            TubeModule module = te.modules[face.ordinal()];
            if (module != null) {
                List<String> currenttip = new ArrayList<>();
                module.addInfo(currenttip);
                for (String s : currenttip) probeInfo.text(s);
            }
        }
    }
}
