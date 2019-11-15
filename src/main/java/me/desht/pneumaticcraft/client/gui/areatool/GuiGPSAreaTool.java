package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class GuiGPSAreaTool extends GuiGPSTool {

    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private final int index;
    
    private GuiGPSAreaTool(Hand hand, BlockPos gpsLoc, String oldVarName, int metadata) {
        super(hand, gpsLoc != null ? gpsLoc : BlockPos.ZERO, oldVarName, metadata);
        this.index = metadata;
    }
    
    public GuiGPSAreaTool(Hand hand, ItemStack stack, int index) {
        this(hand, ItemGPSAreaTool.getGPSLocation(stack, index), ItemGPSAreaTool.getVariable(stack, index), index);
    }

    public static void showGUI(Hand hand, ItemStack stack, int index) {
        Minecraft.getInstance().displayGuiScreen(new GuiGPSAreaTool(hand, stack, index));
    }

    @Override
    public void init() {
        super.init();
        
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        
        int x = xMiddle - CHANGE_AREA_BUTTON_WIDTH / 2;
        int y = yMiddle + 100;
        addButton(new Button(x, y, CHANGE_AREA_BUTTON_WIDTH, 20, "Change area type", b -> {
            ItemStack stack = minecraft.player.getHeldItemMainhand();
            ProgWidgetArea area = ItemGPSAreaTool.getArea(stack);
            Runnable returnAction = () -> minecraft.displayGuiScreen(new GuiGPSAreaTool(hand, stack, index));
            minecraft.displayGuiScreen(new GuiProgWidgetAreaTool(area, returnAction));
        }));
    }
}
