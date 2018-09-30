package me.desht.pneumaticcraft.client;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * With credit to Vazkii for showing me how this can be made to work with connected textures (trick being
 * to pass IBlockAccess/BlockPos via extended state).
 *
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/model/PlatformModel.java
 */
public class CamoModel implements IBakedModel {

    private final IBakedModel originalModel;

    public CamoModel(IBakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null || !(state.getBlock() instanceof BlockPneumaticCraftCamo)) {
            return originalModel.getQuads(state, side, rand);
        }

        IExtendedBlockState ext = (IExtendedBlockState) state;
        IBlockState camoState = ext.getValue(BlockPneumaticCraftCamo.CAMO_STATE);
        IBlockAccess blockAccess = ext.getValue(BlockPneumaticCraftCamo.BLOCK_ACCESS);
        BlockPos pos = ext.getValue(BlockPneumaticCraftCamo.BLOCK_POS);
        if (blockAccess == null || pos == null) {
            return originalModel.getQuads(state, side, rand);
        }

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == null) {
            layer = BlockRenderLayer.SOLID; // workaround for when this isn't set (digging, etc.)
        }
        if (camoState == null && layer == BlockRenderLayer.SOLID) {
            // No camo
            return originalModel.getQuads(state, side, rand);
        } else if (camoState != null && camoState.getBlock().canRenderInLayer(camoState, layer)) {
            IBlockState actual = camoState.getBlock().getActualState(camoState, new FakeBlockAccess(blockAccess), pos);

            // Steal camo's model
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(actual);

            // Their model can be smart too
            IBlockState extended = camoState.getBlock().getExtendedState(actual, new FakeBlockAccess(blockAccess), pos);
            return model.getQuads(extended, side, rand);
        }

        return ImmutableList.of(); // Nothing renders
    }


    @Override
    public boolean isAmbientOcclusion() {
        return originalModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
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

    private static class FakeBlockAccess implements IBlockAccess {

        private final IBlockAccess compose;

        private FakeBlockAccess(IBlockAccess compose) {
            this.compose = compose;
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return compose.getTileEntity(pos);
        }

        @Override
        public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
            return 15 << 20 | 15 << 4;
        }

        @Nonnull
        @Override
        public IBlockState getBlockState(@Nonnull BlockPos pos) {
            IBlockState state = compose.getBlockState(pos);
            if (state.getBlock() instanceof BlockPneumaticCraftCamo) {
                TileEntity te = compose.getTileEntity(pos);
                if (te instanceof ICamouflageableTE) {
                    state = ((ICamouflageableTE) te).getCamouflage();
                }
            }
            return state == null ? Blocks.AIR.getDefaultState() : state;
        }

        @Override
        public boolean isAirBlock(@Nonnull BlockPos pos) {
            return compose.isAirBlock(pos);
        }

        @Nonnull
        @Override
        public Biome getBiome(@Nonnull BlockPos pos) {
            return compose.getBiome(pos);
        }

        @Override
        public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
            return compose.getStrongPower(pos, direction);
        }

        @Override
        public WorldType getWorldType() {
            return compose.getWorldType();
        }

        @Override
        public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
            return compose.isSideSolid(pos, side, _default);
        }
    }
}
