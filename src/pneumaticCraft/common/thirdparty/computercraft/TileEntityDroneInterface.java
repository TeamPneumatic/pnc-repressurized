package pneumaticCraft.common.thirdparty.computercraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.ai.DroneAIManager.EntityAITaskEntry;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketShowArea;
import pneumaticCraft.common.network.PacketSpawnRing;
import pneumaticCraft.common.progwidgets.IBlockOrdered;
import pneumaticCraft.common.progwidgets.IBlockOrdered.EnumOrder;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import pneumaticCraft.common.progwidgets.ProgWidgetString;
import pneumaticCraft.common.progwidgets.WidgetRegistrator;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.InterfaceList({@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT), @Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = ModIds.OPEN_COMPUTERS), @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = ModIds.OPEN_COMPUTERS)})
public class TileEntityDroneInterface extends TileEntity implements IPeripheral, ManagedPeripheral, SimpleComponent{

    private final List<IComputerAccess> attachedComputers = new ArrayList<IComputerAccess>();
    private final List<ILuaMethod> luaMethods = new ArrayList<ILuaMethod>();
    private EntityDrone drone;
    public float rotationYaw, rotationPitch = (float)Math.toRadians(-42);
    private final List<Integer> ringSendList = new ArrayList<Integer>();
    private int ringSendCooldown;
    private IProgWidget curAction;

    @Override
    public void updateEntity(){
        if(drone != null && drone.isDead) {
            setDrone(null);
        }
        if(drone != null) {
            if(worldObj.isRemote) {
                double dx = drone.posX - (xCoord + 0.5);
                double dy = drone.posY - (yCoord + 0.5);
                double dz = drone.posZ - (zCoord + 0.5);
                float f3 = MathHelper.sqrt_double(dx * dx + dz * dz);
                rotationYaw = (float)-Math.atan2(dx, dz);
                rotationPitch = (float)-Math.atan2(dy, f3);
            } else {
                if(ringSendCooldown > 0) ringSendCooldown--;
                if(ringSendList.size() > 0 && ringSendCooldown <= 0) {
                    ringSendCooldown = ringSendList.size() > 10 ? 1 : 5;
                    NetworkHandler.sendToDimension(new PacketSpawnRing(xCoord + 0.5, yCoord + 0.8, zCoord + 0.5, drone, ringSendList.remove(0)), worldObj.provider.dimensionId);
                }
            }
        }
    }

    @Override
    public Packet getDescriptionPacket(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("drone", drone != null ? drone.getEntityId() : -1);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
        Entity entity = worldObj.getEntityByID(pkt.func_148857_g().getInteger("drone"));
        drone = entity instanceof EntityDrone ? (EntityDrone)entity : null;
    }

