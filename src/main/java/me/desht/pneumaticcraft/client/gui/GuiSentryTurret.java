package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.inventory.ContainerSentryTurret;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSentryTurret extends GuiPneumaticContainerBase<TileEntitySentryTurret> {

    private WidgetTextField entityFilter;

    public GuiSentryTurret(InventoryPlayer player, TileEntitySentryTurret te) {

        super(new ContainerSentryTurret(player, te), te, Textures.GUI_SENTRY_TURRET);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(entityFilter = new WidgetTextField(fontRenderer, guiLeft + 80, guiTop + 63, 70, fontRenderer.FONT_HEIGHT));

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!entityFilter.isFocused()) entityFilter.setText(te.getText(0));
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);
        if (!entityFilter.isFocused()) {
            te.setText(0, entityFilter.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        te.setText(0, entityFilter.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
        fontRenderer.drawString(I18n.format("gui.sentryTurret.ammo"), 80, 19, 4210752);
        fontRenderer.drawString(I18n.format("gui.sentryTurret.targetFilter"), 80, 53, 4210752);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        boolean hasAmmo = false;
        for (int i = 4; i < te.getPrimaryInventory().getSlots(); i++) {
            if (!te.getPrimaryInventory().getStackInSlot(i).isEmpty()) {
                hasAmmo = true;
                break;
            }
        }
        if (!hasAmmo) curInfo.add("gui.tab.problems.sentryTurret.noAmmo");
    }

}
