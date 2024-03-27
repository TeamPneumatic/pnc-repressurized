package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActiveEntityHacks implements IActiveEntityHacks, INBTSerializable<CompoundTag> {
    private final Map<ResourceLocation, IHackableEntity<? extends Entity>> hacks = new HashMap<>();

    @Override
    public void tick(Entity entity) {
        hacks.values().removeIf(hackable -> !hackable._afterHackTick(entity));
    }

    @Override
    public void addHackable(IHackableEntity<?> hackable) {
        hacks.put(hackable.getHackableId(), hackable);
    }

    @Override
    public Collection<IHackableEntity<?>> getCurrentHacks() {
        return hacks.values();
    }

    @Override
    public void clear() {
        hacks.clear();
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT() {
        return getCurrentHacks().isEmpty() ?
                null :
                Util.make(new CompoundTag(), compound ->
                        compound.put("hackables", Util.make(new ListTag(), list -> {
                            for (IHackableEntity<?> hackableEntity : getCurrentHacks()) {
                                list.add(Util.make(new CompoundTag(), t1 ->
                                        t1.putString("id", hackableEntity.getHackableId().toString())));
                            }
                        })));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        clear();
        ListTag tagList = nbt.getList("hackables", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            String id = tagList.getCompound(i).getString("id");
            if (ResourceLocation.isValidResourceLocation(id)) {
                ResourceLocation hackableId = new ResourceLocation(id);
                CommonArmorRegistry.getInstance().getHackableEntityForId(hackableId).ifPresentOrElse(
                        this::addHackable,
                        () -> Log.error("entity-hackable '%s' not found when deserializing IHacking capability?", hackableId)
                );
            } else {
                Log.error("invalid hackable id '%s': not a resource location", id);
            }
        }
    }
}
