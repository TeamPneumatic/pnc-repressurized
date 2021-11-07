/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
