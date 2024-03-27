package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;

public class SmoothPlasticBrickBlock extends PlasticBrickBlock {
    public SmoothPlasticBrickBlock(DyeColor dyeColor) {
        super(ModBlocks.defaultProps().sound(SoundType.WOOD).strength(2f).speedFactor(1.35f), dyeColor);
    }

    @Override
    protected boolean hurtsToStepOn() {
        return false;
    }

    public static class SmoothPlasticBrickItem extends BlockItem implements ICustomTooltipName {
        public SmoothPlasticBrickItem(SmoothPlasticBrickBlock blockPlasticBrick) {
            super(blockPlasticBrick, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return "block.pneumaticcraft.smooth_plastic_brick";
        }
    }
}
