# CTRMap
A world editor for the Nintendo 3DS Generation 6 Pokémon games.

![CTRMapPreview](https://user-images.githubusercontent.com/20842714/63652270-91e3f600-c75e-11e9-9131-74c4f1a65c2e.png)

## Current features
- Edit tilemap move permissions
- Edit and create collision mesh data
- Edit and create world props
- Edit and create 3D camera objects
- Edit and add new NPCs to zones

## 3D model editing and conversion
You can add new 3D models to the Pokémon world with [SPICA](https://github.com/HelloOO7/SPICA).
This is my fork with some weird hacks that somehow make the models not crash the game.
Don't ask me why it even works. SPICA is made by gdkchan who has left the project to work on some
way cooler stuff, so don't go into the issue tracker and harass them please, okay?

## Building CTRMap
1. Clone the repo and import the project into a Java IDE of choice. Netbeans is recommended as it's the only one that can
edit the GUI forms.
2. Download and extract JogAmp from https://jogamp.org/deployment/jogamp-current/archive/jogamp-all-platforms.7z.
3. Copy gluegen-rt.jar, jogl-all.jar and the \*native\*.jar from the jars directory to \<project root\>/lib.
4. Check build path configuration to ensure that the jars are correctly linked.
5. Click the build/run button to just compile the classes or run the project.
6. If you want to export a JAR file, open the Files tab in netbeans, right click build.xml, select
Run Target > Other Targets > package-for-store. Your jar should be in store/CTRMap.jar.

## Bug reporting
You are welcome to report any bugs in the [issue tracker](https://github.com/HelloOO7/CTRMap/issues). Be sure to read the guidelines first.
