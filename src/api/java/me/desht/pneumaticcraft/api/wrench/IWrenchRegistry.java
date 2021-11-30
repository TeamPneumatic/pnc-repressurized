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

package me.desht.pneumaticcraft.api.wrench;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Some utilities for interacting with third-party wrench behaviours.
 *
 * Get an instance of this with {@link PneumaticRegistry.IPneumaticCraftInterface#getWrenchRegistry()}
 */
public interface IWrenchRegistry {
    /**
     * Check if the given item is considered as a modded wrench (so does not include the Pneumatic Wrench).
     * PneumaticCraft is by default aware of wrenches from several well-known mods. Note that items added to the
     * <code>forge:tools/wrench</code> item tag will also be considered as modded wrenches.
     *
     * @param stack the item to check
     * @return true if it's a modded wrench, false otherwise
     */
    boolean isModdedWrench(@Nonnull ItemStack stack);

    /**
     * Check if the given item is <strong>any</strong> known wrench item, including the Pneumatic Wrench.
     *
     * @param stack the item to check
     * @return true if it's a wrench, false otherwise
     */
    boolean isWrench(@Nonnull ItemStack stack);

    /**
     * Register the given item as a wrench, as far as PneumaticCraft is concerned. Note that the preferred way to do
     * this is to add the item to the <code>forge:tools/wrench</code> item tag.
     *
     * @param wrench a wrench item
     */
    void registerWrench(Item wrench);

    /**
     * Register a third-party behaviour for when blocks from another mod are wrenched with the Pneumatic Wrench.
     * <p>
     * The return
     * value from <code>behaviourPre</code> is also returned by {@link Item#onItemUseFirst(ItemStack, ItemUseContext)}
     * when the Pneumatic Wrench is used. Any return value other than PASS will suppress the default Pneumatic Wrench
     * behaviour (including air usage); returning CONSUME will also suppress the Pneumatic Wrench's sound effect.
     *<p>
     * The code in <code>behaviourPost</code> will only be run if <code>behaviourPre</code> returned PASS (i.e. did
     * not suppress the default Pneumatic Wrench behaviour).
     *
     * @param modid the mod whose blocks to register the behaviour for; must exactly match the mod's namespace
     * @param behaviourPre code to run when blocks from the mod are about to be wrenched with the Pneumatic Wrench
     * @param behaviourPost code to run when blocks from the mod have just been wrenched with the Pneumatic Wrench
     */
    void addModdedWrenchBehaviour(String modid,
                                  BiFunction<ItemUseContext, BlockState, ActionResultType> behaviourPre,
                                  BiConsumer<ItemUseContext, BlockState> behaviourPost);
}
