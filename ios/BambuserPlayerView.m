/**
 * Copyright Bambuser AB 2018
 */

#import "BambuserPlayerView.h"
#import <BambuserPlayer/BambuserPlayer.h>

@interface BambuserPlayerView() <BambuserPlayerDelegate>
@end

@implementation BambuserPlayerView {
  BambuserPlayer *bambuserPlayer;
  NSTimer *progressTimer;
  BOOL viewReady;
}

@synthesize resourceUri, applicatonId, videoScaleMode, ignoreSilentSwitch, requiredBroadcastState, timeShiftMode, volume, duration, seekTo, play, pause, stop, latencyMode;

-(instancetype)init {
  self = [super init];
  if (self) {
    viewReady = NO;
    videoScaleMode = VideoScaleAspectFit;
    ignoreSilentSwitch = @"inherit";
    requiredBroadcastState = kBambuserBroadcastStateAny;
    timeShiftMode = NO;
    volume = .5f;
    duration = -1;
    latencyMode = @"low";
    bambuserPlayer = [[BambuserPlayer alloc] init];
    bambuserPlayer.delegate = self;
    [self addSubview:bambuserPlayer];
  }
  return self;
}

-(void)startTimer {
  progressTimer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(sendOnProgressUpdate) userInfo:nil repeats:true];
}

-(void)stopTimer {
  [progressTimer invalidate];
  progressTimer = nil;
}

-(void)sendOnProgressUpdate {
  NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
  [dict setValue:[NSString stringWithFormat:@"%d", bambuserPlayer.live] forKey:@"live"];
  [dict setValue:[NSString stringWithFormat:@"%f", floor(bambuserPlayer.playbackPosition)] forKey:@"currentPosition"];
  if (duration > -1) {
    [dict setValue:[NSString stringWithFormat:@"%f", duration] forKey:@"duration"];
  }
  if (self.onProgressUpdate) {
    self.onProgressUpdate(dict);
  }
}

-(void)playbackStarted {
  if (self.onPlaying) {
    self.onPlaying(nil);
  }
  [self startTimer];
}

-(void)playbackPaused {
  if (self.onPaused) {
    self.onPaused(nil);
  }
  [self stopTimer];
}

-(void)playbackStopped {
  if (self.onStopped) {
    self.onStopped(nil);
  }
   [self stopTimer];
}

-(void)playbackCompleted {
  if (self.onPlaybackComplete) {
    self.onPlaybackComplete(nil);
  }
   [self stopTimer];
}

-(void)videoLoadFail {
  if (self.onPlaybackError) {
    self.onPlaybackError(nil);
  }
  [self stopTimer];
}

-(void)currentViewerCountUpdated:(int)viewers {
  if (self.onCurrentViewerCountUpdate) {
    NSString *viewersString = [NSString stringWithFormat:@"%d", viewers];
    self.onCurrentViewerCountUpdate(@{@"viewers": viewersString});
  }
}

-(void)totalViewerCountUpdated:(int)viewers {
  if (self.onTotalViewerCountUpdate) {
    NSString *viewersString = [NSString stringWithFormat:@"%d", viewers];
    self.onTotalViewerCountUpdate(@{@"viewers": viewersString});
  }
}

-(void)durationKnown:(double)duration {
  self.duration = duration;
  [progressTimer fire];
}

-(void)setSeekTo:(float)_seekTo {
  float seekToPosition = _seekTo;
  if (seekToPosition < 0) seekToPosition = 0;
  if (seekToPosition > -1 || ([bambuserPlayer timeShiftModeEnabled] && seekToPosition >= bambuserPlayer.seekableStart && seekToPosition <= bambuserPlayer.seekableEnd)) {
    [bambuserPlayer seekTo:_seekTo];
  }
}

-(void)setResourceUri:(NSString *)_resourceUri {
  if (_resourceUri != nil) {
    resourceUri = _resourceUri;
  }
}

-(void)setApplicatonId:(NSString *)_applicatonId {
  if (_applicatonId != nil) {
    applicatonId = _applicatonId;
  }
}

