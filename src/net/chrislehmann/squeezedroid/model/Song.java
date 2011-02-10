package net.chrislehmann.squeezedroid.model;

import android.util.Log;
import net.chrislehmann.util.SerializationUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class Song extends Item {
    private String artist;
    private String artistId;
    private String album;
    private String albumId;
    private int durationInSeconds;
    private boolean isRadioStation;

    private String url;

    private String serverPath;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    private String year;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public boolean isRadioStation() {
        return isRadioStation;
    }

    public void setRadioStation(boolean isRadioStation) {
        this.isRadioStation = isRadioStation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        Log.d("Song", "Serverpath: " + getServerPath());
        String filename = id + ".mp3";
        if(StringUtils.isNotEmpty( getServerPath() ) )
        {
            filename = FilenameUtils.getName(SerializationUtils.decode(getServerPath()));
        }
        Log.d("Song", "Filename: " + filename);
        return FilenameUtils.normalize(filename);
    }

    public String getLocalPath()
    {
        return normalize(getArtist()) + "/" + normalize(getAlbum()) + "/" + getFileName();
    }

    private String normalize(String string) {
        return FilenameUtils.normalize(StringUtils.strip(string) );
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

}
