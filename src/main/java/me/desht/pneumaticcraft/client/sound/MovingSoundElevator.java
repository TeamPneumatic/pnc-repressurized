package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;

public class MovingSoundElevator extends TickableSound {
    private final TileEntityElevatorBase te;

    MovingSoundElevator(TileEntityElevatorBase te) {
        super(ModSounds.ELEVATOR_RISING.get(), SoundCategory.BLOCKS);
        this.te = te;
        x = te.getBlockPos().getX();
        y = te.getBlockPos().getY() + te.extension;
        z = te.getBlockPos().getZ();
        looping = true;
        delay = 0;
        volume = (float) PNCConfig.Client.Sound.elevatorVolumeRunning;
    }

    @Override
    public void tick() {
        y = te.getBlockPos().getY() + te.extension;
    }

    @Override
    public boolean isStopped() {
        return te.isRemoved() || te.isStopped();
    }
}
