package pneumaticCraft.common.semiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.item.ItemLogisticsFrame;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetSemiBlock;
import pneumaticCraft.common.tileentity.TileEntityBase;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class SemiBlockLogistics extends SemiBlockBasic{
    protected final Map<ItemStack, Integer> incomingStacks = new HashMap<ItemStack, Integer>();
    protected final Map<FluidStackWrapper, Integer> incomingFluid = new HashMap<FluidStackWrapper, Integer>();
    private final IInventory filters = new InventoryBasic("filters", true, 27);//Filters and requests
    @GuiSynced
    private final FluidTank[] fluidFilters = new FluidTank[9];
    @DescSynced
    @GuiSynced
    private boolean invisible;
    private int alpha = 255;

    public SemiBlockLogistics(){
        for(int i = 0; i < fluidFilters.length; i++) {
            fluidFilters[i] = new FluidTank(canFilterStack() ? 64000 : 1000);
        }
    }

    @Override
    public boolean canPlace(){
        return getTileEntity() instanceof IInventory || getTileEntity() instanceof IFluidHandler;
    }

    public abstract int getColor();

    public abstract int getPriority();

    public boolean shouldProvideTo(int level){
        return true;
    }

    public void setInvisible(boolean invisible){
        this.invisible = invisible;
    }

    public boolean isInvisible(){
        return invisible;
    }

    public int getAlpha(){
        return alpha;
    }

    @Override
    public void update(){
        super.update();
        if(!world.isRemote) {
            Iterator<Map.Entry<ItemStack, Integer>> iterator = incomingStacks.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<ItemStack, Integer> entry = iterator.next();
                int counter = entry.getValue();
                if(counter > 10) {
                    iterator.remove();
                } else {
                    entry.setValue(counter + 1);
                }
            }
            Iterator<Map.Entry<FluidStackWrapper, Integer>> it = incomingFluid.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<FluidStackWrapper, Integer> entry = it.next();
                int counter = entry.getValue();
                if(counter > 10) {
                    it.remove();
                } else {
                    entry.setValue(counter + 1);
                }
            }

        } else {
            if(invisible && !playerIsHoldingLogisticItems()) {
                alpha = Math.max(0, alpha - 3);
            } else {
                alpha = Math.min(255, alpha + 3);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean playerIsHoldingLogisticItems(){
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        return stack != null && (stack.getItem() == Itemss.logisticsConfigurator || stack.getItem() instanceof ItemLogisticsFrame);
    }

    public void informIncomingStack(ItemStack stack){
        incomingStacks.put(stack, 0);
    }

    public void clearIncomingStack(ItemStack stack){
        incomingStacks.remove(stack);
    }

    public void informIncomingStack(FluidStackWrapper stack){
        incomingFluid.put(stack, 0);
    }

    public void clearIncomingStack(FluidStackWrapper stack){
        incomingFluid.remove(stack);
    }

    public int getIncomingFluid(Fluid fluid){
        int count = 0;
        for(FluidStackWrapper wrapper : incomingFluid.keySet()) {
            if(wrapper.stack.getFluid() == fluid) count += wrapper.stack.amount;
        }
        return count;
    }

    public int getIncomingItems(ItemStack stack){
        int count = 0;
        for(ItemStack s : incomingStacks.keySet()) {
            if(isItemEqual(s, stack)) {
                count += s.stackSize;
            }
        }
        return count;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        TileEntityBase.writeInventoryToNBT(tag, filters, "filters");

        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < fluidFilters.length; i++) {
            FluidTank filter = fluidFilters[i];
            if(filter.getFluid() != null) {
                NBTTagCompound t = new NBTTagCompound();
                t.setInteger("index", i);
                filter.writeToNBT(t);
                tagList.appendTag(t);
            }
        }
        tag.setTag("fluidFilters", tagList);

        tag.setBoolean("invisible", invisible);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        TileEntityBase.readInventoryFromNBT(tag, filters, "filters");

        NBTTagList tagList = tag.getTagList("fluidFilters", 10);
        for(int i = 0; i < tagList.tagCount(); i++) {
            fluidFilters[tagList.getCompoundTagAt(i).getInteger("index")].readFromNBT(tagList.getCompoundTagAt(i));
        }

        invisible = tag.getBoolean("invisible");
    }

    public void setFilter(int filterIndex, FluidStack stack){
        fluidFilters[filterIndex].setFluid(stack);
    }

    public IFluidTank getTankFilter(int filterIndex){
        return fluidFilters[filterIndex];
    }

    public IInventory getFilters(){
        return filters;
    }

    @Override
    public void addDrops(List<ItemStack> drops){
        super.addDrops(drops);

        boolean shouldAddTag = false;
        for(int i = 0; i < filters.getSizeInventory(); i++) {
            if(filters.getStackInSlot(i) != null) {//Only set a tag when there are requests.
                shouldAddTag = true;
                break;
            }
        }

        for(FluidTank fluidFilter : fluidFilters) {
            if(fluidFilter.getFluidAmount() > 0) {
                shouldAddTag = true;
                break;
            }
        }

        if(invisible) shouldAddTag = true;

        if(shouldAddTag) {
            ItemStack drop = drops.get(0);
            NBTTagCompound tag = new NBTTagCompound();
            writeToNBT(tag);
            drop.setTagCompound(tag);
        }
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack){
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null) {
            readFromNBT(tag);
        }
    }

    @Override
    public boolean onRightClickWithConfigurator(EntityPlayer player){
        if(getGuiID() != null) {
            NetworkHandler.sendTo(new PacketSetSemiBlock(pos, this), (EntityPlayerMP)player);
            player.openGui(PneumaticCraft.instance, getGuiID().ordinal(), world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        }
        return true;
    }

    public EnumGuiId getGuiID(){
        return null;
    }

    public boolean canFilterStack(){
        return false;
    }

    protected boolean isItemEqual(ItemStack s1, ItemStack s2){
        return s1.isItemEqual(s2);
    }

    protected boolean passesFilter(ItemStack stack){
        boolean hasStack = false;
        for(int i = 0; i < filters.getSizeInventory(); i++) {
            ItemStack s = filters.getStackInSlot(i);
            if(s != null) {
                if(isItemEqual(s, stack)) return true;
                hasStack = true;
            }
        }
        return !hasStack;
    }

    protected boolean passesFilter(Fluid fluid){
        boolean hasFilter = false;
        for(FluidTank filter : fluidFilters) {
            if(filter.getFluidAmount() > 0) {
                if(filter.getFluid().getFluid() == fluid) return true;
                hasFilter = true;
            }
        }
        return !hasFilter;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        if(guiID == 9) {
            invisible = !invisible;
        }
    }

    @Override
    public void addWailaTooltip(List<String> curInfo, NBTTagCompound tag){
        super.addWailaTooltip(curInfo, tag);
        // readFromNBT(tag);
        List<ItemStack> drops = new ArrayList<ItemStack>();
        addDrops(drops);
        drops.get(0).setTagCompound(tag);
        ItemLogisticsFrame.addTooltip(drops.get(0), PneumaticCraft.proxy.getPlayer(), curInfo, true);
    }

    @Override
    public void addWailaInfoToTag(NBTTagCompound tag){
        writeToNBT(tag);
    }

    public static class FluidStackWrapper{
        public final FluidStack stack;

        public FluidStackWrapper(FluidStack stack){
            this.stack = stack;
        }
    }
}
