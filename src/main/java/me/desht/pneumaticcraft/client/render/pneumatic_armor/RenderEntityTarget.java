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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.DroneDebuggerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker.EntityTrackEntryDrone;
import me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.render.ProgressBarRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.ProgrammableControllerEntity;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityStart;
import me.desht.pneumaticcraft.common.network.PacketUpdateDebuggingDrone;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RenderEntityTarget {
    private static final float STAT_SCALE = 0.02f;

    public final Entity entity;
    private final RenderTargetCircle circle1;
    private final RenderTargetCircle circle2;
    public int ticksExisted = 0;
    private float oldSize;
    private final IGuiAnimatedStat stat;
    private boolean didMakeLockSound;
    public boolean isLookingAtTarget;
    private List<Component> textList = new ArrayList<>();
    private final List<IEntityTrackEntry> trackEntries;
    private int hackTime;
    private double distToEntity;

    public RenderEntityTarget(Entity entity) {
        this.entity = entity;

        trackEntries = EntityTrackHandler.getInstance().getTrackersForEntity(entity);
        circle1 = new RenderTargetCircle(entity);
        circle2 = new RenderTargetCircle(entity);

        stat = new WidgetAnimatedStat(null, entity.getDisplayName(), StatIcon.NONE,
                20, -20, HUDHandler.getInstance().getStatOverlayColor(), null, false);
        stat.setMinimumContractedDimensions(0, 0);
        stat.setAutoLineWrap(false);
    }

    public RenderDroneAI getDroneAIRenderer(AbstractDroneEntity drone) {
        for (IEntityTrackEntry tracker : trackEntries) {
            if (tracker instanceof EntityTrackEntryDrone td) {
                return td.getDroneAIRenderer(drone);
            }
        }
        throw new IllegalStateException("[RenderTarget] Drone entity, but no drone AI Renderer?");
    }

    public void tick() {
        stat.tickWidget();
        stat.setTitle(entity.getDisplayName());
        Player player = ClientUtils.getClientPlayer();

        distToEntity = entity.distanceTo(ClientUtils.getClientPlayer());

        if (ticksExisted >= 30 && !didMakeLockSound) {
            didMakeLockSound = true;
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.HUD_ENTITY_LOCK.get(), SoundSource.PLAYERS, 0.1F, 1.0F, true);
        }

        boolean tagged = entity instanceof AbstractDroneEntity drone && PneumaticArmorItem.isPlayerDebuggingDrone(player, drone);
        circle1.setRenderingAsTagged(tagged);
        circle1.tick();
        circle2.setRenderingAsTagged(tagged);
        circle2.tick();
        trackEntries.forEach(tracker -> tracker.tick(entity));

        isLookingAtTarget = isPlayerLookingAtTarget();

        if (hackTime > 0) {
            if (HackManager.getHackableForEntity(entity, ClientUtils.getClientPlayer()) != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }
    }

    public boolean isInitialized() {
        return ticksExisted > 120;
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks, boolean justRenderWhenHovering) {
        trackEntries.forEach(tracker -> tracker.render(matrixStack, buffer, entity, partialTicks));

        double x = Mth.lerp(partialTicks, entity.xo, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY()) + entity.getBbHeight() / 2D;
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ());

        matrixStack.pushPose();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);

        float size = entity.getBbHeight() * 0.5F;
        float alpha = 0.5F;
        if (ticksExisted < 60) {
            size += 5 - Math.abs(ticksExisted) * 0.083F;
            alpha = Math.abs(ticksExisted) * 0.005F;
        }
        float renderSize = Mth.lerp(partialTicks, oldSize, size);

        circle1.render(matrixStack, buffer, renderSize, partialTicks, alpha);
        circle2.render(matrixStack, buffer, renderSize + 0.2F, partialTicks, alpha);

        float targetAcquireProgress = ((ticksExisted + partialTicks - 50) / 0.7F);
        if (ticksExisted > 50 && ticksExisted <= 120) {
            ProgressBarRenderer.render3d(matrixStack, buffer, 0F, 0.4F, 1.8F, 0.7F, 0, targetAcquireProgress,  0xD0FFFF00, 0xD000FF00);
        }

        matrixStack.scale(STAT_SCALE, STAT_SCALE, STAT_SCALE);

        if (ticksExisted > 120) {
            if (justRenderWhenHovering && !isLookingAtTarget) {
                stat.closeStat();
            } else {
                stat.openStat();
            }
            textList = new ArrayList<>();
            for (IEntityTrackEntry tracker : trackEntries) {
                tracker.addInfo(entity, textList, isLookingAtTarget);
            }
            textList.add(Component.literal(String.format("Dist: %.1fm", distToEntity)));
            stat.setText(textList);
            // a bit of growing or shrinking to keep the stat on screen and/or of legible size
            float mul = getStatSizeMultiplier(distToEntity);
            matrixStack.scale(mul, mul, mul);
            stat.renderStat(matrixStack, buffer, partialTicks);
        } else if (ticksExisted > 50) {
            RenderUtils.renderString3d(Component.translatable("pneumaticcraft.entityTracker.info.acquiring"), 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
            RenderUtils.renderString3d(Component.literal((int)targetAcquireProgress + "%"), 37, 24, 0xFF002F00, matrixStack, buffer, false, true);
        } else if (ticksExisted < -30) {
            stat.closeStat();
            stat.renderStat(matrixStack, buffer, partialTicks);
            RenderUtils.renderString3d(Component.translatable("pneumaticcraft.blockTracker.info.lostTarget"), 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
        }

        matrixStack.popPose();

        oldSize = size;
    }

    private float getStatSizeMultiplier(double dist) {
        if (dist < 4) {
           return (float) (dist / 4);
        } else if (dist < 10) {
            return 1f;
        } else {
            return (float) (dist / 10);
        }
    }

    public List<Component> getEntityText() {
        return textList;
    }

    private boolean isPlayerLookingAtTarget() {
        // code used from the Enderman player looking code.
        Player player = ClientUtils.getClientPlayer();
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(entity.getX() - player.getX(), entity.getBoundingBox().minY + entity.getBbHeight() / 2.0F - (player.getY() + player.getEyeHeight()), entity.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            Player player = ClientUtils.getClientPlayer();
            IHackableEntity<?> hackable = HackManager.getHackableForEntity(entity, player);
            if (hackable != null && (hackTime == 0 || hackTime > hackable._getHackTime(entity, player)))
                NetworkHandler.sendToServer(new PacketHackingEntityStart(entity.getId()));
        }
    }

    public void selectAsDebuggingTarget() {
        if (isInitialized() && isPlayerLookingAtTarget() && entity instanceof AbstractDroneEntity) {
            DroneDebuggerOptions.clearAreaShowWidgetId();
            Player player = ClientUtils.getClientPlayer();
            if (PneumaticArmorItem.isPlayerDebuggingDrone(player, (AbstractDroneEntity) entity)) {
                NetworkHandler.sendToServer(PacketUpdateDebuggingDrone.create(null));
                player.playSound(ModSounds.SCI_FI.get(), 1.0f, 2.0f);
            } else {
                if (entity instanceof DroneEntity drone) {
                    NetworkHandler.sendToServer(PacketUpdateDebuggingDrone.create(drone));
                    player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
                } else if (entity instanceof ProgrammableControllerEntity pce) {
                    NetworkHandler.sendToServer(PacketUpdateDebuggingDrone.create(pce.getController()));
                    player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
                }
            }
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(InputEvent.MouseScrollingEvent event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
        }
        return false;
    }

    public void updateColor(int color) {
        stat.setBackgroundColor(color);
    }

    private static class RenderTargetCircle {
        private static final float[] DRONE = { 1f, 1f, 0f };
        private static final float[] HANGING = { 0f, 1f, 1f };
        private static final float[] HOSTILE = { 1f, 0f, 0f };
        private static final float[] DEFAULT = { 0f, 1f, 0f };

        private static final double MAX_ROTATION = 8.0D;
        private static final float QUARTER_CIRCLE = (float)(Math.PI / 2);
        private static final float STEP = QUARTER_CIRCLE / 15f;

        private double oldRotationAngle;
        private double rotationAngle = 0;
        private double rotationSpeed = 0;
        private double rotationAcceleration = 0;
        private final Random rand;
        private boolean renderAsTagged;
        private final float[] cols = new float[4];

        RenderTargetCircle(Entity entity) {
            rand = ThreadLocalRandom.current();
            System.arraycopy(getCircleColour(entity), 0, cols, 0, 3);
            cols[3] = 0.5f; // alpha
        }

        void setRenderingAsTagged(boolean tagged) {
            renderAsTagged = tagged;
        }

        public void tick() {
            oldRotationAngle = rotationAngle;
            if (rand.nextInt(15) == 0) {
                rotationAcceleration = (rand.nextDouble() - 0.5D) / 2.5D;
            }
            rotationSpeed = Mth.clamp(rotationSpeed + rotationAcceleration, -MAX_ROTATION, MAX_ROTATION);
            rotationAngle += rotationSpeed;
        }

        public void render(PoseStack poseStack, MultiBufferSource buffer, float size, float partialTicks, float alpha) {
            double renderRotationAngle = Mth.lerp(partialTicks, oldRotationAngle, rotationAngle);

            poseStack.pushPose();

            poseStack.mulPose(Axis.ZP.rotationDegrees((float) renderRotationAngle));

            for (int pass = 0; pass < 2; pass++) {
                RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.TARGET_CIRCLE, (posMat, builder) -> {
                    for (float i = 0; i < QUARTER_CIRCLE; i += STEP) {
                        RenderUtils.posF(builder, posMat,Mth.cos(i) * size, Mth.sin(i) * size, 0)
                                .color(cols[0], cols[1], cols[2], alpha)
                                .uv2(RenderUtils.FULL_BRIGHT)
                                .endVertex();
                        RenderUtils.posF(builder, posMat,Mth.cos(i) * (size + 0.1F), Mth.sin(i) * (size + 0.1F), 0)
                                .color(cols[0], cols[1], cols[2], alpha)
                                .uv2(RenderUtils.FULL_BRIGHT)
                                .endVertex();
                    }
                });

                if (renderAsTagged) {
                    RenderUtils.renderWithTypeAndFinish(poseStack, buffer, ModRenderTypes.getLineLoops(3.0), (posMat, builder) -> {
                        for (float i = 0; i < QUARTER_CIRCLE; i += STEP) {
                            Vec3 v1 = new Vec3(Mth.cos(i) * size, Mth.sin(i) * size, 0);
                            Vec3 v2 = new Vec3(Mth.cos(i + STEP) * size, Mth.sin(i + STEP) * size, 0);
                            RenderUtils.posF(builder, posMat, v1.x(), v1.y(), 0)
                                    .color(255, 0, 0, 255)
                                    .normal(poseStack.last(), (float) (v2.x() - v1.x()), (float) (v2.y() - v1.y()), 0f)
                                    .endVertex();
                        }
                        for (float i = QUARTER_CIRCLE - STEP; i >= 0f; i -= STEP) {
                            Vec3 v1 = new Vec3(Mth.cos(i) * size, Mth.sin(i) * size, 0);
                            Vec3 v2 = new Vec3(Mth.cos(i + STEP) * size, Mth.sin(i + STEP) * size, 0);
                            RenderUtils.posF(builder, posMat, v1.x(), v1.y(), 0)
                                    .color(255, 0, 0, 255)
                                    .normal(poseStack.last(), (float) (v2.x() - v1.x()), (float) (v2.y() - v1.y()), 0f)
                                    .endVertex();
                        }
                    });
                }

                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            }
            poseStack.popPose();
        }

        private float[] getCircleColour(Entity entity) {
            if (entity instanceof AbstractDroneEntity) {
                return DRONE;
            } else if (entity instanceof Enemy) {
                return HOSTILE;
            } else if (entity instanceof HangingEntity || entity instanceof AbstractMinecart) {
                return HANGING;
            } else {
                return DEFAULT;
            }
        }
    }
}
