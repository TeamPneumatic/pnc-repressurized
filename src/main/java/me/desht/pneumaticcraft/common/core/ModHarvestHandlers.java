package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModHarvestHandlers {
    @SubscribeEvent
    public static void register(RegistryEvent.Register<HarvestHandler> event) {
        IForgeRegistry<HarvestHandler> r = event.getRegistry();

        register(r, "crops", new HarvestHandlerCrops());
        register(r, "nether_wart", new HarvestHandlerCropLike(state ->
                state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART)
        );
        register(r, "cocoa_beans", new HarvestHandlerCropLike(state ->
                state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS)
        );
        register(r, "cactus_like", new HarvestHandlerCactusLike(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE)
        );
        register(r, "pumpkin_like", new HarvestHandler.SimpleHarvestHandler(Blocks.PUMPKIN, Blocks.MELON));
        register(r, "leaves", new HarvestHandlerLeaves());
        register(r, "trees", new HarvestHandlerTree());
    }

    private static void register(IForgeRegistry<HarvestHandler> r, String name, HarvestHandler handler) {
        r.register(handler.setRegistryName(RL(name)));
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
                    "_" + this.toString().toLowerCase(), "_" + to.toString().toLowerCase())
            );
            return ForgeRegistries.BLOCKS.getValue(rl);
        }
    }
}
