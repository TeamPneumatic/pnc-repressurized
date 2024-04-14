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

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.render.tube_module.AbstractTubeModuleRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.item.TubeModuleItem;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PressureTubeModuleRenderer implements BlockEntityRenderer<PressureTubeBlockEntity> {

    private final Map<ResourceLocation, AbstractTubeModuleRenderer<?>> models = new HashMap<>();
    private final BlockEntityRendererProvider.Context ctx;

    public PressureTubeModuleRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(PressureTubeBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (tile.getCamouflage() != null) {
            return;
        }

        TubeModuleItem moduleItem;
        Player player = ClientUtils.getClientPlayer();
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TubeModuleItem m) {
            moduleItem = m;
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof TubeModuleItem m) {
            moduleItem = m;
        } else {
            moduleItem = null;
        }

        List<AbstractTubeModule> modules = tile.tubeModules().toList();
        if (modules.isEmpty() && moduleItem == null)
            return;

        if (moduleItem != null && Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            // "fake" module is for showing a preview of where the module would be placed
            Direction face = player.isCrouching() ? blockHitResult.getDirection().getOpposite() : blockHitResult.getDirection();
            if (blockHitResult.getBlockPos().equals(tile.getBlockPos())
                    && player.level().getBlockEntity(blockHitResult.getBlockPos()) == tile
                    && tile.getModule(face) == null) {
                AbstractTubeModule fakeModule = moduleItem.createModule(face, tile);
                if (tile.mayPlaceModule(fakeModule)) {
                    fakeModule.markFake();
                    getModuleRenderer(fakeModule).renderModule(fakeModule, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
                }
            }
        }

        for (AbstractTubeModule m : modules) {
            getModuleRenderer(m).renderModule(m, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
        }
    }

    private <T extends AbstractTubeModule> AbstractTubeModuleRenderer<T> getModuleRenderer(T module) {
        AbstractTubeModuleRenderer<?> res = models.computeIfAbsent(module.getType(), k -> TubeModuleClientRegistry.createModel(module, ctx));
        //noinspection unchecked
        return (AbstractTubeModuleRenderer<T>) res;
    }

    @Override
    public boolean shouldRenderOffScreen(PressureTubeBlockEntity te) {
        return te.tubeModules().findAny().isPresent();
    }

    @Override
    public AABB getRenderBoundingBox(PressureTubeBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
