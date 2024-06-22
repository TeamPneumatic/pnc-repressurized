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

package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatTinted;
import me.desht.pneumaticcraft.common.item.PneumaticCraftBucketItem;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Names.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandlers {
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        for (var item : ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ITintableItem tintable) {
                event.register(tintable::getTintColor, item.get());
            } else if (item.get() instanceof BlockItem bi) {
                Block b = bi.getBlock();
                if (b instanceof ITintableBlock) {
                    event.register((stack, index) -> event.getBlockColors().getColor(b.defaultBlockState(), null, null, index), item.get());
                }
            } else if (item.get() instanceof PneumaticCraftBucketItem) {
                event.register(new DynamicFluidContainerModel.Colors(), item.get());
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        for (var block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof ITintableBlock tintable) {
                event.register(tintable::getTintColor, block.get());
            } else if (block.get() instanceof AbstractCamouflageBlock) {
                event.register((state, level, pos, tintIndex) -> {
                    if (level != null && pos != null) {
                        BlockEntity te = level.getBlockEntity(pos);
                        if (te instanceof CamouflageableBlockEntity camo && camo.getCamouflage() != null) {
                            return event.getBlockColors().getColor(camo.getCamouflage(), te.getLevel(), pos, tintIndex);
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
        return color.getARGB();
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
        int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IHeatTintable extends ITintableBlock {
        @Override
        default int getTintColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
            if (world != null && pos != null) {
                BlockEntity te = world.getBlockEntity(pos);
                TintColor tint = te instanceof IHeatTinted ? ((IHeatTinted) te).getColorForTintIndex(tintIndex) : TintColor.WHITE;
                return tint.getARGB();
            }
            return 0xFFFFFFFF;
        }
    }
}
