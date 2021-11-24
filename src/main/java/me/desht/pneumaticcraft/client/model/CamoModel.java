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

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * With credit to Vazkii for showing me how this can be made to work with connected textures (trick being
 * to pass IBlockAccess/BlockPos via extended state).
 *
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/model/PlatformModel.java
 */
public class CamoModel implements IDynamicBakedModel {

    private final IBakedModel originalModel;

    public CamoModel(IBakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        if (state == null || !(state.getBlock() instanceof BlockPneumaticCraftCamo)) {
            return originalModel.getQuads(state, side, rand, modelData);
        }
        BlockState camoState = modelData.getData(BlockPneumaticCraftCamo.CAMO_STATE);

        RenderType layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = RenderType.solid(); // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == RenderType.solid()) {
            // No camo
            return originalModel.getQuads(state, side, rand, modelData);
        } else if (camoState != null && RenderTypeLookup.canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            IBakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(camoState);
            return model.getQuads(camoState, side, rand, modelData);
        } else {
            // Not rendering in this layer
            return Collections.emptyList();
        }
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
    public ItemCameraTransforms getTransforms() {
        return originalModel.getTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return originalModel.getOverrides();
    }

    private static class FakeBlockAccess implements IBlockReader {
        private final IBlockReader compose;

        private FakeBlockAccess(IBlockReader compose) {
            this.compose = compose;
        }

        @Nullable
        @Override
        public TileEntity getBlockEntity(BlockPos pos) {
            return compose.getBlockEntity(pos);
        }

        @Nonnull
        @Override
        public BlockState getBlockState(@Nonnull BlockPos pos) {
            BlockState state = compose.getBlockState(pos);
            if (state.getBlock() instanceof BlockPneumaticCraftCamo) {
                TileEntity te = compose.getBlockEntity(pos);
                if (te instanceof ICamouflageableTE) {
                    state = ((ICamouflageableTE) te).getCamouflage();
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

    }
}
