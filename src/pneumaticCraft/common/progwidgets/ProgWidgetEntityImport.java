package pneumaticCraft.common.progwidgets;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetAreaShow;
import pneumaticCraft.common.ai.DroneAIEntityImport;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetEntityImport extends ProgWidget implements IProgWidget, IAreaProvider, IEntityProvider{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetAreaShow(this, guiProgrammer);
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAIEntityImport(drone, drone.getSpeed(), widget);
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "entityImport";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.ACTION;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ENTITY_IM;
    }

    @Override
    public Set<ChunkPosition> getArea(){
        return ProgWidgetAreaItemBase.getArea((ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[2]);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.RAIN_PLANT_DAMAGE;
    }

    @Override
    public List<Entity> getValidEntities(World world){
        return ProgWidgetAreaItemBase.getValidEntities(world, this);
    }

    @Override
    public boolean isEntityValid(Entity entity){
        return ProgWidgetAreaItemBase.isEntityValid(entity, this);
    }
}
