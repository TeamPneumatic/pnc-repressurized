package me.desht.pneumaticcraft.client.sound;

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

    MovingSoundMinigun(TileEntity tileEntity) {
        super(ModSounds.MINIGUN.get(), SoundCategory.NEUTRAL);
        this.entity = null;
        this.tileEntity = tileEntity;
        x = tileEntity.getPos().getX();
        y = tileEntity.getPos().getY();
        z = tileEntity.getPos().getZ();
        init((float) PNCConfig.Client.Sound.minigunVolumeSentryTurret);
    }

    private void init(float volume) {
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = volume;
    }

    @Override
    public void tick() {
        Minigun minigun = null;
        if (entity != null) {
            if (!entity.isAlive()) {
                finished = true;
                return;
            }
            x = (float) entity.getPosX();
            y = (float) entity.getPosY();
            z = (float) entity.getPosZ();
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                ItemStack curItem = player.getHeldItemMainhand();
                if (curItem.getItem() == ModItems.MINIGUN.get()) {
                    minigun = ModItems.MINIGUN.get().getMinigun(curItem, player);
                }
            } else if (entity instanceof EntityDrone) {
                minigun = ((EntityDrone) entity).getMinigun();
            }
        } else if (tileEntity != null) {
            if (tileEntity.isRemoved()) {
                finished = true;
                return;
            }
            if (tileEntity instanceof TileEntitySentryTurret) {
                minigun = ((TileEntitySentryTurret) tileEntity).getMinigun();
            }
        }
        finished = minigun == null || !minigun.isMinigunActivated() || minigun.getMinigunSpeed() < Minigun.MAX_GUN_SPEED * 0.9;
    }

    @Override
    public boolean isDonePlaying() {
        return finished;
    }
}
