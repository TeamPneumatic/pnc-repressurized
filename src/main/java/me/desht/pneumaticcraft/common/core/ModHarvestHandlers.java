package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Locale;
import java.util.function.Supplier;

public class ModHarvestHandlers {
    public static final DeferredRegister<HarvestHandler> HARVEST_HANDLERS_DEFERRED = DeferredRegister.create(HarvestHandler.class, Names.MOD_ID);
    public static final Supplier<IForgeRegistry<HarvestHandler>> HARVEST_HANDLERS = HARVEST_HANDLERS_DEFERRED
            .makeRegistry("harvest_handlers", () -> new RegistryBuilder<HarvestHandler>().disableSaving().disableSync());

    public static final RegistryObject<HarvestHandler> CROPS = register("crops", HarvestHandlerCrops::new);
    public static final RegistryObject<HarvestHandler> NETHER_WART = register("nether_wart", () -> new HarvestHandlerCropLike(state ->
            state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART));
    public static final RegistryObject<HarvestHandler> COCOA = register("cocoa_beans", () -> new HarvestHandlerCropLike(state ->
                state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS));
    public static final RegistryObject<HarvestHandler> CACTUS = register("cactus_like", () -> new HarvestHandlerCactusLike(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE));
    public static final RegistryObject<HarvestHandler> PUMPKIN = register("pumpkin_like", () -> new HarvestHandler.SimpleHarvestHandler(Blocks.PUMPKIN, Blocks.MELON));
    public static final RegistryObject<HarvestHandler> LEAVES = register("leaves", HarvestHandlerLeaves::new);
    public static final RegistryObject<HarvestHandler> TREES = register("trees", HarvestHandlerTree::new);

    private static <T extends HarvestHandler> RegistryObject<T> register(String name, final Supplier<T> sup) {
        return HARVEST_HANDLERS_DEFERRED.register(name, sup);
    }

    public enum TreePart {
        LOG, LEAVES, SAPLING;

        /**
         * Given a block which is part of tree (log, leaves, or sapling), try to get a different block from the same tree.
         * This is dependent on the blocks being consistently named (vanilla does this properly); "XXX_log", "XXX_leaves",
         * "XXX_sapling".
         *
         * @param in the block to convert
         * @param to the new part to convert to
         * @return a block for the new part
         */
        public Block convert(Block in, TreePart to) {
            ResourceLocation rl = new ResourceLocation(in.getRegistryName().toString().replace(
                    "_" + this.toString().toLowerCase(Locale.ROOT), "_" + to.toString().toLowerCase(Locale.ROOT))
            );
            return ForgeRegistries.BLOCKS.getValue(rl);
        }
    }
}
