package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class GuiEntityTrackOptions implements IOptionPage {

    private final EntityTrackUpgradeHandler renderHandler;
    private GuiTextField textField;
    private GuiButtonSpecial warningButton;
    private int sendTimer = 0;

    public GuiEntityTrackOptions(EntityTrackUpgradeHandler renderHandler) {
        this.renderHandler = renderHandler;
    }

    @Override
    public String getPageName() {
        return "Entity Tracker";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Stat Screen..."));

        textField = new GuiTextField(-1, gui.getFontRenderer(), 35, 60, 140, 10);
        textField.setFocused(true);
        if (PneumaticCraftRepressurized.proxy.getClientPlayer() != null) {
            textField.setText(ItemPneumaticArmor.getEntityFilter(PneumaticCraftRepressurized.proxy.getClientPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
        }

        warningButton = new GuiButtonSpecial(1, 175, 57, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.visible = false;
        warningButton.setRenderedIcon(new ResourceLocation(Textures.GUI_PROBLEMS_TEXTURE));
        gui.getButtonList().add(warningButton);

        validateEntityFilter(textField.getText());
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            Minecraft.getMinecraft().player.closeScreen();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMoveStat(renderHandler));
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        textField.drawTextBox();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(I18n.format("gui.entityFilter"), 35, 50, 0xFFFFFFFF);
        if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
            GuiUtils.showPopupHelpScreen(Minecraft.getMinecraft().currentScreen, fontRenderer,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    public void keyTyped(char ch, int key) {
        if (textField != null && textField.isFocused() && key != 1) {
            textField.textboxKeyTyped(ch, key);
            if (validateEntityFilter(textField.getText())) {
                sendTimer = 5;
            }
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText("");
            EntityFilter f = new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(TextFormatting.GOLD + e.getMessage());
            return false;
        }
    }

    @Override
    public void updateScreen() {
        if (sendTimer > 0 && --sendTimer == 0) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("entityFilter", textField.getText());
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EntityEquipmentSlot.HEAD, tag));
        }
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
    }

    @Override
    public void handleMouseInput() {
    }

    @Override
    public boolean canBeTurnedOff() {
        return true;
    }

    @Override
    public boolean displaySettingsText() {
        return true;
    }
}
