package me.desht.pneumaticcraft.api.hacking;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

/**
 * Manages the list of "hacks" on an entity. Hacks are added via the Pneumatic Helmet
 * hacking feature.  This interface is exposed via an entity capability; retrieve a
 * <code>Capability&lt;IHacking&gt;</code> via
 * {@link net.minecraftforge.common.capabilities.CapabilityInject capability injection}.
 */
public interface IHacking extends INBTSerializable<CompoundNBT> {
    /**
     * Called every tick on every entity which has been hacked (i.e. which has a non-empty list of hacks)
     *
     * @param entity the hacked entity
     */
    void update(Entity entity);

    /**
     * Add a new hack to the entity's list of hacks.
     * @param hackable a hack
     */
    void addHackable(IHackableEntity hackable);

    /**
     * Get a list of the hacks currently on the entity.
     *
     * @return a list of hacks
     */
    List<IHackableEntity> getCurrentHacks();
}
