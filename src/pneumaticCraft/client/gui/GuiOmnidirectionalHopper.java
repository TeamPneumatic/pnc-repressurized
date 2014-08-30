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

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerOmnidirectionalHopper;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOmnidirectionalHopper extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_OMNIDIRECTIONAL_HOPPER);
    private final TileEntityOmnidirectionalHopper te;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;

    public GuiOmnidirectionalHopper(InventoryPlayer player, TileEntityOmnidirectionalHopper te){

        super(new ContainerOmnidirectionalHopper(player, te));
        ySize = 176;
        this.te = te;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = new GuiAnimatedStat(this, "Hopper Status", new ItemStack(Blockss.omnidirectionalHopper), xStart + xSize, yStart + 5, 0xFFFFAA00, null, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(statusStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setText(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_OMNIDIRECTIONAL_HOPPER);
        upgradeStat.setText(GuiConstants.UPGRADES_OMNIDIRECTIONAL_HOPPER);

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 118, yStart + 30, 117, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);

        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Never";
                break;
            case 1:
                redstoneButton.displayString = "Redstone applied";
                break;
            case 2:
                redstoneButton.displayString = "Redstone not applied";
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

        statusStat.setText(getStatus());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Stop item transfer when"); // the spaces are there
                                                        // to create space for
                                                        // the button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getStatus(){
        List<String> textList = new ArrayList<String>();
        textList.add(EnumChatFormatting.GRAY + "Item transfer speed:");
        int itemsPer = te.getMaxItems();
        if(itemsPer > 1) {
            textList.add(EnumChatFormatting.BLACK.toString() + itemsPer + " items/tick");
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(EnumChatFormatting.BLACK.toString() + (transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1)) + " items/s");
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
