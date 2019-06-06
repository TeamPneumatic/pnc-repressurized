package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiEntityTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderTarget;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class EntityTrackUpgradeHandler implements IUpgradeRenderHandler {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;
    private static final String UPGRADE_NAME = "entityTracker";

    private final Map<Integer, RenderTarget> targets = new HashMap<>();
    private boolean shouldStopSpamOnEntityTracking = false;

    @SideOnly(Side.CLIENT)
    private GuiAnimatedStat entityTrackInfo;
    @Nonnull
    private EntityFilter entityFilter = new EntityFilter("");

    @Override
    @SideOnly(Side.CLIENT)
    public String getUpgradeName() {
        return UPGRADE_NAME;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void update(EntityPlayer player, int rangeUpgrades) {
        SearchUpgradeHandler searchHandler = HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        if (searchHandler != null && (Minecraft.getMinecraft().world.getTotalWorldTime() & 0xf) == 0) {
            searchHandler.trackItemEntities(player, rangeUpgrades, GuiKeybindCheckBox.isHandlerEnabled(searchHandler));
        }

        ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        String filterStr = helmetStack.isEmpty() ? "" : NBTUtil.getString(helmetStack, "entityFilter");
        if (!entityFilter.toString().equals(filterStr)) {
            EntityFilter newFilter = EntityFilter.fromString(filterStr);
            if (newFilter != null) {
                entityFilter = newFilter;
            }
        }

        double entityTrackRange = ENTITY_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        AxisAlignedBB bbBox = getAABBFromRange(player, rangeUpgrades);
        List<EntityLivingBase> mobs = player.world.getEntitiesWithinAABB(EntityLivingBase.class, bbBox,
                new EntityTrackerSelector(player, entityFilter, entityTrackRange));
        for (EntityLivingBase mob : mobs) {
            RenderTarget target = targets.get(mob.getEntityId());
            if (target != null) {
                target.ticksExisted = Math.abs(target.ticksExisted); // cancel lost targets
            } else {
                targets.put(mob.getEntityId(), new RenderTarget(mob));
            }
        }

        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, RenderTarget> entry : targets.entrySet()) {
            RenderTarget target = entry.getValue();
            if (target.entity.isDead || player.getDistance(target.entity) > entityTrackRange + 5 || !entityFilter.test(target.entity)) {
                if (target.ticksExisted > 0) {
                    target.ticksExisted = -60;
                } else if (target.ticksExisted == -1) {
                    toRemove.add(entry.getKey());
                }
            }
        }
        toRemove.forEach(targets::remove);

        if (targets.size() > ENTITY_TRACK_THRESHOLD) {
            if (!shouldStopSpamOnEntityTracking) {
                shouldStopSpamOnEntityTracking = true;
                HUDHandler.instance().addMessage(new ArmorMessage("Stopped spam on Entity Tracker", new ArrayList<>(), 60, 0x7700AA00));
            }
        } else {
            shouldStopSpamOnEntityTracking = false;
        }
        List<String> text = new ArrayList<>();
        for (RenderTarget target : targets.values()) {
            boolean wasNegative = target.ticksExisted < 0;
            target.ticksExisted += CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EntityEquipmentSlot.HEAD);
            if (target.ticksExisted >= 0 && wasNegative) target.ticksExisted = -1;
            target.update();
            if (target.isLookingAtTarget) {
                if (target.isInitialized()) {
                    text.add(TextFormatting.GRAY + target.entity.getDisplayName().getFormattedText());
                    text.addAll(target.getEntityText());
                } else {
                    text.add(TextFormatting.GRAY + "Acquiring target...");
                }
            }
        }
        if (text.size() == 0) {
            text.add("Filter mode: " + (entityFilter.toString().isEmpty() ? "None" : entityFilter.toString()));
        }
        entityTrackInfo.setText(text);
    }

    static AxisAlignedBB getAABBFromRange(EntityPlayer player, int rangeUpgrades) {
        double entityTrackRange = ENTITY_TRACKING_RANGE + Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        return new AxisAlignedBB(player.posX - entityTrackRange, player.posY - entityTrackRange, player.posZ - entityTrackRange, player.posX + entityTrackRange, player.posY + entityTrackRange, player.posZ + entityTrackRange);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render3D(float partialTicks) {
        targets.values().forEach(target -> target.render(partialTicks, shouldStopSpamOnEntityTracking));
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
        return PneumaticValues.USAGE_ENTITY_TRACKER * (1 + (float) Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / ENTITY_TRACKING_RANGE) * CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EntityEquipmentSlot.HEAD);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiEntityTrackOptions(this);
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (entityTrackInfo == null) {
            GuiAnimatedStat.StatIcon icon = GuiAnimatedStat.StatIcon.of(CraftingRegistrator.getUpgrade(EnumUpgrade.ENTITY_TRACKER));
            entityTrackInfo = new GuiAnimatedStat(null, "Current tracked entities:", icon,
                     0x3000AA00, null, ArmorHUDLayout.INSTANCE.entityTrackerStat);
            entityTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return entityTrackInfo;

    }

    public Stream<RenderTarget> getTargetsStream() {
        return targets.values().stream();
    }

    public RenderTarget getTargetForEntity(Entity entity) {
        return getTargetsStream().filter(target -> target.entity == entity).findFirst().orElse(null);
    }

    public void hack() {
        getTargetsStream().forEach(RenderTarget::hack);
    }

    public void selectAsDebuggingTarget() {
        getTargetsStream().forEach(RenderTarget::selectAsDebuggingTarget);
    }

    public boolean scroll(MouseEvent event) {
        return getTargetsStream().anyMatch(target -> target.scroll(event));
    }

    private class EntityTrackerSelector extends StringFilterEntitySelector {
        private final EntityPlayer player;
        private final double threshold;

        private EntityTrackerSelector(EntityPlayer player, EntityFilter filter, double threshold) {
            this.player = player;
            this.threshold = threshold;
            setFilter(Collections.singletonList(filter));
        }

        @Override
        public boolean apply(Entity entity) {
            return entity != player
                    && !entity.isDead
                    && player.getDistance(entity) < threshold
                    && !MinecraftForge.EVENT_BUS.post(new EntityTrackEvent(entity))
                    && super.apply(entity);
        }
    }

    @Override
    public void onResolutionChanged() {
        entityTrackInfo = null;
    }
}
