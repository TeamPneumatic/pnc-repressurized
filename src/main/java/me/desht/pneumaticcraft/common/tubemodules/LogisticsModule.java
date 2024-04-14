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

package me.desht.pneumaticcraft.common.tubemodules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.drone.LogisticsManager;
import me.desht.pneumaticcraft.common.drone.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateLogisticsModule;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.PriorityQueue;

public class LogisticsModule extends AbstractTubeModule implements INetworkedModule {
    private AbstractLogisticsFrameEntity cachedFrame;
    private int colorChannel;
    private int ticksSinceAction = -1; // client sided timer used to display the blue color when doing a logistic task.
    private int ticksSinceNotEnoughAir = -1;
    private int ticksUntilNextCycle;
    private boolean powered;

    public LogisticsModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public Item getItem() {
        return ModItems.LOGISTICS_MODULE.get();
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
        setChanged();
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
    public CompoundTag writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        nbt.putBoolean("powered", powered);
        nbt.putByte("colorChannel", (byte) colorChannel);
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        powered = nbt.getBoolean("powered");
        colorChannel = nbt.getByte("colorChannel");
    }

    public AbstractLogisticsFrameEntity getFrame() {
        if (cachedFrame == null) {
            ISemiBlock semiBlock = SemiblockTracker.getInstance().getSemiblock(getTube().getLevel(), getTube().getBlockPos().relative(dir), dir.getOpposite());
            if (semiBlock instanceof AbstractLogisticsFrameEntity) {
                cachedFrame = (AbstractLogisticsFrameEntity) semiBlock;
            }
        }
        return cachedFrame;
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        DyeColor color = DyeColor.getColor(player.getItemInHand(hand));
        if (color != null) {
            int colorId = color.getId();
            if (!player.level().isClientSide) {
                setColorChannel(colorId);
                NetworkHandler.sendToAllTracking(PacketUpdateLogisticsModule.create(this, 0), getTube());
                if (ConfigHelper.common().general.useUpDyesWhenColoring.get() && !player.isCreative()) {
                    heldStack.shrink(1);
                }
            }
            return true;
        }
        return super.onActivated(player, hand);
    }

    @Override
    protected void tickCommon() {
        super.tickCommon();

        if (cachedFrame != null && !cachedFrame.isValid()) cachedFrame = null;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (powered != getTube().getPressure() >= ConfigHelper.common().logistics.minPressure.get()) {
            powered = !powered;
            NetworkHandler.sendToAllTracking(PacketUpdateLogisticsModule.create(this, 0), getTube());
        }
        if (--ticksUntilNextCycle <= 0) {
            LogisticsManager manager = new LogisticsManager();
            Int2ObjectMap<LogisticsModule> frame2module = new Int2ObjectOpenHashMap<>();
            ModuleNetworkManager networkManager = ModuleNetworkManager.getInstance(getTube().nonNullLevel());
            for (AbstractTubeModule module : networkManager.getConnectedModules(this)) {
                if (module.isValid() && module instanceof LogisticsModule logistics && logistics.getColorChannel() == this.getColorChannel()) {
                    // Make sure any connected module doesn't tick; set it to a 5-second timer.
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
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if (ticksSinceAction >= 0) {
            ticksSinceAction++;
            if (ticksSinceAction > 3) ticksSinceAction = -1;
        }
        if (ticksSinceNotEnoughAir >= 0) {
            ticksSinceNotEnoughAir++;
            if (ticksSinceNotEnoughAir > 20) ticksSinceNotEnoughAir = -1;
        }
    }

    private void handleItems(LogisticsModule providingModule, LogisticsModule requestingModule, LogisticsTask task) {
        IOHelper.getInventoryForBlock(task.requester.getCachedTileEntity(), requestingModule.dir.getOpposite()).ifPresent(requestingHandler -> {
            ItemStack remainder = ItemHandlerHelper.insertItem(requestingHandler, task.transportingItem, true);
            if (remainder.getCount() != task.transportingItem.getCount()) {
                ItemStack toBeExtracted = task.transportingItem.copy();
                toBeExtracted.shrink(remainder.getCount());
                IOHelper.getInventoryForBlock(task.provider.getCachedTileEntity(), providingModule.dir.getOpposite())
                        .ifPresent(providingHandler -> tryItemTransfer(providingModule, requestingModule, providingHandler, requestingHandler, toBeExtracted));
            }
        });
    }

    private void tryItemTransfer(LogisticsModule providingModule, LogisticsModule requestingModule, IItemHandler providingHandler, IItemHandler requestingHandler, ItemStack toTransfer) {
        ItemStack extractedStack = IOHelper.extract(providingHandler, toTransfer, IOHelper.ExtractCount.UP_TO, true, requestingModule.getFrame().isMatchNBT());
        if (extractedStack.isEmpty()) return;

        PNCCapabilities.getAirHandler(requestingModule.getTube()).ifPresent(receiverAirHandler -> {
            int airUsed = (int) (ConfigHelper.common().logistics.itemTransportCost.get() * extractedStack.getCount() * PneumaticCraftUtils.distBetween(providingModule.getTube().getBlockPos(), requestingModule.getTube().getBlockPos()));

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
        });
    }

    private void handleFluids(LogisticsModule providingModule, LogisticsModule requestingModule, LogisticsTask task) {
        IOHelper.getFluidHandlerForBlock(task.requester.getCachedTileEntity(), requestingModule.dir.getOpposite()).ifPresent(requestingHandler -> {
            int amountFilled = requestingHandler.fill(task.transportingFluid, IFluidHandler.FluidAction.SIMULATE);
            if (amountFilled > 0) {
                FluidStack drainingFluid = task.transportingFluid.copy();
                drainingFluid.setAmount(amountFilled);
                IOHelper.getFluidHandlerForBlock(task.provider.getCachedTileEntity(), providingModule.dir.getOpposite())
                        .ifPresent(providingHandler -> tryFluidTransfer(providingModule, providingHandler, requestingModule, requestingHandler, drainingFluid));
            }
        });
    }

    private void tryFluidTransfer(LogisticsModule providingModule, IFluidHandler providingHandler, LogisticsModule requestingModule, IFluidHandler requestingHandler, FluidStack toTransfer) {
        FluidStack extractedFluid = providingHandler.drain(toTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (extractedFluid.isEmpty()) return;

        PNCCapabilities.getAirHandler(requestingModule.getTube()).ifPresent(receiverAirHandler -> {
            double airUsed = (ConfigHelper.common().logistics.fluidTransportCost.get() * extractedFluid.getAmount() * PneumaticCraftUtils.distBetween(providingModule.getTube().getBlockPos(), requestingModule.getTube().getBlockPos()));
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
        });
    }

    private void sendModuleUpdate(LogisticsModule module, boolean enoughAir) {
        NetworkHandler.sendToAllTracking(PacketUpdateLogisticsModule.create(module, enoughAir ? 1 : 2), module.getTube());
    }

    @Override
    public void addInfo(List<Component> curInfo) {
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
