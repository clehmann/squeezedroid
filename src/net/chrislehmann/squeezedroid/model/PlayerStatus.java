package net.chrislehmann.squeezedroid.model;


public class PlayerStatus {
	private Song currentSong;
	private int currentIndex;
	private String status;
	private int currentPosition;

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Song getCurrentSong() {
		return currentSong;
	}

	public void setCurrentSong(Song currentSong) {
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

}
