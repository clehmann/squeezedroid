package net.chrislehmann.squeezedroid.model;



public class PlayerStatus
{
   private static final Object STATUS_PAUSED = "pause";
   private static final Object STATUS_PLAYING = "play";
   private static final String STATUS_STOPPED = "stop";
   
   private Song currentSong;
   private int currentIndex = 0;
   private String status = STATUS_STOPPED;
   private int currentPosition = 0;
   private int volume = 0;
   
   private ShuffleMode shuffleMode;
   private RepeatMode repeatMode;

   public int getCurrentIndex()
   {
      return currentIndex;
   }

   public void setCurrentIndex(int currentIndex)
   {
      this.currentIndex = currentIndex;
   }

   public String getStatus()
   {
      return status;
   }

   public boolean isPlaying()
   {
      return STATUS_PLAYING.equals( status );
   }

   public boolean isPaused()
   {
      return STATUS_PAUSED.equals( status );
   }

   public boolean isStopped()
   {
      return STATUS_STOPPED.equals( status );
   }

   public void setStatus(String status)
   {
      this.status = status;
   }

   public Song getCurrentSong()
   {
      return currentSong;
   }

   public void setCurrentSong(Song currentSong)
   {
      this.currentSong = currentSong;
   }

   public int getCurrentPosition()
   {
      return currentPosition;
   }

   public void setCurrentPosition(int currentPosition)
   {
      this.currentPosition = currentPosition;
   }

   public int getVolume()
   {
      return volume;
   }

   public void setVolume(int volume)
   {
      this.volume = volume;
   }

   public ShuffleMode getShuffleMode()
   {
      return shuffleMode;
   }

   public void setShuffleMode(ShuffleMode shuffleMode)
   {
      this.shuffleMode = shuffleMode;
   }

   public RepeatMode getRepeatMode()
   {
      return repeatMode;
   }

   public void setRepeatMode(RepeatMode repeatMode)
   {
      this.repeatMode = repeatMode;
   }

}
