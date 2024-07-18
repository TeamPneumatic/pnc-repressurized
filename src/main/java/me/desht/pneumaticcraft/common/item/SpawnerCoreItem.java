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

package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SpawnerCoreItem extends Item implements ColorHandlers.ITintableItem {
    public SpawnerCoreItem() {
        super(ModItems.defaultProps().component(ModDataComponents.SPAWNER_CORE_STATS, SpawnerCoreStats.EMPTY));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        ISpawnerCoreStats stats = stack.getOrDefault(ModDataComponents.SPAWNER_CORE_STATS, SpawnerCoreStats.EMPTY);
        if (stats.getUnusedPercentage() < 100) {
            stats.getEntities().keySet().stream()
                    .sorted(Comparator.comparing(t -> I18n.get(t.getDescriptionId())))
                    .forEach(type -> tooltip.add(Symbols.bullet()
                            .append(xlate(type.getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                            .append(": " + stats.getPercentage(type) + "%").withStyle(ChatFormatting.WHITE))
                    );
            if (stats.getUnusedPercentage() > 0) {
                tooltip.add(Symbols.bullet()
                        .append(xlate("pneumaticcraft.gui.misc.empty").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                        .append(": " + stats.getUnusedPercentage() + "%").withStyle(ChatFormatting.WHITE));
            }
        } else {
            tooltip.add(xlate("pneumaticcraft.gui.misc.empty").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            return trySpawnEntity(context) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    private boolean trySpawnEntity(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (stack.getCount() != 1) return false;
        ISpawnerCoreStats stats = stack.get(ModDataComponents.SPAWNER_CORE_STATS);
        if (stats != null) {
            Level world = context.getLevel();
            EntityType<?> type = stats.pickEntity(false);
            if (type == null) return false;
            return PneumaticCraftUtils.getRegistryName(BuiltInRegistries.ENTITY_TYPE, type).map(regName -> {
                Vec3 vec = context.getClickLocation();
                if (world.noCollision(type.getSpawnAABB(vec.x(), vec.y(), vec.z()))) {
                    ServerLevel serverworld = (ServerLevel)world;
                    CompoundTag nbt = new CompoundTag();
                    nbt.putString("id", regName.toString());
                    Entity entity = EntityType.loadEntityRecursive(nbt, world, (e1) -> {
                        e1.moveTo(vec.x(), vec.y(), vec.z(), e1.getYRot(), e1.getXRot());
                        return e1;
                    });
                    if (entity != null) {
                        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
                        if (serverworld.tryAddFreshEntityWithPassengers(entity)) {
                            stats.addAmount(type, -1).save(stack);
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            ISpawnerCoreStats stats = stack.get(ModDataComponents.SPAWNER_CORE_STATS);
            if (stats != null) {
                if (stats.getUnusedPercentage() == 100) {
                    return 0xFFFFFFFF;
                }
                int t = (int) (ClientUtils.getClientLevel().getGameTime() % 40);
                float b = t < 20 ? Mth.sin(3.1415927f * t / 20f) / 6f : 0;
                return TintColor.HSBtoRGB(71/360f, 1f - stats.getUnusedPercentage() / 100f, 0.83333f + b);
            }
        }
        return 0xFFFFFFFF;
    }

    public record SpawnerCoreStats(Map<EntityType<?>, Integer> map, int unused) implements ISpawnerCoreStats {
        public static final Codec<ISpawnerCoreStats> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), ExtraCodecs.intRange(0, 100))
                        .fieldOf("map").forGetter(ISpawnerCoreStats::getEntities),
                ExtraCodecs.intRange(0, 100)
                        .fieldOf("unused").forGetter(ISpawnerCoreStats::getUnusedPercentage)
        ).apply(builder, SpawnerCoreStats::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Map<EntityType<?>, Integer>> ENTITY_MAP
                = ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.registry(Registries.ENTITY_TYPE), ByteBufCodecs.VAR_INT);

        public static final StreamCodec<RegistryFriendlyByteBuf, ISpawnerCoreStats> STREAM_CODEC = StreamCodec.composite(
                ENTITY_MAP, ISpawnerCoreStats::getEntities,
                ByteBufCodecs.VAR_INT, ISpawnerCoreStats::getUnusedPercentage,
                SpawnerCoreStats::new
        );

        public static final SpawnerCoreStats EMPTY = new SpawnerCoreStats(Map.of(), 100);

        @Override
        public Map<EntityType<?>, Integer> getEntities() {
            return Collections.unmodifiableMap(map);
        }

        @Override
        public int getPercentage(EntityType<?> entityType) {
            return map.getOrDefault(entityType, 0);
        }

        @Override
        public int getUnusedPercentage() {
            return unused;
        }

        @Override
        public ISpawnerCoreStats addAmount(EntityType<?> type, int toAdd) {
            var entityCounts = new HashMap<>(map);
            int newUnused = unused;

            int current = getPercentage(type);
            toAdd = Mth.clamp(toAdd, -current, unused);
            if (toAdd != 0) {
                int newAmount = Mth.clamp(current + toAdd, 0, 100);
                entityCounts.put(type, newAmount);
                newUnused -= toAdd;
                return new SpawnerCoreStats(Map.copyOf(entityCounts), newUnused);
            }
            return this;
        }

        @Override
        public void save(ItemStack stack) {
            stack.set(ModDataComponents.SPAWNER_CORE_STATS, this);
        }

        @Override
        public EntityType<?> pickEntity(boolean includeUnused) {
            if (unused == 100) return null; // degenerate case

            List<WeightedEntity> weightedEntities = new ArrayList<>();
            map.forEach((type, amount) -> weightedEntities.add(new WeightedEntity(type, amount)));
            if (includeUnused && unused > 0) {
                weightedEntities.add(new WeightedEntity(null, unused));
            }

            return WeightedRandom.getRandomItem(RandomSource.createNewThreadLocalInstance(), weightedEntities)
                    .map(WeightedEntity::type)
                    .orElse(null);
        }

        @Override
        public ISpawnerCoreStats empty() {
            return EMPTY;
        }

        private record WeightedEntity(EntityType<?> type, int weight) implements WeightedEntry {
            @Override
            public Weight getWeight() {
                return Weight.of(weight);
            }
        }
    }

    public static class SpawnerCoreItemHandler extends BaseItemStackHandler {
        private ISpawnerCoreStats stats;

        public SpawnerCoreItemHandler(BlockEntity owner) {
            super(owner, 1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof SpawnerCoreItem;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            stats = null;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            super.deserializeNBT(provider, nbt);

            stats = null;
        }

        @Override
        public void loadContainerContents(@Nullable ItemContainerContents contents) {
            super.loadContainerContents(contents);

            stats = null;
        }

        @NotNull
        public ISpawnerCoreStats getStats() {
            if (stats == null) {
                stats = getStackInSlot(0).isEmpty() ?
                        SpawnerCoreStats.EMPTY :
                        getStackInSlot(0).getOrDefault(ModDataComponents.SPAWNER_CORE_STATS, SpawnerCoreStats.EMPTY);
            }
            return stats;
        }

        public boolean isCorePresent() {
            return getStackInSlot(0).getItem() instanceof SpawnerCoreItem;
        }
    }
}
