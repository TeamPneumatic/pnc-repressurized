package me.desht.pneumaticcraft.client.gui;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationInventory;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationAddUser;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSecurityStationInventory extends GuiSecurityStationBase {
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat accessStat;

    private GuiButtonSpecial addButton;
    private GuiButton rebootButton;
    private WidgetTextField sharedUserTextField;
    private List<GuiButtonSpecial> removeUserButtons;
    private NetworkConnectionHandler nodeHandler;

    public GuiSecurityStationInventory(InventoryPlayer player, TileEntitySecurityStation te) {

        super(new ContainerSecurityStationInventory(player, te), te, Textures.GUI_SECURITY_STATION);
        ySize = 239;
    }

    @Override
    public void initGui() {
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat("Security Status", new ItemStack(Blockss.SECURITY_STATION), 0xFFFFAA00, false);
        accessStat = addAnimatedStat("Shared Users", new ItemStack(Items.SKULL, 1, 3), 0xFF005500, false);

        Rectangle accessButtonRectangle = accessStat.getButtonScaledRectangle(145, 10, 20, 20);
        addButton = getButtonFromRectangle(1, accessButtonRectangle, "+");
        rebootButton = new GuiButton(2, xStart + 110, yStart + 20, 60, 20, "Reboot");
        sharedUserTextField = getTextFieldFromRectangle(accessStat.getButtonScaledRectangle(20, 15, 120, 10));
        accessStat.addWidget(sharedUserTextField);
        accessStat.addWidget(addButton);

        buttonList.add(new GuiButton(3, guiLeft + 108, guiTop + 103, 64, 20, I18n.format("gui.securityStation.test")));
        buttonList.add(rebootButton);
        buttonList.add(new GuiButton(-1, guiLeft + 108, guiTop + 125, 64, 20, I18n.format("gui.universalSensor.button.showRange")));

        updateUserRemoveButtons();

        nodeHandler = new NetworkConnectionBackground(this, te, xStart + 25, yStart + 30, 18, 0xFF2222FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Network Layout", 15, 12, 4210752);
        fontRenderer.drawString("Upgr.", 133, 52, 4210752);
    }

    @Override
    protected Point getInvTextOffset() {
        return new Point(0, 2);
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -2);
    }

    @Override
    public String getRedstoneButtonText(int mode) {
        switch (mode) {
            case 0:
                return "gui.tab.redstoneBehaviour.button.never";
            case 1:
                return "gui.tab.redstoneBehaviour.securityStation.button.hacked";
            case 2:
                return "gui.tab.redstoneBehaviour.securityStation.button.doneRebooting";
        }
        return "<ERROR>";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        nodeHandler.render();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatusText());
        accessStat.setTextWithoutCuttingString(getAccessText());
        String rebootButtonString;
        if (te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 50 ? "Rebooting.." : PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false);
        } else {
            rebootButtonString = "Reboot";
        }

        rebootButton.displayString = rebootButtonString;

        addButton.visible = accessStat.isDoneExpanding();
        for (GuiButton button : removeUserButtons) {
            button.enabled = accessStat.isDoneExpanding();
        }
        if (removeUserButtons.size() != te.sharedUsers.size()) {
            updateUserRemoveButtons();
        }
    }

    @Override
    protected void addProblems(List<String> text) {
        super.addProblems(text);
        if (te.getRebootTime() > 0) {
            text.add(TextFormatting.GRAY + "The Security Station doesn't provide security!");
            text.add(TextFormatting.BLACK + "The station is rebooting (" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false) + ").");
        } else if (te.isHacked()) {
            text.add(TextFormatting.GRAY + "This Station has been hacked!");
            text.add(TextFormatting.BLACK + "Reboot the station.");
        }
        if (!te.hasValidNetwork()) {
            text.add(TextFormatting.GRAY + "Invalid network configuration!");
            switch (te.checkForNetworkValidity()) {
                case NO_SUBROUTINE:
                    text.add(TextFormatting.BLACK + "Add a Diagnostic Subroutine.");
                    break;
                case NO_IO_PORT:
                    text.add(TextFormatting.BLACK + "Add a Network IO Port.");
                    break;
                case NO_REGISTRY:
                    text.add(TextFormatting.BLACK + "Add a Network Registry.");
                    break;
                case TOO_MANY_SUBROUTINES:
                    text.add(TextFormatting.BLACK + "There can only be one Diagnostic Subroutine.");
                    break;
                case TOO_MANY_IO_PORTS:
                    text.add(TextFormatting.BLACK + "There can only be one Network IO Port.");
                    break;
                case TOO_MANY_REGISTRIES:
                    text.add(TextFormatting.BLACK + "There can only be one Network Registry.");
                    break;
                case NO_CONNECTION_SUB_AND_IO_PORT:
                    text.add(TextFormatting.BLACK + "The Diagnostic Subroutine and the Network IO Port need to be connected in the network.");
                    break;
                case NO_CONNECTION_IO_PORT_AND_REGISTRY:
                    text.add(TextFormatting.BLACK + "The Network Registry and the Network IO Port need to be connected in the network.");
                    break;
            }
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.GRAY + "Protection");
        if (te.getRebootTime() > 0) {
            text.add(TextFormatting.DARK_RED + "No protection because of rebooting!");
        } else if (te.isHacked()) {
            text.add(TextFormatting.DARK_RED + "Hacked by:");
            for (GameProfile hacker : te.hackedUsers) {
                text.add(TextFormatting.DARK_RED + "-" + hacker.getName());
            }
        } else {
            text.add(TextFormatting.BLACK + "System secure");
        }
        text.add(TextFormatting.GRAY + "Security Level");
        text.add(TextFormatting.BLACK + "Level " + te.getSecurityLevel());
        text.add(TextFormatting.GRAY + "Intruder Detection Chance");
        text.add(TextFormatting.BLACK.toString() + te.getDetectionChance() + "%%");
        text.add(TextFormatting.GRAY + "Security Range");
        text.add(TextFormatting.BLACK.toString() + te.getSecurityRange() + "m (square)");
        return text;
    }

    private List<String> getAccessText() {
        List<String> textList = new ArrayList<String>();
        textList.add("                                      ");
        textList.add("");
        for (GameProfile user : te.sharedUsers) {
            textList.add(TextFormatting.BLACK + "-" + user.getName());
        }
        return textList;
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget.getID() == 1 && !sharedUserTextField.getText().equals(""))
            NetworkHandler.sendToServer(new PacketSecurityStationAddUser(te, sharedUserTextField.getText()));
        super.actionPerformed(widget);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            te.rebootStation();
        } else if (button.id == -1) {
            te.showRangeLines();
        }

        super.actionPerformed(button);
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        te.setText(0, sharedUserTextField.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
    }

    private void updateUserRemoveButtons() {
        if (removeUserButtons != null) {
            for (GuiButtonSpecial button : removeUserButtons) {
                accessStat.removeWidget(button);
            }
        }
        removeUserButtons = new ArrayList<GuiButtonSpecial>();
        for (int i = 0; i < te.sharedUsers.size(); i++) {
            Rectangle rect = accessStat.getButtonScaledRectangle(20, 32 + i * 10, fontRenderer.getStringWidth("-" + te.sharedUsers.get(i).getName()), 8);
            GuiButtonSpecial button = getInvisibleButtonFromRectangle(4 + i, rect);
            button.setInvisibleHoverColor(0x44FF0000);
            button.setVisible(false);
            accessStat.addWidget(button);
            removeUserButtons.add(button);
            if (te.sharedUsers.get(i).getName().equals(FMLClientHandler.instance().getClient().player.getGameProfile().getName())) {
                button.visible = false;
            }
        }
    }
}
