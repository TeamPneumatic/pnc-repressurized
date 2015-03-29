package pneumaticCraft.client.gui.tubemodule;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.block.tubes.ModuleAirGrate;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateAirGrateModule;
import pneumaticCraft.lib.Textures;

public class GuiAirGrateModule extends GuiTubeModule{

    public GuiAirGrateModule(EntityPlayer player, int x, int y, int z){
        super(player, x, y, z);
        ySize = 61;
    }

    private GuiTextField textfield;

    @Override
    public void initGui(){
        super.initGui();
        addLabel(I18n.format("gui.entityFilter"), guiLeft + 10, guiTop + 14);

        textfield = new GuiTextField(fontRendererObj, guiLeft + 10, guiTop + 25, 160, 10);
        textfield.setText(((ModuleAirGrate)module).entityFilter);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(!textfield.isFocused()) textfield.setText(((ModuleAirGrate)module).entityFilter);
        textfield.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int par3){
        super.mouseClicked(x, y, par3);
        textfield.mouseClicked(x, y, par3);
    }

    @Override
    public void keyTyped(char par1, int par2){
        if(textfield.isFocused() && par2 != 1) {
            textfield.textboxKeyTyped(par1, par2);
            ((ModuleAirGrate)module).entityFilter = textfield.getText();
            NetworkHandler.sendToServer(new PacketUpdateAirGrateModule(module, textfield.getText()));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_TEXT_WIDGET;
    }

}
