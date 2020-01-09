package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.network.SyncedField;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class ContainerEnergy<T extends TileEntityBase> extends Container4UpgradeSlots<T> {

    private ContainerEnergy(ContainerType type, int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(type, i, playerInventory, getTilePos(buffer));
    }

    public ContainerEnergy(ContainerType type, int i, PlayerInventory playerInventory, BlockPos tilePos) {
        super(type, i, playerInventory, tilePos);

        if (!te.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
            throw new IllegalStateException("tile entity must support CapabilityEnergy.ENERGY on face null!");
        }
        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
            try {
                addSyncedField(new SyncedField.SyncedInt(h, EnergyStorage.class.getDeclaredField("energy")));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }

    public static Container createPneumaticDynamoContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerEnergy(ModContainers.PNEUMATIC_DYNAMO, i, playerInventory, buffer);
    }
}
