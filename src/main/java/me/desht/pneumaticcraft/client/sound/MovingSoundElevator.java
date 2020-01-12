package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;

public class MovingSoundElevator extends TickableSound {
    private final TileEntityElevatorBase te;

    MovingSoundElevator(TileEntityElevatorBase te) {
        super(ModSounds.ELEVATOR_RISING.get(), SoundCategory.BLOCKS);
        this.te = te;
        x = te.getPos().getX();
        y = te.getPos().getY() + te.extension / 2;
        z = te.getPos().getZ();
        repeat = true;
        repeatDelay = 0;
        volume = 0.9F - (0.05F * Math.min(8, te.multiElevatorCount));
    }

    @Override
    public void tick() {
        if (te.isRemoved() || te.extension == te.getTargetExtension()) {
            donePlaying = true;
        }
        y = te.getPos().getY() + te.extension / 2;
    }
}