    public TileEntityDroneInterface(){
        luaMethods.add(new LuaMethod("isConnectedToDrone"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return new Object[]{drone != null};
                } else {
                    throw new IllegalArgumentException("isConnectedToDrone doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getDronePressure"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    return new Object[]{(double)drone.getPressure(null)};
                } else {
                    throw new IllegalArgumentException("getDronePressure doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("exitPiece"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    setDrone(null);//disconnect
                    return null;
                } else {
                    throw new IllegalArgumentException("exitPiece doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getAllActions"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    List<String> actions = new ArrayList<String>();
                    for(IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                        if(widget.canBeRunByComputers(new EntityDrone(worldObj), getWidget())) {
                            actions.add(widget.getWidgetString());
                        }
                    }
                    return new Object[]{getStringTable(actions)};
                } else {
                    throw new IllegalArgumentException("getAllActions doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getDronePosition"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return new Double[]{drone.posX, drone.posY, drone.posZ};
                } else {
                    throw new IllegalArgumentException("getDronePosition doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setBlockOrder"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    String arg = (String)args[0];
                    for(EnumOrder order : IBlockOrdered.EnumOrder.values()) {
                        if(order.toString().equalsIgnoreCase(arg)) {
                            getWidget().setOrder(order);
                            return null;
                        }
                    }
                    throw new IllegalArgumentException("No valid order. Valid arguments:  'closest', 'highToLow' or 'lowToHigh'!");
                } else {
                    throw new IllegalArgumentException("setBlockOrder takes one argument, 'closest', 'highToLow' or 'lowToHigh'!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getAreaTypes"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return getWidget().getAreaTypes();
                } else {
                    throw new IllegalArgumentException("getAreaTypes doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addArea"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 3) {
                    getWidget().addArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue());
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else if(args.length == 7) {
                    getWidget().addArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue(), ((Double)args[3]).intValue(), ((Double)args[4]).intValue(), ((Double)args[5]).intValue(), (String)args[6]);
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("addArea either requires 3 arguments (x, y, z), or 7 (x1, y1, z1, x2, y2, z2, areaType)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("removeArea"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 3) {
                    getWidget().removeArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue());
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else if(args.length == 7) {
                    getWidget().removeArea(((Double)args[0]).intValue(), ((Double)args[1]).intValue(), ((Double)args[2]).intValue(), ((Double)args[3]).intValue(), ((Double)args[4]).intValue(), ((Double)args[5]).intValue(), (String)args[6]);
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("removeArea either requires 3 arguments (x, y, z), or 7 (x1, y1, z1, x2, y2, z2, areaType)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearArea"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearArea();
                    messageToDrone(ProgWidgetArea.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("showArea"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    Set<ChunkPosition> area = new HashSet<ChunkPosition>();
                    getWidget().getArea(area);
                    NetworkHandler.sendToDimension(new PacketShowArea(xCoord, yCoord, zCoord, area), worldObj.provider.dimensionId);
                    return null;
                } else {
                    throw new IllegalArgumentException("showArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("hideArea"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    NetworkHandler.sendToDimension(new PacketShowArea(xCoord, yCoord, zCoord), worldObj.provider.dimensionId);
                    return null;
                } else {
                    throw new IllegalArgumentException("hideArea doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistItemFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 6) {
                    getWidget().addWhitelistItemFilter((String)args[0], ((Double)args[1]).intValue(), (Boolean)args[2], (Boolean)args[3], (Boolean)args[4], (Boolean)args[5]);
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("addWhitelistItemFilter takes 6 arguments (<string> item/block name, <number> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistItemFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 6) {
                    getWidget().addBlacklistItemFilter((String)args[0], ((Double)args[1]).intValue(), (Boolean)args[2], (Boolean)args[3], (Boolean)args[4], (Boolean)args[5]);
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("addBlacklistItemFilter takes 6 arguments (<string> item/block name, <number> item/block metadata, <bool> Use Metadata, <bool> Use NBT, <bool> Use Ore Dictionary, <bool> Use Mod Similarity)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistItemFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearItemWhitelist();
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearWhitelistItemFilter doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistItemFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearItemBlacklist();
                    messageToDrone(ProgWidgetItemFilter.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearBlacklistItemFilter doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistText"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().addWhitelistText((String)args[0]);
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("addWhitelistText takes one argument (text)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistText"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().addBlacklistText((String)args[0]);
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("addBlacklistText takes one argument (text)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistText"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearWhitelistText();
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearWhitelistText doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistText"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearBlacklistText();
                    messageToDrone(ProgWidgetString.class);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearBlacklistText doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setSide"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 2) {
                    ForgeDirection dir = getDirForString((String)args[0]);
                    boolean[] sides = getWidget().getSides();
                    sides[dir.ordinal()] = (Boolean)args[1];//We don't need to set them afterwards, got a reference.
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("addSide takes two arguments (direction, <boolean> valid)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setSides"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 6) {
                    boolean[] sides = new boolean[6];
                    for(int i = 0; i < 6; i++) {
                        sides[i] = (Boolean)args[i];
                    }
                    getWidget().setSides(sides);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setSides takes 6 arguments (6x boolean)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setEmittingRedstone"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setEmittingRedstone(((Double)args[0]).intValue());
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setEmittingRedstone takes 1 argument (redstone strength)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setRenameString"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setNewName((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setRenameString takes 1 argument (new name)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addWhitelistLiquidFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().addWhitelistLiquidFilter((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("addWhitelistLiquidFilter takes 1 argument (liquid name)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("addBlacklistLiquidFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().addBlacklistLiquidFilter((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("addBlacklistLiquidFilter takes 1 argument (liquid name)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearWhitelistLiquidFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearLiquidWhitelist();
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearWhitelistLiquidFilter takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("clearBlacklistLiquidFilter"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getWidget().clearLiquidBlacklist();
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("clearBlacklistLiquidFilter takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setDropStraight"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setDropStraight((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setDropStraight takes 1 argument (boolean straight true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setUseCount"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setUseCount((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setUseCount takes 1 argument (boolean use count true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setCount"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setCount(((Double)args[0]).intValue());
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setCount takes 1 argument (count)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setIsAndFunction"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setAndFunction((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setIsAndFunction takes 1 argument (boolean is and function true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setOperator"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setOperator((String)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setOperator takes 1 argument (\">=\" or \"=\")!");
                }
            }
        });

        luaMethods.add(new LuaMethod("evaluateCondition"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    if(curAction instanceof ICondition) {
                        Boolean bool = ((ICondition)curAction).evaluate(drone, getWidget());
                        return new Object[]{bool};
                    } else {
                        throw new IllegalArgumentException("current action is not a condition! Action: " + (curAction != null ? curAction.getWidgetString() : "no action"));
                    }
                } else {
                    throw new IllegalArgumentException("evaluateCondition takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setUseMaxActions"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setUseMaxActions((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setUseMaxActions takes 1 argument (boolean use max actions true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setMaxActions"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setMaxActions(((Double)args[0]).intValue());
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setMaxActions takes 1 argument (actions)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setSneaking"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setSneaking((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setSneaking takes 1 argument (boolean is sneaking true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setPlaceFluidBlocks"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    getWidget().setPlaceFluidBlocks((Boolean)args[0]);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setPlaceFluidBlocks takes 1 argument (boolean is sneaking true/false!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setAction"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    String widgetName = (String)args[0];
                    for(IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                        if(widget.getWidgetString().equalsIgnoreCase(widgetName)) {
                            EntityAIBase ai = widget.getWidgetAI(drone, getWidget());
                            if(ai == null || !widget.canBeRunByComputers(drone, getWidget())) throw new IllegalArgumentException("The parsed action is not a runnable action! Action: \"" + widget.getWidgetString() + "\".");
                            getAI().setAction(widget, ai);
                            getTargetAI().setAction(widget, widget.getWidgetTargetAI(drone, getWidget()));
                            messageToDrone(ItemDye.field_150922_c[widget.getCraftingColorIndex()]);
                            curAction = widget;
                            return null;
                        }
                    }
                    throw new IllegalArgumentException("No action with the name \"" + widgetName + "\"!");
                } else {
                    throw new IllegalArgumentException("setAction takes one argument (action)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getAction"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return curAction != null ? new Object[]{curAction.getWidgetString()} : null;
                } else {
                    throw new IllegalArgumentException("getAction doesn't take arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("abortAction"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    getAI().abortAction();
                    getTargetAI().abortAction();
                    messageToDrone(0xFFFFFFFF);
                    curAction = null;
                    return null;
                } else {
                    throw new IllegalArgumentException("abortAction takes no arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("isActionDone"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return new Object[]{getAI().isActionDone()};
                } else {
                    throw new IllegalArgumentException("isActionDone doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("forgetTarget"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    drone.setAttackTarget(null);
                    return null;
                } else {
                    throw new IllegalArgumentException("forgetTarget doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("getUpgrades"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    return new Object[]{new Double(drone.getUpgrades(((Double)args[0]).intValue()))};
                } else {
                    throw new IllegalArgumentException("getUpgrades takes one argument (upgrade index)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setCraftingGrid"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 9) {
                    String[] grid = new String[9];
                    for(int i = 0; i < 9; i++) {
                        grid[i] = (String)args[i];
                    }
                    getWidget().setCraftingGrid(grid);
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setCraftingGrid takes 9 arguments (crafting items)!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setVariable"){

            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 2 || args.length == 4) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    String varName = (String)args[0];
                    int x = args[1] instanceof Double ? ((Double)args[1]).intValue() : (Boolean)args[1] ? 1 : 0;
                    int y = 0;
                    int z = 0;
                    if(args.length == 4) {
                        y = ((Double)args[2]).intValue();
                        z = ((Double)args[3]).intValue();
                    }
                    drone.setVariable(varName, new ChunkPosition(x, y, z));
                    messageToDrone(0xFFFFFFFF);
                    return null;
                } else {
                    throw new IllegalArgumentException("setVariable takes 2 or 4 arguments (<variable name>, <true/false>), or (<variable name>, <x> [, <y>, <z>])!");
                }
            }

        });

        luaMethods.add(new LuaMethod("getVariable"){

            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    if(drone == null) throw new IllegalStateException("There's no connected Drone!");
                    String varName = (String)args[0];
                    ChunkPosition var = drone.getVariable(varName);
                    messageToDrone(0xFFFFFFFF);
                    return new Object[]{var.chunkPosX, var.chunkPosY, var.chunkPosZ};
                } else {
                    throw new IllegalArgumentException("setVariable takes 2 or 4 arguments (<variable name>, <true/false>), or (<variable name>, <x> [, <y>, <z>])!");
                }
            }

        });

        luaMethods.add(new LuaMethod("setSignText"){

            @Override
            public Object[] call(Object[] args) throws Exception{
                getWidget().signText = new String[args.length];
                for(int i = 0; i < args.length; i++) {
                    getWidget().signText[i] = (String)args[i];
                }
                return null;
            }

        });
    }

    @Override
    public String getType(){
        return "droneInterface";
    }

    @Override
    public String getComponentName(){
        return getType();
    }

    @Override
    public String[] getMethodNames(){
        String[] methodNames = new String[luaMethods.size()];
        for(int i = 0; i < methodNames.length; i++) {
            methodNames[i] = luaMethods.get(i).getMethodName();
        }
        return methodNames;
    }

    @Override
    public String[] methods(){
        return getMethodNames();
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS)
    public Object[] invoke(String method, Context context, Arguments args) throws Exception{
        if("greet".equals(method)) return new Object[]{String.format("Hello, %s!", args.checkString(0))};
        for(ILuaMethod m : luaMethods) {
            if(m.getMethodName().equals(method)) {
                return m.call(args.toArray());
            }
        }
        throw new IllegalArgumentException("Can't invoke method with name \"" + method + "\". not registered");
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException{
        try {
            return luaMethods.get(method).call(arguments);
        } catch(Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void attach(IComputerAccess computer){
        attachedComputers.add(computer);
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void detach(IComputerAccess computer){
        attachedComputers.remove(computer);
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public boolean equals(IPeripheral other){
        if(other == null) {
            return false;
        }
        if(this == other) {
            return true;
        }
        if(other instanceof TileEntity) {
            TileEntity tother = (TileEntity)other;
            return tother.getWorldObj().equals(worldObj) && tother.xCoord == xCoord && tother.yCoord == yCoord && tother.zCoord == zCoord;
        }

        return false;
    }

    private void sendEvent(String name, Object... parms){
        if(Loader.isModLoaded(ModIds.COMPUTERCRAFT)) {
            for(IComputerAccess computer : attachedComputers) {
                computer.queueEvent(name, parms);
            }
        }
    }

    public void setDrone(EntityDrone drone){
        this.drone = drone;
        sendEvent(drone != null ? "droneConnected" : "droneDisconnected");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public EntityDrone getDrone(){
        return drone;
    }

    private ProgWidgetCC getWidget() throws Exception{
        return getAI().getWidget();
    }

    private DroneAICC getAI() throws Exception{
        if(drone != null) {
            for(EntityAITaskEntry task : drone.getRunningTasks()) {
                if(task.action instanceof DroneAICC) return (DroneAICC)task.action;
            }
        }
        setDrone(null);//set to null in case of the drone is connected, but for some reason isn't currently running the piece (shouldn't be possible).
        throw new IllegalStateException("There's no connected Drone!");
    }

    private DroneAICC getTargetAI() throws Exception{
        if(drone != null && drone.getRunningTargetAI() instanceof DroneAICC) {
            return (DroneAICC)drone.getRunningTargetAI();
        } else {
            setDrone(null);//set to null in case of the drone is connected, but for some reason isn't currently running the piece (shouldn't be possible).
            throw new IllegalStateException("There's no connected Drone!");
        }
    }

    private void messageToDrone(Class<? extends IProgWidget> widget){
        messageToDrone(ItemDye.field_150922_c[ItemProgrammingPuzzle.getWidgetForClass(widget).getCraftingColorIndex()]);
    }

    private void messageToDrone(int color){
        ringSendList.add(color);
    }

}
