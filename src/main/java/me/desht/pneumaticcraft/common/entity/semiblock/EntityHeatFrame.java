package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EntityHeatFrame extends EntitySemiblockBase {
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(EntityHeatFrame.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> TEMPERATURE = EntityDataManager.createKey(EntityHeatFrame.class, DataSerializers.VARINT);

    private static final int MIN_COOKING_TEMP = 373;

    private static final byte IDLE = 0;
    private static final byte COOKING = 1;
    private static final byte COOLING = 2;

    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private final LazyOptional<IHeatExchangerLogic> heatCap;
    private int lastValidSlot; // cache the current cooking slot for performance boost
    private int cookingProgress;

    private int coolingProgress;

    private SyncedTemperature syncedTemperature = new SyncedTemperature();

    public EntityHeatFrame(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

        heatCap = LazyOptional.of(() -> logic);
    }

    public IHeatExchangerLogic getHeatExchangerLogic() {
        return logic;
    }

    @Override
    protected void registerData() {
        super.registerData();

        this.dataManager.register(STATUS, IDLE);
        this.dataManager.register(TEMPERATURE, 0);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.HEAT_EXCHANGER_CAPABILITY) {
            return PNCCapabilities.HEAT_EXCHANGER_CAPABILITY.orEmpty(cap, heatCap);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canPlace(Direction facing) {
        return getCachedTileEntity() != null && getCachedTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
    }

    private void setStatus(byte status) {
        getDataManager().set(STATUS, status);
    }

    private byte getStatus() {
        return getDataManager().get(STATUS);
    }

    private void setSyncedTemperature(int temperature) {
        getDataManager().set(TEMPERATURE, temperature);
    }

    public int getSyncedTemperature() {
        return getDataManager().get(TEMPERATURE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            byte newStatus = IDLE;
            if (logic.getTemperature() > MIN_COOKING_TEMP) {
                newStatus = doCooking();
            } else if (logic.getTemperature() < HeatFrameCoolingRecipeImpl.getMaxThresholdTemp()) {
                newStatus = doCooling();
            }
            setStatus(newStatus);
            syncedTemperature.setCurrentTemp(logic.getTemperature());
            setSyncedTemperature(syncedTemperature.getSyncedTemp());
        } else {
            // client
            if ((ticksExisted & 0x3) == 0) {
                byte status = getStatus();
                switch (status) {
                    case COOKING:
                        ClientUtils.emitParticles(world, getBlockPos(), world.rand.nextInt(4) == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE);
                        break;
                    case COOLING:
                        ClientUtils.emitParticles(world, getBlockPos(), ParticleTypes.SPIT);
                        break;
                }
            }
        }
    }

    private byte doCooking() {
        byte newStatus = IDLE;
        if (cookingProgress < 100) {
            int progress = Math.max(0, ((int) logic.getTemperature() - 343) / 30);
            progress = Math.min(5, progress);
            logic.addHeat(-progress);
            cookingProgress += progress;
            newStatus = COOKING;
        }
        if (cookingProgress >= 100) {
            IOHelper.getInventoryForTE(getCachedTileEntity()).ifPresent(handler -> {
                if (!tryCookSlot(handler, lastValidSlot)) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (tryCookSlot(handler, i)) {
                            cookingProgress -= 100;
                            break;
                        }
                    }
                } else {
                    cookingProgress -= 100;
                }
            });
        }
        return newStatus;
    }

    private boolean tryCookSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            Inventory inv = new Inventory(1);
            inv.setInventorySlotContents(0, stack);
            return world.getRecipeManager().getRecipe(IRecipeType.SMELTING, inv, this.world).map(recipe -> {
                ItemStack result = recipe.getRecipeOutput();
                if (!result.isEmpty()) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, result, true);
                    if (remainder.isEmpty()) {
                        handler.extractItem(slot, 1, false);
                        ItemHandlerHelper.insertItem(handler, result, false);
                        lastValidSlot = slot;
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private byte doCooling() {
        byte newStatus = IDLE;
        if (coolingProgress < 100) {
            int progress = Math.max(0, ((int) logic.getTemperature() - 243) / 30);
            progress = 6 - Math.min(5, progress);
            logic.addHeat(progress);
            coolingProgress += progress;
            newStatus = COOLING;
        }
        if (coolingProgress >= 100) {
            IOHelper.getInventoryForTE(getCachedTileEntity()).ifPresent(handler -> {
                if (!tryCoolSlot(handler, lastValidSlot)) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (tryCoolSlot(handler, i)) {
                            coolingProgress -= 100;
                            break;
                        }
                    }
                } else {
                    coolingProgress -= 100;
                }
            });
        }
        return newStatus;
    }

    private boolean tryCoolSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (stack.isEmpty()) return false;

        HeatFrameCoolingRecipe recipe = PneumaticCraftRecipeType.HEAT_FRAME_COOLING.findFirst(world, r -> r.matches(stack));

        if (recipe != null) {
            ItemStack containerItem = stack.getItem().getContainerItem(stack);
            if (!containerItem.isEmpty() && stack.getCount() > 1) return false;
            ItemStack result = ItemHandlerHelper.copyStackWithSize(recipe.getOutput(), recipe.calculateOutputQuantity(logic.getTemperature()));

            ItemStack remainder = ItemHandlerHelper.insertItem(handler, result, true);
            if (remainder.isEmpty()) {
                handler.extractItem(slot, 1, false);
                if (!containerItem.isEmpty()) {
                    handler.insertItem(slot, containerItem, false);
                }
                ItemHandlerHelper.insertItem(handler, result, false);
                lastValidSlot = slot;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        logic.deserializeNBT(tag.getCompound("heatExchanger"));
        cookingProgress = tag.getInt("cookingProgress");
        coolingProgress = tag.getInt("coolingProgress");
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        tag.put("heatExchanger", logic.serializeNBT());
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);

        return super.serializeNBT(tag);
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        int cook, cool;
        if (!world.isRemote) {
            // TOP
            cook = cookingProgress;
            cool = coolingProgress;
        } else {
            // Waila
            logic.deserializeNBT(tag.getCompound("heatExchanger"));
            cook = tag.getInt("cookingProgress");
            cool = tag.getInt("coolingProgress");
        }

        if (getStatus() != COOKING && cook >= 100) cook = 0;
        if (getStatus() != COOLING && cool >= 100) cool = 0;
        curInfo.add(HeatUtil.formatHeatString(logic.getTemperatureAsInt()));
        curInfo.add(PneumaticCraftUtils.xlate("waila.heatFrame.cooking", cook).applyTextStyle(TextFormatting.GRAY));
        curInfo.add(PneumaticCraftUtils.xlate("waila.heatFrame.cooling", cool).applyTextStyle(TextFormatting.GRAY));
    }
}
