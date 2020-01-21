package me.desht.pneumaticcraft.common.entity.semiblock;

import com.google.common.collect.Sets;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class EntityLogisticsFrame extends EntitySemiblockBase {
    public static final String NBT_INVISIBLE = "invisible";
    public static final String NBT_MATCH_NBT = "matchNBT";
    public static final String NBT_MATCH_TAGS = "matchTags";
    public static final String NBT_WHITELIST = "whitelist";
    public static final String NBT_ITEM_FILTERS = "filters";
    public static final String NBT_FLUID_FILTERS = "fluidFilters";
    private static final String NBT_SIDE = "side";

    public static final int ITEM_FILTER_SLOTS = 27;
    public static final int FLUID_FILTER_SLOTS = 9;

    private static final DataParameter<Boolean> INVISIBLE = EntityDataManager.createKey(EntityLogisticsFrame.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Direction> FACING = EntityDataManager.createKey(EntityLogisticsFrame.class, DataSerializers.DIRECTION);

    private final Map<ItemStack, Integer> incomingStacks = new HashMap<>();
    private final Map<FluidStack, Integer> incomingFluid = new IdentityHashMap<>();
    private final ItemStackHandler filters = new ItemStackHandler(ITEM_FILTER_SLOTS);
    private FluidFilter fluidFilters = new FluidFilter(FLUID_FILTER_SLOTS);
    private boolean matchNBT = false;
    private boolean matchTags = false;
    private boolean whiteList = true;
    private int alpha = 255;
    public final double antiZfight;  // prevents frames on adjacent full-blocks from z-fighting

    EntityLogisticsFrame(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

        this.antiZfight = worldIn.rand.nextDouble() * 0.005;
    }

    /**
     * Get a logistics entity from the given item stack.
     *
     * @param world the player
     * @param player the player (may be null; this could be used for tooltip purposes)
     * @param stack the item stack
     * @return a logistics entity
     */
    public static EntityLogisticsFrame fromItemStack(World world, @Nullable PlayerEntity player, @Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemSemiBlock) {
            EntitySemiblockBase logistics = ((ItemSemiBlock) stack.getItem()).createEntity(world, stack, player, BlockPos.ZERO);
            if (logistics instanceof EntityLogisticsFrame) {
                if (world.isRemote && stack.hasTag()) {
                    // client-side entity creation doesn't load in NBT from the itemstack; we need to do it ourselves in this case
                    // see EntityType#applyItemNBT()
                    CompoundNBT compoundnbt = logistics.writeWithoutTypeId(new CompoundNBT());
                    UUID uuid = logistics.getUniqueID();
                    compoundnbt.merge(stack.getTag().getCompound("EntityTag"));
                    logistics.setUniqueId(uuid);
                    logistics.read(compoundnbt);
                }
                return (EntityLogisticsFrame) logistics;
            }
        }
        return null;
    }

    @Override
    protected void registerData() {
        super.registerData();

        getDataManager().register(INVISIBLE, false);
        getDataManager().register(FACING, Direction.UP);
    }

    @Override
    public boolean canPlace(Direction facing) {
        TileEntity te = getCachedTileEntity();
        return te != null &&
                (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()
                        || te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent());
    }

    public abstract int getColor();

    public abstract int getPriority();

    protected abstract ContainerType<?> getContainerType();

    public boolean shouldProvideTo(int level) {
        return true;
    }

    /**
     * Not to be confused with {@link Entity#isInvisible()}
     * @return true if the semiblock should fade out when not holding a logistics item
     */
    public boolean isSemiblockInvisible() {
        return getDataManager().get(INVISIBLE);
    }

    public void setSemiblockInvisible(boolean invisible) {
        getDataManager().set(INVISIBLE, invisible);
    }

    public Direction getFacing() {
        return getDataManager().get(FACING);
    }

    public void setFacing(Direction facing) {
        getDataManager().set(FACING, facing);
    }

    public int getAlpha() {
        return alpha;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    public boolean isMatchNBT() {
        return matchNBT;
    }

    public void setMatchNBT(boolean matchNBT) {
        this.matchNBT = matchNBT;
    }

    public boolean isMatchTags() {
        return matchTags;
    }

    public void setMatchTags(boolean matchTags) {
        this.matchTags = matchTags;
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
            Iterator<Map.Entry<FluidStack, Integer>> it = incomingFluid.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<FluidStack, Integer> entry = it.next();
                int counter = entry.getValue();
                if (counter > 10) {
                    it.remove();
                } else {
                    entry.setValue(counter + 1);
                }
            }

        } else {
            if (isSemiblockInvisible() && !playerIsHoldingLogisticItems()) {
                alpha = Math.max(0, alpha - 9);
            } else {
                alpha = Math.min(255, alpha + 9);
            }
        }
    }

    public void informIncomingStack(ItemStack stack) {
        incomingStacks.put(stack, 0);
    }

    public void clearIncomingStack(ItemStack stack) {
        incomingStacks.remove(stack);
    }

    public void informIncomingStack(FluidStack stack) {
        incomingFluid.put(stack, 0);
    }

    public void clearIncomingStack(FluidStack stack) {
        incomingFluid.remove(stack);
    }

    public int getIncomingFluid(Fluid fluid) {
        int count = 0;
        for (FluidStack fluidStack : incomingFluid.keySet()) {
            if (fluidStack.getFluid() == fluid) count += fluidStack.getAmount();
        }
        return count;
    }

    public int getIncomingItems(ItemStack stack) {
        int count = 0;
        for (ItemStack s : incomingStacks.keySet()) {
            if (matchItems(s, stack)) {
                count += s.getCount();
            }
        }
        return count;
    }

    public void setItemFilter(int slot, ItemStack stack) {
        filters.setStackInSlot(slot, stack);
    }

    public void setFluidFilter(int filterIndex, FluidStack stack) {
        fluidFilters.fluidStacks.set(filterIndex, stack);
    }

    public FluidStack getFluidFilter(int filterIndex) {
        return fluidFilters.fluidStacks.get(filterIndex);
    }

    public IItemHandlerModifiable getFilters() {
        return filters;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        filters.deserializeNBT(tag.getCompound(NBT_ITEM_FILTERS));
        fluidFilters.deserializeNBT(tag.getCompound(NBT_FLUID_FILTERS));
        setSemiblockInvisible(tag.getBoolean(NBT_INVISIBLE));
        setMatchNBT(tag.getBoolean(NBT_MATCH_NBT));
        setMatchTags(tag.getBoolean(NBT_MATCH_TAGS));
        setWhiteList(tag.getBoolean(NBT_WHITELIST));
        setFacing(tag.contains(NBT_SIDE) ? Direction.byIndex(tag.getInt(NBT_SIDE)) : Direction.UP);
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        tag = super.serializeNBT(tag);

        tag.put(NBT_ITEM_FILTERS, filters.serializeNBT());
        tag.put(NBT_FLUID_FILTERS, fluidFilters.serializeNBT());
        tag.putBoolean(NBT_INVISIBLE, isSemiblockInvisible());
        tag.putBoolean(NBT_MATCH_NBT, isMatchNBT());
        tag.putBoolean(NBT_MATCH_TAGS, isMatchTags());
        tag.putBoolean(NBT_WHITELIST, isWhiteList());
        if (getFacing() != null) tag.putInt(NBT_SIDE, getFacing().getIndex());

        return tag;
    }

    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
        super.onPlaced(player, stack, facing);

        setFacing(facing);
    }

    public boolean canFilterStack() {
        return false;
    }

    boolean passesFilter(ItemStack stack) {
        boolean hasStack = false;
        for (int i = 0; i < filters.getSlots(); i++) {
            ItemStack s = filters.getStackInSlot(i);
            if (!s.isEmpty()) {
                if (matchItems(s, stack)) return true;
                hasStack = true;
            }
        }
        return !hasStack;
    }

    boolean passesFilter(Fluid fluid) {
        boolean hasFilter = false;
        for (FluidStack stack : fluidFilters.fluidStacks) {
            if (stack.getAmount() > 0) {
                if (matchFluids(stack.getFluid(), fluid)) return true;
                hasFilter = true;
            }
        }
        return !hasFilter;
    }

    boolean matchItems(ItemStack s1, ItemStack s2) {
        boolean matched = !s1.isEmpty()
                && tryMatchItem(s1.getItem(), s2.getItem())
                && (!isMatchNBT() || ItemStack.areItemStackTagsEqual(s1, s2));
        return matched == isWhiteList();
    }

    private boolean tryMatchItem(Item i1, Item i2) {
        return i1 == i2 || matchTags && !Sets.intersection(i1.getTags(), i2.getTags()).isEmpty();
    }

    private boolean matchFluids(Fluid f1, Fluid f2) {
        return f1 == f2 || matchTags && !Sets.intersection(f1.getTags(), f2.getTags()).isEmpty();
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        curInfo.add(PneumaticCraftUtils.xlate("gui.logistics_frame.facing", getFacing()));
        if (player.isSneaking()) {
            NonNullList<ItemStack> drops = getDrops();
            if (!drops.isEmpty()) {
                ItemLogisticsFrame.addLogisticsTooltip(drops.get(0), player.world, curInfo, true);
            }
        }
    }

    @Override
    public boolean onRightClickWithConfigurator(PlayerEntity player, Direction side) {
        if (!player.world.isRemote) {
            INamedContainerProvider provider = new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new ItemStack(getDroppedItem()).getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new ContainerLogistics(getContainerType(), i, playerInventory, getEntityId());
                }
            };

            NetworkHandler.sendToPlayer(new PacketSyncSemiblock(this), (ServerPlayerEntity) player);
            NetworkHooks.openGui((ServerPlayerEntity) player, provider, buffer -> buffer.writeVarInt(getEntityId()));
        }
        return true;
    }

    private boolean playerIsHoldingLogisticItems() {
        // only call this client-side!
        PlayerEntity player = ClientUtils.getClientPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        return (stack.getItem() == ModItems.LOGISTICS_CONFIGURATOR.get()
                || stack.getItem() == ModItems.LOGISTICS_DRONE.get()
                || stack.getItem() instanceof ItemSemiBlock);
    }

    public boolean supportsBlacklisting() {
        return true;
    }

    @Override
    public void writeToBuf(PacketBuffer payload) {
        super.writeToBuf(payload);

        payload.writeBoolean(isSemiblockInvisible());
        payload.writeBoolean(whiteList);
        payload.writeBoolean(matchTags);
        payload.writeBoolean(matchNBT);
        payload.writeVarInt(filters.getSlots());
        for (int i = 0; i < filters.getSlots(); i++) {
            payload.writeItemStack(filters.getStackInSlot(i));
        }
        fluidFilters.write(payload);
    }

    @Override
    public void readFromBuf(PacketBuffer payload) {
        super.readFromBuf(payload);

        setSemiblockInvisible(payload.readBoolean());
        whiteList = payload.readBoolean();
        matchTags = payload.readBoolean();
        matchNBT = payload.readBoolean();
        int size = payload.readVarInt();
        for (int i = 0; i < size; i++) {
            filters.setStackInSlot(i, payload.readItemStack());
        }
        fluidFilters = new FluidFilter(payload);
    }

    public static class FluidFilter implements INBTSerializable<CompoundNBT> {
        private final List<FluidStack> fluidStacks;

        public FluidFilter() {
            fluidStacks = new ArrayList<>();
        }

        FluidFilter(int size) {
            fluidStacks = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                fluidStacks.add(FluidStack.EMPTY);
            }
        }

        FluidFilter(PacketBuffer packetBuffer) {
            fluidStacks = new ArrayList<>();
            int size = packetBuffer.readVarInt();
            for (int i = 0; i < size; i++) {
                fluidStacks.add(packetBuffer.readFluidStack());
            }
        }

        public FluidStack get(int slot) {
            return fluidStacks.get(slot);
        }

        public int size() {
            return fluidStacks.size();
        }

        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeVarInt(fluidStacks.size());
            fluidStacks.forEach(packetBuffer::writeFluidStack);
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("size", fluidStacks.size());
            ListNBT list = new ListNBT();
            for (FluidStack f : fluidStacks) {
                list.add(f.writeToNBT(new CompoundNBT()));
            }
            tag.put("filters", list);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            fluidStacks.clear();
            int n = nbt.getInt("size");
            ListNBT l = nbt.getList("filters", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < n; i++) {
                fluidStacks.add(FluidStack.loadFluidStackFromNBT(l.getCompound(i)));
            }
        }
    }
}
