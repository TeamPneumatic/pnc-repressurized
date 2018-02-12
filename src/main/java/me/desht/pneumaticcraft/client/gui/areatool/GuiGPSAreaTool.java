package me.desht.pneumaticcraft.client.gui.areatool;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.capabilities.CapabilityGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class GuiGPSAreaTool extends GuiGPSTool{

    private static final int CHANGE_AREA_BUTTON_ID = 23434;
    private static final int CHANGE_AREA_BUTTON_WIDTH = 4 * 22 + 40 + 5 * 2;
    private final int index;
    
    private GuiGPSAreaTool(BlockPos gpsLoc, String oldVarName, int metadata){
        super(gpsLoc != null ? gpsLoc : new BlockPos(0, 0, 0), oldVarName, metadata);
        this.index = metadata;
    }
    
    public GuiGPSAreaTool(ItemStack stack, int index){
        this(ItemGPSAreaTool.getGPSLocation(stack, index), ItemGPSAreaTool.getVariable(stack, index), index);        
    }

    @Override
    public void initGui(){
        super.initGui();
        
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        
        int x = xMiddle - CHANGE_AREA_BUTTON_WIDTH / 2;
        int y = yMiddle + 100;
        buttonList.add(new GuiButton(CHANGE_AREA_BUTTON_ID, x, y, CHANGE_AREA_BUTTON_WIDTH, 20, "Change area type"));
        
    }
    
    @Override
    protected void actionPerformed(GuiButton button){
        if(button.id == CHANGE_AREA_BUTTON_ID){
            ItemStack stack = mc.player.getHeldItemMainhand();
            CapabilityGPSAreaTool cap = ItemGPSAreaTool.getCap(stack);
            if(cap != null){
                Runnable returnAction = () -> mc.displayGuiScreen(new GuiGPSAreaTool(stack, index));
                mc.displayGuiScreen(new GuiProgWidgetAreaTool(cap.getWidget(), returnAction));
            }
        }else{
            super.actionPerformed(button);
        }
    }
}
