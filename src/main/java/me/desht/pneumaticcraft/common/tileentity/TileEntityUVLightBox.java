package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockUVLightBox;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemEmptyPCB;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//import elucent.albedo.lighting.ILightProvider;
//import elucent.albedo.lighting.Light;

//@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileEntityUVLightBox extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl, INamedContainerProvider /*,ILightProvider */ {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.70",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.80",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.90",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.100"
    );

    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_SLOT = 0;

//    private Object light = null;

    @DescSynced
    public boolean areLightsOn;
    @GuiSynced
    public int redstoneMode;
//    @DescSynced
//    public boolean hasLoadedPCB;

    public final LightBoxItemHandlerInternal inventory = new LightBoxItemHandlerInternal();
    private final LightBoxItemHandlerExternal inventoryExt = new LightBoxItemHandlerExternal(inventory);
    private final LazyOptional<IItemHandlerModifiable> invCap = LazyOptional.of(() -> inventoryExt);
    public int ticksExisted;
    private boolean oldRedstoneStatus;

    public TileEntityUVLightBox() {
        super(ModTileEntityTypes.UV_LIGHT_BOX, PneumaticValues.DANGER_PRESSURE_UV_LIGHTBOX, PneumaticValues.MAX_PRESSURE_UV_LIGHTBOX, PneumaticValues.VOLUME_UV_LIGHTBOX, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        redstoneMode = nbt.getInt("redstoneMode");
        inventory.deserializeNBT(nbt.getCompound("Items"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putInt("redstoneMode", redstoneMode);
        nbt.put("Items", inventory.serializeNBT());
        return nbt;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            ticksExisted++;
            ItemStack stack = getLoadedPCB();
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && stack.getItem() instanceof ItemEmptyPCB && stack.getDamage() > 0) {
                addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                if (ticksExisted % ticksPerProgress(stack.getDamage()) == 0) {
                    if (!areLightsOn) {
                        setLightsOn(true);
                        updateNeighbours();
                    }
                    stack.setDamage(Math.max(0, stack.getDamage() - 1));
                }
            } else if (areLightsOn) {
                setLightsOn(false);
                updateNeighbours();
            }
            if (oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = !oldRedstoneStatus;
                updateNeighbours();
            }
            boolean loaded = getBlockState().get(BlockUVLightBox.LOADED);
            if (loaded == stack.isEmpty()) {
                world.setBlockState(pos, getBlockState().with(BlockUVLightBox.LOADED, !stack.isEmpty()));
            }
        }
    }

//    @Override
//    protected void onFirstServerUpdate() {
//        super.onFirstServerUpdate();
//
//        hasLoadedPCB = !getLoadedPCB().isEmpty();
//    }

    private int ticksPerProgress(int damage) {
        int ticks;
        if (damage > 80) {
            ticks = 20;
        } else if (damage > 60) {
            ticks = 40;
        } else if (damage > 40) {
            ticks = 80;
        } else if (damage > 20) {
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

    private void setLightsOn(boolean lightsOn) {
        boolean check = areLightsOn != lightsOn;
        areLightsOn = lightsOn;
        if (check) {
            world.getChunkProvider().getLightManager().checkBlock(getPos());
            BlockState state = world.getBlockState(pos).with(BlockUVLightBox.LIT, areLightsOn);
            world.setBlockState(pos, state);
        }
    }

    @Override
    public void onDescUpdate() {
        getWorld().getChunkProvider().getLightManager().checkBlock(getPos());

        super.onDescUpdate();
    }

    public int getLightLevel() {
        return areLightsOn ? Math.max(15, getUpgrades(EnumUpgrade.SPEED)) + 11 : 0;
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return side == getRotation().rotateYCCW();
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 4) redstoneMode = 0;
            updateNeighbours();
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    public boolean shouldEmitRedstone() {
        ItemStack stack = getLoadedPCB();
        if (redstoneMode == 0 || stack.getItem() != ModItems.EMPTY_PCB) return false;
        switch (redstoneMode) {
            case 1:
                return stack.getDamage() < 30;
            case 2:
                return stack.getDamage() < 20;
            case 3:
                return stack.getDamage() < 10;
            case 4:
                return stack.getDamage() == 0;
        }
        return false;
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return invCap;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX;
    }

    private ItemStack getLoadedPCB() {
        return inventory.getStackInSlot(PCB_SLOT);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerUVLightBox(i, playerInventory, getPos());
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

    private class LightBoxItemHandlerInternal extends BaseItemStackHandler {
        LightBoxItemHandlerInternal() {
            super(TileEntityUVLightBox.this, INVENTORY_SIZE);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1; // Only process one item at a time.
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemEmptyPCB;
        }

//        @Override
//        protected void onContentsChanged(int slot) {
//            super.onContentsChanged(slot);
//
//        }
    }

    private class LightBoxItemHandlerExternal implements IItemHandlerModifiable {
        private final LightBoxItemHandlerInternal wrapped;

        LightBoxItemHandlerExternal(LightBoxItemHandlerInternal wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int getSlots() {
            return wrapped.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return wrapped.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return wrapped.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (redstoneMode != 0 && !shouldEmitRedstone()) {
                return ItemStack.EMPTY;
            } else {
                return wrapped.extractItem(slot, amount, simulate);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return wrapped.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return wrapped.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            wrapped.setStackInSlot(slot, stack);
        }
    }
}
