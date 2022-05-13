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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public enum CommonArmorRegistry implements ICommonArmorRegistry {
    INSTANCE;

    public final Map<Class<? extends Entity>, Supplier<? extends IHackableEntity>> hackableEntities = new ConcurrentHashMap<>();
    public final Map<ResourceLocation, Supplier<? extends IHackableEntity>> idToEntityHackables = new ConcurrentHashMap<>();

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
    public void addHackable(TagKey<Block> blockTag, Supplier<? extends IHackableBlock> iHackable) {
        Validate.isTrue(!(iHackable instanceof Block), "Blocks that already implement IHackableBlock do not need to be registered as hackable!");

        // can't add these yet because tags aren't populated at this point
        // we'll resolve them later via resolveBlockTags()
        pendingBlockTags.put(blockTag, iHackable);
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity) {
        return entity.getCapability(PNCCapabilities.HACKING_CAPABILITY).map(IHacking::getCurrentHacks).orElse(Collections.emptyList());
    }

    /**
     * Non-API.
     * Called when a TagsUpdatedEvent is received, on both server and client, to refresh the actual blocks referred to
     * by the tags we've registered.
     */
    public void resolveBlockTags() {
        hackableTaggedBlocks.clear();

        pendingBlockTags.forEach((tagKey, hackable) -> Registry.BLOCK.getTagOrEmpty(tagKey).forEach(h -> hackableTaggedBlocks.put(h.value(), hackable)));
    }


    public IHackableBlock getHackable(Block block) {
        Supplier<? extends IHackableBlock> sup = hackableBlocks.get(block);
        if (sup != null) return sup.get();
        sup = hackableTaggedBlocks.get(block);
        return sup == null ? null : sup.get();
    }

    public IHackableEntity getHackable(Entity entity, Player player) {
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
