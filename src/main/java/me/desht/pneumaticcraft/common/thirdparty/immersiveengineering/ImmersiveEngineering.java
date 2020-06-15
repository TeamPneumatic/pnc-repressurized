package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ImmersiveEngineering implements IThirdParty {

    @ObjectHolder("immersiveengineering:hemp")
    private static Block HEMP_BLOCK = null;

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(ElectricAttackHandler.class);
        IEHeatHandler.registerHeatHandler();
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Listener {
        @SubscribeEvent
        public static void registerHarvestHandler(RegistryEvent.Register<HarvestHandler> event) {
            if (HEMP_BLOCK != null) {
                event.getRegistry().register(new HarvestHandlerCactusLike(state -> state.getBlock() == HEMP_BLOCK).setRegistryName(RL("ie_hemp")));
            } else {
                Log.error("Could not find Immersive Engineering's Hemp block 'immersiveengineering:hemp'! Harvesting this block is not supported!");
            }
        }
    }
}
