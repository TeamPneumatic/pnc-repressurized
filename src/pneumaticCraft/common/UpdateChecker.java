package pneumaticCraft.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Versions;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class UpdateChecker extends Thread{

    private static UpdateChecker INSTANCE = new UpdateChecker();
    private final String url = "http://www.minemaarten.com/downloads/pneumaticcraft-changelog/pneumaticcraft-";
    private boolean doneChecking;
    private boolean hasNewVersion;
    private final List<String> additions = new ArrayList<String>();
    private final List<String> apiChanges = new ArrayList<String>();
    private final List<String> bugfixes = new ArrayList<String>();
    private String latestVersion, updateSize;
    private int displayDelay = 200;//build in a delay, as many mods display it on right away, I'll wait a few seconds until their spam is gone (A).

    public static UpdateChecker instance(){
        return INSTANCE;
    }

    @Override
    public void run(){
        checkForUpdate();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.END && event.player.worldObj.isRemote) {
            if(displayDelay > 0) displayDelay--;
            if(doneChecking && displayDelay == 0) {
                if(hasNewVersion) {
                    sendMessage(event.player, "There's a new " + updateSize + " version of PneumaticCraft available: version " + latestVersion + " (you are running " + Versions.fullVersionString() + ").");
                    sendMessage(event.player, "Use '/pc changelog' to see what has changed.");
                }
                FMLCommonHandler.instance().bus().unregister(this);
            }
        }
    }

    private static void sendMessage(ICommandSender player, List<String> message){
        for(String s : message) {
            sendMessage(player, s);
        }
    }

    private static void sendMessage(ICommandSender player, String message){
        player.addChatMessage(new ChatComponentTranslation(message));
    }

    private void checkForUpdate(){
        int versionNumbers = 3;
        latestVersion = Versions.fullVersionString();
        int changeSize = 10000;

        try {
            getPage(url + latestVersion.replace('.', '-') + "/");
            int curDigit = versionNumbers - 1;
            while(curDigit >= 0) {
                try {
                    String checkingVersion = incVersion(latestVersion, curDigit);
                    String totalText = getPage(url + checkingVersion.replace('.', '-') + "/");
                    if(totalText.contains("It looks like nothing was found at this location. Maybe try a search?")) {
                        curDigit--;
                    } else {
                        //  String totalText = new System.Net.WebClient().DownloadString(Constants.URL + checkingVersion.Replace('.', '-') + "/");
                        String[] text = totalText.replace("<br />", "").replace("<p>", "").replace("</p>", "").replace("\t", "").replace("</a>", "").replace("&#8217;", "'").replace("&#8216;", "'").replace("&#8230;", "...").split(System.getProperty("line.separator"));
                        boolean parsingContent = false;
                        for(String line : text) {
                            if(line.contains("entry-content")) {
                                if(parsingContent) {
                                    break;
                                } else {
                                    parsingContent = true;
                                }
                            } else if(parsingContent) {
                                if(line.startsWith("-Bugfix")) {
                                    bugfixes.add(line);
                                } else if(line.startsWith("-API")) {
                                    apiChanges.add(line);
                                } else {
                                    additions.add(line);
                                }
                            }
                        }
                        latestVersion = checkingVersion;
                        changeSize = changeSize > curDigit ? curDigit : changeSize;
                        curDigit = versionNumbers - 1;
                    }
                } catch(IOException e) {
                    curDigit--;
                }
            }

            if(changeSize < 10000) {
                updateSize = new String[]{"Massive", "Major", "Minor"}[changeSize];
                Log.info("New " + updateSize + " update available: " + latestVersion);
                hasNewVersion = true;
            } else {
                Log.info("Up to date!");
            }
            doneChecking = true;
        } catch(IOException e) {
            Log.error("The URL of the current version changed, the current running version is weirdly formatted (dev version?), minemaarten.com is down or there is a problem with your internet connection.");
        }
    }

    private String incVersion(String version, int digit){
        String[] numStrings = version.split("\\.");
        int[] nums = new int[numStrings.length];
        for(int i = 0; i < nums.length; i++) {
            nums[i] = Integer.parseInt(numStrings[i]);
        }
        nums[digit]++;
        for(int i = digit + 1; digit != 0 && i < nums.length; i++) {
            nums[i] = 0;
        }
        String newVersion = Integer.toString(nums[0]);
        for(int i = 1; i < nums.length; i++) {
            newVersion += "." + nums[i];
        }
        return newVersion;
    }

    public static String getPage(final String URL) throws IOException{
        String line = "", all = "";
        URL myUrl = null;
        BufferedReader in = null;
        try {
            myUrl = new URL(URL);
            in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
            while((line = in.readLine()) != null) {
                all += line + System.getProperty("line.separator");
            }
        } finally {
            if(in != null) {
                in.close();
            }
        }

        return all;
    }

    public static class CommandChangelog extends CommandBase{

        @Override
        public String getCommandName(){
            return "pc";
        }

        @Override
        public int getRequiredPermissionLevel(){
            return -100;//everyone can use it
        }

        @Override
        public String getCommandUsage(ICommandSender icommandsender){
            return "/pc changelog [<additions/apichanges/bugfixes/true/false>]";
        }

        @Override
        public void processCommand(ICommandSender icommandsender, String[] astring){
            if(astring.length > 0 && astring[0].equalsIgnoreCase("changelog")) {
                if(astring.length > 1) {
                    if(astring[1].equalsIgnoreCase("additions")) {
                        if(instance().additions.size() > 0) {
                            sendMessage(icommandsender, "-----Additions-----");
                            sendMessage(icommandsender, instance().additions);
                        } else {
                            sendMessage(icommandsender, "No additions in the newest PneumaticCraft. :(");
                        }
                    } else if(astring[1].equalsIgnoreCase("apichanges")) {
                        if(instance().apiChanges.size() > 0) {
                            sendMessage(icommandsender, "-----API Changes-----");
                            sendMessage(icommandsender, instance().apiChanges);
                        } else {
                            sendMessage(icommandsender, "No API changes in the newest PneumaticCraft.");
                        }
                    } else if(astring[1].equalsIgnoreCase("bugfixes")) {
                        if(instance().bugfixes.size() > 0) {
                            sendMessage(icommandsender, "-----Bugfixes-----");
                            sendMessage(icommandsender, instance().bugfixes);
                        } else {
                            sendMessage(icommandsender, "No bugfixes in the newest PneumaticCraft.");
                        }
                    } else if(astring[1].equalsIgnoreCase("true")) {
                        sendMessage(icommandsender, "You'll now get a notification when a new version of PneumaticCraft is released. Checking now...");
                        Config.config.get(Configuration.CATEGORY_GENERAL, "Enable Update Checker", true).set(true);
                        Config.config.save();
                        //if(Config.enableUpdateChecker) FMLCommonHandler.instance().bus().unregister(instance()); causes NPE for some reason...
                        INSTANCE = new UpdateChecker();//reset variables, prevent thread from running twice.
                        instance().displayDelay = 0;
                        FMLCommonHandler.instance().bus().register(instance());
                        instance().start();
                        Config.enableUpdateChecker = true;
                    } else if(astring[1].equalsIgnoreCase("false")) {
                        sendMessage(icommandsender, "You'll no longer get a notification when there's a new version of PneumaticCraft available.");
                        Config.config.get(Configuration.CATEGORY_GENERAL, "Enable Update Checker", true).set(false);
                        Config.config.save();
                        //   if(Config.enableUpdateChecker) FMLCommonHandler.instance().bus().unregister(instance());//save cpu time by unsubscribing to the tickevent.  //causes NPE for some reason.
                        Config.enableUpdateChecker = false;
                    } else {
                        throw new WrongUsageException(getCommandUsage(icommandsender));
                    }
                } else {
                    if(instance().additions.size() > 0) {
                        sendMessage(icommandsender, "-----Additions-----");
                        sendMessage(icommandsender, instance().additions);
                    }
                    if(instance().apiChanges.size() > 0) {
                        sendMessage(icommandsender, "-----API Changes-----");
                        sendMessage(icommandsender, instance().apiChanges);
                    }
                    if(instance().bugfixes.size() > 0) {
                        sendMessage(icommandsender, "-----Bugfixes-----");
                        sendMessage(icommandsender, instance().bugfixes);
                    }
                }
            } else {
                throw new WrongUsageException(getCommandUsage(icommandsender));
            }
        }
    }
}
