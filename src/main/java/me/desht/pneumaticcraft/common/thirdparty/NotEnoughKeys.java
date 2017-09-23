package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraftforge.common.MinecraftForge;

public class NotEnoughKeys implements IThirdParty {

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /*   @SubscribeEvent TODO NEK dep
       public void onKey(KeyBindingPressedEvent event){
           KeyHandler.getInstance().onKey(event.keyBinding);
       }*/

    @Override
    public void init() {

    }

    @Override
    public void postInit() {
    }

    @Override
    public void clientSide() {
    }

    @Override
    public void clientInit() {
        //TODO NEK dep       Api.registerMod(Names.MOD_ID, new String[]{KeyHandler.getInstance().keybindHack.getKeyDescription(), KeyHandler.getInstance().keybindDebuggingDrone.getKeyDescription(), KeyHandler.getInstance().keybindOpenOptions.getKeyDescription()});
    }

}
