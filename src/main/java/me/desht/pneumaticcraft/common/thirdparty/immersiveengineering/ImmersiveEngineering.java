package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;

@GameRegistry.ObjectHolder(ModIds.IMMERSIVEENGINEERING)
public class ImmersiveEngineering implements IThirdParty{

    @GameRegistry.ObjectHolder("hemp")
    public static final Block HEMP_BLOCK = null;
    
    @Override
    public void postInit() {
        if(HEMP_BLOCK != null){
            PneumaticCraftAPIHandler.getInstance().getHarvestRegistry().registerHarvestHandlerCactuslike(state -> state.getBlock() == HEMP_BLOCK);
        }else{
            Log.error("Could not find Immersive Engineering's Hemp block! Harvesting this block is not supported!");
        }
    }
}
