package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemSpawnerCore extends Item {
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
                stats.forEach((type, amount) -> tooltip.add(GuiConstants.bullet().append(xlate(type.getTranslationKey())).appendString(": " + amount + "%")));
                tooltip.add(GuiConstants.bullet().appendString("Unused: " + stats.getUnused() + "%"));
            } else {
                tooltip.add(xlate("pneumaticcraft.gui.tooltip.spawner_core.empty"));
            }
        }
    }

    public static class SpawnerCoreStats {
        private final Map<EntityType<?>, Integer> entityCounts = new HashMap<>();
        private int unused;

        private SpawnerCoreStats(ItemStack stack) {
            CompoundNBT nbt = stack.getOrCreateChildTag(NBT_SPAWNER_CORE);
            int total = 0;
            for (String k : nbt.keySet()) {
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(k));
                if (type != null) {
                    int amount = nbt.getInt(k);
                    entityCounts.put(type, amount);
                    total += amount;
                }
            }
            unused = 100 - total;
        }

        public static SpawnerCoreStats forItemStack(ItemStack stack) {
            return stack.getItem() instanceof ItemSpawnerCore ? new SpawnerCoreStats(stack) : null;
        }

        public static void serialize(SpawnerCoreStats stats, ItemStack stack) {
            if (stack.getItem() instanceof ItemSpawnerCore) {
                CompoundNBT nbt = stack.getOrCreateChildTag(NBT_SPAWNER_CORE);
                stats.entityCounts.forEach((type, amount) -> nbt.putInt(Objects.requireNonNull(type.getRegistryName()).toString(), amount));
            }
        }

        public int getUnused() {
            return unused;
        }

        public void forEach(BiConsumer<? super EntityType<?>, ? super Integer> consumer) {
            entityCounts.forEach(consumer);
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
    }
}
