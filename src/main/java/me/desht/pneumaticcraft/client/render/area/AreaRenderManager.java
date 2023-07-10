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

package me.desht.pneumaticcraft.client.render.area;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.CoordTrackClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.CamoApplicatorItem;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.item.JackHammerItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoordTrackerHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum AreaRenderManager {
    INSTANCE;

    private static final int MAX_DISPLAYED_POS = 15000;

    private final Map<BlockPos, AreaRenderer> showHandlers = new HashMap<>();
    private Level level;
    private DroneDebugClientHandler droneDebugger;

    private List<AreaRenderer> cachedPositionProviderShowers;
    private AreaRenderer camoPositionShower;
    private AreaRenderer jackhammerPositionShower;
    private LastJackhammerDetails lastJackhammerDetails = new LastJackhammerDetails(BlockPos.ZERO, null, null);
    private BlockPos lastPlayerPos;
    private int lastItemHashCode = 0;

    public static AreaRenderManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Player player = ClientUtils.getClientPlayer();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack matrixStack = event.getPoseStack();

        matrixStack.pushPose();

        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        // block entity controlled renderers
        for (AreaRenderer handler : showHandlers.values()) {
            handler.render(matrixStack, buffer);
        }

        // some special rendering for certain items
        maybeRenderPositionProvider(matrixStack, buffer, player);
        maybeRenderCamo(matrixStack, buffer, player);
        maybeRenderDroneDebug(matrixStack, buffer, player);
        maybeRenderAreaTool(matrixStack, buffer, player);
        maybeRenderJackhammer(matrixStack, buffer, player);
        maybeRenderCoordinateTracker(matrixStack, buffer, player, event.getPartialTick());

        matrixStack.popPose();
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.level() != level) {
                level = player.level();
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> PneumaticCraftUtils.distBetweenSq(pos, player.blockPosition()) < 1024 && level.isEmptyBlock(pos));
                }
            }
        }
    }

    private void maybeRenderCoordinateTracker(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player, float partialTicks) {
        CoordTrackerHandler handler = CommonUpgradeHandlers.coordTrackerHandler;
        if (CommonArmorHandler.getHandlerForPlayer().upgradeUsable(handler, true)) {
            BlockPos pos = ClientArmorRegistry.getInstance().getClientHandler(handler, CoordTrackClientHandler.class).getTrackedPos();
            if (pos != null) {
                float progress = (player.level().getGameTime() % 20 + partialTicks) / 20;
                float g = progress < 0.5F ? progress + 0.5F : 1.5F - progress;
                int col = 0xA00000FF | (int)(g * 255) << 8;
                Vec3 targetVec = Vec3.atCenterOf(pos);
                float size = ClientUtils.calculateViewScaling(targetVec);
                float textSize = size * 0.02f;
                matrixStack.pushPose();
                matrixStack.translate(targetVec.x(), targetVec.y(), targetVec.z());
                matrixStack.scale(textSize, textSize, textSize);
                RenderUtils.rotateToPlayerFacing(matrixStack);
                RenderUtils.renderString3d(Component.literal(PneumaticCraftUtils.posToString(pos)), 0, 0, 0xFFFFFFFF, matrixStack, buffer, true, true);
                matrixStack.popPose();
                AreaRenderer.builder().withColor(col).xray().withSize(size / 2f).build(pos).render(matrixStack, buffer);
            }
        }
    }

    private void maybeRenderAreaTool(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof GPSAreaToolItem) {
            // show the raw P1/P2 positions; the area is shown by getHeldPositionProvider()
            GPSAreaToolItem.getGPSLocation(player, curItem, 0)
                    .ifPresent(pos -> AreaRenderer.builder().withColor(0x80FF6060).xray().build(pos).render(matrixStack, buffer));
            GPSAreaToolItem.getGPSLocation(player, curItem, 1)
                    .ifPresent(pos -> AreaRenderer.builder().withColor(0x8060FF60).xray().build(pos).render(matrixStack, buffer));
        }
    }

    private void maybeRenderDroneDebug(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get()) {
            if (droneDebugger == null) {
                droneDebugger = ClientArmorRegistry.getInstance()
                        .getClientHandler(CommonUpgradeHandlers.droneDebugHandler, DroneDebugClientHandler.class);
            }
            Set<BlockPos> posSet = droneDebugger.getShowingPositions();
            Set<BlockPos> areaSet = droneDebugger.getShownArea();
            AreaRenderer.builder().withColor(0x90FF0000).xray().build(posSet).render(matrixStack, buffer);
            AreaRenderer.builder().withColor(0x4040FFA0).xray().build(areaSet).render(matrixStack, buffer);
        }
    }

    private ItemStack getHeldPositionProvider(Player player) {
        if (player.getMainHandItem().getItem() instanceof IPositionProvider) {
            return player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof IPositionProvider) {
            return player.getOffhandItem();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void maybeRenderPositionProvider(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof IPositionProvider positionProvider && curItem.hasTag()) {
            int thisHash = Objects.requireNonNull(curItem.getTag()).hashCode();
            if (thisHash != lastItemHashCode) {
                // Position data has changed: recache stored positions
                lastItemHashCode = thisHash;
                List<BlockPos> posList = positionProvider.getStoredPositions(player.getUUID(), curItem);
                if (posList.size() > MAX_DISPLAYED_POS) {
                    posList.sort(Comparator.comparingDouble(blockPos -> blockPos.distSqr(player.blockPosition())));
                    player.displayClientMessage(xlate("pneumaticcraft.message.gps_tool.culledRenderArea", posList.size()).withStyle(ChatFormatting.GOLD), false);
                }
                Int2ObjectMap<Set<BlockPos>> colorsToPositions = new Int2ObjectOpenHashMap<>();
                int n = Math.min(posList.size(), MAX_DISPLAYED_POS);
                for (int i = 0; i < n; i++) {
                    int renderColor = positionProvider.getRenderColor(i);
                    if (posList.get(i) != null && renderColor != 0) {
                        colorsToPositions.computeIfAbsent(renderColor, k -> new HashSet<>()).add(posList.get(i));
                    }
                }
                cachedPositionProviderShowers = new ArrayList<>(colorsToPositions.size());
                colorsToPositions.int2ObjectEntrySet().forEach((entry) -> {
                            AreaRenderer.Builder builder = AreaRenderer.builder().withColor(entry.getIntKey());
                            if (positionProvider.disableDepthTest()) builder.xray();
                            cachedPositionProviderShowers.add(builder.build(entry.getValue()));
                        }
                );
            }

            cachedPositionProviderShowers.forEach(renderer -> renderer.render(matrixStack, buffer));
        }
    }

    private void maybeRenderCamo(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        if (!(player.getMainHandItem().getItem() instanceof CamoApplicatorItem)) {
            return;
        }
        if (lastPlayerPos == null || camoPositionShower == null || player.distanceToSqr(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.blockPosition();
            Set<BlockPos> s = getNearbyBlockEntities().stream()
                    .filter(te -> te instanceof CamouflageableBlockEntity && te.getBlockPos().distSqr(lastPlayerPos) < 144)
                    .map(BlockEntity::getBlockPos)
                    .collect(Collectors.toSet());
            camoPositionShower = AreaRenderer.builder().withColor(0x408080FF).withSize(0.75f).xray().drawShapes().build(s);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render(matrixStack, buffer);
        }
    }

    private Collection<BlockEntity> getNearbyBlockEntities() {
        List<BlockEntity> res = new ArrayList<>();
        BlockPos pos = ClientUtils.getClientPlayer().blockPosition();
        for (int x = pos.getX() - 16; x <= pos.getX() + 16; x += 16) {
            for (int z = pos.getZ() - 16; z <= pos.getZ() + 16; z += 16) {
                ChunkPos cp = new ChunkPos(pos);
                res.addAll(ClientUtils.getClientLevel().getChunk(cp.x, cp.z).getBlockEntities().values());
            }
        }
        return res;
    }

    private void maybeRenderJackhammer(PoseStack matrixStack, MultiBufferSource.BufferSource buffer, Player player) {
        if (level == null
                || !(player.getMainHandItem().getItem() instanceof JackHammerItem)
                || !((Minecraft.getInstance().hitResult) instanceof BlockHitResult)) {
            return;
        }
        JackHammerItem.DigMode digMode = JackHammerItem.getDigMode(player.getMainHandItem());
        if (digMode == JackHammerItem.DigMode.MODE_1X1) return;

        BlockHitResult brtr = (BlockHitResult) Minecraft.getInstance().hitResult;
        if (!level.isLoaded(brtr.getBlockPos()) || level.getBlockState(brtr.getBlockPos()).isAir()) return;

        if (!lastJackhammerDetails.matches(brtr.getBlockPos(), brtr.getDirection(), digMode)) {
            BlockState state = level.getBlockState(brtr.getBlockPos());
            Set<BlockPos> posSet = level.getBlockEntity(brtr.getBlockPos()) == null && !(state.getBlock() instanceof LiquidBlock) ?
                    JackHammerItem.getBreakPositions(level, brtr.getBlockPos(), brtr.getDirection(), player.getDirection(), digMode) :
                    Collections.emptySet();
            if (!posSet.isEmpty()) posSet.add(brtr.getBlockPos());
            AreaRenderer.Builder b = AreaRenderer.builder().withColor(0x20FFFFFF).withSize(1.01f).disableWriteMask();
            if (state.getShape(level, brtr.getBlockPos()) != (Shapes.block())) b = b.drawShapes();
            jackhammerPositionShower = b.build(posSet);
            lastJackhammerDetails = new LastJackhammerDetails(brtr.getBlockPos(), brtr.getDirection(), digMode);
        }
        jackhammerPositionShower.render(matrixStack, buffer);
    }


    public AreaRenderer showArea(BlockPos[] area, int color, BlockEntity areaShower) {
        return showArea(new HashSet<>(Arrays.asList(area)), color, areaShower);
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, BlockEntity areaShower, boolean depth) {
        if (areaShower == null) return null;
        removeHandlers(areaShower);
        AreaRenderer.Builder builder = AreaRenderer.builder().withColor(color);
        if (depth) builder.xray();
        AreaRenderer handler = builder.build(area);
        showHandlers.put(new BlockPos(areaShower.getBlockPos().getX(), areaShower.getBlockPos().getY(), areaShower.getBlockPos().getZ()), handler);
        return handler;
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, BlockEntity areaShower) {
        return showArea(area, color, areaShower, true);
    }

    public boolean isShowing(BlockEntity te) {
        return showHandlers.containsKey(new BlockPos(te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ()));
    }

    public void removeHandlers(BlockEntity te) {
        showHandlers.remove(new BlockPos(te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ()));
    }

    public void clearPosProviderCache() {
        // called on global variable sync, force a recalc of any cached position provider data
        lastItemHashCode = 0;
    }

    /**
     * Used to determine when the jackhammer preview area needs to be recalculated.
     */
    private record LastJackhammerDetails(BlockPos pos, Direction face,
                                         JackHammerItem.DigMode digMode) {

        private boolean matches(BlockPos pos, Direction face, JackHammerItem.DigMode digMode) {
            return face == this.face && digMode == this.digMode && pos.equals(this.pos);
        }
    }
}
