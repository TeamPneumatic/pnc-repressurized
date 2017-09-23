package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateAirGrateModule;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiAirGrateModule extends GuiTubeModule {

    public GuiAirGrateModule(EntityPlayer player, int x, int y, int z) {
        super(player, x, y, z);
        ySize = 61;
    }

    private GuiTextField textfield;

    @Override
    public void initGui() {
        super.initGui();
        addLabel(I18n.format("gui.entityFilter"), guiLeft + 10, guiTop + 14);

        textfield = new GuiTextField(-1, fontRenderer, guiLeft + 10, guiTop + 25, 160, 10);
        textfield.setText(((ModuleAirGrate) module).entityFilter);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!textfield.isFocused()) textfield.setText(((ModuleAirGrate) module).entityFilter);
        textfield.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int par3) throws IOException {
        super.mouseClicked(x, y, par3);
        textfield.mouseClicked(x, y, par3);
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException {
        if (textfield.isFocused() && par2 != 1) {
            textfield.textboxKeyTyped(par1, par2);
            ((ModuleAirGrate) module).entityFilter = textfield.getText();
            NetworkHandler.sendToServer(new PacketUpdateAirGrateModule(module, textfield.getText()));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_TEXT_WIDGET;
    }

}
