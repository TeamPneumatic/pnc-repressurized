package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public class HempHarvestHandler extends HarvestHandlerCactusLike {
    private static final ResourceLocation HEMP_ID = ResourceLocation.fromNamespaceAndPath(ModIds.IMMERSIVE_ENGINEERING, "hemp");
    private static final Lazy<Block> HEMP = Lazy.of(() -> BuiltInRegistries.BLOCK.get(HEMP_ID));

    public HempHarvestHandler() {
        super(HempHarvestHandler::isIEHemp);
    }

    private static boolean isIEHemp(BlockState state) {
        return !state.isAir() && state.getBlock() == HEMP.get();
    }
}
