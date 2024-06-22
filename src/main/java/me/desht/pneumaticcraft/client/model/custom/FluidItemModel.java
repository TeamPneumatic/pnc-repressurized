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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class FluidItemModel implements IDynamicBakedModel {
    private final BakedModel bakedBaseModel;
    private final ItemOverrides overrideList = new FluidOverridesList(this);
    private List<TankRenderInfo> tanksToRender = Collections.emptyList();

    private FluidItemModel(BakedModel bakedBaseModel) {
        this.bakedBaseModel = bakedBaseModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, RenderType renderType) {
        List<BakedQuad> res = new ArrayList<>(bakedBaseModel.getQuads(state, side, rand, extraData, renderType));

        for (TankRenderInfo info : tanksToRender) {
            IFluidTank tank = info.getTank();
            if (tank.getFluid().isEmpty()) continue;
            Fluid fluid = tank.getFluid().getFluid();
            IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation texture = renderProps.getStillTexture(tank.getFluid());
            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
            int color = renderProps.getTintColor(tank.getFluid());
            float[] cols = new float[]{(color >> 24 & 0xFF) / 255F, (color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F};
            AABB bounds = getRenderBounds(tank, info.getBounds());
            float bx1 = (float) bounds.minX;
            float bx2 = (float) bounds.maxX;
            float by1 = (float) bounds.minY;
            float by2 = (float) bounds.maxY;
            float bz1 = (float) bounds.minZ;
            float bz2 = (float) bounds.maxZ;

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
        if (tank.getFluid().getFluid().getFluidType().isLighterThanAir()) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AABB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    private BakedQuad createQuad(List<Vec3> vecs, float[] cols, TextureAtlasSprite sprite, Direction face, float u1, float u2, float v1, float v2) {
        QuadBakingVertexConsumer quadBaker = new QuadBakingVertexConsumer();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());

        putVertex(quadBaker, normal, vecs.get(0).x, vecs.get(0).y, vecs.get(0).z, u1, v1, sprite, cols, face);
        putVertex(quadBaker, normal, vecs.get(1).x, vecs.get(1).y, vecs.get(1).z, u1, v2, sprite, cols, face);
        putVertex(quadBaker, normal, vecs.get(2).x, vecs.get(2).y, vecs.get(2).z, u2, v2, sprite, cols, face);
        putVertex(quadBaker, normal, vecs.get(3).x, vecs.get(3).y, vecs.get(3).z, u2, v1, sprite, cols, face);

        return quadBaker.bakeQuad();
    }

    private void putVertex(QuadBakingVertexConsumer quadBaker, Vec3 normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float[] cols, Direction face) {
        quadBaker.addVertex((float) x, (float) y, (float) z);
        quadBaker.setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        quadBaker.setColor(cols[1], cols[2], cols[3], cols[0]);
        quadBaker.setUv(sprite.getU(u), sprite.getV(v));
        quadBaker.setSprite(sprite);
        quadBaker.setDirection(face);
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
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
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
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return IDynamicBakedModel.super.getRenderPasses(itemStack, fabulous);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
        bakedBaseModel.getTransforms().getTransform(displayContext).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    public record Geometry(BlockModel baseModel) implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides) {
            return new FluidItemModel(baseModel.bake(baker, Objects.requireNonNull(baseModel.parent), spriteGetter, modelTransform, true));
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
            baseModel.resolveParents(modelGetter);
        }
    }

    public enum Loader implements IGeometryLoader<Geometry> {
        INSTANCE;

        @Override
        public Geometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
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
