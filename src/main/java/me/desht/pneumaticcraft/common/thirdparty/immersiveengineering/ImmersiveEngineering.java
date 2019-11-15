package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ObjectHolder;

public class ImmersiveEngineering implements IThirdParty {

    @ObjectHolder("immersiveengineering:hemp")
    private static Block HEMP_BLOCK = null;

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(ElectricAttackHandler.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerHarvestHandler(RegistryEvent.Register<HarvestHandler> event) {
        if (HEMP_BLOCK != null) {
            event.getRegistry().register(new HarvestHandlerCactusLike(state -> state.getBlock() == HEMP_BLOCK));
        } else {
            Log.error("Could not find Immersive Engineering's Hemp block 'immersiveengineering:hemp'! Harvesting this block is not supported!");
        }
    }
}
