package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.model.module.ModelLogistics;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.ai.LogisticsManager;
import me.desht.pneumaticcraft.common.ai.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateLogisticModule;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.DyeUtils;

import java.util.*;

public class ModuleLogistics extends TubeModule implements INetworkedModule {
    private SemiBlockLogistics cachedFrame;
    private int colorChannel;
    private int ticksSinceAction = -1; // client sided timer used to display the blue color when doing a logistic task.
    private int ticksSinceNotEnoughAir = -1;
    private int ticksUntilNextCycle;
    private boolean powered;
    private static final double MIN_PRESSURE = 3;
    private static final double ITEM_TRANSPORT_COST = 2.5;
    private static final double FLUID_TRANSPORT_COST = 0.05;

    @SideOnly(Side.CLIENT)
    public int getTicksSinceAction() {
        return ticksSinceAction;
    }

    @SideOnly(Side.CLIENT)
    public int getTicksSinceNotEnoughAir() {
        return ticksSinceNotEnoughAir;
    }

    @Override
    public double getWidth() {
        return 13 / 16D;
    }

    @Override
    protected double getHeight() {
        return 4.5D / 16D;
    }

    @Override
    public String getType() {
        return Names.MODULE_LOGISTICS;
    }

    @Override
    protected EnumGuiId getGuiId() {
        return null;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelLogistics.class;
    }

    @Override
    public int getColorChannel() {
        return colorChannel;
    }

    @Override
    public void setColorChannel(int colorChannel) {
        this.colorChannel = colorChannel;
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
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("powered", powered);
        nbt.setByte("colorChannel", (byte) colorChannel);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        powered = nbt.getBoolean("powered");
        colorChannel = nbt.getByte("colorChannel");
    }

    public SemiBlockLogistics getFrame() {
        if (cachedFrame == null) {
            cachedFrame = SemiBlockManager.getInstance(getTube().world()).getSemiBlock(SemiBlockLogistics.class, getTube().world(), getTube().pos().offset(dir));
        }
        return cachedFrame;
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand) {
        if (!player.getHeldItem(hand).isEmpty()) {
            OptionalInt colorIndex = DyeUtils.dyeDamageFromStack(player.getHeldItem(hand));
            if (colorIndex.isPresent()) {
                if (!player.world.isRemote) {
                    setColorChannel(colorIndex.getAsInt());
                    NetworkHandler.sendToAllAround(new PacketUpdateLogisticModule(this, 0), getTube().world());
                }
                return true;
            }
        }
        return super.onActivated(player, hand);
    }

