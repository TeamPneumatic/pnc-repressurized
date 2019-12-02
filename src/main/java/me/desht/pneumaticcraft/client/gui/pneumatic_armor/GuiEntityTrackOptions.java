package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

public class GuiEntityTrackOptions implements IOptionPage {

    private final EntityTrackUpgradeHandler renderHandler;
    private TextFieldWidget textField;
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
        gui.getWidgetList().add(new GuiButtonSpecial(30, 128, 150, 20, "Move Stat Screen...", b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(renderHandler, ArmorHUDLayout.LayoutTypes.ENTITY_TRACKER));
        }));

        textField = new TextFieldWidget(gui.getFontRenderer(), 35, 60, 140, 10, "");
        textField.setFocused2(true);
        if (Minecraft.getInstance().player != null) {
            textField.setText(ItemPneumaticArmor.getEntityFilter(Minecraft.getInstance().player.getItemStackFromSlot(EquipmentSlotType.HEAD)));
        }
        textField.setResponder(s -> {
            if (validateEntityFilter(textField.getText())) {
                sendTimer = 5;
            }
        });

        warningButton = new GuiButtonSpecial(175, 57, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.visible = false;
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        gui.getWidgetList().add(warningButton);

        validateEntityFilter(textField.getText());
    }

    public void renderPre(int x, int y, float partialTicks) {
    }

    public void renderPost(int x, int y, float partialTicks) {
        textField.render(x, y, partialTicks);
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString(I18n.format("gui.entityFilter"), 35, 50, 0xFFFFFFFF);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(Minecraft.getInstance().currentScreen, fontRenderer,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        return false;
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText("");
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(TextFormatting.GOLD + e.getMessage());
            return false;
        }
    }

    public void tick() {
        if (sendTimer > 0 && --sendTimer == 0) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString(ItemPneumaticArmor.NBT_ENTITY_FILTER, textField.getText());
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.HEAD, tag));
        }
    }

    @Override
    public boolean canBeTurnedOff() {
        return true;
    }

    public boolean displaySettingsHeader() {
        return true;
    }
}
