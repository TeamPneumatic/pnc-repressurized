package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateAirGrateModule;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAirGrateModule extends GuiTubeModule {
    private int sendTimer = 0;
    private GuiButtonSpecial warningButton;

    public GuiAirGrateModule(EntityPlayer player, int x, int y, int z) {
        super(player, x, y, z);
        ySize = 61;
    }

    private GuiTextField textfield;

    @Override
    public void initGui() {
        super.initGui();
        addLabel(I18n.format("gui.entityFilter"), guiLeft + 10, guiTop + 14);

        textfield = new GuiTextField(-1, fontRenderer, guiLeft + 10, guiTop + 25, 140, 10);
        textfield.setText(((ModuleAirGrate) module).getEntityFilterString());

        warningButton = new GuiButtonSpecial(3, guiLeft + 152, guiTop + 20, 20, 20, "");
        warningButton.setVisible(false);
        warningButton.setRenderedIcon(Textures.GUI_PROBLEMS_TEXTURE);
        buttonList.add(warningButton);

        validateEntityFilter(textfield.getText());
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!textfield.isFocused()) textfield.setText(((ModuleAirGrate) module).getEntityFilterString());
        textfield.drawTextBox();
        if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
            GuiUtils.showPopupHelpScreen(this, fontRenderer,
                    PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.entityFilter.helpText"), 60));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int par3) throws IOException {
        super.mouseClicked(mouseX, mouseY, par3);
        textfield.mouseClicked(mouseX, mouseY, par3);
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException {
        if (textfield.isFocused() && par2 != Keyboard.KEY_ESCAPE) {
            textfield.textboxKeyTyped(par1, par2);
            String filterStr = textfield.getText();
            if (validateEntityFilter(filterStr)) {
                sendTimer = 5;  // delayed send to reduce packet spam while typing
            }
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (sendTimer > 0 && --sendTimer == 0) {
            NetworkHandler.sendToServer(new PacketUpdateAirGrateModule(module, textfield.getText()));
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TEXT_WIDGET;
    }

}
