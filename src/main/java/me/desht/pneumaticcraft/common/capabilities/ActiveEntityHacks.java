package me.desht.pneumaticcraft.common.capabilities;

import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActiveEntityHacks implements IActiveEntityHacks, ValueIOSerializable {
    private static final Codec<Identifier> ENTRY_CODEC = Identifier.CODEC.fieldOf("id").codec();

    private final Map<Identifier, IHackableEntity<? extends Entity>> hacks = new HashMap<>();

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
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<Identifier> list = output.list("hackables", ENTRY_CODEC);
        for (IHackableEntity<?> hackableEntity : getCurrentHacks()) {
            list.add(hackableEntity.getHackableId());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        clear();
        input.listOrEmpty("hackables", ENTRY_CODEC).forEach(hackableId ->
                CommonArmorRegistry.getInstance().getHackableEntityForId(hackableId).ifPresentOrElse(
                        this::addHackable,
                        () -> Log.error("entity-hackable '{}' not found when deserializing IHacking capability?", hackableId)
                )
        );
    }
}
