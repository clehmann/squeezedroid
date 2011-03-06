package net.chrislehmann.squeezedroid.service;

import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.RepeatMode;
import net.chrislehmann.squeezedroid.model.ShuffleMode;


public interface PlayerStatusHandler
{
   public void onPlaylistChanged( PlayerStatus status );
   
   public void onSongChanged( PlayerStatus status );
   
   public void onTimeChanged( int newPosition );

   public void onVolumeChanged( int newVolume );
   
   public void onPause();

   public void onPlay();

   public void onStop();

   public void onPlayerSynchronized( String playerId, String syncronizedPlayerId );

   public void onPlayerUnsynchronized();

   public void onDisconnect();
   
   public void onShuffleModeChanged( ShuffleMode newMode );
   
   public void onRepeatModeChanged( RepeatMode newMode );
   

}
