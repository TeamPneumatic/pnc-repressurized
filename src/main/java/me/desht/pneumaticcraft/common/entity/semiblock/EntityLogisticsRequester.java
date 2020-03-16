package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class EntityLogisticsRequester extends EntityLogisticsFrame implements ISpecificRequester/*, IProvidingInventoryListener*/ {
    private int minItems = 1;
    private int minFluid = 1;

    public static EntityLogisticsRequester create(EntityType<EntityLogisticsRequester> type, World world) {
        return new EntityLogisticsRequester(type, world);
    }

    private EntityLogisticsRequester(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int getColor() {
        return 0xFF0000FF;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    protected ContainerType<?> getContainerType() {
        return ModContainers.LOGISTICS_FRAME_REQUESTER.get();
    }

    public int getMinItemOrderSize() {
        return minItems;
    }

    public void setMinItemOrderSize(int minItems) {
        this.minItems = minItems;
    }

    public int getMinFluidOrderSize() {
        return minFluid;
    }

    public void setMinFluidOrderSize(int minFluid) {
        this.minFluid = minFluid;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        setMinItemOrderSize(tag.getInt(NBT_MIN_ITEMS));
        setMinFluidOrderSize(tag.getInt(NBT_MIN_FLUID));
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        tag = super.serializeNBT(tag);

        tag.putInt(NBT_MIN_ITEMS, getMinItemOrderSize());
        tag.putInt(NBT_MIN_FLUID, getMinFluidOrderSize());

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
    public void writeToBuf(PacketBuffer payload) {
        super.writeToBuf(payload);

        payload.writeVarInt(minItems);
        payload.writeVarInt(minFluid);
    }

    @Override
    public void readFromBuf(PacketBuffer payload) {
        super.readFromBuf(payload);

        minItems = payload.readVarInt();
        minFluid = payload.readVarInt();
    }

//    @Override
//    public void notify(TileEntityAndFace teAndFace) {
//
//    }

    @Override
    public int amountRequested(ItemStack stack) {
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            return IOHelper.getInventoryForTE(getCachedTileEntity(), getFacing()).map(itemHandler -> {
                int count = 0;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack s = itemHandler.getStackInSlot(i);
                    if (!s.isEmpty() && getItemFilterHandler().matchOneItem(s, stack)) {
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
            return getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getFacing()).map(fluidHandler -> {
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
}
