package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiDroneDebuggerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat.StatIcon;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker.EntityTrackHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketHackingEntityStart;
import me.desht.pneumaticcraft.common.network.PacketUpdateDebuggingDrone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RenderEntityTarget {

    public final Entity entity;
    private final RenderTargetCircle circle1;
    private final RenderTargetCircle circle2;
    public int ticksExisted = 0;
    private float oldSize;
    private final WidgetAnimatedStat stat;
    private boolean didMakeLockSound;
    public boolean isLookingAtTarget;
    private List<String> textList = new ArrayList<>();
    private final List<IEntityTrackEntry> trackEntries;
    private int hackTime;

    public RenderEntityTarget(Entity entity) {
        this.entity = entity;
        trackEntries = EntityTrackHandler.getTrackersForEntity(entity);
        circle1 = new RenderTargetCircle();
        circle2 = new RenderTargetCircle();

        stat = new WidgetAnimatedStat(null, entity.getDisplayName().getFormattedText(), StatIcon.NONE,
                20, -20, 0x3000AA00, null, false);
        stat.setMinDimensionsAndReset(0, 0);
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
        stat.setTitle(entity.getDisplayName().getFormattedText());
        PlayerEntity player = Minecraft.getInstance().player;

        if (ticksExisted >= 30 && !didMakeLockSound) {
            didMakeLockSound = true;
            player.world.playSound(player.posX, player.posY, player.posZ, ModSounds.HUD_ENTITY_LOCK.get(), SoundCategory.PLAYERS, 0.1F, 1.0F, true);
        }

        boolean tagged = ItemPneumaticArmor.isPlayerDebuggingEntity(player, entity);
        circle1.setRenderingAsTagged(tagged);
        circle2.setRenderingAsTagged(tagged);
        circle1.update();
        circle2.update();
        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.update(entity);
        }

        isLookingAtTarget = isPlayerLookingAtTarget();

        if (hackTime > 0) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(entity, ClientUtils.getClientPlayer());
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

    public void render(float partialTicks, boolean justRenderWhenHovering) {
        for (IEntityTrackEntry tracker : trackEntries) {
            tracker.render(entity, partialTicks);
        }
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + entity.getHeight() / 2D;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

        GlStateManager.depthMask(false);
        GlStateManager.disableDepthTest();
        GlStateManager.disableCull();
        GlStateManager.disableTexture();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);

        float red;
        float green;
        float blue;
        float alpha = 0.5F;
        if (entity instanceof EntityDrone) {
            red = 1;
            green = 1;
            blue = 0;
        } else if (entity instanceof IMob) {
            red = 1;
            green = 0;
            blue = 0;
        } else if (entity instanceof HangingEntity) {
            red = 0;
            green = 1;
            blue = 1;
        } else {
            red = 0;
            green = 1;
            blue = 0;
        }

        float size = entity.getHeight() * 0.5F;

        if (ticksExisted < 60) {
            size += 5 - Math.abs(ticksExisted) * 0.083F;
            alpha = Math.abs(ticksExisted) * 0.005F;
        }

        GlStateManager.translated(x, y, z);

        GlStateManager.rotated(180.0F - Minecraft.getInstance().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotated(180.0F - Minecraft.getInstance().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.color4f(red, green, blue, alpha);
        float renderSize = oldSize + (size - oldSize) * partialTicks;
        circle1.render(renderSize, partialTicks);
        circle2.render(renderSize + 0.2D, partialTicks);
        float targetAcquireProgress = ((ticksExisted + partialTicks - 50) / 0.7F);
        if (ticksExisted <= 120 && ticksExisted > 50) {
            RenderProgressBar.render(0D, 0.4D, 1.8D, 0.9D, 0, targetAcquireProgress,  0xD0FFFF00, 0xD000FF00);
        }

        GlStateManager.enableTexture();

        FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
        GlStateManager.scaled(0.02D, 0.02D, 0.02D);
        GlStateManager.color4f(red, green, blue, alpha);
        if (ticksExisted > 120) {
            if (justRenderWhenHovering && !isLookingAtTarget) {
                stat.closeWindow();
            } else {
                stat.openWindow();
            }
            textList = new ArrayList<>();
            for (IEntityTrackEntry tracker : trackEntries) {
                tracker.addInfo(entity, textList, isLookingAtTarget);
            }
            stat.setText(textList);
            stat.render(-1, -1, partialTicks);
        } else if (ticksExisted > 50) {
            fontRenderer.drawString("Acquiring Target...", 0, 0, 0x7F7F7F);
            fontRenderer.drawString((int)targetAcquireProgress + "%", 37, 28, 0x002F00);
        } else if (ticksExisted < -30) {
            stat.closeWindow();
            stat.render(-1, -1, partialTicks);
            fontRenderer.drawString("Lost Target!", 0, 0, 0xFF0000);
        }

        GlStateManager.popMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableDepthTest();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        oldSize = size;
    }

    public List<String> getEntityText() {
        return textList;
    }

    private boolean isPlayerLookingAtTarget() {
        // code used from the Enderman player looking code.
        PlayerEntity player = Minecraft.getInstance().player;
        Vec3d vec3 = player.getLook(1.0F).normalize();
        Vec3d vec31 = new Vec3d(entity.posX - player.posX, entity.getBoundingBox().minY + entity.getHeight() / 2.0F - (player.posY + player.getEyeHeight()), entity.posZ - player.posZ);
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 0.050D / d0;
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, ClientUtils.getClientPlayer());
            if (hackable != null && (hackTime == 0 || hackTime > hackable.getHackTime(entity, ClientUtils.getClientPlayer())))
                NetworkHandler.sendToServer(new PacketHackingEntityStart(entity));
        }
    }

    public void selectAsDebuggingTarget() {
        if (isInitialized() && isPlayerLookingAtTarget() && entity instanceof EntityDrone) {
            GuiDroneDebuggerOptions.clearAreaShowWidgetId();
            if (ItemPneumaticArmor.isPlayerDebuggingEntity(ClientUtils.getClientPlayer(), entity)) {
                NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(-1));
                Minecraft.getInstance().player.playSound(ModSounds.SCI_FI.get(), 1.0f, 2.0f);
            } else {
                NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(entity.getEntityId()));
                Minecraft.getInstance().player.playSound(ModSounds.HUD_ENTITY_LOCK.get(), 1.0f, 2.0f);
            }
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(GuiScreenEvent.MouseScrollEvent.Post event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }
}
