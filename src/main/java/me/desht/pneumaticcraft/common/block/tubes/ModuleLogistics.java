package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.ai.LogisticsManager;
import me.desht.pneumaticcraft.common.ai.LogisticsManager.LogisticsTask;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateLogisticModule;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ModuleLogistics extends TubeModule {
    private SemiBlockLogistics cachedFrame;
    private int colorChannel;
    private int ticksSinceAction = -1;//client sided timer used to display the blue color when doing a logistic task.
    private int ticksSinceNotEnoughAir = -1;
    private int ticksUntilNextCycle;
    private boolean powered;
    private static final double MIN_PRESSURE = 3;
    private static final double ITEM_TRANSPORT_COST = 5;
    private static final double FLUID_TRANSPORT_COST = 0.1;

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
    public String getModelName() {
        return "logisticsModule";
        /*TODO 1.8 if(ticksSinceAction >= 0) {
             model.base1 = model.action;
         } else if(ticksSinceNotEnoughAir >= 0) {
             model.base1 = model.notEnoughAir;
         } else {
             model.base1 = hasPower() ? model.powered : model.notPowered;
         }*/
    }

    @Override
    protected void renderModule() {
        super.renderModule();
        RenderUtils.glColorHex(0xFF000000 | ItemDye.DYE_COLORS[getColorChannel()]);
        //TODO 1.8  model.renderChannelColorFrame(1 / 16F);
        GL11.glColor4d(1, 1, 1, 1);
    }

    @Override
    protected EnumGuiId getGuiId() {
        return null;
    }

    public int getColorChannel() {
        return colorChannel;
    }

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
            ISemiBlock semiBlock = SemiBlockManager.getInstance(getTube().world()).getSemiBlock(getTube().world(), getTube().pos().offset(dir));
            if (semiBlock instanceof SemiBlockLogistics) cachedFrame = (SemiBlockLogistics) semiBlock;
        }
        return cachedFrame;
    }

    @Override
    public boolean onActivated(EntityPlayer player) {
        if (!player.getHeldItemMainhand().isEmpty()) {
            int colorIndex = TileEntityPlasticMixer.getDyeIndex(player.getHeldItemMainhand());
            if (colorIndex >= 0) {
                if (!player.world.isRemote) {
                    colorChannel = colorIndex;
                    NetworkHandler.sendToAllAround(new PacketUpdateLogisticModule(this, 0), getTube().world());
                }
                return true;
            }
        }
        return super.onActivated(player);
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
                for (TubeModule module : ModuleNetworkManager.getInstance().getConnectedModules(this)) {
                    if (module instanceof ModuleLogistics) {
                        ModuleLogistics logistics = (ModuleLogistics) module;
                        if (logistics.getColorChannel() == getColorChannel()) {
                            logistics.ticksUntilNextCycle = 100;//Make sure any connected module doesn't tick, set it to a 5 second timer. This is also a penalty value when no task is executed this tick.
                            if (logistics.hasPower() && logistics.getFrame() != null) {
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
                            ItemStack remainder = IOHelper.insert(task.requester.getTileEntity(), task.transportingItem.copy(), true);
                            if (remainder.getCount() != task.transportingItem.getCount()) {
                                ItemStack toBeExtracted = task.transportingItem.copy();
                                toBeExtracted.shrink(remainder.getCount());
                                ItemStack extractedStack = IOHelper.extract(task.provider.getTileEntity(), toBeExtracted, true);
                                if (!extractedStack.isEmpty()) {
                                    ModuleLogistics provider = frameToModuleMap.get(task.provider);
                                    ModuleLogistics requester = frameToModuleMap.get(task.requester);
                                    int airUsed = (int) (ITEM_TRANSPORT_COST * extractedStack.getCount() * Math.pow(PneumaticCraftUtils.distBetweenSq(provider.getTube().pos(), requester.getTube().pos()), 0.25));
                                    if (requester.getTube().getAirHandler(null).getAir() > airUsed) {
                                        sendModuleUpdate(provider, true);
                                        sendModuleUpdate(requester, true);
                                        requester.getTube().getAirHandler(null).addAir(-airUsed);
                                        IOHelper.extract(task.provider.getTileEntity(), extractedStack, false);
                                        IOHelper.insert(task.requester.getTileEntity(), extractedStack, false);
                                        ticksUntilNextCycle = 20;
                                    } else {
                                        sendModuleUpdate(provider, false);
                                        sendModuleUpdate(requester, false);
                                    }
                                }
                            }
                        } else {
                            TileEntity providingTE = task.provider.getTileEntity();
                            TileEntity requestingTE = task.requester.getTileEntity();
                            for (EnumFacing requesterFace : EnumFacing.VALUES) {
                                if (!requestingTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requesterFace))
                                    continue;
                                IFluidHandler requester = requestingTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requesterFace);
                                int amountFilled = requester.fill(task.transportingFluid.stack, false);
                                if (amountFilled > 0) {
                                    FluidStack drainingFluid = task.transportingFluid.stack.copy();
                                    drainingFluid.amount = amountFilled;
                                    FluidStack extractedFluid = null;
                                    ModuleLogistics p = frameToModuleMap.get(task.provider);
                                    ModuleLogistics r = frameToModuleMap.get(task.requester);
                                    int airUsed = 0;
                                    for (EnumFacing providerFace : EnumFacing.VALUES) {
                                        if (!providingTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, providerFace))
                                            continue;
                                        IFluidHandler provider = providingTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, providerFace);
                                        extractedFluid = provider.drain(drainingFluid, false);
                                        if (extractedFluid != null) {
                                            airUsed = (int) (FLUID_TRANSPORT_COST * extractedFluid.amount * PneumaticCraftUtils.distBetweenSq(p.getTube().pos(), r.getTube().pos()));
                                            if (r.getTube().getAirHandler(null).getAir() > airUsed) {
                                                extractedFluid = provider.drain(drainingFluid, true);
                                                break;
                                            } else {
                                                sendModuleUpdate(p, false);
                                                sendModuleUpdate(r, false);
                                                extractedFluid = null;
                                                break;
                                            }
                                        }
                                    }
                                    if (extractedFluid != null) {
                                        sendModuleUpdate(p, true);
                                        sendModuleUpdate(r, true);
                                        r.getTube().getAirHandler(null).addAir(-airUsed);
                                        requester.fill(extractedFluid, true);
                                        ticksUntilNextCycle = 20;
                                    }
                                    break;
                                }
                            }
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
        curInfo.add(I18n.format("hud.msg.state") + ": " + I18n.format(status));
        curInfo.add(I18n.format("waila.logisticsModule.channel") + " " + TextFormatting.YELLOW + I18n.format("item.fireworksCharge." + ItemDye.DYE_COLORS[colorChannel]));
    }
}
