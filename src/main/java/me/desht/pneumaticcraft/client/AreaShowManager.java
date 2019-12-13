package me.desht.pneumaticcraft.client;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        GlStateManager.pushMatrix();
        GlStateManager.translated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);

        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (AreaShowHandler handler : showHandlers.values()) {
            handler.render();
        }

        maybeRenderPositionProvider(player);
        maybeRenderCamo(player);

        ItemStack helmet = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET) {
            if (droneDebugger == null)
                droneDebugger = HUDHandler.instance().getSpecificRenderer(DroneDebugUpgradeHandler.class);
            Set<BlockPos> set = droneDebugger.getShowingPositions();
            new AreaShowHandler(set, 0x90FF0000, true).render();
            Set<BlockPos> areaSet = droneDebugger.getShownArea();
            new AreaShowHandler(areaSet, 0x4040FFA0, true).render();
        }

        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private ItemStack getHeldPositionProvider(PlayerEntity player) {
        if (player.getHeldItemMainhand().getItem() instanceof IPositionProvider) {
            return player.getHeldItemMainhand();
        } else if (player.getHeldItemOffhand().getItem() instanceof IPositionProvider) {
            return player.getHeldItemOffhand();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void maybeRenderPositionProvider(PlayerEntity player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof IPositionProvider) {
            IPositionProvider positionProvider = (IPositionProvider) curItem.getItem();
            List<BlockPos> posList = positionProvider.getStoredPositions(curItem);
            if (posList != null) {
                if (!posList.equals(cachedPositionProviderData)) { //Cache miss
                    Int2ObjectMap<Set<BlockPos>> colorsToPositions = new Int2ObjectOpenHashMap<>();
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
                    colorsToPositions.int2ObjectEntrySet().forEach((entry) -> cachedPositionProviderShowers.add(new AreaShowHandler(entry.getValue(), entry.getIntKey(), positionProvider.disableDepthTest())));
                }

                cachedPositionProviderShowers.forEach(AreaShowHandler::render);
            }
        }
    }

    private void maybeRenderCamo(PlayerEntity player) {
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemCamoApplicator)) {
            return;
        }
        if (lastPlayerPos == null || camoPositionShower == null || player.getDistanceSq(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.getPosition();
            Set<BlockPos> s = Minecraft.getInstance().world.loadedTileEntityList.stream()
                    .filter(te -> te instanceof ICamouflageableTE && te.getPos().distanceSq(lastPlayerPos) < 144)
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
        PlayerEntity player = PneumaticCraftRepressurized.proxy.getClientPlayer();
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
