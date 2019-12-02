package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class SemiBlockLogistics extends SemiBlockBasic<TileEntity> implements INamedContainerProvider {
    private static final String NBT_INVISIBLE = "invisible";
    private static final String NBT_FUZZY_DAMAGE = "fuzzyDamage";
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
    private boolean fuzzyDamage = true;
    @GuiSynced
    private boolean fuzzyNBT = true;
    @GuiSynced
    private boolean whitelist = true;
    @DescSynced
    @GuiSynced
    private Direction side = Direction.UP;

    private int alpha = 255;
    private int tintColor = 0;

    public SemiBlockLogistics() {
        super(TileEntity.class);
        for (int i = 0; i < fluidFilters.length; i++) {
            fluidFilters[i] = new FluidTank(canFilterStack() ? 64000 : 1000);
        }
    }

    @Override
    public boolean canPlace(Direction facing) {
        return getTileEntity() != null &&
                (getTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent()
                || getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).isPresent());
    }

    @Override
    public boolean canStay() {
        return canPlace(getSide());
    }

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

    public Direction getSide() {
        return side;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    @Override
    public void tick() {
        super.tick();
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

    @OnlyIn(Dist.CLIENT)
    private boolean playerIsHoldingLogisticItems() {
        PlayerEntity player = Minecraft.getInstance().player;
        ItemStack stack = player.getHeldItemMainhand();
        return (stack.getItem() == ModItems.LOGISTICS_CONFIGURATOR
                || stack.getItem() == ModItems.LOGISTIC_DRONE
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
            if (wrapper.stack.getFluid() == fluid) count += wrapper.stack.getAmount();
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
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.put(NBT_FILTERS, filters.serializeNBT());

        ListNBT tagList = new ListNBT();
        for (int i = 0; i < fluidFilters.length; i++) {
            FluidTank filter = fluidFilters[i];
            if (filter.getFluid() != null) {
                CompoundNBT t = new CompoundNBT();
                t.putInt("index", i);
                filter.writeToNBT(t);
                tagList.add(t);
            }
        }
        tag.put(NBT_FLUID_FILTERS, tagList);

        tag.putBoolean(NBT_INVISIBLE, invisible);
        tag.putBoolean(NBT_FUZZY_DAMAGE, fuzzyDamage);
        tag.putBoolean(NBT_FUZZY_NBT, fuzzyNBT);
        tag.putBoolean(NBT_WHITELIST, whitelist);
        if (side != null) tag.putByte(NBT_SIDE, (byte) side.getIndex());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        filters.deserializeNBT(tag.getCompound(NBT_FILTERS));

        ListNBT tagList = tag.getList(NBT_FLUID_FILTERS, 10);
        for (int i = 0; i < tagList.size(); i++) {
            fluidFilters[tagList.getCompound(i).getInt("index")].readFromNBT(tagList.getCompound(i));
        }

        invisible = NBTUtil.fromTag(tag, NBT_INVISIBLE, false);
        fuzzyDamage = NBTUtil.fromTag(tag, NBT_FUZZY_DAMAGE, true);
        fuzzyNBT = NBTUtil.fromTag(tag, NBT_FUZZY_NBT, true);
        whitelist = NBTUtil.fromTag(tag, NBT_WHITELIST, true);
        side = tag.contains(NBT_SIDE) ? Direction.byIndex(tag.getByte(NBT_SIDE)) : Direction.UP;
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

        return invisible || !fuzzyNBT || !whitelist || side != Direction.UP;
    }

    @Override
    public void addDrops(NonNullList<ItemStack> drops) {
        super.addDrops(drops);

        if (shouldSaveNBT()) {
            ItemStack drop = drops.get(0);
            CompoundNBT tag = new CompoundNBT();
            writeToNBT(tag);
            drop.setTag(tag);
        }
    }

    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            readFromNBT(tag);
        }
        setSide(facing);
    }

    @Override
    public boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, this, getPos());
        }
        return true;
    }

    public boolean canFilterStack() {
        return false;
    }

    boolean tryMatch(ItemStack s1, ItemStack s2) {
        boolean isItemMatched = fuzzyDamage ? ItemStack.areItemsEqualIgnoreDurability(s1, s2) : ItemStack.areItemsEqual(s1, s2);
        boolean isNBTMatched = fuzzyNBT || ItemStack.areItemStackTagsEqual(s1, s2);

        return (isItemMatched && isNBTMatched) == whitelist;
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
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals("invisible")) {
            invisible = !invisible;
        } else if (tag.equals("fuzzyDamage")) {
            fuzzyDamage = !fuzzyDamage;
        } else if (tag.equals("fuzzyNBT")) {
            fuzzyNBT = !fuzzyNBT;
        } else if (tag.equals("whitelist") && supportsBlacklisting()) {
            whitelist = !whitelist;
        } else if (tag.startsWith("side:")) {
            try {
                setSide(Direction.byIndex(Integer.parseInt(tag.split(":")[1])));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, CompoundNBT tag, boolean extended) {
        super.addTooltip(curInfo, tag, extended);
        curInfo.add(xlate("gui.logistic_frame.facing", side.toString()));
        if (extended) {
            NonNullList<ItemStack> drops = NonNullList.create();
            addDrops(drops);
            if (!drops.isEmpty()) {
                drops.get(0).setTag(tag);
                ItemLogisticsFrame.addTooltip(drops.get(0), PneumaticCraftRepressurized.proxy.getClientWorld(), curInfo, true);
            }
        }
    }

    @Override
    public void addWailaInfoToTag(CompoundNBT tag) {
        writeToNBT(tag);
    }

    public boolean isFuzzyDamage() {
        return fuzzyDamage;
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

    public int getColor() {
        // cache because this can get called very frequently (rendering)
        if (tintColor == 0) {
            Item item = SemiBlockManager.getItemForSemiBlock(this);
            tintColor = item instanceof ItemLogisticsFrame ? ((ItemLogisticsFrame) item).getTintColor(new ItemStack(item), 0) : 0xFFFFFFFF;
        }
        return tintColor;
    }

    public static class FluidStackWrapper {
        public final FluidStack stack;

        public FluidStackWrapper(FluidStack stack) {
            this.stack = stack;
        }
    }
}
