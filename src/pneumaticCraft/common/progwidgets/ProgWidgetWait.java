package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
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
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_WAIT;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return widget instanceof ProgWidgetWait ? widget.getConnectedParameters()[0] != null ? new DroneAIWait((ProgWidgetString)widget.getConnectedParameters()[0]) : null : null;
    }

    private static class DroneAIWait extends EntityAIBase{

        private final int maxTicks;
        private int ticks;

        private DroneAIWait(ProgWidgetString widget){
            String time = widget.string;
            int multiplier = 1;
            if(time.endsWith("s") || time.endsWith("S")) {
                multiplier = 20;
                time = time.substring(0, time.length() - 1);
            } else if(time.endsWith("m") || time.endsWith("M")) {
                multiplier = 1200;
                time = time.substring(0, time.length() - 1);
            }
            maxTicks = NumberUtils.toInt(time) * multiplier;
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
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FLYING_FLOWER_DAMAGE;
    }
}
