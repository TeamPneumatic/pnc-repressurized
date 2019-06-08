package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class HackableItemFrame implements IHackableEntity {
    @Override
    public String getId() {
        return "itemFrame";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return entity instanceof EntityItemFrame && !((EntityItemFrame) entity).getDisplayedItem().isEmpty();
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("Hack to detach item");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("Item detached!");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 60;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        if (!entity.world.isRemote) {
            ((EntityItemFrame) entity).dropItemOrSelf(player, false);
        }
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
