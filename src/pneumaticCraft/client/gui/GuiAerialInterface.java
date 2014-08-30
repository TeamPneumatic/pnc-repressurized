package pneumaticCraft.client.gui;

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

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityAerialInterface;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAerialInterface extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_4UPGRADE_SLOTS);
    private final TileEntityAerialInterface te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiButton redstoneButton;

    public GuiAerialInterface(InventoryPlayer player, TileEntityAerialInterface te){

        super(new Container4UpgradeSlots(player, te));
        ySize = 176;
        this.te = te;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        pressureStat = this.addAnimatedStat("Pressure", new ItemStack(Blockss.pressureTube), 0xFF00AA00, false);
        problemStat = addAnimatedStat("Problems", Textures.GUI_PROBLEMS_TEXTURE, 0xFFFF0000, false);
        redstoneBehaviourStat = addAnimatedStat("Redstone Behaviour", new ItemStack(Items.redstone), 0xFFCC0000, true);
        redstoneBehaviourStat.setText(getRedstoneBehaviour());
        addAnimatedStat("Information", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText(GuiConstants.INFO_AERIAL_INTERFACE);
        addAnimatedStat("Upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText(GuiConstants.UPGRADES_AERIAL_INTERFACE);

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
                redstoneButton.displayString = "Never";
                break;
            case 1:
                redstoneButton.displayString = "Player Connected";
                break;
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

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE, te.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Emit redstone when     "); // the spaces are there
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
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_AERIAL_INTERFACE) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_AERIAL_INTERFACE;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }
        if(te.getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && te.isConnectedToPlayer) {
            pressureStatText.add(EnumChatFormatting.GRAY + "Usage:");
            pressureStatText.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1) + " mL/tick.");
        }

        return pressureStatText;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(te.playerName.equals("")) {
            textList.add("\u00a77There isn't a player set!");
            textList.add(EnumChatFormatting.BLACK + "Replace the machine.");
        } else if(!te.isConnectedToPlayer) {
            textList.add(EnumChatFormatting.GRAY + te.playerName + " can not be found on the server!");
            textList.add(EnumChatFormatting.BLACK + "Insists he/she comes back.");
        }

        if(te.getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE) {
            textList.add(EnumChatFormatting.GRAY + "Not enough pressure!");
            textList.add(EnumChatFormatting.BLACK + "Apply more pressure.");
        }

        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            textList.add("\u00a70Machine linked with " + te.playerName);

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
