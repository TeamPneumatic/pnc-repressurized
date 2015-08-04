package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiPneumaticInventoryItem extends GuiPneumaticContainerBase<TileEntityChargingStation>{

    protected ItemStack itemStack;

    private GuiButton guiSelectButton;

    public GuiPneumaticInventoryItem(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container, te, Textures.GUI_PNEUMATIC_ARMOR_LOCATION);
        itemStack = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new GuiButton(2, xStart + 90, yStart + 15, 25, 20, "<--");
        buttonList.add(guiSelectButton);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        pressureStatText.add("\u00a77Current Pressure:");
        float curPressure = ((IPressurizable)itemStack.getItem()).getPressure(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX));
        int volume = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE, te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + getDefaultVolume() + " mL.");
        if(volume > getDefaultVolume()) {
            pressureStatText.add("\u00a70" + (double)Math.round(volume - getDefaultVolume()) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(volume) + " mL.");
        }
    }

    @Override
    protected boolean shouldAddRedstoneTab(){
        return false;
    }

    @Override
    protected boolean shouldAddUpgradeTab(){
        return false;
    }

    @Override
    protected boolean shouldAddInfoTab(){
        return false;
    }

    @Override
    protected boolean shouldAddProblemTab(){
        return false;
    }

    protected abstract int getDefaultVolume();

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        NetworkHandler.sendToServer(new PacketGuiButton(button.id));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        String containerName = itemStack.getDisplayName();
        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 4, 4210752);
        fontRendererObj.drawString(I18n.format("gui.tab.upgrades"), 36, 14, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        GuiUtils.drawPressureGauge(fontRendererObj, 0, 10, 10, 0, ((IPressurizable)itemStack.getItem()).getPressure(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)), xStart + xSize * 3 / 4 + 8, yStart + ySize * 1 / 4 + 4, zLevel);
    }

    @Override
    protected Point getGaugeLocation(){
        return null;
    }
}
