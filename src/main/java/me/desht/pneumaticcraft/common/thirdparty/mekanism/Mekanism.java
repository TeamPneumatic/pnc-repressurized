package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import mekanism.api.heat.IHeatHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
public class Mekanism implements IThirdParty {
    @CapabilityInject(IHeatHandler.class)
    public static final Capability<IHeatHandler> CAPABILITY_HEAT_HANDLER = null;

    public static boolean available = false;

    @Override
    public void init() {
        available = true;

        IFuelRegistry fuelApi = PneumaticCraftAPIHandler.getInstance().getFuelRegistry();

        // equivalent to LPG
        fuelApi.registerFuel(PneumaticCraftTags.Fluids.forgeTag("ethene"), 1800000, 1.25f);
        // low fuel value, but fast burning
        fuelApi.registerFuel(PneumaticCraftTags.Fluids.forgeTag("hydrogen"), 300000, 1.5f);

//        // takes some effort to make, so it's as good as LPG
//        PneumaticRegistry.getInstance().getFuelRegistry().registerFuel(FluidTags.makeWrapperTag("forge:ethene"), 1800000, 1.25f);
//        // low fuel value, but burns fast
//        PneumaticRegistry.getInstance().getFuelRegistry().registerFuel(FluidTags.makeWrapperTag("forge:hydrogen"), 300000, 2f);
    }

    @SubscribeEvent
    public static void attachHeatAdapters(AttachCapabilitiesEvent<TileEntity> event) {
        if (PNCConfig.Common.Integration.mekThermalEfficiencyFactor != 0 && CAPABILITY_HEAT_HANDLER != null) {
            if (event.getObject().getType().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
                event.addCapability(RL("pnc2mek_heat_adapter"), new PNC2MekHeatProvider(event.getObject()));
            }
            if (event.getObject().getType().getRegistryName().getNamespace().equals(ModIds.MEKANISM)) {
                event.addCapability(RL("mek2pnc_heat_adapter"), new Mek2PNCHeatProvider(event.getObject()));
            }
        }
    }
}
