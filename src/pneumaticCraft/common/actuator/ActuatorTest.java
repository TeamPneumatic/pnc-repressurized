package pneumaticCraft.common.actuator;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.api.actuator.IActuator;

public class ActuatorTest implements IActuator{

    @Override
    public String getSensorPath(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean needsTextBox(){
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getDescription(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void actuate(TileEntity universalActuator){
        // TODO Auto-generated method stub

    }

}
