package me.desht.pneumaticcraft.api.hacking;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import net.minecraft.entity.Entity;

import java.util.List;

public interface IHacking {
    void update(Entity entity);
    void addHackable(IHackableEntity hackable);
    List<IHackableEntity> getCurrentHacks();
}
