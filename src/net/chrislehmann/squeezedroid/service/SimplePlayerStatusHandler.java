package net.chrislehmann.squeezedroid.service;

import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;


public class SimplePlayerStatusHandler implements PlayerStatusHandler
{
   public void onPlaylistChanged( PlayerStatus status ){};   

   public void onSongChanged( PlayerStatus status ){};
   
   public void onTimeChanged( int newPosition ){};

   public void onVolumeChanged( int newVolume ){}

   public void onPlayerSynchronized( Player player, String syncronizedPlayerId ){};

   public void onPlayerUnsynchronized(){}

   public void onPause(){}

   public void onPlay(){}

   public void onStop(){}

}
