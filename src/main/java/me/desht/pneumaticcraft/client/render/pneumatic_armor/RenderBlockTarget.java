package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockStart;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RenderBlockTarget {

    private final World world;
    private final BlockPos pos;
    private final RenderBlockHighlight highlightRenderer = new RenderBlockHighlight();
    public int ticksExisted = 0;
    public final WidgetAnimatedStat stat;
    private final PlayerEntity player;
    public List<String> textList = new ArrayList<>();
    private int hackTime;
    private final BlockTrackUpgradeHandler blockTracker;
    private TileEntity te;
    private int nEntries;

    public RenderBlockTarget(World world, PlayerEntity player, BlockPos pos, TileEntity te,
                             BlockTrackUpgradeHandler blockTracker) {
        this.world = world;
        this.player = player;
        this.pos = pos;
        this.te = te;
        this.blockTracker = blockTracker;
        String title = world.getBlockState(pos).getBlock().getTranslationKey();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = state.getBlock().getPickBlock(state, Minecraft.getInstance().objectMouseOver, world, pos, player);
        if (!stack.isEmpty()) {
            title = stack.getDisplayName().getFormattedText();
        }
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(stack), 20, -20, 0x3000AA00, null, false);
        stat.setMinDimensionsAndReset(0, 0);
    }

    public void setTileEntity(TileEntity te) {
        this.te = te;
    }

    /**
     * Check if this target still has any applicable entries. This should be called after calling update().
     *
     * @return true if valid, false otherwise
     */
    public boolean isTargetStillValid() {
        return nEntries > 0;
    }

    private List<IBlockTrackEntry> getApplicableEntries() {
        return BlockTrackEntryList.instance.getEntriesForCoordinate(world, pos, te);
    }

    public BlockPos getPos() {
        return pos;
    }

    public double getDistanceToEntity(Entity entity) {
        return Math.sqrt(entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    public void maybeRefreshFromServer(List<IBlockTrackEntry> applicableTrackEntries) {
        for (IBlockTrackEntry entry : applicableTrackEntries) {
            entry.getServerUpdatePositions(te).forEach(p -> NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(p)));
        }
    }

    public void update() {
        if (te != null && te.isRemoved()) {
            te = null;
        }

        stat.tickWidget();

        List<IBlockTrackEntry> applicableTrackEntries = getApplicableEntries();
        nEntries = applicableTrackEntries.size();

        if (world.getGameTime() % 100 == 7) {
            maybeRefreshFromServer(applicableTrackEntries);
        }

        if (!world.isAirBlock(pos)) {
            textList = new ArrayList<>();
            if (ticksExisted > 120) {
                stat.closeWindow();
                for (IBlockTrackEntry entry : applicableTrackEntries) {
                    if (blockTracker.countBlockTrackersOfType(entry) <= entry.spamThreshold()) {
                        stat.openWindow();
                        break;
                    }
                }
                if (isPlayerLookingAtTarget()) {
                    stat.openWindow();
                    addBlockTrackInfo(textList, applicableTrackEntries);
                }
                stat.setText(textList);
            } else if (ticksExisted < -30) {
                stat.closeWindow();
                stat.setText(textList);
            }
        }

        if (hackTime > 0) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(world, pos, player);
            if (hackableBlock != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }
    }

    public void render(float partialTicks) {

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        GlStateManager.disableTexture();
        GlStateManager.pushMatrix();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        GlStateManager.translated(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (!world.isAirBlock(pos)) {
            highlightRenderer.render(world, pos, partialTicks);
        }

        float targetAcquireProgress = (ticksExisted + partialTicks) / 1.20f;

        GlStateManager.rotated(180.0F - Minecraft.getInstance().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotated(180.0F - Minecraft.getInstance().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        if (ticksExisted <= 120 && ticksExisted > 50) {
            RenderProgressBar.render(0D, 0.4D, 1.8D, 0.9D, 0, targetAcquireProgress, 0xD0FFFF00, 0xD000FF00);
        }

        GlStateManager.enableTexture();
        if (!world.isAirBlock(pos)) {
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

            GlStateManager.color4f(0.5F, 1.0F, 0.5F, 0.5F);
            if (ticksExisted > 120) {
                GlStateManager.scaled(0.02D, 0.02D, 0.02D);
                stat.render(-1, -1, partialTicks);
            } else if (ticksExisted > 50) {
                GlStateManager.scaled(0.02D, 0.02D, 0.02D);
                fontRenderer.drawString("Acquiring Target...", 0, 0, 0x7F7F7F);
                fontRenderer.drawString((int)targetAcquireProgress + "%", 37, 28, 0x002F00);
            } else if (ticksExisted < -30) {
                GlStateManager.scaled(0.03D, 0.03D, 0.03D);
                stat.render(-1, -1, partialTicks);
                fontRenderer.drawString("Lost Target!", 0, 0, 0xFF0000);
            }
        }

        GlStateManager.popMatrix();
    }

    private boolean isInitialized() {
        return ticksExisted >= 120;
    }

    private void addBlockTrackInfo(List<String> textList, List<IBlockTrackEntry> entries) {
        entries.forEach(e -> e.addInformation(world, pos, te, isPlayerLookingAtTarget() ? blockTracker.getFocusedFace() : null, textList));
    }

    private boolean isPlayerLookingAtTarget() {
        return pos.equals(blockTracker.getFocusedPos());
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableBlock block = HackableHandler.getHackableForCoord(world, pos, player);
            if (block != null && (hackTime == 0 || hackTime > block.getHackTime(world, pos, player)))
                NetworkHandler.sendToServer(new PacketHackingBlockStart(pos));
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(GuiScreenEvent.MouseScrollEvent.Post event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }
}
