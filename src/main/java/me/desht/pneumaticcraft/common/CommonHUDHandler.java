package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.MagnetUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmorBase;
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
    private final HashMap<String, CommonHUDHandler> playerHudHandlers = new HashMap<>();
    public int rangeUpgradesInstalled;
    private int speedUpgradesInstalled;
    private int magnetUpgradesInstalled;
    private int magnetRadiusSq;
    private final boolean[][] upgradeRenderersInserted = new boolean[4][];
    private final boolean[][] upgradeRenderersEnabled = new boolean[4][];
    private final int[] ticksSinceEquip = new int[4];
    public float[] armorPressure = new float[4];

    private int hackTime;
    private WorldAndCoord hackedBlock;
    private Entity hackedEntity;
    private boolean magnetEnabled;

    public CommonHUDHandler() {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            upgradeRenderersInserted[slot.getIndex()] = new boolean[renderHandlers.size()];
            upgradeRenderersEnabled[slot.getIndex()] = new boolean[renderHandlers.size()];
        }
    }

    public static CommonHUDHandler getHandlerForPlayer(EntityPlayer player) {
        CommonHUDHandler handler = PneumaticCraftRepressurized.proxy.getCommonHudHandler().playerHudHandlers.get(player.getName());
        if (handler != null) return handler;
        PneumaticCraftRepressurized.proxy.getCommonHudHandler().playerHudHandlers.put(player.getName(), new CommonHUDHandler());
        return getHandlerForPlayer(player);
    }

    @SideOnly(Side.CLIENT)
    public static CommonHUDHandler getHandlerForPlayer() {
        return getHandlerForPlayer(FMLClientHandler.instance().getClient().player);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EntityPlayer player = event.player;
            if (this == PneumaticCraftRepressurized.proxy.getCommonHudHandler()) {
                getHandlerForPlayer(player).tickEnd(event);
            } else {
                for (EntityEquipmentSlot slot :UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    tickArmorPiece(player, slot);
                }
                if (!player.world.isRemote) handleHacking(player);
            }
        }
    }

    private void tickArmorPiece(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (armorStack.getItem() instanceof ItemPneumaticArmorBase) {
            armorPressure[slot.getIndex()] = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
            if (ticksSinceEquip[slot.getIndex()] == 0) {
                checkArmorInventory(player, slot);
            }
            ticksSinceEquip[slot.getIndex()]++;
            if (!player.world.isRemote) {
                if (ticksSinceEquip[slot.getIndex()] > getStartupTime(slot) && !player.capabilities.isCreativeMode) {
                    // use up air in the armor piece
                    float oldPressure = armorPressure[slot.getIndex()];
                    float airUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, slot,false);
                    ((IPressurizable) armorStack.getItem()).addAir(armorStack, (int) -airUsage);
                    if (oldPressure > 0F && ((IPressurizable) armorStack.getItem()).getPressure(armorStack) == 0F) {
                        // out of air!
                        NetworkHandler.sendTo(new PacketPlaySound(Sounds.MINIGUN_STOP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 2.0f, false), (EntityPlayerMP) player);
                    }
                    doArmorActions(player, armorStack, slot);
                }
            }

        } else {
            ticksSinceEquip[slot.getIndex()] = 0;
        }
    }

    private void doArmorActions(EntityPlayer player, ItemStack armorStack, EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST:
                if (magnetEnabled && ticksSinceEquip[slot.getIndex()] % PneumaticValues.MAGNET_INTERVAL == 0) {
                    doMagnet(player, armorStack);
                }
                break;
        }
    }

    private void doMagnet(EntityPlayer player, ItemStack armorStack) {
        AxisAlignedBB box = new AxisAlignedBB(player.getPosition())
                .grow(PneumaticValues.MAGNET_BASE_RANGE + Math.min(magnetUpgradesInstalled, PneumaticValues.MAGNET_MAX_UPGRADES));
        List<EntityItem> itemList = player.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box, EntitySelectors.IS_ALIVE);

        for (EntityItem item : itemList) {
            // TODO solegnolia
            if (item.getEntityData().getBoolean(Names.PREVENT_REMOTE_MOVEMENT)) {
                continue;
            }
            if (!item.cannotPickup() && item.getPositionVector().squareDistanceTo(player.getPositionVector()) <= magnetRadiusSq) {
                float pressure = ((IPressurizable) armorStack.getItem()).getPressure(armorStack);
                if (pressure < 0.1F) break;
                item.setPosition(player.posX, player.posY, player.posZ);
                item.setPickupDelay(0);
                ((IPressurizable) armorStack.getItem()).addAir(armorStack, -PneumaticValues.MAGNET_AIR_USAGE);
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

        if (slot == EntityEquipmentSlot.HEAD) {
            rangeUpgradesInstalled = UpgradableItemUtils.getUpgrades(EnumUpgrade.RANGE, armorStack);
            speedUpgradesInstalled = UpgradableItemUtils.getUpgrades(EnumUpgrade.SPEED, armorStack);
        } else if (slot == EntityEquipmentSlot.CHEST) {
            magnetUpgradesInstalled = UpgradableItemUtils.getUpgrades(EnumUpgrade.MAGNET, armorStack);
            magnetRadiusSq = PneumaticValues.MAGNET_BASE_RANGE + Math.min(magnetUpgradesInstalled, PneumaticValues.MAGNET_MAX_UPGRADES);
            magnetRadiusSq *= magnetRadiusSq;
        }

        ItemStack[] upgradeStacks = UpgradableItemUtils.getUpgradeStacks(armorStack);
        Arrays.fill(upgradeRenderersInserted[slot.getIndex()], false);
        for (int i = 0; i < upgradeRenderersInserted[slot.getIndex()].length; i++) {
            upgradeRenderersInserted[slot.getIndex()][i] = isModuleEnabled(upgradeStacks, UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).get(i));
        }
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
        return 1 + speedUpgradesInstalled;
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

    public boolean isMagnetEnabled() {
        return magnetEnabled;
    }
}
