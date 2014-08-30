package pneumaticCraft.common.thirdparty.fmp;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.block.tubes.ISidedPart;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartTubeModule extends JCuboidPart implements JNormalOcclusion, ISidedPart{
    private TubeModule module;
    private static Cuboid6[] boundingBoxes;

    public void setModule(TubeModule module){
        this.module = module;
    }

    public TubeModule getModule(){
        return module;
    }

    @Override
    public void setDirection(ForgeDirection dir){
        module.setDirection(dir);
    }

    @Override
    public void load(NBTTagCompound nbt){
        module = ModuleRegistrator.getModule(nbt.getString("type"));
        module.readFromNBT(nbt);
    }

    @Override
    public void writeDesc(MCDataOutput data){
        data.writeString(module.getType());
        // module.writeDesc(data);
    }

    @Override
    public void readDesc(MCDataInput data){
        String moduleName = data.readString();
        module = ModuleRegistrator.getModule(moduleName);
        // module.readDesc(data);
    }

    @Override
    public void save(NBTTagCompound nbt){
        nbt.setString("type", module.getType());
        module.writeToNBT(nbt);
    }

    @Override
    public void update(){
        if(!Config.convertMultipartsToBlocks && module.getTube() != null) module.update();
    }

    @Override
    public void onWorldJoin(){
        updateTube();
    }

    @Override
    public void onPartChanged(TMultiPart part){
        if(FMP.getMultiPart(world(), new ChunkPosition(x(), y(), z()), PartPressureTube.class) == null) {//If the tube was removed
            if(!world().isRemote) {
                tile().dropItems(getDrops());
                tile().remPart(this);
            }
        } else {
            updateTube();
        }

    }

    private void updateTube(){
        PartPressureTube tube = FMP.getMultiPart(tile(), PartPressureTube.class);
        if(tube != null) module.setTube(tube);
    }

    @Override
    public String getType(){
        return module.getType();
    }

    @Override
    public Iterable<ItemStack> getDrops(){
        return module.getDrops();
    }

    @Override
    public ItemStack pickItem(MovingObjectPosition hit){
        return getItem();
    }

    public ItemStack getItem(){
        return new ItemStack(ModuleRegistrator.getModuleItem(module.getType()));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderDynamic(Vector3 pos, float partialTicks, int renderPass){
        module.renderDynamic(pos.x, pos.y, pos.z, partialTicks, renderPass);
    }

    @Override
    public Cuboid6 getBounds(){
        if(boundingBoxes == null) {
            boundingBoxes = new Cuboid6[6];
            for(int i = 0; i < 6; i++) {
                AxisAlignedBB aabb = module.boundingBoxes[i];
                boundingBoxes[i] = new Cuboid6(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            }
        }
        return boundingBoxes[module.getDirection().ordinal() % 6];
    }

    @Override
    public boolean occlusionTest(TMultiPart multipart){
        return NormalOcclusionTest.apply(this, multipart);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes(){
        return Arrays.asList(getBounds());
    }
}
