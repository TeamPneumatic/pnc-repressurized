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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableEntity;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticHelmetRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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
        manager.registerEntityTrackEntry(EntityTrackEntryMinecart::new);
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
            if (entity instanceof AbstractDroneEntity) {
                droneAIRenderer = new RenderDroneAI((AbstractDroneEntity) entity);
                return true;
            } else {
                return false;
            }
        }

        public RenderDroneAI getDroneAIRenderer() {
            return droneAIRenderer;
        }

        @Override
        public void tick(Entity entity) {
            droneAIRenderer.tick();
        }

        @Override
        public void render(PoseStack matrixStack, MultiBufferSource buffer, Entity entity, float partialTicks) {
            droneAIRenderer.render(matrixStack, buffer, partialTicks);
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            AbstractDroneEntity droneBase = (AbstractDroneEntity) entity;
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.tamed", droneBase.getOwnerName().getString()));
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.routine", droneBase.getLabel()));
            Player player = ClientUtils.getClientPlayer();
            if (DroneDebugClientHandler.enabledForPlayer(player)) {
                Component debugKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindDebuggingDrone);
                if (PneumaticArmorItem.isPlayerDebuggingDrone(player, droneBase)) {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.debugging").withStyle(ChatFormatting.GOLD));
                    Component optionsKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindOpenOptions);
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.debugging.key", optionsKey).withStyle(ChatFormatting.GOLD));
                    if (isLookingAtTarget) {
                        curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.stopDebugging.key", debugKey).withStyle(ChatFormatting.GOLD));
                    }
                } else if (isLookingAtTarget) {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.pressDebugKey", debugKey).withStyle(ChatFormatting.GOLD));
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
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            float pressure = entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                    .map(IAirHandler::getPressure)
                    .orElseThrow(IllegalStateException::new);
            curInfo.add(xlate("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
        }
    }

    public static class EntityTrackEntryLivingBase implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            int healthPercentage = (int) (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth() * 100F);
            curInfo.add(xlate("pneumaticcraft.entityTracker.info.health", healthPercentage));
        }
    }

    public static class EntityTrackEntrySlime implements IEntityTrackEntry {
        private static final String[] MESSAGES = new String[] {
                "pneumaticcraft.entityTracker.info.slimeOther",
                "pneumaticcraft.entityTracker.info.slimeTiny",
                "pneumaticcraft.entityTracker.info.slimeSmall",
                "pneumaticcraft.entityTracker.info.slimeLarge"
        };

        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof Slime;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            int size = ((Slime) entity).getSize();
            if (size >= 1 && size <= 3) {
                curInfo.add(xlate(MESSAGES[size]));
            } else {
                curInfo.add(xlate(MESSAGES[0], size));
            }
        }
    }

    public static class EntityTrackEntryMob implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof Monster;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            Entity target = ((Monster) entity).getTarget();
            if (target != null) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.target", target.getDisplayName().getString()));
            }
        }
    }

    // TODO this doesn't fully work since the client doesn't get the full age data of an entity
    // but is it worth going to the trouble of requesting extra server data?
    public static class EntityTrackEntryAgeable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof AgeableMob;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            int growingAge = ((AgeableMob) entity).getAge();
            if (growingAge > 0) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.canBreedIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(growingAge, false)));
            } else if (growingAge < 0) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.growsUpIn", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(-growingAge, false)));
            } else {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.canBreedNow"));
            }
        }
    }

    public static class EntityTrackEntryTameable implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof TamableAnimal;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            LivingEntity owner = ((TamableAnimal) entity).getOwner();
            if (owner != null) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.tamed", owner.getDisplayName().getString()));
            } else {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.canTame"));
            }
        }
    }

    public static class EntityTrackEntryCreeper implements IEntityTrackEntry {
        private int creeperInFuseTime;

        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof Creeper;
        }

        @Override
        public void tick(Entity entity) {
            if (((Creeper) entity).getSwellDir() == 1) {
                creeperInFuseTime++;
                if (creeperInFuseTime > 30) creeperInFuseTime = 30;
            } else {
                creeperInFuseTime--;
                if (creeperInFuseTime < 0) creeperInFuseTime = 0;
            }
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            if (creeperInFuseTime > 0) {
                if (((Creeper) entity).getSwellDir() == 1) {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.creeper.fuse", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !").withStyle(ChatFormatting.RED));
                } else {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.creeper.coolDown", Math.round((30 - creeperInFuseTime) / 20F * 10F) / 10F + "s !").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }
    }

    public static class EntityTrackEntryPlayer implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof Player;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            Player player = (Player) entity;

            addInventory("pneumaticcraft.entityTracker.info.player.armor", curInfo, player.getInventory().armor);
            addInventory("pneumaticcraft.entityTracker.info.player.holding", curInfo, player.getInventory().items);
        }

        private static void addInventory(String key, List<Component> curInfo, NonNullList<ItemStack> stacks) {
            curInfo.add(xlate(key).withStyle(ChatFormatting.GRAY));
            List<Component> l = PneumaticCraftUtils.summariseItemStacks(new ArrayList<>(), asItemStackArray(stacks));
            if (l.isEmpty()) {
                curInfo.add(xlate("pneumaticcraft.gui.misc.no_items"));
            } else {
                curInfo.addAll(l);
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
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            Player player = ClientUtils.getClientPlayer();
            IHackableEntity hackable = HackManager.getHackableForEntity(entity, player);
            if (hackable != null) {
                int hackTime = ArmorUpgradeClientRegistry.getInstance()
                        .getClientHandler(ArmorUpgradeRegistry.getInstance().entityTrackerHandler, EntityTrackerClientHandler.class)
                        .getTargetsStream()
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
                        curInfo.add(xlate("pneumaticcraft.armor.hacking.hacking", percentageComplete));
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
            return entity instanceof Painting;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            Motive art = ((Painting) entity).motive;

            if (art != null) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.painting.art", art.getRegistryName().getPath()));
            }
        }
    }

    public static class EntityTrackEntryMinecart implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof AbstractMinecart;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            // TODO 1.17 implement an entity syncing protocol (probably as part of a general pneumatic helmet tracker overhaul)
        }
    }

    public static class EntityTrackEntryItemFrame implements IEntityTrackEntry {
        @Override
        public boolean isApplicable(Entity entity) {
            return entity instanceof ItemFrame;
        }

        @Override
        public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
            ItemFrame frame = (ItemFrame) entity;
            ItemStack stack = frame.getItem();

            if (!stack.isEmpty()) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.itemframe.item", stack.getHoverName().getString()));
                if (frame.getRotation() != 0) {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.itemframe.rotation", frame.getRotation() * 45));
                }
            }
        }
    }
}