package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.common.inventory.ContainerAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAirCompressor extends GuiPneumaticContainerBase<TileEntityAirCompressor>{

    public GuiAirCompressor(Container container, TileEntityAirCompressor te, String texture){

        super(container, te, texture);
    }

    public GuiAirCompressor(InventoryPlayer player, TileEntityAirCompressor te){

        super(new ContainerAirCompressor(player, te), te, Textures.GUI_AIR_COMPRESSOR_LOCATION);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindGuiTexture();

        int i1 = te.getBurnTimeRemainingScaled(12);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        if(te.burnTime >= te.curFuelUsage) drawTexturedModalRect(xStart + getFuelSlotXOffset(), yStart + 38 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
    }

    protected int getFuelSlotXOffset(){
        return 80;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        if(te.getBurnTimeRemainingScaled(12) > 0 || TileEntityFurnace.isItemFuel(te.getStackInSlot(0)) && te.redstoneAllows()) {
            pressureStatText.add("\u00a77Currently producing:");
            pressureStatText.add("\u00a70" + (double)Math.round(te.getBaseProduction() * te.getEfficiency() * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots()) / 100) + " mL/tick.");
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.burnTime <= 0 && !TileEntityFurnace.isItemFuel(te.getStackInSlot(0))) {
            textList.add("\u00a77No fuel!");
            textList.add("\u00a70Insert any burnable item.");
        }
        List<Pair<ForgeDirection, IAirHandler>> teSurrounding = te.getConnectedPneumatics();
        if(teSurrounding.isEmpty()) {
            textList.add("\u00a77Air leaking!");
            textList.add("\u00a70Add pipes / machines");
            textList.add("\u00a70to the output.");
        }
    }
}
