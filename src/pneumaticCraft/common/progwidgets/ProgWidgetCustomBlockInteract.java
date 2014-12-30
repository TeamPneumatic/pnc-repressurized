package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.ICustomBlockInteract;
import pneumaticCraft.common.ai.DroneAICustomBlockInteract;
import pneumaticCraft.common.entity.living.EntityDrone;

public class ProgWidgetCustomBlockInteract extends ProgWidgetInventoryBase{

    private ICustomBlockInteract interactor;

    public ProgWidgetCustomBlockInteract setInteractor(ICustomBlockInteract interactor){
        this.interactor = interactor;
        return this;
    }

    @Override
    public IProgWidget copy(){
        ProgWidgetCustomBlockInteract widget = (ProgWidgetCustomBlockInteract)super.copy();
        widget.setInteractor(interactor);
        return widget;
    }

    @Override
    public String getWidgetString(){
        return interactor.getName();
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
    protected ResourceLocation getTexture(){
        return interactor.getTexture();
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAICustomBlockInteract(drone, drone.getSpeed(), (ProgWidgetAreaItemBase)widget, interactor);
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public int getCraftingColorIndex(){
        return interactor.getCraftingColorIndex();
    }

}
