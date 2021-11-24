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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.DroneDebuggerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityStart;
import me.desht.pneumaticcraft.common.network.PacketUpdateDebuggingDrone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;

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
    private List<ITextComponent> textList = new ArrayList<>();
    private final List<IEntityTrackEntry> trackEntries;
    private int hackTime;
    private double distToEntity;

    public RenderEntityTarget(Entity entity) {
        this.entity = entity;
        trackEntries = EntityTrackHandler.getTrackersForEntity(entity);
        circle1 = new RenderTargetCircle(entity);
        circle2 = new RenderTargetCircle(entity);

        stat = new WidgetAnimatedStat(null, entity.getDisplayName(), StatIcon.NONE,
                20, -20, HUDHandler.getInstance().getStatOverlayColor(), null, false);
        stat.setMinimumContractedDimensions(0, 0);
        stat.setAutoLineWrap(false);
    }

    public RenderDroneAI getDroneAIRenderer() {
        for (IEntityTrackEntry tracker : trackEntries) {
            if (tracker instanceof EntityTrackHandler.EntityTrackEntryDrone) {
                return ((EntityTrackHandler.EntityTrackEntryDrone) tracker).getDroneAIRenderer();
            }
        }
        throw new IllegalStateException("[RenderTarget] Drone entity, but no drone AI Renderer?");
    }

    public void update() {
        stat.tickWidget();
        stat.setTitle(entity.getDisplayName());
        PlayerEntity player = Minecraft.getInstance().player;

        distToEntity = entity.distanceTo(ClientUtils.getClientPlayer());

        if (ticksExisted >= 30 && !didMakeLockSound) {
            didMakeLockSound = true;
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.HUD_ENTITY_LOCK.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
        }

        boolean tagged = entity instanceof EntityDroneBase && ItemPneumaticArmor.isPlayerDebuggingDrone(player, (EntityDroneBase) entity);
        circle1.setRenderingAsTagged(tagged);
        circle2.setRenderingAsTagged(tagged);
        circle1.update();
        circle2.update();
        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.update(entity);
        }

        isLookingAtTarget = isPlayerLookingAtTarget();

        if (hackTime > 0) {
            IHackableEntity hackableEntity = HackManager.getHackableForEntity(entity, ClientUtils.getClientPlayer());
            if (hackableEntity != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }
    }

    public boolean isInitialized() {
        return ticksExisted > 120;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks, boolean justRenderWhenHovering) {
        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.render(matrixStack, buffer, entity, partialTicks);
        }

        double x = MathHelper.lerp(partialTicks, entity.xo, entity.getX());
        double y = MathHelper.lerp(partialTicks, entity.yo, entity.getY()) + entity.getBbHeight() / 2D;
        double z = MathHelper.lerp(partialTicks, entity.zo, entity.getZ());

        matrixStack.pushPose();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);

        float size = entity.getBbHeight() * 0.5F;
        float alpha = 0.5F;
        if (ticksExisted < 60) {
            size += 5 - Math.abs(ticksExisted) * 0.083F;
            alpha = Math.abs(ticksExisted) * 0.005F;
        }
        float renderSize = MathHelper.lerp(partialTicks, oldSize, size);

        circle1.render(matrixStack, buffer, renderSize, partialTicks, alpha);
        circle2.render(matrixStack, buffer, renderSize + 0.2F, partialTicks, alpha);

        float targetAcquireProgress = ((ticksExisted + partialTicks - 50) / 0.7F);
        if (ticksExisted > 50 && ticksExisted <= 120) {
            RenderProgressBar.render3d(matrixStack, buffer, 0D, 0.4D, 1.8D, 0.7D, 0, targetAcquireProgress,  0xD0FFFF00, 0xD000FF00);
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
            textList.add(new StringTextComponent(String.format("Dist: %.1fm", distToEntity)));
            stat.setText(textList);
            // a bit of growing or shrinking to keep the stat on screen and/or of legible size
            float mul = getStatSizeMultiplier(distToEntity);
            matrixStack.scale(mul, mul, mul);
            stat.renderStat(matrixStack, buffer, partialTicks);
        } else if (ticksExisted > 50) {
            RenderUtils.renderString3d("Acquiring Target...", 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
            RenderUtils.renderString3d((int)targetAcquireProgress + "%", 37, 24, 0xFF002F00, matrixStack, buffer, false, true);
        } else if (ticksExisted < -30) {
            stat.closeStat();
            stat.renderStat(matrixStack, buffer, partialTicks);
            RenderUtils.renderString3d("Lost Target!", 0, 0, 0xFF7F7F7F, matrixStack, buffer, false, true);
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

    public List<ITextComponent> getEntityText() {
        return textList;
    }

    private boolean isPlayerLookingAtTarget() {
        // code used from the Enderman player looking code.
        PlayerEntity player = Minecraft.getInstance().player;
        Vector3d vec3 = player.getViewVector(1.0F).normalize();
        Vector3d vec31 = new Vector3d(entity.getX() - player.getX(), entity.getBoundingBox().minY + entity.getBbHeight() / 2.0F - (player.getY() + player.getEyeHeight()), entity.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.050D / d0;
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableEntity hackable = HackManager.getHackableForEntity(entity, ClientUtils.getClientPlayer());
            if (hackable != null && (hackTime == 0 || hackTime > hackable.getHackTime(entity, ClientUtils.getClientPlayer())))
                NetworkHandler.sendToServer(new PacketHackingEntityStart(entity));
        }
    }

    public void selectAsDebuggingTarget() {
        if (isInitialized() && isPlayerLookingAtTarget() && entity instanceof EntityDroneBase) {
            DroneDebuggerOptions.clearAreaShowWidgetId();
            if (ItemPneumaticArmor.isPlayerDebuggingDrone(ClientUtils.getClientPlayer(), (EntityDroneBase) entity)) {
                NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(-1));
                Minecraft.getInstance().player.playSound(ModSounds.SCI_FI.get(), 1.0f, 2.0f);
            } else {
                if (entity instanceof EntityDrone) {
                    NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(entity.getId()));
                    Minecraft.getInstance().player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
                } else if (entity instanceof EntityProgrammableController) {
                    NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(((EntityProgrammableController) entity).getControllerPos()));
                    Minecraft.getInstance().player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
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

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }

    public void updateColor(int color) {
        stat.setBackgroundColor(color);
    }
}
