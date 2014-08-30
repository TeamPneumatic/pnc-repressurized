package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberValve;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPressureChamber extends GuiPneumaticContainerBase{

    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_4UPGRADE_SLOTS);
    private final TileEntityPressureChamberValve teValve;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;

    public GuiPressureChamber(InventoryPlayer player, TileEntityPressureChamberValve teValve){

        super(new Container4UpgradeSlots(player, teValve));
        ySize = 176;
        this.teValve = teValve;
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        statusStat = new GuiAnimatedStat(this, "Pressure Chamber Status", new ItemStack(Blockss.pressureChamberWall), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        infoStat.setText(GuiConstants.INFO_PRESSURE_CHAMBER);
        upgradeStat.setText(GuiConstants.UPGRADES_PRESSURE_CHAMBER);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = teValve.hasCustomInventoryName() ? teValve.getInventoryName() : StatCollector.translateToLocal(teValve.getInventoryName());

        fontRendererObj.drawString(teValve.multiBlockSize + "x" + teValve.multiBlockSize + "x" + teValve.multiBlockSize + " " + containerName, xSize / 2 - fontRendererObj.getStringWidth(teValve.multiBlockSize + "x" + teValve.multiBlockSize + "x" + teValve.multiBlockSize + " " + containerName) / 2, 6, 4210752);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, teValve.recipePressure, teValve.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        statusStat.setText(getStatusText());
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(teValve.getPressure(ForgeDirection.UNKNOWN) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(teValve.currentAir + teValve.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_PRESSURE_CHAMBER) + " mL. (valve volume)");
        float chamberVolume = (float)Math.pow(teValve.multiBlockSize - 2, 3) * PneumaticValues.VOLUME_PRESSURE_CHAMBER_PER_EMPTY;
        pressureStatText.add("\u00a70" + (double)Math.round(chamberVolume) + " mL. (" + (teValve.multiBlockSize - 2) + "x" + (teValve.multiBlockSize - 2) + "x" + (teValve.multiBlockSize - 2) + " air blocks)");
        float pressureLeft = teValve.volume - chamberVolume - PneumaticValues.VOLUME_PRESSURE_CHAMBER;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
        }
        pressureStatText.add("\u00a70--------+");
        pressureStatText.add("\u00a70" + (double)Math.round(teValve.volume) + " mL.");
        return pressureStatText;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Chamber Size:");
        text.add("\u00a70" + teValve.multiBlockSize + "x" + teValve.multiBlockSize + "x" + teValve.multiBlockSize + " (outside)");
        text.add("\u00a70" + (teValve.multiBlockSize - 2) + "x" + (teValve.multiBlockSize - 2) + "x" + (teValve.multiBlockSize - 2) + " (inside)");
        text.add("\u00a77Recipe list:");
        if(PneumaticCraft.isNEIInstalled) {
            text.add("\u00a70Click on the Pressure gauge to view all the recipes of this machine. Powered by ChickenBones' NEI.");
        } else {
            text.add("\u00a70Install NEI (an other (client) mod by ChickenBones) to be able to see all the recipes of this machine.");
        }
        return text;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(!teValve.isValidRecipeInChamber) {
            textList.add("\u00a77No (valid) items in the chamber");
            textList.add("\u00a70Insert (valid) items");
            textList.add("\u00a70in the chamber");
        } else if(!teValve.isSufficientPressureInChamber) {
            if(teValve.recipePressure > 0F) {
                textList.add("\u00a77Not enough pressure");
                textList.add("\u00a70Add air to the input");
            } else {
                textList.add("\u00a77Too much pressure");
                textList.add("\u00a70Remove air from the input");
            }
            textList.add("\u00a70Pressure required: " + teValve.recipePressure + " bar");
        } else if(!teValve.areEntitiesDoneMoving) {
            textList.add("\u00a77Items are too far away from eachother");
            textList.add("\u00a70Wait until the items are blown to the middle.");
        }
        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            // textList.add("\u00a70Apply a redstone");
            // textList.add("\u00a70signal to fire.");
        }
        return textList;
    }
}
