package me.desht.pneumaticcraft.common.util.upgrade;

import com.google.common.primitives.Ints;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ApplicableUpgradesDB {
    INSTANCE;

    // lookup table: 2 * sqrt(n) for 0..25
    private static final float[] VOLUME_MULT = new float[] {
            1f,  // special case ... 2 * sqrt(0) is 0, but we need 1 here
            2f,
            2.82842712474619f,
            3.46410161513775f,
            4f,
            4.47213595499958f,
            4.89897948556636f,
            5.29150262212918f,
            5.65685424949238f,
            6f,
            6.32455532033676f,
            6.6332495807108f,
            6.92820323027551f,
            7.21110255092798f,
            7.48331477354788f,
            7.74596669241483f,
            8f,
            8.24621125123532f,
            8.48528137423857f,
            8.71779788708135f,
            8.94427190999916f,
            9.16515138991168f,
            9.38083151964686f,
            9.59166304662544f,
            9.79795897113271f,
            10f
    };

    // map a registry name to a list of upgrade amount
    // each list is (EnumUpgrade.values().length) in size, and stores the max. number of upgrades allowed for each upgrade
    private final Map<ResourceLocation, List<Integer>> TILE_ENTITIES = new HashMap<>();
    private final Map<ResourceLocation, List<Integer>> ENTITIES = new HashMap<>();
    private final Map<ResourceLocation, List<Integer>> ITEMS = new HashMap<>();

    private final List<Integer> NO_UPGRADES = Ints.asList(new int[EnumUpgrade.values().length]);

    public static ApplicableUpgradesDB getInstance() {
        return INSTANCE;
    }

    public void addApplicableUpgrades(TileEntityType<?> type, UpgradesDBSetup.Builder builder) {
        addUpgrades(TILE_ENTITIES, type.getRegistryName(), builder);
    }

    public void addApplicableUpgrades(EntityType<?> type, UpgradesDBSetup.Builder builder) {
        addUpgrades(ENTITIES, type.getRegistryName(), builder);
    }

    public void addApplicableUpgrades(Item item, UpgradesDBSetup.Builder builder) {
        addUpgrades(ITEMS, item.getRegistryName(), builder);
    }

    public int getMaxUpgrades(TileEntity te, EnumUpgrade upgrade) {
        if (te == null || upgrade == null) return 0;
        return TILE_ENTITIES.getOrDefault(te.getType().getRegistryName(), NO_UPGRADES).get(upgrade.ordinal());
    }

    public int getMaxUpgrades(Entity e, EnumUpgrade upgrade) {
        if (e == null || upgrade == null) return 0;
        return ENTITIES.getOrDefault(e.getType().getRegistryName(), NO_UPGRADES).get(upgrade.ordinal());
    }

    public int getMaxUpgrades(Item item, EnumUpgrade upgrade) {
        if (item == null || upgrade == null) return 0;
        return ITEMS.getOrDefault(item.getRegistryName(), NO_UPGRADES).get(upgrade.ordinal());
    }

    public Map<EnumUpgrade, Integer> getApplicableUpgrades(TileEntity te) {
        return getApplicableUpgrades(TILE_ENTITIES.getOrDefault(te.getType().getRegistryName(), NO_UPGRADES));
    }

    public Map<EnumUpgrade, Integer> getApplicableUpgrades(Entity e) {
        return getApplicableUpgrades(ENTITIES.getOrDefault(e.getType().getRegistryName(), NO_UPGRADES));
    }

    public Map<EnumUpgrade, Integer> getApplicableUpgrades(Item item) {
        return getApplicableUpgrades(ITEMS.getOrDefault(item.getRegistryName(), NO_UPGRADES));
    }

    private Map<EnumUpgrade, Integer> getApplicableUpgrades(List<Integer> l) {
        Map<EnumUpgrade,Integer> res = new EnumMap<>(EnumUpgrade.class);
        for (EnumUpgrade upgrade : EnumUpgrade.values()) {
            int n = l.get(upgrade.ordinal());
            if (n > 0) res.put(upgrade, n);
        }
        return res;
    }

    private void addUpgrades(Map<ResourceLocation,List<Integer>> l, ResourceLocation key, UpgradesDBSetup.Builder builder) {
        List<Integer> u = l.computeIfAbsent(key, k -> createArrayList());
        for (int i = 0; i < EnumUpgrade.values().length; i++) {
            u.set(i, u.get(i) + builder.upgrades().get(i));
        }
    }

    private List<Integer> createArrayList() {
        return Ints.asList(new int[EnumUpgrade.values().length]);
    }

    public int getUpgradedVolume(int baseVolume, int upgradeCount) {
        return (int)(baseVolume * VOLUME_MULT[Math.min(upgradeCount, VOLUME_MULT.length - 1)]);
    }
}
