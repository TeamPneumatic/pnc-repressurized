package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
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
import me.desht.pneumaticcraft.common.config.PNCConfig;
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
import me.desht.pneumaticcraft.lib.Names;
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
        pathEnabled = PNCConfig.Client.Armor.pathEnabled;
        wirePath = PNCConfig.Client.Armor.wirePath;
        xRayEnabled = PNCConfig.Client.Armor.xRayEnabled;
        pathUpdateSetting = PNCConfig.Client.Armor.pathUpdateSetting;
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
            BlockPos pos = ItemPneumaticArmor.getCoordTrackerPos(ClientUtils.getWornArmor(EquipmentSlotType.HEAD), armorHandler.getPlayer().world);
            if (pos != null) {
                coordTracker = new RenderCoordWireframe(armorHandler.getPlayer().world, pos);
                navigator = new RenderNavigator(coordTracker.pos);
            }
        }
        if (noPathCooldown > 0) {
            noPathCooldown--;
        }
        if (navigator != null && PNCConfig.Client.Armor.pathEnabled && noPathCooldown == 0 && --pathCalculateCooldown <= 0) {
            navigator.updatePath();
            if (!navigator.tracedToDestination()) {
                noPathCooldown = 100; // wait 5 seconds before recalculating a path.
            }
            pathCalculateCooldown = PNCConfig.Client.Armor.pathUpdateSetting.getTicks(); // == 2 ? 1 : pathUpdateSetting == 1 ? 20 : 100;
        }
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        if (coordTracker != null) {
            if (!Minecraft.getInstance().player.world.getDimensionKey().getLocation().equals(coordTracker.worldKey.getLocation()))
                return;
            coordTracker.render(matrixStack, buffer, partialTicks);
            if (PNCConfig.Client.Armor.pathEnabled && navigator != null) {
                navigator.render(matrixStack, buffer, PNCConfig.Client.Armor.wirePath, PNCConfig.Client.Armor.xRayEnabled, partialTicks);
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
        World world = player.world;
        BlockPos navigatingPos = world.getHeight(Heightmap.Type.WORLD_SURFACE, player.getPosition());
        MobEntity e = PneumaticCraftUtils.createDummyEntity(player);
        Path path = e.getNavigator().getPathToPos(navigatingPos, 0);
        if (path != null) {
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canBlockSeeSky(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.EASY_PATH;
                }
            }
        }
        path = getDronePath(player, navigatingPos);
        if (path != null) {
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canBlockSeeSky(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(pathPos);
                    return EnumNavigationResult.DRONE_PATH;
                }
            }
        }
        return EnumNavigationResult.NO_PATH;
    }

    public static Path getDronePath(PlayerEntity player, BlockPos pos) {
        World world = player.world;
        EntityDrone drone = new EntityDrone(ModEntities.DRONE.get(), world);
        drone.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
        return new EntityPathNavigateDrone(drone, world).getPathToPos(pos, 0);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new CoordinateTrackerOptions(screen, this);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            ItemStack helmetStack = event.getPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!event.getWorld().isRemote || event.getPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
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
                    GlobalPos gPos = GlobalPosHelper.makeGlobalPos(event.getWorld(), event.getPos().offset(event.getFace()));
                    ItemPneumaticArmor.setCoordTrackerPos(helmetStack, gPos);
                    CompoundNBT tag = new CompoundNBT();
                    tag.put(ItemPneumaticArmor.NBT_COORD_TRACKER, GlobalPosHelper.toNBT(gPos));
                    NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.HEAD, tag, handler.getCommonHandler().getID()));
                    HUDHandler.getInstance().addMessage(xlate("pneumaticcraft.armor.gui.coordinateTracker.selectedTarget", PneumaticCraftUtils.posToString(gPos.getPos())), Collections.emptyList(), 60, 0x8000AA00);
                }
            }
        }
    }
}
