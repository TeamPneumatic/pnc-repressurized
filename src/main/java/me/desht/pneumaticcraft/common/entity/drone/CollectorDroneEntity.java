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

package me.desht.pneumaticcraft.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectorDroneEntity extends AbstractBasicDroneEntity {
    public CollectorDroneEntity(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    public CollectorDroneEntity(Level world, Player player) {
        super(ModEntityTypes.COLLECTOR_DRONE.get(), world, player);
    }

    @Override
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());

        BlockPos invPos = clickPos;

        List<IProgWidget> params = new ArrayList<>();
        int rangeUpgrades = UpgradableItemUtils.getUpgradeCount(droneStack, ModUpgrades.RANGE.get());
        params.add(ProgWidgetArea.fromPosition(pos, 16 + rangeUpgrades * 2));

        Optional<IItemHandler> itemCap = IOHelper.getInventoryForBlock(level().getBlockEntity(clickPos), facing);
        if (itemCap.isPresent()) {
            // placed on a chest; filter on the chest's contents, if any
            Set<Item> filtered = getFilteredItems(itemCap.get());
            if (!filtered.isEmpty()) {
                filtered.forEach(item -> params.add(ProgWidgetItemFilter.withFilter(new ItemStack(item))));
            }
        } else {
            // find a nearby chest to insert to
            invPos = findAdjacentInventory(pos);
        }
        builder.add(new ProgWidgetPickupItem(), params.toArray(new IProgWidget[0]));

        ProgWidgetInventoryExport export = new ProgWidgetInventoryExport();
        boolean[] sides = new boolean[6];
        sides[facing.get3DDataValue()] = true;
        export.setSides(sides);
        builder.add(export, ProgWidgetArea.fromPosition(invPos));

        maybeAddStandbyInstruction(builder, droneStack);

        builder.add(new ProgWidgetWait(), ProgWidgetText.withText("2s"));  // be kind to server

        widgets.addAll(builder.build());

        return true;
    }

    private BlockPos findAdjacentInventory(BlockPos pos) {
        return Arrays.stream(Direction.values())
                .filter(d -> IOHelper.getInventoryForBlock(level().getBlockEntity(pos.relative(d)), d.getOpposite()).isPresent())
                .findFirst()
                .map(pos::relative)
                .orElse(pos);
    }

    private Set<Item> getFilteredItems(IItemHandler handler) {
        return IntStream.range(0, handler.getSlots())
                .filter(i -> !handler.getStackInSlot(i).isEmpty())
                .mapToObj(i -> handler.getStackInSlot(i).getItem())
                .collect(Collectors.toSet());
    }
}
