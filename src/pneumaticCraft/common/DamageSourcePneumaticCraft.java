package pneumaticCraft.common;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class DamageSourcePneumaticCraft extends DamageSource{
    public static final DamageSourcePneumaticCraft pressure = (DamageSourcePneumaticCraft)new DamageSourcePneumaticCraft("pressure", 2).setDamageBypassesArmor();
    public static final DamageSourcePneumaticCraft etchingAcid = new DamageSourcePneumaticCraft("acid", 2);
    public static final DamageSourcePneumaticCraft securityStation = (DamageSourcePneumaticCraft)new DamageSourcePneumaticCraft("securityStation").setDamageBypassesArmor();

    private int deathMessages = 0;

    public DamageSourcePneumaticCraft(String damageType, int messages){
        this(damageType);
        deathMessages = 2;
    }

    public DamageSourcePneumaticCraft(String damageType){
        super(damageType);
        deathMessages = 1;
    }

    @Override
    public DamageSource setDamageBypassesArmor(){
        return super.setDamageBypassesArmor();
    }

    @Override
    public DamageSource setDamageAllowedInCreativeMode(){
        return super.setDamageAllowedInCreativeMode();
    }

    @Override
    public DamageSource setFireDamage(){
        return super.setFireDamage();
    }

    /**
     * Returns the message to be displayed on player death.
     */
    @Override
    public IChatComponent func_151519_b(EntityLivingBase par1EntityLivingBase){
        String messageMeta = "";
        int messageNumber = par1EntityLivingBase.getRNG().nextInt(deathMessages) + 1;
        messageMeta = messageNumber + "";

        EntityLivingBase entitylivingbase1 = par1EntityLivingBase.func_94060_bK();
        String s = "death.attack." + damageType + messageMeta;
        String s1 = s + ".player";
        return entitylivingbase1 != null && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, new Object[]{par1EntityLivingBase.func_145748_c_(), entitylivingbase1.func_145748_c_()}) : new ChatComponentTranslation(s, new Object[]{par1EntityLivingBase.func_145748_c_()});
    }
}
