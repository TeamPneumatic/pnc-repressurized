package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySentryTurret;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;

public class MovingSoundMinigun extends MovingSound {
    private final Entity entity;
    private final TileEntity tileEntity;

    protected MovingSoundMinigun(Entity entity) {
        super(Sounds.MINIGUN, SoundCategory.NEUTRAL);
        this.entity = entity;
        this.tileEntity = null;
        init();
    }

    public MovingSoundMinigun(TileEntity tileEntity) {
        super(Sounds.MINIGUN, SoundCategory.NEUTRAL);
        this.entity = null;
        this.tileEntity = tileEntity;
        xPosF = tileEntity.getPos().getX();
        yPosF = tileEntity.getPos().getY();
        zPosF = tileEntity.getPos().getZ();
        init();
    }

    private void init() {
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.3F;
    }

    @Override
    public void update() {
        Minigun minigun = null;
        if (entity != null) {
            if (entity.isDead) {
                donePlaying = true;
                return;
            }
            xPosF = (float) entity.posX;
            yPosF = (float) entity.posY;
            zPosF = (float) entity.posZ;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                ItemStack curItem = player.getHeldItemMainhand();
                if (curItem.getItem() == Itemss.MINIGUN) {
                    minigun = ((ItemMinigun) Itemss.MINIGUN).getMinigun(curItem, player);
                }
            } else if (entity instanceof EntityDrone) {
                minigun = ((EntityDrone) entity).getMinigun();
            }
        } else if (tileEntity != null) {
            if (tileEntity.isInvalid()) {
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
