package pneumaticCraft.common.progwidgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetLiquidExport;
import pneumaticCraft.common.ai.DroneAILiquidExport;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetLiquidExport extends ProgWidgetInventoryBase implements ILiquidFiltered, ILiquidExport{

    private boolean placeFluidBlocks;

    @Override
    public String getWidgetString(){
        return "liquidExport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LIQUID_EX;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetLiquidFilter.class};
    }

    @Override
    public boolean isFluidValid(Fluid fluid){
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 1);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAILiquidExport(drone, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("placeFluidBlocks", placeFluidBlocks);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        placeFluidBlocks = tag.getBoolean("placeFluidBlocks");
    }

    @Override
    public void setPlaceFluidBlocks(boolean placeFluidBlocks){
        this.placeFluidBlocks = placeFluidBlocks;
    }

    @Override
    public boolean isPlacingFluidBlocks(){
        return placeFluidBlocks;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetLiquidExport(this, guiProgrammer);
    }

}
