package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.function.Supplier;

public class PneumaticHelmetRegistry implements IPneumaticHelmetRegistry {
    private static final PneumaticHelmetRegistry INSTANCE = new PneumaticHelmetRegistry();

    public final List<Supplier<? extends IEntityTrackEntry>> entityTrackEntries = new ArrayList<>();

    public final Map<Class<? extends Entity>, Supplier<? extends IHackableEntity>> hackableEntities = new HashMap<>();
    public final Map<ResourceLocation, Supplier<? extends IHackableEntity>> idToEntityHackables = new HashMap<>();

    private final Map<Block, Supplier<? extends IHackableBlock>> hackableBlocks = new HashMap<>();
    // blocks known from tags are stored separately; they could change during the game if tags are reloaded
    private final Map<Block, Supplier<? extends IHackableBlock>> hackableTaggedBlocks = new HashMap<>();
    private final Map<ResourceLocation, Supplier<? extends IHackableBlock>> pendingBlockTags = new HashMap<>();
    private final Map<ResourceLocation, Supplier<? extends IHackableBlock>> idToBlockHackables = new HashMap<>();

    public static PneumaticHelmetRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry) {
        Validate.notNull(entry);
        entityTrackEntries.add(entry);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Supplier<? extends IHackableEntity> iHackable) {
        Validate.isTrue(!(iHackable instanceof Entity),
                "Entities that already implement IHackableEntity do not need to be registered as hackable!");

        IHackableEntity hackableEntity = iHackable.get();
        if (hackableEntity.getHackableId() != null) idToEntityHackables.put(hackableEntity.getHackableId(), iHackable);
        hackableEntities.put(entityClazz, iHackable);
    }

    @Override
    public void addHackable(Block block, Supplier<? extends IHackableBlock> iHackable) {
        Validate.isTrue(!(iHackable instanceof Block),
                "Blocks that already implement IHackableBlock do not need to be registered as hackable!");

        IHackableBlock hackableBlock = iHackable.get();
        if (hackableBlock.getHackableId() != null) idToBlockHackables.put(hackableBlock.getHackableId(), iHackable);
        hackableBlocks.put(block, iHackable);
    }

    @Override
    public void addHackable(Tag<Block> blockTag, Supplier<? extends IHackableBlock> iHackable) {
        Validate.isTrue(!(iHackable instanceof Block), "Blocks that already implement IHackableBlock do not need to be registered as hackable!");

        // can't add these yet because tags aren't populated at this point
        // we'll resolve them later via resolveBlockTags()
        pendingBlockTags.put(blockTag.getId(), iHackable);
    }

    /**
     * Called when a TagsUpdatedEvent is received, on both server and client, to refresh the actual blocks referred to
     * by the tags we've registered.
     *
     * @param tags the newly updated block tags
     */
    public void resolveBlockTags(TagCollection<Block> tags) {
        hackableTaggedBlocks.clear();
        pendingBlockTags.forEach((id, hackable) -> tags.get(id).getAllElements().forEach(block -> hackableTaggedBlocks.put(block, hackable)));
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity) {
        return entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).map(IHacking::getCurrentHacks).orElse(Collections.emptyList());
    }

    @Override
    public void registerBlockTrackEntry(IBlockTrackEntry entry) {
        BlockTrackEntryList.instance.trackList.add(entry);
    }

    @Override
    public void registerRenderHandler(IUpgradeRenderHandler renderHandler) {
        Validate.notNull(renderHandler, "Render handler can't be null!");
        UpgradeRenderHandlerList.instance().addUpgradeRenderer(renderHandler);
    }

    public IHackableBlock getHackable(Block block) {
        Supplier<? extends IHackableBlock> sup = hackableBlocks.get(block);
        if (sup != null) return sup.get();
        sup = hackableTaggedBlocks.get(block);
        return sup == null ? null : sup.get();
    }

    public IHackableEntity getHackable(Entity entity, PlayerEntity player) {
        for (Map.Entry<Class<? extends Entity>, Supplier<? extends IHackableEntity>> entry : hackableEntities.entrySet()) {
            if (entry.getKey().isAssignableFrom(entity.getClass())) {
                IHackableEntity hackable = entry.getValue().get();
                if (hackable.canHack(entity, player)) {
                    return hackable;
                }
            }
        }
        return null;
    }

    public IHackableEntity getEntityById(ResourceLocation id) {
        Supplier<? extends IHackableEntity> sup = idToEntityHackables.get(id);
        return sup == null ? null : sup.get();
    }
}
