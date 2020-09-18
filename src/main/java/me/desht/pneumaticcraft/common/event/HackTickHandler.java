package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum HackTickHandler {
    INSTANCE;

    private final Map<ResourceLocation, Map<BlockPos, IHackableBlock>> hackedBlocks = new HashMap<>();
    private final Map<ResourceLocation, Set<Entity>> hackedEntities = new HashMap<>();

    public static HackTickHandler instance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ResourceLocation worldKey = getKey(event.world);

            if (hackedBlocks.containsKey(worldKey)) {
                hackedBlocks.get(worldKey).entrySet().removeIf(entry -> !entry.getValue().afterHackTick(event.world, entry.getKey()));
            }

            if (hackedEntities.containsKey(worldKey)) {
                Set<Entity> entities = hackedEntities.get(worldKey);
                // IHacking#update() will remove any no-longer-applicable hacks from the entity
                entities.forEach(entity -> entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                    if (entity.isAlive() && !hacking.getCurrentHacks().isEmpty()) hacking.update(entity);
                }));
                // Remove the entity from the tracker if it has no more applicable hacks
                entities.removeIf(e -> !e.isAlive() ||
                        e.getCapability(PNCCapabilities.HACKING_CAPABILITY).map(hacking -> hacking.getCurrentHacks().isEmpty()).orElse(true)
                );
            }
        }
    }

    public void trackBlock(World world, BlockPos pos, IHackableBlock iHackable) {
        hackedBlocks.computeIfAbsent(getKey(world), k1 -> new HashMap<>()).put(pos, iHackable);
    }

    public void trackEntity(Entity entity, IHackableEntity iHackable) {
        if (iHackable.getHackableId() != null) {
            entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).ifPresent(hacking -> {
                hacking.addHackable(iHackable);
                hackedEntities.computeIfAbsent(getKey(entity.world), k -> new HashSet<>()).add(entity);
            });
        }
    }

    private ResourceLocation getKey(World w) {
        return w.getDimensionKey().getLocation();
    }
}
