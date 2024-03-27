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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.harvesting.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModHarvestHandlers {
    public static final ResourceKey<Registry<HarvestHandler>> HARVEST_HANDLERS_KEY
            = ResourceKey.createRegistryKey(RL("harvest_handlers"));
    public static final Registry<HarvestHandler> HARVEST_HANDLER_REGISTRY
            = new RegistryBuilder<>(HARVEST_HANDLERS_KEY).create();

    public static final DeferredRegister<HarvestHandler> HARVEST_HANDLERS_DEFERRED
            = DeferredRegister.create(HARVEST_HANDLER_REGISTRY, Names.MOD_ID);

    public static final Supplier<HarvestHandler> CROPS = register("crops", HarvestHandlerCrops::new);
    public static final Supplier<HarvestHandler> NETHER_WART = register("nether_wart", HarvestHandlerCropLike.NetherWart::new);
    public static final Supplier<HarvestHandler> SWEET_BERRIES = register("sweet_berries", HarvestHandlerCropLike.SweetBerry::new);
    public static final Supplier<HarvestHandler> COCOA = register("cocoa_beans", HarvestHandlerCropLike.Cocoa::new);
    public static final Supplier<HarvestHandler> CACTUS = register("cactus_like", HarvestHandlerCactusLike.VanillaCrops::new);
    public static final Supplier<HarvestHandler> PUMPKIN = register("pumpkin_like",
            () -> new HarvestHandler.SimpleHarvestHandler(Blocks.PUMPKIN, Blocks.MELON));
    public static final Supplier<HarvestHandler> LEAVES = register("leaves", HarvestHandlerLeaves::new);
    public static final Supplier<HarvestHandler> TREES = register("trees", HarvestHandlerTree::new);

    public static <T extends HarvestHandler> Supplier<T> register(String name, final Supplier<T> sup) {
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
            return PneumaticCraftUtils.getRegistryName(in).map(rlIn -> {
                ResourceLocation rlOut = new ResourceLocation(rlIn.getNamespace(), pattern.matcher(rlIn.getPath()).replaceAll(to.suffix));
                return BuiltInRegistries.BLOCK.get(rlOut);
            }).orElse(Blocks.AIR);
        }
    }
}
