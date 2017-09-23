package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateEntityFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GuiEntityTrackOptions implements IOptionPage {

    private final EntityTrackUpgradeHandler renderHandler;
    private GuiTextField textField;

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
        if (PneumaticCraftRepressurized.proxy.getPlayer() != null)
            textField.setText(ItemPneumaticArmor.getEntityFilter(PneumaticCraftRepressurized.proxy.getPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            FMLClientHandler.instance().getClient().player.closeScreen();
            FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler));
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
    }

    @Override
    public void keyTyped(char ch, int key) {
        if (textField != null && textField.isFocused() && key != 1) {
            textField.textboxKeyTyped(ch, key);
            NetworkHandler.sendToServer(new PacketUpdateEntityFilter(textField.getText()));
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
