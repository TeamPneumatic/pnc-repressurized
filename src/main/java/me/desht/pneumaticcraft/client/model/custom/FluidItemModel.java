package me.desht.pneumaticcraft.client.model.custom;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.client.render.fluid.FluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class FluidItemModel implements IDynamicBakedModel {
    private final IBakedModel bakedBaseModel;
    private final ItemOverrideList overrideList = new FluidOverridesList(this);
    private final VertexFormat format;
    private List<TankRenderInfo> tanksToRender;

    private FluidItemModel(VertexFormat format, IBakedModel bakedBaseModel) {
        this.format = format;
        this.bakedBaseModel = bakedBaseModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> res = new ArrayList<>(bakedBaseModel.getQuads(state, side, rand, extraData));

        if (tanksToRender != null) {
            for (TankRenderInfo info : tanksToRender) {
                IFluidTank tank = info.getTank();
                if (tank.getFluid().isEmpty()) continue;
                Fluid f = tank.getFluid().getFluid();
                TextureAtlasSprite still = Minecraft.getInstance().getTextureMap().getAtlasSprite(f.getAttributes().getStill(tank.getFluid()).toString());
                int color = f.getAttributes().getColor(tank.getFluid());
                float[] cols = new float[]{(color >> 24 & 0xFF) / 255F, (color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F};
                AxisAlignedBB bounds = getRenderBounds(tank, info.getBounds());
                float bx1 = (float) (bounds.minX * 16);
                float bx2 = (float) (bounds.maxX * 16);
                float by1 = (float) (bounds.minY * 16);
                float by2 = (float) (bounds.maxY * 16);
                float bz1 = (float) (bounds.minZ * 16);
                float bz2 = (float) (bounds.maxZ * 16);

                if (info.shouldRender(Direction.DOWN)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.maxX, bounds.minY, bounds.minZ), new Vec3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3d(bounds.minX, bounds.minY, bounds.maxZ), new Vec3d(bounds.minX, bounds.minY, bounds.minZ));
                    res.add(createQuad(vecs, cols, still, Direction.DOWN, bx1, bx2, bz1, bz2));
                }
                if (info.shouldRender(Direction.UP)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.minX, bounds.maxY, bounds.minZ), new Vec3d(bounds.minX, bounds.maxY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.maxY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.maxY, bounds.minZ));
                    res.add(createQuad(vecs, cols, still, Direction.UP, bx1, bx2, bz1, bz2));
                }
                if (info.shouldRender(Direction.NORTH)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.maxX, bounds.maxY, bounds.minZ), new Vec3d(bounds.maxX, bounds.minY, bounds.minZ), new Vec3d(bounds.minX, bounds.minY, bounds.minZ), new Vec3d(bounds.minX, bounds.maxY, bounds.minZ));
                    res.add(createQuad(vecs, cols, still, Direction.NORTH, bx1, bx2, by1, by2));
                }
                if (info.shouldRender(Direction.SOUTH)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.minX, bounds.maxY, bounds.maxZ), new Vec3d(bounds.minX, bounds.minY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.maxY, bounds.maxZ));
                    res.add(createQuad(vecs, cols, still, Direction.SOUTH, bx1, bx2, by1, by2));
                }
                if (info.shouldRender(Direction.WEST)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.minX, bounds.maxY, bounds.minZ), new Vec3d(bounds.minX, bounds.minY, bounds.minZ), new Vec3d(bounds.minX, bounds.minY, bounds.maxZ), new Vec3d(bounds.minX, bounds.maxY, bounds.maxZ));
                    res.add(createQuad(vecs, cols, still, Direction.WEST, bz1, bz2, by1, by2));
                }
                if (info.shouldRender(Direction.EAST)) {
                    List<Vec3d> vecs = ImmutableList.of(new Vec3d(bounds.maxX, bounds.maxY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3d(bounds.maxX, bounds.minY, bounds.minZ), new Vec3d(bounds.maxX, bounds.maxY, bounds.minZ));
                    res.add(createQuad(vecs, cols, still, Direction.EAST, bz1, bz2, by1, by2));
                }
            }
        }
        return res;
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getAttributes().getDensity() < 0) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AxisAlignedBB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    private BakedQuad createQuad(List<Vec3d> vecs, float[] cols, TextureAtlasSprite sprite, Direction face, float u1, float u2, float v1, float v2) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        Vec3d normal = new Vec3d(face.getDirectionVec());
        putVertex(builder, cols, normal, vecs.get(0).x, vecs.get(0).y, vecs.get(0).z, sprite, u1, v1);
        putVertex(builder, cols, normal, vecs.get(1).x, vecs.get(1).y, vecs.get(1).z, sprite, u1, v2);
        putVertex(builder, cols, normal, vecs.get(2).x, vecs.get(2).y, vecs.get(2).z, sprite, u2, v2);
        putVertex(builder, cols, normal, vecs.get(3).x, vecs.get(3).y, vecs.get(3).z, sprite, u2, v1);
        builder.setQuadOrientation(face);
        return builder.build();
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, float[] cols, Vec3d normal, double x, double y, double z, TextureAtlasSprite sprite, float u, float v) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, cols[1], cols[2], cols[3], cols[0]);
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

    @Override
    public boolean isAmbientOcclusion() {
        return bakedBaseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return bakedBaseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return bakedBaseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return bakedBaseModel.getParticleTexture(data);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return bakedBaseModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrideList;
    }

    public static class Geometry implements IModelGeometry<Geometry> {
        private final BlockModel baseModel;

        Geometry(BlockModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format, ItemOverrideList overrides) {
            return new FluidItemModel(format, baseModel.bake(bakery, spriteGetter, sprite, format));
        }

        @Override
        public Collection<ResourceLocation> getTextureDependencies(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
            return baseModel.getTextures(modelGetter, missingTextureErrors);
        }
    }

    public enum Loader implements IModelLoader<Geometry> {
        INSTANCE;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
        }

        @Override
        public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            BlockModel baseModel = deserializationContext.deserialize(JSONUtils.getJsonObject(modelContents, "base_model"), BlockModel.class);
            return new FluidItemModel.Geometry(baseModel);
        }
    }

    private class FluidOverridesList extends ItemOverrideList {
        private final FluidItemModel modelIn;

        FluidOverridesList(FluidItemModel modelIn) {
            this.modelIn = modelIn;
        }

        @Nullable
        @Override
        public IBakedModel getModelWithOverrides(IBakedModel original, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
            if (stack.getItem() instanceof IFluidRendered) {
                FluidItemRenderInfoProvider infoProvider = ((IFluidRendered) stack.getItem()).getFluidItemRenderer();
                modelIn.tanksToRender = infoProvider.getTanksToRender(stack);
            }
            return modelIn;
        }
    }

}