-(void)setVideoScaleMode:(NSString *)_videoScaleMode {
  if (_videoScaleMode != nil) {
    if ([_videoScaleMode isEqualToString:@"aspectFit"]) {
      videoScaleMode = [NSString stringWithFormat:@"%d", VideoScaleAspectFit];
    } else if ([_videoScaleMode isEqualToString:@"aspectFill"]) {
      videoScaleMode = [NSString stringWithFormat:@"%d", VideoScaleAspectFill];
    } else if ([_videoScaleMode isEqualToString:@"scaleToFill"]) {
      videoScaleMode = [NSString stringWithFormat:@"%d", VideoScaleToFill];
    }
  }
}

-(void)setIgnoreSilentSwitch:(NSString *)_ignoreSilentSwitch {
  if (_ignoreSilentSwitch != nil) {
    ignoreSilentSwitch = _ignoreSilentSwitch;
  }
}

-(void)setRequiredBroadcastState:(NSString *)_requiredBroadcastState {
  if (_requiredBroadcastState != nil) {
    if ([_requiredBroadcastState isEqualToString:@"any"]) {
      requiredBroadcastState = [NSString stringWithFormat:@"%d", kBambuserBroadcastStateAny];
    } else if ([_requiredBroadcastState isEqualToString:@"live"]) {
      requiredBroadcastState = [NSString stringWithFormat:@"%d", kBambuserBroadcastStateLive];
    } else if ([_requiredBroadcastState isEqualToString:@"archived"]) {
      requiredBroadcastState = [NSString stringWithFormat:@"%d", kBambuserBroadcastStateArchived];
    }
  }
}

-(void)setLatencyMode:(NSString *)_latencyMode {
  if (_latencyMode != nil) {
    if ([_latencyMode isEqualToString:@"low"]) {
      latencyMode = [NSString stringWithFormat:@"%d", BambuserPlayerLatencyModeLow];
    } else if ([_latencyMode isEqualToString:@"high"]) {
      latencyMode = [NSString stringWithFormat:@"%d", BambuserPlayerLatencyModeHigh];
    }
  }
}

-(void)setTimeShiftMode:(BOOL)_timeShiftMode {
  timeShiftMode = _timeShiftMode;
}

-(void)setVolume:(float)_volume {
  volume = _volume;
  bambuserPlayer.volume = self.volume;
}

-(void)setPlay:(BOOL)_play {
  if (bambuserPlayer.status == kBambuserPlayerStateStopped) {
    bambuserPlayer = [[BambuserPlayer alloc] init];
    bambuserPlayer.delegate = self;
    [self addSubview:bambuserPlayer];
    [self loadAndPlay];
  } else if (bambuserPlayer.status == kBambuserPlayerStateIdle) {
    [self loadAndPlay];
  } else {
    if (round([bambuserPlayer playbackPosition]) >= duration && ![bambuserPlayer live]) {
      [bambuserPlayer seekTo:0];
    }
    [bambuserPlayer playVideo];
  }
}

-(void)setPause:(BOOL)_pause {
  [bambuserPlayer pauseVideo];
}

-(void)setStop:(BOOL)_stop {
  [bambuserPlayer stopVideo];
}

-(void)loadAndPlay {
  if([self.ignoreSilentSwitch isEqualToString:@"ignore"]) {
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
  } else if([self.ignoreSilentSwitch isEqualToString:@"obey"]) {
     [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryAmbient error:nil];
  }
  if (self.onLoading) {
    self.onLoading(nil);
  }
  bambuserPlayer.applicationId = applicatonId;
  bambuserPlayer.timeShiftModeEnabled = timeShiftMode;
  bambuserPlayer.volume = volume;
  bambuserPlayer.requiredBroadcastState = [requiredBroadcastState intValue];
  bambuserPlayer.videoScaleMode = [videoScaleMode intValue];
  bambuserPlayer.latencyMode = [latencyMode intValue];
  [bambuserPlayer playVideo:resourceUri];
}

-(void)layoutSubviews {
  [super layoutSubviews];
  if (!viewReady) {
    viewReady = YES;
    if (self.onReady) {
      self.onReady(nil);
    }
  }
  bambuserPlayer.frame = self.bounds;
}

@end
