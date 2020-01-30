package me.desht.pneumaticcraft.common.entity.semiblock;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EntityHeatFrame extends EntitySemiblockBase {
    private static final DataParameter<Integer> HEAT_LEVEL = EntityDataManager.createKey(EntityHeatFrame.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(EntityHeatFrame.class, DataSerializers.BYTE);

    private static final byte IDLE = 0;
    private static final byte COOKING = 1;
    private static final byte COOLING = 2;

    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatRegistry().makeHeatExchangerLogic();
    private final LazyOptional<IHeatExchangerLogic> heatCap;

    private int lastValidSlot; // cache the current cooking slot for performance boost
    private int cookingProgress;
    private int coolingProgress;

    public static EntityHeatFrame create(EntityType<EntityHeatFrame> type, World world) {
        return new EntityHeatFrame(type, world);
    }

    private EntityHeatFrame(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

         heatCap = LazyOptional.of(() -> logic);
    }

    public int getHeatLevel() {
        return getDataManager().get(HEAT_LEVEL);
    }

    private void setHeatLevel(int level) {
        getDataManager().set(HEAT_LEVEL, level);
    }

    public IHeatExchangerLogic getHeatExchangerLogic() {
        return logic;
    }

    @Override
    protected void registerData() {
        super.registerData();

        this.dataManager.register(HEAT_LEVEL, 10);
        this.dataManager.register(STATUS, IDLE);
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

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            byte newStatus = IDLE;
            setHeatLevel(HeatUtil.getHeatLevelForTemperature(logic.getTemperature()));
            if (logic.getTemperature() > 374) {
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
            } else if (logic.getTemperature() < 273) {
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
            }
            setStatus(newStatus);
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

    private boolean tryCookSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            Inventory inv = new Inventory(1);
            inv.setInventorySlotContents(0, stack);
            return world.getRecipeManager().getRecipe(IRecipeType.SMELTING, inv, this.world).map(recipe -> {
                ItemStack result = recipe.getRecipeOutput();
                if (!result.isEmpty()) {
                    ItemStack remainder = IOHelper.insert(getCachedTileEntity(), result, true);
                    if (remainder.isEmpty()) {
                        IOHelper.insert(getCachedTileEntity(), result, false);
                        handler.extractItem(slot, 1, false);
                        lastValidSlot = slot;
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private boolean tryCoolSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (stack.isEmpty()) return false;

        IHeatFrameCoolingRecipe recipe = PneumaticCraftRecipes.heatFrameCoolingRecipes.values().stream()
                .filter(r -> r.matches(stack))
                .findFirst()
                .orElse(null);

        if (recipe != null) {
            ItemStack output = recipe.getOutput();
            if (stack.getCount() >= recipe.getInputAmount()) {
                ItemStack containerItem = stack.getItem().getContainerItem(stack);
                Pair<Integer,Integer> slots = findOutputSpace(handler, output, containerItem);
                if (slots.getLeft() >= 0 && (slots.getRight() >= 0 || containerItem.isEmpty())) {
                    handler.extractItem(slot, recipe.getInputAmount(), false);
                    handler.insertItem(slots.getLeft(), output, false);
                    if (!containerItem.isEmpty()) {
                        handler.insertItem(slots.getRight(), containerItem, false);
                    }
                    lastValidSlot = slot;
                    return true;
                }
            }
        }
        return false;
    }

    private Pair<Integer,Integer> findOutputSpace(IItemHandler handler, ItemStack output, ItemStack containerItem) {
        int outSlot = -1;
        int containerSlot = -1;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack s = handler.getStackInSlot(i);
            if (s.isEmpty()) {
                if (outSlot >= 0) {
                    containerSlot = i;
                } else {
                    outSlot = i;
                }
            } else {
                if (ItemHandlerHelper.canItemStacksStack(s, output) && s.getCount() + output.getCount() < s.getMaxStackSize()) {
                    outSlot = i;
                }
                if (!containerItem.isEmpty()
                        && ItemHandlerHelper.canItemStacksStack(s, containerItem)
                        && s.getCount() + containerItem.getCount() < s.getMaxStackSize()) {
                    containerSlot = i;
                }
            }
        }

        return Pair.of(outSlot, containerSlot);
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        logic.deserializeNBT(tag.getCompound("heatExchanger"));
        cookingProgress = tag.getInt("cookingProgress");
        coolingProgress = tag.getInt("coolingProgress");
    }

    @Override
    protected void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);

        tag.put("heatExchanger", logic.serializeNBT());
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT tag) {
        tag.putInt("temperature", getHeatExchangerLogic().getTemperatureAsInt());
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);

        return super.serializeNBT(tag);
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, PlayerEntity player, CompoundNBT tag, boolean extended) {
        int cook, cool, temp;
        if (!world.isRemote) {
            // TOP
            temp = getHeatExchangerLogic().getTemperatureAsInt();
            cook = cookingProgress;
            cool = coolingProgress;
        } else {
            // Waila
            temp = tag.getInt("temperature");
            cook = tag.getInt("cookingProgress");
            cool = tag.getInt("coolingProgress");
        }

        if (getStatus() != COOKING && cook >= 100) cook = 0;
        if (getStatus() != COOLING && cool >= 100) cool = 0;
        curInfo.add(HeatUtil.formatHeatString(temp));
        curInfo.add(PneumaticCraftUtils.xlate("waila.heatFrame.cooking", cook).applyTextStyle(TextFormatting.GRAY));
        curInfo.add(PneumaticCraftUtils.xlate("waila.heatFrame.cooling", cool).applyTextStyle(TextFormatting.GRAY));
    }
}
