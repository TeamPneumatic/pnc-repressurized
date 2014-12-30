Contributing using Eclipse
==================
a HowTo by [DarkStarDS9](https://github.com/DarkStarDS9)

If you're not quite up to speed with Eclipse, Gradle or Forge, this should get you started.


=======
Eclipse
=======

As of this writing, the current version is Eclipse Luna 4.4.1.
Go to [link](https://www.eclipse.org/downloads/) and download "Eclipse IDE for Java Developers". Install it.


=======
Forge
=======

Get the latest recommended version from [link](http://files.minecraftforge.net) for the Minecraft-Version that the mod is for. Right now, that's 1.7.10 so we'll download 1.7.10-Recommended using the scr link. Unpack it.

Make a directory somewhere that will house Forge and all the mods you're going to work on. I named mine Git, because the mods I'm interested in are on Git. Move the unpacked Forge folder there. Since I'm on Darwin and the Git-folder is in my home directory, this leaves me with the following path: ~/Git/forge-1.7.10-10.13.2.1230-src

Using your favorite shell, cd to the forge directory and run ./gradlew setupDecompWorkspace eclipse
This will download all dependencies and create a decompiled workspace, ready for Eclipse.
Delete the src/main/java subfolder to get rid of the example-mod.

Move the eclipse folder (this is a pre-created workspace) somewhere else, it must not be anywhere below the Forge folder. I moved mine to the Git folder, so at this state my Git folder has two subfolders: forge-1.7.10-10.13.2.1230-src and eclipse.

Start Eclipse. When prompted for a workspace, point eclipse to the folder you just moved - in this example ~/Git/eclipse - note that you can't actually type this in the dialog (at least on Mac OS X) because Eclipse doesn't seem to know that ~ stands for your home-directory. So instead, click browse and go there manually. Oh boy!

If everything is ok, you'll be greeted with a workspace that has one project: Minecraft.

Check if everything is working so far: click on the run icon and select "Client". Minecraft should launch and let you know that there are 3 mods loaded. Great, now quit Minecraft and open your web-browser.


=======
PneumaticCraft
=======

Go to GitHub.com, fork and clone PneumaticCraft (see [link](https://help.github.com/articles/using-pull-requests/)) and clone your fork to a local directory. I'm using ~/Git/PneumaticCraft.

In your shell, cd there and call gradlew from your forge-directory:
../forge-1.7.10-10.13.2.1230-src/gradlew eclipse
this will setup a Eclipse-project for PneumaticCraft. Switch to Eclipse, click File/Import in the menu, select General/Existing Projects into Workspace, click next and browse for your PneumaticCraft folder. It should find a project. Click Finish.

Since PneumaticCraft uses NEI, and NEI uses something called Access Transformers (making some stuff from Minecraft public which would otherwise be protected/private) you now have to do some more tinkering with Forge, or Minecraft will crash as soon as you enter a world. See [link](http://www.minecraftforge.net/wiki/Using_Access_Transformers) if you want to know more, or simply do this:

First, you add the folder META-INF to forge-1.7.10-10.13.2.1230-src/src/main/resources/. Then you find NEI_at.cfg - I grabbed it from [link](https://raw.githubusercontent.com/Chicken-Bones/NotEnoughItems/master/resources/nei_at.cfg) - and put it there.

In your shell, cd to the Forge folder and run "./gradlew setupDecompWorkspace eclipse" again. You should see "Found AccessTransformer in main resources: nei_at.cfg" flash by early on.

Now there are two ways to continue: You can either add Minecraft to PneumaticCraft's build path, which will allow you to run PneumaticCraft (+ required Mods) without other mods you are working on, or you do it the other way around and add PneumaticCraft (and later on all the other mods you are working on) to Minecraft's Build Path. Pick your poison :)


Add Minecraft to PneumaticCraft's build path
--------------------------------------------

To do this, right-click the PneumaticCraft project and select Build Path/Configure Build Path. On the list to the left, Java Build Path should be selected. On the right side, click Projects. Add Minecraft. In Order and Export, scroll all the way down, select Minecraft and click Top. Click OK.

We now have to copy Forge's Run-Configuration to run PneumaticCraft. Go to Run/Run Configurations. We'll use the first "Client" one which is listed in "Java Application" - copy it, rename it to PneumaticCraft, and also select PneumaticCraft as the project (on the Main tab).


Add PneumaticCraft to Minecraft's build path
--------------------------------------------
To do this, right-click the Minecraft project and select Build Path/Configure Build Path. On the list to the left, Java Build Path should be selected. On the right side, click Projects. Add PneumaticCraft. Click OK.


=======
Wrapping Up
=======

Whatever way you chose, you should now be able to select your run-Configuration when you click the little arrow on the run-button. Do this now - you should now have 17 mods loaded. Create a new world. Have fun :o)

Thanks to skyboy (Thermal Expansion) who pointed me in the right direction when I first tried to get a mod to compile and run.