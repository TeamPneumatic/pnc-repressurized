package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.ai.DroneAIManager.EntityAITaskEntry;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketShowArea;
import me.desht.pneumaticcraft.common.network.PacketSpawnRing;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Optional.InterfaceList({
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT),
        @Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = ModIds.OPEN_COMPUTERS),
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = ModIds.OPEN_COMPUTERS)
})
public class TileEntityDroneInterface extends TileEntity
        implements ITickable, IPeripheral, ManagedPeripheral, SimpleComponent {

    private static LuaMethodRegistry luaMethodRegistry = null;

    private final CopyOnWriteArrayList<IComputerAccess> attachedComputers = new CopyOnWriteArrayList<>();
    private EntityDrone drone;
    public float rotationYaw;
    public float rotationPitch = (float) Math.toRadians(-42);
    private final ConcurrentLinkedQueue<Integer> ringSendQueue = new ConcurrentLinkedQueue<>();
    private int ringSendCooldown;
    private IProgWidget curAction;

    @Override
    public void update() {
        if (drone != null && drone.isDead) {
            setDrone(null);
        }
        if (drone != null) {
            if (getWorld().isRemote) {
                double dx = drone.posX - (getPos().getX() + 0.5);
                double dy = drone.posY - (getPos().getY() + 0.5);
                double dz = drone.posZ - (getPos().getZ() + 0.5);
                float f3 = MathHelper.sqrt(dx * dx + dz * dz);
                rotationYaw = (float) -Math.atan2(dx, dz);
                rotationPitch = (float) -Math.atan2(dy, f3);
            } else {
                if (ringSendCooldown > 0) ringSendCooldown--;
                if (!ringSendQueue.isEmpty() && ringSendCooldown <= 0) {
                    ringSendCooldown = ringSendQueue.size() > 10 ? 1 : 5;
                    NetworkHandler.sendToDimension(new PacketSpawnRing(getPos().getX() + 0.5, getPos().getY() + 0.8, getPos().getZ() + 0.5, drone, ringSendQueue.poll()), getWorld().provider.getDimension());
                }
            }
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("drone", drone != null ? drone.getEntityId() : -1);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        Entity entity = getWorld().getEntityByID(tag.getInteger("drone"));
        drone = entity instanceof EntityDrone ? (EntityDrone) entity : null;
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    public TileEntityDroneInterface() {
        if (luaMethodRegistry == null) {
            setupLuaMethods();
        }
    }

    boolean isDroneConnected() {
        return drone != null;
    }

    private EntityDrone validateAndGetDrone() {
        if (drone == null) throw new IllegalStateException("There's no connected Drone!");
        return drone;
    }

    private void setupLuaMethods() {
        luaMethodRegistry = new LuaMethodRegistry();

        registerLuaMethod(new LuaMethod("isConnectedToDrone") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{drone != null};
            }
        });

        registerLuaMethod(new LuaMethod("getDronePressure") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{(double) validateAndGetDrone().getPressure(null)};
            }
        });

        registerLuaMethod(new LuaMethod("exitPiece") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                //noinspection ResultOfMethodCallIgnored
                validateAndGetDrone();
                setDrone(null); // disconnect
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("getAllActions") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                List<String> actions = new ArrayList<>();
                for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                    if (widget.canBeRunByComputers(new EntityDrone(getWorld()), getWidget())) {
                        actions.add(widget.getWidgetString());
                    }
                }
                return new Object[]{getStringTable(actions)};
            }
        });

        registerLuaMethod(new LuaMethod("getDronePosition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                EntityDrone d = validateAndGetDrone();
                return new Double[]{d.posX, d.posY, d.posZ};
            }
        });

        registerLuaMethod(new LuaMethod("setBlockOrder") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "'closest'/'highToLow'/'lowToHigh'");
                String arg = (String) args[0];
                for (EnumOrder order : EnumOrder.values()) {
                    if (order.toString().equalsIgnoreCase(arg)) {
                        getWidget().setOrder(order);
                        return null;
                    }
                }
                throw new IllegalArgumentException("No valid order. Valid arguments:  'closest', 'highToLow' or 'lowToHigh'!");
            }
        });

        registerLuaMethod(new LuaMethod("getAreaTypes") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return getWidget().getAreaTypes();
            }
        });

        registerLuaMethod(new LuaMethod("addArea") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, new int[]{ 3, 7}, "(x,y,z) or (x1,y1,z1,x2,y2,z2,areaType)");
                if (args.length == 3) {
                    getWidget().addArea(((Double) args[0]).intValue(), ((Double) args[1]).intValue(), ((Double) args[2]).intValue());
                } else {  // 7
                    getWidget().addArea(((Double) args[0]).intValue(), ((Double) args[1]).intValue(), ((Double) args[2]).intValue(),
                            ((Double) args[3]).intValue(), ((Double) args[4]).intValue(), ((Double) args[5]).intValue(),
                            (String) args[6]);
                }
                messageToDrone(ProgWidgetArea.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("removeArea") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, new int[]{ 3, 7}, "(x,y,z) or (x1,y1,z1,x2,y2,z2,areaType)");
                if (args.length == 3) {
                    getWidget().removeArea(((Double) args[0]).intValue(), ((Double) args[1]).intValue(), ((Double) args[2]).intValue());
                } else if (args.length == 7) {
                    getWidget().removeArea(((Double) args[0]).intValue(), ((Double) args[1]).intValue(), ((Double) args[2]).intValue(),
                            ((Double) args[3]).intValue(), ((Double) args[4]).intValue(), ((Double) args[5]).intValue(),
                            (String) args[6]);

                }
                messageToDrone(ProgWidgetArea.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearArea();
                messageToDrone(ProgWidgetArea.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("showArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                Set<BlockPos> area = new HashSet<>();
                getWidget().getArea(area);
                NetworkHandler.sendToDimension(new PacketShowArea(getPos(), area), getWorld().provider.getDimension());
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("hideArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                NetworkHandler.sendToDimension(new PacketShowArea(getPos()), getWorld().provider.getDimension());
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 6, "<string> item/block name, <int> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity");
                getWidget().addWhitelistItemFilter((String) args[0], ((Double) args[1]).intValue(), (Boolean) args[2], (Boolean) args[3], (Boolean) args[4], (Boolean) args[5]);
                messageToDrone(ProgWidgetItemFilter.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 6, "<string> item/block name, <int> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity");
                getWidget().addBlacklistItemFilter((String) args[0], ((Double) args[1]).intValue(), (Boolean) args[2], (Boolean) args[3], (Boolean) args[4], (Boolean) args[5]);
                messageToDrone(ProgWidgetItemFilter.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemWhitelist();
                messageToDrone(ProgWidgetItemFilter.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemBlacklist();
                messageToDrone(ProgWidgetItemFilter.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addWhitelistText((String) args[0]);
                messageToDrone(ProgWidgetString.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addBlacklistText((String) args[0]);
                messageToDrone(ProgWidgetString.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearWhitelistText();
                messageToDrone(ProgWidgetString.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearBlacklistText();
                messageToDrone(ProgWidgetString.class);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setSide") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 2, "down/up/north/south/west/east, <boolean> valid");
                EnumFacing dir = getDirForString((String) args[0]);
                boolean[] sides = getWidget().getSides();
                sides[dir.ordinal()] = (Boolean) args[1]; // We don't need to set them afterwards, got a reference.
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setSides") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 6, "6 x boolean (order: DUNSWE)");
                boolean[] sides = new boolean[6];
                for (int i = 0; i < 6; i++) {
                    sides[i] = (Boolean) args[i];
                }
                getWidget().setSides(sides);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setEmittingRedstone") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> signal_strength");
                getWidget().setEmittingRedstone(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setRenameString") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> new_name");
                getWidget().setNewName((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addWhitelistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> fluid_name");
                getWidget().addWhitelistLiquidFilter((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("addBlacklistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> fluid_name");
                getWidget().addBlacklistLiquidFilter((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearWhitelistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearLiquidWhitelist();
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("clearBlacklistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearLiquidBlacklist();
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setDropStraight") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> drop_straight");
                getWidget().setDropStraight((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setUseCount") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_use_count");
                getWidget().setUseCount((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setCount") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> use_count");
                getWidget().setCount(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setIsAndFunction") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> is_and_function");
                getWidget().setAndFunction((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setOperator") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> '>=', '=' or = '>='");
                getWidget().setOperator((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("evaluateCondition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                if (curAction instanceof ICondition) {
                    boolean bool = ((ICondition) curAction).evaluate(drone, getWidget());
                    return new Object[]{bool};
                } else {
                    throw new IllegalArgumentException("Current action is not a condition! Action: " + (curAction != null ? curAction.getWidgetString() : "*none*"));
                }
            }
        });

        registerLuaMethod(new LuaMethod("setUseMaxActions") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_use_max_actions");
                getWidget().setUseMaxActions((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setMaxActions") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> max_actions");
                getWidget().setMaxActions(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setSneaking") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> is_sneaking");
                getWidget().setSneaking((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setPlaceFluidBlocks") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_place_fluid_blocks");
                getWidget().setPlaceFluidBlocks((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setAction") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> action_name");
                String widgetName = (String) args[0];
                for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                    if (widget.getWidgetString().equalsIgnoreCase(widgetName)) {
                        EntityAIBase ai = widget.getWidgetAI(drone, getWidget());
                        if (ai == null || !widget.canBeRunByComputers(drone, getWidget())) {
                            throw new IllegalArgumentException("Parsed action '" + widgetName + "' is not a runnable action!");
                        }
                        getAI().setAction(widget, ai);
                        getTargetAI().setAction(widget, widget.getWidgetTargetAI(drone, getWidget()));
                        messageToDrone(ItemDye.DYE_COLORS[widget.getCraftingColorIndex()]);
                        curAction = widget;
                        return null;
                    }
                }
                throw new IllegalArgumentException("No action with the name '" + widgetName + "'!");
            }
        });

        registerLuaMethod(new LuaMethod("getAction") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return curAction != null ? new Object[]{curAction.getWidgetString()} : null;
            }
        });

        registerLuaMethod(new LuaMethod("abortAction") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getAI().abortAction();
                getTargetAI().abortAction();
                messageToDrone(0xFFFFFFFF);
                curAction = null;
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("isActionDone") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{getAI().isActionDone()};
            }
        });

        registerLuaMethod(new LuaMethod("forgetTarget") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                validateAndGetDrone().setAttackTarget(null);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("getUpgrades") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> upgrade_index");
                return new Object[]{(double) validateAndGetDrone().getUpgrades(EnumUpgrade.values()[((Double) args[0]).intValue()])};
            }
        });

        registerLuaMethod(new LuaMethod("setCraftingGrid") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 9, "9 x item_name");
                String[] grid = new String[9];
                for (int i = 0; i < 9; i++) {
                    grid[i] = (String) args[i];
                }
                getWidget().setCraftingGrid(grid);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setVariable") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, new int[]{2,4}, "<string> var_name, true/false OR <string> var_name, <int> x, <int> y, <int> z");
                EntityDrone d = validateAndGetDrone();
                String varName = (String) args[0];
                int x = args[1] instanceof Double ? ((Double) args[1]).intValue() : (Boolean) args[1] ? 1 : 0;
                int y = 0;
                int z = 0;
                if (args.length == 4) {
                    y = ((Double) args[2]).intValue();
                    z = ((Double) args[3]).intValue();
                }
                d.setVariable(varName, new BlockPos(x, y, z));
                messageToDrone(0xFFFFFFFF);
                return null;
            }

        });

        registerLuaMethod(new LuaMethod("getVariable") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> var_name");
                BlockPos var = validateAndGetDrone().getVariable((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return new Object[]{var.getX(), var.getY(), var.getZ()};
            }
        });

        registerLuaMethod(new LuaMethod("setSignText") {
            @Override
            public Object[] call(Object[] args) {
                getWidget().signText = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    getWidget().signText[i] = (String) args[i];
                }
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("setRequiresTool") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> require_tool");
                getWidget().setRequiresTool((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registerLuaMethod(new LuaMethod("getDroneName") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getName()};
            }
        });

        registerLuaMethod(new LuaMethod("getOwnerName") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getPlayerName()};
            }
        });

        registerLuaMethod(new LuaMethod("getOwnerID") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getOwnerUUID()};
            }
        });
    }

    private void registerLuaMethod(ILuaMethod method) {
        luaMethodRegistry.registerLuaMethod(method);
    }
    
    @Override
    public String getType() {
        return "droneInterface";
    }

    @Override
    public String getComponentName() {
        return getType();
    }

    @Override
    public String[] getMethodNames() {
        return luaMethodRegistry.getMethodNames();
    }

    @Override
    public String[] methods() {
        return getMethodNames();
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS)
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        if ("greet".equals(method)) {
            return new Object[]{String.format("Hello, %s!", args.checkString(0))};
        }
        return luaMethodRegistry.getMethod(method).call(args.toArray());
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException {
        try {
            return luaMethodRegistry.getMethod(method).call(arguments);
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void attach(IComputerAccess computer) {
        attachedComputers.add(computer);
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void detach(IComputerAccess computer) {
        attachedComputers.remove(computer);
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public boolean equals(IPeripheral other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (other instanceof TileEntity) {
            TileEntity tother = (TileEntity) other;
            return tother.getWorld().equals(getWorld()) && tother.getPos().equals(getPos());
        }

        return false;
    }

    private void sendEvent(String name, Object... parms) {
        if (Loader.isModLoaded(ModIds.COMPUTERCRAFT)) {
            for (IComputerAccess computer : attachedComputers) {
                computer.queueEvent(name, parms);
            }
        }
    }

    public void setDrone(EntityDrone drone) {
        this.drone = drone;
        sendEvent(drone != null ? "droneConnected" : "droneDisconnected");
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    public EntityDrone getDrone() {
        return drone;
    }

    private ProgWidgetCC getWidget() {
        return getAI().getWidget();
    }

    private DroneAICC getAI() {
        if (drone != null) {
            for (EntityAITaskEntry task : drone.getRunningTasks()) {
                if (task.action instanceof DroneAICC) {
                    return (DroneAICC) task.action;
                }
            }
        }
        // shouldn't get here under normal circumstances : drone is connected but somehow isn't running the CC piece
        setDrone(null);
        throw new IllegalStateException("There's no connected Drone!");
    }

    private DroneAICC getTargetAI() {
        if (drone != null && drone.getRunningTargetAI() instanceof DroneAICC) {
            return (DroneAICC) drone.getRunningTargetAI();
        } else {
            // shouldn't get here under normal circumstances : drone is connected but somehow isn't running the CC piece
            setDrone(null);
            throw new IllegalStateException("There's no connected Drone!");
        }
    }

    private void messageToDrone(Class<? extends IProgWidget> widget) {
        messageToDrone(ItemDye.DYE_COLORS[ItemProgrammingPuzzle.getWidgetForClass(widget).getCraftingColorIndex()]);
    }

    private void messageToDrone(int color) {
        ringSendQueue.offer(color);
    }

}
