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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModHarvestHandlers {
    public static final DeferredRegister<HarvestHandler> HARVEST_HANDLERS = new DeferredRegister<>(ModRegistries.HARVEST_HANDLERS, Names.MOD_ID);

    static {
        HARVEST_HANDLERS.register("crops", HarvestHandlerCrops::new);
        HARVEST_HANDLERS.register("nether_wart",
                () -> new HarvestHandlerCropLike(state -> state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART));
        HARVEST_HANDLERS.register("cocoa_beans",
                () -> new HarvestHandlerCropLike(state -> state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS));
        HARVEST_HANDLERS.register("cactus_like",
                () -> new HarvestHandlerCactusLike(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE));
        HARVEST_HANDLERS.register("pumpkin_like",
                () -> new HarvestHandler.SimpleHarvestHandler(Blocks.PUMPKIN, Blocks.MELON));
        HARVEST_HANDLERS.register("leaves", HarvestHandlerLeaves::new);
        HARVEST_HANDLERS.register("trees", HarvestHandlerTree::new);
    }

    public static Block convertTree(Block in, String from, String to) {
        ResourceLocation rl = new ResourceLocation(in.getRegistryName().toString().replace("_" + from, "_" + to));
        return ForgeRegistries.BLOCKS.getValue(rl);
    }
}