    @Override
    public void update() {
        super.update();
        if (cachedFrame != null && cachedFrame.isInvalid()) cachedFrame = null;
        if (!getTube().world().isRemote) {
            if (powered != getTube().getAirHandler(null).getPressure() >= MIN_PRESSURE) {
                powered = !powered;
                NetworkHandler.sendToAllAround(new PacketUpdateLogisticModule(this, 0), getTube().world());
            }
            if (--ticksUntilNextCycle <= 0) {
                LogisticsManager manager = new LogisticsManager();
                Map<SemiBlockLogistics, ModuleLogistics> frameToModuleMap = new HashMap<>();
                Map<SemiBlockLogistics, EnumFacing> frameToSide = new HashMap<>();
                for (TubeModule module : ModuleNetworkManager.getInstance(getTube().world()).getConnectedModules(this)) {
                    if (module instanceof ModuleLogistics) {
                        ModuleLogistics logistics = (ModuleLogistics) module;
                        if (logistics.getColorChannel() == getColorChannel()) {
                            // Make sure any connected module doesn't tick, set it to a 5 second timer.
                            // This is also a penalty value when no task is executed this tick.
                            // The timer will be reduced to 20 ticks later if the module does some work.
                            logistics.ticksUntilNextCycle = 100;
                            if (logistics.hasPower() && logistics.getFrame() != null) {
                                // make frame temporarily face the logistics module
                                frameToSide.put(logistics.getFrame(), logistics.getFrame().getSide());
                                logistics.getFrame().setSide(logistics.dir.getOpposite());
                                // record the frame->module mapping and add the frame to the logistics manager
                                frameToModuleMap.put(logistics.getFrame(), logistics);
                                manager.addLogisticFrame(logistics.getFrame());
                            }
                        }
                    }
                }

                PriorityQueue<LogisticsTask> tasks = manager.getTasks(null);
                for (LogisticsTask task : tasks) {
                    if (task.isStillValid(task.transportingItem.isEmpty() ? task.transportingFluid.stack : task.transportingItem)) {
                        if (!task.transportingItem.isEmpty()) {
                            handleItems(frameToModuleMap.get(task.provider), frameToModuleMap.get(task.requester), task);
                        } else {
                            handleFluids(frameToModuleMap.get(task.provider), frameToModuleMap.get(task.requester), task);
                        }
                    }
                }

                // restore facing of frames
                frameToSide.forEach(SemiBlockLogistics::setSide);
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
        IItemHandler requestingHandler = IOHelper.getInventoryForTE(task.requester.getTileEntity(), requestingModule.dir.getOpposite());
        if (requestingHandler == null) return;

        ItemStack remainder = ItemHandlerHelper.insertItem(requestingHandler, task.transportingItem, true);
        if (remainder.getCount() != task.transportingItem.getCount()) {
            ItemStack toBeExtracted = task.transportingItem.copy();
            toBeExtracted.shrink(remainder.getCount());
            IItemHandler providingHandler = IOHelper.getInventoryForTE(task.provider.getTileEntity(), providingModule.dir.getOpposite());
            ItemStack extractedStack = IOHelper.extract(providingHandler, toBeExtracted, IOHelper.ExtractCount.UP_TO, true, false);
            if (!extractedStack.isEmpty()) {
                int airUsed = (int) (ITEM_TRANSPORT_COST * extractedStack.getCount() * PneumaticCraftUtils.distBetween(providingModule.getTube().pos(), requestingModule.getTube().pos()));
                IAirHandler receiverAirHandler = requestingModule.getTube().getAirHandler(null);
                if (airUsed > receiverAirHandler.getAir()) {
                    // not enough air to move all the items - scale back the number to be moved
                    double scale = receiverAirHandler.getAir() / (double)airUsed;
                    extractedStack.setCount((int)(extractedStack.getCount() * scale));
                    airUsed *= scale;
                }
                if (extractedStack.isEmpty()) {
                    sendModuleUpdate(providingModule, false);
                    sendModuleUpdate(requestingModule, false);
                } else {
                    sendModuleUpdate(providingModule, true);
                    sendModuleUpdate(requestingModule, true);
                    receiverAirHandler.addAir(-airUsed);
                    IOHelper.extract(providingHandler, extractedStack, IOHelper.ExtractCount.EXACT, false, false);
                    ItemHandlerHelper.insertItem(requestingHandler, extractedStack, false);
                    ticksUntilNextCycle = 20;
                }
            }
        }
    }

    private void handleFluids(ModuleLogistics providingModule, ModuleLogistics requestingModule, LogisticsTask task) {
        IFluidHandler requestingHandler = task.requester.getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requestingModule.dir.getOpposite());
        if (requestingHandler == null) return;

        int amountFilled = requestingHandler.fill(task.transportingFluid.stack, false);
        if (amountFilled > 0) {
            FluidStack drainingFluid = task.transportingFluid.stack.copy();
            drainingFluid.amount = amountFilled;
            IFluidHandler providingHandler = task.provider.getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, providingModule.dir.getOpposite());
            if (providingHandler != null) {
                FluidStack extractedFluid = providingHandler.drain(drainingFluid, false);
                if (extractedFluid != null) {
                    IAirHandler receiverAirHandler = requestingModule.getTube().getAirHandler(null);
                    double airUsed = (FLUID_TRANSPORT_COST * extractedFluid.amount * PneumaticCraftUtils.distBetween(providingModule.getTube().pos(), requestingModule.getTube().pos()));
                    if (airUsed > receiverAirHandler.getAir()) {
                        // not enough air to move it all - scale back the amount of fluid to be moved
                        double scale = receiverAirHandler.getAir() / airUsed;
                        drainingFluid.amount = (int)(extractedFluid.amount * scale);
                        airUsed *= scale;
                    }
                    if (drainingFluid.amount == 0) {
                        sendModuleUpdate(providingModule, false);
                        sendModuleUpdate(requestingModule, false);
                    } else {
                        sendModuleUpdate(providingModule, true);
                        sendModuleUpdate(requestingModule, true);
                        requestingModule.getTube().getAirHandler(null).addAir((int) -airUsed);
                        requestingHandler.fill(providingHandler.drain(drainingFluid, true), true);
                        ticksUntilNextCycle = 20;
                    }
                }
            }
        }
    }

    private void sendModuleUpdate(ModuleLogistics module, boolean enoughAir) {
        NetworkHandler.sendToAllAround(new PacketUpdateLogisticModule(module, enoughAir ? 1 : 2), module.getTube().world());
    }

    @Override
    public void addInfo(List<String> curInfo) {
        super.addInfo(curInfo);
        String status;
        if (ticksSinceAction >= 0) {
            status = "waila.logisticsModule.transporting";
        } else if (ticksSinceNotEnoughAir >= 0) {
            status = "waila.logisticsModule.notEnoughAir";
        } else if (hasPower()) {
            status = "waila.logisticsModule.powered";
        } else {
            status = "waila.logisticsModule.noPower";
        }
        curInfo.add(PneumaticCraftUtils.xlate("hud.msg.state") + ": " + PneumaticCraftUtils.xlate(status));
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }
}
