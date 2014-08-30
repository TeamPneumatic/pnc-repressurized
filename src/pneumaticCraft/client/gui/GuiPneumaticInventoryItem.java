package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPneumaticInventoryItem extends GuiPneumaticContainerBase{

    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_PNEUMATIC_ARMOR_LOCATION);
    protected ItemStack itemStack;

    private GuiButton guiSelectButton;
    protected final TileEntityChargingStation te;

    public GuiPneumaticInventoryItem(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container);
        ySize = 176;
        itemStack = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
        container.armor.setGui(this);
        this.te = te;
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new GuiButton(2, xStart + 90, yStart + 15, 25, 20, "<--");
        buttonList.add(guiSelectButton);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        String containerName = itemStack.getDisplayName();
        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 4, 4210752);
        fontRendererObj.drawString("Upgrades", 36, 14, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        GuiUtils.drawPressureGauge(fontRendererObj, 0, 10, 10, 0, ((IPressurizable)itemStack.getItem()).getPressure(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)), xStart + xSize * 3 / 4 + 8, yStart + ySize * 1 / 4 + 4, zLevel);
    }
}
