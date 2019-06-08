package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class HackablePainting implements IHackableEntity {
    @Override
    public String getId() {
        return "painting";
    }

    @Override
    public boolean canHack(Entity entity, EntityPlayer player) {
        return entity instanceof EntityPainting;
    }

    @Override
    public void addInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("Hack to change artwork");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, EntityPlayer player) {
        curInfo.add("Artwork changed!");
    }

    @Override
    public int getHackTime(Entity entity, EntityPlayer player) {
        return 40;
    }

    @Override
    public void onHackFinished(Entity entity, EntityPlayer player) {
        EntityPainting painting = (EntityPainting) entity;

        EntityPainting.EnumArt art = painting.art;
        List<EntityPainting.EnumArt> candidate = new ArrayList<>();
        for (EntityPainting.EnumArt newArt : EntityPainting.EnumArt.values()) {
            if (newArt.sizeX == art.sizeX && newArt.sizeY == art.sizeY && newArt != art) {
                candidate.add(newArt);
            }
        }
        painting.art = candidate.get(entity.world.rand.nextInt(candidate.size()));
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
