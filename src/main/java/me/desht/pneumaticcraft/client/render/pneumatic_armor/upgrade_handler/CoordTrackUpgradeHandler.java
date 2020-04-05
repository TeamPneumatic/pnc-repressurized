package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiCoordinateTrackerOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderCoordWireframe;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderNavigator;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.EntityPathNavigateDrone;
import me.desht.pneumaticcraft.common.config.ClientConfig;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketCoordTrackUpdate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CoordTrackUpgradeHandler implements IUpgradeRenderHandler {
    public static final int SEARCH_RANGE = 150;

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

    public enum EnumNavigationResult {
        NO_PATH, EASY_PATH, DRONE_PATH
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getUpgradeID() {
        return "coordinateTracker";
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
    @OnlyIn(Dist.CLIENT)
    public void update(PlayerEntity player, int rangeUpgrades) {
        if (coordTracker != null) {
            coordTracker.ticksExisted++;
        } else {
            BlockPos pos = ItemPneumaticArmor.getCoordTrackerPos(ClientUtils.getWornArmor(EquipmentSlotType.HEAD), player.world);
            if (pos != null) {
                coordTracker = new RenderCoordWireframe(player.world, pos);
                navigator = new RenderNavigator(coordTracker.world, coordTracker.pos);
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
    @OnlyIn(Dist.CLIENT)
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        if (coordTracker != null) {
            if (Minecraft.getInstance().player.world.getDimension().getType() != coordTracker.world.getDimension().getType())
                return;
            coordTracker.render(matrixStack, buffer, partialTicks);
            if (PNCConfig.Client.Armor.pathEnabled && navigator != null) {
                navigator.render(PNCConfig.Client.Armor.wirePath, PNCConfig.Client.Armor.xRayEnabled, partialTicks);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render2D(float partialTicks, boolean upgradeEnabled) {
    }

    @Override
    public EnumUpgrade[] getRequiredUpgrades() {
        return new EnumUpgrade[] { EnumUpgrade.COORDINATE_TRACKER };
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return PneumaticValues.USAGE_COORD_TRACKER;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reset() {
        coordTracker = null;
        navigator = null;
    }

    @SubscribeEvent
    public boolean onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote && isListeningToCoordTrackerSetting) {
            isListeningToCoordTrackerSetting = false;
            Direction dir = event.getFace();
            if (dir == null) return false;
            reset();
            ItemStack stack = event.getPlayer().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!stack.isEmpty()) {
                GlobalPos gPos = GlobalPos.of(event.getWorld().getDimension().getType(), event.getPos().offset(dir));
                ItemPneumaticArmor.setCoordTrackerPos(stack, gPos);
                NetworkHandler.sendToServer(new PacketCoordTrackUpdate(event.getEntity().world, event.getPos().offset(dir)));
            }
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public EnumNavigationResult navigateToSurface(PlayerEntity player) {
        World world = player.world;
        BlockPos navigatingPos = world.getHeight(Heightmap.Type.WORLD_SURFACE, new BlockPos(player));
        MobEntity e = PneumaticCraftUtils.createDummyEntity(player);
        Path path = e.getNavigator().getPathToPos(navigatingPos, 0);
//        Path path = PneumaticCraftUtils.getPathFinder().findPath(world, PneumaticCraftUtils.createDummyEntity(player), navigatingPos.getX(), navigatingPos.getY(), navigatingPos.getZ(), (float)SEARCH_RANGE);
        if (path != null) {
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                PathPoint pathPoint = path.getPathPointFromIndex(i);
                BlockPos pathPos = new BlockPos(pathPoint.x, pathPoint.y, pathPoint.z);
                if (world.canBlockSeeSky(pathPos)) {
                    coordTracker = new RenderCoordWireframe(world, pathPos);
                    navigator = new RenderNavigator(world, pathPos);
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
                    navigator = new RenderNavigator(world, pathPos);
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
        return new GuiCoordinateTrackerOptions(screen, this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    public WidgetAnimatedStat getAnimatedStat() {
        return null;
    }

}
