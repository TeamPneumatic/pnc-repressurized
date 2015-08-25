package pneumaticCraft.common.thirdparty.fmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.Log;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;

public class FMP implements IThirdParty, IPartFactory, IPartConverter{

    private final Map<String, Class<? extends TMultiPart>> multiparts = new HashMap<String, Class<? extends TMultiPart>>();

    @Override
    public void preInit(){
        multiparts.put("tile.pressureTube", PartPressureTube.class);
        multiparts.put("tile.advancedPressureTube", PartAdvancedPressureTube.class);

        for(String part : multiparts.keySet()) {
            MultiPartRegistry.registerParts(this, new String[]{part});
        }
        MultiPartRegistry.registerConverter(this);

        // Itemss.pressureTube = new ItemPart("tile.pressureTube").setUnlocalizedName("pressureTube").setCreativeTab(pneumaticCraftTab);
        //Itemss.registerItem(Itemss.pressureTube, "part.pressureTube");
        MinecraftForge.EVENT_BUS.register(new FMPPlacementListener());
        NetworkHandler.INSTANCE.registerMessage(PacketFMPPlacePart.class, PacketFMPPlacePart.class, NetworkHandler.discriminant++, Side.SERVER);
    }

    public void registerPart(String partName, Class<? extends TMultiPart> part){
        multiparts.put(partName, part);
    }

    @Override
    public void init(){}

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){}

    @Override
    public Iterable<Block> blockTypes(){
        if(Config.convertMultipartsToBlocks) return new ArrayList<Block>();
        return Arrays.asList(new Block[]{Blockss.pressureTube, Blockss.advancedPressureTube});
    }

    @Override
    public TMultiPart convert(World world, BlockCoord pos){
        if(!Config.convertMultipartsToBlocks) {
            if(world.getBlock(pos.x, pos.y, pos.z) == Blockss.pressureTube) return new PartPressureTube((TileEntityPressureTube)world.getTileEntity(pos.x, pos.y, pos.z));
            if(world.getBlock(pos.x, pos.y, pos.z) == Blockss.advancedPressureTube) return new PartAdvancedPressureTube((TileEntityPressureTube)world.getTileEntity(pos.x, pos.y, pos.z));
        }
        return null;
    }

    @Override
    public TMultiPart createPart(String id, boolean client){
        try {
            return multiparts.get(id).newInstance();
        } catch(Exception e) {
            Log.error("Failed to instantiate the multipart with id " + id + ". Is the constructor a parameterless one?");
            return null;
        }
    }

    private static TileMultipart getMultipartTile(IBlockAccess access, ChunkPosition pos){
        TileEntity te = access.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        return te instanceof TileMultipart ? (TileMultipart)te : null;
    }

    /* public static TMultiPart getMultiPart(IBlockAccess w, ChunkPosition bc, int part){
         TileMultipart t = getMultipartTile(w, bc);
         if(t != null) return t.partMap(part);
         return null;
     }*/

    public static <T> T getMultiPart(IBlockAccess access, ChunkPosition pos, Class<T> searchedClass){
        TileMultipart t = getMultipartTile(access, pos);
        return t == null ? null : getMultiPart(t, searchedClass);
    }

    public static <T> T getMultiPart(TileMultipart t, Class<T> searchedClass){
        for(TMultiPart part : t.jPartList()) {
            if(searchedClass.isAssignableFrom(part.getClass())) return (T)part;
        }
        return null;
    }

    public static <T> Iterable<T> getMultiParts(TileMultipart t, Class<T> searchedClass){
        List<T> parts = new ArrayList<T>();
        for(TMultiPart part : t.jPartList()) {
            if(searchedClass.isAssignableFrom(part.getClass())) parts.add((T)part);
        }
        return parts;
    }

    @Override
    public void clientInit(){}
}
