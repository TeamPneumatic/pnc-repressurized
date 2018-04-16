package me.desht.pneumaticcraft.common.progwidgets;

import java.util.List;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetDig;
import me.desht.pneumaticcraft.common.ai.DroneAIDig;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProgWidgetDig extends ProgWidgetDigAndPlace implements IToolUser {

    private boolean requireDiggingTool;
    
    public ProgWidgetDig() {
        super(ProgWidgetDigAndPlace.EnumOrder.CLOSEST);
    }

    @Override
    public String getWidgetString() {
        return "dig";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_DIG;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIDig(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetDig(this, guiProgrammer);
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.BROWN;
    }

    @Override
    public boolean requiresTool(){
        return requireDiggingTool;
    }
    
    @Override
    public void setRequiresTool(boolean requireDiggingTool){
        this.requireDiggingTool = requireDiggingTool;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        
        if(requiresTool()) curTooltip.add(I18n.format("gui.progWidget.dig.requiresDiggingTool"));
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("requireDiggingTool", requireDiggingTool);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        requireDiggingTool = tag.getBoolean("requireDiggingTool");
    }
}
