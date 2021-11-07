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
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;

public class MovingSoundMinigun extends TickableSound {
    private final Entity entity;
    private final TileEntity tileEntity;
    private boolean finished = false;

    MovingSoundMinigun(Entity entity) {
        super(ModSounds.MINIGUN.get(), SoundCategory.NEUTRAL);
        this.entity = entity;
        this.tileEntity = null;
        init(entity instanceof EntityDrone ? (float) PNCConfig.Client.Sound.minigunVolumeDrone : (float) PNCConfig.Client.Sound.minigunVolumeHeld);
    }

    MovingSoundMinigun(TileEntity te) {
        super(ModSounds.MINIGUN.get(), SoundCategory.NEUTRAL);
        this.entity = null;
        this.tileEntity = te;
        x = tileEntity.getBlockPos().getX();
        y = tileEntity.getBlockPos().getY();
        z = tileEntity.getBlockPos().getZ();
        init((float) PNCConfig.Client.Sound.minigunVolumeSentryTurret);
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
        finished = minigun == null || !minigun.isMinigunActivated() || minigun.getMinigunSpeed() < Minigun.MAX_GUN_SPEED * 0.9;
        if (finished && !wasFinished) {
            ClientUtils.getClientWorld().playSound(ClientUtils.getClientPlayer(), x, y, z, ModSounds.MINIGUN_STOP.get(), SoundCategory.NEUTRAL, volume, 1f);
        }
    }

    @Override
    public boolean isStopped() {
        return finished;
    }
}
