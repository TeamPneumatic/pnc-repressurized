package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetAreaShow;
import pneumaticCraft.common.ai.DroneAINearestAttackableTarget;
import pneumaticCraft.common.ai.StringFilterEntitySelector;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetEntityAttack extends ProgWidget implements IAreaProvider, IEntityProvider{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new EntityAIAttackOnCollide(drone, 0.1D, false);
    }

    @Override
    public EntityAIBase getWidgetTargetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAINearestAttackableTarget(drone, 0, false, (ProgWidget)widget);
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ATTACK;
    }

    @Override
    public String getWidgetString(){
        return "entityAttack";
    }

    @Override
    public List<EntityLivingBase> getValidEntities(World world){
        StringFilterEntitySelector whitelistFilter = new StringFilterEntitySelector();
        StringFilterEntitySelector blacklistFilter = new StringFilterEntitySelector();

        ProgWidgetString widget = (ProgWidgetString)getConnectedParameters()[1];
        if(widget != null) {
            while(widget != null) {
                whitelistFilter.addEntry(widget.string);
                widget = (ProgWidgetString)widget.getConnectedParameters()[0];
            }
        } else {
            whitelistFilter.setFilter("");
        }

        widget = (ProgWidgetString)getConnectedParameters()[3];
        while(widget != null) {
            blacklistFilter.addEntry(widget.string);
            widget = (ProgWidgetString)widget.getConnectedParameters()[0];
        }

        List<Entity> entities = ProgWidgetAreaItemBase.getEntitiesInArea((ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[2], world, whitelistFilter, blacklistFilter);
        List<EntityLivingBase> livingEntities = new ArrayList<EntityLivingBase>();
        for(Entity entity : entities) {
            if(entity instanceof EntityLivingBase) {
                livingEntities.add((EntityLivingBase)entity);
            }
        }
        return livingEntities;
    }

    @Override
    public Set<ChunkPosition> getArea(){
        return getArea((ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[2]);
    }

    public static Set<ChunkPosition> getArea(ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget){
        if(whitelistWidget == null) return new HashSet<ChunkPosition>();
        Set<ChunkPosition> area = new HashSet<ChunkPosition>();
        ProgWidgetArea widget = whitelistWidget;
        while(widget != null) {
            ProgWidgetArea.EnumAreaType oldAreaType = widget.type;
            widget.type = ProgWidgetArea.EnumAreaType.FILL;
            area.addAll(widget.getArea());
            widget.type = oldAreaType;
            widget = (ProgWidgetArea)widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while(widget != null) {
            ProgWidgetArea.EnumAreaType oldAreaType = widget.type;
            widget.type = ProgWidgetArea.EnumAreaType.FILL;
            area.removeAll(widget.getArea());
            widget.type = oldAreaType;
            widget = (ProgWidgetArea)widget.getConnectedParameters()[0];
        }
        return new HashSet<ChunkPosition>(area);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetAreaShow(this, guiProgrammer);
    }

    @Override
    public String getGuiTabText(){
        return "This module can be used to kill certain entities in a certain area. Defining an entity filter isn't required: With no filter, every living entity will be attacked. Note that the connected 'Area' puzzle pieces always will be handled as they were in 'Filled' mode. \n \nAir usage: " + PneumaticValues.DRONE_USAGE_ATTACK + "mL/hit.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFF0000;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.ACTION;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FIRE_FLOWER_DAMAGE;
    }
}
