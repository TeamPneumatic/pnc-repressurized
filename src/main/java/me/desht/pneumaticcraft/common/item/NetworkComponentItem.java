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

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.SecurityStationHackingScreen;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class NetworkComponentItem extends Item implements IProgrammable {
    private final NetworkComponentType type;

    public enum NetworkComponentType {
        DIAGNOSTIC_SUBROUTINE("diagnostic_subroutine", true),
        NETWORK_API("network_api", false),
        NETWORK_DATA_STORAGE("network_data_storage", false),
        NETWORK_IO_PORT("network_io_port", true),
        NETWORK_REGISTRY("network_registry", true),
        NETWORK_NODE("network_node", true);

        private final String name;
        private final boolean secStationComponent;

        NetworkComponentType(String name, boolean secStationComponent) {
            this.name = name;
            this.secStationComponent = secStationComponent;
        }

        public boolean isSecStationComponent() {
            return secStationComponent;
        }

        public String getRegistryName() {
            return name;
        }
    }

    public NetworkComponentItem(NetworkComponentType type) {
        super(ModItems.defaultProps());
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> curInfo, TooltipFlag extraInfo) {
        super.appendHoverText(stack, context, curInfo, extraInfo);

        if (context.registries() != null) {
            SecurityStationHackingScreen.addExtraHackInfoStatic(curInfo);
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.getCommandSenderWorld().isClientSide && stack.has(ModDataComponents.SAVED_DRONE_PROGRAM)) {
            entity.setExtendedLifetime();
        }
        return false;
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return type == NetworkComponentType.NETWORK_API || type == NetworkComponentType.NETWORK_DATA_STORAGE;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return type == NetworkComponentType.NETWORK_API;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    public static Optional<NetworkComponentType> getType(ItemStack stack) {
        return stack.getItem() instanceof NetworkComponentItem n ? Optional.ofNullable(n.type) : Optional.empty();
    }

    public static boolean isType(ItemStack stack, NetworkComponentType type) {
        return stack.getItem() instanceof NetworkComponentItem n && n.type == type;
    }
}
