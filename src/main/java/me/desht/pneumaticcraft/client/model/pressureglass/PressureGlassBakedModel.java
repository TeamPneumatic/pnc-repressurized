package me.desht.pneumaticcraft.client.model.pressureglass;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PressureGlassBakedModel implements IDynamicBakedModel {
    public static final int TEXTURE_COUNT = 47;
    public static final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[TEXTURE_COUNT];
    private final VertexFormat format;

    // cached quads, by texture index & face
    private static final BakedQuad[][] QUAD_CACHE = new BakedQuad[6][];
    static {
        for (int i = 0; i < 6; i++) QUAD_CACHE[i] = new BakedQuad[TEXTURE_COUNT];
    }

    // winding order lookup table
    private static final List<List<Vec3d>> VECS = new ArrayList<>();
    static {
        // in DUNSWE order
        VECS.add(ImmutableList.of(new Vec3d(1, 0, 0), new Vec3d(1, 0, 1),
                new Vec3d(0, 0, 1), new Vec3d(0, 0, 0)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 0), new Vec3d(0, 1, 1),
                new Vec3d(1, 1, 1), new Vec3d(1, 1, 0)));
        VECS.add(ImmutableList.of(new Vec3d(1, 1, 0), new Vec3d(1, 0, 0),
                new Vec3d(0, 0, 0), new Vec3d(0, 1, 0)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 1), new Vec3d(0, 0, 1),
                new Vec3d(1, 0, 1), new Vec3d(1, 1, 1)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 0), new Vec3d(0, 0, 0),
                new Vec3d(0, 0, 1), new Vec3d(0, 1, 1)));
        VECS.add(ImmutableList.of(new Vec3d(1, 1, 1), new Vec3d(1, 0, 1),
                new Vec3d(1, 0, 0), new Vec3d(1, 1, 0)));
    }

    public PressureGlassBakedModel(VertexFormat format) {
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

    private BakedQuad getCachedQuad(int textureIndex, Direction side) {
        if (QUAD_CACHE[side.getIndex()][textureIndex] == null) {
            QUAD_CACHE[side.getIndex()][textureIndex] = createQuad(VECS.get(side.getIndex()), SPRITES[textureIndex], side);
        }
        return QUAD_CACHE[side.getIndex()][textureIndex];
    }

    private BakedQuad createQuad(List<Vec3d> vecs, TextureAtlasSprite sprite, Direction face) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        Vec3d normal = new Vec3d(face.getDirectionVec());
        putVertex(builder, normal, vecs.get(0).x, vecs.get(0).y, vecs.get(0).z, sprite, 0, 0);
        putVertex(builder, normal, vecs.get(1).x, vecs.get(1).y, vecs.get(1).z, sprite, 0, 16);
        putVertex(builder, normal, vecs.get(2).x, vecs.get(2).y, vecs.get(2).z, sprite, 16, 16);
        putVertex(builder, normal, vecs.get(3).x, vecs.get(3).y, vecs.get(3).z, sprite, 16, 0);
        builder.setQuadOrientation(face);
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        if (side == null || data == EmptyModelData.INSTANCE) {
            return Collections.emptyList();
        }

        int textureIndex = data.getData(TileEntityPressureChamberGlass.DIR_PROPS.get(side.getIndex()));
        return Collections.singletonList(getCachedQuad(textureIndex, side));
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
        return ItemOverrideList.EMPTY;
    }
}
