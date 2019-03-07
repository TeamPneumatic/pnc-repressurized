package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class SemiBlockLogistics extends SemiBlockBasic<TileEntity> {
    private static final String NBT_INVISIBLE = "invisible";
    private static final String NBT_FUZZY_META = "fuzzyMeta";
    private static final String NBT_FUZZY_NBT = "fuzzyNBT";
    private static final String NBT_WHITELIST = "whitelist";
    private static final String NBT_FILTERS = "filters";
    private static final String NBT_FLUID_FILTERS = "fluidFilters";
    private static final String NBT_SIDE = "side";
    private final Map<ItemStack, Integer> incomingStacks = new HashMap<>();
    private final Map<FluidStackWrapper, Integer> incomingFluid = new HashMap<>();
    private final ItemStackHandler filters = new ItemStackHandler(27);
    @GuiSynced
    private final FluidTank[] fluidFilters = new FluidTank[9];
    @DescSynced
    @GuiSynced
    private boolean invisible;
    @GuiSynced
    private boolean fuzzyMeta = false;  // TODO: remove in 1.13
    @GuiSynced
    private boolean fuzzyNBT = true;
    @GuiSynced
    private boolean whitelist = true;
    @DescSynced
    @GuiSynced
    private EnumFacing side = EnumFacing.UP;

    private int alpha = 255;

    public SemiBlockLogistics() {
        super(TileEntity.class);
        for (int i = 0; i < fluidFilters.length; i++) {
            fluidFilters[i] = new FluidTank(canFilterStack() ? 64000 : 1000);
        }
    }

    @Override
    public boolean canPlace(EnumFacing facing) {
        return getTileEntity() != null &&
                (getTileEntity().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                || getTileEntity().hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
    }

    public abstract int getColor();

    public abstract int getPriority();

    public boolean shouldProvideTo(int level) {
        return true;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public int getAlpha() {
        return alpha;
    }

    public EnumFacing getSide() {
        return side;
    }

    public void setSide(EnumFacing side) {
        this.side = side;
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            Iterator<Map.Entry<ItemStack, Integer>> iterator = incomingStacks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ItemStack, Integer> entry = iterator.next();
                int counter = entry.getValue();
                if (counter > 10) {
                    iterator.remove();
                } else {
                    entry.setValue(counter + 1);
                }
            }
            Iterator<Map.Entry<FluidStackWrapper, Integer>> it = incomingFluid.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<FluidStackWrapper, Integer> entry = it.next();
                int counter = entry.getValue();
                if (counter > 10) {
                    it.remove();
                } else {
                    entry.setValue(counter + 1);
                }
            }

        } else {
            if (invisible && !playerIsHoldingLogisticItems()) {
                alpha = Math.max(0, alpha - 9);
            } else {
                alpha = Math.min(255, alpha + 9);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private boolean playerIsHoldingLogisticItems() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack stack = player.getHeldItemMainhand();
        return (stack.getItem() == Itemss.LOGISTICS_CONFIGURATOR
                || stack.getItem() == Itemss.LOGISTICS_DRONE
                || stack.getItem() instanceof ItemSemiBlockBase);
    }

    public void informIncomingStack(ItemStack stack) {
        incomingStacks.put(stack, 0);
    }

    public void clearIncomingStack(ItemStack stack) {
        incomingStacks.remove(stack);
    }

    public void informIncomingStack(FluidStackWrapper stack) {
        incomingFluid.put(stack, 0);
    }

    public void clearIncomingStack(FluidStackWrapper stack) {
        incomingFluid.remove(stack);
    }

    public int getIncomingFluid(Fluid fluid) {
        int count = 0;
        for (FluidStackWrapper wrapper : incomingFluid.keySet()) {
            if (wrapper.stack.getFluid() == fluid) count += wrapper.stack.amount;
        }
        return count;
    }

    public int getIncomingItems(ItemStack stack) {
        int count = 0;
        for (ItemStack s : incomingStacks.keySet()) {
            if (tryMatch(s, stack)) {
                count += s.getCount();
            }
        }
        return count;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag(NBT_FILTERS, filters.serializeNBT());

        NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < fluidFilters.length; i++) {
            FluidTank filter = fluidFilters[i];
            if (filter.getFluid() != null) {
                NBTTagCompound t = new NBTTagCompound();
                t.setInteger("index", i);
                filter.writeToNBT(t);
                tagList.appendTag(t);
            }
        }
        tag.setTag(NBT_FLUID_FILTERS, tagList);

        tag.setBoolean(NBT_INVISIBLE, invisible);
        tag.setBoolean(NBT_FUZZY_META, fuzzyMeta);
        tag.setBoolean(NBT_FUZZY_NBT, fuzzyNBT);
        tag.setBoolean(NBT_WHITELIST, whitelist);
        tag.setString(NBT_SIDE, side.getName());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        filters.deserializeNBT(tag.getCompoundTag(NBT_FILTERS));

        NBTTagList tagList = tag.getTagList(NBT_FLUID_FILTERS, 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            fluidFilters[tagList.getCompoundTagAt(i).getInteger("index")].readFromNBT(tagList.getCompoundTagAt(i));
        }

        invisible = NBTUtil.fromTag(tag, NBT_INVISIBLE, false);
        fuzzyMeta = NBTUtil.fromTag(tag, NBT_FUZZY_META, false);
        fuzzyNBT = NBTUtil.fromTag(tag, NBT_FUZZY_NBT, true);
        whitelist = NBTUtil.fromTag(tag, NBT_WHITELIST, true);
        side = tag.hasKey(NBT_SIDE) ? EnumFacing.byName(tag.getString(NBT_SIDE)) : EnumFacing.UP;
    }

    public void setFilter(int filterIndex, FluidStack stack) {
        fluidFilters[filterIndex].setFluid(stack);
    }

    public IFluidTank getTankFilter(int filterIndex) {
        return fluidFilters[filterIndex];
    }

    public IItemHandlerModifiable getFilters() {
        return filters;
    }

    /**
     * Check if any settings are non-default and thus need to be saved as NBT on the logistics item
     *
     * @return true if NBT should be saved
     */
    protected boolean shouldSaveNBT() {
        for (int i = 0; i < filters.getSlots(); i++) {
            if (!filters.getStackInSlot(i).isEmpty()) return true;
        }

        for (FluidTank fluidFilter : fluidFilters) {
            if (fluidFilter.getFluidAmount() > 0) return true;
        }

        return invisible || fuzzyMeta || !fuzzyNBT || !whitelist || side != EnumFacing.UP;
    }

    @Override
    public void addDrops(NonNullList<ItemStack> drops) {
        super.addDrops(drops);

        if (shouldSaveNBT()) {
            ItemStack drop = drops.get(0);
            NBTTagCompound tag = new NBTTagCompound();
            writeToNBT(tag);
            drop.setTagCompound(tag);
        }
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            readFromNBT(tag);
        }
    }

    @Override
    public boolean onRightClickWithConfigurator(EntityPlayer player) {
        if (getGuiID() != null) {
//            NetworkHandler.sendTo(new PacketAddSemiBlock(pos, this), (EntityPlayerMP) player);
            player.openGui(PneumaticCraftRepressurized.instance, getGuiID().ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    public boolean canFilterStack() {
        return false;
    }

    boolean tryMatch(ItemStack s1, ItemStack s2) {
        boolean matched;
        if (!fuzzyMeta && !fuzzyNBT) {
            matched = ItemStack.areItemStacksEqual(s1, s2);
        } else if (fuzzyMeta && !fuzzyNBT) {
            matched = !s1.isEmpty() && s1.getItem() == s2.getItem() && ItemStack.areItemStackTagsEqual(s1, s2);
        } else if (!fuzzyMeta) {
            matched = s1.isItemEqual(s2);
        } else {
            matched = !s1.isEmpty() && s1.getItem() == s2.getItem();
        }
        return matched == whitelist;
    }

    boolean passesFilter(ItemStack stack) {
        boolean hasStack = false;
        for (int i = 0; i < filters.getSlots(); i++) {
            ItemStack s = filters.getStackInSlot(i);
            if (!s.isEmpty()) {
                if (tryMatch(s, stack)) return true;
                hasStack = true;
            }
        }
        return !hasStack;
    }

    boolean passesFilter(Fluid fluid) {
        boolean hasFilter = false;
        for (FluidTank filter : fluidFilters) {
            if (filter.getFluidAmount() > 0) {
                if (filter.getFluid().getFluid() == fluid) return true;
                hasFilter = true;
            }
        }
        return !hasFilter;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        if (guiID == 9) {
            invisible = !invisible;
        } else if (guiID == 10) {
            fuzzyMeta = !fuzzyMeta;
        } else if (guiID == 11) {
            fuzzyNBT = !fuzzyNBT;
        } else if (guiID == 12 && supportsBlacklisting()) {
            whitelist = !whitelist;
        } else if (guiID >= 13 && guiID <= 18) {
            setSide(EnumFacing.byIndex(guiID - 13));
        }
    }

    @Override
    public void addTooltip(List<String> curInfo, NBTTagCompound tag, boolean extended) {
        super.addTooltip(curInfo, tag, extended);
        curInfo.add(I18n.format("gui.logistic_frame.facing", side));
        if (extended) {
            NonNullList<ItemStack> drops = NonNullList.create();
            addDrops(drops);
            if (!drops.isEmpty()) {
                drops.get(0).setTagCompound(tag);
                ItemLogisticsFrame.addTooltip(drops.get(0), PneumaticCraftRepressurized.proxy.getClientWorld(), curInfo, true);
            }
        }
    }

    @Override
    public void addWailaInfoToTag(NBTTagCompound tag) {
        writeToNBT(tag);
    }

    public boolean isFuzzyMeta() {
        return fuzzyMeta;
    }

    public boolean isFuzzyNBT() {
        return fuzzyNBT;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean supportsBlacklisting() {
        return true;
    }

    public static class FluidStackWrapper {
        public final FluidStack stack;

        public FluidStackWrapper(FluidStack stack) {
            this.stack = stack;
        }
    }
}
