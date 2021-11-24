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

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemCamoApplicator;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public enum AreaRenderManager {
    INSTANCE;

    private static final int MAX_DISPLAYED_POS = 15000;

    private final Map<BlockPos, AreaRenderer> showHandlers = new HashMap<>();
    private World world;
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
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.pushPose();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        // tile entity controlled renderers
        for (AreaRenderer handler : showHandlers.values()) {
            handler.render(matrixStack, buffer);
        }

        // some special rendering for certain items
        maybeRenderPositionProvider(matrixStack, buffer, player);
        maybeRenderCamo(matrixStack, buffer, player);
        maybeRenderDroneDebug(matrixStack, buffer, player);
        maybeRenderAreaTool(matrixStack, buffer, player);
        maybeRenderJackhammer(matrixStack, buffer, player);

        matrixStack.popPose();
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        PlayerEntity player = ClientUtils.getClientPlayer();
        if (player != null) {
            if (player.level != world) {
                world = player.level;
                showHandlers.clear();
            } else {
                if (event.phase == TickEvent.Phase.END) {
                    showHandlers.keySet().removeIf(pos -> PneumaticCraftUtils.distBetweenSq(pos, player.blockPosition()) < 1024 && world.isEmptyBlock(pos));
                }
            }
        }
    }

    private void maybeRenderAreaTool(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof ItemGPSAreaTool) {
            // show the raw P1/P2 positions; the area is shown by getHeldPositionProvider()
            BlockPos p1 = ItemGPSAreaTool.getGPSLocation(player.getCommandSenderWorld(), curItem, 0);
            BlockPos p2 = ItemGPSAreaTool.getGPSLocation(player.getCommandSenderWorld(), curItem, 1);
            AreaRenderer.builder().withColor(0x80FF6060).xray().build(p1).render(matrixStack, buffer);
            AreaRenderer.builder().withColor(0x8060FF60).xray().build(p2).render(matrixStack, buffer);
        }
    }

    private void maybeRenderDroneDebug(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlotType.HEAD);
        if (helmet.getItem() == ModItems.PNEUMATIC_HELMET.get()) {
            if (droneDebugger == null) {
                droneDebugger = ArmorUpgradeClientRegistry.getInstance()
                        .getClientHandler(ArmorUpgradeRegistry.getInstance().droneDebugHandler, DroneDebugClientHandler.class);
            }
            Set<BlockPos> posSet = droneDebugger.getShowingPositions();
            Set<BlockPos> areaSet = droneDebugger.getShownArea();
            AreaRenderer.builder().withColor(0x90FF0000).xray().build(posSet).render(matrixStack, buffer);
            AreaRenderer.builder().withColor(0x4040FFA0).xray().build(areaSet).render(matrixStack, buffer);
        }
    }

    private ItemStack getHeldPositionProvider(PlayerEntity player) {
        if (player.getMainHandItem().getItem() instanceof IPositionProvider) {
            return player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof IPositionProvider) {
            return player.getOffhandItem();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void maybeRenderPositionProvider(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        ItemStack curItem = getHeldPositionProvider(player);
        if (curItem.getItem() instanceof IPositionProvider && curItem.hasTag()) {
            int thisHash = curItem.getTag().hashCode();
            if (thisHash != lastItemHashCode) {
                // Position data has changed: recache stored positions
                lastItemHashCode = thisHash;
                IPositionProvider positionProvider = (IPositionProvider) curItem.getItem();
                List<BlockPos> posList = positionProvider.getStoredPositions(player.getCommandSenderWorld(), curItem);
                if (posList.size() > MAX_DISPLAYED_POS) {
                    posList.sort(Comparator.comparingDouble(blockPos -> blockPos.distSqr(player.blockPosition())));
                    player.displayClientMessage(xlate("pneumaticcraft.message.gps_tool.culledRenderArea", posList.size()).withStyle(TextFormatting.GOLD), false);
                }
                Int2ObjectMap<Set<BlockPos>> colorsToPositions = new Int2ObjectOpenHashMap<>();
                int n = Math.min(posList.size(), MAX_DISPLAYED_POS);
                for (int i = 0; i < n; i++) {
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

    private void maybeRenderCamo(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        if (!(player.getMainHandItem().getItem() instanceof ItemCamoApplicator)) {
            return;
        }
        if (lastPlayerPos == null || camoPositionShower == null || player.distanceToSqr(lastPlayerPos.getX(), lastPlayerPos.getY(), lastPlayerPos.getZ()) > 9) {
            lastPlayerPos = player.blockPosition();
            Set<BlockPos> s = Minecraft.getInstance().level.blockEntityList.stream()
                    .filter(te -> te instanceof ICamouflageableTE && te.getBlockPos().distSqr(lastPlayerPos) < 144)
                    .map(TileEntity::getBlockPos)
                    .collect(Collectors.toSet());
            camoPositionShower = AreaRenderer.builder().withColor(0x408080FF).withSize(0.75f).xray().drawShapes().build(s);
        }
        if (camoPositionShower != null) {
            camoPositionShower.render(matrixStack, buffer);
        }
    }

    private void maybeRenderJackhammer(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, PlayerEntity player) {
        if (world == null
                || !(player.getMainHandItem().getItem() instanceof ItemJackHammer)
                || !((Minecraft.getInstance().hitResult) instanceof BlockRayTraceResult)) {
            return;
        }
        ItemJackHammer.DigMode digMode = ItemJackHammer.getDigMode(player.getMainHandItem());
        if (digMode == ItemJackHammer.DigMode.MODE_1X1) return;

        BlockRayTraceResult brtr = (BlockRayTraceResult) Minecraft.getInstance().hitResult;
        if (!world.isAreaLoaded(brtr.getBlockPos(), 1) || world.getBlockState(brtr.getBlockPos()).isAir(world, brtr.getBlockPos())) return;

        if (!lastJackhammerDetails.matches(brtr.getBlockPos(), brtr.getDirection(), digMode)) {
            BlockState state = world.getBlockState(brtr.getBlockPos());
            Set<BlockPos> posSet = world.getBlockEntity(brtr.getBlockPos()) == null && !(state.getBlock() instanceof FlowingFluidBlock) ?
                    ItemJackHammer.getBreakPositions(world, brtr.getBlockPos(), brtr.getDirection(), player.getDirection(), digMode) :
                    Collections.emptySet();
            if (!posSet.isEmpty()) posSet.add(brtr.getBlockPos());
            AreaRenderer.Builder b = AreaRenderer.builder().withColor(0x20FFFFFF).withSize(1.01f).disableWriteMask();
            if (state.getShape(world, brtr.getBlockPos()) != (VoxelShapes.block())) b = b.drawShapes();
            jackhammerPositionShower = b.build(posSet);
            lastJackhammerDetails = new LastJackhammerDetails(brtr.getBlockPos(), brtr.getDirection(), digMode);
        }
        jackhammerPositionShower.render(matrixStack, buffer);
    }


    public AreaRenderer showArea(BlockPos[] area, int color, TileEntity areaShower) {
        return showArea(new HashSet<>(Arrays.asList(area)), color, areaShower);
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, TileEntity areaShower, boolean depth) {
        if (areaShower == null) return null;
        removeHandlers(areaShower);
        AreaRenderer.Builder builder = AreaRenderer.builder().withColor(color);
        if (depth) builder.xray();
        AreaRenderer handler = builder.build(area);
        showHandlers.put(new BlockPos(areaShower.getBlockPos().getX(), areaShower.getBlockPos().getY(), areaShower.getBlockPos().getZ()), handler);
        return handler;
    }

    public AreaRenderer showArea(Set<BlockPos> area, int color, TileEntity areaShower) {
        return showArea(area, color, areaShower, true);
    }

    public boolean isShowing(TileEntity te) {
        return showHandlers.containsKey(new BlockPos(te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ()));
    }

    public void removeHandlers(TileEntity te) {
        showHandlers.remove(new BlockPos(te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ()));
    }

    public void clearPosProviderCache() {
        // called on global variable sync, force a recalc of any cached position provider data
        lastItemHashCode = 0;
    }

    /**
     * Used to determine when the jackhammer preview area needs to be recalculated.
     */
    private static class LastJackhammerDetails {
        private final BlockPos pos;
        private final Direction face;
        private final ItemJackHammer.DigMode digMode;

        private LastJackhammerDetails(BlockPos pos, Direction face, ItemJackHammer.DigMode digMode) {
            this.pos = pos;
            this.face = face;
            this.digMode = digMode;
        }

        private boolean matches(BlockPos pos, Direction face, ItemJackHammer.DigMode digMode) {
            return face == this.face && digMode == this.digMode && pos.equals(this.pos);
        }
    }
}
