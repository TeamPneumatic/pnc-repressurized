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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

public class EntityTrackOptions extends IOptionPage.SimpleOptionPage<EntityTrackerClientHandler> {

    private EditBox textField;
    private WidgetButtonExtended warningButton;
    private int sendTimer = 0;

    public EntityTrackOptions(IGuiScreen screen, EntityTrackerClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(ClientArmorRegistry.getInstance().makeStatMoveButton(30, 128, getClientUpgradeHandler()));

        textField = new EditBox(gui.getFontRenderer(), 35, 75, 140, 10, Component.empty());
        if (Minecraft.getInstance().player != null) {
            textField.setValue(PneumaticArmorItem.getEntityFilter(Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.HEAD)));
        }
        textField.setResponder(s -> {
            if (validateEntityFilter(textField.getValue())) {
                sendTimer = 5;
            }
        });
        gui.addWidget(textField);
        gui.setFocusedWidget(textField);

        warningButton = new WidgetButtonExtended(175, 57, 20, 20, Component.empty());
        warningButton.setVisible(false);
        warningButton.visible = false;
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        gui.addWidget(warningButton);

        validateEntityFilter(textField.getValue());
    }

    @Override
    public void renderPost(GuiGraphics graphics, int x, int y, float partialTicks) {
        Font font = getGuiScreen().getFontRenderer();
        graphics.drawString(font, I18n.get("pneumaticcraft.gui.entityFilter"), 35, textField.getY() - font.lineHeight - 2, 0xFFFFFFFF, false);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(graphics, getGuiScreen().getScreen(), font,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText(Component.empty());
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(Component.literal(e.getMessage()).withStyle(ChatFormatting.GOLD));
            return false;
        }
    }

    @Override
    public void tick() {
        if (sendTimer > 0 && --sendTimer == 0) {
            CompoundTag tag = new CompoundTag();
            tag.putString(PneumaticArmorItem.NBT_ENTITY_FILTER, textField.getValue());
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlot.HEAD, tag, getClientUpgradeHandler().getID()));
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
