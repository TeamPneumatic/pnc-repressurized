package me.desht.pneumaticcraft.client.model.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.client.render.fluid.IFluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class FluidItemModel implements IDynamicBakedModel {
    private final IBakedModel bakedBaseModel;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transformMap;
    private final ItemOverrideList overrideList = new FluidOverridesList(this);
    private List<TankRenderInfo> tanksToRender = Collections.emptyList();

    private FluidItemModel(IBakedModel bakedBaseModel, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transformMap) {
        this.bakedBaseModel = bakedBaseModel;
        this.transformMap = transformMap;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> res = new ArrayList<>(bakedBaseModel.getQuads(state, side, rand, extraData));

        for (TankRenderInfo info : tanksToRender) {
            IFluidTank tank = info.getTank();
            if (tank.getFluid().isEmpty()) continue;
            Fluid fluid = tank.getFluid().getFluid();
            ResourceLocation texture = fluid.getAttributes().getStillTexture(tank.getFluid());
            TextureAtlasSprite still = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
            int color = fluid.getAttributes().getColor(tank.getFluid());
            float[] cols = new float[]{(color >> 24 & 0xFF) / 255F, (color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F};
            AxisAlignedBB bounds = getRenderBounds(tank, info.getBounds());
            float bx1 = (float) (bounds.minX * 16);
            float bx2 = (float) (bounds.maxX * 16);
            float by1 = (float) (bounds.minY * 16);
            float by2 = (float) (bounds.maxY * 16);
            float bz1 = (float) (bounds.minZ * 16);
            float bz2 = (float) (bounds.maxZ * 16);

            if (info.shouldRender(Direction.DOWN)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.maxX, bounds.minY, bounds.minZ), new Vector3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vector3d(bounds.minX, bounds.minY, bounds.maxZ), new Vector3d(bounds.minX, bounds.minY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.DOWN, bx1, bx2, bz1, bz2));
            }
            if (info.shouldRender(Direction.UP)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.minX, bounds.maxY, bounds.minZ), new Vector3d(bounds.minX, bounds.maxY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.maxY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.UP, bx1, bx2, bz1, bz2));
            }
            if (info.shouldRender(Direction.NORTH)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.maxX, bounds.maxY, bounds.minZ), new Vector3d(bounds.maxX, bounds.minY, bounds.minZ), new Vector3d(bounds.minX, bounds.minY, bounds.minZ), new Vector3d(bounds.minX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.NORTH, bx1, bx2, by1, by2));
            }
            if (info.shouldRender(Direction.SOUTH)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.minX, bounds.maxY, bounds.maxZ), new Vector3d(bounds.minX, bounds.minY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.maxY, bounds.maxZ));
                res.add(createQuad(vecs, cols, still, Direction.SOUTH, bx1, bx2, by1, by2));
            }
            if (info.shouldRender(Direction.WEST)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.minX, bounds.maxY, bounds.minZ), new Vector3d(bounds.minX, bounds.minY, bounds.minZ), new Vector3d(bounds.minX, bounds.minY, bounds.maxZ), new Vector3d(bounds.minX, bounds.maxY, bounds.maxZ));
                res.add(createQuad(vecs, cols, still, Direction.WEST, bz1, bz2, by1, by2));
            }
            if (info.shouldRender(Direction.EAST)) {
                List<Vector3d> vecs = ImmutableList.of(new Vector3d(bounds.maxX, bounds.maxY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.minY, bounds.maxZ), new Vector3d(bounds.maxX, bounds.minY, bounds.minZ), new Vector3d(bounds.maxX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.EAST, bz1, bz2, by1, by2));
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

    private BakedQuad createQuad(List<Vector3d> vecs, float[] cols, TextureAtlasSprite sprite, Direction face, float u1, float u2, float v1, float v2) {
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        Vector3d normal = Vector3d.copy(face.getDirectionVec());
        putVertex(builder, normal, vecs.get(0).x, vecs.get(0).y, vecs.get(0).z, u1, v1, sprite, cols);
        putVertex(builder, normal, vecs.get(1).x, vecs.get(1).y, vecs.get(1).z, u1, v2, sprite, cols);
        putVertex(builder, normal, vecs.get(2).x, vecs.get(2).y, vecs.get(2).z, u2, v2, sprite, cols);
        putVertex(builder, normal, vecs.get(3).x, vecs.get(3).y, vecs.get(3).z, u2, v1, sprite, cols);
        builder.setQuadOrientation(face);
        return builder.build();
    }

    private void putVertex(BakedQuadBuilder builder, Vector3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float[] col) {
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for (int e = 0; e < elements.size(); e++) {
            switch (elements.get(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z);
                    break;
                case COLOR:
                    builder.put(e, col[1], col[2], col[3], col[0]);
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

    @Override
    public boolean isAmbientOcclusion() {
        return bakedBaseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return bakedBaseModel.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return false;
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

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        return PerspectiveMapWrapper.handlePerspective(this, transformMap, cameraTransformType, mat);
    }

    public static class Geometry implements IModelGeometry<Geometry> {
        private final BlockModel baseModel;

        Geometry(BlockModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new FluidItemModel(baseModel.bakeModel(bakery, baseModel.parent, spriteGetter, modelTransform, modelLocation, true), PerspectiveMapWrapper.getTransforms(baseModel.getAllTransforms()));
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
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

    private static class FluidOverridesList extends ItemOverrideList {
        private final FluidItemModel modelIn;

        FluidOverridesList(FluidItemModel modelIn) {
            this.modelIn = modelIn;
        }

        @Nullable
        @Override
        public IBakedModel getOverrideModel(IBakedModel original, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
            if (stack.getItem() instanceof IFluidRendered) {
                IFluidItemRenderInfoProvider infoProvider = ((IFluidRendered) stack.getItem()).getFluidItemRenderer();
                modelIn.tanksToRender = infoProvider.getTanksToRender(stack);
            }
            return modelIn;
        }
    }
}
