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

import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.thirdparty.ae2.AE2Integration;
import me.desht.pneumaticcraft.common.thirdparty.ae2.AE2RequesterIntegration;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class LogisticsRequesterEntity extends AbstractLogisticsFrameEntity implements ISpecificRequester, IProvidingInventoryListener {
    private static final EntityDataAccessor<Boolean> AE2_ENABLED = SynchedEntityData.defineId(LogisticsRequesterEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String NBT_AE2_INTEGRATION = "AE2_Integration";

    private int minItems = 1;
    private int minFluid = 1;

    private AE2RequesterIntegration ae2requester = null;

    public LogisticsRequesterEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(AE2_ENABLED, false);
        if (AE2Integration.isAvailable()) {
        }
    }

    @Override
    public int getColor() {
        return 0xFF0000FF;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.MODEL_LOGISTICS_FRAME_REQUESTER;  // TODO ridanisaurus
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    protected MenuType<?> getContainerType() {
        return ModMenuTypes.LOGISTICS_FRAME_REQUESTER.get();
    }

    @Override
    public int getMinItemOrderSize() {
        return minItems;
    }

    @Override
    public void setMinItemOrderSize(int minItems) {
        this.minItems = minItems;
    }

    @Override
    public int getMinFluidOrderSize() {
        return minFluid;
    }

    @Override
    public void setMinFluidOrderSize(int minFluid) {
        this.minFluid = minFluid;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (AE2Integration.isAvailable()) {
            setAE2enabled(tag.getBoolean(NBT_AE2_INTEGRATION));
        }
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag = super.serializeNBT(tag, provider);

        if (AE2Integration.isAvailable()) {
            tag.putBoolean(NBT_AE2_INTEGRATION, isAE2enabled());
        }
        return tag;
    }

    @Override
    public boolean canFilterStack() {
        return true;
    }

    @Override
    public boolean supportsBlacklisting() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

//        if (!level.isClientSide && AE2Integration.isAvailable()) {
//            getAE2integration().maybeCheckForInterface();
//        }
    }

    @Override
    public void notify(BlockEntityAndFace teAndFace) {
//        if (AE2Integration.isAvailable()) {
//            getAE2integration().maybeAddTE(teAndFace);
//        }
    }

    @Override
    protected void doExtraCleanupTasks(boolean removingSemiblock) {
//        if (!level.isClientSide() && AE2Integration.isAvailable()) {
//            getAE2integration().shutdown();
//        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);

//        if (tag.equals("ae2") && AE2Integration.isAvailable()) {
//            setAE2enabled(!isAE2enabled());
//            getAE2integration().setEnabled(isAE2enabled());
//        }
    }

    @Override
    public int amountRequested(ItemStack stack) {
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            return IOHelper.getInventoryForBlock(getCachedTileEntity(), getSide()).map(itemHandler -> {
                int count = 0;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack s = itemHandler.getStackInSlot(i);
                    // important that the request stack is passed first to matchOneItem()
                    // otherwise tag filters in the dest inventory will count as that item, messing up amounts
                    if (!s.isEmpty() && getItemFilterHandler().matchOneItem(stack, s)) {
                        count += s.getCount();
                    }
                }
                count += getIncomingItems(stack);
                return Math.max(0, Math.min(stack.getCount(), totalRequestingAmount - count));
            }).orElse(0);
        }
        return 0;
    }

    @Override
    public int amountRequested(FluidStack stack) {
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            return IOHelper.getFluidHandlerForBlock(getCachedTileEntity(), getSide()).map(fluidHandler -> {
                int count = 0;
                for (int i = 0; i < fluidHandler.getTanks(); i++) {
                    FluidStack contents = fluidHandler.getFluidInTank(i);
                    if (contents.getFluid() == stack.getFluid()) {
                        count += contents.getAmount();
                    }
                }
                count += getIncomingFluid(stack.getFluid());
                return Math.max(0, Math.min(stack.getAmount(), totalRequestingAmount - count));
            }).orElse(0);
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack) {
        return getItemFilterHandler().getMatchedCount(stack);
    }

    private int getTotalRequestedAmount(FluidStack stack) {
        int requesting = 0;
        for (int i = 0; i < 9; i++) {
            FluidStack requestingStack = getFluidFilter(i);
            if (requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.getAmount();
            }
        }
        return requesting;
    }

    private void setAE2enabled(boolean enabled) {
        getEntityData().set(AE2_ENABLED, enabled);
    }

    public boolean isAE2enabled() {
        return AE2Integration.isAvailable() && getEntityData().get(AE2_ENABLED);
    }

//    public AE2RequesterIntegration getAE2integration() {
//        if (ae2requester == null) {
//            ae2requester = new AE2RequesterIntegration(this);
//        }
//        return ae2requester;
//    }
}
