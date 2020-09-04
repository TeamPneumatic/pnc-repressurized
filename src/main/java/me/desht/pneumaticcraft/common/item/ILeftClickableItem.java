package me.desht.pneumaticcraft.common.item;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface ILeftClickableItem {
    void onLeftClickEmpty(ServerPlayerEntity sender);
}
