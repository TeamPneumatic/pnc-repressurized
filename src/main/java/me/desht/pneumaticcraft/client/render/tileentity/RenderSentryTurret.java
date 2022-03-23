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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderSentryTurret extends AbstractTileModelRenderer<TileEntitySentryTurret> {
    private final ModelMinigun model;

    public RenderSentryTurret(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        model = new ModelMinigun();
    }

    @Override
    void renderModel(TileEntitySentryTurret te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.translate(0, -13 / 16F, 0);
        model.renderMinigun(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, te.getMinigun(), partialTicks, false);
    }
}
