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

package me.desht.pneumaticcraft.client.model;

import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * With credit to Vazkii for showing me how this can be made to work with connected textures (trick being
 * to pass IBlockAccess/BlockPos via extended state).
 *
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/model/PlatformModel.java
 */
public class CamoModel implements IDynamicBakedModel {

    private final BakedModel originalModel;

    public CamoModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
        if (state == null || !(state.getBlock() instanceof AbstractCamouflageBlock)) {
            return originalModel.getQuads(state, side, rand, modelData, renderType);
        }
        BlockState camoState = modelData.get(AbstractCamouflageBlock.CAMO_STATE);

        if (renderType == null) {
            renderType = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && renderType == RenderType.solid()) {
            // No camo
            return originalModel.getQuads(state, side, rand, modelData, renderType);
        } else if (camoState != null && getRenderTypes(camoState, rand, modelData).contains(renderType)) {
            // Steal camo's model
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(camoState);
            return model.getQuads(camoState, side, rand, modelData, renderType);
        } else {
            // Not rendering in this layer
            return Collections.emptyList();
        }
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        BlockState camoState = data.get(AbstractCamouflageBlock.CAMO_STATE);
        return IDynamicBakedModel.super.getRenderTypes(camoState == null ? state : camoState, rand, data);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return originalModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return originalModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return originalModel.getOverrides();
    }

    private static class FakeBlockAccess implements BlockGetter {
        private final BlockGetter compose;

        private FakeBlockAccess(BlockGetter compose) {
            this.compose = compose;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return compose.getBlockEntity(pos);
        }

        @Nonnull
        @Override
        public BlockState getBlockState(@Nonnull BlockPos pos) {
            BlockState state = compose.getBlockState(pos);
            if (state.getBlock() instanceof AbstractCamouflageBlock) {
                BlockEntity te = compose.getBlockEntity(pos);
                if (te instanceof CamouflageableBlockEntity) {
                    state = ((CamouflageableBlockEntity) te).getCamouflage();
                }
            }
            return state == null ? Blocks.AIR.defaultBlockState() : state;
        }

        @Nonnull
        @Override
        public FluidState getFluidState(@Nonnull BlockPos blockPos) {
            // todo test for 1.13
            return compose.getFluidState(blockPos);
        }

        @Override
        public int getHeight() {
            return compose.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return compose.getMinBuildHeight();
        }
    }
}
