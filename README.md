PneumaticCraft
==================

This is the source of PneumaticCraft. It's licenced under GPLv3, so have fun. Also have fun learning from the code where possible!
I'm open to pull requests.

If you have any questions about the source, you can bug me on IRC, in #PneumaticCraft on Espernet.

I know parts of the code still need refactoring, notably TileEntity rendering.

=======
Developping with PneumaticCraft
=======
If you want to use the PneumaticCraft API in your mod, it's really easy to include the mod or API to your development environment, as the mod has a maven.

In your build.gradle, add:

	repositories {
		maven {
			name = "MM repo"
			url = "http://maven.k-4u.nl/"
		}
	}

	dependencies{
		//If you want to load the whole source, use this line
		compile "pneumaticCraft:PneumaticCraft-1.7.10:1.2.6-6:userdev"
		
		//If you want to load the PneumaticCraft API only, use this line
		compile "pneumaticCraft:PneumaticCraft-1.7.10:1.2.6-6:api"
	}

It should be clear that the version number used in the 'compile' is an example, to see which versions you can use, go to http://maven.k-4u.nl/pneumaticCraft/PneumaticCraft-1.7.10/

=======
Contributing to PneumaticCraft
=======
If you're planning to contribute to the PneumaticCraft's mods source, the best thing you can do is fork this github repository, and run 'gradle setupDecompWorkspace idea/eclipse' on the build.gradle file in this repository.

After you've made changes, do a pull request :)

For more details on pull-requests see [link](https://help.github.com/articles/using-pull-requests/)

For more details on how to get PneumaticCraft up and running in Eclipse, see [link](CONTRIBUTING-ECLIPSE.md)
