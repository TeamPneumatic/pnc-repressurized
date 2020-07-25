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
        x = te.getPos().getX();
        y = te.getPos().getY() + te.extension / 2;
        z = te.getPos().getZ();
        repeat = true;
        repeatDelay = 0;
        volume = (float) PNCConfig.Client.Sound.elevatorVolumeRunning;
    }

    @Override
    public void tick() {
        y = te.getPos().getY() + te.extension / 2;
    }

    @Override
    public boolean isDonePlaying() {
        return te.isRemoved() || te.extension == te.getTargetExtension();
    }
}
