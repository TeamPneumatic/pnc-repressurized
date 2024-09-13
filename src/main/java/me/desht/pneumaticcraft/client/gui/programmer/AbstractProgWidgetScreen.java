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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketProgrammerSync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractProgWidgetScreen<P extends IProgWidget> extends AbstractPneumaticCraftScreen {
    protected final P progWidget;
    protected final ProgrammerScreen guiProgrammer;

    AbstractProgWidgetScreen(P progWidget, ProgrammerScreen guiProgrammer) {
        super(Component.translatable(progWidget.getTranslationKey()));

        this.progWidget = progWidget;
        this.guiProgrammer = guiProgrammer;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        Component title = xlate(progWidget.getTranslationKey());
        addLabel(title, width / 2 - font.width(title) / 2, guiTop + 5);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(guiProgrammer);
    }

    @Override
    public void removed() {
        // Important: when overriding this in subclasses, copy any update gui data into the
        // progwidget BEFORE calling super.removed() !

        if (guiProgrammer != null) {
            NetworkHandler.sendToServer(PacketProgrammerSync.forBlockEntity(guiProgrammer.te));
        } else {
            super.removed();
        }
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
        return ConfigHelper.client().general.programmerGuiPauses.get();
    }

    public AbstractContainerMenu getProgrammerContainer() {
        return guiProgrammer == null ? null : guiProgrammer.getMenu();
    }
}
