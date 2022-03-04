package me.desht.pneumaticcraft.common.loot;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Added to various vanilla loot tables by Global Loot Modifiers
 */
public class DungeonLootTable {
    // credit to Ars Nouveau, this is based on their loot modifier system
    public static List<Supplier<ItemStack>> COMMON_LOOT = new ArrayList<>();
    public static List<Supplier<ItemStack>> UNCOMMON_LOOT = new ArrayList<>();
    public static List<Supplier<ItemStack>> RARE_LOOT = new ArrayList<>();

    public static final Random r = ThreadLocalRandom.current();
    static {
        COMMON_LOOT.add(() -> makeStack(ModItems.COMPRESSED_IRON_INGOT.get(), 1, 3));
        COMMON_LOOT.add(() -> makeStack(ModBlocks.COMPRESSED_STONE.get(), 5, 10));
        COMMON_LOOT.add(() -> makeStack(ModItems.LOGISTICS_CORE.get(), 1));
        COMMON_LOOT.add(() -> makeStack(ModBlocks.PRESSURE_TUBE.get(), 8));

        UNCOMMON_LOOT.add(() -> makeStack(ModItems.VORTEX_CANNON.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.SPAWNER_AGITATOR.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.COMPRESSED_IRON_BOOTS.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.COMPRESSED_IRON_LEGGINGS.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.COMPRESSED_IRON_CHESTPLATE.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.COMPRESSED_IRON_HELMET.get(), 1));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.TRANSISTOR.get(), 1, 4));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.CAPACITOR.get(), 1, 4));
        UNCOMMON_LOOT.add(() -> makeStack(ModItems.PNEUMATIC_CYLINDER.get(), 2, 3));
        UNCOMMON_LOOT.add(() -> ammo(ModItems.GUN_AMMO.get()));

        RARE_LOOT.add(() -> makeStack(ModItems.STOP_WORM.get(), 1, 2));
        RARE_LOOT.add(() -> makeStack(ModItems.NUKE_VIRUS.get(), 1, 2));
        RARE_LOOT.add(() -> ammo(ModItems.GUN_AMMO_AP.get()));
        RARE_LOOT.add(() -> ammo(ModItems.GUN_AMMO_FREEZING.get()));
        RARE_LOOT.add(() -> ammo(ModItems.GUN_AMMO_WEIGHTED.get()));
        RARE_LOOT.add(() -> ammo(ModItems.GUN_AMMO_INCENDIARY.get()));
        RARE_LOOT.add(() -> ammo(ModItems.GUN_AMMO_EXPLOSIVE.get()));
        RARE_LOOT.add(() -> makeStack(ModItems.PROGRAMMING_PUZZLE.get(), 4, 12));
        RARE_LOOT.add(() -> makeStack(ModItems.MICROMISSILES.get(), 1));
    }

    private static ItemStack makeStack(ItemLike item, int min, int max) {
        return new ItemStack(item, min + r.nextInt(max + 1 - min));
    }

    private static ItemStack makeStack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    private static ItemStack ammo(AbstractGunAmmoItem ammo) {
        ItemStack ammoStack = new ItemStack(ammo);
        if (r.nextBoolean()) EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.UNBREAKING, r.nextInt(3) + 1), ammoStack);
        return ammoStack;
    }

    public static ItemStack getRandomItem(List<Supplier<ItemStack>> pool){
        return pool.isEmpty() ? ItemStack.EMPTY : pool.get(r.nextInt(pool.size())).get();
    }

    public static List<ItemStack> getRandomRoll(PNCDungeonLootModifier modifier) {
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < modifier.commonRolls; i++) {
            if (r.nextDouble() <= modifier.commonChance)
                stacks.add(getRandomItem(COMMON_LOOT));
        }
        for (int i = 0; i < modifier.uncommonRolls; i++) {
            if (r.nextDouble() <= modifier.uncommonChance)
                stacks.add(getRandomItem(UNCOMMON_LOOT));
        }
        for (int i = 0; i < modifier.rareRolls; i++) {
            if (r.nextDouble() <= modifier.rareChance)
                stacks.add(getRandomItem(RARE_LOOT));
        }
        return stacks;
    }

}

