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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.SyncedTemperature;
import me.desht.pneumaticcraft.common.recipes.VanillaRecipeCache;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.function.Consumer;

public class HeatFrameEntity extends AbstractSemiblockEntity {
    private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(HeatFrameEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> TEMPERATURE = SynchedEntityData.defineId(HeatFrameEntity.class, EntityDataSerializers.INT);

    private static final int MIN_COOKING_TEMP = 373;

    private static final byte IDLE = 0;
    private static final byte COOKING = 1;
    private static final byte COOLING = 2;

    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();

    private int lastValidSlot; // cache the current cooking slot for performance boost
    private int cookingProgress;

    private int coolingProgress;

    private final SyncedTemperature syncedTemperature = new SyncedTemperature(logic);

    public HeatFrameEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public IHeatExchangerLogic getHeatExchangerLogic() {
        return logic;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(STATUS, IDLE);
        this.entityData.define(TEMPERATURE, 0);
    }

    @Override
    public boolean canPlace(Direction facing) {
        BlockEntity te = getCachedTileEntity();
        return te != null
                && IOHelper.getInventoryForBlock(te).isPresent()
                && IOHelper.getFluidHandlerForBlock(te).isEmpty();
    }

    private void setStatus(byte status) {
        getEntityData().set(STATUS, status);
    }

    private byte getStatus() {
        return getEntityData().get(STATUS);
    }

    private void setSyncedTemperature(int temperature) {
        getEntityData().set(TEMPERATURE, temperature);
    }

    public int getSyncedTemperature() {
        return getEntityData().get(TEMPERATURE);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1) {
            logic.initializeAmbientTemperature(level(), getBlockPos());
        }

        if (!getWorld().isClientSide) {
            byte newStatus = IDLE;
            if (logic.getTemperature() > MIN_COOKING_TEMP) {
                newStatus = doCooking();
            } else if (logic.getTemperature() < HeatFrameCoolingRecipeImpl.getMaxThresholdTemp(getWorld())) {
                newStatus = doCooling();
            }
            setStatus(newStatus);
            if (newStatus == IDLE) {
                double delta = logic.getTemperature() - logic.getAmbientTemperature();
                if (delta > 1) {
                    logic.addHeat(-0.1);
                } else if (delta < -1) {
                    logic.addHeat(0.1);
                }
            }
            syncedTemperature.tick();
            setSyncedTemperature(syncedTemperature.getSyncedTemp());
        } else {
            // client
            if ((tickCount & 0x3) == 0) {
                byte status = getStatus();
                switch (status) {
                    case COOKING -> ClientUtils.emitParticles(level(), getBlockPos(), level().random.nextInt(4) == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE);
                    case COOLING -> ClientUtils.emitParticles(level(), getBlockPos(), ParticleTypes.SPIT);
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
            IOHelper.getInventoryForBlock(getCachedTileEntity()).ifPresent(handler -> {
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
        if (slot >= 0 & slot < handler.getSlots()) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return VanillaRecipeCache.SMELTING.getCachedRecipe(level(), new SimpleContainer(stack)).map(recipe -> {
                    ItemStack result = recipe.getResultItem(level().registryAccess()).copy();
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
            IOHelper.getInventoryForBlock(getCachedTileEntity()).ifPresent(handler -> {
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
        if (slot >= 0 & slot < handler.getSlots()) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) return false;

            return ModRecipeTypes.HEAT_FRAME_COOLING.get().findFirst(level(), r -> r.matches(stack)).map(holder -> {
                HeatFrameCoolingRecipe recipe = holder.value();
                boolean extractedOK;
                if (recipe.getInput() instanceof FluidIngredient fluidIngredient) {
                    if (stack.getCount() != 1) return false;  // fluid-containing items must not be stacked!
                    extractedOK = IOHelper.getFluidHandlerForItem(stack).map(fluidHandler -> {
                        int toDrain = fluidIngredient.getAmount();
                        if (fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE).getAmount() == toDrain) {
                            ItemStack containerStack = fluidHandler.getContainer().copy();
                            handler.extractItem(slot, 1, false);
                            handler.insertItem(slot, containerStack, false);
                            return true;
                        }
                        return false;
                    }).orElse(false);
                } else {
                    extractedOK = handler.extractItem(slot, 1, false).getCount() == 1;
                }
                if (extractedOK) {
                    ItemStack result = ItemHandlerHelper.copyStackWithSize(recipe.getOutput(), recipe.calculateOutputQuantity(logic.getTemperature()));
                    ItemHandlerHelper.insertItem(handler, result, false);
                    lastValidSlot = slot;
                }
                return extractedOK;
            }).orElse(false);
        }

        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        logic.deserializeNBT(tag.getCompound("heatExchanger"));
        cookingProgress = tag.getInt("cookingProgress");
        coolingProgress = tag.getInt("coolingProgress");
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag) {
        tag.put("heatExchanger", logic.serializeNBT());
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);

        return super.serializeNBT(tag);
    }

    @Override
    public void addTooltip(Consumer<Component> curInfo, Player player, CompoundTag tag, boolean extended) {
        int cook, cool;
        if (!level().isClientSide) {
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
        curInfo.accept(HeatUtil.formatHeatString(logic.getTemperatureAsInt()));
        if (cook != 0)
            curInfo.accept(PneumaticCraftUtils.xlate("pneumaticcraft.waila.heatFrame.cooking", cook).withStyle(ChatFormatting.GRAY));
        if (cool != 0)
            curInfo.accept(PneumaticCraftUtils.xlate("pneumaticcraft.waila.heatFrame.cooling", cool).withStyle(ChatFormatting.GRAY));
    }

}
