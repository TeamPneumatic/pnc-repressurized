package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.*;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmorBase;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockFinish;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityFinish;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommonHUDHandler {
    private static final CommonHUDHandler clientHandler = new CommonHUDHandler();
    private static final CommonHUDHandler serverHandler = new CommonHUDHandler();

    private final HashMap<String, CommonHUDHandler> playerHudHandlers = new HashMap<>();
    private int magnetRadius;
    private int magnetRadiusSq;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    public float[] armorPressure = new float[4];
    private int upgradeMatrix[][] = new int [4][];

    private int hackTime;
    private WorldAndCoord hackedBlock;
    private Entity hackedEntity;
    private boolean magnetEnabled;
    private boolean chargingEnabled;
    private boolean stepAssistEnabled;
    private boolean runSpeedEnabled;
    private boolean jumpBoostEnabled;
    private boolean jetBootsEnabled;  // are jet boots switched on?
    private boolean jetBootsActive;  // are jet boots actually firing (player rising) ?

    public CommonHUDHandler() {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeMatrix[slot.getIndex()] = new int[EnumUpgrade.values().length];
        }
    }

    private static CommonHUDHandler getManagerInstance(EntityPlayer player) {
        return player.world.isRemote ? clientHandler : serverHandler;
    }

    public static CommonHUDHandler getHandlerForPlayer(EntityPlayer player) {
        return getManagerInstance(player).playerHudHandlers.computeIfAbsent(player.getName(), v -> new CommonHUDHandler());
    }

    @SideOnly(Side.CLIENT)
    public static CommonHUDHandler getHandlerForPlayer() {
        return getHandlerForPlayer(FMLClientHandler.instance().getClient().player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            getHandlerForPlayer(event.player).tick(event.player);
        }
    }

    private void tick(EntityPlayer player) {
        for (EntityEquipmentSlot slot :UpgradeRenderHandlerList.ARMOR_SLOTS) {
            tickArmorPiece(player, slot);
        }
        if (!player.world.isRemote) handleHacking(player);
    }

    private void tickArmorPiece(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (armorStack.getItem() instanceof ItemPneumaticArmorBase) {
            armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
            if (ticksSinceEquip[slot.getIndex()] == 1) {
                checkArmorInventory(player, slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (!player.world.isRemote) {
                if (isArmorReady(slot) && !player.capabilities.isCreativeMode) {
                    // use up air in the armor piece
                    float airUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, slot,false);
                    if (airUsage != 0) {
                        float oldPressure = addAir(armorStack, slot, (int) -airUsage);
                        if (oldPressure > 0F && armorPressure[slot.getIndex()] == 0F) {
                            // out of air!
                            NetworkHandler.sendTo(new PacketPlaySound(Sounds.MINIGUN_STOP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 2.0f, false), (EntityPlayerMP) player);
                        }
                    }
                    doArmorActions(player, armorStack, slot);
                }
            }

        } else {
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    public float addAir(ItemStack armorStack, EntityEquipmentSlot slot, int air) {
        float oldPressure = armorPressure[slot.getIndex()];
        ((IPressurizable) armorStack.getItem()).addAir(armorStack, air);
        armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
        return oldPressure;
    }

    private void doArmorActions(EntityPlayer player, ItemStack armorStack, EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST:
                if (magnetEnabled && ticksSinceEquip[slot.getIndex()] % PneumaticValues.MAGNET_INTERVAL == 0) {
                    doMagnet(player, armorStack);
                }
                if (chargingEnabled && ticksSinceEquip[slot.getIndex()] % PneumaticValues.ARMOR_CHARGER_INTERVAL == 5) {
                    doCharging(player, armorStack);
                }
                break;
        }

        if (getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE) > 0) {
            doItemRepair(armorStack, slot);
        }
    }

    private void doCharging(EntityPlayer player, ItemStack chestplateStack) {
        int upgrades = Math.min(getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.CHARGING), PneumaticValues.ARMOR_CHARGING_MAX_UPGRADES);
        int airAmount = upgrades * 50 + 100;

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot == EntityEquipmentSlot.CHEST) continue;
            if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) return;
            ItemStack stack = player.getItemStackFromSlot(slot);
            tryPressurize(chestplateStack, airAmount, stack);
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) return;
            tryPressurize(chestplateStack, airAmount, stack);
        }
    }

    private void tryPressurize(ItemStack chestplateStack, int airAmount, ItemStack destStack) {
        if (destStack.getItem() instanceof IPressurizable) {
            IPressurizable p = (IPressurizable) destStack.getItem();
            float pressure = p.getPressure(destStack);
            if (pressure < p.maxPressure(destStack) && pressure < armorPressure[EntityEquipmentSlot.CHEST.getIndex()]) {
                float currentAir = pressure * p.getVolume(destStack);
                float targetAir = armorPressure[EntityEquipmentSlot.CHEST.getIndex()] * p.getVolume(destStack);
                int amountToMove = Math.min((int)(targetAir - currentAir), airAmount);
                p.addAir(destStack, amountToMove);
                addAir(chestplateStack, EntityEquipmentSlot.CHEST, -amountToMove);
            }
        }
    }

    private void doItemRepair(ItemStack armorStack, EntityEquipmentSlot slot) {
        int upgrades = Math.min(getUpgradeCount(slot, EnumUpgrade.ITEM_LIFE), PneumaticValues.ARMOR_REPAIR_MAX_UPGRADES);
        int interval = 120 - (20 * upgrades);
        int airUsage = PneumaticValues.PNEUMATIC_ARMOR_REPAIR_USAGE * upgrades;

        if (armorStack.getItemDamage() > 0
                && armorPressure[slot.getIndex()] > 0.1F
                && ticksSinceEquip[slot.getIndex()] % interval == 0) {
            addAir(armorStack, slot, -airUsage);
            armorStack.setItemDamage(armorStack.getItemDamage() - 1);
        }
    }

    private void doMagnet(EntityPlayer player, ItemStack armorStack) {
        AxisAlignedBB box = new AxisAlignedBB(player.getPosition()).grow(magnetRadius);
        List<EntityItem> itemList = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE);

        for (EntityItem item : itemList) {
            if (!item.cannotPickup()
                    && item.getPositionVector().squareDistanceTo(player.getPositionVector()) <= magnetRadiusSq
                    && !ItemRegistry.getInstance().shouldSuppressMagnet(item)
                    && !item.getEntityData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                if (armorPressure[EntityEquipmentSlot.CHEST.getIndex()] < 0.1F) break;
                item.setPosition(player.posX, player.posY, player.posZ);
                item.setPickupDelay(0);
                addAir(armorStack, EntityEquipmentSlot.CHEST, -PneumaticValues.MAGNET_AIR_USAGE);
            }
        }
    }

    private void handleHacking(EntityPlayer player) {
        if (hackedBlock != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(hackedBlock, player);
            if (hackableBlock != null) {
                if (++hackTime >= hackableBlock.getHackTime(hackedBlock.world, hackedBlock.pos, player)) {
                    hackableBlock.onHackFinished(player.world, hackedBlock.pos, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackBlock(hackedBlock, hackableBlock);
                    NetworkHandler.sendToAllAround(new PacketHackingBlockFinish(hackedBlock), player.world);
                    setHackedBlock(null);
                }
            } else {
                setHackedBlock(null);
            }
        } else if (hackedEntity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(hackedEntity, player);
            if (hackableEntity != null) {
                if (++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    hackableEntity.onHackFinished(hackedEntity, player);
                    PneumaticCraftRepressurized.proxy.getHackTickHandler().trackEntity(hackedEntity, hackableEntity);
                    NetworkHandler.sendToAllAround(new PacketHackingEntityFinish(hackedEntity), new NetworkRegistry.TargetPoint(hackedEntity.world.provider.getDimension(), hackedEntity.posX, hackedEntity.posY, hackedEntity.posZ, 64));
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
        }
    }

    public void checkArmorInventory(EntityPlayer player, EntityEquipmentSlot slot) {
        // armorStack has already been validated as a pneumatic armor piece at this point
        ItemStack armorStack = player.getItemStackFromSlot(slot);

        ItemStack[] upgradeStacks = UpgradableItemUtils.getUpgradeStacks(armorStack);
        Arrays.fill(upgradeRenderersInserted[slot.getIndex()], false);
        for (int i = 0; i < upgradeRenderersInserted[slot.getIndex()].length; i++) {
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgradeStacks, UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(i));
        }

        Arrays.fill(upgradeMatrix[slot.getIndex()], 0);
        for (ItemStack stack : upgradeStacks) {
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                upgradeMatrix[slot.getIndex()][((ItemMachineUpgrade) stack.getItem()).getUpgradeType().ordinal()] += stack.getCount();
            }
        }

        magnetRadius = PneumaticValues.MAGNET_BASE_RANGE
                + Math.min(getUpgradeCount(EntityEquipmentSlot.CHEST, EnumUpgrade.MAGNET), PneumaticValues.MAGNET_MAX_UPGRADES);
        magnetRadiusSq = magnetRadius * magnetRadius;
    }

    public int getUpgradeCount(EntityEquipmentSlot slot, EnumUpgrade upgrade) {
        return upgradeMatrix[slot.getIndex()][upgrade.ordinal()];
    }

    public int getUpgradeCount(EntityEquipmentSlot slot, EnumUpgrade upgrade, int max) {
        return Math.min(max, upgradeMatrix[slot.getIndex()][upgrade.ordinal()]);
    }

    public boolean isUpgradeRendererInserted(EntityEquipmentSlot slot, int i) {
        return upgradeRenderersInserted[slot.getIndex()][i];
    }

    public boolean isUpgradeRendererEnabled(EntityEquipmentSlot slot, int i) {
        return upgradeRenderersEnabled[slot.getIndex()][i];
    }

    public void setUpgradeRenderEnabled(EntityEquipmentSlot slot, byte featureIndex, boolean state) {
        upgradeRenderersEnabled[slot.getIndex()][featureIndex] = state;
        IUpgradeRenderHandler handler = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(featureIndex);
        if (handler instanceof MagnetUpgradeRenderHandler) {
            magnetEnabled = state;
        } else if (handler instanceof ChargingUpgradeRenderHandler) {
            chargingEnabled = state;
        } else if (handler instanceof StepAssistUpgradeHandler) {
            stepAssistEnabled = state;
        } else if (handler instanceof RunSpeedUpgradeHandler) {
            runSpeedEnabled = state;
        } else if (handler instanceof JumpBoostUpgradeHandler) {
            jumpBoostEnabled = state;
        } else if (handler instanceof JetBootsUpgradeHandler) {
            jetBootsEnabled = state;
        }
    }

    public int getTicksSinceEquipped(EntityEquipmentSlot slot) {
        return ticksSinceEquip[slot.getIndex()];
    }

    public void resetTicksSinceEquip(EntityEquipmentSlot slot) {
        ticksSinceEquip[slot.getIndex()] = 0;
    }

    private boolean isModuleEnabled(ItemStack[] helmetStacks, IUpgradeRenderHandler handler) {
        for (Item requiredUpgrade : handler.getRequiredUpgrades()) {
            boolean found = false;
            for (ItemStack stack : helmetStacks) {
                if (stack.getItem() == requiredUpgrade) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public int getSpeedFromUpgrades() {
        return 1 + getUpgradeCount(EntityEquipmentSlot.HEAD, EnumUpgrade.SPEED);
    }

    public int getStartupTime(EntityEquipmentSlot slot) {
        int baseTime = slot == EntityEquipmentSlot.HEAD ? 200 : 100;
        return baseTime / getSpeedFromUpgrades();
    }

    public void setHackedBlock(WorldAndCoord blockPos) {
        hackedBlock = blockPos;
        hackedEntity = null;
        hackTime = 0;
    }

    public void setHackedEntity(Entity entity) {
        hackedEntity = entity;
        hackedBlock = null;
        hackTime = 0;
    }

    public boolean isArmorReady(EntityEquipmentSlot slot) {
        return getTicksSinceEquipped(slot) > getStartupTime(slot);
    }

    public boolean isStepAssistEnabled() {
        return stepAssistEnabled;
    }

    public boolean isRunSpeedEnabled() {
        return runSpeedEnabled;
    }

    public boolean isJumpBoostEnabled() {
        return jumpBoostEnabled;
    }

    public float getArmorPressure(EntityEquipmentSlot slot) {
        return armorPressure[slot.getIndex()];
    }

    public void setJetBootsActive(boolean jetBootsActive) {
        this.jetBootsActive = jetBootsActive;
    }

    public boolean isJetBootsActive() {
        return jetBootsActive;
    }

    public boolean isJetBootsEnabled() {
        return jetBootsEnabled;
    }
}
