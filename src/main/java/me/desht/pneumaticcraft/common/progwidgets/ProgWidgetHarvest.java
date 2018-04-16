package me.desht.pneumaticcraft.common.progwidgets;

import java.util.List;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetHarvest;
import me.desht.pneumaticcraft.common.ai.DroneAIHarvest;
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

public class ProgWidgetHarvest extends ProgWidgetDigAndPlace implements IToolUser{

    private boolean requireHoe;
    
    public ProgWidgetHarvest() {
        super(ProgWidgetDigAndPlace.EnumOrder.CLOSEST);
    }

    @Override
    public String getWidgetString() {
        return "harvest";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_HARVEST;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIHarvest(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetHarvest(this, guiProgrammer);
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.BROWN;
    }

    @Override
    public boolean requiresTool(){
        return requireHoe;
    }
    
    @Override
    public void setRequiresTool(boolean requireHoe){
        this.requireHoe = requireHoe;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        
        if(requiresTool()) curTooltip.add(I18n.format("gui.progWidget.harvest.requiresHoe"));
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("requireHoe", requireHoe);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        requireHoe = tag.getBoolean("requireHoe");
    }
}
