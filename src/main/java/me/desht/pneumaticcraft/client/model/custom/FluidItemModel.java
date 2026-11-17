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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.fluid.AbstractFluidTER;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Draws the fluid contents of fluid-holding items (tanks, liquid hopper), using the same code as the block entity
 * renderers. Used as the second part of a {@code minecraft:composite} item model, after the item's base model;
 * the special model's {@code base} should be the same model so that the display transforms match.
 */
public class FluidItemModel implements SpecialModelRenderer<List<TankRenderInfo>> {
    private static final FluidItemModel INSTANCE = new FluidItemModel();

    @Override
    public @Nullable List<TankRenderInfo> extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof IFluidRendered r) {
            List<TankRenderInfo> tanks = r.getFluidItemRenderer().getTanksToRender(stack);
            return tanks.isEmpty() ? null : List.copyOf(tanks);
        }
        return null;
    }

    @Override
    public void submit(@Nullable List<TankRenderInfo> tanks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (tanks == null) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.getEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS), (pose, builder) -> {
            for (TankRenderInfo tank : tanks) {
                AbstractFluidTER.renderFluid(builder, tank, pose.pose(), lightCoords, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // the base model of the composite item model provides the extents
    }

    public enum Unbaked implements SpecialModelRenderer.Unbaked<List<TankRenderInfo>> {
        INSTANCE;

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public SpecialModelRenderer<List<TankRenderInfo>> bake(BakingContext context) {
            return FluidItemModel.INSTANCE;
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
