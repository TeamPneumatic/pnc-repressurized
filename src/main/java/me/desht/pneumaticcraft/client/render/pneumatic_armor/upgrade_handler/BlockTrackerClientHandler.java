package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.BlockTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.BlockTrackerHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackerClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<BlockTrackerHandler> {
    static final int BLOCK_TRACKING_RANGE = 30;
    private static final int HARD_MAX_BLOCKS_PER_TICK = 50000;

    private final Map<BlockPos, RenderBlockTarget> blockTargets = new Object2ObjectOpenHashMap<>();
    private IGuiAnimatedStat blockTrackInfo;
    private final Object2IntMap<ResourceLocation> blockTypeCount = new Object2IntOpenHashMap<>();
    private final Object2IntMap<ResourceLocation> blockTypeCountPartial = new Object2IntOpenHashMap<>();
    private int xOff = 0, yOff = 0, zOff = 0;
    private RenderBlockTarget focusedTarget = null;
    private Direction focusedFace = null;

    public BlockTrackerClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().blockTrackerHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        int blockTrackRange = BLOCK_TRACKING_RANGE + Math.min(armorHandler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.RANGE), 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        long now = System.nanoTime();

        PlayerEntity player = armorHandler.getPlayer();
        World world = armorHandler.getPlayer().level;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < HARD_MAX_BLOCKS_PER_TICK; i++) {
            // 1% of a tick = 500,000ns
            if ((i & 0xff) == 0 && System.nanoTime() - now > ConfigHelper.client().armor.blockTrackerMaxTimePerTick.get() * 500_000L) {
                break;
            }

            nextScanPos(pos, player, blockTrackRange);

            if (!world.isAreaLoaded(pos, 0)) break;

            if (world.isEmptyBlock(pos)) continue;

            TileEntity te = world.getBlockEntity(pos);

            if (!MinecraftForge.EVENT_BUS.post(new BlockTrackEvent(world, pos, te))) {
                if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                    SearchClientHandler searchHandler = ArmorUpgradeClientRegistry.getInstance()
                            .getClientHandler(ArmorUpgradeRegistry.getInstance().searchHandler, SearchClientHandler.class);
                    searchHandler.checkInventoryForItems(te, null, WidgetKeybindCheckBox.isHandlerEnabled(ArmorUpgradeRegistry.getInstance().searchHandler));
                }
                List<IBlockTrackEntry> entries = BlockTrackEntryList.INSTANCE.getEntriesForCoordinate(world, pos, te);
                if (!entries.isEmpty()) {
                    entries.forEach(entry -> blockTypeCountPartial.mergeInt(entry.getEntryID(), 1, Integer::sum));

                    // there's at least one tracker type relevant to this blockpos
                    RenderBlockTarget blockTarget = blockTargets.get(pos);
                    if (blockTarget != null) {
                        // we already have a tracker active for this pos
                        blockTarget.ticksExisted = Math.abs(blockTarget.ticksExisted); // cancel possible "lost target" status
                        blockTarget.setTileEntity(te);
                    } else if (pos.distSqr(player.blockPosition()) < blockTrackRangeSq) {
                        // no tracker currently active for this pos - add a new one
                        addBlockTarget(new RenderBlockTarget(world, player, pos.immutable(), te, this));
                    }
                }
            }
        }

        checkBlockFocus(player, blockTrackRange);

        processTrackerEntries(player, blockTrackRange);

        updateTrackerText();
    }

    private void checkBlockFocus(PlayerEntity player, int blockTrackRange) {
        focusedTarget = null;
        focusedFace = null;
        Vector3d eyes = player.getEyePosition(1.0f);
        Vector3d v = eyes;
        Vector3d lookVec = player.getLookAngle().scale(0.25);  // scale down to minimise clipping across a corner and missing the block
        BlockPos.Mutable checkPos = new BlockPos.Mutable();
        for (int i = 0; i < blockTrackRange * 4; i++) {
            v = v.add(lookVec);
            checkPos.set(v.x, v.y, v.z);
            if (blockTargets.containsKey(checkPos)) {
                BlockState state = player.level.getBlockState(checkPos);
                BlockRayTraceResult brtr = state.getShape(player.level, checkPos).clip(eyes, v, checkPos);
                if (brtr != null && brtr.getType() == RayTraceResult.Type.BLOCK) {
                    focusedTarget = blockTargets.get(checkPos);
                    focusedFace = brtr.getDirection();
                    break;
                }
            }
        }
    }

    public BlockPos getFocusedPos() {
        return focusedTarget == null ? null : focusedTarget.getPos();
    }

    public Direction getFocusedFace() {
        return focusedFace;
    }

    /**
     * Advance the scan position but be clever about it; we never need to scan blocks behind the player
     */
    private void nextScanPos(BlockPos.Mutable pos, PlayerEntity player, int range) {
        Direction dir = PneumaticCraftUtils.getDirectionFacing(player, true);
        switch (dir) {
            case UP:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = 0;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case DOWN:
                if (++xOff > range) {
                    xOff = -range;
                    if (--yOff < -range) {
                        yOff = 0;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case EAST:
                if (++xOff > range) {
                    xOff = 0;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case WEST:
                if (--xOff < -range) {
                    xOff = 0;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case NORTH:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = -range;
                        if (--zOff < -range) {
                            zOff = 0;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case SOUTH:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = 0;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
        }
        pos.set(player.getX() + xOff, MathHelper.clamp(player.getY() + yOff, 0, 255), player.getZ() + zOff);
    }

    private void updateBlockTypeCounts() {
        blockTypeCount.clear();
        blockTypeCount.putAll(blockTypeCountPartial);
        blockTypeCountPartial.clear();
    }

    /**
     * Update all existing trackers and cull any which are either out of range or otherwise invalid
     * @param player the player
     * @param blockTrackRange the track range
     */
    private void processTrackerEntries(PlayerEntity player, int blockTrackRange) {
        List<RenderBlockTarget> toRemove = new ArrayList<>();
        int rangeSq = (blockTrackRange + 5) * (blockTrackRange + 5);
        int incr = CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlotType.HEAD);
        for (RenderBlockTarget blockTarget : blockTargets.values()) {
            boolean wasNegative = blockTarget.ticksExisted < 0;
            blockTarget.ticksExisted += incr;
            if (blockTarget.ticksExisted >= 0 && wasNegative) {
                blockTarget.ticksExisted = -1;
            }

            blockTarget.tick();

            BlockPos pos = blockTarget.getPos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > rangeSq || !blockTarget.isTargetStillValid()) {
                if (blockTarget.ticksExisted > 0) {
                    blockTarget.ticksExisted = -100;
                } else if (blockTarget.ticksExisted == -1) {
                    toRemove.add(blockTarget);
                }
            }
        }
        toRemove.forEach(this::removeBlockTarget);
    }

    private void updateTrackerText() {
        if (focusedTarget != null) {
            blockTrackInfo.setTitle(xlate("pneumaticcraft.armor.upgrade.block_tracker"));
            blockTrackInfo.setText(focusedTarget.getTitle());
        } else {
            blockTrackInfo.setTitle(xlate("pneumaticcraft.blockTracker.info.trackedBlocks"));

            List<ITextComponent> textList = new ArrayList<>();
            blockTypeCount.forEach((k, v) -> {
                if (v > 0 && WidgetKeybindCheckBox.get(k).checked) {
                    textList.add(xlate("pneumaticcraft.message.misc.countedItem", v, xlate(ArmorUpgradeRegistry.getStringKey(k))));
                }
            });

            if (textList.isEmpty()) textList.add(xlate("pneumaticcraft.blockTracker.info.noTrackedBlocks"));
            blockTrackInfo.setText(textList);
        }
    }

    private void addBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.put(blockTarget.getPos(), blockTarget);
    }

    private void removeBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.remove(blockTarget.getPos());
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type) {
        return blockTypeCount.getOrDefault(type.getEntryID(), 0);
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        blockTargets.values().forEach(t -> t.render(matrixStack, buffer, partialTicks));
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
        blockTypeCountPartial.clear();
        blockTypeCount.clear();
        blockTrackInfo = null;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new BlockTrackOptions(screen,this);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (blockTrackInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(EnumUpgrade.BLOCK_TRACKER.getItemStack());
            blockTrackInfo = new WidgetAnimatedStat(null, xlate("pneumaticcraft.blockTracker.info.trackedBlocks"),
                    icon, HUDHandler.getInstance().getStatOverlayColor(), null, ArmorHUDLayout.INSTANCE.blockTrackerStat);
            blockTrackInfo.setMinimumContractedDimensions(0, 0);
            blockTrackInfo.setAutoLineWrap(false);
        }
        return blockTrackInfo;

    }

    public void hack() {
        blockTargets.values().forEach(RenderBlockTarget::hack);
    }

    public RenderBlockTarget getTargetForCoord(BlockPos pos) {
        return blockTargets.get(pos);
    }

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        for (RenderBlockTarget target : blockTargets.values()) {
            if (target.scroll(event)) {
                getAnimatedStat().mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResolutionChanged() {
        blockTrackInfo = null;
    }

    @Override
    public Collection<ResourceLocation> getSubKeybinds() {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        BlockTrackEntryList.INSTANCE.trackList.forEach(entry -> builder.add(entry.getEntryID()));
        return builder.build();
    }

    @Override
    public String getSubKeybindCategory() {
        return Names.PNEUMATIC_KEYBINDING_CATEGORY_BLOCK_TRACKER;
    }

    @Override
    public void setOverlayColor(int color) {
        super.setOverlayColor(color);

        blockTargets.values().forEach(target -> target.updateColor(color));
    }
}
