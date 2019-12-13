package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.BlockTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiBlockTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockTrackUpgradeHandler implements IUpgradeRenderHandler {
    static final int BLOCK_TRACKING_RANGE = 30;
    private static final int HARD_MAX_BLOCKS_PER_TICK = 50000;

    private final Map<BlockPos, RenderBlockTarget> blockTargets = new HashMap<>();
    private WidgetAnimatedStat blockTrackInfo;
    private final Map<String,Integer> blockTypeCount = new HashMap<>();
    private final Map<String,Integer> blockTypeCountPartial = new HashMap<>();
    private int xOff = 0, yOff = 0, zOff = 0;
    private RenderBlockTarget focusedTarget = null;
    private Direction focusedFace = null;

    @Override
    public String getUpgradeID() {
        return "blockTracker";
    }

    @Override
    public void update(PlayerEntity player, int rangeUpgrades) {
        SearchUpgradeHandler searchHandler = HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        int blockTrackRange = BLOCK_TRACKING_RANGE + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        long now = System.nanoTime();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < HARD_MAX_BLOCKS_PER_TICK; i++) {
            // 1% of a tick = 500,000ns
            if ((i & 0xff) == 0 && System.nanoTime() - now > PNCConfig.Client.Armor.blockTrackerMaxTimePerTick * 500000) {
                break;
            }

            nextScanPos(pos, player, blockTrackRange);

            if (!player.world.isAreaLoaded(pos, 0)) break;

            TileEntity te = player.world.getTileEntity(pos);

            if (!MinecraftForge.EVENT_BUS.post(new BlockTrackEvent(player.world, pos, te))) {
                if (searchHandler != null && te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                    searchHandler.checkInventoryForItems(te, null, WidgetKeybindCheckBox.isHandlerEnabled(searchHandler));
                }
                List<IBlockTrackEntry> entries = BlockTrackEntryList.instance.getEntriesForCoordinate(player.world, pos, te);
                if (!entries.isEmpty()) {

                    entries.forEach(entry -> {
                        String k = entry.getEntryName();
                        blockTypeCountPartial.put(k, blockTypeCountPartial.getOrDefault(k, 0) + 1);
                    });

                    // there's at least one tracker type relevant to this blockpos
                    RenderBlockTarget blockTarget = blockTargets.get(pos);
                    if (blockTarget != null) {
                        // we already have a tracker active for this pos
                        blockTarget.ticksExisted = Math.abs(blockTarget.ticksExisted); // cancel possible "lost target" status
                        blockTarget.setTileEntity(te);
                    } else {
                        // no tracker currently active - add one
                        RenderBlockTarget target = addBlockTarget(new RenderBlockTarget(player.world, player, pos.toImmutable(), te, this));
                        target.maybeRefreshFromServer(entries);
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
        Vec3d eyes = player.getEyePosition(1.0f);
        Vec3d v = eyes;
        Vec3d lookVec = player.getLookVec();
        for (int i = 0; i < blockTrackRange * 4; i++) {
            v = v.add(lookVec.scale(0.25));  // scale down to minimise clipping across a corner and missing the block
            BlockPos checkPos = new BlockPos(v.x, v.y, v.z);
            if (blockTargets.containsKey(checkPos)) {
                BlockState state = player.world.getBlockState(checkPos);
                BlockRayTraceResult brtr = state.getShape(player.world, checkPos).rayTrace(eyes, v, checkPos);
                if (brtr != null && brtr.getType() == RayTraceResult.Type.BLOCK) {
                    focusedTarget = blockTargets.get(checkPos);
                    focusedFace = brtr.getFace();
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
    private void nextScanPos(BlockPos.MutableBlockPos pos, PlayerEntity player, int range) {
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
        pos.setPos(player.posX + xOff, MathHelper.clamp(player.posY + yOff, 0, 255), player.posZ + zOff);
    }

    private void updateBlockTypeCounts() {
        blockTypeCount.clear();
        blockTypeCountPartial.forEach(blockTypeCount::put);
        blockTypeCountPartial.clear();
    }

    /**
     * Update all existing trackers and cull any which are either out of range or otherwise invalid
     * @param player the player
     * @param blockTrackRange the track range
     */
    private void processTrackerEntries(PlayerEntity player, int blockTrackRange) {
        List<RenderBlockTarget> toRemove = new ArrayList<>();
        for (RenderBlockTarget blockTarget : blockTargets.values()) {
            boolean wasNegative = blockTarget.ticksExisted < 0;
            blockTarget.ticksExisted += CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlotType.HEAD);
            if (blockTarget.ticksExisted >= 0 && wasNegative) {
                blockTarget.ticksExisted = -1;
            }

            blockTarget.update();

            if (blockTarget.getDistanceToEntity(player) > blockTrackRange + 5 || !blockTarget.isTargetStillValid()) {
                if (blockTarget.ticksExisted > 0) {
                    blockTarget.ticksExisted = -60;
                } else if (blockTarget.ticksExisted == -1) {
                    toRemove.add(blockTarget);
                }
            }
        }
        toRemove.forEach(this::removeBlockTarget);
    }

    private void updateTrackerText() {
        List<String> textList = new ArrayList<>();

        if (focusedTarget != null) {
            blockTrackInfo.setTitle(focusedTarget.stat.getTitle());
            textList.addAll(focusedTarget.textList);
        } else {
            blockTrackInfo.setTitle("Current tracked blocks:");

            blockTypeCount.forEach((k, v) -> {
                if (v > 0 && WidgetKeybindCheckBox.fromKeyBindingName(k).checked) textList.add(v + " " + I18n.format(k));
            });

            if (textList.size() == 0) textList.add("Tracking no blocks currently.");
        }

        blockTrackInfo.setText(textList);
    }

    private RenderBlockTarget addBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.put(blockTarget.getPos(), blockTarget);

        return blockTarget;
    }

    private void removeBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.remove(blockTarget.getPos());
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type) {
        return blockTypeCount.getOrDefault(type.getEntryName(), 0);
    }

    @Override
    public void render3D(float partialTicks) {
        GlStateManager.depthMask(false);
        GlStateManager.disableDepthTest();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        blockTargets.values().forEach(t -> t.render(partialTicks));

        GlStateManager.enableCull();
        GlStateManager.enableDepthTest();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled) {
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{ EnumUpgrade.BLOCK_TRACKER.getItem() };
    }

    @Override
    public void reset() {
        blockTypeCountPartial.clear();
        blockTypeCount.clear();
        blockTrackInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
        return PneumaticValues.USAGE_BLOCK_TRACKER
                * (1 + (float) Math.min(5, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / BLOCK_TRACKING_RANGE)
                * CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlotType.HEAD);
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiBlockTrackOptions(this);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot() {
        return EquipmentSlotType.HEAD;
    }

    @Override
    public WidgetAnimatedStat getAnimatedStat() {
        if (blockTrackInfo == null) {
            WidgetAnimatedStat.StatIcon icon = WidgetAnimatedStat.StatIcon.of(EnumUpgrade.BLOCK_TRACKER.getItem());
            blockTrackInfo = new WidgetAnimatedStat(null, "Current tracked blocks:",
                    icon, 0x3000AA00, null, ArmorHUDLayout.INSTANCE.blockTrackerStat);
            blockTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return blockTrackInfo;

    }

    public void hack() {
        for (RenderBlockTarget target : blockTargets.values()) {
            target.hack();
        }
    }

    public RenderBlockTarget getTargetForCoord(BlockPos pos) {
        return blockTargets.get(pos);
    }

    public boolean scroll(GuiScreenEvent.MouseScrollEvent.Post event) {
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
}
