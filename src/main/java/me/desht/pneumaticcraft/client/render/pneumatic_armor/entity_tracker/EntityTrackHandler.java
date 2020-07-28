package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EntityTrackHandler {
    // list of track entry supplier & reference track entry, which is only used to call .isApplicable()
    private static final List<Pair<Supplier<? extends IEntityTrackEntry>, IEntityTrackEntry>> trackEntries = new ArrayList<>();

    public static void registerDefaultEntries() {
        IPneumaticHelmetRegistry manager = PneumaticRegistry.getInstance().getHelmetRegistry();

        manager.registerEntityTrackEntry(EntityTrackEntryLivingBase::new);
        manager.registerEntityTrackEntry(EntityTrackEntryHackable::new);
        manager.registerEntityTrackEntry(EntityTrackEntryDrone::new);
        manager.registerEntityTrackEntry(EntityTrackEntryPressurizable::new);
        manager.registerEntityTrackEntry(EntityTrackEntryAgeable::new);
        manager.registerEntityTrackEntry(EntityTrackEntryTameable::new);
        manager.registerEntityTrackEntry(EntityTrackEntryCreeper::new);
        manager.registerEntityTrackEntry(EntityTrackEntrySlime::new);
        manager.registerEntityTrackEntry(EntityTrackEntryPlayer::new);
        manager.registerEntityTrackEntry(EntityTrackEntryMob::new);
        manager.registerEntityTrackEntry(EntityTrackEntryItemFrame::new);
        manager.registerEntityTrackEntry(EntityTrackEntryPainting::new);
    }

    public static void init() {
        for (Supplier<? extends IEntityTrackEntry> sup : PneumaticHelmetRegistry.getInstance().entityTrackEntries) {
            trackEntries.add(Pair.of(sup, sup.get()));
        }
    }

    public static List<IEntityTrackEntry> getTrackersForEntity(Entity entity) {
        List<IEntityTrackEntry> trackers = new ArrayList<>();
        for (Pair<Supplier<? extends IEntityTrackEntry>, IEntityTrackEntry> pair : trackEntries) {
            if (pair.getRight().isApplicable(entity)) {
                IEntityTrackEntry newTracker = pair.getLeft().get();
                newTracker.isApplicable(entity); // just as an initializer.
                trackers.add(newTracker);
            }
        }
        return trackers;
    }

    public static class EntityTrackEntryDrone implements IEntityTrackEntry {
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
        public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, Entity entity, float partialTicks) {
            droneAIRenderer.render(matrixStack, buffer, partialTicks);
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.tamed", ((EntityDrone) entity).ownerName));
            curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.drone.routine", ((EntityDrone) entity).getLabel()));
            PlayerEntity player = ClientUtils.getClientPlayer();
            if (DroneDebugClientHandler.enabledForPlayer(player)) {
                String debugKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindDebuggingDrone);
                if (ItemPneumaticArmor.isPlayerDebuggingEntity(player, entity)) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("pneumaticcraft.entityTracker.info.drone.debugging"));
                    String optionsKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindOpenOptions);
                    curInfo.add(TextFormatting.GOLD + I18n.format("pneumaticcraft.entityTracker.info.drone.debugging.key", optionsKey));
                    if (isLookingAtTarget) {
                        curInfo.add(TextFormatting.GOLD + I18n.format("pneumaticcraft.entityTracker.info.drone.stopDebugging.key", debugKey));
                    }
                } else if (isLookingAtTarget) {
                    curInfo.add(TextFormatting.GOLD + I18n.format("pneumaticcraft.entityTracker.info.drone.pressDebugKey", debugKey));
                }
            }
        }
    }

    public static class EntityTrackEntryPressurizable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY).isPresent();
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            float pressure = entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .map(IAirHandler::getPressure)
                    .orElseThrow(IllegalStateException::new);
            curInfo.add(I18n.format("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
        }
    }

    public static class EntityTrackEntryLivingBase implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int healthPercentage = (int) (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth() * 100F);
            curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.health", healthPercentage));
        }
    }

    public static class EntityTrackEntrySlime implements IEntityTrackEntry {
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

    public static class EntityTrackEntryMob implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof MonsterEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            Entity target = ((MonsterEntity) entity).getAttackTarget();
            if (target != null) {
                curInfo.add("Target: " + target.getDisplayName().getString());
            }
        }
    }

    // TODO this doesn't fully work since the client doesn't get the full age data of an entity
    // but is it worth going to the trouble of requesting extra server data?
    public static class EntityTrackEntryAgeable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof AgeableEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            int growingAge = ((AgeableEntity) entity).getGrowingAge();
            if (growingAge > 0) {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.canBreedIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false)));
            } else if (growingAge < 0) {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.growsUpIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false)));
            } else {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.canBreedNow"));
            }
        }
    }

    public static class EntityTrackEntryTameable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof TameableEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            LivingEntity owner = ((TameableEntity) entity).getOwner();
            if (owner != null) {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.tamed", owner.getDisplayName().getString()));
            } else {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.canTame"));
            }
        }
    }

    public static class EntityTrackEntryCreeper implements IEntityTrackEntry {
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
                    curInfo.add(TextFormatting.RED + I18n.format("pneumaticcraft.entityTracker.info.creeper.fuse", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !"));
                } else {
                    curInfo.add(TextFormatting.DARK_GREEN + I18n.format("pneumaticcraft.entityTracker.info.creeper.coolDown", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !"));
                }
            }
        }
    }

    public static class EntityTrackEntryPlayer implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof PlayerEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            PlayerEntity player = (PlayerEntity) entity;

            addInventory("pneumaticcraft.entityTracker.info.player.armor", curInfo, player.inventory.armorInventory);
            addInventory("pneumaticcraft.entityTracker.info.player.holding", curInfo, player.inventory.mainInventory);
        }

        private static void addInventory(String title, List<String> curInfo, NonNullList<ItemStack> stacks) {
            curInfo.add(TextFormatting.GRAY + I18n.format(title));
            List<ITextComponent> l = new ArrayList<>();
            PneumaticCraftUtils.sortCombineItemStacksAndToString(l, asItemStackArray(stacks));
            if (l.isEmpty()) {
                curInfo.add(I18n.format("pneumaticcraft.gui.misc.no_items"));
            } else {
                curInfo.addAll(l.stream().map(ITextComponent::getString).collect(Collectors.toList()));
            }
        }
    }

    private static ItemStack[] asItemStackArray(NonNullList<ItemStack> stacks) {
        return stacks.stream().filter(stack -> !stack.isEmpty()).toArray(ItemStack[]::new);
    }

    public static class EntityTrackEntryHackable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return HackClientHandler.enabledForPlayer(ClientUtils.getClientPlayer());
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            PlayerEntity player = ClientUtils.getClientPlayer();
            IHackableEntity hackable = HackableHandler.getHackableForEntity(entity, player);
            if (hackable != null) {
                int hackTime = HUDHandler.getInstance().getSpecificRenderer(EntityTrackerClientHandler.class).getTargetsStream()
                        .filter(target -> target.entity == entity)
                        .findFirst()
                        .map(RenderEntityTarget::getHackTime)
                        .orElse(0);
                if (hackTime == 0) {
                    if (isLookingAtTarget) {
                        hackable.addHackInfo(entity, curInfo, player);
                        HackClientHandler.addKeybindTooltip(curInfo);
                    }
                } else {
                    int requiredHackTime = hackable.getHackTime(entity, player);
                    int percentageComplete = hackTime * 100 / requiredHackTime;
                    if (percentageComplete < 100) {
                        curInfo.add(I18n.format("pneumaticcraft.armor.hacking.hacking", percentageComplete));
                    } else if (hackTime < requiredHackTime + 20) {
                        hackable.addPostHackInfo(entity, curInfo, player);
                    } else if (isLookingAtTarget) {
                        hackable.addHackInfo(entity, curInfo, player);
                        HackClientHandler.addKeybindTooltip(curInfo);
                    }
                }
            }
        }
    }

    public static class EntityTrackEntryPainting implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof PaintingEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            PaintingType art = ((PaintingEntity) entity).art;

            if (art != null) {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.painting.art", art.getRegistryName().getPath()));
            }
        }
    }

    public static class EntityTrackEntryItemFrame implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof ItemFrameEntity;
        }

        @Override
        public void addInfo(Entity entity, List<String> curInfo, boolean isLookingAtTarget) {
            ItemFrameEntity frame = (ItemFrameEntity) entity;
            ItemStack stack = frame.getDisplayedItem();

            if (!stack.isEmpty()) {
                curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.itemframe.item", stack.getDisplayName().getString()));
                if (frame.getRotation() != 0) {
                    curInfo.add(I18n.format("pneumaticcraft.entityTracker.info.itemframe.rotation", frame.getRotation() * 45));
                }
            }
        }
    }
}