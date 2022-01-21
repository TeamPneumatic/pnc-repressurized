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

package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.render.tube_module.TubeModuleRendererBase;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenderPressureTubeModule implements BlockEntityRenderer<TileEntityPressureTube> {

    private final Map<ResourceLocation, TubeModuleRendererBase<?>> models = new HashMap<>();
    private final BlockEntityRendererProvider.Context ctx;

    public RenderPressureTubeModule(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(TileEntityPressureTube tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (tile.getCamouflage() != null) {
            return;
        }

        ItemTubeModule moduleItem;
        Player player = ClientUtils.getClientPlayer();
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemTubeModule m) {
            moduleItem = m;
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemTubeModule m) {
            moduleItem = m;
        } else {
            moduleItem = null;
        }

        List<TubeModule> modules = tile.tubeModules().collect(Collectors.toList());
        if (modules.isEmpty() && moduleItem == null)
            return;

        if (moduleItem != null && Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
            // "fake" module is for showing a preview of where the module would be placed
            Direction face = player.isCrouching() ? blockHitResult.getDirection().getOpposite() : blockHitResult.getDirection();
            if (blockHitResult.getBlockPos().equals(tile.getBlockPos())
                    && player.level.getBlockEntity(blockHitResult.getBlockPos()) == tile
                    && tile.getModule(face) == null) {
                TubeModule fakeModule = moduleItem.createModule();
                if (tile.mayPlaceModule(fakeModule, face)) {
                    fakeModule.markFake();
                    fakeModule.setDirection(face);
                    fakeModule.setTube(tile);
                    getModuleRenderer(fakeModule).renderModule(fakeModule, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
                }
            }
        }

        for (TubeModule m : modules) {
            getModuleRenderer(m).renderModule(m, matrixStack, buffer, partialTicks, combinedLight, combinedOverlay);
        }
    }

    private <T extends TubeModule> TubeModuleRendererBase<T> getModuleRenderer(T module) {
        TubeModuleRendererBase<?> res = models.computeIfAbsent(module.getType(), k -> TubeModuleClientRegistry.createModel(module, ctx));
        //noinspection unchecked
        return (TubeModuleRendererBase<T>) res;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityPressureTube te) {
        return te.tubeModules().findAny().isPresent();
    }
}
