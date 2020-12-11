package me.desht.pneumaticcraft.client.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationMain;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationAddUser;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiSecurityStationInventory extends GuiSecurityStationBase<ContainerSecurityStationMain> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat accessStat;

    private WidgetButtonExtended addUserButton;
    private Button rebootButton;
    private WidgetTextField sharedUserTextField;
    private List<WidgetButtonExtended> removeUserButtons;
    private NetworkConnectionHandler nodeHandler;

    public GuiSecurityStationInventory(ContainerSecurityStationMain container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 239;
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat(new StringTextComponent("Security Status"), new ItemStack(ModBlocks.SECURITY_STATION.get()), 0xFFFFAA00, false);
        accessStat = addAnimatedStat(new StringTextComponent("Shared Users"), new ItemStack(Items.PLAYER_HEAD), 0xFF005500, false);

        Rectangle2d accessButtonRectangle = new Rectangle2d(145, 10, 20, 20);
        addUserButton = getButtonFromRectangle(null, accessButtonRectangle, "+", b -> {
            if (!sharedUserTextField.getText().equals(""))
                NetworkHandler.sendToServer(new PacketSecurityStationAddUser(te, sharedUserTextField.getText()));
        });

        rebootButton = new WidgetButtonExtended(xStart + 110, yStart + 20, 60, 20, "Reboot").withTag("reboot");
        sharedUserTextField = getTextFieldFromRectangle(new Rectangle2d(20, 15, 120, 10));
        sharedUserTextField.setResponder(s -> {
            te.setText(0, sharedUserTextField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        });
        accessStat.addSubWidget(sharedUserTextField);
        accessStat.addSubWidget(addUserButton);
        accessStat.setMinimumExpandedDimensions(150, 40);

        addButton(new WidgetButtonExtended(guiLeft + 108, guiTop + 103, 64, 20, xlate("pneumaticcraft.gui.securityStation.test")))
                .withTag("test");
        addButton(rebootButton);
        addButton(new WidgetRangeToggleButton(guiLeft + 154, guiTop + 130, te));

        updateUserRemoveButtons();

        nodeHandler = new NetworkConnectionBackground(this, te, xStart + 25, yStart + 30, 18, 0xFF2222FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        font.drawString(matrixStack, "Network Layout", 15, 12, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SECURITY_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, opacity, x, y);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        nodeHandler.render(matrixStack);
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatusText());
        accessStat.setText(getAccessText());
        String rebootButtonString;
        if (te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 50 ? "Rebooting.." : PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false);
        } else {
            rebootButtonString = "Reboot";
        }

        rebootButton.setMessage(new StringTextComponent(rebootButtonString));

        addUserButton.visible = accessStat.isDoneExpanding();
        for (Button button : removeUserButtons) {
            button.active = accessStat.isDoneExpanding();
        }
        if (removeUserButtons.size() != te.sharedUsers.size()) {
            updateUserRemoveButtons();
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> text) {
        super.addProblems(text);
        if (te.getRebootTime() > 0) {
            text.add(new StringTextComponent("The Security Station doesn't provide security!").mergeStyle(TextFormatting.GRAY));
            text.add(new StringTextComponent("The station is rebooting (" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false) + ").").mergeStyle(TextFormatting.BLACK));
        } else if (te.isHacked()) {
            text.add(new StringTextComponent("This Station has been hacked!").mergeStyle(TextFormatting.GRAY));
            text.add(new StringTextComponent("Reboot the station.").mergeStyle(TextFormatting.BLACK));
        }
        if (!te.hasValidNetwork()) {
            text.add(new StringTextComponent("Invalid network configuration!").mergeStyle(TextFormatting.GRAY));
            switch (te.checkForNetworkValidity()) {
                case NO_SUBROUTINE:
                    text.add(new StringTextComponent("Add a Diagnostic Subroutine.").mergeStyle(TextFormatting.BLACK));
                    break;
                case NO_IO_PORT:
                    text.add(new StringTextComponent("Add a Network IO Port.").mergeStyle(TextFormatting.BLACK));
                    break;
                case NO_REGISTRY:
                    text.add(new StringTextComponent("Add a Network Registry.").mergeStyle(TextFormatting.BLACK));
                    break;
                case TOO_MANY_SUBROUTINES:
                    text.add(new StringTextComponent("There can only be one Diagnostic Subroutine.").mergeStyle(TextFormatting.BLACK));
                    break;
                case TOO_MANY_IO_PORTS:
                    text.add(new StringTextComponent("There can only be one Network IO Port.").mergeStyle(TextFormatting.BLACK));
                    break;
                case TOO_MANY_REGISTRIES:
                    text.add(new StringTextComponent("There can only be one Network Registry.").mergeStyle(TextFormatting.BLACK));
                    break;
                case NO_CONNECTION_SUB_AND_IO_PORT:
                    text.add(new StringTextComponent("The Diagnostic Subroutine and the Network IO Port need to be connected in the network.").mergeStyle(TextFormatting.BLACK));
                    break;
                case NO_CONNECTION_IO_PORT_AND_REGISTRY:
                    text.add(new StringTextComponent("The Network Registry and the Network IO Port need to be connected in the network.").mergeStyle(TextFormatting.BLACK));
                    break;
            }
        }
    }

    private List<ITextComponent> getStatusText() {
        List<ITextComponent> text = new ArrayList<>();
        text.add(new StringTextComponent("Protection").mergeStyle(TextFormatting.GRAY));
        if (te.getRebootTime() > 0) {
            text.add(new StringTextComponent("No protection because of rebooting!").mergeStyle(TextFormatting.DARK_RED));
        } else if (te.isHacked()) {
            text.add(new StringTextComponent("Hacked by:").mergeStyle(TextFormatting.DARK_RED));
            for (GameProfile hacker : te.hackedUsers) {
                text.add(GuiConstants.bullet().appendString(hacker.getName()));
            }
        } else {
            text.add(new StringTextComponent("System secure").mergeStyle(TextFormatting.BLACK));
        }
        text.add(new StringTextComponent("Security Level").mergeStyle(TextFormatting.GRAY));
        text.add(new StringTextComponent("Level " + te.getSecurityLevel()).mergeStyle(TextFormatting.BLACK));
        text.add(new StringTextComponent("Intruder Detection Chance").mergeStyle(TextFormatting.BLACK));
        text.add(new StringTextComponent(te.getDetectionChance() + "%%").mergeStyle(TextFormatting.BLACK));
        text.add(new StringTextComponent("Security Range").mergeStyle(TextFormatting.BLACK));
        text.add(new StringTextComponent(te.getRange() + "m (square)").mergeStyle(TextFormatting.BLACK));
        return text;
    }

    private List<ITextComponent> getAccessText() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(StringTextComponent.EMPTY);
        textList.add(StringTextComponent.EMPTY);
        for (GameProfile user : te.sharedUsers) {
            textList.add(GuiConstants.bullet().appendString(user.getName()));
        }
        return textList;
    }

    private void updateUserRemoveButtons() {
        if (removeUserButtons != null) {
            for (WidgetButtonExtended button : removeUserButtons) {
                accessStat.removeSubWidget(button);
            }
        }
        removeUserButtons = new ArrayList<>();
        for (int i = 0; i < te.sharedUsers.size(); i++) {
            Rectangle2d rect = new Rectangle2d(24, 30 + i * 10, font.getStringWidth(te.sharedUsers.get(i).getName()), 8);
            WidgetButtonExtended button = getInvisibleButtonFromRectangle("remove:" + i, rect, b -> {});
            button.setInvisibleHoverColor(0x44FF0000);
            button.setVisible(false);
            accessStat.addSubWidget(button);
            removeUserButtons.add(button);
            if (te.sharedUsers.get(i).getName().equals(minecraft.player.getGameProfile().getName())) {
                button.visible = false;
            }
        }
    }
}
