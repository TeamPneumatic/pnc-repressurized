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
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.CoordinateTrackerOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderNavigator;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ClientConfig;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.drone.EntityPathNavigateDrone;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoordTrackerHandler;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CoordTrackClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<CoordTrackerHandler> {
//    public static final int SEARCH_RANGE = 150;

    //    private RenderCoordWireframe coordTracker;
    private RenderNavigator navigator;
    private BlockPos trackedPos;
    private ResourceKey<Level> worldKey;

    public boolean isListeningToCoordTrackerSetting = false;

    public boolean pathEnabled;
    public boolean wirePath;
    public boolean xRayEnabled;
    public ClientConfig.PathUpdateSetting pathUpdateSetting = ClientConfig.PathUpdateSetting.NORMAL;
    // Timer used to delay the client recalculating a path when it didn't last time. This prevents
    // gigantic lag, as it uses much performance to find a path when it doesn't have anything cached.
    private int pathCalculateCooldown;
    private int noPathCooldown;

    public CoordTrackClientHandler() {
        super(CommonUpgradeHandlers.coordTrackerHandler);
    }

    public enum EnumNavigationResult {
        NO_PATH, EASY_PATH, DRONE_PATH
    }

    @Override
    public void initConfig() {
        pathEnabled = ConfigHelper.client().armor.pathEnabled.get();
        wirePath = ConfigHelper.client().armor.wirePath.get();
        xRayEnabled = ConfigHelper.client().armor.xRayEnabled.get();
        pathUpdateSetting = ConfigHelper.client().armor.pathUpdateSetting.get();
    }

    @Override
    public void saveToConfig() {
        ConfigHelper.updateCoordTracker(pathEnabled, wirePath, xRayEnabled, pathUpdateSetting);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
        if (!isEnabled) return;

        trackedPos = PneumaticArmorItem.getCoordTrackerPos(ClientUtils.getWornArmor(EquipmentSlot.HEAD), armorHandler.getPlayer().level());
        worldKey = armorHandler.getPlayer().level().dimension();
        if (trackedPos != null) {
            navigator = new RenderNavigator(trackedPos);
        }
        if (noPathCooldown > 0) {
            noPathCooldown--;
        }
        if (navigator != null && ConfigHelper.client().armor.pathEnabled.get() && noPathCooldown == 0 && --pathCalculateCooldown <= 0) {
            navigator.updatePath();
            if (!navigator.tracedToDestination()) {
                noPathCooldown = 100; // wait 5 seconds before recalculating a path.
            }
            pathCalculateCooldown = ConfigHelper.client().armor.pathUpdateSetting.get().getTicks(); // == 2 ? 1 : pathUpdateSetting == 1 ? 20 : 100;
        }
    }

    @Override
    public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        if (trackedPos != null && ClientUtils.getClientLevel().dimension().location().equals(worldKey.location())) {
            if (ConfigHelper.client().armor.pathEnabled.get() && navigator != null) {
                navigator.render(matrixStack, buffer, ConfigHelper.client().armor.wirePath.get(), ConfigHelper.client().armor.xRayEnabled.get(), partialTicks);
            }
        }
    }

    @Override
    public void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
        trackedPos = null;
        navigator = null;
    }

    public EnumNavigationResult navigateToSurface(Player player) {
        Level level = player.level();
        BlockPos navigatingPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, player.blockPosition());
        Mob e = PneumaticCraftUtils.createDummyEntity(player);
        Path path = e.getNavigation().createPath(navigatingPos, 0);
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                Node pathPoint = path.getNode(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (level.canSeeSkyFromBelowWater(pathPos)) {
                    trackedPos = pathPos;
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.EASY_PATH;
                }
            }
        }
        path = getDronePath(player, navigatingPos);
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                Node pathPoint = path.getNode(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (level.canSeeSkyFromBelowWater(pathPos)) {
                    trackedPos = pathPos;
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.DRONE_PATH;
                }
            }
        }
        return EnumNavigationResult.NO_PATH;
    }

    public static Path getDronePath(Player player, BlockPos pos) {
        Level level = player.level();
        DroneEntity drone = new DroneEntity(ModEntityTypes.DRONE.get(), level);
        drone.setPos(player.getX(), player.getY(), player.getZ());
        return new EntityPathNavigateDrone(drone, level).createPath(pos, 0);
    }

    public BlockPos getTrackedPos() {
        return trackedPos;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new CoordinateTrackerOptions(screen, this);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            ItemStack helmetStack = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
            if (!event.getLevel().isClientSide || event.getEntity().getItemBySlot(EquipmentSlot.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
                return;
            }
            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            if (commonArmorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.COORDINATE_TRACKER.get()) == 0) return;

            CoordTrackClientHandler handler = ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.coordTrackerHandler, CoordTrackClientHandler.class);
            if (handler.isListeningToCoordTrackerSetting) {
                handler.isListeningToCoordTrackerSetting = false;
                if (event.getFace() != null) {
                    handler.reset();
                    GlobalPos gPos = GlobalPosHelper.makeGlobalPos(event.getLevel(), event.getPos().relative(event.getFace()));
                    PneumaticArmorItem.setCoordTrackerPos(helmetStack, gPos);
                    CompoundTag tag = new CompoundTag();
                    tag.put(PneumaticArmorItem.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
                    NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlot.HEAD, tag, handler.getID()));
                    HUDHandler.getInstance().addMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.selectedTarget", PneumaticCraftUtils.posToString(gPos.pos())), Collections.emptyList(), 60, 0x8000AA00);
                }
            }
        }
    }
}
