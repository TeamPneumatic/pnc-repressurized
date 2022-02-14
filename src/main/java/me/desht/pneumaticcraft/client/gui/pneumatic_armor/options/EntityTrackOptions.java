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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorStatMoveScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackOptions extends IOptionPage.SimpleOptionPage<EntityTrackerClientHandler> {

    private EditBox textField;
    private WidgetButtonExtended warningButton;
    private int sendTimer = 0;

    public EntityTrackOptions(IGuiScreen screen, EntityTrackerClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeContainer();
            Minecraft.getInstance().setScreen(new ArmorStatMoveScreen(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.ENTITY_TRACKER));
        }));

        textField = new EditBox(gui.getFontRenderer(), 35, 60, 140, 10, TextComponent.EMPTY);
        if (Minecraft.getInstance().player != null) {
            textField.setValue(ItemPneumaticArmor.getEntityFilter(Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.HEAD)));
        }
        textField.setResponder(s -> {
            if (validateEntityFilter(textField.getValue())) {
                sendTimer = 5;
            }
        });
        gui.addWidget(textField);
        gui.setFocusedWidget(textField);

        warningButton = new WidgetButtonExtended(175, 57, 20, 20, TextComponent.EMPTY);
        warningButton.setVisible(false);
        warningButton.visible = false;
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        gui.addWidget(warningButton);

        validateEntityFilter(textField.getValue());
    }

    @Override
    public void renderPost(PoseStack matrixStack, int x, int y, float partialTicks) {
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.draw(matrixStack, I18n.get("pneumaticcraft.gui.entityFilter"), 35, 50, 0xFFFFFFFF);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(matrixStack, Minecraft.getInstance().screen, fontRenderer,
                    GuiUtils.xlateAndSplit("pneumaticcraft.gui.entityFilter.helpText"));
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText(TextComponent.EMPTY);
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(new TextComponent(e.getMessage()).withStyle(ChatFormatting.GOLD));
            return false;
        }
    }

    @Override
    public void tick() {
        if (sendTimer > 0 && --sendTimer == 0) {
            CompoundTag tag = new CompoundTag();
            tag.putString(ItemPneumaticArmor.NBT_ENTITY_FILTER, textField.getValue());
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlot.HEAD, tag, getClientUpgradeHandler().getCommonHandler().getID()));
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
