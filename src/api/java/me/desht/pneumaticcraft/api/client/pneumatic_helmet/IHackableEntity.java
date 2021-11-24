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

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Use this interface to specify any hackable entity. When it's your entity, you can simply implement this interface in
 * the entity's class. If you don't have access to the entity (i.e. vanilla entities or entities from other mods), you
 * can implement this interface in a separate class and register it using
 * {@link IPneumaticHelmetRegistry#addHackable(Class, java.util.function.Supplier)}.
 * Either way, there will be an IHackableEntity instance for every entity.
 */
public interface IHackableEntity {
    /**
     * Should return a unique id to represent this hackable. Used in NBT saving to be able to trigger the afterHackTime
     * after a server restart. Null is a valid return: afterHackTick will not be triggered at all in that case.
     *
     * @return a unique String id
     */
    @Nullable
    ResourceLocation getHackableId();

    /**
     * Returning true will allow the player to hack this entity. This can be used to only allow hacking under certain
     * conditions.
     *
     * @param entity the potential hacking target
     * @param player the player who is looking at the entity
     */
    boolean canHack(Entity entity, PlayerEntity player);

    /**
     * Add info that is displayed on the tracker tooltip here. Text like "Hack to explode" can be added.
     * This method is only called when canHack(Entity) returned true.
     * @param entity the potential hack target
     * @param curInfo a text component list to append info to
     * @param player the player who is looking at the entity
     */
    void addHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player);

    /**
     * Add info that is being displayed after hacking, as long as 'afterHackTick' is returning true.
     * Things like "Neutralized".
     * @param entity the hacked entity
     * @param curInfo a text component list to append info to
     * @param player the player who has hacked the entity
     */
    void addPostHackInfo(Entity entity, List<ITextComponent> curInfo, PlayerEntity player);

    /**
     * Return the time it takes to hack this entity in ticks. For more powerful hacks, a longer required hacking time
     * is recommended.
     *
     * @param entity the potential hack target
     * @param player the player who is looking at the entity
     */
    int getHackTime(Entity entity, PlayerEntity player);

    /**
     * Called when a player successfully hacks an entity; basically {@code getHackTime(Entity)} ticks after the
     * hack was initiated.
     *
     * @param entity the hacked entity
     * @param player the player who has hacked the entity
     */
    void onHackFinished(Entity entity, PlayerEntity player);

    /**
     * Called every tick after the hacking finished. Returning true will keep this going (e.g. for endermen, to suppress
     * their teleportation) or false for one-shot hacks (e.g. hacking a cow turns it into a mooshroom, and that is all)
     *
     * @param entity the hacked entity
     */
    boolean afterHackTick(Entity entity);
}
