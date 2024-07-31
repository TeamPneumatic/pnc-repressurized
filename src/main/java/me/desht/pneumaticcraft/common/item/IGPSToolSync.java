package me.desht.pneumaticcraft.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IGPSToolSync {
    /**
     * Called when the GPS (Area) Tool GUI is closed, to send position &amp; variable information to server
     *
     * @param player      the player
     * @param stack       the gps (area) tool itemstack
     * @param index       index of the pos &amp; var (ignore for GPS Tool)
     * @param pos         the new position for this index
     * @param varName     the new variable name for this index (empty string if absent)
     * @param activeIndex
     */
    void syncFromClient(Player player, ItemStack stack, int index, BlockPos pos, String varName, boolean activeIndex);
}
