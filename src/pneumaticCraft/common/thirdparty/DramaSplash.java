package pneumaticCraft.common.thirdparty;

import java.lang.reflect.Method;

import pneumaticCraft.lib.Log;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;

/*
 * Implementation for Aphorism Tiles using Mr_okushama's DramaSplash mod
 */
public class DramaSplash extends Thread{
    private static Method getAsieSplashMethod;
    private static Object okuTickHandlerInstance;
    public static String cachedLine;
    private static boolean failed;

    public static void newDrama(){
        if(Loader.isModLoaded("drama") && !failed) {
            new DramaSplash().start();
        }
    }

    @Override
    public void run(){
        try {
            if(getAsieSplashMethod == null) {
                Class okuTickHandler = Class.forName("okushama.drama.TickHandlerClient");
                okuTickHandlerInstance = okuTickHandler.newInstance();
                getAsieSplashMethod = ReflectionHelper.findMethod(okuTickHandler, okuTickHandlerInstance, new String[]{"asieSplash"});
            }
            cachedLine = (String)getAsieSplashMethod.invoke(okuTickHandlerInstance);
        } catch(Exception e) {
            Log.warning("Reflection failed on the Drama Splash getter!");
            e.printStackTrace();
            failed = true;
        }
    }
}
