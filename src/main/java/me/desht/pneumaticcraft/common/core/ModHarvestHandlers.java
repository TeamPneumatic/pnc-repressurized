package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.harvesting.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModHarvestHandlers {

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<HarvestHandler> event) {
            IForgeRegistry<HarvestHandler> r = event.getRegistry();

            // Crops: harvest when fully grown
            r.register(new HarvestHandlerCrops().setRegistryName(RL("crops")));
            r.register(new HarvestHandlerCropLike(state -> state.getBlock() == Blocks.NETHER_WART, NetherWartBlock.AGE, stack -> stack.getItem() == Items.NETHER_WART).setRegistryName(RL("nether_wart")));
            r.register(new HarvestHandlerCropLike(state -> state.getBlock() == Blocks.COCOA, CocoaBlock.AGE, stack -> stack.getItem() == Items.COCOA_BEANS).setRegistryName(RL("cocoa_beans")));

            // Cactus-like: harvest block when same block is below
            r.register(new HarvestHandlerCactusLike(state -> state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SUGAR_CANE).setRegistryName(RL("cactus_like")));

            // Melons/pumpkins: just harvest the block
            r.register(new HarvestHandler() {
                @Override
                public boolean canHarvest(World world, IBlockReader chunkCache, BlockPos pos, BlockState state, IDrone drone) {
                    return state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.MELON;
                }
            }.setRegistryName(RL("pumpkin_like")));

            // Leaves
            r.register(new HarvestHandlerLeaves().setRegistryName(RL("leaves")));

            // this works for all vanilla trees, and should work for all modded trees too as long as mod authors are consistent about block names
            for (Block logBlock : BlockTags.LOGS.getAllElements()) {
                Predicate<BlockState> isLog = state -> state.getBlock() == logBlock;
                Block sapling = convertTree(logBlock, "log", "sapling");
                if (sapling != null && sapling != Blocks.AIR) {
                    Predicate<ItemStack> isSapling = stack -> stack.getItem() == sapling.asItem();
                    r.register(new HarvestHandlerTree(isLog, isSapling, sapling.getDefaultState()).setRegistryName(RL("trees")));
                }
            }
        }
    }

    public static Block convertTree(Block in, String from, String to) {
        ResourceLocation rl = new ResourceLocation(in.getRegistryName().toString().replace("_" + from, "_" + to));
        return ForgeRegistries.BLOCKS.getValue(rl);
    }
}
