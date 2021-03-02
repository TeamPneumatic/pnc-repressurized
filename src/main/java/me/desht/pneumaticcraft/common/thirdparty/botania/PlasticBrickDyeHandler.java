package me.desht.pneumaticcraft.common.thirdparty.botania;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.item.DyeColor;
import vazkii.botania.api.BotaniaAPI;

public class PlasticBrickDyeHandler {
    static void setup() {
        for (DyeColor color : DyeColor.values()) {
            BotaniaAPI.instance().registerPaintableBlock(ModBlocks.PLASTIC_BRICKS.get(color.getId()).get(),
                    dyeColor -> ModBlocks.PLASTIC_BRICKS.get(dyeColor.getId()).get());
        }
    }
}
