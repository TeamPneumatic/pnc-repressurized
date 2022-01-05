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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModSounds;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class MovingSoundAirLeak extends TickableSound {
    private final TileEntity te;
    private final Direction dir;
    private float targetPitch;

    MovingSoundAirLeak(TileEntity te, Direction dir) {
        super(ModSounds.LEAKING_GAS.get(), SoundCategory.BLOCKS);
        this.te = te;
        this.dir = dir;
        this.x = te.getBlockPos().getX();
        this.y = te.getBlockPos().getY();
        this.z = te.getBlockPos().getZ();
        this.looping = true;
        this.delay = 0;
        this.targetPitch = 1F;
        this.volume = ConfigHelper.client().sound.airLeakVolume.get().floatValue();
    }

    @Override
    public void tick() {
        te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir).ifPresent(handler -> {
            targetPitch = MathHelper.clamp(1.0f + ((handler.getPressure() - 3) / 20), 0.8f, 1.6f);
            if (pitch > targetPitch) pitch -= 0.005F;
            else if (pitch < targetPitch) pitch += 0.005F;
        });
    }

    @Override
    public boolean isStopped() {
        return te.isRemoved() || te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir)
                .map(h -> h.getSideLeaking() == null || h.getAir() == 0)
                .orElse(true);
    }
}
