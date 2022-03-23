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
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModParticleTypes;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class MovingSoundMinigun extends TickableSound {
    private final Entity entity;
    private final TileEntity tileEntity;
    private boolean finished = false;

    MovingSoundMinigun(Entity entity) {
        super(ModSounds.MINIGUN.get(), SoundCategory.NEUTRAL);
        this.entity = entity;
        this.tileEntity = null;
        init(entity instanceof EntityDrone ?
                ConfigHelper.client().sound.minigunVolumeDrone.get().floatValue() :
                ConfigHelper.client().sound.minigunVolumeHeld.get().floatValue());
    }

    MovingSoundMinigun(TileEntity te) {
        super(ModSounds.MINIGUN.get(), SoundCategory.NEUTRAL);
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
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;
                    ItemStack curItem = player.getMainHandItem();
                    if (curItem.getItem() == ModItems.MINIGUN.get()) {
                        minigun = ModItems.MINIGUN.get().getMinigun(curItem, player);
                    }
                } else if (entity instanceof EntityDrone) {
                    minigun = ((EntityDrone) entity).getMinigun();
                }
            }
        } else if (tileEntity != null) {
            if (tileEntity.isRemoved()) {
                finished = true;
            } else {
                if (tileEntity instanceof TileEntitySentryTurret) {
                    minigun = ((TileEntitySentryTurret) tileEntity).getMinigun();
                }
            }
        }
        if (minigun != null) {
            playParticles(minigun);
        }

        finished = minigun == null || !minigun.isMinigunActivated() || minigun.getMinigunSpeed() < Minigun.MAX_GUN_SPEED * 0.9;
        if (finished && !wasFinished) {
            ClientUtils.getClientWorld().playSound(ClientUtils.getClientPlayer(), x, y, z, ModSounds.MINIGUN_STOP.get(), SoundCategory.NEUTRAL, volume, 1f);
            if (minigun != null) {
                Vector3d startVec = minigun.getMuzzlePosition();
                if (startVec != null) {
                    ClientUtils.getClientWorld().addParticle(ParticleTypes.LARGE_SMOKE, startVec.x, startVec.y, startVec.z, 0, 0, 0);
                }
            }
        }
    }

    private void playParticles(Minigun minigun) {
        Vector3d muzzlePos = minigun.getMuzzlePosition();
        if (muzzlePos == null) return;

        Vector3d lookVec = minigun.getLookAngle();
        Random r = ClientUtils.getClientWorld().random;
        for (int i = 0; i < 10; i ++) {
            Vector3d velVec = lookVec.scale(3f + i * 0.2f);
            Particle bullet = Minecraft.getInstance().particleEngine.createParticle(ModParticleTypes.BULLET_PARTICLE.get(),
                    muzzlePos.x + r.nextFloat() * 0.1f - 0.05f,
                    muzzlePos.y + r.nextFloat() * 0.1f - 0.05f,
                    muzzlePos.z + r.nextFloat() * 0.1f - 0.05f,
                    velVec.x, velVec.y, velVec.z);
            if (bullet != null) {
                switch (r.nextInt(8)) {
                    case 0:
                        bullet.setColor(0.1f, 0.1f, 0.1f);
                        break;
                    case 1: case 2: case 3:
                        bullet.setColor(1f, 0.25f + r.nextFloat() * 0.75f, 0f);
                        break;
                    default:
                        float[] rgb = RenderUtils.decomposeColorF(minigun.getAmmoColor());
                        bullet.setColor(rgb[1], rgb[2], rgb[3]);
                        break;
                }
                bullet.scale(minigun.getParticleScale());
            }
        }
        if (r.nextInt(10) == 0) {
            ClientUtils.getClientWorld().addParticle(ParticleTypes.FLAME, muzzlePos.x, muzzlePos.y, muzzlePos.z, lookVec.x * 0.01f, lookVec.y * 0.01f, lookVec.z * 0.01f);
        }
    }

    @Override
    public boolean isStopped() {
        return finished;
    }
}
