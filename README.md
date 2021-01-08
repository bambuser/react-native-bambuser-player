<div>
  <br/><br />
  <p align="center">
    <a href="https://bambuser.com" target="_blank" align="center">
        <img src="https://brand.bambuser.net/current/logo/bambuser-black-512.png" width="280">
    </a>
  </p>
  <br /><br />
  <h1>React Native component for Bambuser player SDK</h1>
</div>

## Requirements

* Version `0.0.10` - `0.0.18`of this React Native component requires version `0.9.22` - `0.9.31` of the iOS libbambuser SDK.
* Version `0.0.13` and above of this React Native component requires React Native `0.60` or higher.
* Version `0.0.19` of this React Native component requires version `0.9.28` - `0.9.31` of the iOS libbambuser SDK
* Version `0.0.19` and above of this React Native component requires version `0.9.21` of the Android libbambuser SDK.
* Version `0.0.20` and above of this React Native component requires version `1.0.0` or higher of the iOS libbambuser SDK.

## Installation

1. Add `react-native-bambuser-player` to your React Native project.  
    `$ yarn add react-native-bambuser-player --save`
2. Download Android SDK from https://dashboard.bambuser.com/developer
3. Android only: Head over to https://bambuser.com/docs/playback/android-player/ and follow the sections [Add the Bambuser SDK for Android](https://bambuser.com/docs/playback/android-player/#add-the-productname-sdk-for-android) and [Add required Android app permissions](https://bambuser.com/docs/playback/android-player/#add-required-android-app-permissions) for the Android project within your React Native project.
    **Important note:** when adding the libbambuser subproject, make sure you name it: **libbambuser**, the React Native component will look for this subproject when building your React Native app.
4. iOS only: Go to https://bambuser.com/docs/playback/ios-player/ and follow the sections [Installing dependencies using CocoaPods](https://bambuser.com/docs/playback/ios-player/#installing-dependencies-using-cocoapods).


### To automatically link this React Native module to your Xcode/Android projects run the following command:

If you are using React Native `0.60` or above run: `$ cd ios && pod install && cd ..`.
If you are using an older version of React Native please refer back to README.md in this repository for specific version.


### If you want to manually add this React Native module to your Xcode/Android projects, then follow these steps:


For Xcode:
1. In Xcode, in the project navigator, right click the `Libraries` directory, and choose `Add Files to your [your projects name]`.
2. Go to `node_modules` ➜ `react-native-bambuser-player` and add `RNBambuserPlayer.xcodeproj`.
3. In Xcode, in the project navigator, select your project. Add `libRNBambuserPlayer.a` to your projects `Build Phases` ➜ `Link Binary With Libraries`.


For Android:
1. Open up `android/app/src/main/java/.../MainActivity.java`.
2. Add `import com.bambuserplayer.BambuserPlayerViewPackage` to the imports at the top.
3. Add `new BambuserPlayerViewPackage()` to the return array in the `getPackages()` method. Your `getPackages()` method should look something like this:  
    ```java
    ...
    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new BambuserPlayerViewPackage()
        );
    }
    ...
    ```

4. Add the following lines to `android/settings.gradle`:  
    ```
    include ':react-native-bambuser-player'
    project(':react-native-bambuser-player').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bambuser-player/android')
    ```

5. Add the following lines inside the dependencies block in `android/app/build.gradle`:  
    ```
    compile project(':react-native-bambuser-player')
    ```


## Usage

In order to use this React Native component and our SDKs you'll need to generate an `applicationId` over at https://dashboard.bambuser.com/developer.

```javascript
import RNBambuserPlayer from 'react-native-bambuser-player';

...
<RNBambuserPlayer applicationId={yourApplicationId} />
```

### Props
```javascript
applicationId: String
// This applicationId should be fetched from your backend, rather than being hardcoded within your React Native app. Read more here https://bambuser.com/docs/key-concepts/application-id/

resourceUri: String
// The uri to the resource you want this player to play. Read more here https://bambuser.com/docs/key-concepts/resource-uri/. In order to play a new resourceUri after the component has been mounted the function .stop() has to be called on this player object.

timeShiftMode: Boolean
// Enable this if you want be able to seek in a live broadcast.

volume: Number
// Set the desired player volume.

videoScaleMode: String
// RNBambuserPlayer.VIDEO_SCALE_MODE.ASPECT_FIT || RNBambuserPlayer.VIDEO_SCALE_MODE.ASPECT_FILL || RNBambuserPlayer.VIDEO_SCALE_MODE.SCALE_TO_FILL

ignoreSilentSwitch: String
// RNBambuserPlayer.IGNORE_SILENT_SWITCH.INHERIT || RNBambuserPlayer.IGNORE_SILENT_SWITCH.IGNORE || RNBambuserPlayer.IGNORE_SILENT_SWITCH.OBEY

requiredBroadcastState: String
// The player will only play this broadcast state.
// RNBambuserPlayer.REQUIRED_BROADCAST_STATE.ANY || RNBambuserPlayer.REQUIRED_BROADCAST_STATE.LIVE || RNBambuserPlayer.REQUIRED_BROADCAST_STATE.ARCHIVED

latencyMode: String
// Set whether the player should prioritize low latency or playback with less interruptions.
// RNBambuserPlayer.LATENCY_MODE.LOW || RNBambuserPlayer.LATENCY_MODE.HIGH

onCurrentViewerCountUpdate: function(viewers)
// Callback when current viewer count is updated.

onTotalViewerCountUpdate: function(viewers)
// Callback when total viewer count is updated.

onReady: function()
// Callback when the player is available and can accept inputs, such as playing a video.

onProgressUpdate: function(live, currentPosition, duration)
// Callback with arguments which can be used to present information about the current video playback. This is called continuously while the video is playing.

onLoading: function()
// Callback when a video is being loaded.

onPlaying: function()
// Callback when the player started to play the video.

onPaused: function()
// Callback when the player become paused.

onPlaybackComplete: function()
// Callback when the player reaches the end of the video.

onStopped: function()
// Callback when the player is stopped.

onPlaybackError: function()
// Callback when the player fails to play the video.
```


### Controlling the player
By storing a reference to the RNBambuserPlayer you can call its functions.

```javascript
<RNBambuserPlayer
ref={ref => {this.myPlayerRef = ref; }} applicationId={yourApplicationId} />
```

The available functions for RNBambuserPlayer which can be called are:
```javascript
this.myPlayerRef.play();
// Call this to play the latest resourceUri video. If you want to play a new resourceUri on this same player instance this.myPlayerRef.stop() has to be called first.

this.myPlayerRef.pause();
// Call this to pause the video.

this.myPlayerRef.stop();
// Call this to stop the current video.

this.myPlayerRef.seekTo(position);
// Call this to seek to desired position in the video.
```

## More information

* [Bambuser Docs](https://bambuser.com/docs)

* [Bambuser AB](https://bambuser.com)
