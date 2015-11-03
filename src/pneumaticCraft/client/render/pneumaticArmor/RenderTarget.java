package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityUtils;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.render.RenderProgressBar;
import pneumaticCraft.client.render.pneumaticArmor.entitytracker.EntityTrackHandler;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketHackingEntityStart;
import pneumaticCraft.common.network.PacketUpdateDebuggingDrone;
import pneumaticCraft.lib.NBTKeys;
import pneumaticCraft.lib.Sounds;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RenderTarget{

    public Entity entity;
    private final RenderTargetCircle circle1;
    private final RenderTargetCircle circle2;
    public int ticksExisted = 0;
    private float oldSize;
    @SideOnly(Side.CLIENT)
    private final GuiAnimatedStat stat;
    private boolean didMakeLockSound;
    public boolean isLookingAtTarget;
    private List<String> textList = new ArrayList<String>();
    private final List<IEntityTrackEntry> trackEntries;
    private int hackTime;

    public RenderTarget(Entity entity){
        this.entity = entity;
        trackEntries = EntityTrackHandler.getTrackersForEntity(entity);
        circle1 = new RenderTargetCircle();
        circle2 = new RenderTargetCircle();
        Item droppedItem = null;
        if(entity instanceof EntityLiving) {
            try {
                droppedItem = EntityUtils.getLivingDrop((EntityLiving)entity);
            } catch(Throwable e) {}
        }
        if(droppedItem != null) {
            stat = new GuiAnimatedStat(null, entity.getCommandSenderName(), new ItemStack(droppedItem, 1, 0), 20, -20, 0x3000AA00, null, false);
        } else {
            stat = new GuiAnimatedStat(null, entity.getCommandSenderName(), "", 20, -20, 0x3000AA00, null, false);
        }
        stat.setMinDimensionsAndReset(0, 0);
    }

    public RenderDroneAI getDroneAIRenderer(){
        for(IEntityTrackEntry tracker : trackEntries) {
            if(tracker instanceof EntityTrackHandler.EntityTrackEntryDrone) {
                return ((EntityTrackHandler.EntityTrackEntryDrone)tracker).getDroneAIRenderer();
            }
        }
        throw new IllegalStateException("[RenderTarget] Drone entity, but no drone AI Renderer?");
    }

    public void update(){
        stat.update();
        stat.setTitle(entity.getCommandSenderName());
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        if(ticksExisted >= 30 && !didMakeLockSound) {
            didMakeLockSound = true;
            player.worldObj.playSound(player.posX, player.posY, player.posZ, Sounds.HUD_ENTITY_LOCK, 0.1F, 1.0F, true);
        }
        boolean tagged = NBTUtil.getInteger(player.getCurrentArmor(3), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == entity.getEntityId();
        circle1.setRenderingAsTagged(tagged);
        circle2.setRenderingAsTagged(tagged);
        circle1.update();
        circle2.update();
        for(IEntityTrackEntry tracker : trackEntries) {
            tracker.update(entity);
        }
        isLookingAtTarget = isPlayerLookingAtTarget();

        if(hackTime > 0) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(entity, PneumaticCraft.proxy.getPlayer());
            if(hackableEntity != null) {
                hackTime++;// = Math.min(hackTime + 1, hackableEntity.getHackTime(entity, PneumaticCraft.proxy.getPlayer()));
            } else {
                hackTime = 0;
            }
        }
    }

    public boolean isInitialized(){
        return ticksExisted > 120;
    }

    public void render(float partialTicks, boolean justRenderWhenHovering){
        for(IEntityTrackEntry tracker : trackEntries) {
            tracker.render(entity, partialTicks);
        }
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + entity.height / 2D;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glPushMatrix();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        float red;
        float green;
        float blue;
        float alpha = 0.5F;
        if(entity instanceof EntityDrone) {
            red = 1;
            green = 1;
            blue = 0;
        } else if(entity instanceof EntityMob || entity instanceof EntitySlime) {
            red = 1;
            green = 0;
            blue = 0;
        } else {
            red = 0;
            green = 1;
            blue = 0;
        }

        float size = entity.height * 0.5F;

        if(ticksExisted < 60) {
            size += 5 - Math.abs(ticksExisted) * 0.083F;
            alpha = Math.abs(ticksExisted) * 0.005F;
        }

        GL11.glTranslated(x, y, z);

        GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F - RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glColor4d(red, green, blue, alpha);
        float renderSize = oldSize + (size - oldSize) * partialTicks;
        circle1.render(renderSize, partialTicks);
        circle2.render(renderSize + 0.2D, partialTicks);
        int targetAcquireProgress = (int)((ticksExisted - 50) / 0.7F);
        if(ticksExisted <= 120 && ticksExisted > 50) {
            GL11.glColor4d(0, 1, 0, 0.8D);
            RenderProgressBar.render(0D, 0.4D, 1.8D, 0.9D, 0, targetAcquireProgress);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
        GL11.glScaled(0.02D, 0.02D, 0.02D);
        GL11.glColor4d(red, green, blue, alpha);
        if(ticksExisted > 120) {
            if(justRenderWhenHovering && !isLookingAtTarget) {
                stat.closeWindow();
            } else {
                stat.openWindow();
            }
            textList = new ArrayList<String>();
            for(IEntityTrackEntry tracker : trackEntries) {
                tracker.addInfo(entity, textList);
            }
            stat.setText(textList);
            stat.render(-1, -1, partialTicks);
        } else if(ticksExisted > 50) {
            fontRenderer.drawString("Acquiring Target...", 0, 0, 0x7F7F7F);
            fontRenderer.drawString(targetAcquireProgress + "%", 37, 28, 0x002F00);
        } else if(ticksExisted < -30) {
            stat.closeWindow();

            //if(stat.getWidth() > stat.getMinWidth() || stat.getHeight() > stat.getMinHeight()) {
            //    stat.setText(new ArrayList<String>());
            stat.render(-1, -1, partialTicks);
            //            }
            fontRenderer.drawString("Lost Target!", 0, 0, 0xFF0000);
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);

        oldSize = size;
    }

    public List<String> getEntityText(){
        return textList;
    }

    private boolean isPlayerLookingAtTarget(){
        // code used from the Enderman player looking code.
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        World world = FMLClientHandler.instance().getClient().theWorld;
        Vec3 vec3 = player.getLook(1.0F).normalize();
        Vec3 vec31 = Vec3.createVectorHelper(entity.posX - player.posX, entity.boundingBox.minY + entity.height / 2.0F - (player.posY + player.getEyeHeight()), entity.posZ - player.posZ);
        double d0 = vec31.lengthVector();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 0.050D / d0;
    }

    public void hack(){
        if(isInitialized() && isPlayerLookingAtTarget()) {
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, PneumaticCraft.proxy.getPlayer());
            if(hackable != null && (hackTime == 0 || hackTime > hackable.getHackTime(entity, PneumaticCraft.proxy.getPlayer()))) NetworkHandler.sendToServer(new PacketHackingEntityStart(entity));
        }
    }

    public void selectAsDebuggingTarget(){
        if(isInitialized() && isPlayerLookingAtTarget() && entity instanceof EntityDrone) {
            NetworkHandler.sendToServer(new PacketUpdateDebuggingDrone(entity.getEntityId()));
        }
    }

    public void onHackConfirmServer(){
        hackTime = 1;
    }

    public int getHackTime(){
        return hackTime;
    }

    public boolean scroll(MouseEvent event){
        if(isInitialized() && isPlayerLookingAtTarget()) {
            return stat.handleMouseWheel(event.dwheel);
        }
        return false;
    }
}
