package pneumaticCraft.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry.IPneumaticCraftInterface;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.api.drone.ICustomBlockInteract;
import pneumaticCraft.api.drone.IPathfindHandler;
import pneumaticCraft.api.item.IInventoryItem;
import pneumaticCraft.api.recipe.IPneumaticRecipeRegistry;
import pneumaticCraft.api.tileentity.HeatBehaviour;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler.HackingEntityProperties;
import pneumaticCraft.common.heat.HeatExchangerLogic;
import pneumaticCraft.common.heat.HeatExchangerLogicConstant;
import pneumaticCraft.common.heat.HeatExchangerManager;
import pneumaticCraft.common.heat.SimpleHeatExchanger;
import pneumaticCraft.common.heat.behaviour.HeatBehaviourManager;
import pneumaticCraft.common.progwidgets.ProgWidgetCustomBlockInteract;
import pneumaticCraft.common.progwidgets.WidgetRegistrator;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.common.util.ProgrammedDroneUtils;
import pneumaticCraft.lib.Log;

/**
 * With this class you can register your entities to give more info in the tooltip of the Entity Tracker.
 */
public class PneumaticCraftAPIHandler implements IPneumaticCraftInterface{
    private final static PneumaticCraftAPIHandler INSTANCE = new PneumaticCraftAPIHandler();
    public final List<Class<? extends IEntityTrackEntry>> entityTrackEntries = new ArrayList<Class<? extends IEntityTrackEntry>>();
    public final Map<Class<? extends Entity>, Class<? extends IHackableEntity>> hackableEntities = new HashMap<Class<? extends Entity>, Class<? extends IHackableEntity>>();
    public final Map<Block, Class<? extends IHackableBlock>> hackableBlocks = new HashMap<Block, Class<? extends IHackableBlock>>();
    public final Map<String, Class<? extends IHackableEntity>> stringToEntityHackables = new HashMap<String, Class<? extends IHackableEntity>>();
    public final Map<String, Class<? extends IHackableBlock>> stringToBlockHackables = new HashMap<String, Class<? extends IHackableBlock>>();
    public final Map<Block, IPathfindHandler> pathfindableBlocks = new HashMap<Block, IPathfindHandler>();
    public final List<IInventoryItem> inventoryItems = new ArrayList<IInventoryItem>();
    public final List<Integer> concealableRenderIds = new ArrayList<Integer>();
    public final Map<Fluid, Integer> liquidXPs = new HashMap<Fluid, Integer>();
    public final Map<String, Integer> liquidFuels = new HashMap<String, Integer>();

    private PneumaticCraftAPIHandler(){
        concealableRenderIds.add(0);
        concealableRenderIds.add(31);
        concealableRenderIds.add(39);
        concealableRenderIds.add(10);
        concealableRenderIds.add(16);
        concealableRenderIds.add(26);
    }

    public static PneumaticCraftAPIHandler getInstance(){
        return INSTANCE;
    }

    @Override
    public void registerEntityTrackEntry(Class<? extends IEntityTrackEntry> entry){
        if(entry == null) throw new IllegalArgumentException("Can't register null!");
        entityTrackEntries.add(entry);
    }

