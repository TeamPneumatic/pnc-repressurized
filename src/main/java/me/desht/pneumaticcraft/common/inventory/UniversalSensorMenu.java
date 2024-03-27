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

package me.desht.pneumaticcraft.common.inventory;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.block.entity.UniversalSensorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Collections;
import java.util.List;

public class UniversalSensorMenu extends AbstractPneumaticCraftMenu<UniversalSensorBlockEntity> {
    private final List<String> globalVars;

    public UniversalSensorMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.UNIVERSAL_SENSOR.get(), windowId, playerInventory, pos);

        globalVars = Collections.emptyList();

        commonInit(playerInventory);
    }

    public UniversalSensorMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(ModMenuTypes.UNIVERSAL_SENSOR.get(), windowId, playerInventory, buffer.readBlockPos());

        int nVars = buffer.readVarInt();
        ImmutableList.Builder<String> b = ImmutableList.builder();
        for (int i = 0; i < nVars; i++) {
            b.add(buffer.readUtf());
        }
        globalVars = b.build();

        commonInit(playerInventory);
    }

    private void commonInit(Inventory playerInventory) {
        addUpgradeSlots(19, 108);

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 29, 72));

        addPlayerSlots(playerInventory, 157);
    }

    public List<String> getGlobalVars() {
        return globalVars;
    }
}
