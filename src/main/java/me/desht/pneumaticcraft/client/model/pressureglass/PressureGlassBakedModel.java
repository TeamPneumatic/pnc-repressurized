package me.desht.pneumaticcraft.client.model.pressureglass;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
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
    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Names.MOD_ID + ":pressure_chamber_glass");
    private final VertexFormat format;

    PressureGlassBakedModel(VertexFormat format) {
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

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite, Direction face) {
        Vec3d normal = new Vec3d(face.getDirectionVec());//v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, sprite, 0, 0);
        putVertex(builder, normal, v2.x, v2.y, v2.z, sprite, 0, 16);
        putVertex(builder, normal, v3.x, v3.y, v3.z, sprite, 16, 16);
        putVertex(builder, normal, v4.x, v4.y, v4.z, sprite, 16, 0);
        builder.setQuadOrientation(face);
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
        if (side == null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();
        switch (side) {
            case DOWN:
                int down = data.getData(TileEntityPressureChamberGlass.DOWN);
                quads.add(createQuad(
                        new Vec3d(1, 0, 0), new Vec3d(1, 0, 1),
                        new Vec3d(0, 0, 1), new Vec3d(0, 0, 0), SPRITES[down], side));
                break;
            case UP:
                int up = data.getData(TileEntityPressureChamberGlass.UP);
                quads.add(createQuad(
                        new Vec3d(0, 1, 0), new Vec3d(0, 1, 1),
                        new Vec3d(1, 1, 1), new Vec3d(1, 1, 0), SPRITES[up], side));
                break;
            case NORTH:
                int north = data.getData(TileEntityPressureChamberGlass.NORTH);
                quads.add(createQuad(
                        new Vec3d(1, 1, 0), new Vec3d(1, 0, 0),
                        new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), SPRITES[north], side));
                break;
            case SOUTH:
                int south = data.getData(TileEntityPressureChamberGlass.SOUTH);
                quads.add(createQuad(
                        new Vec3d(0, 1, 1), new Vec3d(0, 0, 1),
                        new Vec3d(1, 0, 1), new Vec3d(1, 1, 1), SPRITES[south], side));
                break;
            case WEST:
                int west = data.getData(TileEntityPressureChamberGlass.WEST);
                quads.add(createQuad(
                        new Vec3d(0, 1, 0), new Vec3d(0, 0, 0),
                        new Vec3d(0, 0, 1), new Vec3d(0, 1, 1), SPRITES[west], side));
                break;
            case EAST:
                int east = data.getData(TileEntityPressureChamberGlass.EAST);
                quads.add(createQuad(
                        new Vec3d(1, 1, 1), new Vec3d(1, 0, 1),
                        new Vec3d(1, 0, 0), new Vec3d(1, 1, 0), SPRITES[east], side));
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
        return ItemOverrideList.EMPTY;
    }
}
