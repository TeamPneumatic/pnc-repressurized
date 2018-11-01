package me.desht.pneumaticcraft.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;

public class BakedMinigunWrapper implements IBakedModel {
    private static final EmptyMinigunModel EMPTY_MODEL = new EmptyMinigunModel();

    private final IBakedModel original;

    BakedMinigunWrapper(IBakedModel original) {
        this.original = original;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return original.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return original.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return original.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        switch (cameraTransformType) {
            case GROUND:
            case HEAD:
            case NONE:
            case GUI:
            case FIXED:
                return original.handlePerspective(cameraTransformType);
        }
        return of(EMPTY_MODEL, null);
    }

    public static class EmptyMinigunModel implements IBakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return Collections.emptyList();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return true;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return null;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }
    }
}
