Android program to control a SqueezeBox from your phone via SqueezeServer.

Current (semi) implemented features include:

* Choose/switch player to control
* Browse and add music by artist/album/song/genre/new music
* View album art/song info with animated display
* View/remove artist/album/song from playlist
* Sync players
* Search
* Application support
* Folder browsing
* Download support
* Playback control (pause,next,back,shuffle,repeat,volume,seek)

This is still in early development, it can/will break you phone, burn down your house, etc (but probably just won't work in the worst possible case). Patches/bug reports/better graphics/suggestions are much welcomed.

You can install from the android market by searching for 'SqueezeDroid'

BUILDING

Squeezedroid uses maven as the build system. To build from source:

* Download and install the android sdk following the instructions at http://developer.android.com/sdk/installing.html. You don't need the adt, but you will need to install at least one SDK platform.
* Download and install maven2 following the instructions at http://maven.apache.org/.  If you're using a macintosh, it's already installed.
* type ANDROID_HOME=&lt;PATH/TO/ANDROID/SDK&gt; mvn package.  This will an apk in the target folder.


You're on your own as far as IDE support goes.  For intellij,  you can simply import the pom.xml as an external model, configure the android sdk and you're good to go.