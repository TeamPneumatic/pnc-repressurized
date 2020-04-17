package me.desht.pneumaticcraft.common.hacking.block;

import net.minecraft.block.TrapDoorBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HackableTrapDoor extends HackableDoor {
    @Override
    public ResourceLocation getHackableId() {
        return RL("trapdoor");
    }

    @Override
    protected BooleanProperty getOpenProperty() {
        return TrapDoorBlock.OPEN;
    }
}
