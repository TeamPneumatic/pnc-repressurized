package me.desht.pneumaticcraft.common.hacking.entity;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class HackablePainting implements IHackableEntity {
    @Override
    public String getId() {
        return "painting";
    }

    @Override
    public boolean canHack(Entity entity, PlayerEntity player) {
        return entity instanceof PaintingEntity;
    }

    @Override
    public void addHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("Hack to change artwork");
    }

    @Override
    public void addPostHackInfo(Entity entity, List<String> curInfo, PlayerEntity player) {
        curInfo.add("Artwork changed!");
    }

    @Override
    public int getHackTime(Entity entity, PlayerEntity player) {
        return 40;
    }

    @Override
    public void onHackFinished(Entity entity, PlayerEntity player) {
        PaintingEntity painting = (PaintingEntity) entity;

        PaintingType art = painting.art;
        List<PaintingType> candidate = new ArrayList<>();
        for (PaintingType a : ForgeRegistries.PAINTING_TYPES.getValues()) {
            if (a.getHeight() == art.getHeight() && a.getWidth() == art.getWidth()) {
                candidate.add(a);
            }
        }
        painting.art = candidate.get(entity.world.rand.nextInt(candidate.size()));
    }

    @Override
    public boolean afterHackTick(Entity entity) {
        return false;
    }
}
