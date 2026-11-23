package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.item.ICustomTooltipName;
import me.desht.pneumaticcraft.common.item.PNCBlockItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.item.DyeColor;

public class SmoothPlasticBrickBlock extends PlasticBrickBlock {
    public SmoothPlasticBrickBlock(Properties props, DyeColor dyeColor) {
        super(props, dyeColor);
    }

    @Override
    protected boolean hurtsToStepOn() {
        return false;
    }

    public static class SmoothPlasticBrickItem extends PNCBlockItem implements ICustomTooltipName {
        public SmoothPlasticBrickItem(SmoothPlasticBrickBlock blockPlasticBrick) {
            super(blockPlasticBrick, ModItems.defaultProps());
        }

        @Override
        public String getCustomTooltipTranslationKey() {
            return "block.pneumaticcraft.smooth_plastic_brick";
        }
    }
}
