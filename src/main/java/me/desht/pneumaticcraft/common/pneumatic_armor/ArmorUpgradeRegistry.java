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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public enum ArmorUpgradeRegistry {
    INSTANCE;

    private final List<List<IArmorUpgradeHandler<?>>> upgradeHandlers;
    private final Map<ResourceLocation, IArmorUpgradeHandler<?>> byID = new ConcurrentHashMap<>();

    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public static ArmorUpgradeRegistry getInstance() {
        return INSTANCE;
    }

    ArmorUpgradeRegistry() {
        ImmutableList.Builder<List<IArmorUpgradeHandler<?>>> b = ImmutableList.builder();
        for (int i = 0; i < 4; i++) {
            b.add(new CopyOnWriteArrayList<>());
        }
        upgradeHandlers = b.build();
    }

    public synchronized <T extends IArmorUpgradeHandler<?>> T registerUpgradeHandler(T handler) {
        Validate.isTrue(!byID.containsKey(handler.getID()), "handler " + handler.getID() + " is already registered!");

        List<IArmorUpgradeHandler<?>> handlerList = upgradeHandlers.get(handler.getEquipmentSlot().getIndex());
        handler.setIndex(handlerList.size());
        byID.put(handler.getID(), handler);
        handlerList.add(handler);
        return handler;
    }

    public List<IArmorUpgradeHandler<?>> getHandlersForSlot(EquipmentSlot slotType) {
        return upgradeHandlers.get(slotType.getIndex());
    }

    public IArmorUpgradeHandler<?> getUpgradeEntry(ResourceLocation upgradeID) {
        if (upgradeID == null) return null;
        return byID.get(upgradeID);
    }

    public Stream<IArmorUpgradeHandler<?>> entries() {
        return byID.values().stream();
    }
}
