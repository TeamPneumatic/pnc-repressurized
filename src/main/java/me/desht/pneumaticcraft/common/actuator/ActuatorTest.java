package me.desht.pneumaticcraft.common.actuator;

import me.desht.pneumaticcraft.api.actuator.IActuator;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class ActuatorTest implements IActuator {

    @Override
    public String getSensorPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean needsTextBox() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void actuate(TileEntity universalActuator) {
        // TODO Auto-generated method stub

    }

}
