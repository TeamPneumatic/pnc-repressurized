package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerSecurityStationInventory;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketSecurityStationAddUser;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSecurityStationInventory extends GuiSecurityStationBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_SECURITY_STATION);
    private final TileEntitySecurityStation te;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat accessStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;

    private GuiButton addButton;
    private GuiButton rebootButton;
    private GuiButton redstoneButton;
    private GuiTextField sharedUserTextField;
    private List<GuiButtonSpecial> removeUserButtons;
    private NetworkConnectionHandler nodeHandler;

    public GuiSecurityStationInventory(InventoryPlayer player, TileEntitySecurityStation teSecurityStation){

        super(new ContainerSecurityStationInventory(player, teSecurityStation));
        ySize = 239;
        te = teSecurityStation;
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, yStart + 5, 0xFFFF0000, null, false);
        statusStat = new GuiAnimatedStat(this, "Security Status", new ItemStack(Blockss.securityStation), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        accessStat = new GuiAnimatedStat(this, "Shared Users", new ItemStack(Items.skull, 1, 3), xStart + xSize, 3, 0xFF005500, statusStat, false);

        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);

        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        animatedStatList.add(accessStat);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_SECURITY_STATION);
        upgradeStat.setText(GuiConstants.UPGRADES_SECURITY_STATION);

        Rectangle accessButtonRectangle = accessStat.getButtonScaledRectangle(xStart + 323, yStart + 53, 20, 20);
        Rectangle redstoneButtonRectangle = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 131, yStart + 30, 130, 20);
        addButton = getButtonFromRectangle(1, accessButtonRectangle, "+");
        rebootButton = new GuiButton(2, xStart + 110, yStart + 20, 60, 20, "Reboot");
        sharedUserTextField = getTextFieldFromRectangle(accessStat.getButtonScaledRectangle(xStart + 195, yStart + 58, 120, 10));
        redstoneButton = getButtonFromRectangle(0, redstoneButtonRectangle, "-");
        buttonList.add(redstoneButton);
        buttonList.add(addButton);
        buttonList.add(rebootButton);

        updateUserRemoveButtons();

        nodeHandler = new NetworkConnectionBackground(this, te, xStart + 25, yStart + 30, 18, 0xFF2222FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 4, 4210752);
        fontRendererObj.drawString("Network Layout", 15, 12, 4210752);
        fontRendererObj.drawString("Upgr.", 133, 52, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 14, 4210752);

        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Never";
                break;
            case 1:
                redstoneButton.displayString = "Hacked";
                break;
            case 2:
                redstoneButton.displayString = "Done rebooting";
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

        problemStat.setText(getProblems());
        statusStat.setText(getStatusText());
        accessStat.setTextWithoutCuttingString(getAccessText());
        String rebootButtonString;
        if(te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 50 ? "Rebooting.." : PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false);
        } else {
            rebootButtonString = "Reboot";
        }

        rebootButton.displayString = rebootButtonString;
        if(accessStat.isDoneExpanding()) sharedUserTextField.drawTextBox();
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
        addButton.visible = accessStat.isDoneExpanding();
        for(GuiButton button : removeUserButtons) {
            button.enabled = accessStat.isDoneExpanding();
        }
        if(removeUserButtons.size() != te.sharedUsers.size()) {
            updateUserRemoveButtons();
        }
        nodeHandler.render();

    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Emit redstone if         "); // the spaces are
                                                          // there to create
                                                          // space for the
                                                          // button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getProblems(){
        List<String> text = new ArrayList<String>();
        if(te.getRebootTime() > 0) {
            text.add(EnumChatFormatting.GRAY + "The Security Station doesn't provide security!");
            text.add(EnumChatFormatting.BLACK + "The station is rebooting (" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false) + ").");
        } else if(te.isHacked()) {
            text.add(EnumChatFormatting.GRAY + "This Station has been hacked!");
            text.add(EnumChatFormatting.BLACK + "Reboot the station.");
        }
        if(!te.hasValidNetwork()) {
            text.add(EnumChatFormatting.GRAY + "Invalid network configuration!");
            switch(te.checkForNetworkValidity()){
                case NO_SUBROUTINE:
                    text.add(EnumChatFormatting.BLACK + "Add a Diagnostic Subroutine.");
                    break;
                case NO_IO_PORT:
                    text.add(EnumChatFormatting.BLACK + "Add a Network IO Port.");
                    break;
                case NO_REGISTRY:
                    text.add(EnumChatFormatting.BLACK + "Add a Network Registry.");
                    break;
                case TOO_MANY_SUBROUTINES:
                    text.add(EnumChatFormatting.BLACK + "There can only be one Diagnostic Subroutine.");
                    break;
                case TOO_MANY_IO_PORTS:
                    text.add(EnumChatFormatting.BLACK + "There can only be one Network IO Port.");
                    break;
                case TOO_MANY_REGISTRIES:
                    text.add(EnumChatFormatting.BLACK + "There can only be one Network Registry.");
                    break;
                case NO_CONNECTION_SUB_AND_IO_PORT:
                    text.add(EnumChatFormatting.BLACK + "The Diagnostic Subroutine and the Network IO Port need to be connected in the network.");
                    break;
                case NO_CONNECTION_IO_PORT_AND_REGISTRY:
                    text.add(EnumChatFormatting.BLACK + "The Network Registry and the Network IO Port need to be connected in the network.");
                    break;
            }
        }
        if(text.size() == 0) {
            text.add(EnumChatFormatting.BLACK + "There are no problems.");
        }
        return text;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.GRAY + "Protection");
        if(te.getRebootTime() > 0) {
            text.add(EnumChatFormatting.DARK_RED + "No protection because of rebooting!");
        } else if(te.isHacked()) {
            text.add(EnumChatFormatting.DARK_RED + "Hacked by:");
            for(GameProfile hacker : te.hackedUsers) {
                text.add(EnumChatFormatting.DARK_RED + "-" + hacker.getName());
            }
        } else {
            text.add(EnumChatFormatting.BLACK + "System secure");
        }
        text.add(EnumChatFormatting.GRAY + "Security Level");
        text.add(EnumChatFormatting.BLACK + "Level " + te.getSecurityLevel());
        text.add(EnumChatFormatting.GRAY + "Intruder Detection Chance");
        text.add(EnumChatFormatting.BLACK.toString() + te.getDetectionChance() + "%%");
        text.add(EnumChatFormatting.GRAY + "Security Range");
        text.add(EnumChatFormatting.BLACK.toString() + te.getSecurityRange() + "m (square)");
        return text;
    }

    private List<String> getAccessText(){
        List<String> textList = new ArrayList<String>();
        textList.add("                                      ");
        textList.add("");
        for(GameProfile user : te.sharedUsers) {
            textList.add(EnumChatFormatting.BLACK + "-" + user.getName());
        }
        return textList;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);

        if(accessStat.isDoneExpanding()) sharedUserTextField.mouseClicked(par1, par2, par3);
        if(sharedUserTextField.isFocused()) {
            accessStat.openWindow();
        }
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
            case 1:
                accessStat.closeWindow();
                NetworkHandler.sendToServer(new PacketSecurityStationAddUser(te, sharedUserTextField.getText()));
                break;
            case 2:
                te.rebootStation();
                break;
        }
        if(button.id > 2) {
            accessStat.closeWindow();
        }

        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(sharedUserTextField.isFocused() && par2 != 1) {
            sharedUserTextField.textboxKeyTyped(par1, par2);
            te.setText(0, sharedUserTextField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    private void updateUserRemoveButtons(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        if(removeUserButtons != null) {
            for(GuiButton button : removeUserButtons) {
                buttonList.remove(button);
            }
        }
        removeUserButtons = new ArrayList<GuiButtonSpecial>();
        for(int i = 0; i < te.sharedUsers.size(); i++) {
            Rectangle rect = accessStat.getButtonScaledRectangle(xStart + 202, yStart + 77 + i * 10, fontRendererObj.getStringWidth("-" + te.sharedUsers.get(i)), 8);
            GuiButtonSpecial button = getInvisibleButtonFromRectangle(3 + i, rect);
            button.setInvisibleHoverColor(0x44FF0000);
            button.setVisible(false);
            buttonList.add(button);
            removeUserButtons.add(button);
            if(te.sharedUsers.get(i).getName().equals(FMLClientHandler.instance().getClient().thePlayer.getGameProfile().getName())) {
                button.visible = false;
            }
        }
    }
}
