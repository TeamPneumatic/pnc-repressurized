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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.CoordinateTrackerOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderNavigator;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.EntityPathNavigateDrone;
import me.desht.pneumaticcraft.common.config.ClientConfig;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoordTrackerHandler;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CoordTrackClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<CoordTrackerHandler> {
//    public static final int SEARCH_RANGE = 150;

    private RenderCoordWireframe coordTracker;
    private RenderNavigator navigator;

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
        super(ArmorUpgradeRegistry.getInstance().coordTrackerHandler);
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
    public void tickClient(ICommonArmorHandler armorHandler) {
        if (coordTracker != null) {
            coordTracker.ticksExisted++;
        } else {
            BlockPos pos = ItemPneumaticArmor.getCoordTrackerPos(ClientUtils.getWornArmor(EquipmentSlotType.HEAD), armorHandler.getPlayer().level);
            if (pos != null) {
                coordTracker = new RenderCoordWireframe(armorHandler.getPlayer().level, pos);
                navigator = new RenderNavigator(coordTracker.pos);
            }
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
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        if (coordTracker != null) {
            if (!Minecraft.getInstance().player.level.dimension().location().equals(coordTracker.worldKey.location()))
                return;
            coordTracker.render(matrixStack, buffer, partialTicks);
            if (ConfigHelper.client().armor.pathEnabled.get() && navigator != null) {
                navigator.render(matrixStack, buffer, ConfigHelper.client().armor.wirePath.get(), ConfigHelper.client().armor.xRayEnabled.get(), partialTicks);
            }
        }
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
        coordTracker = null;
        navigator = null;
    }


    public EnumNavigationResult navigateToSurface(PlayerEntity player) {
        World world = player.level;
        BlockPos navigatingPos = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE, player.blockPosition());
        MobEntity e = PneumaticCraftUtils.createDummyEntity(player);
        Path path = e.getNavigation().createPath(navigatingPos, 0);
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                PathPoint pathPoint = path.getNode(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canSeeSkyFromBelowWater(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.EASY_PATH;
                }
            }
        }
        path = getDronePath(player, navigatingPos);
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                PathPoint pathPoint = path.getNode(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canSeeSkyFromBelowWater(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.DRONE_PATH;
                }
            }
        }
        return EnumNavigationResult.NO_PATH;
    }

    public static Path getDronePath(PlayerEntity player, BlockPos pos) {
        World world = player.level;
        EntityDrone drone = new EntityDrone(ModEntities.DRONE.get(), world);
        drone.setPos(player.getX(), player.getY(), player.getZ());
        return new EntityPathNavigateDrone(drone, world).createPath(pos, 0);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new CoordinateTrackerOptions(screen, this);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            ItemStack helmetStack = event.getPlayer().getItemBySlot(EquipmentSlotType.HEAD);
            if (!event.getWorld().isClientSide || event.getPlayer().getItemBySlot(EquipmentSlotType.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
                return;
            }
            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            if (commonArmorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.COORDINATE_TRACKER) == 0) return;

            CoordTrackClientHandler handler = ArmorUpgradeClientRegistry.getInstance()
                    .getClientHandler(ArmorUpgradeRegistry.getInstance().coordTrackerHandler, CoordTrackClientHandler.class);
            if (handler.isListeningToCoordTrackerSetting) {
                handler.isListeningToCoordTrackerSetting = false;
                if (event.getFace() != null) {
                    handler.reset();
                    GlobalPos gPos = GlobalPosHelper.makeGlobalPos(event.getWorld(), event.getPos().relative(event.getFace()));
                    ItemPneumaticArmor.setCoordTrackerPos(helmetStack, gPos);
                    CompoundNBT tag = new CompoundNBT();
                    tag.put(ItemPneumaticArmor.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
                    NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.HEAD, tag, handler.getCommonHandler().getID()));
                    HUDHandler.getInstance().addMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.selectedTarget", PneumaticCraftUtils.posToString(gPos.pos())), Collections.emptyList(), 60, 0x8000AA00);
                }
            }
        }
    }
}
