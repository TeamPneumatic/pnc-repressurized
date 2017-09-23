package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerGasLift extends ContainerPneumaticBase<TileEntityGasLift> {

    public ContainerGasLift(InventoryPlayer inventoryPlayer, TileEntityGasLift te) {
        super(te);

        addUpgradeSlots(11, 29);

        addSlotToContainer(new SlotInventoryLimiting(te, 0, 55, 48));

        addPlayerSlots(inventoryPlayer, 84);

//        if (te.getTankInfo(EnumFacing.UP)[0].fluid != null && te.getTankInfo(EnumFacing.UP)[0].fluid.getFluid() == Fluids.OIL) {
//            AchievementHandler.giveAchievement(inventoryPlayer.player, new ItemStack(Fluids.getBucket(Fluids.OIL)));
//        }
    }

//    @Override
//    public boolean canInteractWith(EntityPlayer player) {
//        return te.isGuiUseableByPlayer(player);
//    }

}
