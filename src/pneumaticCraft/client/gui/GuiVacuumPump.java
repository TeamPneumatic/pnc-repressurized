package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerVacuumPump;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityVacuumPump;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVacuumPump extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_VACUUM_PUMP_LOCATION);
    private final TileEntityVacuumPump te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;

    public GuiVacuumPump(InventoryPlayer player, TileEntityVacuumPump teVacuumPump){

        super(new ContainerVacuumPump(player, teVacuumPump));
        ySize = 176;
        te = teVacuumPump;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_VACUUM_PUMP);
        upgradeStat.setText(GuiConstants.UPGRADES_VACUUM_PUMP);

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 131, yStart + 30, 130, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString("Upgr.", 76, 19, 4210752);

        fontRendererObj.drawString("+", 32, 47, 0xFF00AA00);
        fontRendererObj.drawString("-", 138, 47, 0xFFFF0000);
        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Ignore Redstone";
                break;
            case 1:
                redstoneButton.displayString = "High Redstone Signal";
                break;
            case 2:
                redstoneButton.displayString = "Low Redstone Signal";
        }

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MIN_PRESSURE_VACUUM_PUMP, te.getPressure(te.getInputSide()), xStart + xSize * 1 / 5, yStart + ySize * 1 / 5 + 4, zLevel);
        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, -1, te.getPressure(te.getVacuumSide()), xStart + xSize * 4 / 5, yStart + ySize * 1 / 5 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());

        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Only generate on       "); // the spaces are there
                                                        // to create space for
                                                        // the button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Input Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(te.getInputSide()) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Input Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Current Vacuum Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(te.getVacuumSide()) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Vacuum Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.vacuumAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_VACUUM_PUMP) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_VACUUM_PUMP;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }

        if(te.turning) {
            pressureStatText.add("\u00a77Currently sucking at:");
            pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.PRODUCTION_VACUUM_PUMP * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots())) + " mL/tick.");
        }
        return pressureStatText;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(te.getPressure(te.getInputSide()) < PneumaticValues.MIN_PRESSURE_VACUUM_PUMP) {
            textList.add("\u00a77Low pressure!");
            textList.add("\u00a70Apply more pressure.");
        } else if(!te.redstoneAllows()) {
            textList.add("\u00a77Redstone prevents production");
            if(te.redstoneMode == 1) {
                textList.add("\u00a70Apply a redstone signal");
            } else {
                textList.add("\u00a70Remove the redstone signal");
            }
        }

        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            if(te.getPressure(te.getVacuumSide()) <= -1F) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Minimal vacuum pressure reached.", GuiConstants.maxCharPerLineLeft));
            } else {
                textList.add("\u00a70Machine running.");
            }
        }
        return textList;
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        switch(button.id){
            case 0:// redstone button
                if(redstoneBehaviourStat != null) redstoneBehaviourStat.closeWindow();
                break;
        }

        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

}
