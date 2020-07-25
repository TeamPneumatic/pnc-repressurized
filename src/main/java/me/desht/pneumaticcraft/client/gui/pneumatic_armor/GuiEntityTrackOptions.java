package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiEntityTrackOptions extends IOptionPage.SimpleToggleableOptions<EntityTrackUpgradeHandler> {

    private TextFieldWidget textField;
    private WidgetButtonExtended warningButton;
    private int sendTimer = 0;

    public GuiEntityTrackOptions(IGuiScreen screen, EntityTrackUpgradeHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.ENTITY_TRACKER));
        }));

        textField = new TextFieldWidget(gui.getFontRenderer(), 35, 60, 140, 10, StringTextComponent.EMPTY);
//        textField.setFocused2(true);
        if (Minecraft.getInstance().player != null) {
            textField.setText(ItemPneumaticArmor.getEntityFilter(Minecraft.getInstance().player.getItemStackFromSlot(EquipmentSlotType.HEAD)));
        }
        textField.setResponder(s -> {
            if (validateEntityFilter(textField.getText())) {
                sendTimer = 5;
            }
        });
        gui.addWidget(textField);
        gui.setFocusedWidget(textField);

        warningButton = new WidgetButtonExtended(175, 57, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.visible = false;
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        gui.addWidget(warningButton);

        validateEntityFilter(textField.getText());
    }

    @Override
    public void renderPost(MatrixStack matrixStack, int x, int y, float partialTicks) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        fontRenderer.drawString(matrixStack, I18n.format("pneumaticcraft.gui.entityFilter"), 35, 50, 0xFFFFFFFF);
        if (ClientUtils.isKeyDown(GLFW.GLFW_KEY_F1)) {
            GuiUtils.showPopupHelpScreen(matrixStack, Minecraft.getInstance().currentScreen, fontRenderer,
                    PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.entityFilter.helpText"), 60));
        }
    }

    private boolean validateEntityFilter(String filter) {
        try {
            warningButton.visible = false;
            warningButton.setTooltipText(StringTextComponent.EMPTY);
            new EntityFilter(filter);  // syntax check
            return true;
        } catch (Exception e) {
            warningButton.visible = true;
            warningButton.setTooltipText(new StringTextComponent(e.getMessage()).mergeStyle(TextFormatting.GOLD));
            return false;
        }
    }

    @Override
    public void tick() {
        if (sendTimer > 0 && --sendTimer == 0) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString(ItemPneumaticArmor.NBT_ENTITY_FILTER, textField.getText());
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.HEAD, tag));
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }
}
