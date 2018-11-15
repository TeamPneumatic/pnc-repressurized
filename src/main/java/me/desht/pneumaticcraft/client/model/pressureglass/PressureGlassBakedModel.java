package me.desht.pneumaticcraft.client.model.pressureglass;

import me.desht.pneumaticcraft.common.block.BlockPressureChamberGlass;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PressureGlassBakedModel implements IBakedModel {
    public static final int TEXTURE_COUNT = 47;
    public static final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[TEXTURE_COUNT];
    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Names.MOD_ID + ":pressure_chamber_glass");
    private final VertexFormat format;

    public PressureGlassBakedModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, TextureAtlasSprite sprite, float u, float v) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, sprite, 0, 0);
        putVertex(builder, normal, v2.x, v2.y, v2.z, sprite, 0, 16);
        putVertex(builder, normal, v3.x, v3.y, v3.z, sprite, 16, 16);
        putVertex(builder, normal, v4.x, v4.y, v4.z, sprite, 16, 0);
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (!(state instanceof IExtendedBlockState) || side == null) {
            return Collections.emptyList();
        }

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        List<BakedQuad> quads = new ArrayList<>();
        switch (side) {
            case DOWN:
                int down = extendedBlockState.getValue(BlockPressureChamberGlass.DOWN);
                quads.add(createQuad(
                        new Vec3d(1, 0, 0), new Vec3d(1, 0, 1),
                        new Vec3d(0, 0, 1), new Vec3d(0, 0, 0), SPRITES[down]));
                break;
            case UP:
                int up = extendedBlockState.getValue(BlockPressureChamberGlass.UP);
                quads.add(createQuad(
                        new Vec3d(0, 1, 0), new Vec3d(0, 1, 1),
                        new Vec3d(1, 1, 1), new Vec3d(1, 1, 0), SPRITES[up]));
                break;
            case NORTH:
                int north = extendedBlockState.getValue(BlockPressureChamberGlass.NORTH);
                quads.add(createQuad(
                        new Vec3d(1, 1, 0), new Vec3d(1, 0, 0),
                        new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), SPRITES[north]));
                break;
            case SOUTH:
                int south = extendedBlockState.getValue(BlockPressureChamberGlass.SOUTH);
                quads.add(createQuad(
                        new Vec3d(0, 1, 1), new Vec3d(0, 0, 1),
                        new Vec3d(1, 0, 1), new Vec3d(1, 1, 1), SPRITES[south]));
                break;
            case WEST:
                int west = extendedBlockState.getValue(BlockPressureChamberGlass.WEST);
                quads.add(createQuad(
                        new Vec3d(0, 1, 0), new Vec3d(0, 0, 0),
                        new Vec3d(0, 0, 1), new Vec3d(0, 1, 1), SPRITES[west]));
                break;
            case EAST:
                int east = extendedBlockState.getValue(BlockPressureChamberGlass.EAST);
                quads.add(createQuad(
                        new Vec3d(1, 1, 1), new Vec3d(1, 0, 1),
                        new Vec3d(1, 0, 0), new Vec3d(1, 1, 0), SPRITES[east]));
                break;
        }
        return quads;
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
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return SPRITES[0];
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
