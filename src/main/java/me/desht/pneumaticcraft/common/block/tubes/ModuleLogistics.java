package me.desht.pneumaticcraft.common.block.tubes;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.ai.LogisticsManager;
import me.desht.pneumaticcraft.common.ai.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateLogisticsModule;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ModuleLogistics extends TubeModule implements INetworkedModule {
    private EntityLogisticsFrame cachedFrame;
    private int colorChannel;
    private int ticksSinceAction = -1; // client sided timer used to display the blue color when doing a logistic task.
    private int ticksSinceNotEnoughAir = -1;
    private int ticksUntilNextCycle;
    private boolean powered;

    public ModuleLogistics(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    public int getTicksSinceAction() {
        return ticksSinceAction;
    }

    public int getTicksSinceNotEnoughAir() {
        return ticksSinceNotEnoughAir;
    }

    @Override
    public double getWidth() {
        return 13;
    }

    @Override
    protected double getHeight() {
        return 4.5D;
    }

    @Override
    public int getColorChannel() {
        return colorChannel;
    }

    @Override
    public void setColorChannel(int colorChannel) {
        this.colorChannel = colorChannel;
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    public boolean hasPower() {
        return powered;
    }

    public void onUpdatePacket(int status, int colorChannel) {
        powered = status > 0;
        if (status == 2) ticksSinceAction = 0;
        if (status == 3) ticksSinceNotEnoughAir = 0;
        this.colorChannel = colorChannel;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        super.writeToNBT(nbt);
        nbt.putBoolean("powered", powered);
        nbt.putByte("colorChannel", (byte) colorChannel);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundNBT nbt) {
        super.readFromNBT(nbt);
        powered = nbt.getBoolean("powered");
        colorChannel = nbt.getByte("colorChannel");
    }

    public EntityLogisticsFrame getFrame() {
        if (cachedFrame == null) {
            ISemiBlock semiBlock = SemiblockTracker.getInstance().getSemiblock(getTube().getLevel(), getTube().getBlockPos().relative(dir), dir.getOpposite());
            if (semiBlock instanceof EntityLogisticsFrame) {
                cachedFrame = (EntityLogisticsFrame) semiBlock;
            }
        }
        return cachedFrame;
    }

    @Override
    public boolean onActivated(PlayerEntity player, Hand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        DyeColor color = DyeColor.getColor(player.getItemInHand(hand));
        if (color != null) {
            int colorId = color.getId();
            if (!player.level.isClientSide) {
                setColorChannel(colorId);
                NetworkHandler.sendToAllTracking(new PacketUpdateLogisticsModule(this, 0), getTube());
                if (PNCConfig.Common.General.useUpDyesWhenColoring && !player.isCreative()) {
                    heldStack.shrink(1);
                }
            }
            return true;
        }
        return super.onActivated(player, hand);
    }

    @Override
    public void update() {
        super.update();
        if (cachedFrame != null && !cachedFrame.isValid()) cachedFrame = null;
        if (!getTube().getLevel().isClientSide) {
            if (powered != getTube().getPressure() >= PNCConfig.Common.Logistics.minPressure) {
                powered = !powered;
                NetworkHandler.sendToAllTracking(new PacketUpdateLogisticsModule(this, 0), getTube());
            }
            if (--ticksUntilNextCycle <= 0) {
                LogisticsManager manager = new LogisticsManager();
                Map<Integer, ModuleLogistics> frame2module = new Int2ObjectOpenHashMap<>();
                for (TubeModule module : ModuleNetworkManager.getInstance(getTube().getLevel()).getConnectedModules(this)) {
                    if (module instanceof ModuleLogistics) {
                        ModuleLogistics logistics = (ModuleLogistics) module;
                        if (logistics.getColorChannel() == getColorChannel()) {
                            // Make sure any connected module doesn't tick; set it to a 5 second timer.
                            // This is also a penalty value when no task is executed this tick.
                            // The timer will be reduced to 20 ticks later if the module does some work.
                            logistics.ticksUntilNextCycle = 100;
                            if (logistics.hasPower() && logistics.getFrame() != null) {
                                // record the frame->module mapping and add the frame to the logistics manager
                                frame2module.put(logistics.getFrame().getId(), logistics);
                                manager.addLogisticFrame(logistics.getFrame());
                            }
                        }
                    }
                }

                PriorityQueue<LogisticsTask> tasks = manager.getTasks(null, false);
                for (LogisticsTask task : tasks) {
                    if (task.isStillValid(task.transportingItem.isEmpty() ? task.transportingFluid : task.transportingItem)) {
                        if (!task.transportingItem.isEmpty()) {
                            handleItems(frame2module.get(task.provider.getId()), frame2module.get(task.requester.getId()), task);
                        } else {
                            handleFluids(frame2module.get(task.provider.getId()), frame2module.get(task.requester.getId()), task);
                        }
                    }
                }
            }
        } else {
            if (ticksSinceAction >= 0) {
                ticksSinceAction++;
                if (ticksSinceAction > 3) ticksSinceAction = -1;
            }
            if (ticksSinceNotEnoughAir >= 0) {
                ticksSinceNotEnoughAir++;
                if (ticksSinceNotEnoughAir > 20) ticksSinceNotEnoughAir = -1;
            }
        }
    }

    private void handleItems(ModuleLogistics providingModule, ModuleLogistics requestingModule, LogisticsTask task) {
        IOHelper.getInventoryForTE(task.requester.getCachedTileEntity(), requestingModule.dir.getOpposite()).ifPresent(requestingHandler -> {
            ItemStack remainder = ItemHandlerHelper.insertItem(requestingHandler, task.transportingItem, true);
            if (remainder.getCount() != task.transportingItem.getCount()) {
                ItemStack toBeExtracted = task.transportingItem.copy();
                toBeExtracted.shrink(remainder.getCount());
                IOHelper.getInventoryForTE(task.provider.getCachedTileEntity(), providingModule.dir.getOpposite())
                        .ifPresent(providingHandler -> tryItemTransfer(providingModule, requestingModule, providingHandler, requestingHandler, toBeExtracted));
            }
        });
    }

    private void tryItemTransfer(ModuleLogistics providingModule, ModuleLogistics requestingModule, IItemHandler providingHandler, IItemHandler requestingHandler, ItemStack toTransfer) {
        ItemStack extractedStack = IOHelper.extract(providingHandler, toTransfer, IOHelper.ExtractCount.UP_TO, true, requestingModule.getFrame().isMatchNBT());
        if (extractedStack.isEmpty()) return;

        IAirHandlerMachine receiverAirHandler = requestingModule.getTube().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                .orElseThrow(RuntimeException::new);

        int airUsed = (int) (PNCConfig.Common.Logistics.itemTransportCost * extractedStack.getCount() * PneumaticCraftUtils.distBetween(providingModule.getTube().getBlockPos(), requestingModule.getTube().getBlockPos()));

        if (airUsed > receiverAirHandler.getAir()) {
            // not enough air to move all the items - scale back the number to be moved
            double scaleBack = receiverAirHandler.getAir() / (double) airUsed;
            extractedStack.setCount((int) (extractedStack.getCount() * scaleBack));
            airUsed *= scaleBack;
        }

        if (extractedStack.isEmpty()) {
            sendModuleUpdate(providingModule, false);
            sendModuleUpdate(requestingModule, false);
        } else {
            sendModuleUpdate(providingModule, true);
            sendModuleUpdate(requestingModule, true);
            receiverAirHandler.addAir(-airUsed);
            IOHelper.extract(providingHandler, extractedStack, IOHelper.ExtractCount.EXACT, false, requestingModule.getFrame().isMatchNBT());
            ItemHandlerHelper.insertItem(requestingHandler, extractedStack, false);
            ticksUntilNextCycle = 20;
        }
    }

    private void handleFluids(ModuleLogistics providingModule, ModuleLogistics requestingModule, LogisticsTask task) {
        task.requester.getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requestingModule.dir.getOpposite()).ifPresent(requestingHandler -> {
            int amountFilled = requestingHandler.fill(task.transportingFluid, IFluidHandler.FluidAction.SIMULATE);
            if (amountFilled > 0) {
                FluidStack drainingFluid = task.transportingFluid.copy();
                drainingFluid.setAmount(amountFilled);
                task.provider.getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, providingModule.dir.getOpposite())
                        .ifPresent(providingHandler -> tryFluidTransfer(providingModule, providingHandler, requestingModule, requestingHandler, drainingFluid));
            }
        });
    }

    private void tryFluidTransfer(ModuleLogistics providingModule, IFluidHandler providingHandler, ModuleLogistics requestingModule, IFluidHandler requestingHandler, FluidStack toTransfer) {
        FluidStack extractedFluid = providingHandler.drain(toTransfer, IFluidHandler.FluidAction.SIMULATE);

        if (extractedFluid.isEmpty()) return;

        IAirHandlerMachine receiverAirHandler = requestingModule.getTube().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                .orElseThrow(RuntimeException::new);

        double airUsed = (PNCConfig.Common.Logistics.fluidTransportCost * extractedFluid.getAmount() * PneumaticCraftUtils.distBetween(providingModule.getTube().getBlockPos(), requestingModule.getTube().getBlockPos()));
        if (airUsed > receiverAirHandler.getAir()) {
            // not enough air to move it all - scale back the amount of fluid to be moved
            double scaleBack = receiverAirHandler.getAir() / airUsed;
            toTransfer.setAmount((int) (extractedFluid.getAmount() * scaleBack));
            airUsed *= scaleBack;
        }
        if (toTransfer.isEmpty()) {
            sendModuleUpdate(providingModule, false);
            sendModuleUpdate(requestingModule, false);
        } else {
            sendModuleUpdate(providingModule, true);
            sendModuleUpdate(requestingModule, true);
            receiverAirHandler.addAir((int) -airUsed);
            requestingHandler.fill(providingHandler.drain(toTransfer, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            ticksUntilNextCycle = 20;
        }
    }

    private void sendModuleUpdate(ModuleLogistics module, boolean enoughAir) {
        NetworkHandler.sendToAllTracking(new PacketUpdateLogisticsModule(module, enoughAir ? 1 : 2), module.getTube());
    }

    @Override
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);
        String status;
        if (ticksSinceAction >= 0) {
            status = "pneumaticcraft.waila.logisticsModule.transporting";
        } else if (ticksSinceNotEnoughAir >= 0) {
            status = "pneumaticcraft.waila.logisticsModule.notEnoughAir";
        } else if (hasPower()) {
            status = "pneumaticcraft.waila.logisticsModule.powered";
        } else {
            status = "pneumaticcraft.waila.logisticsModule.noPower";
        }
        curInfo.add(PneumaticCraftUtils.xlate("pneumaticcraft.hud.msg.state").append(": ").append(PneumaticCraftUtils.xlate(status)));
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }
}
