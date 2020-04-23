package me.desht.pneumaticcraft.common.thirdparty.botania;

import me.desht.pneumaticcraft.api.item.IMagnetSuppressor;
import net.minecraft.entity.Entity;
import vazkii.botania.api.BotaniaAPI;

public class SolegnoliaHandler implements IMagnetSuppressor {
    @Override
    public boolean shouldSuppressMagnet(Entity e) {
        return BotaniaAPI.instance().internalHandler().hasSolegnoliaAround(e);
    }
}
