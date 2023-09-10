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

package me.desht.pneumaticcraft.api.pneumatic_armor;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Retrieve an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getCommonArmorRegistry()}
 * <p>
 * Contains some common registration &amp; query methods for Pneumatic Armor.
 */
public interface ICommonArmorRegistry {
    /**
     * Register a common (client and server) handler for a Pneumatic Armor upgrade.  This must be called from a
     * {@link FMLCommonSetupEvent} handler
     * (do <strong>not</strong> use {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}).
     * @param handler the handler to register
     */
    @Nonnull
    <T extends IArmorUpgradeHandler<?>> T registerUpgradeHandler(@Nonnull T handler);

    /**
     * Retrieve the {@link ICommonArmorHandler} for the given player. Note that this will return a non-null result even
     * if the player isn't currently wearing any Pneumatic Armor.
     *
     * @param player the player who is wearing one or more pieces of Pneumatic Armor
     * @return the common armor handler
     */
    @Nonnull
    ICommonArmorHandler getCommonArmorHandler(Player player);

    /**
     * Register a "foreign" entity with your hackable. This should be used for entities you didn't create, i.e.
     * vanilla or from a different mod.  For your own entities, just have your entity implement {@link IHackableEntity}.
     * <p>
     * This must be called from a {@link FMLCommonSetupEvent} handler
     * (it is not necessary to use {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}).
     *
     * @param entityClazz entity class; subclasses of this entity will also be affected
     * @param iHackable the hack to register
     */
    void addHackable(@Nonnull Class<? extends Entity> entityClazz, @Nonnull Supplier<? extends IHackableEntity<?>> iHackable);

    /**
     * Register a "foreign" block with your hackable. This should be used for blocks you didn't create, i.e.
     * vanilla or from a different mod.  For your own blocks, just have your block implement {@link IHackableBlock}.
     * <p>
     * This must be called from a {@link FMLCommonSetupEvent} handler
     * (it is not necessary to use {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}).
     *
     * @param block the block class; subclasses of this block will also be affected
     * @param iHackable the hack to register
     */
    void addHackable(@Nonnull Block block, @Nonnull Supplier<? extends IHackableBlock> iHackable);

    /**
     * Register a block tag with your hackable. By default, the vanilla doors, buttons and trapdoors block tags are
     * registered, meaning any block added to any of those tags (e.g. modded doors) will also be considered hackable.
     * <p>
     * This must be called from a {@link FMLCommonSetupEvent} handler
     * (it is not necessary to use {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}).
     *
     * @param blockTag the block tag to register
     * @param iHackable the hack to register
     */
    void addHackable(@Nonnull TagKey<Block> blockTag, @Nonnull Supplier<? extends IHackableBlock> iHackable);

    /**
     * Get a list of all current successful hacks on a given entity. This is used for example in Enderman hacking, so
     * the user can only hack an enderman once (more times wouldn't have any effect). This is mostly used for display
     * purposes.
     *
     * @param entity the entity to check
     * @return a list of hacks currently on the entity, or an empty list if no hacks
     */
    @Nonnull
    Collection<IHackableEntity<?>> getCurrentEntityHacks(@Nonnull Entity entity);

    /**
     * Register a block entity as being able to have a loot table for the purposes of dungeon-style loot generation.
     * This is for the benefit of the Pneumatic Helmet Block Tracker module (inventory scanning).
     * <p>
     * The supplied consumer must check that the block entity is of the appropriate type, and that the player may loot
     * the chest (e.g. vanilla locking is honoured), and if so generate the chest's loot as if the player had just
     * opened the chest.
     * <p>
     * Vanilla chests and PneumaticCraft chests are registered with this by default; this method can be used for third
     * party mods which add chests or other inventories with their own custom loot tables.
     *
     * @param consumer consumer accepting a player (who is doing the scanning) and the block entity of interest
     */
    void registerBlockTrackerLootable(BiConsumer<Player, BlockEntity> consumer);
}
