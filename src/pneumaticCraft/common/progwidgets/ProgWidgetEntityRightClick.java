package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetAreaShow;
import pneumaticCraft.common.ai.DroneAIBlockInteract;
import pneumaticCraft.common.ai.DroneEntityBase;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetEntityRightClick extends ProgWidget implements IAreaProvider, IEntityProvider{

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(getConnectedParameters()[0] == null) {
            curInfo.add("gui.progWidget.area.error.noArea");
        }
    }

    @Override
    public boolean hasStepInput(){
        return true;
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
    public String getWidgetString(){
        return "entityRightClick";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ENTITY_RIGHT_CLICK;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneEntityBase<IProgWidget, EntityLivingBase>(drone, widget){
            private final List<Entity> visitedEntities = new ArrayList<Entity>();

            @Override
            protected boolean isEntityValid(Entity entity){
                return entity instanceof EntityLivingBase && !visitedEntities.contains(entity);
            }

            @Override
            protected boolean doAction(){
                visitedEntities.add(targetedEntity);
                boolean activated = false;
                ItemStack stack = drone.getInventory().getStackInSlot(0);
                if(stack != null && stack.getItem().itemInteractionForEntity(stack, drone.getFakePlayer(), targetedEntity)) {
                    activated = true;
                }
                if(!activated && targetedEntity instanceof EntityAgeable && ((EntityAgeable)targetedEntity).interact(drone.getFakePlayer())) {
                    activated = true;
                }
                DroneAIBlockInteract.transferToDroneFromFakePlayer(drone);
                return false;//return activated; <-- will right click as long as it's sucessfully activated.
            }

        };
    }

    @Override
    public List<Entity> getValidEntities(World world){
        return ProgWidgetAreaItemBase.getValidEntities(world, this);
    }

    @Override
    public boolean isEntityValid(Entity entity){
        return ProgWidgetAreaItemBase.isEntityValid(entity, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetAreaShow(this, guiProgrammer);
    }

    @Override
    public void getArea(Set<ChunkPosition> area){
        ProgWidgetEntityAttack.getArea(area, (ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[2]);
    }
}
