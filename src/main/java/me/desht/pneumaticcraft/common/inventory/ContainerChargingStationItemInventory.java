package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerChargingStationItemInventory extends ContainerPneumaticBase<TileEntityChargingStation> {

    private ContainerChargingStationItemInventory(ContainerType type, int windowId, PlayerInventory inv, PacketBuffer data) {
        this(type, windowId, inv, getTilePos(data));
    }

    public ContainerChargingStationItemInventory(ContainerType type, int windowId, PlayerInventory inventoryPlayer, BlockPos pos) {
        super(type, windowId, inventoryPlayer, pos);

        TileEntity te = inventoryPlayer.player.world.getTileEntity(pos);
        if (!(te instanceof TileEntityChargingStation)) {
            throw new IllegalArgumentException("tile entity at " + pos + " is not a charging station!");
        }
        TileEntityChargingStation teCS = (TileEntityChargingStation) te;

        if (teCS.getPrimaryInventory().getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).isEmpty())
            throw new IllegalArgumentException("instantiating ContainerChargingStationItemInventory with no chargeable item installed!");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new UpgradeSlot(teCS, i * 3 + j, 31 + j * 18, 24 + i * 18));
            }
        }

        addPlayerSlots(inventoryPlayer, 84);
        addArmorSlots(inventoryPlayer, 9, 8);
    }

    public static ContainerChargingStationItemInventory createMinigunContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
        return new ContainerChargingStationItemInventory(ModContainerTypes.CHARGING_MINIGUN, windowId, inv, data);
    }

    public static ContainerChargingStationItemInventory createDroneContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
        return new ContainerChargingStationItemInventory(ModContainerTypes.CHARGING_DRONE, windowId, inv, data);
    }

    public static ContainerChargingStationItemInventory createArmorContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
        return new ContainerChargingStationItemInventory(ModContainerTypes.CHARGING_ARMOR, windowId, inv, data);
    }

    private static class UpgradeSlot extends SlotItemHandler {
        UpgradeSlot(TileEntityChargingStation te, int slotIndex, int posX, int posY) {
            super(te.getChargeableInventory(), slotIndex, posX, posY);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            ((ChargeableItemHandler) getItemHandler()).writeToNBT();
        }

    }
}
