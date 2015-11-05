package pneumaticCraft.client.render.pneumaticArmor.entitytracker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IEntityTrackEntry.EntityTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.KeyHandler;
import pneumaticCraft.client.render.pneumaticArmor.DroneDebugUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.HackUpgradeRenderHandler;
import pneumaticCraft.client.render.pneumaticArmor.RenderDroneAI;
import pneumaticCraft.client.render.pneumaticArmor.RenderTarget;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.NBTKeys;

public class EntityTrackHandler{
    private static List<IEntityTrackEntry> trackEntries = new ArrayList<IEntityTrackEntry>();

    public static void registerDefaultEntries(){
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryLivingBase.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryHackable.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryDrone.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryPressurizable.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryAgeable.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryTameable.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryCreeper.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntrySlime.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryPlayer.class);
        PneumaticRegistry.getInstance().registerEntityTrackEntry(EntityTrackEntryMob.class);
    }

    public static void init(){
        for(Class<? extends IEntityTrackEntry> clazz : PneumaticCraftAPIHandler.getInstance().entityTrackEntries) {
            try {
                trackEntries.add(clazz.newInstance());
            } catch(InstantiationException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Does it have a parameterless constructor?");
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Is it a public class?");
                e.printStackTrace();
            }
        }
    }

    public static List<IEntityTrackEntry> getTrackersForEntity(Entity entity){
        List<IEntityTrackEntry> trackers = new ArrayList<IEntityTrackEntry>();
        for(IEntityTrackEntry tracker : trackEntries) {
            if(tracker.isApplicable(entity)) {
                try {
                    IEntityTrackEntry newTracker = tracker.getClass().newInstance();
                    newTracker.isApplicable(entity);//just as an initializer.
                    trackers.add(newTracker);
                } catch(Exception e) {
                    //Shouldn't happen, as we tried it in the init().
                    e.printStackTrace();
                }
            }
        }
        return trackers;
    }

    public static class EntityTrackEntryDrone extends EntityTrackEntry{
        private RenderDroneAI droneAIRenderer;

        @Override
        public boolean isApplicable(Entity entity){
            if(entity instanceof EntityDrone) {
                droneAIRenderer = new RenderDroneAI((EntityDrone)entity);
                return true;
            } else {
                return false;
            }
        }

        public RenderDroneAI getDroneAIRenderer(){
            return droneAIRenderer;
        }

        @Override
        public void update(Entity entity){
            droneAIRenderer.update();//TODO render
        }

        @Override
        public void render(Entity entity, float partialTicks){
            droneAIRenderer.render(partialTicks);
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            curInfo.add("Owner: " + ((EntityDrone)entity).playerName);
            curInfo.add("Routine: " + ((EntityDrone)entity).getLabel());
            if(DroneDebugUpgradeHandler.enabledForPlayer(PneumaticCraft.proxy.getPlayer()) && NBTUtil.getInteger(PneumaticCraft.proxy.getPlayer().getCurrentArmor(3), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) != entity.getEntityId()) {
                curInfo.add(EnumChatFormatting.RED + "Press '" + Keyboard.getKeyName(KeyHandler.getInstance().keybindDebuggingDrone.getKeyCode()) + "' to debug");
            }
        }
    }

    public static class EntityTrackEntryPressurizable extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof IPressurizable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            curInfo.add("Pressure: " + PneumaticCraftUtils.roundNumberTo(((IPressurizable)entity).getPressure(null), 1) + " bar");
        }
    }

    public static class EntityTrackEntryLivingBase extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityLivingBase;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            int healthPercentage = (int)(((EntityLivingBase)entity).getHealth() / ((EntityLivingBase)entity).getMaxHealth() * 100F);
            curInfo.add("Health: " + healthPercentage + "%%");
        }
    }

    public static class EntityTrackEntrySlime extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntitySlime;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            switch(((EntitySlime)entity).getSlimeSize()){
                case 1:
                    curInfo.add("Size: Tiny");
                    return;
                case 2:
                    curInfo.add("Size: Small");
                    return;
                case 4:
                    curInfo.add("Size: Big");
                    return;
                default:
                    curInfo.add("Size: " + ((EntitySlime)entity).getSlimeSize());
                    return;
            }
        }
    }

    public static class EntityTrackEntryMob extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityMob;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            Entity target = ((EntityMob)entity).getAttackTarget();
            curInfo.add("Target: " + (target != null ? target.getCommandSenderName() : "-"));
        }
    }

    public static class EntityTrackEntryAgeable extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityAgeable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            int growingAge = ((EntityAgeable)entity).getGrowingAge();
            if(growingAge > 0) {
                curInfo.add("Can breed in " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false));
            } else if(growingAge < 0) {
                curInfo.add("Becomes adult in " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false));
            } else {
                curInfo.add("This animal can be bred");
            }
        }
    }

    public static class EntityTrackEntryTameable extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityTameable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            EntityLivingBase owner = ((EntityTameable)entity).getOwner();
            if(owner != null) {
                curInfo.add("Owner: " + owner.getCommandSenderName());
            } else {
                curInfo.add("This animal can be tamed");
            }
        }
    }

    public static class EntityTrackEntryCreeper extends EntityTrackEntry{
        private int creeperInFuseTime;

        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityCreeper;
        }

        @Override
        public void update(Entity entity){
            if(((EntityCreeper)entity).getCreeperState() == 1) {
                creeperInFuseTime++;
                if(creeperInFuseTime > 30) creeperInFuseTime = 30;
            } else {
                creeperInFuseTime--;
                if(creeperInFuseTime < 0) creeperInFuseTime = 0;
            }
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            if(creeperInFuseTime > 0) {
                if(((EntityCreeper)entity).getCreeperState() == 1) {
                    curInfo.add(EnumChatFormatting.RED + "FUSE: " + Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !");
                } else {
                    curInfo.add(EnumChatFormatting.DARK_GREEN + "Cooling down: " + Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !");
                }
            }
        }
    }

    public static class EntityTrackEntryPlayer extends EntityTrackEntry{
        @Override
        public boolean isApplicable(Entity entity){
            return entity instanceof EntityPlayer;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            EntityPlayer player = (EntityPlayer)entity;
            boolean isArmorEmpty = true;
            for(ItemStack stack : player.inventory.armorInventory) {
                if(stack != null) {
                    isArmorEmpty = false;
                    break;
                }
            }
            boolean isMainInventoryEmpty = true;
            for(ItemStack stack : player.inventory.mainInventory) {
                if(stack != null) {
                    isMainInventoryEmpty = false;
                    break;
                }
            }
            curInfo.add(EnumChatFormatting.GRAY + "Armor:" + (isArmorEmpty ? " -" : ""));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, player.inventory.armorInventory);
            curInfo.add(EnumChatFormatting.GRAY + "Held item:" + (isMainInventoryEmpty ? " -" : ""));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, player.inventory.mainInventory);
        }
    }

    public static class EntityTrackEntryHackable extends EntityTrackEntry{

        @Override
        public boolean isApplicable(Entity entity){
            return HackUpgradeRenderHandler.enabledForPlayer(PneumaticCraft.proxy.getPlayer());
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo){
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, PneumaticCraft.proxy.getPlayer());
            if(hackable != null) {
                List<RenderTarget> targets = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargets();
                int hackTime = 0;
                for(RenderTarget target : targets) {
                    if(target.entity == entity) {
                        hackTime = target.getHackTime();
                        break;
                    }
                }

                if(hackTime == 0) {
                    hackable.addInfo(entity, curInfo, PneumaticCraft.proxy.getPlayer());
                } else {
                    int requiredHackTime = hackable.getHackTime(entity, PneumaticCraft.proxy.getPlayer());
                    int percentageComplete = hackTime * 100 / requiredHackTime;
                    if(percentageComplete < 100) {
                        curInfo.add("Hacking... (" + percentageComplete + "%%)");
                    } else if(hackTime < requiredHackTime + 20) {
                        hackable.addPostHackInfo(entity, curInfo, PneumaticCraft.proxy.getPlayer());
                    } else {
                        hackable.addInfo(entity, curInfo, PneumaticCraft.proxy.getPlayer());
                    }
                }
            }
        }
    }
}