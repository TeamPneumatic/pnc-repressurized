package pneumaticCraft.common.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPlasticPlants extends ItemPneumatic{

    public static final int SQUID_PLANT_DAMAGE = 0;
    public static final int FIRE_FLOWER_DAMAGE = 1;
    public static final int CREEPER_PLANT_DAMAGE = 2;
    public static final int SLIME_PLANT_DAMAGE = 3;
    public static final int RAIN_PLANT_DAMAGE = 4;
    public static final int ENDER_PLANT_DAMAGE = 5;
    public static final int LIGHTNING_PLANT_DAMAGE = 6;
    public static final int ADRENALINE_PLANT_DAMAGE = 7;
    public static final int BURST_PLANT_DAMAGE = 8;
    public static final int POTION_PLANT_DAMAGE = 9;
    public static final int REPULSION_PLANT_DAMAGE = 10;
    public static final int HELIUM_PLANT_DAMAGE = 11;
    public static final int CHOPPER_PLANT_DAMAGE = 12;
    public static final int MUSIC_PLANT_DAMAGE = 13;
    public static final int PROPULSION_PLANT_DAMAGE = 14;
    public static final int FLYING_FLOWER_DAMAGE = 15;
    private static Random rand = new Random();

    public static final String[] PLANT_NAMES = new String[]{"Squid Plant", "Fire Flower", "Creeper Plant", "Slime Plant", "Rain Plant", "Ender Plant", "Lightning Plant", "Adrenaline Plant", "Burst Plant", "Potion Plant", "Repulsion Plant", "Helium Plant", "Chopper Plant", "Music Plant", "Propulsion Plant", "Flying Flower"};
    public static final boolean[] NEEDS_GENERATION = new boolean[]{true, true, true, true, true, false, true, false, true, true, true, true, true, false, true, true};

    private IIcon[] texture;

    public ItemPlasticPlants(){
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg){
        texture = new IIcon[16];
        texture[0] = registerSeed(reg, Textures.ICON_SQUID_PLANT_LOCATION);
        texture[1] = registerSeed(reg, Textures.ICON_FIRE_FLOWER_LOCATION);
        texture[2] = registerSeed(reg, Textures.ICON_CREEPER_PLANT_LOCATION);
        texture[3] = registerSeed(reg, Textures.ICON_SLIME_PLANT_LOCATION);
        texture[4] = registerSeed(reg, Textures.ICON_RAIN_PLANT_LOCATION);
        texture[5] = registerSeed(reg, Textures.ICON_ENDER_PLANT_LOCATION);
        texture[6] = registerSeed(reg, Textures.ICON_LIGHTNING_PLANT_LOCATION);
        texture[7] = registerSeed(reg, Textures.ICON_ADRENALINE_PLANT_LOCATION);
        texture[8] = registerSeed(reg, Textures.ICON_BURST_PLANT_LOCATION);
        texture[9] = registerSeed(reg, Textures.ICON_POTION_PLANT_LOCATION);
        texture[10] = registerSeed(reg, Textures.ICON_REPULSION_PLANT_LOCATION);
        texture[11] = registerSeed(reg, Textures.ICON_HELIUM_PLANT_LOCATION);
        texture[12] = registerSeed(reg, Textures.ICON_CHOPPER_PLANT_LOCATION);
        texture[13] = registerSeed(reg, Textures.ICON_MUSIC_PLANT_LOCATION);
        texture[14] = registerSeed(reg, Textures.ICON_PROPULSION_PLANT_LOCATION);
        texture[15] = registerSeed(reg, Textures.ICON_FLYING_FLOWER_LOCATION);
    }

    @SideOnly(Side.CLIENT)
    public IIcon registerSeed(IIconRegister register, String texture){
        return register.registerIcon(texture + "Seeds");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta){
        return texture[meta % 16];
    }

    @Override
    public String getUnlocalizedName(ItemStack stack){
        return super.getUnlocalizedName(stack) + stack.getItemDamage();
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item par1, CreativeTabs tab, List subItems){
        addSubItems(subItems);
    }

    public void addSubItems(List items){
        for(int i = 0; i < 16; i++) {
            if(i == ADRENALINE_PLANT_DAMAGE) continue;
            if(i == MUSIC_PLANT_DAMAGE) continue;
            items.add(new ItemStack(this, 1, i));
        }
    }

    public static Map<Block, ItemStack> getBlockToSeedMap(){
        Map<Block, ItemStack> blockToSeedMap = new HashMap<Block, ItemStack>();
        List<ItemStack> seeds = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(seeds);
        for(ItemStack seed : seeds) {
            blockToSeedMap.put(getPlantBlockIDFromSeed(seed.getItemDamage()), seed);
        }
        return blockToSeedMap;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        BlockPneumaticPlantBase plant = (BlockPneumaticPlantBase)getPlantBlockIDFromSeed(stack.getItemDamage());
        if(side != (plant.isPlantHanging() ? 0 : 1)) {
            return false;
        } else if(player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + (plant.isPlantHanging() ? -1 : 1), z, side, stack)) {
            if(plant.canBlockStay(world, x, y + (plant.isPlantHanging() ? -1 : 1), z) && world.isAirBlock(x, y + (plant.isPlantHanging() ? -1 : 1), z)) {
                world.setBlock(x, y + (plant.isPlantHanging() ? -1 : 1), z, plant, 7, 3);
                --stack.stackSize;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        BlockPneumaticPlantBase plant = (BlockPneumaticPlantBase)getPlantBlockIDFromSeed(stack.getItemDamage());
        if(plant == Blockss.squidPlant) {
            MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, true);
            if(mop != null) {
                int x = mop.blockX;
                int y = mop.blockY;
                int z = mop.blockZ;
                if(player.canPlayerEdit(x, y, z, 1, stack) && player.canPlayerEdit(x, y + 1, z, 1, stack)) {
                    if(plant.canBlockStay(world, x, y + 1, z) && world.isAirBlock(x, y + 1, z)) {
                        stack.stackSize--;
                        world.setBlock(x, y + 1, z, Blockss.squidPlant, 7, 3);
                    }
                }
            }
        }
        return stack;
    }

    public static void onEntityConstruction(Entity entity){
        if(entity instanceof EntityItem) {
            if(((EntityItem)entity).getExtendedProperties("PneumaticCraft_Active") == null) entity.registerExtendedProperties("PneumaticCraft_Active", new ActivityProperty(true));
        }
    }

    public static boolean isActive(EntityItem entityItem){
        ActivityProperty prop = (ActivityProperty)entityItem.getExtendedProperties("PneumaticCraft_Active");
        return prop == null || prop.active;
    }

    public static void markInactive(EntityItem entityItem){
        ((ActivityProperty)entityItem.getExtendedProperties("PneumaticCraft_Active")).active = false;
    }

    public static class ActivityProperty implements IExtendedEntityProperties{
        public boolean active;

        public ActivityProperty(boolean active){
            this.active = active;
        }

        @Override
        public void saveNBTData(NBTTagCompound compound){
            if(active) compound.setBoolean("PneumaticCraft_Active", active);
        }

        @Override
        public void loadNBTData(NBTTagCompound compound){
            active = compound.getBoolean("PneumaticCraft_Active");
        }

        @Override
        public void init(Entity entity, World world){}

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        infoList.add(EnumChatFormatting.RED + "Plastic plants are being removed in favor of Oil!");
        infoList.add(EnumChatFormatting.RED + "They only exist still to provide a smooth transition towards the Oil system!");
        switch(stack.getItemDamage()){
            case SQUID_PLANT_DAMAGE:
                infoList.add("Soil: Water");
                break;
            case FIRE_FLOWER_DAMAGE:
            case HELIUM_PLANT_DAMAGE:
                infoList.add("Soil: Netherrack");
                break;
            case ENDER_PLANT_DAMAGE:
                infoList.add("Soil: End Stone");
                break;
            default:
                infoList.add("Soil: Dirt, Grass or Farmland");
                break;
        }
        infoList.add(I18n.format("gui.tooltip.plasticPlant.plant"));
    }

    public static Block getPlantBlockIDFromSeed(int seedMetadata){
        switch(seedMetadata % 16){
            case 0:
                return Blockss.squidPlant;
            case 1:
                return Blockss.fireFlower;
            case 2:
                return Blockss.creeperPlant;
            case 3:
                return Blockss.slimePlant;
            case 4:
                return Blockss.rainPlant;
            case 5:
                return Blockss.enderPlant;
            case 6:
                return Blockss.lightningPlant;
            case 7:
                return Blockss.adrenalinePlant;
            case 8:
                return Blockss.burstPlant;
            case 9:
                return Blockss.potionPlant;
            case 10:
                return Blockss.repulsionPlant;
            case 11:
                return Blockss.heliumPlant;
            case 12:
                return Blockss.chopperPlant;
            case 13:
                return Blockss.musicPlant;
            case 14:
                return Blockss.propulsionPlant;
            case 15:
                return Blockss.flyingFlower;
        }
        throw new IllegalArgumentException("[PneumaticCraft] Wrong metadata for seed! Meta: " + seedMetadata);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem){
        MotionProperties motProps = (MotionProperties)entityItem.getExtendedProperties("plasticPlant");
        double oldMotionX = entityItem.motionX;
        double oldMotionY = entityItem.motionY;
        double oldMotionZ = entityItem.motionZ;
        if(motProps != null) {
            oldMotionX = motProps.oldMotionX;
            oldMotionY = motProps.oldMotionY;
            oldMotionZ = motProps.oldMotionZ;
        }

        ItemStack stack = entityItem.getEntityItem();
        int itemDamage = stack.getItemDamage();

        if(motProps == null && (itemDamage % 16 == ItemPlasticPlants.PROPULSION_PLANT_DAMAGE || itemDamage % 16 == ItemPlasticPlants.REPULSION_PLANT_DAMAGE)) {
            motProps = new MotionProperties();
            entityItem.registerExtendedProperties("plasticPlant", motProps);
        }
        if(motProps != null) motProps.update(entityItem);

        boolean isDelayOver = isActive(entityItem) || entityItem.age > 60 && entityItem.delayBeforeCanPickup == 0;
        if(entityItem.onGround || Math.abs(entityItem.motionY) < 0.13D && (itemDamage % 16 == ItemPlasticPlants.HELIUM_PLANT_DAMAGE || itemDamage % 16 == ItemPlasticPlants.SQUID_PLANT_DAMAGE)) {
            if(!handleRepulsionBehaviour(entityItem, oldMotionX, oldMotionY, oldMotionZ)) return false;
            if(!handlePropulsionBehaviour(entityItem, oldMotionX, oldMotionZ)) return false;
            if(!entityItem.worldObj.isRemote) {
                Block blockID = getPlantBlockIDFromSeed(itemDamage % 16);
                int landedBlockX = (int)Math.floor(entityItem.posX);// - 0.5F);
                int landedBlockY = (int)Math.floor(entityItem.posY);
                int landedBlockZ = (int)Math.floor(entityItem.posZ);// - 0.5F);

                boolean canSustain = false;

                canSustain = ((BlockPneumaticPlantBase)blockID).canBlockStay(entityItem.worldObj, landedBlockX, landedBlockY, landedBlockZ);
                if(itemDamage % 16 == ItemPlasticPlants.FIRE_FLOWER_DAMAGE && !canSustain && !isInChamber(entityItem.worldObj.getBlock(landedBlockX, landedBlockY - 1, landedBlockZ)) && net.minecraft.init.Blocks.fire.canPlaceBlockAt(entityItem.worldObj, landedBlockX, landedBlockY, landedBlockZ) && entityItem.worldObj.isAirBlock(landedBlockX, landedBlockY, landedBlockZ)) {
                    entityItem.worldObj.setBlock(landedBlockX, landedBlockY, landedBlockZ, net.minecraft.init.Blocks.fire);
                }

                if(canSustain && isDelayOver) {
                    if(entityItem.worldObj.isAirBlock(landedBlockX, landedBlockY, landedBlockZ)) {

                        entityItem.worldObj.setBlock(landedBlockX, landedBlockY, landedBlockZ, blockID, itemDamage > 15 || !isActive(entityItem) ? 0 : 7, 3);

                        entityItem.playSound("mob.chicken.plop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
                        for(int i = 0; i < 10; i++) {
                            spawnParticle(entityItem.worldObj, "explode", entityItem.posX + rand.nextDouble() - 0.5D, entityItem.posY + rand.nextDouble() - 0.5D, entityItem.posZ + rand.nextDouble() - 0.5D, 0.0D, 0.0D, 0.0D);
                        }
                        if(stack.stackSize == 1) {
                            entityItem.setDead();
                        } else {
                            stack.stackSize--;
                        }
                    }
                }
            }
        }

        // when the entity on the ground check whether the block beneath it is
        // dirt, and the block above it is air, if yes, then plant it.
        if(itemDamage % 16 == ItemPlasticPlants.SQUID_PLANT_DAMAGE && entityItem.worldObj.isMaterialInBB(entityItem.boundingBox.contract(0.003D, 0.003D, 0.003D), Material.water)) {
            entityItem.motionY += 0.06D;
        }
        if(itemDamage % 16 == ItemPlasticPlants.HELIUM_PLANT_DAMAGE) {
            entityItem.motionY += 0.08D;
        }
        if(itemDamage % 16 == ItemPlasticPlants.FLYING_FLOWER_DAMAGE) {
            entityItem.motionY += 0.04D;
            if(entityItem.age % 60 == 0) {
                entityItem.motionX += (rand.nextDouble() - 0.5D) * 0.1D;
                entityItem.motionY += (rand.nextDouble() - 0.6D) * 0.1D;
                entityItem.motionZ += (rand.nextDouble() - 0.5D) * 0.1D;
            }
        }
        return false;
    }

    private boolean isInChamber(Block block){
        return block == Blockss.pressureChamberInterface || block == Blockss.pressureChamberValve || block == Blockss.pressureChamberWall;
    }

    private boolean handlePropulsionBehaviour(EntityItem entityItem, double oldMotionX, double oldMotionZ){
        int itemDamage = entityItem.getEntityItem().getItemDamage();
        if(itemDamage == ItemPlasticPlants.PROPULSION_PLANT_DAMAGE) {
            boolean flag = Math.sqrt(entityItem.motionX * entityItem.motionX + entityItem.motionZ * entityItem.motionZ) < 0.1D;
            if(Math.sqrt(entityItem.motionX * entityItem.motionX + entityItem.motionZ * entityItem.motionZ) < 0.3D && entityItem.ticksExisted < 200) {
                entityItem.motionX = oldMotionX * 1.1D;
                entityItem.motionZ = oldMotionZ * 1.1D;
            }
            return flag;
        }
        return true;
    }

    private boolean handleRepulsionBehaviour(EntityItem entityItem, double oldMotionX, double oldMotionY, double oldMotionZ){
        int itemDamage = entityItem.getEntityItem().getItemDamage();
        if(itemDamage == ItemPlasticPlants.REPULSION_PLANT_DAMAGE) {
            if(oldMotionY < -0.2D) {
                entityItem.motionX = oldMotionX;
                entityItem.motionY = -oldMotionY;
                entityItem.motionZ = oldMotionZ;
                return false;
            } else if(oldMotionY > 0) {
                return false;
            }
        }
        return true;
    }

    private void spawnParticle(World world, String particleName, double spawnX, double spawnY, double spawnZ, double spawnMotX, double spawnMotY, double spawnMotZ){
        NetworkHandler.sendToAllAround(new PacketSpawnParticle(particleName, spawnX, spawnY, spawnZ, spawnMotX, spawnMotY, spawnMotZ), world);
    }

    public static class MotionProperties implements IExtendedEntityProperties{
        public double oldMotionX, oldMotionY, oldMotionZ;

        @Override
        public void saveNBTData(NBTTagCompound compound){}

        @Override
        public void loadNBTData(NBTTagCompound compound){}

        @Override
        public void init(Entity entity, World world){}

        public void update(Entity entity){
            oldMotionX = entity.posX - entity.prevPosX;
            oldMotionY = entity.posY - entity.prevPosY;
            oldMotionZ = entity.posZ - entity.prevPosZ;
        }

    }
}
