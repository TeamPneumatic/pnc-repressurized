/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.pneumatic_armor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public enum CommonArmorRegistry implements ICommonArmorRegistry {
    INSTANCE;

    public final Map<Class<? extends Entity>, Supplier<? extends IHackableEntity<?>>> hackableEntities = new ConcurrentHashMap<>();
    public final Map<ResourceLocation, Supplier<? extends IHackableEntity<?>>> idToEntityHackables = new ConcurrentHashMap<>();
    // cached list of entity-type -> list of hacks applicable to this entity type
    // - the first of these hacks for which IHackableEntity#canHack() returns true will be the hack that is used
    private final Multimap<EntityType<?>, IHackableEntity<?>> hackablesByType = ArrayListMultimap.create();

    private final Map<Block, Supplier<? extends IHackableBlock>> hackableBlocks = new ConcurrentHashMap<>();
    // blocks known from tags are stored separately; they could change during the game if tags are reloaded
    private final Map<Block, Supplier<? extends IHackableBlock>> hackableTaggedBlocks = new ConcurrentHashMap<>();
    private final Map<TagKey<Block>, Supplier<? extends IHackableBlock>> pendingBlockTags = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Supplier<? extends IHackableBlock>> idToBlockHackables = new ConcurrentHashMap<>();

    public static CommonArmorRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized <T extends IArmorUpgradeHandler<?>> T registerUpgradeHandler(T handler) {
        Validate.notNull(handler, "Upgrade handler can't be null!");
        return ArmorUpgradeRegistry.getInstance().registerUpgradeHandler(handler);
    }

    @Override
    public ICommonArmorHandler getCommonArmorHandler(Player player) {
        return CommonArmorHandler.getHandlerForPlayer(player);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Supplier<? extends IHackableEntity<?>> iHackable) {
        Validate.isTrue(!(iHackable instanceof Entity),
                "Entities that already implement IHackableEntity do not need to be registered as hackable!");

        IHackableEntity<?> hackableEntity = iHackable.get();
        hackableEntity.getHackableId();
        idToEntityHackables.put(hackableEntity.getHackableId(), iHackable);
        hackableEntities.put(entityClazz, iHackable);
    }

    @Override
    public void addHackable(Block block, Supplier<? extends IHackableBlock> iHackable) {
        Validate.isTrue(!(iHackable instanceof Block),
                "Blocks that already implement IHackableBlock do not need to be registered as hackable!");

        IHackableBlock hackableBlock = iHackable.get();
        hackableBlock.getHackableId();
        idToBlockHackables.put(hackableBlock.getHackableId(), iHackable);
        hackableBlocks.put(block, iHackable);
    }

    @Override
    public void addHackable(TagKey<Block> blockTag, Supplier<? extends IHackableBlock> iHackable) {
        Validate.isTrue(!(iHackable instanceof Block), "Blocks that already implement IHackableBlock do not need to be registered as hackable!");

        // can't add these yet because tags aren't populated at this point
        // we'll resolve them later via resolveBlockTags()
        pendingBlockTags.put(blockTag, iHackable);
    }

    @Override
    public Collection<IHackableEntity<?>> getCurrentEntityHacks(Entity entity) {
        return HackManager.getActiveHacks(entity)
                .map(IActiveEntityHacks::getCurrentHacks)
                .orElse(List.of());
    }

    @Override
    public void registerBlockTrackerLootable(BiConsumer<Player, BlockEntity> consumer) {
        BlockTrackLootable.INSTANCE.addLootable(consumer);
    }

    @Override
    public Optional<IArmorUpgradeHandler<?>> getArmorUpgradeHandler(ResourceLocation id) {
        return Optional.ofNullable(ArmorUpgradeRegistry.getInstance().getUpgradeEntry(id));
    }

    /**
     * Non-API.
     * Called when a TagsUpdatedEvent is received, on both server and client, to refresh the actual blocks referred to
     * by the tags we've registered.
     */
    public void resolveBlockTags() {
        hackableTaggedBlocks.clear();

        pendingBlockTags.forEach((tagKey, hackable) -> BuiltInRegistries.BLOCK.getTagOrEmpty(tagKey)
                .forEach(block -> hackableTaggedBlocks.put(block.value(), hackable)));
    }

    public IHackableBlock getHackable(Block block) {
        Supplier<? extends IHackableBlock> sup = hackableBlocks.get(block);
        if (sup != null) return sup.get();
        sup = hackableTaggedBlocks.get(block);
        return sup == null ? null : sup.get();
    }

    public IHackableEntity<?> getHackable(Entity entity, Player player) {
        if (!hackablesByType.containsKey(entity.getType())) {
            for (var entry : hackableEntities.entrySet()) {
                if (entry.getKey().isAssignableFrom(entity.getClass())) {
                    hackablesByType.put(entity.getType(), entry.getValue().get());
                }
            }
        }

        return hackablesByType.get(entity.getType()).stream()
                .filter(hackable -> hackable.canHack(entity, player))
                .findFirst()
                .orElse(null);
    }

    public Optional<IHackableEntity<?>> getHackableEntityForId(ResourceLocation id) {
        Supplier<? extends IHackableEntity<?>> sup = idToEntityHackables.get(id);
        return sup == null ? Optional.empty() : Optional.ofNullable(sup.get());
    }

    public Optional<IHackableBlock> getHackableBlockForId(ResourceLocation id) {
        Supplier<? extends IHackableBlock> sup = idToBlockHackables.get(id);
        return sup == null ? Optional.empty() : Optional.ofNullable(sup.get());
    }
}
