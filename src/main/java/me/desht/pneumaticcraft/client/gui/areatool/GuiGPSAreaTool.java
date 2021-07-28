package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiGPSAreaTool extends GuiGPSTool {

    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private static final int P1P2_BUTTON_WIDTH = 30;

    private final BlockPos[] p1p2Pos = new BlockPos[2];
    private final String[] vars = new String[2];
    private final boolean[] playerGlobals = new boolean[2];
    private int index;

    private GuiGPSAreaTool(ItemStack stack, Hand hand, int index) {
        super(stack.getDisplayName(), hand,
                ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().player, stack, index).orElse(BlockPos.ZERO),
                ItemGPSAreaTool.getVariable(Minecraft.getInstance().player, stack, index));

        this.index = index;
        for (int i = 0; i <= 1; i++) {
            p1p2Pos[i] = ItemGPSAreaTool.getGPSLocation(Minecraft.getInstance().player, stack, i).orElse(BlockPos.ZERO);
            vars[i] = ItemGPSAreaTool.getVariable(Minecraft.getInstance().player, stack, i);
            playerGlobals[i] = !vars[i].startsWith("%");
            vars[i] = GlobalVariableHelper.stripVarPrefix(vars[i]);
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
        addButton(new Button(x, y, CHANGE_AREA_BUTTON_WIDTH, 20, xlate("pneumaticcraft.gui.gps_area_tool.changeAreaType"), b -> {
            ItemStack stack = minecraft.player.getHeldItem(hand);
            ProgWidgetArea area = ItemGPSAreaTool.getArea(minecraft.player, stack);
            minecraft.displayGuiScreen(new GuiProgWidgetAreaTool(area, hand, () -> minecraft.displayGuiScreen(new GuiGPSAreaTool(stack, hand, index))));
        }));

        addButton(new Button(xMiddle - P1P2_BUTTON_WIDTH / 2, yMiddle - 45, P1P2_BUTTON_WIDTH, 20, getToggleLabel(),
                this::toggleP1P2));
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
            if (changed(i)) {
                String varName = GlobalVariableHelper.getPrefixedVar(vars[i], playerGlobals[i]);
                NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(p1p2Pos[i], hand, varName, i));
            }
        }
    }

    private boolean changed(int index) {
        ItemStack stack = minecraft.player.getHeldItem(hand);
        BlockPos p = ItemGPSAreaTool.getGPSLocation(minecraft.player, stack, index).orElse(PneumaticCraftUtils.invalidPos());
        String var = ItemGPSAreaTool.getVariable(ClientUtils.getClientPlayer(), stack, index);
        String var2 = GlobalVariableHelper.getPrefixedVar(vars[index], playerGlobals[index]);
        return !p.equals(p1p2Pos[index]) || !var.equals(var2);
    }

    @Override
    protected void toggleVarType() {
        playerGlobals[index] = !playerGlobals[index];
        varTypeButton.setMessage(new StringTextComponent(GlobalVariableHelper.getVarPrefix(playerGlobals[index])));
    }

    private void toggleP1P2(Button b) {
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
            varTypeButton.setMessage(new StringTextComponent(GlobalVariableHelper.getVarPrefix(playerGlobals[index])));
        }
    }

    private ITextComponent getToggleLabel() {
        TextFormatting color = index == 0 ? TextFormatting.RED : TextFormatting.GREEN;
        return new StringTextComponent("P" + (index + 1)).mergeStyle(color);
    }
}
