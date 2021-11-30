/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.wrench.IWrenchRegistry;
import me.desht.pneumaticcraft.common.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.item.ItemPneumaticWrench;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public enum ModdedWrenchUtils implements IWrenchRegistry {
    INSTANCE;

    private final Set<ResourceLocation> wrenches = new HashSet<>();

    private static final BiFunction<ItemUseContext,BlockState,ActionResultType> NO_OP_PRE = (ctx, state) -> ActionResultType.PASS;
    private static final BiConsumer<ItemUseContext,BlockState> NO_OP_POST = (ctx, state) -> {};

    private final Map<String, BiFunction<ItemUseContext,BlockState,ActionResultType>> modBehavioursPre = new Object2ObjectOpenHashMap<>();
    private final Map<String, BiConsumer<ItemUseContext,BlockState>> modBehavioursPost = new Object2ObjectOpenHashMap<>();

    public static ModdedWrenchUtils getInstance() {
        return INSTANCE;
    }

    void registerThirdPartyWrenches() {
        // some well-known wrenches. item tag "forge:tools/wrench" can also be used to detect a wrench item
        registerWrench("thermalfoundation:wrench");
        registerWrench("rftools:smartwrench");
        registerWrench("immersiveengineering:hammer");
        registerWrench("appliedenergistics2:certus_quartz_wrench");
        registerWrench("appliedenergistics2:nether_quartz_wrench");
        registerWrench("enderio:item_yeta_wrench");
        registerWrench("buildcraftcore:wrench");
        registerWrench("teslacorelib:wrench");
        registerWrench("ic2:wrench");
        registerWrench("chiselsandbits:wrench_wood");
        registerWrench("mekanism:configurator");
    }

    private void registerWrench(String wrenchId) {
        wrenches.add(new ResourceLocation(wrenchId));
    }

    @Override
    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof ItemPneumaticWrench) &&
                (stack.getItem().is(PneumaticCraftTags.Items.WRENCHES) || wrenches.contains(stack.getItem().getRegistryName()));
    }

    @Override
    public boolean isWrench(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemPneumaticWrench || isModdedWrench(stack);
    }

    @Override
    public void registerWrench(Item wrench) {
        wrenches.add(wrench.getRegistryName());
    }

    @Override
    public void addModdedWrenchBehaviour(String modid, BiFunction<ItemUseContext,BlockState,ActionResultType> behaviourPre, BiConsumer<ItemUseContext,BlockState> behaviourPost) {
        modBehavioursPre.put(modid, behaviourPre);
        modBehavioursPost.put(modid, behaviourPost);
    }

    /**
     * Called server-side when a non-PneumaticCraft block is about to be wrenched by the Pneumatic Wrench. Possibly run
     * any mod-specific behaviour on it.
     * @param ctx the item usage context
     * @param state the block being wrenched
     * @return the action result; if SUCCESS or CONSUME, then Pneumatic Wrenching behaviour will not be carried out; if
     *         CONSUME then the Pneumatic Wrench sound effect will also not be played
     */
    public ActionResultType onWrenchedPre(ItemUseContext ctx, BlockState state) {
        return modBehavioursPre.getOrDefault(state.getBlock().getRegistryName().getNamespace(), NO_OP_PRE).apply(ctx, state);
    }

    /**
     * Called server-side when a non-PneumaticCraft block has just been wrenched by the Pneumatic Wrench. Will not be
     * called if {@link #onWrenchedPre(ItemUseContext, BlockState)} returned SUCCESS or CONSUME.
     * @param ctx the item usage context
     * @param state the block being wrenched
     */
    public void onWrenchedPost(ItemUseContext ctx, BlockState state) {
        modBehavioursPost.getOrDefault(state.getBlock().getRegistryName().getNamespace(), NO_OP_POST).accept(ctx, state);
    }
}
