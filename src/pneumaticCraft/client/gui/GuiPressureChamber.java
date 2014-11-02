package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPressureChamber extends GuiPneumaticContainerBase<TileEntityPressureChamberValve>{

    private GuiAnimatedStat statusStat;

    public GuiPressureChamber(InventoryPlayer player, TileEntityPressureChamberValve te){

        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        statusStat = addAnimatedStat("Pressure Chamber Status", new ItemStack(Blockss.pressureChamberWall), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : I18n.format("gui.pressureChamberTitle", te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize);
        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected Point getInvNameOffset(){
        return null;
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Chamber Size:");
        text.add("\u00a70" + te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize + " (outside)");
        text.add("\u00a70" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + " (inside)");
        text.add("\u00a77Recipe list:");
        if(PneumaticCraft.isNEIInstalled) {
            text.add("\u00a70Click on the Pressure gauge to view all the recipes of this machine. Powered by ChickenBones' NEI.");
        } else {
            text.add("\u00a70Install NEI (an other (client) mod by ChickenBones) to be able to see all the recipes of this machine.");
        }
        return text;
    }

    @Override
    protected void addProblems(List<String> textList){
        if(!te.isValidRecipeInChamber) {
            textList.add("\u00a77No (valid) items in the chamber");
            textList.add("\u00a70Insert (valid) items");
            textList.add("\u00a70in the chamber");
        } else if(!te.isSufficientPressureInChamber) {
            if(te.recipePressure > 0F) {
                textList.add("\u00a77Not enough pressure");
                textList.add("\u00a70Add air to the input");
            } else {
                textList.add("\u00a77Too much pressure");
                textList.add("\u00a70Remove air from the input");
            }
            textList.add("\u00a70Pressure required: " + te.recipePressure + " bar");
        } else if(!te.areEntitiesDoneMoving) {
            textList.add("\u00a77Items are too far away from eachother");
            textList.add("\u00a70Wait until the items are blown to the middle.");
        }
    }
}
