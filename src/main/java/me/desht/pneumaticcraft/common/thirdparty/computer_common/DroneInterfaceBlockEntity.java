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

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.upgrade.IUpgradeRegistry;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ILuaMethodProvider;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager.WrappedGoal;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered.Ordering;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockRightClicker;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.drone.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketShowArea;
import me.desht.pneumaticcraft.common.network.PacketSpawnRing;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class DroneInterfaceBlockEntity extends AbstractTickingBlockEntity
        implements ILuaMethodProvider {

    private final LuaMethodRegistry luaMethodRegistry = new LuaMethodRegistry(this);

    private DroneEntity drone;
    public float rotationYaw;
    public float rotationPitch = (float) Math.toRadians(-42);
    private final ConcurrentLinkedQueue<Integer> ringSendQueue = new ConcurrentLinkedQueue<>();
    private int ringSendCooldown;
    private IProgWidget curAction;
    private int droneId; // track drone ID client-side

    public DroneInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRONE_INTERFACE.get(), pos, state);
    }

    @Override
    public void tickClient() {
        super.tickClient();

        drone = nonNullLevel().getEntity(droneId) instanceof DroneEntity eDrone ? eDrone : null;

        if (drone != null) {
            double dx = drone.getX() - (getBlockPos().getX() + 0.5);
            double dy = drone.getY() - (getBlockPos().getY() + 0.5);
            double dz = drone.getZ() - (getBlockPos().getZ() + 0.5);
            double f3 = Math.sqrt(dx * dx + dz * dz);
            rotationYaw = (float) -Math.atan2(dx, dz);
            rotationPitch = (float) -Math.atan2(dy, f3);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (drone != null && !drone.isAlive()) {
            setDrone(null);
        }
        if (drone != null) {
            if (ringSendCooldown > 0) ringSendCooldown--;
            if (!ringSendQueue.isEmpty() && ringSendCooldown <= 0) {
                ringSendCooldown = ringSendQueue.size() > 10 ? 1 : 5;
                NetworkHandler.sendToAllTracking(new PacketSpawnRing(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, drone, ringSendQueue.poll()), this);
            }
            if (!getBlockState().getValue(DroneInterfaceBlock.CONNECTED)) {
                nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(DroneInterfaceBlock.CONNECTED, true));
            }
        } else {
            if (getBlockState().getValue(DroneInterfaceBlock.CONNECTED)) {
                NetworkHandler.sendToAllTracking(new PacketShowArea(getBlockPos()), DroneInterfaceBlockEntity.this);
                nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(DroneInterfaceBlock.CONNECTED, false));
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("drone",  drone != null ? drone.getId() : -1);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        droneId = tag.getInt("drone");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        droneId = pkt.getTag().getInt("drone");
    }

    private DroneEntity validateAndGetDrone() {
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
                DroneEntity drone = ModEntityTypes.DRONE.get().create(nonNullLevel());
                ModProgWidgets.PROG_WIDGETS.get().getEntries().forEach(entry -> {
                    IProgWidget widget = IProgWidget.create(entry.getValue());
                    if (widget.canBeRunByComputers(drone, getWidget())) {
                        actions.add(entry.getKey().location().toString());
                    }
                });
                return new Object[]{getStringTable(actions)};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getDronePosition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                DroneEntity d = validateAndGetDrone();
                return new Double[]{d.getX(), d.getY(), d.getZ()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getDronePositionVec") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                DroneEntity d = validateAndGetDrone();
                return new Object[]{Map.of("x", d.getX(), "y", d.getY(), "z", d.getZ())};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setBlockOrder") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "'closest'/'highToLow'/'lowToHigh'");
                String arg = (String) args[0];
                for (Ordering order : Ordering.values()) {
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
                messageToDrone(ModProgWidgets.AREA.get());
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
                messageToDrone(ModProgWidgets.AREA.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearArea();
                messageToDrone(ModProgWidgets.AREA.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("showArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                Set<BlockPos> area = new HashSet<>();
                getWidget().getArea(area);
                NetworkHandler.sendToAllTracking(new PacketShowArea(getBlockPos(), area), DroneInterfaceBlockEntity.this);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("hideArea") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                NetworkHandler.sendToAllTracking(new PacketShowArea(getBlockPos()), DroneInterfaceBlockEntity.this);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 3, "<string> item/block name, <bool> Use NBT, <bool> Use Mod Similarity");
                getWidget().addWhitelistItemFilter((String) args[0], (Boolean) args[1], (Boolean) args[2]);
                messageToDrone(ModProgWidgets.ITEM_FILTER.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 3, "<string> item/block name, <bool> Use NBT, <bool> Use Mod Similarity");
                getWidget().addBlacklistItemFilter((String) args[0], (Boolean) args[1], (Boolean) args[2]);
                messageToDrone(ModProgWidgets.ITEM_FILTER.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearWhitelistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemWhitelist();
                messageToDrone(ModProgWidgets.ITEM_FILTER.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearBlacklistItemFilter") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearItemBlacklist();
                messageToDrone(ModProgWidgets.ITEM_FILTER.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addWhitelistText((String) args[0]);
                messageToDrone(ModProgWidgets.TEXT.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("addBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> text");
                getWidget().addBlacklistText((String) args[0]);
                messageToDrone(ModProgWidgets.TEXT.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearWhitelistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearWhitelistText();
                messageToDrone(ModProgWidgets.TEXT.get());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("clearBlacklistText") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                getWidget().clearBlacklistText();
                messageToDrone(ModProgWidgets.TEXT.get());
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
                requireArgs(args, 1, "<string> '>=', '=' or = '<='");
                getWidget().setOperator((String) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("evaluateCondition") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                if (curAction instanceof ICondition cond) {
                    // Sets the current action's condition variables to the current widget's
                    // This is because the current widget is what has these attributes set, but cannot be what
                    // is compared as it is always the computer control widget
                    cond.setOperator(getWidget().getOperator());
                    cond.setRequiredCount(getWidget().getRequiredCount());
                    cond.setMeasureVar(getWidget().getMeasureVar());
                    cond.setAndFunction(getWidget().isAndFunction());

                    // Evaluates condition
                    boolean bool = cond.evaluate(drone, getWidget());

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

        registry.registerLuaMethod(new LuaMethod("setCanSteal") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> can_steal");
                getWidget().setCanSteal((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setRightClickType") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> 'click_item' or 'click_block'");
                String val = ((String) args[0]).toUpperCase(Locale.ROOT);
                getWidget().setClickType(IBlockRightClicker.RightClickType.valueOf(val));
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
                ProgWidgetType<?> type = ModProgWidgets.PROG_WIDGETS.get().getValue(id);

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
                messageToDrone(widget.getColor());
                curAction = widget;

                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getAction") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                if (curAction != null) {
                    return new Object[] {
                            PneumaticCraftUtils.getRegistryName(ModProgWidgets.PROG_WIDGETS.get(), curAction.getType())
                                    .map(ResourceLocation::toString)
                                    .orElse("?")
                    };
                }
                return null;
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
                validateAndGetDrone().setTarget(null);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getUpgrades") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<string> upgrade_name");
                IUpgradeRegistry reg = PneumaticRegistry.getInstance().getUpgradeRegistry();
                PNCUpgrade upgrade = reg.getUpgradeById(PneumaticCraftUtils.modDefaultedRL((String) args[0]));
                Validate.isTrue(upgrade != null, "unknown upgrade: '" + args[0] + "'");
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
                DroneEntity d = validateAndGetDrone();
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
                return validateAndGetDrone().getVariable((String) args[0]).map(var -> {
                    messageToDrone(0xFFFFFFFF);
                    return new Object[]{var.getX(), var.getY(), var.getZ()};
                }).orElse(null);
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

        registry.registerLuaMethod(new LuaMethod("setSignBack") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> is_back_side");
                getWidget().setSignBackSide((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
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
                return new Object[]{validateAndGetDrone().getName().getString()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getOwnerName") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getOwnerName().getString()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getOwnerID") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{validateAndGetDrone().getOwnerUUID().toString()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setCheckLineOfSight") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> line_of_sight");
                getWidget().setCheckSight((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setAllowStandbyPickup") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "<boolean> standby_pickup");
                getWidget().setAllowStandbyPickup((Boolean) args[0]);
                messageToDrone(0xFFFFFFFF);
                return null;
            }
        });
    }

    @Override
    public LuaMethodRegistry getLuaMethodRegistry() {
        return luaMethodRegistry;
    }

    @Override
    public String getPeripheralType() {
        return "drone_interface";
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    public void setDrone(DroneEntity drone) {
        this.drone = drone;
        ComputerEventManager.getInstance().sendEvents(this, drone != null ? "droneConnected" : "droneDisconnected");
        BlockState state = nonNullLevel().getBlockState(getBlockPos());
        nonNullLevel().sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_ALL);
    }

    public DroneEntity getDrone() {
        return drone;
    }

    private ProgWidgetCC getWidget() {
        return getAI().getWidget();
    }

    private DroneAICC getAI() {
        if (drone != null) {
            for (WrappedGoal wrappedGoal : drone.getRunningTasks()) {
                if (wrappedGoal.goal() instanceof DroneAICC) {
                    return (DroneAICC) wrappedGoal.goal();
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
        messageToDrone(type.create().getColor());
    }

    private void messageToDrone(DyeColor color) {
        float[] c = color.getTextureDiffuseColors();
        messageToDrone(((int)(c[0] * 256) << 24) | ((int)(c[1] * 256) << 16) | (int)(c[2] * 256) | 0xFF000000);
    }

    private void messageToDrone(int color) {
        ringSendQueue.offer(color);
    }
}
