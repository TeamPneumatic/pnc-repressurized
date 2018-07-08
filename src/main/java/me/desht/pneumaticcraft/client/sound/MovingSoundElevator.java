package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;

public class MovingSoundElevator extends MovingSound {
    private final TileEntityElevatorBase te;

    protected MovingSoundElevator(TileEntityElevatorBase te) {
        super(Sounds.ELEVATOR_MOVING, SoundCategory.BLOCKS);
        this.te = te;
        xPosF = te.getPos().getX();
        yPosF = te.getPos().getY() + te.extension / 2;
        zPosF = te.getPos().getZ();
        repeat = true;
        repeatDelay = 0;
        volume = 0.85F;
    }

    @Override
    public void update() {
        if (te.isInvalid() || te.extension == te.getTargetExtension()) {
            donePlaying = true;
        }
        yPosF = te.getPos().getY() + te.extension / 2;
    }
}
