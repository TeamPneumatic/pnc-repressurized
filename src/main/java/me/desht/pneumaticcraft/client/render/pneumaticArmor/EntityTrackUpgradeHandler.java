package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiEntityTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.Map.Entry;

public class EntityTrackUpgradeHandler implements IUpgradeRenderHandler {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;
    public static final String UPGRADE_NAME = "entityTracker";

    private final List<RenderTarget> targets = new ArrayList<>();
    private final Map<Entity, Integer> targetingEntities = new HashMap<>();
    private boolean shouldStopSpamOnEntityTracking = false;

    public boolean gaveNotAbleToTrackEntityWarning;
    @SideOnly(Side.CLIENT)
    private GuiAnimatedStat entityTrackInfo;
    private int statX;
    private int statY;
    private boolean statLeftSided;

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName() {
        return UPGRADE_NAME;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        String entityFilter = "";
        if (!helmetStack.isEmpty()) entityFilter = NBTUtil.getString(helmetStack, "entityFilter");
        double entityTrackRange = ENTITY_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        AxisAlignedBB bbBox = getAABBFromRange(player, rangeUpgrades);
        List<EntityLivingBase> mobs = player.world.getEntitiesWithinAABB(EntityLivingBase.class, bbBox);
        if (mobs.contains(player)) mobs.remove(player);
        for (EntityLivingBase mob : mobs) {
            if (player.getDistanceToEntity(mob) > entityTrackRange || !PneumaticCraftUtils.isEntityValidForFilter(entityFilter, mob) || MinecraftForge.EVENT_BUS.post(new EntityTrackEvent(mob)))
                continue;
            boolean inList = false;
            for (RenderTarget target : targets) {
                if (target.entity == mob) {
                    inList = true;
                    target.ticksExisted = Math.abs(target.ticksExisted); // cancel lost targets
                    break;
                }
            }
            if (!inList) {
                //player.world.playSoundAtEntity(player, Sounds.CANNON_SOUND, 1.0F, 1.0F);
                targets.add(new RenderTarget(mob));
                if (mob instanceof EntityMob && !isEntityWithinPlayerFOV(player, mob)) {
                    //       HUDHandler.instance().addMessage(new ArmorMessage("A mob is sneaking up on you!", new ArrayList<String>(), 60, 0x70FF0000));
                }
            }
        }
        for (int j = 0; j < targets.size(); j++) {
            RenderTarget target = targets.get(j);
            if (target.entity.isDead || player.getDistanceToEntity(target.entity) > entityTrackRange + 5 || !PneumaticCraftUtils.isEntityValidForFilter(entityFilter, target.entity)) {
                if (target.ticksExisted > 0) {
                    target.ticksExisted = -60;
                } else if (target.ticksExisted == -1) {
                    targets.remove(target);
                    j--;
                }
            }
        }
        if (targets.size() > ENTITY_TRACK_THRESHOLD) {
            if (!shouldStopSpamOnEntityTracking) {
                shouldStopSpamOnEntityTracking = true;
                HUDHandler.instance().addMessage(new ArmorMessage("Stopped spam on Entity Tracker", new ArrayList<>(), 60, 0x7700AA00));
            }
        } else {
            shouldStopSpamOnEntityTracking = false;
        }
        List<String> text = new ArrayList<String>();
        for (RenderTarget target : targets) {
            boolean wasNegative = target.ticksExisted < 0;
            target.ticksExisted += CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
            if (target.ticksExisted >= 0 && wasNegative) target.ticksExisted = -1;
            target.update();
            if (target.isLookingAtTarget) {
                if (target.isInitialized()) {
                    text.add(TextFormatting.GRAY + target.entity.getName());
                    text.addAll(target.getEntityText());
                } else {
                    text.add(TextFormatting.GRAY + "Acquiring target...");
                }
            }
        }
        if (text.size() == 0) {
            text.add("Filter mode: " + (entityFilter.equals("") ? "None" : entityFilter));
        }
        entityTrackInfo.setText(text);

        // Remove entities that don't need to be tracked anymore.
        Iterator<Entry<Entity, Integer>> iterator = targetingEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Entity, Integer> entry = iterator.next();
            Entity entity = entry.getKey();
            if (entry.getValue() >= 0) entry.setValue(entry.getValue() + 1);
            if (entity.isDead || !player.world.getLoadedEntityList().contains(entity) || entry.getValue() > 50)
                iterator.remove();
        }
    }

    public void warnIfNecessary(Entity entity) {
        if (!targetingEntities.containsKey(entity)) {
            HUDHandler.instance().addMessage(new ArmorMessage("A mob is targeting you!", new ArrayList<String>(), 60, 0x70FF0000));
        }
        targetingEntities.put(entity, -1);
    }

    public void removeTargetingEntity(Entity entity) {
        if (targetingEntities.containsKey(entity)) {
            targetingEntities.put(entity, 0);
        }
    }

    public static AxisAlignedBB getAABBFromRange(EntityPlayer player, int rangeUpgrades) {
        double entityTrackRange = ENTITY_TRACKING_RANGE + Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        return new AxisAlignedBB(player.posX - entityTrackRange, player.posY - entityTrackRange, player.posZ - entityTrackRange, player.posX + entityTrackRange, player.posY + entityTrackRange, player.posZ + entityTrackRange);
    }

    private boolean isEntityWithinPlayerFOV(EntityPlayer player, Entity entity) {
        // code used from the Enderman player looking code.
        Vec3d vec3 = player.getLook(1.0F).normalize();
        Vec3d vec31 = new Vec3d(entity.posX - player.posX, entity.getEntityBoundingBox().minY + entity.height / 2.0F - (player.posY + player.getEyeHeight()), entity.posZ - player.posZ);
        double d0 = vec31.lengthVector();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 2.5D / d0;
        // return d1 > 1.0D - 0.025D / d0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
        for (RenderTarget target : targets) {
            target.render(partialTicks, shouldStopSpamOnEntityTracking);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render2D(float partialTicks, boolean upgradeEnabled) {
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.ENTITY_TRACKER)};
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reset() {
        targets.clear();
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return PneumaticValues.USAGE_ENTITY_TRACKER * (1 + (float) Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / ENTITY_TRACKING_RANGE) * CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiEntityTrackOptions(this);
    }

    @Override
    public void initConfig() {
        statX = ConfigHandler.helmetOptions.entityTrackerX;
        statY = ConfigHandler.helmetOptions.entityTrackerY;
        statLeftSided = ConfigHandler.helmetOptions.entityTrackerLeft;
    }

    @Override
    public void saveToConfig() {
        ConfigHandler.helmetOptions.entityTrackerX = statX = entityTrackInfo.getBaseX();
        ConfigHandler.helmetOptions.entityTrackerY = statY = entityTrackInfo.getBaseY();
        ConfigHandler.helmetOptions.entityTrackerLeft = statLeftSided = entityTrackInfo.isLeftSided();
        ConfigHandler.sync();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (entityTrackInfo == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft);
            entityTrackInfo = new GuiAnimatedStat(null, "Current tracked entities:", CraftingRegistrator.getUpgrade(EnumUpgrade.ENTITY_TRACKER), statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            entityTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return entityTrackInfo;

    }

    public List<RenderTarget> getTargets() {
        return targets;
    }

    public RenderTarget getTargetForEntity(Entity entity) {
        for (RenderTarget target : targets) {
            if (target.entity == entity) {
                return target;
            }
        }
        return null;
    }

    public void hack() {
        for (RenderTarget target : targets) {
            target.hack();
        }
    }

    public void selectAsDebuggingTarget() {
        for (RenderTarget target : targets) {
            target.selectAsDebuggingTarget();
        }
    }

    public boolean scroll(MouseEvent event) {
        for (RenderTarget target : targets) {
            if (target.scroll(event)) return true;
        }
        return false;
    }

}
