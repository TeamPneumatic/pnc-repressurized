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

package me.desht.pneumaticcraft.api;

import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.fuel.IFuelRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeRegistry;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachineFactory;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorRegistry;
import me.desht.pneumaticcraft.api.wrench.IWrenchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

        IClientRegistry getClientRegistry();

        ISensorRegistry getSensorRegistry();

        IItemRegistry getItemRegistry();

        IUpgradeRegistry getUpgradeRegistry();

        IFuelRegistry getFuelRegistry();

        IWrenchRegistry getWrenchRegistry();

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
        IItemHandler deserializeSmartChest(CompoundTag tag);

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
        void forceClientShapeRecalculation(Level world, BlockPos pos);
    }
}
