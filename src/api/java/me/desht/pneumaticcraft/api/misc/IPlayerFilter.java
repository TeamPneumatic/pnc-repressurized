package me.desht.pneumaticcraft.api.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Predicate;

/**
 * A player filter is a collection of individual matcher objects with either match-any or match-all behaviour.
 * Custom matcher objects can be registered and have full codec/stream-codec support, so can be used in recipes etc.
 */
@ApiStatus.NonExtendable
public interface IPlayerFilter extends Predicate<Player> {
    boolean isReal();

    boolean matchAll();

    void getDescription(Player player, List<Component> tooltip);

    List<IPlayerMatcher> matchers();
}
