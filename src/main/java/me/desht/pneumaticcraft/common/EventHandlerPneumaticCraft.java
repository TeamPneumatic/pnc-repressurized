package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.block.IPneumaticWrenchable;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.InventoryTrackEvent;
import me.desht.pneumaticcraft.api.drone.AmadronRetrievalEvent;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.DroneSuicideEvent;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.CapabilityHackingProvider;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.entity.HackableEnderman;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.ai.EntityAINoAIWhenRidingDrone;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.network.PacketRotateBlock;
import me.desht.pneumaticcraft.common.network.PacketSetMobTarget;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.thirdparty.ModInteractionUtilImplementation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefinery;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class EventHandlerPneumaticCraft {

    private static final ItemStack IRON_INGOT = new ItemStack(Items.IRON_INGOT);
    private static final ItemStack IRON_BLOCK = new ItemStack(Blocks.IRON_BLOCK);

    @SubscribeEvent
    public void handleFuelEvent(FurnaceFuelBurnTimeEvent event) {
        FluidStack fluidStack = FluidUtil.getFluidContained(event.getItemStack());
        if (fluidStack != null) {
            int value = PneumaticCraftAPIHandler.getInstance().liquidFuels.getOrDefault(fluidStack.getFluid().getName(), -1);
            event.setBurnTime(value > 0 ? (int)(value * ConfigHandler.general.fuelBucketEfficiencyMultiplier) : -1);
        }
    }

    @SubscribeEvent
    public void handleIronExplosions(ExplosionEvent.Detonate event) {
        if (!ConfigHandler.general.explosionCrafting) {
            return;
        }

        Iterator<Entity> iterator = event.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity instanceof EntityItem) {
                ItemStack stack = ((EntityItem) entity).getItem();
                if (!stack.isEmpty() && !entity.isDead && (PneumaticCraftUtils.isSameOreDictStack(stack, IRON_INGOT) || PneumaticCraftUtils.isSameOreDictStack(stack, IRON_BLOCK))) {
                    Random rand = new Random();
                    int lossRate = ConfigHandler.general.configCompressedIngotLossRate;
                    if (stack.getCount() >= 3 || rand.nextDouble() >= lossRate / 100D) {
                        Item newItem = PneumaticCraftUtils.isSameOreDictStack(stack, IRON_INGOT) ? Itemss.INGOT_IRON_COMPRESSED : Item.getItemFromBlock(Blockss.COMPRESSED_IRON);
                        ItemStack newStack = new ItemStack(newItem, stack.getCount(), stack.getItemDamage());
                        if (stack.getCount() >= 3) {
                            newStack.setCount((int) (stack.getCount() * (rand.nextDouble() * Math.min(lossRate * 0.02D, 0.2D) + (Math.max(0.9D, 1D - lossRate * 0.01D) - lossRate * 0.01D))));
                        }
                        ((EntityItem) entity).setItem(newStack);
                        iterator.remove();
                        if (!event.getWorld().isRemote) {
                            Vec3d exp = event.getExplosion().getPosition();
                            for (EntityPlayer player : event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(exp.x - 32, exp.y - 32, exp.z - 32, exp.x + 32, exp.y + 32, exp.z + 32))) {
                                AdvancementTriggers.EXPLODE_IRON.trigger((EntityPlayerMP) player);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(EntityConstructing event) {
        if (event.getEntity() instanceof IDroneBase) {
            MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent((IDroneBase) event.getEntity()));
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(!event.getWorld().isRemote){
            if(event.getEntity() instanceof EntityLiving){
                ((EntityLiving)event.getEntity()).tasks.addTask(Integer.MIN_VALUE, new EntityAINoAIWhenRidingDrone((EntityLiving)event.getEntity()));
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstruction(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(RL("hacking"), new CapabilityHackingProvider());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnderTeleport(EnderTeleportEvent event) {
        if (!HackableEnderman.onEndermanTeleport(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        RayTraceResult rtr = event.getTarget();
        if (rtr != null) {
            Block b = event.getWorld().getBlockState(rtr.getBlockPos()).getBlock();
            if (b instanceof IFluidBlock) {
                Fluid fluid = ((IFluidBlock) b).getFluid();
                if (TileEntityRefinery.isInputFluidValid(fluid, 4) && event.getEntityPlayer() instanceof EntityPlayerMP) {
                    AdvancementTriggers.OIL_BUCKET.trigger((EntityPlayerMP) event.getEntityPlayer());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.RightClickEmpty) return;

        ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
        IBlockState interactedBlockState = event.getWorld().getBlockState(event.getPos());
        Block interactedBlock = interactedBlockState.getBlock();

        if (!event.getEntityPlayer().capabilities.isCreativeMode || !event.getEntityPlayer().canUseCommand(2, "securityStation")) {
            if (event.getWorld() != null && !event.getWorld().isRemote) {
                if (interactedBlock != Blockss.SECURITY_STATION || event instanceof PlayerInteractEvent.LeftClickBlock) {
                    boolean tryingToPlaceSecurityStation = heldItem.getItem() instanceof ItemBlock && ((ItemBlock) heldItem.getItem()).getBlock() == Blockss.SECURITY_STATION;
                    int blockingStations = PneumaticCraftUtils.getProtectingSecurityStations(event.getWorld(), event.getPos(), event.getEntityPlayer(), true, tryingToPlaceSecurityStation);
                    if (blockingStations > 0) {
                        event.setCanceled(true);
                        event.getEntityPlayer().sendStatusMessage(
                                new TextComponentTranslation(
                                        tryingToPlaceSecurityStation ? "message.securityStation.stationPlacementPrevented" : "message.securityStation.accessPrevented",
                                        blockingStations), false);
                    }
                }
            }
        }

        if (!event.isCanceled() && event instanceof PlayerInteractEvent.RightClickBlock) {
            if (interactedBlock instanceof IPneumaticWrenchable && ModInteractionUtilImplementation.getInstance().isModdedWrench(heldItem)) {
                // When a player clicks one of our rotatable blocks with a wrench from another mod, cancel that and
                // send our custom PacketRotateBlock, which ensures our rotateBlock() gets called & includes the
                // player information, which is needed in several places
                if (event.getWorld().isRemote) {
                    NetworkHandler.sendToServer(new PacketRotateBlock(event.getPos(), event.getFace()));
                }
                event.setCanceled(true);
            }
        }

//        if (!event.isCanceled() && interactedBlock == Blocks.COBBLESTONE) {
//            AdvancementUtils.checkFor9x9(event.getEntityPlayer(), event.getPos());
//        }
    }

    /**
     * Used by PneumaticHelmet
     *
     * @param event
     */
    @SubscribeEvent
    public void onMobTargetSet(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof EntityCreature) {
            if (!event.getEntity().world.isRemote) {
                NetworkHandler.sendToAllAround(
                        new PacketSetMobTarget((EntityCreature) event.getEntity(), event.getTarget()),
                        new NetworkRegistry.TargetPoint(event.getEntity().world.provider.getDimension(),
                                event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, TileEntityConstants.PACKET_UPDATE_DISTANCE));
            } else {
                warnPlayerIfNecessary(event);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void warnPlayerIfNecessary(LivingSetAttackTargetEvent event) {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        if (event.getTarget() == player && (event.getEntityLiving() instanceof EntityGolem || event.getEntityLiving() instanceof EntityMob)) {
            ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (helmetStack.getItem() instanceof ItemPneumaticArmor
                    && ((IPressurizable) helmetStack.getItem()).getPressure(helmetStack) > 0
                    && UpgradableItemUtils.getUpgrades(EnumUpgrade.ENTITY_TRACKER, helmetStack) > 0
                    && GuiKeybindCheckBox.getCoreComponents().checked
                    && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + EntityTrackUpgradeHandler.UPGRADE_NAME).checked) {
                HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).warnIfNecessary(event.getEntity());
            }
        } else {
            HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).removeTargetingEntity(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void quetziMoo(ServerChatEvent event) {
        if (event.getUsername().equals("Quetzz") && event.getMessage().equals("m00")) {
            for (int i = 0; i < 4; i++)
                NetworkHandler.sendTo(new PacketPlaySound(SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, event.getPlayer().posX, event.getPlayer().posY, event.getPlayer().posZ, 1, 1, true), event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            if (event.getWorld().provider.getDimension() == 0) {
                GlobalVariableManager.overworld = event.getWorld();
                event.getWorld().loadData(GlobalVariableManager.class, GlobalVariableManager.DATA_KEY);
            }
        }
    }

    @SubscribeEvent
    public void onEntityTracking(EntityTrackEvent event) {
        if (event.trackingEntity instanceof EntityProgrammableController) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onInventoryTracking(InventoryTrackEvent event) {
        if (event.getTileEntity() instanceof TileEntityProgrammer) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDroneSuicide(DroneSuicideEvent event) {
        if (event.drone instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) event.drone;
            AmadronOffer offer = drone.getHandlingOffer();
            if (offer != null) {
                int times = drone.getOfferTimes();
                if (offer.getInput() instanceof ItemStack) {
                    int requiredCount = ((ItemStack) offer.getInput()).getCount() * times;
                    for (int i = 0; i < drone.getInv().getSlots(); i++) {
                        requiredCount -= drone.getInv().getStackInSlot(i).getCount();
                    }
                    if (requiredCount <= 0) {
                        for (int i = 0; i < drone.getInv().getSlots(); i++) {
                            drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                        }
                        MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                    }
                } else {
                    int requiredCount = ((FluidStack) offer.getInput()).amount * times;
                    if (drone.getTank().getFluidAmount() >= requiredCount) {
                        MinecraftForge.EVENT_BUS.post(new AmadronRetrievalEvent(event.drone));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAmadronSuccess(AmadronRetrievalEvent event) {
        EntityDrone drone = (EntityDrone) event.drone;
        AmadronOffer offer = drone.getHandlingOffer();

        boolean shouldDeliver = false;
        if (offer instanceof AmadronOfferCustom) {
            boolean shouldSave = false;
            AmadronOffer realOffer = AmadronOfferManager.getInstance().get(offer);
            if (realOffer != null) {//If we find the non-inverted offer, that means the Drone just has completed trading with a different player.
                ((AmadronOfferCustom) realOffer).addPayment(drone.getOfferTimes());
                ((AmadronOfferCustom) realOffer).addStock(-drone.getOfferTimes());
                realOffer.onTrade(drone.getOfferTimes(), drone.getBuyingPlayer());
                shouldDeliver = true;
                shouldSave = true;
            }
            realOffer = AmadronOfferManager.getInstance().get(((AmadronOfferCustom) offer).copy().invert());
            if (realOffer != null) {//If we find the inverted offer, that means the Drone has just restocked.
                ((AmadronOfferCustom) realOffer).addStock(drone.getOfferTimes());
                shouldSave = true;
            }
            if (shouldSave) {
                try {
                    AmadronOfferStaticConfig.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            shouldDeliver = true;
        }
        if (shouldDeliver) {
            ItemStack usedTablet = drone.getUsedTablet();
            if (offer.getOutput() instanceof ItemStack) {
                ItemStack offeringItems = (ItemStack) offer.getOutput();
                int producedItems = offeringItems.getCount() * drone.getOfferTimes();
                List<ItemStack> stacks = new ArrayList<>();
                while (producedItems > 0) {
                    ItemStack stack = offeringItems.copy();
                    stack.setCount(Math.min(producedItems, stack.getMaxStackSize()));
                    stacks.add(stack);
                    producedItems -= stack.getCount();
                }
                BlockPos pos = ItemAmadronTablet.getItemProvidingLocation(usedTablet);
                if (pos != null) {
                    World world = DimensionManager.getWorld(ItemAmadronTablet.getItemProvidingDimension(usedTablet));
                    DroneRegistry.getInstance().deliverItemsAmazonStyle(world, pos, stacks.toArray(new ItemStack[0]));
                }
            } else {
                FluidStack offeringFluid = ((FluidStack) offer.getOutput()).copy();
                offeringFluid.amount *= drone.getOfferTimes();
                BlockPos pos = ItemAmadronTablet.getLiquidProvidingLocation(usedTablet);
                if (pos != null) {
                    World world = DimensionManager.getWorld(ItemAmadronTablet.getLiquidProvidingDimension(usedTablet));
                    DroneRegistry.getInstance().deliverFluidAmazonStyle(world, pos, offeringFluid);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (ConfigHandler.general.enableDungeonLoot) {
            String prefix = "minecraft:chests/";
            String name = event.getName().toString();
            if (name.startsWith(prefix)) {
                String file = name.substring(name.indexOf(prefix) + prefix.length());
                switch (file) {
                    case "abandoned_mineshaft":
                    case "desert_pyramid":
                    case "jungle_temple":
                    case "simple_dungeon":
                    case "spawn_bonus_chest":
                    case "stronghold_corridor":
                    case "village_blacksmith":
                        LootEntry entry = new LootEntryTable(RL("inject/simple_dungeon_loot"), 1, 0,  new LootCondition[0], "pneumaticcraft_inject_entry");
                        LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "pneumaticcraft_inject_pool");
                        event.getTable().addPool(pool);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
