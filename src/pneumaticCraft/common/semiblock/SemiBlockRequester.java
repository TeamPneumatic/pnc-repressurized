package pneumaticCraft.common.semiblock;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.proxy.CommonProxy;
import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileInterface;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;

@Optional.InterfaceList({@Interface(iface = "appeng.api.networking.IGridHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.IGridBlock", modid = ModIds.AE2)})
public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester, IGridHost, IGridBlock{

    public static final String ID = "logisticFrameRequester";
    private Object gridNode;

    @Override
    public int getColor(){
        return 0xFF0000FF;
    }

    @Override
    public int amountRequested(ItemStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
            int count = 0;
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if(s != null && isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                for(ItemStack s : incomingStacks.keySet()) {
                    if(isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                int requested = Math.max(0, Math.min(stack.stackSize, totalRequestingAmount - count));
                return requested;
            }
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack){
        int requesting = 0;
        for(int i = 0; i < getFilters().getSizeInventory(); i++) {
            ItemStack requestingStack = getFilters().getStackInSlot(i);
            if(requestingStack != null && isItemEqual(stack, requestingStack)) {
                requesting += requestingStack.stackSize;
            }
        }
        return requesting;
    }

    @Override
    public int amountRequested(FluidStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            TileEntity te = getTileEntity();
            if(te instanceof IFluidHandler) {

                int count = 0;

                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    FluidTankInfo[] infos = ((IFluidHandler)te).getTankInfo(d);
                    if(infos != null) {
                        for(FluidTankInfo info : infos) {
                            if(info.fluid != null && info.fluid.getFluid() == stack.getFluid()) {
                                count += info.fluid.amount;
                            }
                        }
                        if(count > 0) break;
                    }
                }

                for(FluidStackWrapper s : incomingFluid.keySet()) {
                    if(s.stack.getFluid() == stack.getFluid()) {
                        count += s.stack.amount;
                    }
                }
                int requested = Math.max(0, Math.min(stack.amount, totalRequestingAmount - count));
                return requested;
            }

        }
        return 0;
    }

    private int getTotalRequestedAmount(FluidStack stack){
        int requesting = 0;
        for(int i = 0; i < 9; i++) {
            FluidStack requestingStack = getTankFilter(i).getFluid();
            if(requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.amount;
            }
        }
        return requesting;
    }

    @Override
    public int getPriority(){
        return 2;
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_LOGISTICS_REQUESTER;
    }

    @Override
    public boolean canFilterStack(){
        return true;
    }

    /*
     ****************************************** Applied Energistics 2 Integration ***************************************************************
     */

    @Override
    public void initialize(World world, ChunkPosition pos){
        super.initialize(world, pos);
        if(Loader.isModLoaded(ModIds.AE2) && !world.isRemote) checkForInterface();
    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(gridNode != null) {
            disconnectFromInterface();
        }
    }

    private void checkForInterface(){
        TileEntity te = getTileEntity();
        if(te instanceof TileInterface) {
            try {
                AEApi.instance().createGridConnection(getGridNode(null), ((TileInterface)te).getGridNode(null));
                Log.info("connection created");
            } catch(FailedConnection e) {
                Log.info("Couldn't connect to an ME Interface!");
                e.printStackTrace();
            }
        }
    }

    private void disconnectFromInterface(){
        ((IGridNode)gridNode).destroy();
    }

    //IGridHost
    @Override
    @Optional.Method(modid = ModIds.AE2)
    public AECableType getCableConnectionType(ForgeDirection arg0){
        return AECableType.NONE;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridNode getGridNode(ForgeDirection d){
        if(gridNode == null) {
            gridNode = AEApi.instance().createGridNode(this);
        }
        return (IGridNode)gridNode;
    }

    @Override
    public void securityBreak(){
        drop();
    }

    //IGridBlock
    @Override
    public EnumSet<ForgeDirection> getConnectableSides(){
        return null;//Shouldn't be called as isWorldAccessible is false.
    }

    @Override
    public EnumSet<GridFlags> getFlags(){
        return EnumSet.noneOf(GridFlags.class);
    }

    @Override
    public AEColor getGridColor(){
        return AEColor.Transparent;
    }

    @Override
    public double getIdlePowerUsage(){
        return 0;
    }

    @Override
    public DimensionalCoord getLocation(){
        return new DimensionalCoord(world, getX(), getY(), getZ());
    }

    @Override
    public IGridHost getMachine(){
        return this;
    }

    @Override
    public ItemStack getMachineRepresentation(){
        return new ItemStack(Itemss.logisticsFrameRequester);
    }

    @Override
    public void gridChanged(){
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWorldAccessible(){
        return false;
    }

    @Override
    public void onGridNotification(GridNotification arg0){
        // TODO Auto-generated method stub

    }

    @Override
    public void setNetworkStatus(IGrid arg0, int arg1){
        // TODO Auto-generated method stub

    }

}
