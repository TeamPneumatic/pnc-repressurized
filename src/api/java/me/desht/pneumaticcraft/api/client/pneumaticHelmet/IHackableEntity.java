package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Use this interface to specify any hackable entity. When it's your entity, you can simply implement this interface in
 * the entity's class. If you don't have access to the class (vanilla entities), you can implement this interface in a
 * separate class and register it using {@link IPneumaticHelmetRegistry#addHackable(Class, Class)}.
 * Either way, there will be an IHackableEntity instance for every entity.
 */
public interface IHackableEntity {
    /**
     * Should return a unique id to represent this hackable. Used in NBT saving to be able to trigger the afterHackTime after a server restart.
     * Null is a valid return: afterHackTick will not be triggered at all in that case.
     *
     * @return a unique String id
     */
    String getId();

    /**
     * Returning true will allow the player to hack this entity. This can be used to only allow hacking on certain
     * conditions.
     *
     * @param entity the potential hacking target
     * @param player the player who is looking at the entity
     */
    boolean canHack(Entity entity, EntityPlayer player);

    /**
     * Add info that is displayed on the tracker tooltip here. Text like "Hack to explode" can be added.
     * This method is only called when canHack(Entity) returned true.
     * The added lines automatically will be tried to get localized.
     *
     * @param entity the potential hacking target
     * @param curInfo a string list to append info to
     * @param player the player who is looking at the entity
     */
    void addInfo(Entity entity, List<String> curInfo, EntityPlayer player);

    /**
     * Add info that is being displayed after hacking, as long as 'afterHackTick' is returning true.
     * Things like "Neutralized".
     * The added lines automatically will be tried to get localized.
     *
     * @param entity the hacked entity
     * @param curInfo a string list to append info to
     * @param player the player who has hacked the entity
     */
    void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player);

    /**
     * Return the time it takes to hack this entity in ticks. For more powerful hacks, a longer required hacking time
     * is recommended.
     *
     * @param entity the potential hack target
     * @param player the player who is looking at the entity
     */
    int getHackTime(Entity entity, EntityPlayer player);

    /**
     * Called when a player successfully hacks an entity; basically {@code getHackTime(Entity)} ticks after the
     * hack was initiated.
     *
     * @param entity the hacked entity
     * @param player the player who has hacked the entity
     */
    void onHackFinished(Entity entity, EntityPlayer player);

    /**
     * Called every tick after the hacking finished. Returning true will keep this going (e.g. for endermen, to suppress
     * their teleportation) or false for one-shot hacks (e.g. hacking a cow turns it into a mooshroom, and that is all)
     *
     * @param entity the hacked entity
     */
    boolean afterHackTick(Entity entity);
}
