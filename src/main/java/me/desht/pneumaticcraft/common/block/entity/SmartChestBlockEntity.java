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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.SmartChestMenu;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.network.PacketSyncSmartChest;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class SmartChestBlockEntity extends AbstractTickingBlockEntity
        implements MenuProvider, IRedstoneControl<SmartChestBlockEntity>, IComparatorSupport {
    public static final int CHEST_SIZE = 72;
    private static final String NBT_ITEMS = "Items";

    private final SmartChestItemHandler inventory = new SmartChestItemHandler(this, CHEST_SIZE);
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);
    @GuiSynced
    private final RedstoneController<SmartChestBlockEntity> rsController = new RedstoneController<>(this);
    @GuiSynced
    private int pushPullModes = 0;  // 6 tri-state (2-bit) values packed into an int (for sync reasons...)
    @GuiSynced
    private int cooldown = 0;

    // track the current slots being used to push/pull from, to reduce inventory scanning
    private final EnumMap<Direction,Integer> pullSlots = new EnumMap<>(Direction.class);
    private final EnumMap<Direction,Integer> pushSlots = new EnumMap<>(Direction.class);

    public SmartChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMART_CHEST.get(), pos, state, 4);
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (getUpgrades(ModUpgrades.MAGNET.get()) == 0) {
            AreaRenderManager.getInstance().removeHandlers(this);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (rsController.shouldRun() && (nonNullLevel().getGameTime() % Math.max(getTickRate(), cooldown)) == 0) {
            boolean didWork = false;
            for (SideConfigurator.RelativeFace face : SideConfigurator.RelativeFace.values()) {
                switch (getPushPullMode(face)) {
                    case PUSH -> didWork |= tryPush(getAbsoluteFacing(face, getRotation()));
                    case PULL -> didWork |= tryPull(getAbsoluteFacing(face, getRotation()));
                }
            }
            cooldown = didWork ? 0 : Math.min(cooldown + 4, 20);
        }
    }

    private boolean tryPush(Direction dir) {
        BlockEntity te = getCachedNeighbor(dir);

        if (te == null) {
            return tryDispense(dir);
        }

        return IOHelper.getInventoryForTE(te, dir.getOpposite()).map(dstHandler -> {
            validateCachedSlot(pushSlots, dir, dstHandler);

            ItemStack toPush = findNextItem(inventory, pushSlots, dir);
            if (toPush.isEmpty()) {
                // empty inventory
                return false;
            }
            ItemStack excess = ItemHandlerHelper.insertItem(dstHandler, toPush, false);
            int transferred = toPush.getCount() - excess.getCount();
            if (transferred > 0) {
                // success!
                inventory.extractItem(pushSlots.getOrDefault(dir, 0), transferred, false);
                return true;
            } else {
                // this item can't be pushed... move on
                pushSlots.put(dir, scanForward(inventory, pushSlots.getOrDefault(dir, 0)));
            }
            return false;
        }).orElse(false);
    }

    private boolean tryDispense(Direction dir) {
        if (getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            if (!Block.canSupportCenter(nonNullLevel(), worldPosition, dir.getOpposite())) {
                ItemStack toPush = findNextItem(inventory, pushSlots, dir);
                if (!toPush.isEmpty()) {
                    ItemStack pushed = inventory.extractItem(pushSlots.getOrDefault(dir, 0), toPush.getCount(), false);
                    BlockPos dropPos = worldPosition.relative(dir);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(pushed, level,dropPos.getX() + 0.5, dropPos.getY() + 0.5, dropPos.getZ() + 0.5);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryPull(Direction dir) {
        BlockEntity te = getCachedNeighbor(dir);

        if (te == null) {
            return tryMagnet(dir);
        }

        return IOHelper.getInventoryForTE(getCachedNeighbor(dir), dir.getOpposite()).map(srcHandler -> {
            validateCachedSlot(pullSlots, dir, srcHandler);

            ItemStack toPull = findNextItem(srcHandler, pullSlots, dir);
            if (toPull.isEmpty()) {
                // source inventory is empty
                return false;
            }
            ItemStack excess = ItemHandlerHelper.insertItem(inventory, toPull, false);
            int transferred = toPull.getCount() - excess.getCount();
            if (transferred > 0) {
                // success!
                srcHandler.extractItem(pullSlots.getOrDefault(dir, 0), transferred, false);
                return true;
            } else {
                // this item can't be inserted into this chest... move on to next item next tick
                pullSlots.put(dir, scanForward(inventory, pullSlots.getOrDefault(dir, 0)));
            }
            return false;
        }).orElse(false);
    }

    private boolean tryMagnet(Direction dir) {
        if (getUpgrades(ModUpgrades.MAGNET.get()) > 0) {
            int range = getUpgrades(ModUpgrades.RANGE.get());
            BlockPos centrePos = worldPosition.relative(dir, range + 2);
            AABB aabb = new AABB(centrePos).inflate(range + 1);
            List<ItemEntity> items = nonNullLevel().getEntitiesOfClass(ItemEntity.class, aabb,
                    item -> item != null && item.isAlive() && !item.hasPickUpDelay() && !ItemRegistry.getInstance().shouldSuppressMagnet(item));
            boolean didWork = false;
            for (ItemEntity item : items) {
                ItemStack stack = item.getItem();
                ItemStack excess = ItemHandlerHelper.insertItemStacked(inventory, stack, false);
                if (excess.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(excess);
                }
                if (excess.getCount() < stack.getCount()) {
                    NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, item.getX(), item.getY() + 0.5, item.getZ(), 0, 0, 0, 5, 0.5, 0.5, 0.5), this);
                    didWork = true;
                }
            }
            return didWork;
        }
        return false;
    }

    private void validateCachedSlot(Map<Direction,Integer> slotMap, Direction dir, IItemHandler handler) {
        // ensure cached slot is still valid for the handler we have
        // could become invalid if the neighbouring BE has been replaced by one with a smaller inventory?
        if (slotMap.getOrDefault(dir, 0) >= handler.getSlots()) {
            slotMap.remove(dir);
        }
    }

    /**
     * Scan through the inventory, finding the next non-empty itemstack, including the current slot
     * @param handler the inventory
     * @param slotMap array of current slot indices, one per side
     * @param dir the side; index into the slots array
     * @return the next non-empty item (or ItemStack.EMPTY if the whole inventory is empty)
     */
    private ItemStack findNextItem(IItemHandler handler, EnumMap<Direction,Integer> slotMap, Direction dir) {
        if (handler.getSlots() <= 0) return ItemStack.EMPTY; // https://github.com/TeamPneumatic/pnc-repressurized/issues/882

        if (handler.getStackInSlot(slotMap.getOrDefault(dir, 0)).isEmpty()) {
            slotMap.put(dir, scanForward(handler, slotMap.getOrDefault(dir, 0)));
        }
        return handler.extractItem(slotMap.getOrDefault(dir, 0), getMaxItems(), true);
    }

    /**
     * Scan through the target inventory, looking for a non-empty slot.
     *
     * @param handler the inventory to scan
     * @param slot slot to start at
     * @return the first non-empty slot (but test this slot; if empty, the entire inventory is empty)
     */
    private int scanForward(IItemHandler handler, int slot) {
        int limit = handler.getSlots();
        for (int i = 0; i < limit; i++) {
            if (++slot >= limit) slot = 0;
            if (!handler.getStackInSlot(slot).isEmpty()) break;
        }
        return slot;
    }

    public int getTickRate() {
        return 8 >> Math.min(3, getUpgrades(ModUpgrades.SPEED.get()));
    }

    public int getMaxItems() {
        int upgrades = getUpgrades(ModUpgrades.SPEED.get());
        return upgrades > 3 ? Math.min(1 << (upgrades - 3), 256) : 1;
    }

    public Direction getAbsoluteFacing(SideConfigurator.RelativeFace face, Direction dir) {
        return switch (face) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case FRONT -> dir;
            case RIGHT -> dir.getCounterClockWise();
            case LEFT -> dir.getClockWise();
            case BACK -> dir.getOpposite();
        };
    }

    public PushPullMode getPushPullMode(SideConfigurator.RelativeFace face) {
        int idx = face.ordinal();
        int mask = 0b11 << idx * 2;
        int n = (pushPullModes & mask) >> idx * 2;
        return PushPullMode.values()[n];
    }

    private void setPushPullMode(SideConfigurator.RelativeFace face, PushPullMode mode) {
        int idx = face.ordinal();
        int mask = 0b11 << idx * 2;
        pushPullModes &= ~mask;
        pushPullModes |= mode.ordinal() << idx * 2;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap(Direction side) {
        return inventoryCap;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        if (player instanceof ServerPlayer) {
            NetworkHandler.sendToPlayer(new PacketSyncSmartChest(this), (ServerPlayer) player);
        }
        return new SmartChestMenu(windowId, inv, getBlockPos());
    }

    @Override
    public boolean shouldPreserveStateOnBreak() {
        return true;  // always, even when broken with a pick
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        // drop nothing; contents are serialized to the dropped item
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(NBT_ITEMS, inventory.serializeNBT());
        tag.putInt("pushPull", pushPullModes);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        inventory.deserializeNBT(tag.getCompound(NBT_ITEMS));
        pushPullModes = tag.getInt("pushPull");
    }

    @Override
    public void serializeExtraItemData(CompoundTag blockEntityTag, boolean preserveState) {
        super.serializeExtraItemData(blockEntityTag, preserveState);

        boolean shouldSave = inventory.lastSlot < CHEST_SIZE || rsController.getCurrentMode() != 0;
        if (!shouldSave) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (!inventory.getStackInSlot(i).isEmpty() || !inventory.filter[i].isEmpty()) {
                    shouldSave = true;
                }
            }
        }
        if (shouldSave) {
            blockEntityTag.put(NBT_ITEMS, inventory.serializeNBT());
            blockEntityTag.putInt(NBTKeys.NBT_REDSTONE_MODE, rsController.getCurrentMode());
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        if (tag.startsWith("push_pull:")) {
            String[] s = tag.split(":");
            if (s.length == 2) {
                try {
                    SideConfigurator.RelativeFace face = SideConfigurator.RelativeFace.valueOf(s[1]);
                    cycleMode(face, shiftHeld);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void cycleMode(SideConfigurator.RelativeFace face, boolean shiftHeld) {
        setPushPullMode(face, getPushPullMode(face).cycle(shiftHeld));
    }

    public int getLastSlot() {
        return inventory.getLastSlot();
    }

    public void setLastSlot(int lastSlot) {
        inventory.setLastSlot(lastSlot);
    }

    public List<Pair<Integer, ItemStack>> getFilter() {
        List<Pair<Integer, ItemStack>> res = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.filter[i].isEmpty()) {
                res.add(Pair.of(i, inventory.filter[i].copy()));
            }
        }
        return res;
    }

    public void setFilter(List<Pair<Integer, ItemStack>> l) {
        Arrays.fill(inventory.filter, ItemStack.EMPTY);
        for (Pair<Integer, ItemStack> p : l) {
            inventory.setFilter(p.getLeft(), p.getRight());
        }
    }

    public ItemStack getFilter(int slotId) {
        return inventory.filter[slotId];
    }

    public void setFilter(int slotId, ItemStack stack) {
        inventory.filter[slotId] = stack.copy();
    }

    @Override
    public RedstoneController<SmartChestBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public int getComparatorValue() {
        return inventory.getComparatorValue();
    }

    public enum PushPullMode implements ITranslatableEnum {
        NONE("none"),
        PUSH("push"),
        PULL("pull");

        private final String key;

        PushPullMode(String key) {
            this.key = key;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tooltip.smartChest.mode." + key;
        }

        public PushPullMode cycle(boolean backward) {
            int n = ordinal() + (backward ? -1 : 1);
            if (n < 0) n = values().length - 1;
            else if (n >= values().length) n = 0;
            return PushPullMode.values()[n];
        }
    }

    public static IItemHandler deserializeSmartChest(CompoundTag tag) {
        SmartChestItemHandler res = new SmartChestItemHandler(null, CHEST_SIZE);
        res.deserializeNBT(tag);
        return res;
    }

    public static class SmartChestItemHandler extends ComparatorItemStackHandler {
        private final ItemStack[] filter = new ItemStack[CHEST_SIZE];
        private int lastSlot = CHEST_SIZE;

        SmartChestItemHandler(BlockEntity te, int size) {
            super(te, size);

            Arrays.fill(filter, ItemStack.EMPTY);
        }

        @Override
        public int getSlots() {
            return Math.min(CHEST_SIZE, lastSlot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return filter[slot].isEmpty() ? super.getSlotLimit(slot) : filter[slot].getCount();
        }

        int getLastSlot() {
            return lastSlot;
        }

        void setLastSlot(int lastSlot) {
            for (int i = lastSlot; i < getSlots(); i++) {
                if (!getStackInSlot(i).isEmpty()) return;
            }
            this.lastSlot = lastSlot;
        }

        public void setFilter(int slot, ItemStack stack) {
            filter[slot] = stack;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot < lastSlot
                    && (filter[slot].isEmpty() || filter[slot].getItem() == stack.getItem())
                    && stack.getItem() != ModBlocks.REINFORCED_CHEST.get().asItem()
                    && stack.getItem() != ModBlocks.SMART_CHEST.get().asItem()
                    && super.isItemValid(slot, stack);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();

            tag.putInt("LastSlot", lastSlot);
            ListTag l = new ListTag();
            for (int i = 0; i < CHEST_SIZE; i++) {
                if (!filter[i].isEmpty()) {
                    CompoundTag subTag = new CompoundTag();
                    subTag.putInt("Slot", i);
                    filter[i].save(subTag);
                    l.add(subTag);
                }
            }
            tag.put("Filter", l);
            tag.putBoolean("V2", true);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            super.deserializeNBT(nbt);

            lastSlot = nbt.getInt("LastSlot");
            ListTag l = nbt.getList("Filter", Tag.TAG_COMPOUND);
            boolean isV2 = nbt.getBoolean("V2");
            for (int i = 0; i < l.size(); i++) {
                CompoundTag tag = l.getCompound(i);
                ItemStack stack = ItemStack.of(tag);
                if (!isV2 && stack.getCount() == 1) stack.setCount(stack.getMaxStackSize());
                filter[tag.getInt("Slot")] = stack;
            }
        }
    }
}
