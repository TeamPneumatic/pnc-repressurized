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

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.harvesting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModHarvestHandlers {
    public static final DeferredRegister<HarvestHandler> HARVEST_HANDLERS_DEFERRED = DeferredRegister.create(RL("harvest_handlers"), Names.MOD_ID);
    public static final Supplier<IForgeRegistry<HarvestHandler>> HARVEST_HANDLERS = HARVEST_HANDLERS_DEFERRED
            .makeRegistry(HarvestHandler.class, () -> new RegistryBuilder<HarvestHandler>().disableSaving().disableSync());

    public static final RegistryObject<HarvestHandler> CROPS = register("crops", HarvestHandlerCrops::new);
    public static final RegistryObject<HarvestHandler> NETHER_WART = register("nether_wart", () -> new HarvestHandlerCropLike(state ->
            state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART));
    public static final RegistryObject<HarvestHandler> SWEET_BERRIES = register("sweet_berries", () -> new HarvestHandlerCropLike(state ->
            state.getBlock() == Blocks.SWEET_BERRY_BUSH, SweetBerryBushBlock.AGE, stack -> stack.getItem() == Items.SWEET_BERRIES) {
        @Override
        protected BlockState withMinAge(BlockState state) {
            return state.setValue(SweetBerryBushBlock.AGE, 1);
        }
    });
    public static final RegistryObject<HarvestHandler> COCOA = register("cocoa_beans", () -> new HarvestHandlerCropLike(state ->
                state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS));
    public static final RegistryObject<HarvestHandler> CACTUS = register("cactus_like", () -> new HarvestHandlerCactusLike(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE || state.getBlock() == Blocks.KELP_PLANT));
    public static final RegistryObject<HarvestHandler> PUMPKIN = register("pumpkin_like", () -> new HarvestHandler.SimpleHarvestHandler(Blocks.PUMPKIN, Blocks.MELON));
    public static final RegistryObject<HarvestHandler> LEAVES = register("leaves", HarvestHandlerLeaves::new);
    public static final RegistryObject<HarvestHandler> TREES = register("trees", HarvestHandlerTree::new);

    private static <T extends HarvestHandler> RegistryObject<T> register(String name, final Supplier<T> sup) {
        return HARVEST_HANDLERS_DEFERRED.register(name, sup);
    }

    public enum TreePart {
        LOG("_log"), LEAVES("_leaves"), SAPLING("_sapling");

        private final String suffix;
        private final Pattern pattern;

        TreePart(String suffix) {
            this.suffix = suffix;
            this.pattern = Pattern.compile(suffix + "$");
        }

        /**
         * Given a block which is part of tree (log, leaves, or sapling), try to get a different part of the same tree.
         * This is dependent on the blocks being consistently named (vanilla does this properly): "XXX_log", "XXX_leaves",
         * "XXX_sapling".
         *
         * @param in the block to convert
         * @param to the new part to convert to
         * @return a block for the new part
         */
        public Block convert(Block in, TreePart to) {
            ResourceLocation rl0 = in.getRegistryName();
            if (rl0 == null) return Blocks.AIR;
            ResourceLocation rl = new ResourceLocation(rl0.getNamespace(), pattern.matcher(rl0.getPath()).replaceAll(to.suffix));
            return ForgeRegistries.BLOCKS.getValue(rl);
        }
    }
}
