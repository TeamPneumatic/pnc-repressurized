package pneumaticCraft.common.progwidgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetBlockRightClick;
import pneumaticCraft.common.ai.DroneAIBlockInteract;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace implements IBlockRightClicker{

    private boolean sneaking;

    @Override
    public String getWidgetString(){
        return "blockRightClick";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_BLOCK_RIGHT_CLICK;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return setupMaxActions(new DroneAIBlockInteract(drone, (ProgWidgetAreaItemBase)widget), (IMaxActions)widget);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetBlockRightClick(this, guiProgrammer);
    }

    @Override
    public boolean isSneaking(){
        return sneaking;
    }

    public void setSneaking(boolean sneaking){
        this.sneaking = sneaking;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("sneaking", sneaking);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        sneaking = tag.getBoolean("sneaking");
    }

}
