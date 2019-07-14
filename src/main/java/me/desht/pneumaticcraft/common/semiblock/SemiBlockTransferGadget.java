package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class SemiBlockTransferGadget extends SemiBlockBasic<TileEntity> implements IDirectionalSemiblock {
    private static final int TRANSFER_INTERVAL = 40;
    public static final String ID = "transfer_gadget";
    
    public enum EnumInputOutput{
        INPUT,
        OUTPUT
    }
    
    @DescSynced
    private EnumInputOutput io;
    @DescSynced
    private Direction facing;
    private SemiBlockTransferGadget connectedGadget;
    
    private int counter;
    
    public SemiBlockTransferGadget(){
        super(TileEntity.class);
    }
    
    @Override
    public boolean canStay(){
        return super.canStay() && getConnectedGadget() != null && (!isAir() || !getConnectedGadget().isAir());
    }
    
    @Override
    public void addDrops(NonNullList<ItemStack> drops) {
        // Only drop one of the two halves.
        if(io == EnumInputOutput.INPUT) super.addDrops(drops);
    }
    
    @Override
    public void prePlacement(PlayerEntity player, ItemStack stack, Direction facing){
        super.prePlacement(player, stack, facing);
        io = EnumInputOutput.INPUT;
        this.facing = facing;
    }
    
    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing){
        super.onPlaced(player, stack, facing);

        connectedGadget = new SemiBlockTransferGadget();
        connectedGadget.facing = this.facing.getOpposite();
        connectedGadget.io = EnumInputOutput.OUTPUT;
        SemiBlockManager.getInstance(world).addSemiBlock(world, pos.offset(this.facing), connectedGadget);
    }
    
    @Override
    public boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
        if (getFacing() == side) {
            toggleIO();
            getConnectedGadget().toggleIO();
            return true;
        } else {
            return super.onRightClickWithConfigurator(player, side);
        }
    }
    
    private void toggleIO(){
        io = (io == EnumInputOutput.INPUT ? EnumInputOutput.OUTPUT : EnumInputOutput.INPUT);
    }
    
    @Override
    public boolean canCoexistInSameBlock(ISemiBlock semiBlock){
        return semiBlock instanceof SemiBlockTransferGadget && ((SemiBlockTransferGadget)semiBlock).facing != facing;
    }
    
    private SemiBlockTransferGadget getConnectedGadget(){
        if(connectedGadget == null || connectedGadget.isInvalid()){
            connectedGadget = SemiBlockManager.getInstance(world).getSemiBlocks(SemiBlockTransferGadget.class, world, pos.offset(facing))
                                                                 .filter(gadget -> gadget.facing == facing.getOpposite())
                                                                 .findFirst().orElse(null);
        }
        return connectedGadget;
    }
    
    @Override
    public void tick(){
        super.tick();
        
        if(!world.isRemote && !isInvalid() && io == EnumInputOutput.INPUT && ++counter >= TRANSFER_INTERVAL){
            counter = 0;
            transfer();
        }
    }
    
    private void transfer(){
        TileEntity inputTE = getTileEntity();
        TileEntity outputTE = getConnectedGadget().getTileEntity();
        if(inputTE == null || outputTE == null) return;
        tryTransferItem(inputTE, outputTE); 
        tryTransferFluid(inputTE, outputTE);
    }

    private void tryTransferItem(TileEntity inputTE, TileEntity outputTE){
        inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)
                .ifPresent(input -> outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                        .ifPresent(output -> IOHelper.transferOneItem(input, output)));
    }
    
    private void tryTransferFluid(TileEntity inputTE, TileEntity outputTE){
        inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)
                .ifPresent(input -> outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())
                        .ifPresent(output -> FluidUtil.tryFluidTransfer(output, input, 100, true)));
    }

    @Override
    public Direction getFacing(){
        return facing;
    }
    
    public EnumInputOutput getInputOutput(){
        return io;
    }
    
    @Override
    public void writeToNBT(CompoundNBT tag){
        super.writeToNBT(tag);
        tag.putInt("counter", counter);
        tag.putByte("facing", (byte)facing.getIndex());
        tag.putBoolean("input", io == EnumInputOutput.INPUT);
    }
    
    @Override
    public void readFromNBT(CompoundNBT tag){
        super.readFromNBT(tag);
        counter = tag.getInt("counter");
        facing = Direction.byIndex(tag.getByte("facing"));
        io = tag.getBoolean("input") ? EnumInputOutput.INPUT : EnumInputOutput.OUTPUT;
    }
}
