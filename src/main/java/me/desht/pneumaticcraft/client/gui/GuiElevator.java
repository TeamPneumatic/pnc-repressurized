package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerElevator;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.GuiConstants.ARROW_LEFT;
import static me.desht.pneumaticcraft.lib.GuiConstants.ARROW_RIGHT;

public class GuiElevator extends GuiPneumaticContainerBase<ContainerElevator, TileEntityElevatorBase> {
    private WidgetAnimatedStat statusStat;
    private WidgetTextField floorNameField;
    private WidgetLabel noFloorsLabel, floorNumberLabel;
    private WidgetButtonExtended cycleDown, cycleUp;
    private int currentEditedFloor;

    public GuiElevator(ContainerElevator container, PlayerInventory inventoryPlayer, ITextComponent displayName) {
        super(container, inventoryPlayer, displayName);
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.ELEVATOR_BASE.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat floorNameStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.elevator.floorNames"),
                new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), 0xFF005500, false);
        floorNameStat.addPadding(7, 40);

        floorNameField = new WidgetTextField(font,6, 60, 160, 20);
        floorNameField.setText(te.getFloorName(currentEditedFloor));
        floorNameField.setResponder(this::updateFloor);  // gui responder

        floorNameStat.addSubWidget(floorNameField);
        floorNameStat.addSubWidget(noFloorsLabel = new WidgetLabel(5, 20, xlate("pneumaticcraft.gui.tab.info.elevator.noCallers")).setColor(0xFFFFFFFF));
        floorNameStat.addSubWidget(floorNumberLabel = new WidgetLabel(85, 40, StringTextComponent.EMPTY)
                .setAlignment(WidgetLabel.Alignment.CENTRE).setColor(0xFFFFFFFF));
        floorNameStat.addSubWidget(cycleDown = new WidgetButtonExtended(5, 35, 20, 20, ARROW_LEFT, button -> cycleFloor(-1)));
        floorNameStat.addSubWidget(cycleUp = new WidgetButtonExtended(145, 35, 20, 20, ARROW_RIGHT, button -> cycleFloor(1)));
    }

    private void cycleFloor(int dir) {
        if (te.floorHeights.length > 0) {
            currentEditedFloor += dir;
            if (currentEditedFloor >= te.floorHeights.length) currentEditedFloor = 0;
            else if (currentEditedFloor < 0) currentEditedFloor = te.floorHeights.length - 1;

            floorNameField.setText(te.getFloorName(currentEditedFloor));
            floorNameField.setFocused2(true);
        }
    }

    private void updateFloor(String floorName) {
        te.setFloorName(currentEditedFloor, floorName);
        sendDelayed(5);
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
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, opacity, x, y);
    }

    @Override
    public void tick() {
        super.tick();

        statusStat.setText(getStatusText());
        cycleDown.active = cycleUp.active = te.floorHeights.length > 0;
        noFloorsLabel.visible = te.floorHeights.length == 0;
        floorNumberLabel.visible = te.floorHeights.length > 0;
        floorNameField.active = te.floorHeights.length > 0;
        floorNumberLabel.setMessage(xlate("pneumaticcraft.gui.tab.info.elevator.floorNumber", currentEditedFloor + 1, te.floorHeights.length));
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.elevator.extension",
                PneumaticCraftUtils.roundNumberTo(te.extension, 1)));
        text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.elevator.maxExtension",
                PneumaticCraftUtils.roundNumberTo(te.getMaxElevatorHeight(), 1)));

        return text;
    }

    @Override
    protected void addWarnings(List<String> textList) {
        super.addWarnings(textList);
        if (te.getMaxElevatorHeight() == te.extension) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.elevator.fully_extended"));
        }
    }
}
