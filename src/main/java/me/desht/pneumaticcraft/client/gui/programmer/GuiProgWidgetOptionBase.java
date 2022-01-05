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

package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerUpdate;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class GuiProgWidgetOptionBase<P extends IProgWidget> extends GuiPneumaticScreenBase {
    protected final P progWidget;
    protected final GuiProgrammer guiProgrammer;

    GuiProgWidgetOptionBase(P progWidget, GuiProgrammer guiProgrammer) {
        super(new TranslationTextComponent(progWidget.getTranslationKey()));

        this.progWidget = progWidget;
        this.guiProgrammer = guiProgrammer;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        ITextComponent title = xlate(progWidget.getTranslationKey());
        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(guiProgrammer);
    }

    @Override
    public void removed() {
        // Important: when overriding this in subclasses, copy any update gui data into the
        // progwidget BEFORE calling super.close() !

        if (guiProgrammer != null) {
            NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
        } else {
            super.removed();
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean isPauseScreen() {
        return ConfigHelper.client().general.programmerGuiPauses.get();
    }

    public Container getProgrammerContainer() {
        return guiProgrammer == null ? null : guiProgrammer.getMenu();
    }
}
