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

package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractTubeModuleScreen<M extends AbstractTubeModule> extends AbstractPneumaticCraftScreen {
    protected final M module;

    AbstractTubeModuleScreen(M module) {
        super(new ItemStack(module.getItem()).getHoverName());

        this.module = module;
        this.xSize = 183;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        if (getGuiTexture() == null) {
            GuiUtils.drawScreenWithTitleArea(graphics, guiLeft, guiTop, xSize, ySize, 0xFFDDD7BA);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void openGuiForModule(AbstractTubeModule module) {
        Minecraft.getInstance().setScreen(TubeModuleClientRegistry.createGUI(module));
    }
}
