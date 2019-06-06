package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IEntityTrackEntry.EntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackUpgradeHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class EntityTrackHandler {
    private static final List<IEntityTrackEntry> trackEntries = new ArrayList<>();

    public static void registerDefaultEntries() {
        IPneumaticHelmetRegistry manager = PneumaticRegistry.getInstance().getHelmetRegistry();
        manager.registerEntityTrackEntry(EntityTrackEntryLivingBase.class);
        manager.registerEntityTrackEntry(EntityTrackEntryHackable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryDrone.class);
        manager.registerEntityTrackEntry(EntityTrackEntryPressurizable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryAgeable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryTameable.class);
        manager.registerEntityTrackEntry(EntityTrackEntryCreeper.class);
        manager.registerEntityTrackEntry(EntityTrackEntrySlime.class);
        manager.registerEntityTrackEntry(EntityTrackEntryPlayer.class);
        manager.registerEntityTrackEntry(EntityTrackEntryMob.class);
    }

    public static void init() {
        for (Class<? extends IEntityTrackEntry> clazz : PneumaticHelmetRegistry.getInstance().entityTrackEntries) {
            try {
                trackEntries.add(clazz.newInstance());
            } catch (InstantiationException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Does it have a parameterless constructor?");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.error("[Entity Tracker] Couldn't registrate " + clazz.getName() + ". Is it a public class?");
                e.printStackTrace();
            }
        }
    }

    public static List<IEntityTrackEntry> getTrackersForEntity(Entity entity) {
        List<IEntityTrackEntry> trackers = new ArrayList<>();
        for (IEntityTrackEntry tracker : trackEntries) {
            if (tracker.isApplicable(entity)) {
                try {
                    IEntityTrackEntry newTracker = tracker.getClass().newInstance();
                    newTracker.isApplicable(entity); // just as an initializer.
                    trackers.add(newTracker);
                } catch (InstantiationException | IllegalAccessException e) {
                    // shouldn't get here, since we already tried this in init()
                    e.printStackTrace();
                }
            }
        }
        return trackers;
    }

    public static class EntityTrackEntryDrone extends EntityTrackEntry {
        private RenderDroneAI droneAIRenderer;

        @Override
        public boolean isApplicable(Entity entity) {
            if (entity instanceof EntityDrone) {
                droneAIRenderer = new RenderDroneAI((EntityDrone) entity);
                return true;
            } else {
                return false;
            }
        }

        public RenderDroneAI getDroneAIRenderer() {
            return droneAIRenderer;
        }

        @Override
        public void update(Entity entity) {
            droneAIRenderer.update();
        }

        @Override
        public void render(Entity entity, float partialTicks) {
            droneAIRenderer.render(partialTicks);
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            curInfo.add(I18n.format("entityTracker.info.tamed", ((EntityDrone) entity).playerName));
            curInfo.add(I18n.format("entityTracker.info.drone.routine", ((EntityDrone) entity).getLabel()));
            EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            if (DroneDebugUpgradeHandler.enabledForPlayer(player)) {
                if (NBTUtil.getInteger(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == entity.getEntityId()) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.debugging"));
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.debugging.key",
                            Keyboard.getKeyName(KeyHandler.getInstance().keybindOpenOptions.getKeyCode())));
                } else if (isLookingAtTarget) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.pressDebugKey",
                            Keyboard.getKeyName(KeyHandler.getInstance().keybindDebuggingDrone.getKeyCode())));
                }
            }
        }
    }

    public static class EntityTrackEntryPressurizable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof IPressurizable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            curInfo.add(I18n.format("gui.tooltip.pressure",
                    PneumaticCraftUtils.roundNumberTo(((IPressurizable) entity).getPressure(null), 1)));
        }
    }

    public static class EntityTrackEntryLivingBase extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityLivingBase;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int healthPercentage = (int) (((EntityLivingBase) entity).getHealth() / ((EntityLivingBase) entity).getMaxHealth() * 100F);
            curInfo.add(I18n.format("entityTracker.info.health", healthPercentage));
        }
    }

    public static class EntityTrackEntrySlime extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntitySlime;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            switch (((EntitySlime) entity).getSlimeSize()) {
                case 1:
                    curInfo.add("Size: Tiny");
                    break;
                case 2:
                    curInfo.add("Size: Small");
                    break;
                case 4:
                    curInfo.add("Size: Big");
                    break;
                default:
                    curInfo.add("Size: " + ((EntitySlime) entity).getSlimeSize());
                    break;
            }
        }
    }

    public static class EntityTrackEntryMob extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityMob;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            Entity target = ((EntityMob) entity).getAttackTarget();
            if (target != null) {
                curInfo.add("Target: " + target.getDisplayName().getUnformattedText());
            }
        }
    }

    // TODO this doesn't fully work since the client doesn't get the full age data of an entity
    // but is it worth going to the trouble of requesting extra server data?
    public static class EntityTrackEntryAgeable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityAgeable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int growingAge = ((EntityAgeable) entity).getGrowingAge();
            if (growingAge > 0) {
                curInfo.add(I18n.format("entityTracker.info.canBreedIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false)));
            } else if (growingAge < 0) {
                curInfo.add(I18n.format("entityTracker.info.growsUpIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false)));
            } else {
                curInfo.add(I18n.format("entityTracker.info.canBreedNow"));
            }
        }
    }

    public static class EntityTrackEntryTameable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityTameable;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            EntityLivingBase owner = ((EntityTameable) entity).getOwner();
            if (owner != null) {
                curInfo.add(I18n.format("entityTracker.info.tamed", owner.getDisplayName().getUnformattedText()));
            } else {
                curInfo.add(I18n.format("entityTracker.info.canTame"));
            }
        }
    }

    public static class EntityTrackEntryCreeper extends EntityTrackEntry {
        private int creeperInFuseTime;

        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityCreeper;
        }

        @Override
        public void update(Entity entity) {
            if (((EntityCreeper) entity).getCreeperState() == 1) {
                creeperInFuseTime++;
                if (creeperInFuseTime > 30) creeperInFuseTime = 30;
            } else {
                creeperInFuseTime--;
                if (creeperInFuseTime < 0) creeperInFuseTime = 0;
            }
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            if (creeperInFuseTime > 0) {
                if (((EntityCreeper) entity).getCreeperState() == 1) {
                    curInfo.add(TextFormatting.RED + I18n.format("entityTracker.info.creeper.fuse", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !"));
                } else {
                    curInfo.add(TextFormatting.DARK_GREEN + I18n.format("entityTracker.info.creeper.coolDown", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !"));
                }
            }
        }
    }

    public static class EntityTrackEntryPlayer extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof EntityPlayer;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            EntityPlayer player = (EntityPlayer) entity;

            curInfo.add(TextFormatting.GRAY + I18n.format("entityTracker.info.player.armor"));
            int l = curInfo.size();
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, asItemStackArray(player.inventory.armorInventory));
            if (l == curInfo.size()) curInfo.add(I18n.format("gui.misc.no_items"));

            l = curInfo.size();
            curInfo.add(TextFormatting.GRAY + I18n.format("entityTracker.info.player.holding"));
            PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, asItemStackArray(player.inventory.mainInventory));
            if (l == curInfo.size()) curInfo.add(I18n.format("gui.misc.no_items"));
        }
    }

    private static ItemStack[] asItemStackArray(NonNullList<ItemStack> stacks) {
        return stacks.stream().filter(stack -> !stack.isEmpty()).toArray(ItemStack[]::new);
    }

    public static class EntityTrackEntryHackable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return HackUpgradeHandler.enabledForPlayer(PneumaticCraftRepressurized.proxy.getClientPlayer());
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, PneumaticCraftRepressurized.proxy.getClientPlayer());
            if (hackable != null) {
                int hackTime = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetsStream()
                        .filter(target -> target.entity == entity)
                        .findFirst()
                        .map(RenderTarget::getHackTime)
                        .orElse(0);
                if (hackTime == 0) {
                    if (isLookingAtTarget) {
                        hackable.addInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                        HackUpgradeHandler.addKeybindTooltip(curInfo);
                    }
                } else {
                    int requiredHackTime = hackable.getHackTime(entity, PneumaticCraftRepressurized.proxy.getClientPlayer());
                    int percentageComplete = hackTime * 100 / requiredHackTime;
                    if (percentageComplete < 100) {
                        curInfo.add(I18n.format("pneumaticHelmet.hacking.hacking", percentageComplete));
                    } else if (hackTime < requiredHackTime + 20) {
                        hackable.addPostHackInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                    } else if (isLookingAtTarget) {
                        hackable.addInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                        HackUpgradeHandler.addKeybindTooltip(curInfo);
                    }
                }
            }
        }
    }
}