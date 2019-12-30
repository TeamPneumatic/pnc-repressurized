package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry.EntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
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
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        manager.registerEntityTrackEntry(EntityTrackEntryItemFrame.class);
        manager.registerEntityTrackEntry(EntityTrackEntryPainting.class);
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
            PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
            if (DroneDebugUpgradeHandler.enabledForPlayer(player)) {
                if (NBTUtil.getInteger(player.getItemStackFromSlot(EquipmentSlotType.HEAD), NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE) == entity.getEntityId()) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.debugging"));
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.debugging.key",
                            KeyHandler.getInstance().keybindOpenOptions.getLocalizedName()));
                } else if (isLookingAtTarget) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("entityTracker.info.drone.pressDebugKey",
                            KeyHandler.getInstance().keybindDebuggingDrone.getLocalizedName()));
                }
            }
        }
    }

    public static class EntityTrackEntryPressurizable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY).isPresent();
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            float pressure = entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .map(IAirHandler::getPressure)
                    .orElseThrow(IllegalStateException::new);
            curInfo.add(I18n.format("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
        }
    }

    public static class EntityTrackEntryLivingBase extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int healthPercentage = (int) (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth() * 100F);
            curInfo.add(I18n.format("entityTracker.info.health", healthPercentage));
        }
    }

    public static class EntityTrackEntrySlime extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof SlimeEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            switch (((SlimeEntity) entity).getSlimeSize()) {
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
                    curInfo.add("Size: " + ((SlimeEntity) entity).getSlimeSize());
                    break;
            }
        }
    }

    public static class EntityTrackEntryMob extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof MonsterEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            Entity target = ((MonsterEntity) entity).getAttackTarget();
            if (target != null) {
                curInfo.add("Target: " + target.getDisplayName().getFormattedText());
            }
        }
    }

    // TODO this doesn't fully work since the client doesn't get the full age data of an entity
    // but is it worth going to the trouble of requesting extra server data?
    public static class EntityTrackEntryAgeable extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof AgeableEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int growingAge = ((AgeableEntity) entity).getGrowingAge();
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
            return entity instanceof TameableEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            LivingEntity owner = ((TameableEntity) entity).getOwner();
            if (owner != null) {
                curInfo.add(I18n.format("entityTracker.info.tamed", owner.getDisplayName().getFormattedText()));
            } else {
                curInfo.add(I18n.format("entityTracker.info.canTame"));
            }
        }
    }

    public static class EntityTrackEntryCreeper extends EntityTrackEntry {
        private int creeperInFuseTime;

        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof CreeperEntity;
        }

        @Override
        public void update(Entity entity) {
            if (((CreeperEntity) entity).getCreeperState() == 1) {
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
                if (((CreeperEntity) entity).getCreeperState() == 1) {
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
            return entity instanceof PlayerEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            PlayerEntity player = (PlayerEntity) entity;

            addInventory("entityTracker.info.player.armor", curInfo, player.inventory.armorInventory);
            addInventory("entityTracker.info.player.holding", curInfo, player.inventory.mainInventory);
        }

        private static void addInventory(String title, List<String> curInfo, NonNullList<ItemStack> stacks) {
            curInfo.add(TextFormatting.GRAY + I18n.format(title));
            List<ITextComponent> l = new ArrayList<>();
            PneumaticCraftUtils.sortCombineItemStacksAndToString(l, asItemStackArray(stacks));
            if (l.isEmpty()) {
                curInfo.add(I18n.format("gui.misc.no_items"));
            } else {
                curInfo.addAll(l.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));
            }
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
                        .map(RenderEntityTarget::getHackTime)
                        .orElse(0);
                if (hackTime == 0) {
                    if (isLookingAtTarget) {
                        hackable.addHackInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
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
                        hackable.addHackInfo(entity, curInfo, PneumaticCraftRepressurized.proxy.getClientPlayer());
                        HackUpgradeHandler.addKeybindTooltip(curInfo);
                    }
                }
            }
        }
    }

    public static class EntityTrackEntryPainting extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof PaintingEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            PaintingType art = ((PaintingEntity) entity).art;

            if (art != null) {
                curInfo.add(I18n.format("entityTracker.info.painting.art", art.getRegistryName().getPath()));
            }
        }
    }

    public static class EntityTrackEntryItemFrame extends EntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof ItemFrameEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            ItemFrameEntity frame = (ItemFrameEntity) entity;
            ItemStack stack = frame.getDisplayedItem();

            if (!stack.isEmpty()) {
                curInfo.add(I18n.format("entityTracker.info.itemframe.item", stack.getDisplayName()));
                if (frame.getRotation() != 0) {
                    curInfo.add(I18n.format("entityTracker.info.itemframe.rotation", frame.getRotation() * 45));
                }
            }
        }
    }
}