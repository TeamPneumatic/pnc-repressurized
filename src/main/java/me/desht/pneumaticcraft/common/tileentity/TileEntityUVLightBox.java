package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockUVLightBox;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemEmptyPCB;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//import elucent.albedo.lighting.ILightProvider;
//import elucent.albedo.lighting.Light;

//@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileEntityUVLightBox extends TileEntityPneumaticBase implements
        IMinWorkingPressure, IRedstoneControl<TileEntityUVLightBox>, INamedContainerProvider /*,ILightProvider */ {
    private static final String NBT_EXPOSURE = "pneumaticcraft:uv_exposure";

    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_SLOT = 0;

    private static final List<RedstoneMode<TileEntityUVLightBox>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("standard.always", new ItemStack(Items.GUNPOWDER),
                    te -> true),
            new ReceivingRedstoneMode<>("standard.high_signal", new ItemStack(Items.REDSTONE),
                    te -> te.getCurrentRedstonePower() > 0),
            new ReceivingRedstoneMode<>("standard.low_signal", new ItemStack(Items.REDSTONE_TORCH),
                    te -> te.getCurrentRedstonePower() == 0),
            new ReceivingRedstoneMode<>("uvLightBox.interpolate", new ItemStack(Items.COMPARATOR),
                    te -> te.getCurrentRedstonePower() > 0)
    );
    public static final int RS_MODE_INTERPOLATE = 3;

