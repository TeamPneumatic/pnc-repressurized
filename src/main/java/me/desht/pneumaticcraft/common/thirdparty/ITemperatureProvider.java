package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.world.entity.player.Player;

public interface ITemperatureProvider {
    default void tickAirConditioning(Player player) {
    }

    enum None implements ITemperatureProvider {
        INSTANCE;
    }
}