    @Override
    public void addHackable(Class<? extends Entity> entityClazz, Class<? extends IHackableEntity> iHackable){
        if(entityClazz == null) throw new NullPointerException("Entity class is null!");
        if(iHackable == null) throw new NullPointerException("IHackableEntity is null!");
        if(Entity.class.isAssignableFrom(iHackable)) {
            Log.warning("Entities that implement IHackableEntity shouldn't be registered as hackable! Registering entity: " + entityClazz.getCanonicalName());
        } else {
            try {
                IHackableEntity hackableEntity = iHackable.newInstance();
                if(hackableEntity.getId() != null) stringToEntityHackables.put(hackableEntity.getId(), iHackable);
                hackableEntities.put(entityClazz, iHackable);
            } catch(InstantiationException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                Log.error("Not able to register hackable entity: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addHackable(Block block, Class<? extends IHackableBlock> iHackable){
        if(block == null) throw new NullPointerException("Block is null!");
        if(iHackable == null) throw new NullPointerException("IHackableBlock is null!");

        if(Block.class.isAssignableFrom(iHackable)) {
            Log.warning("Blocks that implement IHackableBlock shouldn't be registered as hackable! Registering block: " + block.getLocalizedName());
        } else {
            try {
                IHackableBlock hackableBlock = iHackable.newInstance();
                if(hackableBlock.getId() != null) stringToBlockHackables.put(hackableBlock.getId(), iHackable);
                hackableBlocks.put(block, iHackable);
            } catch(InstantiationException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Does the class have a parameterless constructor?");
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                Log.error("Not able to register hackable block: " + iHackable.getName() + ". Is the class a public class?");
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<IHackableEntity> getCurrentEntityHacks(Entity entity){
        HackingEntityProperties hackingProps = (HackingEntityProperties)entity.getExtendedProperties("PneumaticCraftHacking");
        if(hackingProps != null) {
            List<IHackableEntity> hackables = hackingProps.getCurrentHacks();
            if(hackables != null) return hackables;
        } else {
            Log.warning("Extended entity props HackingEntityProperties couldn't be found in the entity " + entity.getCommandSenderName());
        }
        return new ArrayList<IHackableEntity>();
    }

    @Override
    public void registerBlockTrackEntry(IBlockTrackEntry entry){
        if(entry == null) throw new IllegalArgumentException("Block Track Entry can't be null!");
        BlockTrackEntryList.instance.trackList.add(entry);
    }

    @Override
    public void addPathfindableBlock(Block block, IPathfindHandler handler){
        if(block == null) throw new IllegalArgumentException("Block can't be null!");
        pathfindableBlocks.put(block, handler);
    }

    @Override
    public int getProtectingSecurityStations(World world, int x, int y, int z, EntityPlayer player, boolean showRangeLines){
        if(world.isRemote) throw new IllegalArgumentException("This method can only be called from the server side!");
        return PneumaticCraftUtils.getProtectingSecurityStations(world, x, y, z, player, showRangeLines, false);
    }

    @Override
    public void registerInventoryItem(IInventoryItem handler){
        inventoryItems.add(handler);
    }

    @Override
    public void registerConcealableRenderId(int id){
        concealableRenderIds.add(id);
    }

    @Override
    public void registerXPLiquid(Fluid fluid, int liquidToPointRatio){
        if(fluid == null) throw new NullPointerException("Fluid can't be null!");
        if(liquidToPointRatio <= 0) throw new IllegalArgumentException("liquidToPointRatio can't be <= 0");
        liquidXPs.put(fluid, liquidToPointRatio);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(){
        return new HeatExchangerLogic();
    }

    public void registerBlockExchanger(Block block, IHeatExchanger heatExchanger){
        HeatExchangerManager.getInstance().registerBlockExchanger(block, heatExchanger);
    }

    public void registerBlockExchanger(Block block, IHeatExchangerLogic heatExchangerLogic){
        registerBlockExchanger(block, new SimpleHeatExchanger(heatExchangerLogic));
    }

    @Override
    public void registerBlockExchanger(Block block, double temperature, double thermalResistance){
        registerBlockExchanger(block, new HeatExchangerLogicConstant(temperature, thermalResistance));
    }

    @Override
    public void registerFuel(Fluid fluid, int mLPerBucket){
        if(fluid == null) throw new NullPointerException("Fluid can't be null!");
        if(mLPerBucket < 0) throw new IllegalArgumentException("mLPerBucket can't be < 0");
        if(liquidFuels.containsKey(fluid.getName())) {
            Log.info("Overriding liquid fuel entry " + fluid.getLocalizedName(new FluidStack(fluid, 1)) + " (" + fluid.getName() + ") with a fuel value of " + mLPerBucket + " (previously " + liquidFuels.get(fluid.getName()) + ")");
            if(mLPerBucket == 0) liquidFuels.remove(fluid.getName());
        }
        if(mLPerBucket > 0) liquidFuels.put(fluid.getName(), mLPerBucket);
    }

    @Override
    public void registerCustomBlockInteractor(ICustomBlockInteract interactor){
        WidgetRegistrator.register(new ProgWidgetCustomBlockInteract().setInteractor(interactor));
    }

    @Override
    public EntityCreature deliverItemsAmazonStyle(World world, int x, int y, int z, ItemStack... deliveredStacks){
        return ProgrammedDroneUtils.deliverItemsAmazonStyle(world, x, y, z, deliveredStacks);
    }

    @Override
    public EntityCreature retrieveItemsAmazonStyle(World world, int x, int y, int z, ItemStack... queriedStacks){
        return ProgrammedDroneUtils.retrieveItemsAmazonStyle(world, x, y, z, queriedStacks);
    }

    @Override
    public EntityCreature deliverFluidAmazonStyle(World world, int x, int y, int z, FluidStack deliveredFluid){
        return ProgrammedDroneUtils.deliverFluidAmazonStyle(world, x, y, z, deliveredFluid);
    }

    @Override
    public EntityCreature retrieveFluidAmazonStyle(World world, int x, int y, int z, FluidStack queriedFluid){
        return ProgrammedDroneUtils.retrieveFluidAmazonStyle(world, x, y, z, queriedFluid);
    }

    @Override
    public IPneumaticRecipeRegistry getRecipeRegistry(){
        return PneumaticRecipeRegistry.getInstance();
    }

    @Override
    public void registerHeatBehaviour(Class<? extends HeatBehaviour> heatBehaviour){
        HeatBehaviourManager.getInstance().registerBehaviour(heatBehaviour);
    }
}
