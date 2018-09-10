package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemEmptyPCB;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileEntityUVLightBox extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl, ILightProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.70",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.80",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.90",
            "gui.tab.redstoneBehaviour.uvLightBox.button.chance.100"
    );

    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_SLOT = 0;

    private Object light = null;

    @DescSynced
    public boolean leftConnected;
    @DescSynced
    public boolean rightConnected;
    @DescSynced
    public boolean areLightsOn;
    @GuiSynced
    public int redstoneMode;
    @DescSynced
    public final LightBoxItemHandlerInternal inventory = new LightBoxItemHandlerInternal();
    public final LightBoxItemHandlerExternal inventoryExt = new LightBoxItemHandlerExternal(inventory);
    public int ticksExisted;
    private boolean oldRedstoneStatus;

    public TileEntityUVLightBox() {
        super(PneumaticValues.DANGER_PRESSURE_UV_LIGHTBOX, PneumaticValues.MAX_PRESSURE_UV_LIGHTBOX, PneumaticValues.VOLUME_UV_LIGHTBOX, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        redstoneMode = nbt.getInteger("redstoneMode");
        inventory.deserializeNBT(nbt.getCompoundTag("Items"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("redstoneMode", redstoneMode);
        nbt.setTag("Items", inventory.serializeNBT());
        return nbt;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            ticksExisted++;
            ItemStack stack = getLoadedPCB();
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && stack.getItem() instanceof ItemEmptyPCB && stack.getItemDamage() > 0) {
                addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                if (ticksExisted % ticksPerProgress(stack.getItemDamage()) == 0) {
                    if (!areLightsOn) {
                        setLightsOn(true);
                        updateNeighbours();
                    }
                    stack.setItemDamage(Math.max(0, stack.getItemDamage() - 1));
                }
            } else if (areLightsOn) {
                setLightsOn(false);
                updateNeighbours();
            }
            if (oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = !oldRedstoneStatus;
                updateNeighbours();
            }
        }
    }

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

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
    }

    private void setLightsOn(boolean lightsOn) {
        boolean check = areLightsOn != lightsOn;
        areLightsOn = lightsOn;
        if (check) {
            getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
            sendDescriptionPacket();
        }
    }

    @Override
    public void onDescUpdate() {
        getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    public int getLightLevel() {
        return areLightsOn ? Math.max(15, getUpgrades(EnumUpgrade.SPEED)) + 11 : 0;
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side == getRotation().rotateYCCW();
    }

    private void updateConnections() {
        leftConnected = false;
        rightConnected = false;

        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            if (entry.getKey() == getRotation().rotateY()) { //TODO 1.8 test
                leftConnected = true;
            } else if (entry.getKey() == getRotation().rotateYCCW()) {
                rightConnected = true;
            }
        }
    }

    @Override
    public String getName() {
        return Blockss.UV_LIGHT_BOX.getUnlocalizedName();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 4) redstoneMode = 0;
            updateNeighbours();
        }
    }

    public boolean shouldEmitRedstone() {
        ItemStack stack = getLoadedPCB();
        if (redstoneMode == 0 || stack.getItem() != Itemss.EMPTY_PCB) return false;
        switch (redstoneMode) {
            case 1:
                return stack.getItemDamage() < 30;
            case 2:
                return stack.getItemDamage() < 20;
            case 3:
                return stack.getItemDamage() < 10;
            case 4:
                return stack.getItemDamage() == 0;
        }
        return false;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        // return the wrapped item handler when accessed via capability
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventoryExt);
        }
        return super.getCapability(capability, facing);
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

    public ItemStack getLoadedPCB() {
        return inventory.getStackInSlot(PCB_SLOT);
    }

    @Optional.Method(modid = "albedo")
    @Override
    public Light provideLight() {
        if (light == null && areLightsOn) {
            int radius = Math.max(8, 4 + getUpgrades(EnumUpgrade.SPEED));
            light = new Light(pos().getX(), pos.getY(), pos().getZ(), 0.2f, 0.0f, 1.0f, 1.0f, radius);
        } else if (!areLightsOn) {
            light = null;
        }
        return (Light) light;
    }

    private class LightBoxItemHandlerInternal extends FilteredItemStackHandler {
        LightBoxItemHandlerInternal() {
            super(INVENTORY_SIZE);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1; // Only process one item at a time.
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemEmptyPCB;
        }
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
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            wrapped.setStackInSlot(slot, stack);
        }
    }
}
