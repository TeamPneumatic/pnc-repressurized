/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.pneumatic_armor.hacking;

import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Use this interface to specify any hackable entity. When it's your entity, you can simply implement this interface in
 * the entity's class. If you don't have access to the entity (i.e. vanilla entities or entities from other mods), you
 * can implement this interface in a separate class and register it using
 * {@link ICommonArmorRegistry#addHackable(Class, java.util.function.Supplier)}.
 * Either way, there will be an IHackableEntity instance for every entity.
 * @param <T> target entity class
 */
public interface IHackableEntity<T extends Entity> {
    /**
     * Should return a unique id to represent this hackable.
     *
     * @return a unique String id
     */
    @Nonnull
    ResourceLocation getHackableId();

    /**
     * Get the class of the entity this hack should apply to. All subclasses of this class will also be applicable.
     * @return the target entity class
     */
    @Nonnull
    Class<T> getHackableClass();

    /**
     * Returning true will allow the player to hack this entity. This can be used to only allow hacking under certain
     * conditions. Default implementation just checks the entity's class is appropriate and the entity is alive, but
     * this can be overridden to add extract checks. Just be sure to call this super method in your overridden method.
     *
     * @param entity the potential hacking target
     * @param player the player who is potentially hacking the target entity
     */
    default boolean canHack(Entity entity, Player player) {
        return entity.isAlive() && getHackableClass().isAssignableFrom(entity.getClass());
    }

    /**
     * Add info that is displayed on the entity tracker panel, describing what the hack would do to the entity. This is
     * only called if {@link #canHack(Entity, Player)} returned true. Keep this message short; one short phrase is enough.
     *
     * @param entity the potential hack target
     * @param curInfo a text component list to append info to
     * @param player the player who is potentially hacking the target entity
     */
    void addHackInfo(T entity, List<Component> curInfo, Player player);

    /**
     * Add info that is displayed on the entity tracker panel, describing what the hack has done to the entity. This
     * is displayed for a second or so after the hack completes.
     *
     * @param entity the hacked target
     * @param curInfo a text component list to append info to
     * @param player the player who has hacked the entity
     */
    void addPostHackInfo(T entity, List<Component> curInfo, Player player);

    /**
     * Return the time it takes to hack this entity in ticks. Most builtin PneumaticCraft hacks use a time of 60
     * ticks, but for more powerful hacks, a longer required hacking time is suggested.
     *
     * @param entity the potential hack target
     * @param player the player who is potentially hacking the target entity
     */
    int getHackTime(T entity, Player player);

    /**
     * Called when a player successfully hacks an entity; basically {@code getHackTime(Entity)} ticks after the
     * hack was initiated.
     *
     * @param entity the hacked entity
     * @param player the player who has hacked the entity
     */
    void onHackFinished(T entity, Player player);

    /**
     * Called every tick after the hack completed. Return false for one-shot hacks, or true to keep this hack ticking.
     * The majority of hacks are one-shot; see also {@link AbstractPersistentEntityHack} for a class to use for
     * persistent hacks.
     * <p>
     * Dead or otherwise removed entities will automatically be removed from the after-hack tick list.
     *
     * @param entity the hacked entity
     * @return true to keep having this method called each tick for the entity, false to stop ticking
     */
    default boolean afterHackTick(T entity) {
        return false;
    }

    /* =============================================================================
     * Do not use or override any methods below here! Only here for generics support.
     */

    @ApiStatus.Internal
    default boolean _afterHackTick(Entity e) {
        //noinspection unchecked
        return getHackableClass().isAssignableFrom(e.getClass()) && afterHackTick((T) e);
    }
    @ApiStatus.Internal
    default int _getHackTime(Entity e, Player p) {
        //noinspection unchecked
        return getHackableClass().isAssignableFrom(e.getClass()) ? getHackTime((T) e, p) : -1;
    }
    @ApiStatus.Internal
    default void _onHackFinished(Entity e, Player p) {
        if (getHackableClass().isAssignableFrom(e.getClass())) {
            //noinspection unchecked
            onHackFinished((T) e, p);
        }
    }
    @ApiStatus.Internal
    default void _addHackInfo(Entity e, List<Component> componentList, Player p) {
        if (getHackableClass().isAssignableFrom(e.getClass())) {
            //noinspection unchecked
            addHackInfo((T) e, componentList, p);
        }
    }
    @ApiStatus.Internal
    default void _addPostHackInfo(Entity e, List<Component> componentList, Player p) {
        if (getHackableClass().isAssignableFrom(e.getClass())) {
            //noinspection unchecked
            addPostHackInfo((T) e, componentList, p);
        }
    }
}
