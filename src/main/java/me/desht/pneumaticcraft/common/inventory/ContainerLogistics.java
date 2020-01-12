package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.item.ItemLogisticsFrame;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ContainerLogistics extends ContainerPneumaticBase<TileEntityBase> {
    public final SemiBlockLogistics logistics;
    private final boolean itemContainer;  // true if GUI opened from held item, false if from in-world semiblock

    public ContainerLogistics(ContainerType<?> containerType, int i, PlayerInventory playerInventory, BlockPos pos) {
        super(containerType, i, playerInventory);

        if (pos.equals(BlockPos.ZERO)) {
            this.logistics = getLogistics(playerInventory.player.world, getHeldLogisticsFrame(playerInventory.player));
            this.itemContainer = true;
        } else {
            World world = playerInventory.player.world;
            this.logistics = SemiBlockManager.getInstance(world).getSemiBlock(SemiBlockLogistics.class, world, pos);
            this.itemContainer = false;
        }
        if (logistics != null) {
            addSyncedFields(logistics);
            IItemHandler requests = logistics.getFilters();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    addSlot(logistics.canFilterStack() ?
                            new SlotPhantom(requests, y * 9 + x, x * 18 + 8, y * 18 + 29) :
                            new SlotPhantomUnstackable(requests, y * 9 + x, x * 18 + 8, y * 18 + 29));
                }
            }

            addPlayerSlots(playerInventory, 134);
        }
    }

    private ContainerLogistics(ContainerType logisticsFrameRequester, int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(logisticsFrameRequester, i, playerInventory, getTilePos(buffer));
    }

    public static SemiBlockLogistics getLogistics(World world, ItemStack stack) {
        return getLogistics(world, null, stack);
    }

    private static SemiBlockLogistics getLogistics(World world, PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof ItemLogisticsFrame) {
            SemiBlockLogistics logistics = (SemiBlockLogistics) SemiBlockManager.getSemiBlockForKey(stack.getItem().getRegistryName());
            if (logistics != null) {
                logistics.initialize(world, BlockPos.ZERO);
                logistics.onPlaced(player, stack, null);
                return logistics;
            }
        }
        return null;
    }

    public boolean isItemContainer() {
        return itemContainer;
    }

    /**
     * Called when the container is closed. If configuring a logistics frame in-hand, update its NBT now.
     */
    @Override
    public void onContainerClosed(PlayerEntity player) {
        if (itemContainer && logistics != null) {
            ItemStack logisticsStack = getHeldLogisticsFrame(player);
            if (!logisticsStack.isEmpty()) {
                NonNullList<ItemStack> drops = NonNullList.create();
                logistics.addDrops(drops);
                CompoundNBT settingTag = drops.get(0).getTag();
                logisticsStack.setTag(settingTag != null ? settingTag.copy() : null);
            }
        }
    }

    private ItemStack getHeldLogisticsFrame(PlayerEntity player) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItem(Hand.OFF_HAND).getItem() instanceof ItemLogisticsFrame) {
            return player.getHeldItem(Hand.OFF_HAND);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        super.handleGUIButtonPress(tag, player);
        if (logistics != null) {
            logistics.handleGUIButtonPress(tag, player);
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return logistics != null && !logistics.isInvalid();
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int slotIndex) {
        Slot srcSlot = inventorySlots.get(slotIndex);
        if (slotIndex >= playerSlotsStart && srcSlot != null && srcSlot.getHasStack()) {
            // shift-click from player inventory into filter
            ItemStack stackInSlot = srcSlot.getStack();
            for (int i = 0; i < 27; i++) {
                Slot slot = inventorySlots.get(i);
                if (!slot.getHasStack()) {
                    slot.putStack(stackInSlot.copy());
                    slot.getStack().setCount(slot.getSlotStackLimit());
                    break;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static ContainerLogistics createPassiveProviderContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), i, playerInventory, buffer);
    }

    public static ContainerLogistics createRequesterContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_REQUESTER.get(), i, playerInventory, buffer);
    }

    public static ContainerLogistics createStorageContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_STORAGE.get(), i, playerInventory, buffer);
    }

    public static ContainerLogistics createDefaultStorageContainer(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerLogistics(ModContainers.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), i, playerInventory, buffer);
    }
}
