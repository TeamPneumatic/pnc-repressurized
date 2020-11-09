package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.EntityTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackerClientHandler extends IArmorUpgradeClientHandler.AbstractHandler {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;

    private final Map<Integer, RenderEntityTarget> targets = new HashMap<>();
    private boolean shouldStopSpamOnEntityTracking = false;

    private WidgetAnimatedStat entityTrackInfo;
    @Nonnull
    private EntityFilter entityFilter = new EntityFilter("");

    public EntityTrackerClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().entityTrackerHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        int rangeUpgrades = armorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.RANGE);
        PlayerEntity player = armorHandler.getPlayer();

        SearchClientHandler searchHandler = (SearchClientHandler) ArmorUpgradeClientRegistry.getInstance().getClientHandler(ArmorUpgradeRegistry.getInstance().searchHandler);

        if (searchHandler != null && (Minecraft.getInstance().world.getGameTime() & 0xf) == 0) {
            searchHandler.trackItemEntities(player, rangeUpgrades, WidgetKeybindCheckBox.isHandlerEnabled(ArmorUpgradeRegistry.getInstance().searchHandler));
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
        List<Entity> entities = armorHandler.getPlayer().world.getEntitiesWithinAABB(Entity.class, bbBox,
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
                ITextComponent msg = xlate("pneumaticcraft.blockTracker.message.stopSpam", I18n.format("pneumaticcraft.armor.upgrade.entity_tracker"));
                HUDHandler.getInstance().addMessage(new ArmorMessage(msg, new ArrayList<>(), 60, 0x7700AA00));
            }
        } else {
            shouldStopSpamOnEntityTracking = false;
        }
        List<ITextComponent> text = new ArrayList<>();
        for (RenderEntityTarget target : targets.values()) {
            boolean wasNegative = target.ticksExisted < 0;
            target.ticksExisted += armorHandler.getSpeedFromUpgrades(EquipmentSlotType.HEAD);
            if (target.ticksExisted >= 0 && wasNegative) target.ticksExisted = -1;
            target.update();
            if (target.isLookingAtTarget) {
                if (target.isInitialized()) {
                    text.add(target.entity.getDisplayName().deepCopy().mergeStyle(TextFormatting.GRAY));
                    text.addAll(target.getEntityText());
                } else {
                    text.add(xlate("pneumaticcraft.entityTracker.info.acquiring").mergeStyle(TextFormatting.GRAY));
                }
            }
        }
        if (text.isEmpty()) {
            String f = entityFilter.toString();
            text.add(xlate("pneumaticcraft.gui.entityFilter").appendString(": " + (f.isEmpty() ? "-" : f)));
        }
        entityTrackInfo.setText(text);
    }

    static AxisAlignedBB getAABBFromRange(PlayerEntity player, int rangeUpgrades) {
        double entityTrackRange = ENTITY_TRACKING_RANGE + Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        return new AxisAlignedBB(player.getPosition()).grow(entityTrackRange);
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        targets.values().forEach(target -> target.render(matrixStack, buffer, partialTicks, shouldStopSpamOnEntityTracking));
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean upgradeEnabled) {
    }

    @Override
    public void reset() {
        targets.clear();
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new EntityTrackOptions(screen,this);
    }

    @Override
    public WidgetAnimatedStat getAnimatedStat() {
        if (entityTrackInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(EnumUpgrade.ENTITY_TRACKER.getItemStack());
            entityTrackInfo = new WidgetAnimatedStat(null, xlate("pneumaticcraft.entityTracker.info.trackedEntities"), icon,
                     0x3000AA00, null, ArmorHUDLayout.INSTANCE.entityTrackerStat);
            entityTrackInfo.setMinimumContractedDimensions(0, 0);
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

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        return getTargetsStream().anyMatch(target -> target.scroll(event));
    }

    private static class EntityTrackerSelector extends StringFilterEntitySelector {
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
