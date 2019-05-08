package me.desht.pneumaticcraft.client;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;

public class AreaShowManager {
    private static final AreaShowManager INSTANCE = new AreaShowManager();
    private final Map<BlockPos, AreaShowHandler> showHandlers = new HashMap<>();
    private World world;
    private DroneDebugUpgradeHandler droneDebugger;

    private List<BlockPos> cachedPositionProviderData;
    private List<AreaShowHandler> cachedPositionProviderShowers;
    private AreaShowHandler camoPositionShower;
    private BlockPos lastPlayerPos;

    public static AreaShowManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        maybeRenderPositionProvider(player);
        maybeRenderCamo(player);

        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.getItem() == Itemss.PNEUMATIC_HELMET) {
            if (droneDebugger == null)
                droneDebugger = HUDHandler.instance().getSpecificRenderer(DroneDebugUpgradeHandler.class);
            Set<BlockPos> set = droneDebugger.getShowingPositions();
            new AreaShowHandler(set, 0x90FF0000, true).render();
            Set<BlockPos> areaSet = droneDebugger.getShownArea();
            new AreaShowHandler(areaSet, 0x4040FFA0, true).render();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void maybeRenderPositionProvider(EntityPlayer player) {
        ItemStack curItem = player.getHeldItemMainhand();
        if (curItem.getItem() instanceof IPositionProvider) {
            IPositionProvider positionProvider = (IPositionProvider) curItem.getItem();
            List<BlockPos> posList = positionProvider.getStoredPositions(curItem);
            if (posList != null) {
                if (!posList.equals(cachedPositionProviderData)) { //Cache miss
                    TIntObjectMap<Set<BlockPos>> colorsToPositions = new TIntObjectHashMap<>();
                    for (int i = 0; i < posList.size(); i++) {
                        int renderColor = positionProvider.getRenderColor(i);
                        if (posList.get(i) != null && renderColor != 0) {
                            Set<BlockPos> positionsForColor = colorsToPositions.get(renderColor);
                            if (positionsForColor == null) {
                                positionsForColor = new HashSet<>();
                                colorsToPositions.put(renderColor, positionsForColor);
                            }
                            positionsForColor.add(posList.get(i));
                        }
                    }
                    cachedPositionProviderData = posList;
                    cachedPositionProviderShowers = new ArrayList<>(colorsToPositions.size());
                    colorsToPositions.forEachEntry((color, positions) -> {
                        cachedPositionProviderShowers.add(new AreaShowHandler(positions, color, positionProvider.disableDepthTest()));
                        return true;
                    });
                }

                cachedPositionProviderShowers.forEach(AreaShowHandler::render);
            }
        }
    }

    private void maybeRenderCamo(EntityPlayer player) {
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemCamoApplicator)) {
            return;
        }
        if (lastPlayerPos == null || camoPositionShower == null || player.getDistanceSq(lastPlayerPos) > 9) {
            lastPlayerPos = player.getPosition();
            Set<BlockPos> s = Minecraft.getMinecraft().world.loadedTileEntityList.stream()
                    .filter(te -> te instanceof ICamouflageableTE && te.getPos().distanceSq(player.posX, player.posY, player.posZ) < 144)
                    .map(TileEntity::getPos)
                    .collect(Collectors.toSet());
            camoPositionShower = new AreaShowHandler(s, 0x2080FFFF, 0.75, true);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render();
        }
    }

    public AreaShowHandler showArea(BlockPos[] area, int color, TileEntity areaShower) {
        return showArea(new HashSet<>(Arrays.asList(area)), color, areaShower);
    }

    public AreaShowHandler showArea(Set<BlockPos> area, int color, TileEntity areaShower) {
        if (areaShower == null) return null;
        removeHandlers(areaShower);
        AreaShowHandler handler = new AreaShowHandler(area, color, false);
        showHandlers.put(new BlockPos(areaShower.getPos().getX(), areaShower.getPos().getY(), areaShower.getPos().getZ()), handler);
        return handler;
    }

    public boolean isShowing(TileEntity te) {
        return showHandlers.containsKey(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    public void removeHandlers(TileEntity te) {
        showHandlers.remove(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        EntityPlayer player = PneumaticCraftRepressurized.proxy.getClientPlayer();
        if (player != null) {
            if (player.world != world) {
                world = player.world;
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> PneumaticCraftUtils.distBetween(pos, player.posX, player.posY, player.posZ) < 32 && world.isAirBlock(pos));
                }
            }
        }
    }
}
