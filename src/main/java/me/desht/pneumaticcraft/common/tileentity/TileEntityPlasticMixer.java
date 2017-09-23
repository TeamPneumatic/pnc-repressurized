package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.fluid.Fluids;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class TileEntityPlasticMixer extends TileEntityBase implements IHeatExchanger, IRedstoneControlled {
    private static final int BASE_TEMPERATURE = FluidRegistry.WATER.getTemperature();
    public static final int INVENTORY_SIZE = 5;
    public static final int DYE_PER_DYE = 0xFF * 10;
    public static final int DYE_BUFFER_MAX = 0xFF * 2 * PneumaticValues.NORMAL_TANK_CAPACITY / 1000;
    public static final String[] DYES = {
            "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray",
            "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"
    };
    public static final int INV_INPUT = 0, INV_OUTPUT = 1, INV_DYE_RED = 2, INV_DYE_GREEN = 3, INV_DYE_BLUE = 4;

    @DescSynced
    private final FluidTank tank = new PlasticFluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ItemStackHandler inventory = new PlasticItemStackHandler();

    private int lastTickInventoryStacksize;
    @GuiSynced
    private final IHeatExchangerLogic hullLogic = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @GuiSynced
    private final IHeatExchangerLogic itemLogic = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @GuiSynced
    public int selectedPlastic = -1;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    public boolean lockSelection;
    @GuiSynced
    public int[] dyeBuffers = new int[3];

    public TileEntityPlasticMixer() {
        super(4);
        hullLogic.addConnectedExchanger(itemLogic);
        hullLogic.setThermalCapacity(100);
    }

    @SideOnly(Side.CLIENT)
    public IHeatExchangerLogic getLogic(int index) {
        switch (index) {
            case 0:
                return hullLogic;
            case 1:
                return itemLogic;
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }

    @SideOnly(Side.CLIENT)
    public IFluidTank getFluidTank() {
        return tank;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public String getName() {
        return Blockss.PLASTIC_MIXER.getUnlocalizedName();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            refillDyeBuffers();
            itemLogic.update();
            ItemStack inputStack = inventory.getStackInSlot(INV_INPUT);
            if (getWorld().getTotalWorldTime() % 20 == 0) { // We don't need to run _that_ often.
                if (inputStack.getCount() > lastTickInventoryStacksize) {
                    int stackIncrease = inputStack.getCount() - lastTickInventoryStacksize;
                    double ratio = (double) inputStack.getCount() / (inputStack.getCount() + stackIncrease);
                    itemLogic.setTemperature((int) (ratio * itemLogic.getTemperature() + (1 - ratio) * BASE_TEMPERATURE));
                } else if (inputStack.isEmpty()) {
                    itemLogic.setTemperature(BASE_TEMPERATURE);
                }

                if (itemLogic.getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    FluidStack moltenPlastic = new FluidStack(Fluids.PLASTIC, inputStack.getCount() * 1000);
                    int maxFill = tank.fill(moltenPlastic, false) / 1000;
                    if (maxFill > 0) {
                        inventory.extractItem(INV_INPUT, maxFill, false);
                        tank.fill(new FluidStack(moltenPlastic, maxFill * 1000), true);
                    }
                }

                lastTickInventoryStacksize = inventory.getStackInSlot(INV_INPUT).getCount();
                itemLogic.setThermalCapacity(lastTickInventoryStacksize);
            }
            if (tank.getFluid() != null && selectedPlastic >= 0 && redstoneAllows()) {
                ItemStack solidifiedStack = new ItemStack(Itemss.PLASTIC, tank.getFluid().amount / 1000, selectedPlastic);
                if (solidifiedStack.getCount() > 0) {
                    solidifiedStack.setCount(1);
                    if (inventory.getStackInSlot(INV_OUTPUT).isEmpty()) {
                        solidifiedStack.setCount(useDye(solidifiedStack.getCount()));
                        if (solidifiedStack.getCount() > 0) {
                            inventory.setStackInSlot(INV_OUTPUT, solidifiedStack);
                            tank.drain(solidifiedStack.getCount() * 1000, true);
                            sendDescriptionPacket();
                        }
                    } else if (solidifiedStack.isItemEqual(inventory.getStackInSlot(INV_OUTPUT))) {
                        int solidifiedItems = Math.min(64 - inventory.getStackInSlot(INV_OUTPUT).getCount(), solidifiedStack.getCount());
                        solidifiedItems = useDye(solidifiedItems);
                        ItemStack newStack = inventory.getStackInSlot(INV_OUTPUT);
                        newStack.grow(solidifiedItems);
                        inventory.setStackInSlot(INV_OUTPUT, newStack);
                        tank.drain(solidifiedItems * 1000, true);
                        sendDescriptionPacket();
                    }
                }
            }
            if (!lockSelection) selectedPlastic = -1;
            if (redstoneMode == 3) selectedPlastic = poweredRedstone;
        }
    }

    private void refillDyeBuffers() {
        for (int i = 0; i < 3; i++) {
            if (!inventory.getStackInSlot(INV_DYE_RED + i).isEmpty() && dyeBuffers[i] <= DYE_BUFFER_MAX - DYE_PER_DYE) {
                inventory.extractItem(INV_DYE_RED + i, 1, false);
                dyeBuffers[i] += DYE_PER_DYE;
            }
        }
    }

    private int useDye(int maxItems) {
        int desiredColor = ItemDye.DYE_COLORS[selectedPlastic];
        if (selectedPlastic == 15) return maxItems; //Converting to white plastic is free.
        for (int i = 0; i < 3; i++) {
            int colorComponent = desiredColor >> 8 * i & 0xFF;
            colorComponent = 0xFF - colorComponent; //Invert, because we start out with white, and we darken the plastic.
            if (colorComponent > 0) {
                maxItems = Math.min(maxItems, dyeBuffers[i] / colorComponent);
            }
        }
        for (int i = 0; i < 3; i++) {
            int colorComponent = desiredColor >> 8 * i & 0xFF;
            colorComponent = 0xFF - colorComponent; //Invert, because we start out with white, and we darken the plastic.
            dyeBuffers[i] -= colorComponent * maxItems;
        }
        return maxItems;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        lastTickInventoryStacksize = tag.getInteger("lastTickInventoryStacksize");
        selectedPlastic = tag.getInteger("selectedPlastic");
        lockSelection = tag.getBoolean("lockSelection");
        dyeBuffers[0] = tag.getInteger("dyeBuffer0");
        dyeBuffers[1] = tag.getInteger("dyeBuffer1");
        dyeBuffers[2] = tag.getInteger("dyeBuffer2");
        redstoneMode = tag.getInteger("redstoneMode");

        itemLogic.readFromNBT(tag.getCompoundTag("itemLogic"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Items", inventory.serializeNBT());
        tag.setInteger("lastTickInventoryStacksize", lastTickInventoryStacksize);
        tag.setInteger("selectedPlastic", selectedPlastic);
        tag.setBoolean("lockSelection", lockSelection);
        tag.setInteger("dyeBuffer0", dyeBuffers[0]);
        tag.setInteger("dyeBuffer1", dyeBuffers[1]);
        tag.setInteger("dyeBuffer2", dyeBuffers[2]);
        tag.setInteger("redstoneMode", redstoneMode);

        NBTTagCompound heatTag = new NBTTagCompound();
        itemLogic.writeToNBT(heatTag);
        tag.setTag("itemLogic", heatTag);

        return tag;
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        tank.setFluid(null);
        tank.readFromNBT(tag.getCompoundTag("fluid"));
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("fluid", tankTag);
    }

    private static class PlasticFluidTank extends FluidTank {
        PlasticFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return Fluids.areFluidsEqual(fluid.getFluid(), Fluids.PLASTIC);
        }

        @Override
        protected void onContentsChanged() {
            System.out.println("tank contents changed! " + getFluid());
        }
    }

    private static class PlasticItemStackHandler extends FilteredItemStackHandler {
        PlasticItemStackHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            if (itemStack.isEmpty()) return true;
            switch (slot) {
                case INV_INPUT: case INV_OUTPUT: return itemStack.getItem() == Itemss.PLASTIC;
                case INV_DYE_RED: return getDyeIndex(itemStack) == 1;
                case INV_DYE_GREEN: return getDyeIndex(itemStack) == 2;
                case INV_DYE_BLUE: return getDyeIndex(itemStack) == 4;
            }
            return false;
        }
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return hullLogic;
    }

    public static int getDyeIndex(ItemStack stack) {
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int id : ids) {
            String name = OreDictionary.getOreName(id);
            for (int i = 0; i < DYES.length; i++) {
                if (DYES[i].equals(name)) return i;
            }
        }
        return -1;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        super.handleGUIButtonPress(guiID, player);
        if (guiID == 0) {
            if (++redstoneMode > 3) {
                redstoneMode = 0;
            }
        } else if (guiID >= 1 && guiID < 17) {
            if (selectedPlastic != guiID) {
                selectedPlastic = guiID - 1;
//                if (tank.getFluidAmount() >= 1000) {
//                    AchievementHandler.giveAchievement(player, new ItemStack(Itemss.PLASTIC));
//                }
            } else {
                selectedPlastic = -1;
            }
        } else if (guiID == 17) {
            lockSelection = !lockSelection;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public boolean redstoneAllows() {
        return redstoneMode == 3 || super.redstoneAllows();
    }

    @Override
    protected void addLuaMethods() {
        super.addLuaMethods();
        luaMethods.add(new LuaMethod("selectColor") {
            @Override
            public Object[] call(Object[] args) throws Exception {
                if (args.length == 1) {
                    int selection = ((Double) args[0]).intValue();
                    if (selection >= 0 && selection <= 16) {
                        selectedPlastic = selection - 1;
                        return null;
                    } else {
                        throw new IllegalArgumentException("selectColor method only accepts a value ranging from 0-16. The value passed was: " + selection);
                    }
                } else {
                    throw new IllegalArgumentException("selectColor method requires 1 argument (int color index, with 0 being no color");
                }
            }
        });
    }
}
