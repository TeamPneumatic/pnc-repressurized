package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.items.IItemHandler;

/**
 * This class can be used to register and access various things to and from the mod.  All access is via
 * {@link PneumaticRegistry#getInstance()}
 */
public final class PneumaticRegistry {
    public static final String MOD_ID = "pneumaticcraft";

    private static IPneumaticCraftInterface instance;

    /**
     * Get an instance of the top-level API interface.
     *
     * @return the top-level API interface
     */
    public static IPneumaticCraftInterface getInstance() {
        return instance;
    }

    /**
     * Do not call this method yourself.  For PneumaticCraft internal usage only!
     * @param inter reference to the API interface object
     */
    public static void init(IPneumaticCraftInterface inter) {
        if (instance == null && ModLoadingContext.get().getActiveContainer().getModId().equals(MOD_ID))
            instance = inter;//only allow initialization once; by PneumaticCraft
        else throw new IllegalStateException("Only pneumaticcraft is allowed to call this method!");
    }

    /**
     * Get a resource location with the domain of PneumaticCraft: Repressurized's mod ID.
     *
     * @param path the path
     * @return a mod-specific ResourceLocation for the given path
     */
    public static ResourceLocation RL(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /**
     * Retrieve an instance of this via {@link PneumaticRegistry#getInstance()}
     */
    public interface IPneumaticCraftInterface {

        IPneumaticRecipeRegistry getRecipeRegistry();

        IAirHandlerMachineFactory getAirHandlerMachineFactory();

        IPneumaticHelmetRegistry getHelmetRegistry();

        IDroneRegistry getDroneRegistry();

        IHeatRegistry getHeatRegistry();

        IClientRegistry getGuiRegistry();

        ISensorRegistry getSensorRegistry();

        IItemRegistry getItemRegistry();

        IFuelRegistry getFuelRegistry();

        /**
         * Returns the number of Security Stations that disallow interaction with the given coordinate for the given
         * player. Usually you'd disallow interaction when this returns > 0.
         *
         * @param player the player who is trying to access the block
         * @param pos blockpos of the block being tested
         * @param showRangeLines this is ignored and will disappear in a future release
         * @return the number of Security Stations that disallow interaction for the given player.
         * @throws IllegalArgumentException when called from the client side
         * @deprecated use {@link #getProtectingSecurityStations(PlayerEntity, BlockPos)}
         */
        @Deprecated
        int getProtectingSecurityStations(PlayerEntity player, BlockPos pos, boolean showRangeLines);

        /**
         * Returns the number of Security Stations that disallow interaction with the given coordinate for the given
         * player. Usually you'd disallow interaction when this returns > 0.
         *
         * @param player the player who is trying to access the block
         * @param pos blockpos of the block being tested
         * @return the number of Security Stations that disallow interaction for the given player.
         * @throws IllegalArgumentException when called from the client side
         */
        int getProtectingSecurityStations(PlayerEntity player, BlockPos pos);

        /**
         * Register a fluid that represents liquid XP (e.g. PneumaticCraft Memory Essence, CoFH Essence of
         * Knowledge, or OpenBlocks Liquid XP). This is used in the Aerial Interface to transfer experience to/from
         * the player. See also {@link #registerXPFluid(FluidIngredient, int)}.
         *
         * @param fluid the fluid to register
         * @param liquidToPointRatio the amount of fluid (in mB) for one XP point; use a value of 0 or less to
         *                          unregister this fluid
         * @deprecated use {@link #registerXPFluid(FluidIngredient, int)}
         */
        @Deprecated
        void registerXPFluid(Fluid fluid, int liquidToPointRatio);

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
         * Convenience method to get a resource location in PneumaticCraft: Repressurized's namespace.
         *
         * @param path a path
         * @return a resource location
         * @deprecated use {@link PneumaticRegistry#RL(String)}
         */
        @Deprecated
        ResourceLocation RL(String path);

        /**
         * Sync a global variable from server to client for the given player. Primarily intended for use by
         * {@link me.desht.pneumaticcraft.api.item.IPositionProvider#syncVariables(ServerPlayerEntity, ItemStack)}
         *
         * @param player the player to sync to
         * @param varName the global variable name (with or without the leading '#')
         */
        void syncGlobalVariable(ServerPlayerEntity player, String varName);

        /**
         * Register a custom player matcher object. This is safe to call from a
         * {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} handler. Note that matchers should be
         * able to run on both client and server.
         *
         * @param id matcher ID, can be used as a key in recipe JSON's
         * @param factory a factory object used to create instances of this matcher from JSON or a packet buffer
         */
        void registerPlayerMatcher(ResourceLocation id, IPlayerMatcher.MatcherFactory<?> factory);

        /**
         * Return a Smart Chest item handler properly deserialized from the supplied NBT. Not for general use; here
         * to help with Create compatibility, using Smart Chests as part of Create contraptions.
         * @param tag NBT to be deserialized, previously serialized from a Smart Chest
         * @return an item handler deserialized by the Smart Chest
         */
        IItemHandler deserializeSmartChest(CompoundNBT tag);

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
         */
        void forceClientShapeRecalculation(World world, BlockPos pos);
    }
}
