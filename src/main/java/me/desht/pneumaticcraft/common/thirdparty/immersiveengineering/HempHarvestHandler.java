package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HempHarvestHandler extends HarvestHandlerCactusLike {
    private static final ResourceLocation HEMP_ID = new ResourceLocation("immersiveengineering:hemp");
    private static Block HEMP = null;

    public HempHarvestHandler() {
        super(HempHarvestHandler::isIEHemp);
    }

    private static boolean isIEHemp(BlockState state) {
        if (HEMP == null) {
            HEMP = BuiltInRegistries.BLOCK.get(HEMP_ID);
        }
        return !state.isAir() && state.getBlock() == HEMP;
    }
}
