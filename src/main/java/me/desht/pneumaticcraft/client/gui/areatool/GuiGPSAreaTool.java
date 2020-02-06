package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class GuiGPSAreaTool extends GuiGPSTool {

    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private static final int P1P2_BUTTON_WIDTH = 30;

    private BlockPos[] p1p2Pos = new BlockPos[2];
    private String[] vars = new String[2];
    private int index;

    private GuiGPSAreaTool(ItemStack stack, Hand hand, int index) {
        super(stack.getDisplayName(), hand,
                ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().world, stack, index),
                ItemGPSAreaTool.getVariable(stack, index));

        this.index = index;
        for (int i = 0; i <= 1; i++) {
            p1p2Pos[i] = ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().world, stack, i);
            vars[i] = ItemGPSAreaTool.getVariable(stack, i);
        }
    }

    public static void showGUI(ItemStack stack, Hand hand, int index) {
        Minecraft.getInstance().displayGuiScreen(new GuiGPSAreaTool(stack, hand, index));
    }

    @Override
    public void init() {
        super.init();

        int xMiddle = width / 2;
        int yMiddle = height / 2;

        int x = xMiddle - CHANGE_AREA_BUTTON_WIDTH / 2;
        int y = yMiddle + 100;
        addButton(new Button(x, y, CHANGE_AREA_BUTTON_WIDTH, 20, I18n.format("gui.gps_area_tool.changeAreaType"), b -> {
            ItemStack stack = minecraft.player.getHeldItem(hand);
            ProgWidgetArea area = ItemGPSAreaTool.getArea(stack);
            minecraft.displayGuiScreen(new GuiProgWidgetAreaTool(area, hand, () -> minecraft.displayGuiScreen(new GuiGPSAreaTool(stack, hand, index))));
        }));

        addButton(new Button(xMiddle - P1P2_BUTTON_WIDTH / 2, yMiddle - 45, P1P2_BUTTON_WIDTH, 20, getToggleLabel(),
                this::toggle));
    }

    @Override
    protected int getIndex() {
        return index;
    }

    @Override
    protected void syncToServer() {
        p1p2Pos[index] = new BlockPos(textFields[0].getValue(), textFields[1].getValue(), textFields[2].getValue());
        vars[index] = variableField.getText();
        for (int i = 0; i <= 1; i++) {
            if (changed(i)) NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(p1p2Pos[i], hand, vars[i], i));
        }
    }

    private boolean changed(int index) {
        ItemStack stack = minecraft.player.getHeldItem(hand);
        BlockPos p = ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().world, stack, index);
        String var = ItemGPSAreaTool.getVariable(stack, index);
        return !p.equals(p1p2Pos[index]) || !var.equals(vars[index]);
    }

    private void toggle(Button b) {
        ItemStack stack = Minecraft.getInstance().player.getHeldItem(hand);
        if (stack.getItem() instanceof ItemGPSAreaTool) {
            p1p2Pos[index] = new BlockPos(textFields[0].getValue(), textFields[1].getValue(), textFields[2].getValue());
            vars[index] = variableField.getText();

            index = 1 - index;

            b.setMessage(getToggleLabel());
            textFields[0].setValue(p1p2Pos[index].getX());
            textFields[1].setValue(p1p2Pos[index].getY());
            textFields[2].setValue(p1p2Pos[index].getZ());
            variableField.setText(vars[index]);
        }
    }

    private String getToggleLabel() {
        String color = index == 0 ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
        return color + "P" + (index + 1);
    }
}
