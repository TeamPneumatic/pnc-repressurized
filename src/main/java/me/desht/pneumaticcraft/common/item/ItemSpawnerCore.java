package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemSpawnerCore extends Item implements ColorHandlers.ITintableItem {
    private static final String NBT_SPAWNER_CORE = "pneumaticcraft:SpawnerCoreStats";

    public ItemSpawnerCore() {
        super(ModItems.defaultProps());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        SpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
        if (stats != null) {
            if (stats.getUnused() < 100) {
                stats.entityCounts.keySet().stream()
                        .sorted(Comparator.comparing(t -> I18n.format(t.getTranslationKey())))
                        .forEach(type -> tooltip.add(GuiConstants.bullet()
                                .append(xlate(type.getTranslationKey()).mergeStyle(TextFormatting.YELLOW))
                                .appendString(": " + stats.entityCounts.get(type) + "%").mergeStyle(TextFormatting.WHITE))
                        );
                tooltip.add(GuiConstants.bullet()
                        .append(xlate("pneumaticcraft.gui.misc.empty").mergeStyle(TextFormatting.YELLOW, TextFormatting.ITALIC))
                        .appendString(": " + stats.getUnused() + "%").mergeStyle(TextFormatting.WHITE));
            } else {
                tooltip.add(xlate("pneumaticcraft.gui.misc.empty").mergeStyle(TextFormatting.YELLOW));
            }
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            return trySpawnEntity(context) ? ActionResultType.CONSUME : ActionResultType.PASS;
        } else {
            return ActionResultType.SUCCESS;
        }
    }

    private boolean trySpawnEntity(ItemUseContext context) {
        ItemStack stack = context.getItem();
        if (stack.getCount() != 1) return false;
        SpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
        if (stats != null) {
            World world = context.getWorld();
            EntityType<?> type = stats.pickEntity(false);
            if (type != null && type.getRegistryName() != null) {
                Vector3d vec = context.getHitVec();
                if (world.hasNoCollisions(type.getBoundingBoxWithSizeApplied(vec.getX(), vec.getY(), vec.getZ()))) {
                    ServerWorld serverworld = (ServerWorld)world;
                    CompoundNBT nbt = new CompoundNBT();
                    nbt.putString("id", type.getRegistryName().toString());
                    Entity entity = EntityType.loadEntityAndExecute(nbt, world, (e1) -> {
                        e1.setLocationAndAngles(vec.getX(), vec.getY(), vec.getZ(), e1.rotationYaw, e1.rotationPitch);
                        return e1;
                    });
                    if (entity != null) {
                        entity.setLocationAndAngles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), world.rand.nextFloat() * 360.0F, 0.0F);
                        if (serverworld.func_242106_g(entity)) {
                            stats.addAmount(type, -1);
                            stats.serialize(stack);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            SpawnerCoreStats stats = SpawnerCoreStats.forItemStack(stack);
            if (stats != null) {
                if (stats.getUnused() == 100) return 0xFFFFFFFF;
                int t = (int) (ClientUtils.getClientWorld().getGameTime() % 40);
                float b = t < 20 ? MathHelper.sin(3.1415927f * t / 20f) / 6f : 0;
                return TintColor.HSBtoRGB(0f, 1f - stats.getUnused() / 100f, 0.83333f + b);
            }
        }
        return 0xFFFFFFFF;
    }

    public static class SpawnerCoreStats {

        private final Map<EntityType<?>, Integer> entityCounts = new HashMap<>();
        private int unused;

        private SpawnerCoreStats(ItemStack stack) {
            CompoundNBT nbt0 = stack.getTag();
            int total = 0;
            if (nbt0 != null && nbt0.contains(NBT_SPAWNER_CORE)) {
                CompoundNBT nbt = nbt0.getCompound(NBT_SPAWNER_CORE);
                for (String k : nbt.keySet()) {
                    EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(k));
                    if (type != null) {
                        int amount = nbt.getInt(k);
                        entityCounts.put(type, amount);
                        total += amount;
                    }
                }
            }
            unused = Math.max(0, 100 - total);
        }

        public static SpawnerCoreStats forItemStack(ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore ? new SpawnerCoreStats(stack) : null;
        }

        public void serialize(ItemStack stack) {
            if (stack.getItem() instanceof ItemSpawnerCore) {
                if (unused == 100) {
                    CompoundNBT tag = stack.getTag();
                    if (tag != null) tag.remove(NBT_SPAWNER_CORE);
                } else {
                    CompoundNBT subTag = stack.getOrCreateChildTag(NBT_SPAWNER_CORE);
                    entityCounts.forEach((type, amount) -> {
                        if (type.getRegistryName() != null) {
                            if (amount > 0) {
                                subTag.putInt(type.getRegistryName().toString(), amount);
                            } else {
                                subTag.remove(type.getRegistryName().toString());
                            }
                        }
                    });
                }
            }
        }

        public int getUnused() {
            return unused;
        }

        public void forEach(BiConsumer<? super EntityType<?>, Integer> c) {
            entityCounts.forEach(c);
        }

        public boolean addAmount(EntityType<?> type, int toAdd) {
            int current = entityCounts.getOrDefault(type, 0);
            toAdd = MathHelper.clamp(toAdd, -current, unused);
            if (toAdd != 0) {
                int newAmount = MathHelper.clamp(current + toAdd, 0, 100);
                entityCounts.put(type, newAmount);
                unused -= toAdd;
                return true;
            }
            return false;
        }

        public EntityType<?> pickEntity(boolean includeUnused) {
            if (unused == 100) return null; // degenerate case
            List<WeightedEntity> weightedEntities = new ArrayList<>();
            entityCounts.forEach((type, amount) -> weightedEntities.add(new WeightedEntity(type, amount)));
            if (includeUnused) weightedEntities.add(new WeightedEntity(null, unused));
            return WeightedRandom.getRandomItem(new Random(), weightedEntities).type;
        }

        private static class WeightedEntity extends WeightedRandom.Item {
            private final EntityType<?> type;

            public WeightedEntity(EntityType<?> type, int itemWeightIn) {
                super(itemWeightIn);
                this.type = type;
            }
        }
    }

    public static class SpawnerCoreItemHandler extends BaseItemStackHandler {
        private SpawnerCoreStats stats;

        public SpawnerCoreItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore;
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
        public void deserializeNBT(CompoundNBT nbt) {
            super.deserializeNBT(nbt);

            readSpawnerCoreStats();
        }

        public SpawnerCoreStats getStats() {
            return stats;
        }

        private void readSpawnerCoreStats() {
            stats = getStackInSlot(0).isEmpty() ? null : SpawnerCoreStats.forItemStack(getStackInSlot(0));
        }
    }
}
