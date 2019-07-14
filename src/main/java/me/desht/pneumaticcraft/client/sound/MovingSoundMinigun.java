package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
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

    MovingSoundMinigun(Entity entity) {
        super(Sounds.MINIGUN, SoundCategory.NEUTRAL);
        this.entity = entity;
        this.tileEntity = null;
        init();
    }

    MovingSoundMinigun(TileEntity tileEntity) {
        super(Sounds.MINIGUN, SoundCategory.NEUTRAL);
        this.entity = null;
        this.tileEntity = tileEntity;
        x = tileEntity.getPos().getX();
        y = tileEntity.getPos().getY();
        z = tileEntity.getPos().getZ();
        init();
    }

    private void init() {
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.3F;
    }

    @Override
    public void tick() {
        Minigun minigun = null;
        if (entity != null) {
            if (!entity.isAlive()) {
                donePlaying = true;
                return;
            }
            x = (float) entity.posX;
            y = (float) entity.posY;
            z = (float) entity.posZ;
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                ItemStack curItem = player.getHeldItemMainhand();
                if (curItem.getItem() == ModItems.MINIGUN) {
                    minigun = ((ItemMinigun) ModItems.MINIGUN).getMinigun(curItem, player);
                }
            } else if (entity instanceof EntityDrone) {
                minigun = ((EntityDrone) entity).getMinigun();
            }
        } else if (tileEntity != null) {
            if (tileEntity.isRemoved()) {
                donePlaying = true;
                return;
            }
            if (tileEntity instanceof TileEntitySentryTurret) {
                minigun = ((TileEntitySentryTurret) tileEntity).getMinigun();
            }
        }
        if (minigun != null) {
            donePlaying = !minigun.isMinigunActivated() || minigun.getMinigunSpeed() < Minigun.MAX_GUN_SPEED * 0.9;
        } else {
            donePlaying = true;
        }
    }
}
