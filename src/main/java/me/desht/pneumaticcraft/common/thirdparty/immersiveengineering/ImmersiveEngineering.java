package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ImmersiveEngineering implements IThirdParty {

    @ObjectHolder("immersiveengineering:hemp")
    private static Block HEMP_BLOCK = null;

    @ObjectHolder("immersiveengineering:biodiesel")
    private static Fluid IE_BIODIESEL = null;

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(ElectricAttackHandler.class);
        IEHeatHandler.registerHeatHandler();

        DieselHandler.registerFuel(ModFluids.DIESEL.get(), 125);  // equivalent to IE biodiesel

        if (IE_BIODIESEL != null && IE_BIODIESEL != Fluids.EMPTY) {
            // equivalent to PNC:R diesel
            PneumaticRegistry.getInstance().getFuelRegistry().registerFuel(IE_BIODIESEL, 1000000, 0.8f);
        } else if (ModList.get().isLoaded(ModIds.IMMERSIVE_ENGINEERING)) {
            Log.error("fluid 'immersive:engineering biodiesel' did not get registered? not adding it as a PneumaticCraft fuel!");
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Listener {
        @SubscribeEvent
        public static void registerHarvestHandler(RegistryEvent.Register<HarvestHandler> event) {
            if (HEMP_BLOCK == null && ModList.get().isLoaded(ModIds.IMMERSIVE_ENGINEERING)) {
                Log.error("block 'immersiveengineering:hemp' did not get registered? PneumaticCraft drone harvesting won't work!");
            }
            event.getRegistry().register(new HarvestHandlerCactusLike(state -> HEMP_BLOCK != null && state.getBlock() == HEMP_BLOCK)
                    .setRegistryName(RL("ie_hemp")));
        }
    }
}
