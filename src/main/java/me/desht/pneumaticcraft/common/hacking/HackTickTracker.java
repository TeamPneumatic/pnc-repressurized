package me.desht.pneumaticcraft.common.hacking;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HackTickTracker extends SavedData {
    // Note: only block hacks are serialized to world saved data
    // Entity hacks are saved on the entity via CapabilityHacking
    private static final String DATA_NAME = "PneumaticCraftBlockHacks";

    private static final HackTickTracker clientInstance = new HackTickTracker();

    private final Map<BlockPos, IHackableBlock> hackedBlocks = new HashMap<>();
    private final Set<Entity> hackedEntities = new HashSet<>();

    private HackTickTracker() {
    }

    private static HackTickTracker load(CompoundTag tag) {
        return new HackTickTracker().readNBT(tag);
    }

    public static HackTickTracker getInstance(Level level) {
        return level instanceof ServerLevel s ?
                s.getDataStorage().computeIfAbsent(new Factory<>(HackTickTracker::new, HackTickTracker::load), DATA_NAME) :
                clientInstance;
    }

    private HackTickTracker readNBT(CompoundTag tag) {
        ListTag list = tag.getList("block_hacks", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag sub = list.getCompound(i);
            BlockPos pos = new BlockPos(sub.getInt("x"), sub.getInt("y"), sub.getInt("z"));
            try {
                ResourceLocation id = new ResourceLocation(sub.getString("id"));
                CommonArmorRegistry.getInstance().getHackableBlockForId(id).ifPresentOrElse(
                        hackable -> hackedBlocks.put(pos, hackable),
                        () -> Log.error("unknown hackable block ID '{}'", id)
                );
            } catch (ResourceLocationException e) {
                Log.error("invalid hackable block ID '{}'", sub.getString("id"));
            }
        }

        return this;
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag blockTag = new ListTag();
        hackedBlocks.forEach((pos, hackable) -> {
            CompoundTag sub = new CompoundTag();
            sub.putInt("x", pos.getX());
            sub.putInt("y", pos.getY());
            sub.putInt("z", pos.getZ());
            sub.putString("id", hackable.getHackableId().toString());
            blockTag.add(sub);
        });
        pCompoundTag.put("block_hacks", blockTag);
        return pCompoundTag;
    }

    void tick(Level world) {
        if (hackedBlocks.entrySet().removeIf(entry -> !entry.getValue().afterHackTick(world, entry.getKey()))) {
            setDirty();
        }

        // IHacking#tick() will remove any no-longer-applicable hacks from the entity
        hackedEntities.forEach(entity -> HackManager.getActiveHacks(entity).ifPresent(hacking -> {
            if (entity.isAlive() && !hacking.getCurrentHacks().isEmpty()) hacking.tick(entity);
        }));
        // Remove the entity from the tracker if it has no more applicable hacks
        hackedEntities.removeIf(e -> !e.isAlive() ||
                HackManager.getActiveHacks(e).map(hacking -> hacking.getCurrentHacks().isEmpty()).orElse(true)
        );
    }

    public void trackBlock(BlockPos pos, IHackableBlock iHackable) {
        hackedBlocks.put(pos, iHackable);
        setDirty();
    }

    public void trackEntity(Entity entity, IHackableEntity<?> iHackable) {
        HackManager.getActiveHacks(entity).ifPresent(hacking -> {
            hacking.addHackable(iHackable);
            hackedEntities.add(entity);
        });
    }

    public boolean isEntityTracked(Entity entity) {
        return hackedEntities.contains(entity);
    }
}
