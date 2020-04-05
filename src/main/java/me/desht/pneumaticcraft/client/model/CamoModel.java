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
import net.minecraft.fluid.IFluidState;
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
            layer = RenderType.getSolid(); // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == RenderType.getSolid()) {
            // No camo
            return originalModel.getQuads(state, side, rand, modelData);
        } else if (camoState != null && RenderTypeLookup.canRenderInLayer(camoState, layer)) {
            // Steal camo's model
            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState);
            return model.getQuads(camoState, side, rand, modelData);
        } else {
            // Not rendering in this layer
            return Collections.emptyList();
        }
    }

//    @Override
//    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
//        if (state == null || !(state.getBlock() instanceof BlockPneumaticCraftCamo)) {
//            return originalModel.getQuads(state, side, rand, data);
//        }
//
//        BlockState camoState = data.getData(BlockPneumaticCraftCamo.CAMO_STATE);
//        IEnviromentBlockReader blockAccess = data.getData(BlockPneumaticCraftCamo.BLOCK_ACCESS);
//        BlockPos pos = data.getData(BlockPneumaticCraftCamo.BLOCK_POS);
//        if (blockAccess == null || pos == null) {
//            return originalModel.getQuads(state, side, rand, data);
//        }
//
//        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//        if (layer == null) {
//            layer = BlockRenderLayer.SOLID; // workaround for when this isn't set (digging, etc.)
//        }
//        if (camoState == null && layer == BlockRenderLayer.SOLID) {
//            // No camo
//            return originalModel.getQuads(state, side, rand, data);
//        } else if (camoState != null && camoState.getBlock().canRenderInLayer(camoState, layer)) {
//            // Steal camo's model
//            IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(camoState);
//
//            // Their model can be smart too
//            return model.getQuads(camoState, side, rand, data);
//        }
//
//        return ImmutableList.of(); // Nothing renders
//    }


    @Override
    public boolean isAmbientOcclusion() {
        return originalModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return originalModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return originalModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return originalModel.getItemCameraTransforms();
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
        public TileEntity getTileEntity(BlockPos pos) {
            return compose.getTileEntity(pos);
        }

        @Nonnull
        @Override
        public BlockState getBlockState(@Nonnull BlockPos pos) {
            BlockState state = compose.getBlockState(pos);
            if (state.getBlock() instanceof BlockPneumaticCraftCamo) {
                TileEntity te = compose.getTileEntity(pos);
                if (te instanceof ICamouflageableTE) {
                    state = ((ICamouflageableTE) te).getCamouflage();
                }
            }
            return state == null ? Blocks.AIR.getDefaultState() : state;
        }

        @Nonnull
        @Override
        public IFluidState getFluidState(@Nonnull BlockPos blockPos) {
            // todo test for 1.13
            return compose.getFluidState(blockPos);
        }

    }
}
