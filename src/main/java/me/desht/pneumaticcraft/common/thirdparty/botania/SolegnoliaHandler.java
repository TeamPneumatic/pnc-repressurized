package me.desht.pneumaticcraft.common.thirdparty.botania;

import me.desht.pneumaticcraft.api.item.IMagnetSuppressor;
import net.minecraft.entity.Entity;

public class SolegnoliaHandler implements IMagnetSuppressor {
    @Override
    public boolean shouldSuppressMagnet(Entity e) {
        return false;
//        return BotaniaAPI.internalHandler.hasSolegnoliaAround(e);
    }
}
