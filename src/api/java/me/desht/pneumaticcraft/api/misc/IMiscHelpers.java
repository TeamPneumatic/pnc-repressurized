package me.desht.pneumaticcraft.api.misc;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A collection of miscellaneous helper methods which don't fit elsewhere. Get an instance of this with
 * {@link PneumaticRegistry.IPneumaticCraftInterface#getMiscHelpers()}.
 */
public interface IMiscHelpers {
    /**
     * Returns the number of Security Stations that disallow interaction with the given coordinate for the given
     * player. Usually you'd disallow interaction when this returns > 0.
     *
     * @param player the player who is trying to access the block
     * @param pos blockpos of the block being tested
     * @return the number of Security Stations that disallow interaction for the given player.
     * @throws IllegalArgumentException when called from the client side
     */
    int getProtectingSecurityStations(Player player, BlockPos pos);

    /**
     * Register a fluid ingredient that represents liquid XP. This ingredient could be a fluid, or a fluid tag,
     * or even a stream of fluid ingredients.
     *
     * Note that a fluid ingredient of the "forge:experience" fluid tag is registered by default with a ratio of
     * 20mb per XP; this tag includes PneumaticCraft Memory Essence, and possibly other modded XP fluids too.
     *
     * @param fluid the fluid tag to register; all fluids in this tag will have the given XP value
     * @param liquidToPointRatio the amount of fluid (in mB) for one XP point; use a value of 0 or less to
     *                          unregister all fluids matching this fluid ingredient
     */
    void registerXPFluid(FluidIngredient fluid, int liquidToPointRatio);

    /**
     * Sync a global variable from server to client for the given player. Primarily intended for use by
     * {@link me.desht.pneumaticcraft.api.item.IPositionProvider#syncVariables(ServerPlayer, ItemStack)}
     *
     * @param player the player to sync to
     * @param varName the global variable name (with or without the leading '#')
     */
    void syncGlobalVariable(ServerPlayer player, String varName);

    /**
     * Return a Smart Chest item handler properly deserialized from the supplied NBT. Not for general use; here
     * to help with Create compatibility, using Smart Chests as part of Create contraptions.
     *
     * @param tag      NBT to be deserialized, previously serialized from a Smart Chest
     * @param provider the lookup provider
     * @return an item handler deserialized by the Smart Chest
     */
    IItemHandler deserializeSmartChest(CompoundTag tag, HolderLookup.Provider provider);

    /**
     * Notify tracking clients to recalculate the block shapes of all neighbours of the block at the given world
     * and position. You should call this for any blocks which can connect pneumatically to neighbours when those
     * blocks are changed server-side only (e.g. rotated, sneak-wrenched). This should only be called server-side
     * (it is no-op if called on the client).
     * <p>
     * This is a bit of a kludge, but necessary since blocks do not normally get signalled about neighbour changes
     * on the client, which is needed for blocks such as Pressure Tubes to recalculate their cached block shapes.
     *
     * @param world the world
     * @param pos the position of the block that has been changed or removed
     * @deprecated this kludge is no longer necessary as of v8.0.4
     */
    @Deprecated(since = "8.0.4", forRemoval = true)
    void forceClientShapeRecalculation(Level world, BlockPos pos);

    /**
     * Play the standard PNC effect when a pneumatic machine (which contains some air) is broken with a pickaxe;
     * a puff of air particles, and a short pneumatic "hiss". This is intended to be called from the overridden
     * {@link net.minecraft.world.level.block.Block#onRemove(BlockState, Level, BlockPos, BlockState, boolean)} in
     * your block objects.
     *
     * @param blockEntity the block entity of the machine being broken
     */
    void playMachineBreakEffect(BlockEntity blockEntity);

    /**
     * Get an air particle data object, suitable for passing to {@link Level#addParticle(ParticleOptions, double, double, double, double, double, double)}
     * and related methods.
     *
     * @return the air particle data
     */
    ParticleOptions airParticle();

    /**
     * Get the hacking manager for the given entity; this manager allows querying and modifying the list of active
     * hacks on the entity.
     * @param entity the entity
     * @param create if true, create a new list if necessary
     * @return the hack manager, if present
     */
    Optional<? extends IActiveEntityHacks> getHackingForEntity(Entity entity, boolean create);

    /**
     * Get the global variable helper; this helper allows querying and modifying global variables, as used by drones,
     * the Universal Sensor, and the Remote item.
     *
     * @return the global variable helper
     */
    IGlobalVariableHelper getGlobalVariableHelper();

    /**
     * Register a custom modifier for entity filtering.
     *
     * @param name the modifier name
     * @param options a set of valid options for the modifier values
     * @param predicate a predicate to test if the modifier applies to the given entity
     */
    void registerEntityFilterModifier(String name, Set<String> options, BiPredicate<Entity,String> predicate);

    /**
     * Register a custom modifier for entity filtering.
     *
     * @param name the modifier name
     * @param validator a predicate to test if the given string is a valid possible value for this modifier
     * @param desc a freeform description of what the valid values for this modifier are (keep it brief)
     * @param predicate a predicate to test if the modifier applies to the given entity
     */
    void registerEntityFilterModifier(String name, Predicate<String> validator, String desc, BiPredicate<Entity,String> predicate);
}
