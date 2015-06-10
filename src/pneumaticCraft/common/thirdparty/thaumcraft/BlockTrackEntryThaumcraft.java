package pneumaticCraft.common.thirdparty.thaumcraft;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;

/**
 * Created by Maarten on 25-Jul-14.
 */
public class BlockTrackEntryThaumcraft implements IBlockTrackEntry{
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block, TileEntity te){
        return te instanceof IAspectContainer;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te){
        return false;
    }

    @Override
    public int spamThreshold(){
        return 8;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, TileEntity te, List<String> infoList){
        if(te instanceof IAspectContainer) {
            IAspectContainer container = (IAspectContainer)te;
            AspectList aspects = container.getAspects();
            if(aspects != null && aspects.size() > 0) {
                infoList.add("blockTracker.info.thaumcraft");
                for(Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
                    infoList.add("-" + entry.getValue() + "x " + entry.getKey().getName());
                }
            } else {
                infoList.add(I18n.format("blockTracker.info.thaumcraft") + " -");
            }
            if(container instanceof INode) {
                INode node = (INode)container;
                infoList.add(I18n.format("blockTracker.info.thaumcraft.nodetype") + " " + I18n.format("nodetype." + node.getNodeType() + ".name"));
                if(node.getNodeModifier() != null) infoList.add(I18n.format("blockTracker.info.thaumcraft.nodeModifier") + " " + I18n.format("nodemod." + node.getNodeModifier() + ".name"));
            }
        }
    }

    @Override
    public String getEntryName(){
        return "blockTracker.module.thaumcraft";
    }
}
