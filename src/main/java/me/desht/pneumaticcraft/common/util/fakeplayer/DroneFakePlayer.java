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

package me.desht.pneumaticcraft.common.util.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public class DroneFakePlayer extends FakePlayer {
    private final IDroneBase drone;
    private boolean sneaking;

    public DroneFakePlayer(ServerLevel world, GameProfile name, IDroneBase drone) {
        super(world, name);
        this.drone = drone;
    }

    @Override
    public void giveExperiencePoints(int amount) {
        Vec3 pos = drone.getDronePos();
        ExperienceOrb orb = new ExperienceOrb(drone.getDroneLevel(), pos.x, pos.y, pos.z, amount);
        drone.getDroneLevel().addFreshEntity(orb);
    }

    @Override
    public void playNotifySound(SoundEvent soundEvent, SoundSource category, float volume, float pitch) {
        drone.playSound(soundEvent, category, volume, pitch);
    }

    @Override
    public boolean isShiftKeyDown() {
        return sneaking;
    }

    @Override
    public void setShiftKeyDown(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public void tick() {
        attackStrengthTicker++;  // without this, drone's melee will be hopeless
        getCooldowns().tick();   // allow use of items with cooldowns
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public Vec3 position() {
        return drone.getDronePos();
    }

    @Override
    public BlockPos blockPosition() {
        return BlockPos.containing(drone.getDronePos());
    }
}
