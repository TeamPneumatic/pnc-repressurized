package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.render.RenderProgressBar;
import pneumaticCraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryInventory;
import pneumaticCraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketDescriptionPacketRequest;
import pneumaticCraft.common.network.PacketHackingBlockStart;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderBlockTarget{

    private final World world;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final RenderBlockArrows arrowRenderer = new RenderBlockArrows();
    public int ticksExisted = 0;
    public final GuiAnimatedStat stat;
    private final EntityPlayer player;
    private boolean playerIsLooking;
    public List<String> textList = new ArrayList<String>();
    private int hackTime;
    private final BlockTrackUpgradeHandler blockTracker;
    private TileEntity te;

    public RenderBlockTarget(World world, EntityPlayer player, int x, int y, int z, TileEntity te,
            BlockTrackUpgradeHandler blockTracker){
        this.world = world;
        this.player = player;
        blockX = x;
        blockY = y;
        blockZ = z;
        this.te = te;
        this.blockTracker = blockTracker;
        // oldTicksExisted = entity.ticksExisted;
        String title = world.getBlock(x, y, z).getLocalizedName();
        if(title.contains(".name")) {
            try {
                ItemStack stack = world.getBlock(x, y, z).getPickBlock(FMLClientHandler.instance().getClient().objectMouseOver, world, x, y, z, FMLClientHandler.instance().getClientPlayerEntity());
                if(stack != null) title = stack.getDisplayName();
            } catch(Throwable e) {}
        }
        if(title.contains(".name")) {
            if(te instanceof IInventory) {
                try {
                    title = I18n.format(((IInventory)te).getInventoryName());
                } catch(Throwable e) {
                    BlockTrackEntryInventory.addTileEntityToBlackList(te, e);
                }
            }
        }
        stat = new GuiAnimatedStat(null, title, "", 20, -20, 0x3000AA00, null, false);
        stat.setMinDimensionsAndReset(0, 0);
    }

    public void setTileEntity(TileEntity te){
        this.te = te;
    }

    public boolean isTargetStillValid(){
        return getApplicableEntries().size() > 0;
    }

    public List<IBlockTrackEntry> getApplicableEntries(){
        return BlockTrackEntryList.instance.getEntriesForCoordinate(world, blockX, blockY, blockZ, te);
    }

    public boolean isSameTarget(World world, int x, int y, int z){
        return blockX == x && blockY == y && blockZ == z;
    }

    public Block getBlock(){
        return world.getBlock(blockX, blockY, blockZ);
    }

    public double getDistanceToEntity(Entity entity){
        return entity.getDistance(blockX + 0.5D, blockY + 0.5D, blockZ + 0.5D);
    }

    public void update(){
        if(te != null && te.isInvalid()) te = null;
        stat.update();
        List<IBlockTrackEntry> applicableTrackEntries = getApplicableEntries();
        if(CommonHUDHandler.getHandlerForPlayer().ticksExisted % 100 == 0) {
            boolean sentUpdate = false;
            for(IBlockTrackEntry entry : applicableTrackEntries) {
                if(entry.shouldBeUpdatedFromServer(te)) {
                    if(!sentUpdate) {
                        NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(blockX, blockY, blockZ));
                        sentUpdate = true;
                    }
                }
            }
        }
        playerIsLooking = isPlayerLookingAtTarget();
        arrowRenderer.ticksExisted++;

        if(!getBlock().isAir(world, blockX, blockY, blockZ)) {
            textList = new ArrayList<String>();
            if(ticksExisted > 120) {
                stat.closeWindow();
                for(IBlockTrackEntry entry : applicableTrackEntries) {
                    if(blockTracker.countBlockTrackersOfType(entry) <= entry.spamThreshold()) {
                        stat.openWindow();
                        break;
                    }
                }
                if(playerIsLooking) {
                    stat.openWindow();
                    addBlockTrackInfo(textList);
                }
                stat.setText(textList);
            } else if(ticksExisted < -30) {
                stat.closeWindow();
                stat.setText(textList);
            }
        }

        if(hackTime > 0) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(world, blockX, blockY, blockZ, player);
            if(hackableBlock != null) {
                hackTime++;// = Math.min(hackTime + 1, hackableBlock.getHackTime(world, blockX, blockY, blockZ, player));
            } else {
                hackTime = 0;
            }
        }
    }

    public void render(float partialTicks){

        double x = blockX + 0.5D;
        double y = blockY + 0.5D;
        double z = blockZ + 0.5D;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glPushMatrix();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        float red = 0.5F;
        float green = 0.5F;
        float blue = 1.0F;
        float alpha = 0.5F;

        GL11.glTranslated(x, y, z);

        // for some reason the blend function resets... that's why this line is
        // here.
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if(!getBlock().isAir(world, blockX, blockY, blockZ)) arrowRenderer.render(world, blockX, blockY, blockZ, partialTicks);

        int targetAcquireProgress = (int)((ticksExisted - 50) / 0.7F);
        GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F - RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        if(ticksExisted <= 120 && ticksExisted > 50) {
            GL11.glColor4d(0, 1, 0, 0.8D);
            RenderProgressBar.render(0D, 0.4D, 1.8D, 0.9D, 0, targetAcquireProgress);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if(!getBlock().isAir(world, blockX, blockY, blockZ)) {
            FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();

            GL11.glColor4d(red, green, blue, alpha);
            if(ticksExisted > 120) {
                GL11.glScaled(0.02D, 0.02D, 0.02D);
                stat.render(-1, -1, partialTicks);
            } else if(ticksExisted > 50) {
                GL11.glScaled(0.02D, 0.02D, 0.02D);
                fontRenderer.drawString("Acquiring Target...", 0, 0, 0x7F7F7F);
                fontRenderer.drawString(targetAcquireProgress + "%", 37, 28, 0x002F00);
            } else if(ticksExisted < -30) {
                GL11.glScaled(0.03D, 0.03D, 0.03D);
                stat.render(-1, -1, partialTicks);
                fontRenderer.drawString("Lost Target!", 0, 0, 0xFF0000);
            }
        }
        GL11.glPopMatrix();
    }

    public boolean isInitialized(){
        return ticksExisted >= 120;
    }

    public void addBlockTrackInfo(List<String> textList){
        for(IBlockTrackEntry blockTrackEntry : getApplicableEntries())
            blockTrackEntry.addInformation(world, blockX, blockY, blockZ, te, textList);
    }

    public boolean isPlayerLooking(){
        return playerIsLooking;
    }

    private boolean isPlayerLookingAtTarget(){
        Vec3 vec3 = player.getLook(1.0F).normalize();
        Vec3 vec31 = Vec3.createVectorHelper(blockX + 0.5D - player.posX, blockY + 0.5D - player.posY + player.getEyeHeight(), blockZ + 0.5D - player.posZ);
        double d0 = vec31.lengthVector();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }

    public void hack(){
        if(isInitialized() && isPlayerLookingAtTarget()) {
            IHackableBlock block = HackableHandler.getHackableForCoord(world, blockX, blockY, blockZ, player);
            if(block != null && (hackTime == 0 || hackTime > block.getHackTime(world, blockX, blockY, blockZ, player))) NetworkHandler.sendToServer(new PacketHackingBlockStart(blockX, blockY, blockZ));
        }
    }

    public void onHackConfirmServer(){
        hackTime = 1;
    }

    public int getHackTime(){
        return hackTime;
    }

    public boolean scroll(MouseEvent event){
        if(isInitialized() && isPlayerLookingAtTarget()) {
            return stat.handleMouseWheel(event.dwheel);
        }
        return false;
    }
}
