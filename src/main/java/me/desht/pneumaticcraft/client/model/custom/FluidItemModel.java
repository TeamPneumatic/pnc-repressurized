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

package me.desht.pneumaticcraft.client.model.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private final BakedModel bakedBaseModel;
    private final ImmutableMap<ItemTransforms.TransformType, Transformation> transformMap;
    private final ItemOverrides overrideList = new FluidOverridesList(this);
    private List<TankRenderInfo> tanksToRender = Collections.emptyList();

    private FluidItemModel(BakedModel bakedBaseModel, ImmutableMap<ItemTransforms.TransformType, Transformation> transformMap) {
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
            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
            int color = fluid.getAttributes().getColor(tank.getFluid());
            float[] cols = new float[]{(color >> 24 & 0xFF) / 255F, (color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F};
            AABB bounds = getRenderBounds(tank, info.getBounds());
            float bx1 = (float) (bounds.minX * 16);
            float bx2 = (float) (bounds.maxX * 16);
            float by1 = (float) (bounds.minY * 16);
            float by2 = (float) (bounds.maxY * 16);
            float bz1 = (float) (bounds.minZ * 16);
            float bz2 = (float) (bounds.maxZ * 16);

            if (info.shouldRender(Direction.DOWN)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.maxX, bounds.minY, bounds.minZ), new Vec3(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3(bounds.minX, bounds.minY, bounds.maxZ), new Vec3(bounds.minX, bounds.minY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.DOWN, bx1, bx2, bz1, bz2));
            }
            if (info.shouldRender(Direction.UP)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.minX, bounds.maxY, bounds.minZ), new Vec3(bounds.minX, bounds.maxY, bounds.maxZ), new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ), new Vec3(bounds.maxX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.UP, bx1, bx2, bz1, bz2));
            }
            if (info.shouldRender(Direction.NORTH)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.maxX, bounds.maxY, bounds.minZ), new Vec3(bounds.maxX, bounds.minY, bounds.minZ), new Vec3(bounds.minX, bounds.minY, bounds.minZ), new Vec3(bounds.minX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.NORTH, bx1, bx2, by1, by2));
            }
            if (info.shouldRender(Direction.SOUTH)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.minX, bounds.maxY, bounds.maxZ), new Vec3(bounds.minX, bounds.minY, bounds.maxZ), new Vec3(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ));
                res.add(createQuad(vecs, cols, still, Direction.SOUTH, bx1, bx2, by1, by2));
            }
            if (info.shouldRender(Direction.WEST)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.minX, bounds.maxY, bounds.minZ), new Vec3(bounds.minX, bounds.minY, bounds.minZ), new Vec3(bounds.minX, bounds.minY, bounds.maxZ), new Vec3(bounds.minX, bounds.maxY, bounds.maxZ));
                res.add(createQuad(vecs, cols, still, Direction.WEST, bz1, bz2, by1, by2));
            }
            if (info.shouldRender(Direction.EAST)) {
                List<Vec3> vecs = ImmutableList.of(new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ), new Vec3(bounds.maxX, bounds.minY, bounds.maxZ), new Vec3(bounds.maxX, bounds.minY, bounds.minZ), new Vec3(bounds.maxX, bounds.maxY, bounds.minZ));
                res.add(createQuad(vecs, cols, still, Direction.EAST, bz1, bz2, by1, by2));
            }
        }
        return res;
    }

    private AABB getRenderBounds(IFluidTank tank, AABB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getAttributes().getDensity() < 0) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AABB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    private BakedQuad createQuad(List<Vec3> vecs, float[] cols, TextureAtlasSprite sprite, Direction face, float u1, float u2, float v1, float v2) {
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        putVertex(builder, normal, vecs.get(0).x, vecs.get(0).y, vecs.get(0).z, u1, v1, sprite, cols);
        putVertex(builder, normal, vecs.get(1).x, vecs.get(1).y, vecs.get(1).z, u1, v2, sprite, cols);
        putVertex(builder, normal, vecs.get(2).x, vecs.get(2).y, vecs.get(2).z, u2, v2, sprite, cols);
        putVertex(builder, normal, vecs.get(3).x, vecs.get(3).y, vecs.get(3).z, u2, v1, sprite, cols);
        builder.setQuadOrientation(face);
        return builder.build();
    }

    private void putVertex(BakedQuadBuilder builder, Vec3 normal,
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
                        float iu = sprite.getU(u);
                        float iv = sprite.getV(v);
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
    public boolean useAmbientOcclusion() {
        return bakedBaseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return bakedBaseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return bakedBaseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return bakedBaseModel.getParticleIcon(data);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return bakedBaseModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrideList;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
        return PerspectiveMapWrapper.handlePerspective(this, transformMap, cameraTransformType, mat);
    }

    public static class Geometry implements IModelGeometry<Geometry> {
        private final BlockModel baseModel;

        Geometry(BlockModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new FluidItemModel(baseModel.bake(bakery, baseModel.parent, spriteGetter, modelTransform, modelLocation, true), PerspectiveMapWrapper.getTransforms(baseModel.getTransforms()));
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return baseModel.getMaterials(modelGetter, missingTextureErrors);
        }
    }

    public enum Loader implements IModelLoader<Geometry> {
        INSTANCE;

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
        }

        @Override
        public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            BlockModel baseModel = deserializationContext.deserialize(GsonHelper.getAsJsonObject(modelContents, "base_model"), BlockModel.class);
            return new FluidItemModel.Geometry(baseModel);
        }
    }

    private static class FluidOverridesList extends ItemOverrides {
        private final FluidItemModel modelIn;

        FluidOverridesList(FluidItemModel modelIn) {
            this.modelIn = modelIn;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            if (stack.getItem() instanceof IFluidRendered r) {
                modelIn.tanksToRender = r.getFluidItemRenderer().getTanksToRender(stack);
            }
            return modelIn;
        }
    }
}
