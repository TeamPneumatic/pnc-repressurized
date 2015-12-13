package pneumaticCraft.common.minigun;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.render.RenderProgressingLine;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemGunAmmo;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Sounds;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class Minigun{
    private final boolean requiresTarget;
    private final double raytraceRange = 50;

    private double minigunSpeed;
    private int minigunTriggerTimeOut;
    public static final double MAX_GUN_SPEED = 0.4;
    private static final double MAX_GUN_YAW_CHANGE = 10;
    private static final double MAX_GUN_PITCH_CHANGE = 10;
    private int minigunSoundCounter = -1;
    private final Random rand = new Random();
    private double minigunRotation, oldMinigunRotation;
    public double minigunYaw, oldMinigunYaw;
    public double minigunPitch, oldMinigunPitch;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);
    private boolean sweeping; //When true, the yaw of the minigun will sweep with a sinus pattern when not targeting.
    private double sweepingProgress;

    private boolean gunAimedAtTarget;

    protected IPressurizable pressurizable;
    private int airUsage;
    protected ItemStack stack, ammo;
    protected EntityPlayer player;
    protected World world;
    protected EntityLivingBase attackTarget;

    public Minigun(boolean requiresTarget){
        this.requiresTarget = requiresTarget;
    }

    public Minigun setPressurizable(IPressurizable pressurizable, int airUsage){
        this.pressurizable = pressurizable;
        this.airUsage = airUsage;
        return this;
    }

    public Minigun setItemStack(ItemStack stack){
        this.stack = stack;
        return this;
    }

    public Minigun setAmmo(ItemStack ammo){
        this.ammo = ammo;
        return this;
    }

    public Minigun setPlayer(EntityPlayer player){
        this.player = player;
        return this;
    }

    public Minigun setWorld(World world){
        this.world = world;
        return this;
    }

    public Minigun setAttackTarget(EntityLivingBase entity){
        attackTarget = entity;
        return this;
    }

    public abstract boolean isMinigunActivated();

    public abstract void setMinigunActivated(boolean activated);

    public abstract void setAmmoColorStack(ItemStack ammo);

    public abstract int getAmmoColor();

    public abstract void playSound(String soundName, float volume, float pitch);

    protected int getAmmoColor(ItemStack stack){
        return stack != null ? stack.getItem().getColorFromItemStack(stack, 1) : 0xFF313131;
    }

    public double getMinigunSpeed(){
        return minigunSpeed;
    }

    public void setMinigunSpeed(double minigunSpeed){
        this.minigunSpeed = minigunSpeed;
    }

    public int getMinigunTriggerTimeOut(){
        return minigunTriggerTimeOut;
    }

    public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut){
        this.minigunTriggerTimeOut = minigunTriggerTimeOut;
    }

    public int getMinigunSoundCounter(){
        return minigunSoundCounter;
    }

    public void setMinigunSoundCounter(int minigunSoundCounter){
        this.minigunSoundCounter = minigunSoundCounter;
    }

    public double getMinigunRotation(){
        return minigunRotation;
    }

    public void setMinigunRotation(double minigunRotation){
        this.minigunRotation = minigunRotation;
    }

    public double getOldMinigunRotation(){
        return oldMinigunRotation;
    }

    public void setOldMinigunRotation(double oldMinigunRotation){
        this.oldMinigunRotation = oldMinigunRotation;
    }

    public EntityLivingBase getAttackTarget(){
        return attackTarget;
    }

    public void setSweeping(boolean sweeping){
        this.sweeping = sweeping;
    }

    public boolean isSweeping(){
        return sweeping;
    }

    public boolean tryFireMinigun(EntityLivingBase target){
        boolean lastShotOfAmmo = false;
        if(ammo != null && (pressurizable == null || pressurizable.getPressure(stack) > 0)) {
            setMinigunTriggerTimeOut(Math.max(10, getMinigunSoundCounter()));
            if(getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget)) {
                if(!requiresTarget) target = raytraceTarget();
                lastShotOfAmmo = ammo.attemptDamageItem(1, rand);
                if(pressurizable != null) pressurizable.addAir(stack, -airUsage);
                if(target != null) {
                    ItemStack potion = ItemGunAmmo.getPotion(ammo);
                    if(potion != null) {
                        if(rand.nextInt(20) == 0) {
                            List<PotionEffect> effects = Items.potionitem.getEffects(potion);
                            if(effects != null) {
                                for(PotionEffect effect : effects) {
                                    target.addPotionEffect(new PotionEffect(effect));
                                }
                            }
                        }
                    } else {
                        target.attackEntityFrom(DamageSource.causePlayerDamage(player), Config.configMinigunDamage);
                    }
                }
            }
        }
        return lastShotOfAmmo;
    }

    private EntityLivingBase raytraceTarget(){
        MovingObjectPosition mop = PneumaticCraftUtils.getMouseOverServer(player, raytraceRange);
        return mop != null && mop.entityHit instanceof EntityLivingBase ? (EntityLivingBase)mop.entityHit : null;
    }

    public void update(double posX, double posY, double posZ){
        setOldMinigunRotation(getMinigunRotation());
        oldMinigunYaw = minigunYaw;
        oldMinigunPitch = minigunPitch;
        if(attackTarget != null && attackTarget.isDead) attackTarget = null;
        if(!world.isRemote) {
            setMinigunActivated(getMinigunTriggerTimeOut() > 0);

            setAmmoColorStack(ammo);

            if(getMinigunTriggerTimeOut() > 0) {
                setMinigunTriggerTimeOut(getMinigunTriggerTimeOut() - 1);
                if(getMinigunSpeed() == 0) {
                    playSound(Sounds.HUD_INIT, 2, 0.9F);
                }
            }
            if(getMinigunSoundCounter() == 0 && getMinigunTriggerTimeOut() == 0) {
                playSound(Sounds.MINIGUN_STOP, 3, 0.5F);
                setMinigunSoundCounter(-1);
            }
        }
        if(isMinigunActivated()) {
            setMinigunSpeed(Math.min(getMinigunSpeed() + 0.01D, MAX_GUN_SPEED));
        } else {
            setMinigunSpeed(Math.max(0, getMinigunSpeed() - 0.003D));
        }

        setMinigunRotation(getMinigunRotation() + getMinigunSpeed());

        double targetYaw = 0;
        double targetPitch = 0;
        if(attackTarget != null) {
            double deltaX = posX - attackTarget.posX;
            double deltaZ = posZ - attackTarget.posZ;

            if(deltaX >= 0 && deltaZ < 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D;
            } else if(deltaX >= 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 90;
            } else if(deltaX < 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D + 180;
            } else {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 270;
            }
            if(targetYaw - minigunYaw > 180) {
                targetYaw -= 360;
            } else if(minigunYaw - targetYaw > 180) {
                targetYaw += 360;
            }
            targetPitch = Math.toDegrees(Math.atan((posY - attackTarget.posY - attackTarget.height / 2) / PneumaticCraftUtils.distBetween(posX, posZ, attackTarget.posX, attackTarget.posZ)));

            if(minigunPitch > targetPitch) {
                if(minigunPitch - MAX_GUN_PITCH_CHANGE > targetPitch) {
                    minigunPitch -= MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            } else {
                if(minigunPitch + MAX_GUN_PITCH_CHANGE < targetPitch) {
                    minigunPitch += MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            }

            if(minigunPitch < -80 || minigunPitch > 80) {
                minigunYaw = targetYaw;
            } else {
                if(minigunYaw > targetYaw) {
                    if(minigunYaw - MAX_GUN_YAW_CHANGE > targetYaw) {
                        minigunYaw -= MAX_GUN_YAW_CHANGE;
                    } else {
                        minigunYaw = targetYaw;
                    }
                } else {
                    if(minigunYaw + MAX_GUN_YAW_CHANGE < targetYaw) {
                        minigunYaw += MAX_GUN_YAW_CHANGE;
                    } else {
                        minigunYaw = targetYaw;
                    }
                }
            }
            gunAimedAtTarget = minigunYaw == targetYaw && minigunPitch == targetPitch;
        } else if(isSweeping()) {
            minigunYaw -= Math.cos(sweepingProgress) * 22;
            sweepingProgress += 0.05D;
            minigunYaw += Math.cos(sweepingProgress) * 22;

            if(minigunPitch > targetPitch) {
                if(minigunPitch - MAX_GUN_PITCH_CHANGE > targetPitch) {
                    minigunPitch -= MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            } else {
                if(minigunPitch + MAX_GUN_PITCH_CHANGE < targetPitch) {
                    minigunPitch += MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            }
        }

        if(!world.isRemote && isMinigunActivated() && getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget && attackTarget != null)) {
            if(getMinigunSoundCounter() <= 0) {
                playSound(Sounds.MINIGUN, 0.3F, 1);
                setMinigunSoundCounter(20);
            }
        }
        if(getMinigunSoundCounter() > 0) setMinigunSoundCounter(getMinigunSoundCounter() - 1);
    }

    @SideOnly(Side.CLIENT)
    public void render(double x, double y, double z, double gunRadius){
        if(isMinigunActivated() && getMinigunSpeed() == MAX_GUN_SPEED && gunAimedAtTarget && attackTarget != null) {
            GL11.glPushMatrix();
            GL11.glScaled(1, 1, 1);
            GL11.glTranslated(-x, -y, -z);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            //GL11.glDisable(GL11.GL_LIGHTING);
            RenderUtils.glColorHex(0xFF000000 | getAmmoColor());
            for(int i = 0; i < 5; i++) {

                Vec3 vec = Vec3.createVectorHelper(attackTarget.posX - x, attackTarget.posY - y, attackTarget.posZ - z).normalize();
                minigunFire.startX = x + vec.xCoord * gunRadius;
                minigunFire.startY = y + vec.yCoord * gunRadius;
                minigunFire.startZ = z + vec.zCoord * gunRadius;
                minigunFire.endX = attackTarget.posX + rand.nextDouble() - 0.5;
                minigunFire.endY = attackTarget.posY + attackTarget.height / 2 + rand.nextDouble() - 0.5;
                minigunFire.endZ = attackTarget.posZ + rand.nextDouble() - 0.5;
                minigunFire.render();
            }
            GL11.glColor4d(1, 1, 1, 1);
            // GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }
    }
}
