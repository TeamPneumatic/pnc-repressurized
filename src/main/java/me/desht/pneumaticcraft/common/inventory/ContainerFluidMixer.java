package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerFluidMixer extends ContainerPneumaticBase<TileEntityFluidMixer> {
    public ContainerFluidMixer(int windowId, PlayerInventory inv, BlockPos pos) {
        super(ModContainers.FLUID_MIXER.get(), windowId, inv, pos);

        addSlot(new SlotOutput(te.getPrimaryInventory(), 0, 73, 67));

        for (int i = 0; i < 4; i++) {
            addSlot(new SlotUpgrade(te, i, 98 + i * 18, 106));
        }
        addPlayerSlots(inv, 130);
    }

    public ContainerFluidMixer(int windowId, PlayerInventory invPlayer, PacketBuffer extra) {
        this(windowId, invPlayer, getTilePos(extra));
    }
}
