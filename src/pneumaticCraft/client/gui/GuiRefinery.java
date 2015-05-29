package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerRefinery;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.lib.Textures;

public class GuiRefinery extends GuiPneumaticContainerBase<TileEntityRefinery>{
    private GuiAnimatedStat statusStat;
    private int refineries = 1;

    public GuiRefinery(InventoryPlayer player, TileEntityRefinery te){
        super(new ContainerRefinery(player, te), te, Textures.GUI_REFINERY);
    }

    @Override
    public void initGui(){
        super.initGui();

        addWidget(new WidgetTemperature(0, guiLeft + 32, guiTop + 20, 273, 673, te.getHeatExchangerLogic(ForgeDirection.UNKNOWN), 573));

        addWidget(new WidgetTank(0, guiLeft + 8, guiTop + 13, te.getOilTank()));

        int x = guiLeft + 95;
        int y = guiTop + 17;
        addWidget(new WidgetTank(0, x, y, te.getOutputTank()));

        refineries = 1;
        TileEntityRefinery refinery = te;
        while(refineries < 4 && refinery.getTileCache()[ForgeDirection.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery)refinery.getTileCache()[ForgeDirection.UP.ordinal()].getTileEntity();
            x += 20;
            y -= 4;
            addWidget(new WidgetTank(0, x, y, refinery.getOutputTank()));
            refineries++;
        }

        statusStat = addAnimatedStat("gui.tab.hopperStatus", new ItemStack(Blockss.omnidirectionalHopper), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y){
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if(refineries < 4) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            drawTexturedModalRect(guiLeft + 155, guiTop + 5, xSize, 0, 16, 64);
            //drawTexturedModalRect(guiLeft + 48, guiTop + 22, xSize + 16, 0, 26, 10);
            if(refineries < 3) {
                drawTexturedModalRect(guiLeft + 135, guiTop + 9, xSize, 0, 16, 64);
                // drawTexturedModalRect(guiLeft + 48, guiTop + 32, xSize + 42, 0, 26, 15);
            }
        }
    }

    @Override
    protected Point getInvNameOffset(){
        return new Point(0, -1);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatus());
    }

    private List<String> getStatus(){
        List<String> textList = new ArrayList<String>();
        return textList;
    }
}
