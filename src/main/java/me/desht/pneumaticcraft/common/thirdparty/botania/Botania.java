package me.desht.pneumaticcraft.common.thirdparty.botania;

import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraft.item.DyeColor;
import vazkii.botania.api.BotaniaAPI;

public class Botania implements IThirdParty {
    @Override
    public void postInit() {
        PneumaticCraftAPIHandler.getInstance().getItemRegistry().registerMagnetSuppressor(new SolegnoliaHandler());

        for (DyeColor color : DyeColor.values()) {
            BotaniaAPI.instance().registerPaintableBlock(ModBlocks.PLASTIC_BRICKS.get(color.getId()).get(),
                    dyeColor -> ModBlocks.PLASTIC_BRICKS.get(dyeColor.getId()).get());
        }
    }
}
