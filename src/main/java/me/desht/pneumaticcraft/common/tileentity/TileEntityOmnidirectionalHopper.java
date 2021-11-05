package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.inventory.handler.ComparatorItemStackHandler;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityOmnidirectionalHopper extends TileEntityAbstractHopper<TileEntityOmnidirectionalHopper> {
    public static final int INVENTORY_SIZE = 5;

    private final ComparatorItemStackHandler itemHandler = new ComparatorItemStackHandler(this, getInvSize());
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> itemHandler);
    @GuiSynced
    public boolean roundRobin;
    private int rrSlot;
    @GuiSynced
    private final RedstoneController<TileEntityOmnidirectionalHopper> rsController = new RedstoneController<>(this);

    public TileEntityOmnidirectionalHopper() {
        super(ModTileEntities.OMNIDIRECTIONAL_HOPPER.get());
    }

    protected int getInvSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    protected int getComparatorValueInternal() {
        return itemHandler.getComparatorValue();
    }

    protected boolean doExport(final int maxItems) {
        Direction outputDir = getRotation();

        // TODO cache the capability rather than the TE?
        LazyOptional<IItemHandler> inv = IOHelper.getInventoryForTE(getCachedNeighbor(outputDir), outputDir.getOpposite());
        int notExported = maxItems;
        if (inv.isPresent()) {
            notExported = inv.map(h -> exportToInventory(h, maxItems)).orElse(maxItems);
        } else if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0) {
            notExported = tryEntityExport(maxItems, outputDir.getOpposite());
        }
        if (notExported == maxItems && ConfigHelper.common().machines.omniHopperDispenser.get() && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            notExported = exportToInventory(new DropInWorldHandler(getLevel(), getBlockPos(), outputDir), maxItems);
        }
        return notExported < maxItems;
    }

    private int tryEntityExport(int maxItems, Direction dir) {
        for (Entity e : cachedOutputEntities) {
            if (!e.isAlive()) continue;
            int notExported = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).map(h -> exportToInventory(h, maxItems)).orElse(maxItems);
            if (notExported < maxItems) return notExported;
        }
        return maxItems;
    }

    private int exportToInventory(IItemHandler otherHandler, int maxItems) {
        int remaining = maxItems;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(actualSlot(i));
            if (stack.getCount() > leaveMaterialCount) {
                ItemStack exportedStack = ItemHandlerHelper.copyStackWithSize(stack, Math.min(stack.getCount() - leaveMaterialCount, remaining));
                int toExport = exportedStack.getCount();
                ItemStack excess = ItemHandlerHelper.insertItem(otherHandler, exportedStack, false);
                int exportedCount = toExport - excess.getCount();
                if (!isCreative) {
                    stack.shrink(exportedCount);
                    if (exportedCount > 0) itemHandler.invalidateComparatorValue();
                }
                remaining -= exportedCount;
                if (remaining <= leaveMaterialCount) {
                    if (roundRobin) {
                        rrSlot = actualSlot(i) + 1;
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

        // Suck from input inventory
        LazyOptional<IItemHandler> cap = IOHelper.getInventoryForTE(getCachedNeighbor(inputDir), inputDir.getOpposite());
        if (cap.isPresent()) {
            int imported = cap.map(otherHandler -> importFromInventory(otherHandler, maxItems, false)).orElse(0);
            return imported > 0;
        } else if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0 && tryEntityImport(maxItems) > 0) {
            return true;
        }

        // Suck in item entities in front of the input
        BlockPos inputPos = worldPosition.relative(inputDir);
        if (!Block.canSupportCenter(level, inputPos, inputDir.getOpposite())) {
            for (Entity e : cachedInputEntities) {
                if (e.isAlive() && e instanceof ItemEntity) {
                    ItemEntity entity = (ItemEntity) e;
                    ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, entity.getItem(), false);
                    if (remainder.isEmpty()) {
                        entity.remove();
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
            if (!e.isAlive()) continue;
            final int r = remaining;
            boolean playerArmor = e instanceof PlayerEntity && dir.getAxis().isHorizontal();
            int imported = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).map(h -> importFromInventory(h, r, playerArmor)).orElse(0);
            remaining -= imported;
            if (remaining <= 0) return maxItems - remaining;
        }
        return 0;
    }

    private int importFromInventory(IItemHandler inv, int maxItems, boolean playerArmor) {
        int remaining = maxItems;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).isEmpty()) continue;
            ItemStack toExtract = inv.extractItem(i, remaining, true);
            if (playerArmor && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, toExtract) > 0) {
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
        AxisAlignedBB bowl = BlockOmnidirectionalHopper.INPUT_SHAPES[inputDir.get3DDataValue()].bounds().move(worldPosition);
        inputAABB = bowl.minmax(new AxisAlignedBB(worldPosition.relative(inputDir)));
        // output zone is a bit simpler
        outputAABB = new AxisAlignedBB(getBlockPos().relative(getRotation()));

        cachedInputEntities.clear();
        cachedOutputEntities.clear();
    }

    @Override
    boolean shouldScanForEntities(Direction dir) {
        if (Block.canSupportCenter(level, worldPosition.relative(dir), dir.getOpposite())
                || dir == getRotation() && getUpgrades(EnumUpgrade.ENTITY_TRACKER) == 0) {
            return false;
        }
        TileEntity te = getCachedNeighbor(dir);
        return te == null || !te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).isPresent();
    }

    @Override
    public int getItemTransferInterval() {
        return 8 / (1 << getUpgrades(EnumUpgrade.SPEED));
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putBoolean("RoundRobin", roundRobin);
        if (roundRobin) tag.putInt("RRSlot", rrSlot);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        roundRobin = tag.getBoolean("RoundRobin");
        rrSlot = tag.getInt("RRSlot");
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerOmnidirectionalHopper(i, playerInventory, getBlockPos());
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (tag.equals("rr")) {
            roundRobin = !roundRobin;
            setChanged();
        } else {
            super.handleGUIButtonPress(tag, shiftHeld, player);
        }
    }

    @Override
    public RedstoneController<TileEntityOmnidirectionalHopper> getRedstoneController() {
        return rsController;
    }

    private static class DropInWorldHandler implements IItemHandler {
        private final World world;
        private final BlockPos pos;
        private final Direction outputDir;

        public DropInWorldHandler(World world, BlockPos pos, Direction outputDir) {
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
