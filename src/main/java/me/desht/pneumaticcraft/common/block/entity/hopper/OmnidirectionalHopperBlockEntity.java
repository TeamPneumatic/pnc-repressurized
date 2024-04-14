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

package me.desht.pneumaticcraft.common.block.entity.hopper;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.block.OmnidirectionalHopperBlock;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.OmnidirectionalHopperMenu;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OmnidirectionalHopperBlockEntity extends AbstractHopperBlockEntity<OmnidirectionalHopperBlockEntity> {
    public static final int INVENTORY_SIZE = 5;

    private final ComparatorItemStackHandler itemHandler = new ComparatorItemStackHandler(this, getInvSize());
//    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> itemHandler);
    @GuiSynced
    public boolean roundRobin;
    private int rrSlot;
    @GuiSynced
    private final RedstoneController<OmnidirectionalHopperBlockEntity> rsController = new RedstoneController<>(this);

    private BlockCapabilityCache<IItemHandler,Direction> inputCache, outputCache;

    public OmnidirectionalHopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.OMNIDIRECTIONAL_HOPPER.get(), pos, state);
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    private BlockCapabilityCache<IItemHandler,Direction> getInputCache() {
        if (inputCache == null) {
            inputCache = createItemHandlerCache(inputDir);
        }
        return inputCache;
    }

    private BlockCapabilityCache<IItemHandler,Direction> getOutputCache() {
        if (outputCache == null) {
            outputCache = createItemHandlerCache(getRotation());
        }
        return outputCache;
    }

    @Override
    public void onBlockRotated() {
        super.onBlockRotated();
        inputCache = outputCache = null;
    }

    protected int getComparatorValueInternal() {
        return itemHandler.getComparatorValue();
    }

    protected boolean doExport(final int maxItems) {
        Direction outputDir = getRotation();

        IItemHandler destInv = getOutputCache().getCapability();

        int notExported = maxItems;
        if (destInv != null) {
            notExported = exportToInventory(destInv, maxItems);
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            notExported = tryEntityExport(maxItems, outputDir.getOpposite());
        }
        if (notExported == maxItems && ConfigHelper.common().machines.omniHopperDispenser.get() && getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
            notExported = exportToInventory(new DropInWorldHandler(getLevel(), getBlockPos(), outputDir), maxItems);
        }
        return notExported < maxItems;
    }

    private int tryEntityExport(int maxItems, Direction dir) {
        for (Entity e : cachedOutputEntities) {
            if (!e.isAlive()) continue;
            int notExported = IOHelper.getInventoryForEntity(e, dir).map(h -> exportToInventory(h, maxItems)).orElse(maxItems);
            if (notExported < maxItems) return notExported;
        }
        return maxItems;
    }

    private int exportToInventory(IItemHandler otherHandler, int maxItems) {
        int remaining = maxItems;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            int slot = actualSlot(i);
            int amount = itemHandler.getStackInSlot(slot).getCount();
            ItemStack exportedStack = itemHandler.extractItem(slot, amount - leaveMaterialCount, true);  //getStackInSlot(actualSlot(i));
            if (exportedStack.getCount() > leaveMaterialCount) {
                exportedStack.setCount(Math.min(exportedStack.getCount(), remaining));
                ItemStack excess = ItemHandlerHelper.insertItem(otherHandler, exportedStack, false);
                int exportedCount = exportedStack.getCount() - excess.getCount();
                if (!isCreative && exportedCount > 0) {
                    itemHandler.extractItem(slot, exportedCount, false);
                }
                remaining -= exportedCount;
                if (remaining <= leaveMaterialCount) {
                    if (roundRobin) {
                        rrSlot = slot + 1;
                        if (rrSlot >= itemHandler.getSlots()) rrSlot = 0;
                    }
                    break;
                }
            }
        }
        return remaining;
    }

    private int actualSlot(int i) {
        if (roundRobin) {
            int slot = rrSlot + i;
            if (slot >= itemHandler.getSlots()) slot -= itemHandler.getSlots();
            return slot;
        } else {
            return i;
        }
    }

    protected boolean doImport(final int maxItems) {
        boolean success = false;

        if (isInventoryFull()) {
            return false;
        }

        IItemHandler srcInv = getInputCache().getCapability();

        // Suck from input inventory
        if (srcInv != null) {
            int imported = importFromInventory(srcInv, maxItems, false);
            return imported > 0;
        } else if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0 && tryEntityImport(maxItems) > 0) {
            return true;
        }

        // Suck in item entities in front of the input
        if (!isInputBlocked()) {
            for (Entity e : cachedInputEntities) {
                if (e.isAlive() && e instanceof ItemEntity entity) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, entity.getItem(), false);
                    if (remainder.isEmpty()) {
                        entity.discard();
                        success = true;
                    } else if (remainder.getCount() < entity.getItem().getCount()) {
                        // some but not all were inserted
                        entity.setItem(remainder);
                        success = true;
                    }
                }
            }
        }

        return success;
    }

    private int tryEntityImport(int maxItems) {
        Direction dir = inputDir.getOpposite();
        int remaining = maxItems;
        for (Entity e : cachedInputEntities) {
            if (e.isAlive() && !e.getType().is(PneumaticCraftTags.EntityTypes.OMNIHOPPER_BLACKLISTED)) {
                final int r = remaining;
                boolean playerArmor = e instanceof Player && dir.getAxis().isHorizontal();
                int imported = IOHelper.getInventoryForEntity(e, dir)
                        .map(h -> importFromInventory(h, r, playerArmor))
                        .orElse(0);
                remaining -= imported;
                if (remaining <= 0) return maxItems - remaining;
            }
        }
        return 0;
    }

    private int importFromInventory(IItemHandler inv, int maxItems, boolean playerArmor) {
        int remaining = maxItems;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).isEmpty()) continue;
            ItemStack toExtract = inv.extractItem(i, remaining, true);
            if (playerArmor && EnchantmentHelper.hasBindingCurse(toExtract)) {
                continue;
            }
            ItemStack excess = ItemHandlerHelper.insertItemStacked(itemHandler, toExtract, false);
            int transferred = toExtract.getCount() - excess.getCount();
            if (transferred > 0) {
                inv.extractItem(i, transferred, false);
                remaining -= transferred;
                if (remaining <= 0) {
                    return maxItems;
                }
            }
        }
        return maxItems - remaining;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void setupInputOutputRegions() {
        // Ensure the input region also contains the hollow part of the hopper itself
        AABB bowl = OmnidirectionalHopperBlock.INPUT_SHAPES[inputDir.get3DDataValue()].bounds().move(worldPosition);
        inputAABB = bowl.minmax(new AABB(worldPosition.relative(inputDir)));
        // output zone is a bit simpler
        outputAABB = new AABB(getBlockPos().relative(getRotation()));

        cachedInputEntities.clear();
        cachedOutputEntities.clear();
    }

    @Override
    boolean shouldScanForEntities(Direction dir) {
        if (isInputBlocked() || dir == getRotation() && getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) == 0) {
            return false;
        }
        BlockEntity te = getCachedNeighbor(dir);
        return te == null || IOHelper.getInventoryForBlock(te, dir.getOpposite()).isEmpty();
    }

    @Override
    public int getItemTransferInterval() {
        return 8 / (1 << getUpgrades(ModUpgrades.SPEED.get()));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putBoolean("RoundRobin", roundRobin);
        if (roundRobin) tag.putInt("RRSlot", rrSlot);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        roundRobin = tag.getBoolean("RoundRobin");
        rrSlot = tag.getInt("RRSlot");
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return itemHandler;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new OmnidirectionalHopperMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (tag.equals("rr")) {
            roundRobin = !roundRobin;
            setChanged();
        } else {
            super.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    @Override
    public RedstoneController<OmnidirectionalHopperBlockEntity> getRedstoneController() {
        return rsController;
    }

    private record DropInWorldHandler(Level world, BlockPos pos, Direction outputDir) implements IItemHandler {
        private DropInWorldHandler(Level world, BlockPos pos, Direction outputDir) {
            this.world = world;
            this.pos = pos.relative(outputDir);
            this.outputDir = outputDir;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!Block.canSupportCenter(world, pos, outputDir.getOpposite())) {
                if (!simulate) {
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }
}
