package pneumaticCraft.common.thirdparty.buildcraft;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.GuiUtils;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiKineticCompressor extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_4UPGRADE_SLOTS);
    private final TileEntityKineticCompressor te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat inputStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;

    public GuiKineticCompressor(InventoryPlayer player, TileEntityKineticCompressor te){

        super(new Container4UpgradeSlots(player, te));
        ySize = 176;
        this.te = te;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        inputStat = new GuiAnimatedStat(this, "Input", Textures.GUI_BUILDCRAFT_ENERGY, xStart + xSize, 3, 0xFFbd630a, pressureStat, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, inputStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(inputStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setText(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_KINETIC_COMPRESSOR);
        upgradeStat.setText(GuiConstants.UPGRADES_KINETIC_COMPRESSOR);

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 118, yStart + 30, 117, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);

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

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_KINETIC_COMPRESSOR, PneumaticValues.DANGER_PRESSURE_KINETIC_COMPRESSOR, -1, te.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        inputStat.setText(getOutputStat());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
    }

    private List<String> getOutputStat(){
        List<String> textList = new ArrayList<String>();
        textList.add(EnumChatFormatting.GRAY + "Energy used:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.energyUsed, 1) + " MJ/tick");
        textList.add(EnumChatFormatting.GRAY + "Energy stored:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.getPowerNetEnergy(), 1) + " MJ");
        return textList;
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
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.getPressure(ForgeDirection.UNKNOWN), 1) + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_KINETIC_COMPRESSOR) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_KINETIC_COMPRESSOR;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }

        pressureStatText.add("\u00a77Currently producing:");
        pressureStatText.add("\u00a70" + te.lastEnergyProduction + " mL/tick.");
        return pressureStatText;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(!te.redstoneAllows()) {
            textList.add("\u00a77Redstone prevents production");
            if(te.redstoneMode == 1) {
                textList.add("\u00a70Apply a redstone signal");
            } else {
                textList.add("\u00a70Remove the redstone signal");
            }
        } else if(te.outputTimer <= 0) {
            textList.add(EnumChatFormatting.GRAY + "There is no MJ input!");
            textList.add(EnumChatFormatting.BLACK + "Add a (bigger) MJ supply to the network.");
        }

        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            textList.add("\u00a70Machine generating compressed air.");

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
                redstoneBehaviourStat.closeWindow();
                break;
        }
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }
}
