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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.SentryTurretBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModParticleTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class MovingSoundMinigun extends AbstractTickableSoundInstance {
    private final Entity entity;
    private final BlockEntity tileEntity;
    private boolean finished = false;

    MovingSoundMinigun(Entity entity) {
        super(ModSounds.MINIGUN.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.tileEntity = null;
        init(entity instanceof DroneEntity ?
                ConfigHelper.client().sound.minigunVolumeDrone.get().floatValue() :
                ConfigHelper.client().sound.minigunVolumeHeld.get().floatValue());
    }

    MovingSoundMinigun(BlockEntity te) {
        super(ModSounds.MINIGUN.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.entity = null;
        this.tileEntity = te;
        x = tileEntity.getBlockPos().getX();
        y = tileEntity.getBlockPos().getY();
        z = tileEntity.getBlockPos().getZ();
        init(ConfigHelper.client().sound.minigunVolumeSentryTurret.get().floatValue());
    }

    private void init(float volume) {
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    @Override
    public void tick() {
        Minigun minigun = null;
        boolean wasFinished = finished;
        if (entity != null) {
            if (!entity.isAlive()) {
                finished = true;
            } else {
                x = (float) entity.getX();
                y = (float) entity.getY();
                z = (float) entity.getZ();
                if (entity instanceof Player player) {
                    ItemStack curItem = player.getMainHandItem();
                    if (curItem.getItem() == ModItems.MINIGUN.get()) {
                        minigun = ModItems.MINIGUN.get().getMinigun(curItem, player);
                    }
                } else if (entity instanceof DroneEntity drone) {
                    minigun = drone.getMinigun();
                }


            }
        } else if (tileEntity != null) {
            if (tileEntity.isRemoved()) {
                finished = true;
            } else {
                if (tileEntity instanceof SentryTurretBlockEntity) {
                    minigun = ((SentryTurretBlockEntity) tileEntity).getMinigun();
                }
            }
        }
        if (minigun != null) {
            playParticles(minigun);
        }

        finished = minigun == null || !minigun.isMinigunActivated() || minigun.getMinigunSpeed() < Minigun.MAX_GUN_SPEED * 0.9;
        if (finished && !wasFinished) {
            ClientUtils.getClientLevel().playSound(ClientUtils.getClientPlayer(), x, y, z, ModSounds.MINIGUN_STOP.get(), SoundSource.NEUTRAL, volume, 1f);
            if (minigun != null) {
                Vec3 startVec = minigun.getMuzzlePosition();
                if (startVec != null) {
                    ClientUtils.getClientLevel().addParticle(ParticleTypes.LARGE_SMOKE, startVec.x, startVec.y, startVec.z, 0, 0, 0);
                }
            }
        }
    }

    private void playParticles(Minigun minigun) {
        Vec3 muzzlePos = minigun.getMuzzlePosition();
        if (muzzlePos == null) return;

        Vec3 lookVec = minigun.getLookAngle();
        RandomSource r = ClientUtils.getClientLevel().random;
        for (int i = 0; i < 10; i ++) {
            Vec3 velVec = lookVec.scale(3f + i * 0.2f);
            Particle bullet = Minecraft.getInstance().particleEngine.createParticle(ModParticleTypes.BULLET_PARTICLE.get(),
                    muzzlePos.x + r.nextFloat() * 0.1f - 0.05f,
                    muzzlePos.y + r.nextFloat() * 0.1f - 0.05f,
                    muzzlePos.z + r.nextFloat() * 0.1f - 0.05f,
                    velVec.x, velVec.y, velVec.z);
            if (bullet != null) {
                switch (r.nextInt(8)) {
                    case 0 -> bullet.setColor(0.1f, 0.1f, 0.1f);
                    case 1,2,3 -> bullet.setColor(1f, 0.25f + r.nextFloat() * 0.75f, 0f);
                    default -> {
                        float[] rgb = RenderUtils.decomposeColorF(minigun.getAmmoColor());
                        bullet.setColor(rgb[1], rgb[2], rgb[3]);
                    }
                }
                bullet.scale(minigun.getParticleScale());
            }
        }
        if (r.nextInt(10) == 0) {
            ClientUtils.getClientLevel().addParticle(ParticleTypes.FLAME, muzzlePos.x, muzzlePos.y, muzzlePos.z, lookVec.x * 0.01f, lookVec.y * 0.01f, lookVec.z * 0.01f);
        }
    }

    @Override
    public boolean isStopped() {
        return finished;
    }
}
