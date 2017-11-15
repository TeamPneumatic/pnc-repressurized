package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

import java.util.List;

public interface IPneumaticHelmetRegistry {
    void registerEntityTrackEntry(Class<? extends IEntityTrackEntry> entry);

    void registerBlockTrackEntry(IBlockTrackEntry entry);

    void addHackable(Class<? extends Entity> entityClazz, Class<? extends IHackableEntity> iHackable);

    void addHackable(Block block, Class<? extends IHackableBlock> iHackable);

    /**
     * Returns a list of all current successful hacks of a given entity. This is used for example in Enderman hacking, so the user
     * can only hack an enderman once (more times wouldn't have any effect). This is mostly used for display purposes.
     *
     * @param entity
     * @return empty list if no hacks.
     */
    List<IHackableEntity> getCurrentEntityHacks(Entity entity);

    /**
     * Registers a Pneumatic Helmet module
     *
     * @param renderHandler
     */
    void registerRenderHandler(IUpgradeRenderHandler renderHandler);
}
