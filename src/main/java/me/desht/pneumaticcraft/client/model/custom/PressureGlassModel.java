package me.desht.pneumaticcraft.client.model.custom;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberGlass;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class PressureGlassModel implements IDynamicBakedModel {
    private static final int TEXTURE_COUNT = 47;
    private static final TextureAtlasSprite[] SPRITE_CACHE = new TextureAtlasSprite[TEXTURE_COUNT];

    private final Function<Material, TextureAtlasSprite> spriteGetter;

    // cached quads, by texture index & face
    private static final BakedQuad[][] QUAD_CACHE = new BakedQuad[6][];
    static {
        for (int i = 0; i < 6; i++) QUAD_CACHE[i] = new BakedQuad[TEXTURE_COUNT];
    }

    // winding order lookup table
    private static final List<List<Vec3d>> VECS = new ArrayList<>();
    static {
        // in DUNSWE order
        VECS.add(ImmutableList.of(new Vec3d(1, 0, 0), new Vec3d(1, 0, 1), new Vec3d(0, 0, 1), new Vec3d(0, 0, 0)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 0), new Vec3d(0, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 0)));
        VECS.add(ImmutableList.of(new Vec3d(1, 1, 0), new Vec3d(1, 0, 0), new Vec3d(0, 0, 0), new Vec3d(0, 1, 0)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 1), new Vec3d(0, 0, 1), new Vec3d(1, 0, 1), new Vec3d(1, 1, 1)));
        VECS.add(ImmutableList.of(new Vec3d(0, 1, 0), new Vec3d(0, 0, 0), new Vec3d(0, 0, 1), new Vec3d(0, 1, 1)));
        VECS.add(ImmutableList.of(new Vec3d(1, 1, 1), new Vec3d(1, 0, 1), new Vec3d(1, 0, 0), new Vec3d(1, 1, 0)));
    }

    private PressureGlassModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        this.spriteGetter = spriteGetter;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (side == null || extraData == EmptyModelData.INSTANCE) {
            return Collections.emptyList();
        }

        int textureIndex = extraData.getData(TileEntityPressureChamberGlass.DIR_PROPS.get(side.getIndex()));
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
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getSprite(0);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    private void putVertex(BakedQuadBuilder builder, Vec3d normal,
                   double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b, float a) {
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for (int e = 0; e < elements.size(); e++) {
            switch (elements.get(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z);
                    break;
                case COLOR:
                    builder.put(e, r, g, b, a);
                    break;
                case UV:
                    if (elements.get(e).getIndex() == 0) {
                        float iu = sprite.getInterpolatedU(u);
                        float iv = sprite.getInterpolatedV(v);
                        builder.put(e, iu, iv);
                    } else {
                        builder.put(e);
                    }
                    break;
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private BakedQuad getCachedQuad(int textureIndex, Direction side) {
        if (QUAD_CACHE[side.getIndex()][textureIndex] == null) {
            List<Vec3d> v = VECS.get(side.getIndex());
            QUAD_CACHE[side.getIndex()][textureIndex] = createQuad(v.get(0), v.get(1), v.get(2), v.get(3), getSprite(textureIndex), 1f, 1f, 1f, 1f);
        }
        return QUAD_CACHE[side.getIndex()][textureIndex];
    }

    private TextureAtlasSprite getSprite(int textureIndex) {
        if (SPRITE_CACHE[textureIndex] == null) {
            SPRITE_CACHE[textureIndex] = spriteGetter.apply(new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (textureIndex + 1))));
        }
        return SPRITE_CACHE[textureIndex];
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite,
                         float r, float g, float b, float a) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, r, g, b, a);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, r, g, b, a);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, r, g, b, a);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, r, g, b, a);
        return builder.build();
    }

    public enum Loader implements IModelLoader<Geometry> {
        INSTANCE;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
        }

        @Override
        public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            return new Geometry();
        }
    }

    private static class Geometry implements IModelGeometry<Geometry> {
        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new PressureGlassModel(spriteGetter);
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> res = new ArrayList<>();
            for (int i = 0; i < PressureGlassModel.TEXTURE_COUNT; i++) {
                res.add(new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(Textures.PRESSURE_GLASS_LOCATION + "window_" + (i + 1))));
            }
            return res;
        }
    }
}
