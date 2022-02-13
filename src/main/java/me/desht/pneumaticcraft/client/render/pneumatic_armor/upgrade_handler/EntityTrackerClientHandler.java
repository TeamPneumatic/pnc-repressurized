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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.EntityTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.EntityTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.RenderEntityTarget;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EntityTrackerHandler;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackerClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<EntityTrackerHandler> {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;

    private final Map<Integer, RenderEntityTarget> targets = new HashMap<>();

    private IGuiAnimatedStat entityTrackInfo;
    @Nonnull
    private EntityFilter entityFilter = new EntityFilter("");

    public EntityTrackerClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().entityTrackerHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        int rangeUpgrades = armorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.RANGE.get());
        Player player = armorHandler.getPlayer();

        if ((ClientUtils.getClientLevel().getGameTime() & 0xf) == 0 && WidgetKeybindCheckBox.isHandlerEnabled(ArmorUpgradeRegistry.getInstance().searchHandler)) {
            ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(ArmorUpgradeRegistry.getInstance().searchHandler, SearchClientHandler.class)
                    .trackItemEntities(player, rangeUpgrades);
        }

        ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
        String filterStr = helmetStack.isEmpty() ? "" : ItemPneumaticArmor.getEntityFilter(helmetStack);
        if (!entityFilter.toString().equals(filterStr)) {
            EntityFilter newFilter = EntityFilter.fromString(filterStr);
            if (newFilter != null) {
                entityFilter = newFilter;
            }
        }

        double entityTrackRange = ENTITY_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        AABB bbBox = getAABBFromRange(player, rangeUpgrades);
        List<Entity> entities = armorHandler.getPlayer().level.getEntitiesOfClass(Entity.class, bbBox,
                new EntityTrackerSelector(player, entityFilter, entityTrackRange));
        for (Entity entity : entities) {
            RenderEntityTarget target = targets.get(entity.getId());
            if (target != null) {
                target.ticksExisted = Math.abs(target.ticksExisted); // cancel lost targets
            } else {
                targets.put(entity.getId(), new RenderEntityTarget(entity));
            }
        }

        List<Integer> toRemove = new ArrayList<>();
        targets.forEach((entityId, target) -> {
            if (!target.entity.isAlive() || player.distanceTo(target.entity) > entityTrackRange + 5 || !entityFilter.test(target.entity)) {
                if (target.ticksExisted > 0) {
                    target.ticksExisted = -60;
                } else if (target.ticksExisted == -1) {
                    toRemove.add(entityId);
                }
            }
        });
        toRemove.forEach(targets::remove);

        List<Component> text = new ArrayList<>();
        for (RenderEntityTarget target : targets.values()) {
            boolean wasNegative = target.ticksExisted < 0;
            target.ticksExisted += armorHandler.getSpeedFromUpgrades(EquipmentSlot.HEAD);
            if (target.ticksExisted >= 0 && wasNegative) target.ticksExisted = -1;
            target.tick();
            if (target.isLookingAtTarget) {
                if (target.isInitialized()) {
                    text.add(target.entity.getDisplayName().copy().withStyle(ChatFormatting.GRAY));
                    text.addAll(target.getEntityText());
                } else {
                    text.add(xlate("pneumaticcraft.entityTracker.info.acquiring").withStyle(ChatFormatting.GRAY));
                }
            }
        }
        if (text.isEmpty()) {
            String f = entityFilter.toString();
            text.add(xlate("pneumaticcraft.gui.entityFilter").append(": " + (f.isEmpty() ? "-" : f)));
        }
        entityTrackInfo.setText(text);
    }

    static AABB getAABBFromRange(Player player, int rangeUpgrades) {
        double entityTrackRange = ENTITY_TRACKING_RANGE + Math.min(10, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        return new AABB(player.blockPosition()).inflate(entityTrackRange);
    }

    @Override
    public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        targets.values().forEach(target -> target.render(matrixStack, buffer, partialTicks,  targets.size() > ENTITY_TRACK_THRESHOLD));
    }

    @Override
    public void render2D(PoseStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
        targets.clear();
        entityTrackInfo = null;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new EntityTrackOptions(screen,this);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (entityTrackInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(ModUpgrades.ENTITY_TRACKER.get().getItemStack());
            entityTrackInfo = new WidgetAnimatedStat(null, xlate("pneumaticcraft.entityTracker.info.trackedEntities"), icon,
                    HUDHandler.getInstance().getStatOverlayColor(), null, ArmorHUDLayout.INSTANCE.entityTrackerStat);
            entityTrackInfo.setMinimumContractedDimensions(0, 0);
            entityTrackInfo.setAutoLineWrap(false);
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
        private final Player player;
        private final double threshold;

        private EntityTrackerSelector(Player player, EntityFilter filter, double threshold) {
            this.player = player;
            this.threshold = threshold;
            setFilter(Collections.singletonList(filter));
        }

        @Override
        public boolean test(Entity entity) {
            return entity != player
                    && (entity instanceof LivingEntity || entity instanceof HangingEntity || entity instanceof AbstractMinecart)
                    && entity.isAlive()
                    && player.distanceTo(entity) < threshold
                    && !MinecraftForge.EVENT_BUS.post(new EntityTrackEvent(entity))
                    && super.test(entity);
        }
    }

    @Override
    public void onResolutionChanged() {
        entityTrackInfo = null;
    }

    @Override
    public void setOverlayColor(int color) {
        super.setOverlayColor(color);

        targets.values().forEach(target -> target.updateColor(color));
    }
}
