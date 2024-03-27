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

package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.EntityTrackOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.EntityTrackerHandler;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.StringFilterEntitySelector;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackerClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<EntityTrackerHandler> {
    private static final int ENTITY_TRACK_THRESHOLD = 7;
    private static final float ENTITY_TRACKING_RANGE = 16F;
    private static final StatPanelLayout DEFAULT_STAT_LAYOUT = StatPanelLayout.expandsLeft(0.995f, 0.2f);

    private final Int2ObjectMap<RenderEntityTarget> targets = new Int2ObjectOpenHashMap<>();

    private IGuiAnimatedStat entityTrackInfo;
    @Nonnull
    private EntityFilter entityFilter = new EntityFilter("");

    public EntityTrackerClientHandler() {
        super(CommonUpgradeHandlers.entityTrackerHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
        if (!isEnabled) return;

        int rangeUpgrades = armorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.RANGE.get());
        Player player = armorHandler.getPlayer();

        // check for filter change and recompile filter if necessary
        ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
        String filterStr = helmetStack.isEmpty() ? "" : PneumaticArmorItem.getEntityFilter(helmetStack);
        if (!entityFilter.toString().equals(filterStr)) {
            EntityFilter newFilter = EntityFilter.fromString(filterStr);
            if (newFilter != null) {
                entityFilter = newFilter;
            }
        }

        // find applicable entities and create/update render targets for them as needed
        double entityTrackRange = ENTITY_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        AABB bbBox = getAABBFromRange(player, rangeUpgrades);
        List<Entity> entities = armorHandler.getPlayer().level().getEntitiesOfClass(Entity.class, bbBox,
                new EntityTrackerSelector(player, entityFilter, entityTrackRange));
        for (Entity entity : entities) {
            RenderEntityTarget target = targets.get(entity.getId());
            if (target != null) {
                target.ticksExisted = Math.abs(target.ticksExisted); // cancel lost targets
            } else {
                targets.put(entity.getId(), new RenderEntityTarget(entity));
            }
        }

        // prune no-longer-applicable entities from the render target list
        IntList toRemove = new IntArrayList();
        targets.forEach((entityId, target) -> {
            if (!target.entity.isAlive() || player.distanceTo(target.entity) > entityTrackRange + 5 || !entityFilter.test(target.entity)) {
                if (target.ticksExisted > 0) {
                    target.ticksExisted = -60;
                } else if (target.ticksExisted == -1) {
                    toRemove.add(entityId.intValue());
                }
            }
        });
        toRemove.forEach(targets::remove);

        // handle tick logic for every valid render target
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
    public void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure) {
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
            ItemStack icon = ModUpgrades.ENTITY_TRACKER.get().getItemStack();
            entityTrackInfo = ClientArmorRegistry.getInstance().makeHUDStatPanel(xlate("pneumaticcraft.entityTracker.info.trackedEntities"), icon, this);
            entityTrackInfo.setMinimumContractedDimensions(0, 0);
            entityTrackInfo.setAutoLineWrap(false);
        }
        return entityTrackInfo;
    }

    @Override
    public StatPanelLayout getDefaultStatLayout() {
        return DEFAULT_STAT_LAYOUT;
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

    public boolean scroll(InputEvent.MouseScrollingEvent event) {
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
            if (entity == player
                    || !(entity instanceof LivingEntity || entity instanceof HangingEntity || entity instanceof AbstractMinecart)
                    || !entity.isAlive()
                    || player.distanceTo(entity) > threshold)
                return false;
            EntityTrackEvent event = NeoForge.EVENT_BUS.post(new EntityTrackEvent(entity));
            return !event.isCanceled() && super.test(entity);
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
