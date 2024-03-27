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

import me.desht.pneumaticcraft.api.item.ISpawnerCoreStats;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SpawnerCoreItem extends Item implements ColorHandlers.ITintableItem {
    private static final String NBT_SPAWNER_CORE = "pneumaticcraft:SpawnerCoreStats";

    public SpawnerCoreItem() {
        super(ModItems.defaultProps());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        ISpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
        if (stats != null) {
            if (stats.getUnusedPercentage() < 100) {
                stats.getEntities().stream()
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
        ISpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
        if (stats != null) {
            Level world = context.getLevel();
            EntityType<?> type = stats.pickEntity(false);
            if (type == null) return false;
            return PneumaticCraftUtils.getRegistryName(BuiltInRegistries.ENTITY_TYPE, type).map(regName -> {
                Vec3 vec = context.getClickLocation();
                if (world.noCollision(type.getAABB(vec.x(), vec.y(), vec.z()))) {
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
                            stats.addAmount(type, -1);
                            stats.serialize(stack);
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
            ISpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
            if (stats != null) {
                if (stats.getUnusedPercentage() == 100) return 0xFFFFFFFF;
                int t = (int) (ClientUtils.getClientLevel().getGameTime() % 40);
                float b = t < 20 ? Mth.sin(3.1415927f * t / 20f) / 6f : 0;
                return TintColor.HSBtoRGB(71/360f, 1f - stats.getUnusedPercentage() / 100f, 0.83333f + b);
            }
        }
        return 0xFFFFFFFF;
    }

    public static class SpawnerCoreStats implements ISpawnerCoreStats {
        private final Map<EntityType<?>, Integer> entityCounts = new HashMap<>();
        private int unused;

        private SpawnerCoreStats(ItemStack stack) {
            CompoundTag nbt0 = stack.getTag();
            MutableInt total = new MutableInt(0);
            if (nbt0 != null && nbt0.contains(NBT_SPAWNER_CORE)) {
                CompoundTag nbt = nbt0.getCompound(NBT_SPAWNER_CORE);
                for (String k : nbt.getAllKeys()) {
                    BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation(k)).ifPresent(type -> {
                        int amount = nbt.getInt(k);
                        entityCounts.put(type, amount);
                        total.add(amount);
                    });
                }
            }
            unused = Math.max(0, 100 - total.intValue());
        }

        static SpawnerCoreStats forItemStack(ItemStack stack) {
            return stack.getItem() instanceof SpawnerCoreItem ? new SpawnerCoreStats(stack) : null;
        }

        @Override
        public void serialize(ItemStack stack) {
            if (stack.getItem() instanceof SpawnerCoreItem) {
                if (unused == 100) {
                    CompoundTag tag = stack.getTag();
                    if (tag != null) tag.remove(NBT_SPAWNER_CORE);
                } else {
                    CompoundTag subTag = stack.getOrCreateTagElement(NBT_SPAWNER_CORE);
                    entityCounts.forEach((type, amount) -> PneumaticCraftUtils.getRegistryName(BuiltInRegistries.ENTITY_TYPE, type).ifPresent(regName -> {
                        if (amount > 0) {
                            subTag.putInt(regName.toString(), amount);
                        } else {
                            subTag.remove(regName.toString());
                        }
                    }));
                }
            } else {
                throw new IllegalArgumentException("item is not a spawner core!");
            }
        }

        @Override
        public Set<EntityType<?>> getEntities() {
            return entityCounts.keySet();
        }

        @Override
        public int getPercentage(EntityType<?> entityType) {
            return entityCounts.getOrDefault(entityType, 0);
        }

        @Override
        public int getUnusedPercentage() {
            return unused;
        }

        @Override
        public boolean addAmount(EntityType<?> type, int toAdd) {
            int current = entityCounts.getOrDefault(type, 0);
            toAdd = Mth.clamp(toAdd, -current, unused);
            if (toAdd != 0) {
                int newAmount = Mth.clamp(current + toAdd, 0, 100);
                entityCounts.put(type, newAmount);
                unused -= toAdd;
                return true;
            }
            return false;
        }

        @Override
        public EntityType<?> pickEntity(boolean includeUnused) {
            if (unused == 100) return null; // degenerate case
            List<WeightedEntity> weightedEntities = new ArrayList<>();
            entityCounts.forEach((type, amount) -> weightedEntities.add(new WeightedEntity(type, amount)));
            if (includeUnused) weightedEntities.add(new WeightedEntity(null, unused));
            return WeightedRandom.getRandomItem(RandomSource.createNewThreadLocalInstance(), weightedEntities)
                    .map(WeightedEntity::type)
                    .orElse(null);
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

            if (slot == 0) {
                readSpawnerCoreStats();
            }
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);

            readSpawnerCoreStats();
        }

        public ISpawnerCoreStats getStats() {
            return stats;
        }

        private void readSpawnerCoreStats() {
            stats = getStackInSlot(0).isEmpty() ? null : SpawnerCoreStats.forItemStack(getStackInSlot(0));
        }
    }
}
