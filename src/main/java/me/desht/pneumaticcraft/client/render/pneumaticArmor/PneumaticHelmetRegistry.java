package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.*;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.CapabilityHackingProvider;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

import java.util.*;

public class PneumaticHelmetRegistry implements IPneumaticHelmetRegistry {

    private static final PneumaticHelmetRegistry INSTANCE = new PneumaticHelmetRegistry();
    public final List<Class<? extends IEntityTrackEntry>> entityTrackEntries = new ArrayList<Class<? extends IEntityTrackEntry>>();
    public final Map<Class<? extends Entity>, Class<? extends IHackableEntity>> hackableEntities = new HashMap<Class<? extends Entity>, Class<? extends IHackableEntity>>();
    public final Map<Block, Class<? extends IHackableBlock>> hackableBlocks = new HashMap<Block, Class<? extends IHackableBlock>>();
    public final Map<String, Class<? extends IHackableEntity>> stringToEntityHackables = new HashMap<>();
    public final Map<String, Class<? extends IHackableBlock>> stringToBlockHackables = new HashMap<>();

    public static PneumaticHelmetRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Class<? extends IEntityTrackEntry> entry) {
        if (entry == null) throw new NullPointerException("Can't register null!");
        entityTrackEntries.add(entry);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Class<? extends IHackableEntity> iHackable) {
        if (entityClazz == null) throw new NullPointerException("Entity class is null!");
        if (iHackable == null) throw new NullPointerException("IHackableEntity is null!");
        if (Entity.class.isAssignableFrom(iHackable)) {
            Log.warning("Entities that implement IHackableEntity shouldn't be registered as hackable! Registering entity: " + entityClazz.getCanonicalName());
        } else {
            try {
                IHackableEntity hackableEntity = iHackable.newInstance();
                if (hackableEntity.getId() != null) stringToEntityHackables.put(hackableEntity.getId(), iHackable);
                hackableEntities.put(entityClazz, iHackable);
            } catch (InstantiationException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addHackable(Block block, Class<? extends IHackableBlock> iHackable) {
        if (block == null) throw new NullPointerException("Block is null! class = " + iHackable);
        if (iHackable == null) throw new NullPointerException("IHackableBlock is null! block = " + block.getRegistryName());

        if (Block.class.isAssignableFrom(iHackable)) {
            Log.warning("Blocks that implement IHackableBlock shouldn't be registered as hackable! Registering block: " + block.getLocalizedName());
        } else {
            try {
                IHackableBlock hackableBlock = iHackable.newInstance();
                if (hackableBlock.getId() != null) stringToBlockHackables.put(hackableBlock.getId(), iHackable);
                hackableBlocks.put(block, iHackable);
            } catch (InstantiationException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity) {
        IHacking hacking = entity.getCapability(CapabilityHackingProvider.HACKING_CAPABILITY, null);
        if (hacking != null) {
            return hacking.getCurrentHacks();
        } else {
            Log.warning("Hacking capability couldn't be found in the entity " + entity.getName());
        }
        return Collections.emptyList();

    }

    @Override
    public void registerBlockTrackEntry(IBlockTrackEntry entry) {
        BlockTrackEntryList.instance.trackList.add(entry);
    }

    @Override
    public void registerRenderHandler(IUpgradeRenderHandler renderHandler) {
        if (renderHandler == null) throw new NullPointerException("Render handler can't be null!");
        UpgradeRenderHandlerList.instance().upgradeRenderers.add(renderHandler);
    }
}
