package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.inventory.ContainerSentryTurret;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.tileentity.TileEntitySentryTurret;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSentryTurret extends GuiPneumaticContainerBase<TileEntitySentryTurret>{

    private WidgetTextField entityFilter;

    public GuiSentryTurret(InventoryPlayer player, TileEntitySentryTurret te){

        super(new ContainerSentryTurret(player, te), te, Textures.GUI_SENTRY_TURRET);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(entityFilter = new WidgetTextField(fontRendererObj, guiLeft + 80, guiTop + 63, 70, fontRendererObj.FONT_HEIGHT));

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(!entityFilter.isFocused()) entityFilter.setText(te.getText(0));
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        if(!entityFilter.isFocused()) {
            te.setText(0, entityFilter.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        }
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();

        te.setText(0, entityFilter.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
        fontRendererObj.drawString(I18n.format("gui.sentryTurret.ammo"), 80, 19, 4210752);
        fontRendererObj.drawString(I18n.format("gui.sentryTurret.targetFilter"), 80, 53, 4210752);
    }

    @Override
    protected void addProblems(List<String> curInfo){
        super.addProblems(curInfo);

        boolean hasAmmo = false;
        for(int i = 4; i < te.getSizeInventory(); i++) {
            if(te.getStackInSlot(i) != null) {
                hasAmmo = true;
                break;
            }
        }
        if(!hasAmmo) curInfo.add("gui.tab.problems.sentryTurret.noAmmo");
    }

}
