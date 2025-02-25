# Noisefield

This is Noisefield (aka. Bubbles) which was made around Android JB/ICS and originally contained RenderScript (the OpenGL abstraction that Google chose at the time).

This app is near and dear to my heart having used it for the better part of 10 years or so.

Recently with the purchase of a new Android phone I couldn't for the life of me find any precompiled `noisefield.apk` that targeted Android API 21 or newer. My device just wouldn't accept this old RenderScript based app due to missing some missing ABIs.

After a lot of bashing my head against the wall with OpenGL ES2.0 and Android Livewallpaper creation in the Android Studio IDE, I have finally finished it and it bring me joy to keep using this wallpaper for another 10years or more.

As the original is available at the [Android git](https://android.googlesource.com/platform/packages/wallpapers/NoiseField/+/94eec8049435f00040effa703683db0610224447) I chose to make a repo and convert it so I can just hit recompile if ever needed.

Right now the configuring is minimum API 9 (Gingerbread 2.3.0 – 2.3.2) with target API 35 (Vanilla Ice Cream 15) (way more range than the original).
I only was able to test it with API 10 (Gingerbread (MR1) 2.3.3 - 2.3.7) and there may also be context switching issues as GLES context preservation is only available since Honeycomb (Android 3.0).

It should run on basically any Android phone out there as I explicitly let it build with all architectures (at least I think that what the setting in gradle does).

## Compilation

Just get the official Android Studio IDE and install the API level 35 SDK.
You may need to install Java 17

## Contributing

I'd love to try and optimize it a bit more but I am no Java or OpenGL man.

As I barely understand why the current code works or what kind of noise was used and how to make it maybe a bit more random (currently the bubbles sometimes all follow the same path), I'd love some contributions.

Please make sure if you want to contribute to test debug `.apk`'s in a local device emulator first.

## License

[Apache2.0](https://choosealicense.com/licenses/apache-2.0/)
