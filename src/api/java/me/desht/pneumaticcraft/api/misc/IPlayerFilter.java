package me.desht.pneumaticcraft.api.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

public interface IPlayerFilter extends Predicate<Player> {
    boolean isReal();

    boolean matchAll();

    void getDescription(Player player, List<Component> tooltip);

    List<IPlayerMatcher> matchers();
}
