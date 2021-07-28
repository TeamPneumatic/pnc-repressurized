package me.desht.pneumaticcraft.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IGPSToolSync {
    /**
     * Called when the GPS (Area) Tool GUI is closed, to send position & variable information to server
     * @param player the player
     * @param stack the gps (area) tool itemstack
     * @param index index of the pos & var (ignore for GPS Tool)
     * @param pos the new position for this index
     * @param varName the new variable name for this index (empty string if absent)
     */
    void syncFromClient(PlayerEntity player, ItemStack stack, int index, BlockPos pos, String varName);
}
