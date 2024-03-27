package me.desht.pneumaticcraft.api.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ILaunchBehaviour {
    /**
     * Used by the Air Cannon and the Pneumatic Chestplate item launcher (Dispenser upgrade).
     *
     * Given an item being launched, and the player (possibly a fake player) doing the launching, get the entity to be
     * launched. Note this is only called for dispenser-like behaviour (non-dispenser behaviour is simply to make an
     * item entity from the item).
     *
     * @param stack the item being launched
     * @param player the player doing the launching
     * @return the entity to be launched
     */
    Entity getEntityToLaunch(ItemStack stack, ServerPlayer player);
}
