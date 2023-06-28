/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.LogisticsMenu;
import me.desht.pneumaticcraft.common.item.TagFilterItem;
import me.desht.pneumaticcraft.common.item.logistics.AbstractLogisticsFrameItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractLogisticsFrameEntity extends AbstractSemiblockEntity implements IDirectionalSemiblock {
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

    private static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(AbstractLogisticsFrameEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Direction> SIDE = SynchedEntityData.defineId(AbstractLogisticsFrameEntity.class, EntityDataSerializers.DIRECTION);
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

    AbstractLogisticsFrameEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    /**
     * Get a logistics entity from the given item stack.
     *
     * @param world the player
     * @param player the player (may be null; this could be used for tooltip purposes)
     * @param stack the item stack
     * @return a logistics entity
     */
    public static AbstractLogisticsFrameEntity fromItemStack(Level world, @Nullable Player player, @Nonnull ItemStack stack) {
        if (stack.getItem() instanceof SemiblockItem semiblockItem) {
            AbstractSemiblockEntity semiblock = semiblockItem.createEntity(world, stack, player, BlockPos.ZERO);
            if (semiblock instanceof AbstractLogisticsFrameEntity logisticsFrame) {
                if (world.isClientSide && stack.hasTag()) {
                    // client-side entity creation doesn't load in NBT from the itemstack; we need to do it ourselves in this case
                    // see EntityType#applyItemNBT()
                    CompoundTag tag = logisticsFrame.saveWithoutId(new CompoundTag());
                    UUID uuid = logisticsFrame.getUUID();
                    //noinspection ConstantConditions
                    tag.merge(stack.getTag().getCompound(NBTKeys.ENTITY_TAG));
                    logisticsFrame.setUUID(uuid);
                    logisticsFrame.load(tag);
                }
                return logisticsFrame;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        getEntityData().define(INVISIBLE, false);
        getEntityData().define(SIDE, Direction.SOUTH);
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockEntity te = getCachedTileEntity();
        return te != null &&
                (te.getCapability(ForgeCapabilities.ITEM_HANDLER, facing).isPresent()
                        || te.getCapability(ForgeCapabilities.FLUID_HANDLER, facing).isPresent());
    }

    @Override
    public boolean canStay() {
        return canPlace(getSide());
    }

    @Override
    protected AABB calculateBlockBounds() {
        AABB bb = super.calculateBlockBounds();
        return switch (getSide()) {
            case DOWN -> new AABB(bb.minX, bb.minY - FRAME_WIDTH, bb.minZ, bb.maxX, bb.minY, bb.maxZ);
            case UP -> new AABB(bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY + FRAME_WIDTH, bb.maxZ);
            case NORTH -> new AABB(bb.minX, bb.minY, bb.minZ - FRAME_WIDTH, bb.maxX, bb.maxY, bb.minZ);
            case SOUTH -> new AABB(bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ + FRAME_WIDTH);
            case WEST -> new AABB(bb.minX - FRAME_WIDTH, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.maxZ);
            case EAST -> new AABB(bb.maxX, bb.minY, bb.minZ, bb.maxX + FRAME_WIDTH, bb.maxY, bb.maxZ);
        };
    }

    @Override
    public abstract int getColor();

    public abstract ResourceLocation getTexture();

    public abstract int getPriority();

    protected abstract MenuType<?> getContainerType();

    public boolean shouldProvideTo(int level) {
        return true;
    }

    /**
     * Not to be confused with {@link net.minecraft.world.entity.Entity#isInvisible()}
     * @return true if the semiblock should fade out when not holding a logistics item
     */
    public boolean isSemiblockInvisible() {
        return getEntityData().get(INVISIBLE);
    }

    public void setSemiblockInvisible(boolean invisible) {
        getEntityData().set(INVISIBLE, invisible);
    }

    @Override
    public Direction getSide() {
        return getEntityData().get(SIDE);
    }

    @Override
    public void setSide(Direction facing) {
        if (SemiblockTracker.getInstance().getSemiblock(level(), getBlockPos(), facing) == null) {
            getEntityData().set(SIDE, facing);
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

        if (!level().isClientSide) {
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
        return incomingStacks.keySet().stream()
                .filter(s -> itemFilterHandler.matchOneItem(stack, s))
                .mapToInt(ItemStack::getCount)
                .sum();
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
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        itemFilterHandler.deserializeNBT(tag.getCompound(NBT_ITEM_FILTERS));
        fluidFilters.deserializeNBT(tag.getCompound(NBT_FLUID_FILTERS));
        setSemiblockInvisible(tag.getBoolean(NBT_INVISIBLE));
        setMatchNBT(tag.getBoolean(NBT_MATCH_NBT));
        setMatchDurability(tag.getBoolean(NBT_MATCH_DURABILITY));
        setMatchModId(tag.getBoolean(NBT_MATCH_MODID));
        setItemWhiteList(tag.getBoolean(NBT_ITEM_WHITELIST));
        setFluidWhiteList(tag.getBoolean(NBT_FLUID_WHITELIST));
        setSide(tag.contains(NBT_SIDE) ? Direction.from3DDataValue(tag.getInt(NBT_SIDE)) : Direction.UP);

        if (this instanceof ISpecificRequester spr) {
            spr.setMinItemOrderSize(Math.max(1, tag.getInt(ISpecificRequester.NBT_MIN_ITEMS)));
            spr.setMinFluidOrderSize(Math.max(1, tag.getInt(ISpecificRequester.NBT_MIN_FLUID)));
        }
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag) {
        tag = super.serializeNBT(tag);

        tag.put(NBT_ITEM_FILTERS, itemFilterHandler.serializeNBT());
        tag.put(NBT_FLUID_FILTERS, fluidFilters.serializeNBT());
        if (isSemiblockInvisible()) tag.putBoolean(NBT_INVISIBLE, true);
        if (isMatchNBT()) tag.putBoolean(NBT_MATCH_NBT, true);
        if (isMatchDurability()) tag.putBoolean(NBT_MATCH_DURABILITY, true);
        if (isMatchModId()) tag.putBoolean(NBT_MATCH_MODID, true);
        if (isItemWhiteList()) tag.putBoolean(NBT_ITEM_WHITELIST, true);
        if (isFluidWhiteList()) tag.putBoolean(NBT_FLUID_WHITELIST, true);
        if (getSide() != null) tag.putInt(NBT_SIDE, getSide().get3DDataValue());
        if (this instanceof ISpecificRequester spr) {
            tag.putInt(ISpecificRequester.NBT_MIN_ITEMS, spr.getMinItemOrderSize());
            tag.putInt(ISpecificRequester.NBT_MIN_FLUID, spr.getMinFluidOrderSize());
        }

        return tag;
    }

    @Override
    public void onPlaced(Player player, ItemStack stack, Direction facing) {
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
    public void addTooltip(Consumer<Component> curInfo, Player player, CompoundTag tag, boolean extended) {
        curInfo.accept(PneumaticCraftUtils.xlate("pneumaticcraft.gui.logistics_frame.facing", getSide()));
        if (player.isShiftKeyDown()) {
            NonNullList<ItemStack> drops = getDrops();
            if (!drops.isEmpty()) {
                AbstractLogisticsFrameItem.addLogisticsTooltip(drops.get(0), player.level(), new ArrayList<>(), true).forEach(curInfo);
            }
        }
    }

    @Override
    public boolean onRightClickWithConfigurator(Player player, Direction side) {
        if (!player.level().isClientSide) {
            if (side != getSide()) {
                return false;
            }
            MenuProvider provider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new ItemStack(getDroppedItem()).getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new LogisticsMenu(getContainerType(), i, playerInventory, getId());
                }
            };

            NetworkHandler.sendToPlayer(new PacketSyncSemiblock(this, false), (ServerPlayer) player);
            NetworkHooks.openScreen((ServerPlayer) player, provider, buffer -> buffer.writeVarInt(getId()));
        }
        return true;
    }

    private boolean playerIsHoldingLogisticItems() {
        // only call this client-side!
        Player player = ClientUtils.getClientPlayer();
        ItemStack stack = player.getMainHandItem();
        return (stack.getItem() == ModItems.LOGISTICS_CONFIGURATOR.get()
                || stack.getItem() == ModItems.LOGISTICS_DRONE.get()
                || stack.getItem() instanceof SemiblockItem);
    }

    public boolean supportsBlacklisting() {
        return true;
    }

    @Override
    public void writeToBuf(FriendlyByteBuf payload) {
        super.writeToBuf(payload);

        payload.writeByte(getSide().get3DDataValue());
        payload.writeBoolean(isSemiblockInvisible());
        payload.writeBoolean(itemWhiteList);
        payload.writeBoolean(fluidWhiteList);
        payload.writeBoolean(matchNBT);
        payload.writeBoolean(matchDurability);
        payload.writeBoolean(matchModId);
        payload.writeVarInt(itemFilterHandler.getSlots());
        for (int i = 0; i < itemFilterHandler.getSlots(); i++) {
            payload.writeItem(itemFilterHandler.getStackInSlot(i));
        }
        fluidFilters.write(payload);
        if (this instanceof ISpecificRequester spr) {
            payload.writeVarInt(spr.getMinItemOrderSize());
            payload.writeVarInt(spr.getMinFluidOrderSize());
        }
    }

    @Override
    public void readFromBuf(FriendlyByteBuf payload) {
        super.readFromBuf(payload);

        setSide(Direction.from3DDataValue(payload.readByte()));
        setSemiblockInvisible(payload.readBoolean());
        itemWhiteList = payload.readBoolean();
        fluidWhiteList = payload.readBoolean();
        matchNBT = payload.readBoolean();
        matchDurability = payload.readBoolean();
        matchModId = payload.readBoolean();
        int size = payload.readVarInt();
        for (int i = 0; i < size; i++) {
            itemFilterHandler.setStackInSlot(i, payload.readItem());
        }
        fluidFilters = new FluidFilter(payload);
        if (this instanceof ISpecificRequester spr) {
            spr.setMinItemOrderSize(payload.readVarInt());
            spr.setMinFluidOrderSize(payload.readVarInt());
        }
    }

    public boolean isObstructed(PathComputationType pathType) {
        BlockPos pos = getBlockPos().relative(getSide());
        return !level().getBlockState(pos).isPathfindable(level(), pos, pathType);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerLeftClick(AttackEntityEvent event) {
            if (event.getTarget() instanceof AbstractLogisticsFrameEntity frame) {
                // pass a left-click on invisible logistics frame through to the block it's on
                if (frame.isSemiblockInvisible()) {
                    frame.getBlockState().attack(frame.getWorld(), frame.getBlockPos(), event.getEntity());
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
            return filterStacks.stream()
                    .anyMatch(filterStack -> matchOneItem(filterStack, stack));
        }

        int getMatchedCount(ItemStack stack) {
            return filterStacks.stream()
                    .filter(filterStack -> matchOneItem(filterStack, stack))
                    .mapToInt(ItemStack::getCount)
                    .sum();
        }

        /**
         * Match against a filter stack. It's important that the filter stack is passed <em>first</em> here; since if
         * it's a {@link TagFilterItem}, it will have some special handling.
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
        public void deserializeNBT(CompoundTag nbt) {
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

    public static class FluidFilter implements INBTSerializable<CompoundTag> {
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

        FluidFilter(FriendlyByteBuf packetBuffer) {
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

        public void write(FriendlyByteBuf packetBuffer) {
            packetBuffer.writeVarInt(fluidStacks.size());
            fluidStacks.forEach(packetBuffer::writeFluidStack);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("size", fluidStacks.size());
            ListTag list = new ListTag();
            for (FluidStack f : fluidStacks) {
                list.add(f.writeToNBT(new CompoundTag()));
            }
            tag.put("filters", list);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            fluidStacks.clear();
            int n = nbt.getInt("size");
            ListTag l = nbt.getList("filters", Tag.TAG_COMPOUND);
            for (int i = 0; i < n; i++) {
                fluidStacks.add(FluidStack.loadFluidStackFromNBT(l.getCompound(i)));
            }
        }
    }
}
