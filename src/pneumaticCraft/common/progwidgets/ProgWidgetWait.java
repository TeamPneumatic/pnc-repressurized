package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetWait extends ProgWidget{

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
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist(){
        return false;
    }

    @Override
    public String getWidgetString(){
        return "wait";
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
        return Textures.PROG_WIDGET_WAIT;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return widget.getConnectedParameters()[0] != null ? new DroneAIWait((ProgWidgetString)widget.getConnectedParameters()[0]) : null;
    }

    private static class DroneAIWait extends EntityAIBase{

        private final int maxTicks;
        private int ticks;

        private DroneAIWait(ProgWidgetString widget){
            maxTicks = NumberUtils.toInt(widget.string);
        }

        @Override
        public boolean shouldExecute(){
            return ticks < maxTicks;
        }

        @Override
        public boolean continueExecuting(){
            ticks++;
            return shouldExecute();
        }

    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.ACTION;
    }
}
