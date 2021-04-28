package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class EntityLogisticsFrame extends EntitySemiblockBase implements IDirectionalSemiblock {
    public static final String NBT_INVISIBLE = "invisible";
    public static final String NBT_MATCH_NBT = "matchNBT";
    public static final String NBT_MATCH_DURABILITY = "matchDurability";
    public static final String NBT_MATCH_MODID = "matchModID";
    public static final String NBT_ITEM_WHITELIST = "whitelist";
    public static final String NBT_FLUID_WHITELIST = "fluidWhitelist";
    public static final String NBT_ITEM_FILTERS = "filters";
    public static final String NBT_FLUID_FILTERS = "fluidFilters";
    private static final String NBT_SIDE = "side";

    private static final int ITEM_FILTER_SLOTS = 27;
    public static final int FLUID_FILTER_SLOTS = 9;

    private static final DataParameter<Boolean> INVISIBLE = EntityDataManager.createKey(EntityLogisticsFrame.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Direction> SIDE = EntityDataManager.createKey(EntityLogisticsFrame.class, DataSerializers.DIRECTION);
    private static final float FRAME_WIDTH = 1 / 32f;

    private final Map<ItemStack, Integer> incomingStacks = new HashMap<>();
    private final Map<FluidStack, Integer> incomingFluid = new IdentityHashMap<>();
    private final ItemFilterHandler itemFilterHandler = new ItemFilterHandler(ITEM_FILTER_SLOTS);
    private FluidFilter fluidFilters = new FluidFilter(FLUID_FILTER_SLOTS);
    private boolean matchNBT = false;
    private boolean matchDurability = false;
    private boolean matchModId = false;
    private boolean itemWhiteList = true;
    private boolean fluidWhiteList = true;
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
                    //noinspection ConstantConditions
                    compoundnbt.merge(stack.getTag().getCompound(NBTKeys.ENTITY_TAG));
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
        getDataManager().register(SIDE, Direction.SOUTH);
    }

    @Override
    public boolean canPlace(Direction facing) {
        TileEntity te = getCachedTileEntity();
        return te != null &&
                (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent()
                        || te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).isPresent());
    }

    @Override
    public boolean canStay() {
        return canPlace(getSide());
    }

    @Override
    protected AxisAlignedBB calculateBlockBounds() {
        AxisAlignedBB bounds = super.calculateBlockBounds();
        switch (getSide()) {
            case DOWN:  bounds = new AxisAlignedBB(bounds.minX, bounds.minY - FRAME_WIDTH, bounds.minZ, bounds.maxX, bounds.minY, bounds.maxZ); break;
            case UP:    bounds = new AxisAlignedBB(bounds.minX, bounds.maxY, bounds.minZ, bounds.maxX, bounds.maxY + FRAME_WIDTH, bounds.maxZ); break;
            case NORTH: bounds = new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ - FRAME_WIDTH, bounds.maxX, bounds.maxY, bounds.minZ); break;
            case SOUTH: bounds = new AxisAlignedBB(bounds.minX, bounds.minY, bounds.maxZ, bounds.maxX, bounds.maxY, bounds.maxZ + FRAME_WIDTH); break;
            case WEST:  bounds = new AxisAlignedBB(bounds.minX - FRAME_WIDTH, bounds.minY, bounds.minZ, bounds.minX, bounds.maxY, bounds.maxZ); break;
            case EAST:  bounds = new AxisAlignedBB(bounds.maxX, bounds.minY, bounds.minZ, bounds.maxX + FRAME_WIDTH, bounds.maxY, bounds.maxZ); break;
        }
        return bounds;
    }

    @Override
    public abstract int getColor();

    public abstract ResourceLocation getTexture();

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

    @Override
    public Direction getSide() {
        return getDataManager().get(SIDE);
    }

    @Override
    public void setSide(Direction facing) {
        if (SemiblockTracker.getInstance().getSemiblock(world, getBlockPos(), facing) == null) {
            getDataManager().set(SIDE, facing);
        }
    }

    public int getAlpha() {
        return alpha;
    }

    public boolean isItemWhiteList() {
        return itemWhiteList;
    }

    public void setItemWhiteList(boolean whiteList) {
        this.itemWhiteList = whiteList;
    }

    public boolean isFluidWhiteList() {
        return fluidWhiteList;
    }

    public void setFluidWhiteList(boolean whiteList) {
        this.fluidWhiteList = whiteList;
    }

    public boolean isMatchNBT() {
        return matchNBT;
    }

    public void setMatchNBT(boolean matchNBT) {
        this.matchNBT = matchNBT;
    }

    public boolean isMatchDurability() {
        return matchDurability;
    }

    public void setMatchDurability(boolean matchDurability) {
        this.matchDurability = matchDurability;
    }

    public boolean isMatchModId() {
        return matchModId;
    }

    public void setMatchModId(boolean matchModId) {
        this.matchModId = matchModId;
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
            if (itemFilterHandler.matchOneItem(stack, s)) {
                count += s.getCount();
            }
        }
        return count;
    }

    public void setItemFilter(int slot, ItemStack stack) {
        itemFilterHandler.setStackInSlot(slot, stack);
    }

    public void setFluidFilter(int filterIndex, FluidStack stack) {
        fluidFilters.fluidStacks.set(filterIndex, stack);
    }

    public FluidStack getFluidFilter(int filterIndex) {
        return fluidFilters.fluidStacks.get(filterIndex);
    }

    public ItemFilterHandler getItemFilterHandler() {
        return itemFilterHandler;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        itemFilterHandler.deserializeNBT(tag.getCompound(NBT_ITEM_FILTERS));
        fluidFilters.deserializeNBT(tag.getCompound(NBT_FLUID_FILTERS));
        setSemiblockInvisible(tag.getBoolean(NBT_INVISIBLE));
        setMatchNBT(tag.getBoolean(NBT_MATCH_NBT));
        setMatchDurability(tag.getBoolean(NBT_MATCH_DURABILITY));
        setMatchModId(tag.getBoolean(NBT_MATCH_MODID));
        setItemWhiteList(tag.getBoolean(NBT_ITEM_WHITELIST));
        if (!tag.contains(NBT_FLUID_WHITELIST, Constants.NBT.TAG_BYTE)) {
            setFluidWhiteList(true);
        } else {
            setFluidWhiteList(tag.getBoolean(NBT_FLUID_WHITELIST));
        }
        setSide(tag.contains(NBT_SIDE) ? Direction.byIndex(tag.getInt(NBT_SIDE)) : Direction.UP);

        if (this instanceof ISpecificRequester) {
            ((ISpecificRequester) this).setMinItemOrderSize(Math.max(1, tag.getInt(ISpecificRequester.NBT_MIN_ITEMS)));
            ((ISpecificRequester) this).setMinFluidOrderSize(Math.max(1, tag.getInt(ISpecificRequester.NBT_MIN_FLUID)));
        }
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        tag = super.serializeNBT(tag);

        tag.put(NBT_ITEM_FILTERS, itemFilterHandler.serializeNBT());
        tag.put(NBT_FLUID_FILTERS, fluidFilters.serializeNBT());
        tag.putBoolean(NBT_INVISIBLE, isSemiblockInvisible());
        tag.putBoolean(NBT_MATCH_NBT, isMatchNBT());
        tag.putBoolean(NBT_MATCH_DURABILITY, isMatchDurability());
        tag.putBoolean(NBT_MATCH_MODID, isMatchModId());
        tag.putBoolean(NBT_ITEM_WHITELIST, isItemWhiteList());
        tag.putBoolean(NBT_FLUID_WHITELIST, isFluidWhiteList());
        if (getSide() != null) tag.putInt(NBT_SIDE, getSide().getIndex());
        if (this instanceof ISpecificRequester) {
            tag.putInt(ISpecificRequester.NBT_MIN_ITEMS, ((ISpecificRequester) this).getMinItemOrderSize());
            tag.putInt(ISpecificRequester.NBT_MIN_FLUID, ((ISpecificRequester) this).getMinFluidOrderSize());
        }

        return tag;
    }

    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
        super.onPlaced(player, stack, facing);

        setSide(facing);
    }

    public boolean canFilterStack() {
        return false;
    }

    boolean passesFilter(ItemStack stack) {
        return itemFilterHandler.isEmpty() || itemFilterHandler.match(stack) == isItemWhiteList();
    }

    boolean passesFilter(Fluid fluid) {
        boolean hasFilter = false;
        for (FluidStack filterStack : fluidFilters.fluidStacks) {
            if (filterStack.getAmount() > 0) {
                if (matchFluids(filterStack, fluid)) return isFluidWhiteList();
                hasFilter = true;
            }
        }
        return !hasFilter;
    }

    private boolean matchFluids(FluidStack filterStack, Fluid fluid) {
        // TODO tag matcher fluid support
        return filterStack.getFluid() == fluid;
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
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
            if (side != getSide()) {
                return false;
            }
            INamedContainerProvider provider = new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new ItemStack(getDroppedItem()).getDisplayName();
                }

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

        payload.writeByte(getSide().getIndex());
        payload.writeBoolean(isSemiblockInvisible());
        payload.writeBoolean(itemWhiteList);
        payload.writeBoolean(fluidWhiteList);
        payload.writeBoolean(matchNBT);
        payload.writeBoolean(matchDurability);
        payload.writeBoolean(matchModId);
        payload.writeVarInt(itemFilterHandler.getSlots());
        for (int i = 0; i < itemFilterHandler.getSlots(); i++) {
            payload.writeItemStack(itemFilterHandler.getStackInSlot(i));
        }
        fluidFilters.write(payload);
        if (this instanceof ISpecificRequester) {
            payload.writeVarInt(((ISpecificRequester) this).getMinItemOrderSize());
            payload.writeVarInt(((ISpecificRequester) this).getMinFluidOrderSize());
        }
    }

    @Override
    public void readFromBuf(PacketBuffer payload) {
        super.readFromBuf(payload);

        setSide(Direction.byIndex(payload.readByte()));
        setSemiblockInvisible(payload.readBoolean());
        itemWhiteList = payload.readBoolean();
        fluidWhiteList = payload.readBoolean();
        matchNBT = payload.readBoolean();
        matchDurability = payload.readBoolean();
        matchModId = payload.readBoolean();
        int size = payload.readVarInt();
        for (int i = 0; i < size; i++) {
            itemFilterHandler.setStackInSlot(i, payload.readItemStack());
        }
        fluidFilters = new FluidFilter(payload);
        if (this instanceof ISpecificRequester) {
            ((ISpecificRequester) this).setMinItemOrderSize(payload.readVarInt());
            ((ISpecificRequester) this).setMinFluidOrderSize(payload.readVarInt());
        }
    }

    public boolean isObstructed(PathType pathType) {
        BlockPos pos = getBlockPos().offset(getSide());
        return !world.getBlockState(pos).allowsMovement(world, pos, pathType);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerLeftClick(AttackEntityEvent event) {
            if (event.getTarget() instanceof EntityLogisticsFrame) {
                // pass a left-click on invisible logistics frame through to the block it's on
                EntityLogisticsFrame frame = (EntityLogisticsFrame) event.getTarget();
                if (frame.isSemiblockInvisible()) {
                    frame.getBlockState().onBlockClicked(frame.getWorld(), frame.getBlockPos(), event.getPlayer());
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Specialised item handler which caches a list of non-empty filter stacks, for more efficient iteration over them.
     */
    public class ItemFilterHandler extends ItemStackHandler {
        private final List<ItemStack> filterStacks = new ArrayList<>();

        ItemFilterHandler(int size) {
            super(size);
        }

        boolean match(ItemStack stack) {
            for (ItemStack filterStack : filterStacks) {
                if (matchOneItem(filterStack, stack)) {
                    return true;
                }
            }
            return false;
        }

        int getMatchedCount(ItemStack stack) {
            int count = 0;
            for (ItemStack filterStack : filterStacks) {
                if (matchOneItem(filterStack, stack)) {
                    count += filterStack.getCount();
                }
            }
            return count;
        }

        /**
         * Match against a filter stack. It's important that the filter stack is passed <em>first</em> here; since if
         * it's a {@link me.desht.pneumaticcraft.common.item.ItemTagFilter}, it will have some special handling.
         *
         * @param filterStack the stack to filter against, which is treated specially if it's a Tag Filter - beware
         * @param stack the stack being tested
         * @return true if the stack being tested matches the filter
         */
        boolean matchOneItem(ItemStack filterStack, ItemStack stack) {
            return !filterStack.isEmpty()
                    && PneumaticCraftUtils.doesItemMatchFilter(filterStack, stack, isMatchDurability(), isMatchNBT(), isMatchModId());
        }

        private void buildFilterList() {
            filterStacks.clear();
            for (int i = 0; i < getSlots(); i++) {
                if (!getStackInSlot(i).isEmpty()) {
                    filterStacks.add(getStackInSlot(i));
                }
            }
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            super.deserializeNBT(nbt);

            buildFilterList();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            buildFilterList();
        }

        public boolean isEmpty() {
            return filterStacks.isEmpty();
        }
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
