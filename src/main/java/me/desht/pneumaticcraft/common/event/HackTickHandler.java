package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HackTickHandler {
    private final Map<GlobalPos, IHackableBlock> hackedBlocks = new HashMap<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Iterator<Map.Entry<GlobalPos, IHackableBlock>> blockIterator = hackedBlocks.entrySet().iterator();
            while (blockIterator.hasNext()) {
                Map.Entry<GlobalPos, IHackableBlock> entry = blockIterator.next();
                IHackableBlock hackableBlock = entry.getValue();
                GlobalPos gPos = entry.getKey();
                World world = PneumaticCraftUtils.getWorldForGlobalPos(gPos);

                boolean found = false;
                for (Map.Entry<Block, Class<? extends IHackableBlock>> registeredEntry : PneumaticHelmetRegistry.getInstance().hackableBlocks.entrySet()) {
                    if (hackableBlock.getClass() == registeredEntry.getValue()) {
                        if (world.getBlockState(gPos.getPos()).getBlock() == registeredEntry.getKey()) {
                            if (!hackableBlock.afterHackTick(world, gPos.getPos())) {
                                blockIterator.remove();
                            }
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) blockIterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            try {
                for (Entity entity : PneumaticCraftRepressurized.proxy.getAllEntities(event.world)) {
                    entity.getCapability(PneumaticRegistry.HACKING_CAPABILITY, null).ifPresent(h -> {
                        if (!h.getCurrentHacks().isEmpty()) h.update(entity);
                    });
                }
            } catch (Throwable e) {
                // Catching a CME which I have no clue on what might cause it.
            }
        }
    }

    public void trackBlock(GlobalPos coord, IHackableBlock iHackable) {
        hackedBlocks.put(coord, iHackable);
    }

    public void trackEntity(Entity entity, IHackableEntity iHackable) {
        if (iHackable.getId() != null) {
            entity.getCapability(PneumaticRegistry.HACKING_CAPABILITY, null).ifPresent(h -> h.addHackable(iHackable));
        }
    }
}
