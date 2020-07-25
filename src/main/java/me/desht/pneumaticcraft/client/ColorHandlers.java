package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.tileentity.IHeatTinted;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandlers {
    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ITintableItem) {
                event.getItemColors().register(((ITintableItem) item.get())::getTintColor, item.get());
            } else if (item.get() instanceof BlockItem) {
                Block b = ((BlockItem)item.get()).getBlock();
                if (b instanceof ITintableBlock) {
                    event.getItemColors().register((stack, index) -> event.getBlockColors().getColor(b.getDefaultState(), null, null, index), item.get());
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
        for (RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof ITintableBlock) {
                event.getBlockColors().register(((ITintableBlock) block.get())::getTintColor, block.get());
            } else if (block.get() instanceof BlockPneumaticCraftCamo) {
                event.getBlockColors().register((state, blockAccess, pos, tintIndex) -> {
                    if (blockAccess != null && pos != null) {
                        TileEntity te = blockAccess.getTileEntity(pos);
                        if (te instanceof ICamouflageableTE && ((ICamouflageableTE) te).getCamouflage() != null) {
                            return event.getBlockColors().getColor(((ICamouflageableTE) te).getCamouflage(), te.getWorld(), pos, tintIndex);
                        }
                    }
                    return 0xFFFFFFFF;
                }, block.get());
            }
        }
    }

    public static int desaturate(int c) {
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getRGB();
    }

    /**
     * Items implementing this will be automatically registered in the ColorHandler.Item event
     */
    public interface ITintableItem {
        int getTintColor(ItemStack stack, int tintIndex);
    }

    /**
     * Items implementing this will be automatically registered in the ColorHandler.Block event
     */
    public interface ITintableBlock {
        int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IHeatTintable extends ITintableBlock {
        @Override
        default int getTintColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
            if (world != null && pos != null) {
                TileEntity te = world.getTileEntity(pos);
                TintColor tint = te instanceof IHeatTinted ? ((IHeatTinted) te).getColorForTintIndex(tintIndex) : TintColor.WHITE;
                return tint.getRGB();
            }
            return 0xFFFFFFFF;
        }
    }
}
