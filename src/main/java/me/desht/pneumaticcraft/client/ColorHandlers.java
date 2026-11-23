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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatTinted;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class ColorHandlers {
    // highest "tintindex" used by our block models is 2
    private static final int BLOCK_TINT_LAYERS = 3;

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(RL("tintable"), TintableItemTint.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        for (var block : ModBlocks.BLOCKS.getEntries()) {
            if (block.get() instanceof ITintableBlock tintable) {
                event.register(makeLayers(i -> new TintableBlockTint(tintable, i)), block.get());
            } else if (block.get() instanceof AbstractCamouflageBlock) {
                event.register(makeLayers(CamouflageBlockTint::new), block.get());
            }
        }
    }

    private static List<BlockTintSource> makeLayers(IntFunction<BlockTintSource> factory) {
        List<BlockTintSource> layers = new ArrayList<>(BLOCK_TINT_LAYERS);
        for (int i = 0; i < BLOCK_TINT_LAYERS; i++) {
            layers.add(factory.apply(i));
        }
        return layers;
    }

    private record TintableBlockTint(ITintableBlock tintable, int tintIndex) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return tintable.getTintColor(state, null, null, tintIndex);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return tintable.getTintColor(state, level, pos, tintIndex);
        }
    }

    private record CamouflageBlockTint(int tintIndex) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return 0xFFFFFFFF;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof CamouflageableBlockEntity camo && camo.getCamouflage() != null) {
                BlockState camoState = camo.getCamouflage();
                BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(camoState, tintIndex);
                if (source != null) {
                    return source.colorInWorld(camoState, level, pos);
                }
            }
            return 0xFFFFFFFF;
        }
    }

    /**
     * Item tint source for items implementing {@link ITintableItem}, and for block items whose block implements
     * {@link ITintableBlock}. Item model JSON refers to it as {@code pneumaticcraft:tintable} with a
     * {@code tint_index} field.
     */
    public record TintableItemTint(int tintIndex) implements ItemTintSource {
        public static final MapCodec<TintableItemTint> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("tint_index").forGetter(TintableItemTint::tintIndex)
        ).apply(builder, TintableItemTint::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof ITintableItem tintable) {
                return tintable.getTintColor(stack, tintIndex);
            } else if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ITintableBlock tintable) {
                return tintable.getTintColor(bi.getBlock().defaultBlockState(), null, null, tintIndex);
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<TintableItemTint> type() {
            return MAP_CODEC;
        }
    }

    public static int desaturate(int c) {
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getARGB();
    }

    /**
     * Items implementing this can use the {@link TintableItemTint} tint source in their item model
     */
    public interface ITintableItem {
        int getTintColor(ItemStack stack, int tintIndex);
    }

    /**
     * Blocks implementing this will be automatically registered in the BlockTintSources event
     */
    // the level passed at runtime is the client BlockAndTintGetter, typed here as the common BlockAndLightGetter
    //  so that common block classes can implement this
    public interface ITintableBlock {
        int getTintColor(BlockState state, @Nullable BlockAndLightGetter world, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IHeatTintable extends ITintableBlock {
        @Override
        default int getTintColor(BlockState state, @Nullable BlockAndLightGetter world, @Nullable BlockPos pos, int tintIndex) {
            if (world != null && pos != null) {
                BlockEntity te = world.getBlockEntity(pos);
                TintColor tint = te instanceof IHeatTinted ? ((IHeatTinted) te).getColorForTintIndex(tintIndex) : TintColor.WHITE;
                return tint.getARGB();
            }
            return 0xFFFFFFFF;
        }
    }
}
