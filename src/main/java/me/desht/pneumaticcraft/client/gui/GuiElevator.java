package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerElevator;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiElevator extends GuiPneumaticContainerBase<TileEntityElevatorBase> {
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat floorNameStat;
    private int currentEditedFloor;
    private WidgetTextField floorNameField;

    public GuiElevator(InventoryPlayer player, TileEntityElevatorBase te) {
        super(new ContainerElevator(player, te), te, Textures.GUI_ELEVATOR);
    }

    @Override
    public void initGui() {
        super.initGui();
        statusStat = addAnimatedStat("Elevator Status", new ItemStack(Blockss.ELEVATOR_BASE), 0xFFFFAA00, false);
        floorNameStat = addAnimatedStat("Floor Names", new ItemStack(Blockss.ELEVATOR_CALLER), 0xFF005500, false);
        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());

        Rectangle fieldRectangle = floorNameStat.getButtonScaledRectangle(6, 60, 160, 20);
        floorNameField = getTextFieldFromRectangle(fieldRectangle);
        floorNameField.setText(te.getFloorName(currentEditedFloor));
        floorNameStat.addWidget(floorNameField);

        Rectangle namePreviousRectangle = floorNameStat.getButtonScaledRectangle(5, 35, 20, 20);
        floorNameStat.addWidget(getButtonFromRectangle(1, namePreviousRectangle, "\u27f5"));

        Rectangle nameNextRectangle = floorNameStat.getButtonScaledRectangle(145, 35, 20, 20);
        floorNameStat.addWidget(getButtonFromRectangle(2, nameNextRectangle, "\u27f6"));

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    private List<String> getFloorNameStat() {
        List<String> textList = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            textList.add("");
        textList.add(TextFormatting.GRAY + "         Floor " + (currentEditedFloor + 1) + "                   ");
        for (int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        text.add(TextFormatting.GRAY + "Current Extension:");
        text.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.extension, 1) + " meter");
        text.add(TextFormatting.GRAY + "Max Extension:");
        text.add(TextFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.getMaxElevatorHeight(), 1) + " meter");
        return text;
    }

    @Override
    protected void addWarnings(List<String> textList) {
        super.addWarnings(textList);
        if (te.getMaxElevatorHeight() == te.extension) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "The elevator can't extend anymore.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.BLACK + "Add (more) Elevator Frames on top of the elevator", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        super.actionPerformed(widget);

        if (widget.getID() == 1 || widget.getID() == 2) {
            int[] floorHeights = te.floorHeights;

            if (widget.getID() == 1) {
                currentEditedFloor--;
                if (currentEditedFloor < 0) {
                    currentEditedFloor = floorHeights.length - 1;
                    if (floorHeights.length == 0) currentEditedFloor = 0;
                }
            } else {
                currentEditedFloor++;
                if (currentEditedFloor >= floorHeights.length) {
                    currentEditedFloor = 0;
                }
            }
            floorNameField.setText(te.getFloorName(currentEditedFloor));
            floorNameStat.setTextWithoutCuttingString(getFloorNameStat());
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        te.setFloorName(currentEditedFloor, floorNameField.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, currentEditedFloor));
    }
}
