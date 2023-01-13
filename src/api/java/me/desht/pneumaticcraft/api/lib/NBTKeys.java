/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.lib;

public class NBTKeys {
    public static final String PNEUMATIC_HELMET_DEBUGGING_DRONE = "debuggingDrone";
    public static final String PNEUMATIC_HELMET_DEBUGGING_PC = "debuggingPC";

    // Saved on tile entities and also serialized to itemstacks
    public static final String NBT_UPGRADE_INVENTORY = "UpgradeInventory";
    public static final String NBT_AIR_AMOUNT = "AirAmount";
    public static final String NBT_SAVED_TANKS = "SavedTanks";
    public static final String NBT_HEAT_EXCHANGER = "HeatExchanger";
    public static final String NBT_AIR_HANDLER = "AirHandler";
    public static final String NBT_SIDE_CONFIG = "SideConfiguration";
    public static final String NBT_EXTRA = "ExtraData";
    public static final String NBT_REDSTONE_MODE = "redstoneMode";
    public static final String NBT_BROKEN = "IsBroken";

    // Standard tag for saving an item inventory
    public static final String NBT_ITEM_INV = "Items";

    // this is the tag vanilla uses to serialize BE data onto dropped items
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    // this is the tag vanilla uses to serialize entity data onto items
    public static final String ENTITY_TAG = "EntityTag";
}
