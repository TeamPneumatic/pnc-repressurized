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

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.wrench.IWrenchRegistry;
import me.desht.pneumaticcraft.common.item.PneumaticWrenchItem;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public enum ModdedWrenchUtils implements IWrenchRegistry {
    INSTANCE;

    private final Set<ResourceLocation> wrenches = new HashSet<>();

    private static final BiFunction<UseOnContext,BlockState,InteractionResult> NO_OP_PRE = (ctx, state) -> InteractionResult.PASS;
    private static final BiConsumer<UseOnContext,BlockState> NO_OP_POST = (ctx, state) -> {};

    private final Map<String, BiFunction<UseOnContext,BlockState,InteractionResult>> modBehavioursPre = new ConcurrentHashMap<>();
    private final Map<String, BiConsumer<UseOnContext,BlockState>> modBehavioursPost = new ConcurrentHashMap<>();

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
        wrenches.add(ResourceLocation.parse(wrenchId));
    }

    @Override
    public boolean isModdedWrench(@Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof PneumaticWrenchItem) &&
                (stack.is(PneumaticCraftTags.Items.WRENCHES) || wrenches.contains(PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow()));
    }

    @Override
    public boolean isWrench(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof PneumaticWrenchItem || isModdedWrench(stack);
    }

    @Override
    public void registerWrench(Item wrench) {
        wrenches.add(PneumaticCraftUtils.getRegistryName(wrench).orElseThrow());
    }

    @Override
    public void addModdedWrenchBehaviour(String modid, BiFunction<UseOnContext,BlockState,InteractionResult> behaviourPre, BiConsumer<UseOnContext,BlockState> behaviourPost) {
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
    public InteractionResult onWrenchedPre(UseOnContext ctx, BlockState state) {
        return modBehavioursPre.getOrDefault(getModId(state), NO_OP_PRE).apply(ctx, state);
    }

    /**
     * Called server-side when a non-PneumaticCraft block has just been wrenched by the Pneumatic Wrench. Will not be
     * called if {@link #onWrenchedPre(UseOnContext, BlockState)} returned SUCCESS or CONSUME.
     * @param ctx the item usage context
     * @param state the block being wrenched
     */
    public void onWrenchedPost(UseOnContext ctx, BlockState state) {
        modBehavioursPost.getOrDefault(getModId(state), NO_OP_POST).accept(ctx, state);
    }

    private static String getModId(BlockState state) {
        return PneumaticCraftUtils.getRegistryName(state.getBlock()).orElseThrow().getNamespace();
    }
}
