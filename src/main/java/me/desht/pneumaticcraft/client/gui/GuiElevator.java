package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerElevator;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.lib.GuiConstants.ARROW_LEFT;
import static me.desht.pneumaticcraft.lib.GuiConstants.ARROW_RIGHT;

public class GuiElevator extends GuiPneumaticContainerBase<ContainerElevator, TileEntityElevatorBase> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat floorNameStat;
    private WidgetTextField floorNameField;
    private int currentEditedFloor;

    public GuiElevator(ContainerElevator container, PlayerInventory inventoryPlayer, ITextComponent displayName) {
        super(container, inventoryPlayer, displayName);
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat("Elevator Status", new ItemStack(ModBlocks.ELEVATOR_BASE.get()), 0xFFFFAA00, false);

        floorNameStat = addAnimatedStat("Floor Names", new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), 0xFF005500, false);
        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());

        Rectangle2d fieldRectangle = floorNameStat.getButtonScaledRectangle(6, 60, 160, 20);
        floorNameField = getTextFieldFromRectangle(fieldRectangle);
        floorNameField.setText(te.getFloorName(currentEditedFloor));
        floorNameField.setResponder(this::updateFloor);  // gui responder
        floorNameStat.addSubWidget(floorNameField);

        Rectangle2d prev = floorNameStat.getButtonScaledRectangle(5, 35, 20, 20);
        floorNameStat.addSubWidget(getButtonFromRectangle("", prev, ARROW_LEFT, button -> cycleFloor(-1)));

        Rectangle2d next = floorNameStat.getButtonScaledRectangle(145, 35, 20, 20);
        floorNameStat.addSubWidget(getButtonFromRectangle("", next, ARROW_RIGHT, button -> cycleFloor(1)));
    }

    private void cycleFloor(int dir) {
        currentEditedFloor += dir;
        if (currentEditedFloor >= te.floorHeights.length) currentEditedFloor = 0;
        else if (currentEditedFloor < 0) currentEditedFloor = te.floorHeights.length - 1;

        floorNameField.setText(te.getFloorName(currentEditedFloor));
        floorNameField.setFocused2(true);

        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());
    }

    private void updateFloor(String floorName) {
        sendDelayed(5);
        te.setFloorName(currentEditedFloor, floorName);
    }

    @Override
    protected void doDelayedAction() {
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, currentEditedFloor));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ELEVATOR;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
    }

    @Override
    public void tick() {
        super.tick();
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
}
