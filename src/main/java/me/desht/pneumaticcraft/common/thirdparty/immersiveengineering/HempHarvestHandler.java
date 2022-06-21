package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class HempHarvestHandler extends HarvestHandlerCactusLike {
    private static final ResourceLocation HEMP_ID = new ResourceLocation("immersiveengineering:hemp");
    private static Block HEMP = null;

    public HempHarvestHandler() {
        super(HempHarvestHandler::isIEHemp);
    }

    private static boolean isIEHemp(BlockState state) {
        if (HEMP == null) {
            HEMP = ForgeRegistries.BLOCKS.getValue(HEMP_ID);
        }
        return state.getBlock() == HEMP;
    }
}
