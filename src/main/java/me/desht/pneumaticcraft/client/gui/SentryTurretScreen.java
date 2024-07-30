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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.SentryTurretBlockEntity;
import me.desht.pneumaticcraft.common.inventory.SentryTurretMenu;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SentryTurretScreen extends AbstractPneumaticCraftContainerScreen<SentryTurretMenu,SentryTurretBlockEntity> {
    private WidgetTextField entityFilter;
    private WidgetButtonExtended errorButton;

    public SentryTurretScreen(SentryTurretMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SENTRY_TURRET;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(entityFilter = new WidgetTextField(font, leftPos + 80, topPos + 62, 70));
        entityFilter.setMaxLength(256);
        setFocused(entityFilter);

        addRenderableWidget(errorButton = new WidgetButtonExtended(leftPos + 155, topPos + 52, 16, 16, Component.empty()));
        errorButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE).setVisible(false);
        errorButton.visible = false;
    }

    @Override
    public void containerTick() {
        if (firstUpdate) {
            // setting the filter value in the textfield on init() isn't reliable; might not be sync'd in time
            entityFilter.setValue(te.getText(0));
            entityFilter.setResponder(this::onEntityFilterChanged);
        }

        super.containerTick();

//        errorButton.visible = errorButton.hasTooltip();
    }

    private void onEntityFilterChanged(String newText) {
        try {
            new EntityFilter(newText);
            errorButton.visible = false;
            errorButton.setTooltipText(Collections.emptyList());
            sendDelayed(5);
        } catch (IllegalArgumentException e) {
            errorButton.visible = true;
            errorButton.setTooltipText(Component.literal(e.getMessage()));
        }
    }

    @Override
    protected void doDelayedAction() {
        te.setText(0, entityFilter.getValue());
        NetworkHandler.sendToServer(PacketUpdateTextfield.create(te, 0));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        graphics.drawString(font, xlate("pneumaticcraft.gui.sentryTurret.ammo"), 80, 19, 0x404040, false);
        graphics.drawString(font, xlate("pneumaticcraft.gui.sentryTurret.targetFilter"), 80, 53, 0x404040, false);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(graphics, this, font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        } else if (x >= leftPos + 76 && y >= topPos + 51 && x <= leftPos + 153 && y <= topPos + 74) {
            // cursor inside the entity filter area
            Component str = xlate("pneumaticcraft.gui.entityFilter.holdF1");
            graphics.drawString(font, str, (imageWidth - font.width(str)) / 2, imageHeight + 5, 0x808080, false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
        }

        return entityFilter.keyPressed(keyCode, scanCode, modifiers)
                || entityFilter.canConsumeInput()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        boolean hasAmmo = false;
        for (int i = 0; i < te.getItemHandler().getSlots(); i++) {
            if (!te.getItemHandler().getStackInSlot(i).isEmpty()) {
                hasAmmo = true;
                break;
            }
        }
        if (!hasAmmo) curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.sentryTurret.noAmmo"));
    }
}
