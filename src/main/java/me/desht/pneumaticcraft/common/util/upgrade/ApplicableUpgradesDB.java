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

package me.desht.pneumaticcraft.common.util.upgrade;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Map;

public enum ApplicableUpgradesDB {
    INSTANCE;

    private final Table<BlockEntityType<?>, PNCUpgrade, Integer> TILE_ENTITIES = HashBasedTable.create();
    private final Table<EntityType<?>, PNCUpgrade, Integer> ENTITIES = HashBasedTable.create();
    private final Table<Item, PNCUpgrade, Integer> ITEMS = HashBasedTable.create();

    public static ApplicableUpgradesDB getInstance() {
        return INSTANCE;
    }

    public void addApplicableUpgrades(BlockEntityType<?> type, UpgradesDBSetup.Builder builder) {
        addUpgrades(TILE_ENTITIES, type, builder);
    }

    public void addApplicableUpgrades(EntityType<?> type, UpgradesDBSetup.Builder builder) {
        addUpgrades(ENTITIES, type, builder);
    }

    public void addApplicableUpgrades(Item item, UpgradesDBSetup.Builder builder) {
        addUpgrades(ITEMS, item, builder);
    }

    public int getMaxUpgrades(BlockEntity te, PNCUpgrade upgrade) {
        if (te == null || upgrade == null) return 0;
        Integer max = TILE_ENTITIES.get(te.getType(), upgrade);
        return max == null ? 0 : max;
    }

    public int getMaxUpgrades(Entity e, PNCUpgrade upgrade) {
        if (e == null || upgrade == null) return 0;
        Integer max = ENTITIES.get(e.getType(), upgrade);
        return max == null ? 0 : max;
    }

    public int getMaxUpgrades(Item item, PNCUpgrade upgrade) {
        if (item == null || upgrade == null) return 0;
        Integer max = ITEMS.get(item, upgrade);
        return max == null ? 0 : max;
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(BlockEntity te) {
        return TILE_ENTITIES.row(te.getType());
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(Entity e) {
        return ENTITIES.row(e.getType());
    }

    public Map<PNCUpgrade, Integer> getApplicableUpgrades(Item item) {
        return ITEMS.row(item);
    }

    private <T extends ForgeRegistryEntry<?>> void addUpgrades(Table<T,PNCUpgrade,Integer> table, T entry, UpgradesDBSetup.Builder builder) {
        builder.getUpgrades().forEach((upgrade, max) -> table.put(entry, upgrade, max));
    }
}
