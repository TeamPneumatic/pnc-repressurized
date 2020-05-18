package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.ai.DroneAIManager.EntityAITaskEntry;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.core.ModRegistries;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketShowArea;
import me.desht.pneumaticcraft.common.network.PacketSpawnRing;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.tileentity.ILuaMethodProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class TileEntityDroneInterface extends TileEntity implements ITickableTileEntity, ILuaMethodProvider {

    private final LuaMethodRegistry luaMethodRegistry = new LuaMethodRegistry(this);

    private EntityDrone drone;
    public float rotationYaw;
    public float rotationPitch = (float) Math.toRadians(-42);
    private final ConcurrentLinkedQueue<Integer> ringSendQueue = new ConcurrentLinkedQueue<>();
    private int ringSendCooldown;
    private IProgWidget curAction;
    private int droneId; // track drone ID client-side

    public TileEntityDroneInterface() {
        super(ModTileEntities.DRONE_INTERFACE.get());
    }

    @Override
    public void tick() {
        if (drone != null && !drone.isAlive()) {
            setDrone(null);
        }
        if (drone != null) {
            if (!getWorld().isRemote) {
                if (ringSendCooldown > 0) ringSendCooldown--;
                if (!ringSendQueue.isEmpty() && ringSendCooldown <= 0) {
                    ringSendCooldown = ringSendQueue.size() > 10 ? 1 : 5;
                    NetworkHandler.sendToAllAround(new PacketSpawnRing(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, drone, ringSendQueue.poll()), getWorld());
                }
                if (!getBlockState().get(BlockDroneInterface.CONNECTED)) {
                    world.setBlockState(pos, getBlockState().with(BlockDroneInterface.CONNECTED, true));
                }
            } else {
                // client
                double dx = drone.getPosX() - (getPos().getX() + 0.5);
                double dy = drone.getPosY() - (getPos().getY() + 0.5);
                double dz = drone.getPosZ() - (getPos().getZ() + 0.5);
                float f3 = MathHelper.sqrt(dx * dx + dz * dz);
                rotationYaw = (float) -Math.atan2(dx, dz);
                rotationPitch = (float) -Math.atan2(dy, f3);
            }
        } else {
            if (!getWorld().isRemote && getBlockState().get(BlockDroneInterface.CONNECTED)) {
                world.setBlockState(pos, getBlockState().with(BlockDroneInterface.CONNECTED, false));
            }
        }
        if (getWorld().isRemote) {
            Entity e = getWorld().getEntityByID(droneId);
            if (e instanceof EntityDrone) {
                drone = (EntityDrone) e;
            } else {
                drone = null;
            }
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        tag.putInt("drone",  drone != null ? drone.getEntityId() : -1);
        return tag;
    }


    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
        droneId = tag.getInt("drone");
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    private EntityDrone validateAndGetDrone() {
        if (drone == null) throw new IllegalStateException("There's no connected Drone!");
        return drone;
    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
        registry.registerLuaMethod(new LuaMethod("isConnectedToDrone") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{drone != null};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getDronePressure") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{
                        (double) validateAndGetDrone().getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                                .orElseThrow(RuntimeException::new).getPressure()
                };
            }
        });

        registry.registerLuaMethod(new LuaMethod("exitPiece") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                //noinspection ResultOfMethodCallIgnored
                validateAndGetDrone();
                setDrone(null); // disconnect
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getAllActions") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                List<String> actions = new ArrayList<>();
                EntityDrone drone = ModEntities.DRONE.get().create(getWorld());
                for (ProgWidgetType<?> type : ModProgWidgets.Sorted.WIDGET_LIST) {
                    IProgWidget widget = IProgWidget.create(type);
                    if (widget.canBeRunByComputers(drone, getWidget())) {
                        actions.add(type.getRegistryName().toString());
                    }
                }
                return new Object[]{getStringTable(actions)};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getDronePosition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                EntityDrone d = validateAndGetDrone();
                return new Double[]{d.getPosX(), d.getPosY(), d.getPosZ()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setBlockOrder") {
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

        registry.registerLuaMethod(new LuaMethod("getAreaTypes") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return getWidget().getAreaTypes();
            }
        });

        registry.registerLuaMethod(new LuaMethod("addArea") {
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
                messageToDrone(ModProgWidgets.AREA);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("removeArea") {
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
                messageToDrone(ModProgWidgets.AREA);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearArea();
                messageToDrone(ModProgWidgets.AREA);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("showArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                Set<BlockPos> area = new HashSet<>();
                getWidget().getArea(area);
                NetworkHandler.sendToAllAround(new PacketShowArea(getPos(), area), world);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("hideArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                NetworkHandler.sendToAllAround(new PacketShowArea(getPos()), world);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 3, "<string> item/block name, <bool> Use NBT, <bool> Use Mod Similarity");
                getWidget().addWhitelistItemFilter((String) args[0], (Boolean) args[1], (Boolean) args[2]);
                messageToDrone(ModProgWidgets.ITEM_FILTER);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 3, "<string> item/block name, <bool> Use NBT, <bool> Use Mod Similarity");
                getWidget().addBlacklistItemFilter((String) args[0], (Boolean) args[1], (Boolean) args[2]);
                messageToDrone(ModProgWidgets.ITEM_FILTER);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemWhitelist();
                messageToDrone(ModProgWidgets.ITEM_FILTER);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemBlacklist();
                messageToDrone(ModProgWidgets.ITEM_FILTER);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addWhitelistText((String) args[0]);
                messageToDrone(ModProgWidgets.TEXT);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addBlacklistText((String) args[0]);
                messageToDrone(ModProgWidgets.TEXT);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearWhitelistText();
                messageToDrone(ModProgWidgets.TEXT);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearBlacklistText();
                messageToDrone(ModProgWidgets.TEXT);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setSide") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 2, "down/up/north/south/west/east, <boolean> valid");
                Direction dir = getDirForString((String) args[0]);
                boolean[] sides = getWidget().getSides();
                sides[dir.ordinal()] = (Boolean) args[1]; // We don't need to set them afterwards, got a reference.
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setSides") {
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

        registry.registerLuaMethod(new LuaMethod("setEmittingRedstone") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> signal_strength");
                getWidget().setEmittingRedstone(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setRenameString") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> new_name");
                getWidget().setNewName((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addWhitelistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> fluid_name");
                getWidget().addWhitelistLiquidFilter((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addBlacklistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> fluid_name");
                getWidget().addBlacklistLiquidFilter((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearWhitelistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearLiquidWhitelist();
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearBlacklistLiquidFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearLiquidBlacklist();
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setDropStraight") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> drop_straight");
                getWidget().setDropStraight((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setUseCount") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_use_count");
                getWidget().setUseCount((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setCount") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> use_count");
                getWidget().setCount(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setIsAndFunction") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> is_and_function");
                getWidget().setAndFunction((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setOperator") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> '>=', '=' or = '>='");
                getWidget().setOperator((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("evaluateCondition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                if (curAction instanceof ICondition) {
                    boolean bool = ((ICondition) curAction).evaluate(drone, getWidget());
                    return new Object[]{bool};
                } else {
                    throw new IllegalArgumentException("Current action is not a condition! Action: " + (curAction != null ? curAction.getType().toString() : "*none*"));
                }
            }
        });

        registry.registerLuaMethod(new LuaMethod("setUseMaxActions") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_use_max_actions");
                getWidget().setUseMaxActions((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setMaxActions") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<int> max_actions");
                getWidget().setMaxActions(((Double) args[0]).intValue());
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setSneaking") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> is_sneaking");
                getWidget().setSneaking((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setPlaceFluidBlocks") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> should_place_fluid_blocks");
                getWidget().setPlaceFluidBlocks((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setAction") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> action_name");
                String widgetName = (String) args[0];
                // allow a default namespace of 'pneumaticcraft' if omitted
                ResourceLocation id = widgetName.contains(":") ? new ResourceLocation(widgetName) : RL(widgetName);
                ProgWidgetType<?> type = ModRegistries.PROG_WIDGETS.getValue(id);

                Validate.notNull(type,
                        "No action with the name '" + widgetName + "'!");
                IProgWidget widget = IProgWidget.create(type);
                Validate.isTrue(widget.isAvailable(),
                        "Widget '" + widget.getTypeID() + "' is not available in this instance!");
                Goal ai = widget.getWidgetAI(drone, getWidget());
                Validate.isTrue(ai != null && widget.canBeRunByComputers(drone, getWidget()),
                        "Parsed action '" + widgetName + "' is not a runnable action!");

                getAI().setAction(widget, ai);
                getTargetAI().setAction(widget, widget.getWidgetTargetAI(drone, getWidget()));
                messageToDrone(widget.getColor().getColorValue());
                curAction = widget;

                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getAction") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return curAction != null ? new Object[]{ curAction.getType().getRegistryName().toString() } : null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("abortAction") {
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

        registry.registerLuaMethod(new LuaMethod("isActionDone") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{getAI().isActionDone()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("forgetTarget") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                validateAndGetDrone().setAttackTarget(null);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getUpgrades") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> upgrade_name");
                EnumUpgrade upgrade = EnumUpgrade.valueOf(((String) args[0]).toUpperCase());
                return new Object[]{(double) validateAndGetDrone().getUpgrades(upgrade)};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setCraftingGrid") {
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

        registry.registerLuaMethod(new LuaMethod("setVariable") {
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

        registry.registerLuaMethod(new LuaMethod("getVariable") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> var_name");
                BlockPos var = validateAndGetDrone().getVariable((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return new Object[]{var.getX(), var.getY(), var.getZ()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setSignText") {
            @Override
            public Object[] call(Object[] args) {
                getWidget().signText = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    getWidget().signText[i] = (String) args[i];
                }
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setRequiresTool") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> require_tool");
                getWidget().setRequiresTool((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getDroneName") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getName()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getOwnerName") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getOwnerName()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getOwnerID") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getOwnerUUID()};
            }
        });
    }

    @Override
    public LuaMethodRegistry getLuaMethodRegistry() {
        return luaMethodRegistry;
    }

    @Override
    public String getPeripheralType() {
        return "droneInterface";
    }

    public void setDrone(EntityDrone drone) {
        this.drone = drone;
        ComputerEventSender.getInstance().sendEvents(this, drone != null ? "droneConnected" : "droneDisconnected");
        BlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, Constants.BlockFlags.DEFAULT);
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
                if (task.goal instanceof DroneAICC) {
                    return (DroneAICC) task.goal;
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

    private <P extends ProgWidget> void messageToDrone(ProgWidgetType<P> type) {
        messageToDrone(type.create().getColor().getColorValue());
    }

    private void messageToDrone(int color) {
        ringSendQueue.offer(color);
    }

}
