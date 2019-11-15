package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiEntityTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class EntityTrackUpgradeHandler implements IUpgradeRenderHandler {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;
    private static final String UPGRADE_NAME = "entityTracker";

    private final Map<Integer, RenderEntityTarget> targets = new HashMap<>();
    private boolean shouldStopSpamOnEntityTracking = false;

    @OnlyIn(Dist.CLIENT)
    private GuiAnimatedStat entityTrackInfo;
    @Nonnull
    private EntityFilter entityFilter = new EntityFilter("");

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getUpgradeName() {
        return UPGRADE_NAME;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void update(PlayerEntity player, int rangeUpgrades) {
        SearchUpgradeHandler searchHandler = HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        if (searchHandler != null && (Minecraft.getInstance().world.getGameTime() & 0xf) == 0) {
            searchHandler.trackItemEntities(player, rangeUpgrades, GuiKeybindCheckBox.isHandlerEnabled(searchHandler));
        }

        ItemStack helmetStack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        String filterStr = helmetStack.isEmpty() ? "" : ItemPneumaticArmor.getEntityFilter(helmetStack);
        if (!entityFilter.toString().equals(filterStr)) {
            EntityFilter newFilter = EntityFilter.fromString(filterStr);
            if (newFilter != null) {
                entityFilter = newFilter;
            }
        }

        double entityTrackRange = ENTITY_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        AxisAlignedBB bbBox = getAABBFromRange(player, rangeUpgrades);
        List<Entity> entities = player.world.getEntitiesWithinAABB(Entity.class, bbBox,
                new EntityTrackerSelector(player, entityFilter, entityTrackRange));
        for (Entity entity : entities) {
            RenderEntityTarget target = targets.get(entity.getEntityId());
            if (target != null) {
                target.ticksExisted = Math.abs(target.ticksExisted); // cancel lost targets
            } else {
                targets.put(entity.getEntityId(), new RenderEntityTarget(entity));
            }
        }

        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, RenderEntityTarget> entry : targets.entrySet()) {
            RenderEntityTarget target = entry.getValue();
            if (!target.entity.isAlive() || player.getDistance(target.entity) > entityTrackRange + 5 || !entityFilter.test(target.entity)) {
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
        for (RenderEntityTarget target : targets.values()) {
            boolean wasNegative = target.ticksExisted < 0;
            target.ticksExisted += CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlotType.HEAD);
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

    static AxisAlignedBB getAABBFromRange(PlayerEntity player, int rangeUpgrades) {
        double entityTrackRange = ENTITY_TRACKING_RANGE + Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        return new AxisAlignedBB(player.posX - entityTrackRange, player.posY - entityTrackRange, player.posZ - entityTrackRange, player.posX + entityTrackRange, player.posY + entityTrackRange, player.posZ + entityTrackRange);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render3D(float partialTicks) {
        targets.values().forEach(target -> target.render(partialTicks, shouldStopSpamOnEntityTracking));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render2D(float partialTicks, boolean upgradeEnabled) {
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{ EnumUpgrade.ENTITY_TRACKER.getItem() };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reset() {
        targets.clear();
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return PneumaticValues.USAGE_ENTITY_TRACKER * (1 + (float) Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / ENTITY_TRACKING_RANGE) * CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlotType.HEAD);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IOptionPage getGuiOptionsPage() {
        return new GuiEntityTrackOptions(this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiAnimatedStat getAnimatedStat() {
        if (entityTrackInfo == null) {
            GuiAnimatedStat.StatIcon icon = GuiAnimatedStat.StatIcon.of(EnumUpgrade.ENTITY_TRACKER.getItem());
            entityTrackInfo = new GuiAnimatedStat(null, "Current tracked entities:", icon,
                     0x3000AA00, null, ArmorHUDLayout.INSTANCE.entityTrackerStat);
            entityTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return entityTrackInfo;

    }

    public Stream<RenderEntityTarget> getTargetsStream() {
        return targets.values().stream();
    }

    public RenderEntityTarget getTargetForEntity(Entity entity) {
        return getTargetsStream().filter(target -> target.entity == entity).findFirst().orElse(null);
    }

    public void hack() {
        getTargetsStream().forEach(RenderEntityTarget::hack);
    }

    public void selectAsDebuggingTarget() {
        getTargetsStream().forEach(RenderEntityTarget::selectAsDebuggingTarget);
    }

    public boolean scroll(GuiScreenEvent.MouseScrollEvent.Post event) {
        return getTargetsStream().anyMatch(target -> target.scroll(event));
    }

    private class EntityTrackerSelector extends StringFilterEntitySelector {
        private final PlayerEntity player;
        private final double threshold;

        private EntityTrackerSelector(PlayerEntity player, EntityFilter filter, double threshold) {
            this.player = player;
            this.threshold = threshold;
            setFilter(Collections.singletonList(filter));
        }

        @Override
        public boolean apply(Entity entity) {
            return entity != player
                    && (entity instanceof LivingEntity || entity instanceof HangingEntity)
                    && entity.isAlive()
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