//    private Object light = null;

    // avoid rapid blockstate switching, which is a framerate killer
    private long lastStateUpdate = 0;
    private BlockState pendingState;

    @GuiSynced
    public final RedstoneController<TileEntityUVLightBox> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @GuiSynced
    public int threshold = 100;

    private final UVInputHandler inputHandler = new UVInputHandler();
    private final ItemStackHandler outputHandler = new BaseItemStackHandler(this, INVENTORY_SIZE);
    private final UVInvWrapper inventoryExt = new UVInvWrapper();
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventoryExt);
    private LazyOptional<IItemHandler> cachedEjectHandler = LazyOptional.empty();

    public int ticksExisted;

    public TileEntityUVLightBox() {
        super(ModTileEntities.UV_LIGHT_BOX.get(), PneumaticValues.DANGER_PRESSURE_UV_LIGHTBOX, PneumaticValues.MAX_PRESSURE_UV_LIGHTBOX, PneumaticValues.VOLUME_UV_LIGHTBOX, 4);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide) {
            ticksExisted++;
            ItemStack stack = getLoadedPCB();
            boolean didWork = false;
            if (rsController.getCurrentMode() == RS_MODE_INTERPOLATE) {
                threshold = (rsController.getCurrentRedstonePower() * 100) / 15;
                threshold = (threshold / 10) * 10;  // round to multiple of 10
            }
            if (!stack.isEmpty() && rsController.shouldRun()) {
                int progress = getExposureProgress(stack);
                if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && progress < 100) {
                    addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                    if (ticksExisted % ticksPerProgress(progress) == 0) {
                        progress++;
                        setExposureProgress(stack, progress);
                    }
                    if (progress >= threshold) {
                        if (outputHandler.insertItem(0, inputHandler.getStackInSlot(0), true).isEmpty()) {
                            ItemStack toMove = inputHandler.extractItem(0, 1, false);
                            outputHandler.insertItem(0, toMove, false);
                        }
                    }
                    didWork = true;
                }
            }
            if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
                tryEject();
            }
            checkStateUpdates(stack, didWork);
        }
    }

    private void checkStateUpdates(ItemStack loadedStack, boolean didWork) {
        BlockState state = getBlockState();
        if (state.getBlock() == ModBlocks.UV_LIGHT_BOX.get()) {
            boolean loaded = state.getValue(BlockUVLightBox.LOADED);
            boolean update = false;
            if (loaded == loadedStack.isEmpty()) {
                state = state.setValue(BlockUVLightBox.LOADED, !loadedStack.isEmpty());
                update = true;
            }
            if (didWork != getBlockState().getValue(BlockUVLightBox.LIT)) {
                state = state.setValue(BlockUVLightBox.LIT, didWork);
                update = true;
            }
            long now = level.getGameTime();
            if (update) {
                if (now - lastStateUpdate > 10) {
                    level.setBlockAndUpdate(worldPosition, state);
                    pendingState = null;
                    lastStateUpdate = now;
                } else {
                    pendingState = state;
                }
            } else if (pendingState != null && now - lastStateUpdate > 10) {
                level.setBlockAndUpdate(worldPosition, pendingState);
                pendingState = null;
                lastStateUpdate = now;
            }
        }
    }

    private void tryEject() {
        Direction dir = getUpgradeCache().getEjectDirection();
        if (dir != null) {
            getEjectionHandler().ifPresent(handler -> {
                ItemStack stack = outputHandler.extractItem(0, 1, true);
                if (!stack.isEmpty() && ItemHandlerHelper.insertItem(handler, stack, false).isEmpty()) {
                    outputHandler.extractItem(0, 1, false);
                }
            });
        }
    }

    private LazyOptional<IItemHandler> getEjectionHandler() {
        if (!cachedEjectHandler.isPresent()) {
            Direction dir = getUpgradeCache().getEjectDirection();
            TileEntity te = level.getBlockEntity(worldPosition.relative(dir));
            cachedEjectHandler = IOHelper.getInventoryForTE(te, dir.getOpposite());
            if (cachedEjectHandler.isPresent()) {
                cachedEjectHandler.addListener(l -> cachedEjectHandler = LazyOptional.empty());
            }
        }
        return cachedEjectHandler;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        threshold = tag.getInt("threshold");
        inputHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.putInt("threshold", threshold);
        nbt.put("Items", inputHandler.serializeNBT());
        return nbt;
    }

    public static int getExposureProgress(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(NBT_EXPOSURE) : 0;
    }

    public static void setExposureProgress(ItemStack stack, int progress) {
        Validate.isTrue(progress >= 0 && progress <= 100);
        stack.getOrCreateTag().putInt(NBT_EXPOSURE, progress);
    }

    private int ticksPerProgress(int progress) {
        int ticks;
        if (progress < 20) {
            ticks = 20;
        } else if (progress < 40) {
            ticks = 40;
        } else if (progress < 60) {
            ticks = 80;
        } else if (progress < 80) {
            ticks = 160;
        } else {
            ticks = 300;
        }
        return Math.max(1, (int) (ticks / getSpeedMultiplierFromUpgrades()));
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void onDescUpdate() {
        getLevel().getChunkSource().getLightEngine().checkBlock(getBlockPos());

        super.onDescUpdate();
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side == getRotation();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag))
            return;
        try {
            threshold = MathHelper.clamp(Integer.parseInt(tag), 1, 100);
            setChanged();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inputHandler;
    }

    public IItemHandler getOutputInventory() {
        return outputHandler;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    @Override
    public RedstoneController<TileEntityUVLightBox> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX;
    }

    private ItemStack getLoadedPCB() {
        return inputHandler.getStackInSlot(PCB_SLOT);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerUVLightBox(i, playerInventory, getBlockPos());
    }

    public int getThreshold() {
        return threshold;
    }

    /*
    @Optional.Method(modid = "albedo")
    @Override
    public Light provideLight() {
        if (light == null && areLightsOn) {
            int radius = Math.max(8, 4 + getUpgrades(EnumUpgrade.SPEED));
            light = Light.builder().pos(pos()).color(0.2f, 0.0f, 1.0f).radius(radius).build();
        } else if (!areLightsOn) {
            light = null;
        }
        return (Light) light;
    }
    */

    private class UVInputHandler extends BaseItemStackHandler {
        UVInputHandler() {
            super(TileEntityUVLightBox.this, INVENTORY_SIZE);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1; // Only process one item at a time.
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemEmptyPCB && ItemEmptyPCB.getEtchProgress(itemStack) == 0;
        }
    }

    private class UVInvWrapper implements IItemHandler {
        UVInvWrapper() {
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? inputHandler.getStackInSlot(0) : outputHandler.getStackInSlot(0);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot == 0 ? inputHandler.insertItem(0, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? outputHandler.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? inputHandler.getSlotLimit(0) : outputHandler.getSlotLimit(0);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? inputHandler.isItemValid(0, stack) : outputHandler.isItemValid(0, stack);
        }
    }
}
