package net.chrislehmann.squeezedroid.service;

import android.util.Log;
import net.chrislehmann.squeezedroid.activity.SqueezeDroidConstants;
import net.chrislehmann.squeezedroid.exception.ApplicationException;
import net.chrislehmann.squeezedroid.model.Album;
import net.chrislehmann.squeezedroid.model.ApplicationMenuItem;
import net.chrislehmann.squeezedroid.model.Artist;
import net.chrislehmann.squeezedroid.model.BrowseResult;
import net.chrislehmann.squeezedroid.model.Folder;
import net.chrislehmann.squeezedroid.model.Genre;
import net.chrislehmann.squeezedroid.model.Item;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerIdEqualsPredicate;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.model.Playlist;
import net.chrislehmann.squeezedroid.model.RepeatMode;
import net.chrislehmann.squeezedroid.model.SearchResult;
import net.chrislehmann.squeezedroid.model.ShuffleMode;
import net.chrislehmann.squeezedroid.model.Song;
import net.chrislehmann.util.ImageLoader;
import net.chrislehmann.util.SerializationUtils;
import net.chrislehmann.util.SerializationUtils.Unserializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SqueezeService} that uses the SqueezeCenter command
 * line interface
 *
 * @author lehmanc
 */
public class CliSqueezeService implements SqueezeService {

    private static final String LOGTAG = "SQUEEZE";
    private static final String SONG_TAGS = "aslepPdxKJu";

    /**
     * Host to connect to
     */
    private String host = "localhost";
    /**
     * Port(s) to connect to
     */
    private int cliPort = 9090;
    private int httpPort = 9000;

    private Socket clientSocket;
    private Writer clientWriter;
    private BufferedReader clientReader;

    private String username;
    private String password;

    private EventThread eventThread;
    private BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<Runnable>();


    private class CommandThread extends Thread {
        public void run() {
            try {
                while (!isInterrupted()) {
                    Runnable r = commandQueue.take();
                    if (!isInterrupted()) {
                        r.run();
                    }
                }
            } catch (InterruptedException e) {
                //just finish...
            }

        }

        ;
    }

    ;

    private CommandThread commandThread;

    public CliSqueezeService(String host, int cliPort, int httpPort) {
        super();
        this.host = host;
        this.cliPort = cliPort;
        this.httpPort = httpPort;
    }

    private Pattern countPattern = Pattern.compile(" count%3A([^ ]*)");
    private Pattern artistsResponsePattern = Pattern.compile("id%3A([^ ]*) artist%3A([^ ]*)");
    private Pattern genresResponsePattern = Pattern.compile("id%3A([^ ]*) genre%3A([^ ]*)");
    private Pattern applicationItemPattern = Pattern.compile("id%3A([^ ]*) name%3A([^ ]*) .*?(type%3A([^ ]*) )*.*?(image%3A([^ ]*) )*.*?isaudio%3A([^ ]*) hasitems%3A([^ ]*)");

    private Pattern albumsResponsePattern = Pattern.compile("id%3A([^ ]*) album%3A([^ ]*)( artwork_track_id%3A([0-9]+)){0,1}( artist%3A([^ ]*)){0,1}");
    private Pattern playersResponsePattern = Pattern.compile("playerid%3A([^ ]*) uuid%3A([^ ]*) ip%3A([^ ]*) name%3A([^ ]*)");
    private Pattern songsResponsePattern = Pattern
            .compile(" id%3A([^ ]*) .*?title%3A([^ ]*) .*?(artist%3A([^ ]*) )*.*?(artist_id%3A([^ ]*) )*.*?(album%3A([^ ]*) )*.*?(album_id%3A([^ ]*) )*.*?duration%3A([^ ]*).*?( remote%3A([^ ]*))*.*?( artwork_url%3A([^ ]*))*.*?( artwork_track_id%3A([^ ]*))*.*?( url%3A([^ ]*))*");
    private Pattern playlistCountPattern = Pattern.compile("playlist_tracks%3A([^ ]*)");
    private Pattern playerStatusResponsePattern = Pattern.compile(" mode%3A([^ ]*) .*?(time%3A([^ ]*))* .*?mixer%20volume%3A([^ ]*) .*?playlist%20repeat%3A([^ ]*) .*?playlist%20shuffle%3A([^ ]*) .*?playlist_cur_index%3A([0-9]*)");
    private Pattern syncgroupsResponsePattern = Pattern.compile("sync (.*)");
    private Pattern versionResponsePattern = Pattern.compile("version ([0-9|.]+)");

    private Pattern foldersResponsePattern = Pattern.compile("id%3A([^ ]*) filename%3A([^ ]*) type%3A([^ ]*)");

    private Pattern appsResponsePattern = Pattern.compile("icon%3A([^ ]*) cmd%3A([^ ]*) weight%3A([^ ]*) name%3A([^ ]*) type%3Axmlbrowser");

    private Pattern searchResultResponsePattern = Pattern.compile("count%3A([^ ]*).*?( contributors_count%3A([^ ]*))*.*?( albums_count%3A([^ ]*))*.*?( genres_count%3A([^ ]*))*.*?( tracks_count%3A([^ ]*))*");

    private Pattern artistSearchResultResponsePattern = Pattern.compile("contributor_id%3A([^ ]*) contributor%3A([^ ]*)");
    private Pattern albumSearchResultResponsePattern = Pattern.compile("album_id%3A([^ ]*) album%3A([^ ]*)");
    private Pattern genreSearchResultResponsePattern = Pattern.compile("genre_id%3A([^ ]*) genre%3A([^ ]*)");
    private Pattern songSearchResultResponsePattern = Pattern.compile("song_id%3A([^ ]*) song%3A([^ ]*)");

    private Pattern playlistResponsePattern = Pattern.compile("id%3A([^ ]*) playlist%3A([^ ]*)");

    private Pattern urlPattern = Pattern.compile("url%3A([^ ]*)");

    private Unserializer<Song> songUnserializer = new SerializationUtils.Unserializer<Song>() {
        public Song unserialize(Matcher matcher) {
            Song song = new Song();
            song.setId(matcher.group(1));
            song.setName(SerializationUtils.decode(matcher.group(2)));
            if (matcher.group(3) != null) {
                song.setArtist(SerializationUtils.decode(matcher.group(4)));
            }
            if (matcher.group(6) != null) {
                song.setArtistId(SerializationUtils.decode(matcher.group(6)));
            }
            song.setAlbum(SerializationUtils.decode(matcher.group(8)));
            if (matcher.group(9) != null) {
                song.setAlbumId(SerializationUtils.decode(matcher.group(10)));
            }


            if (matcher.group(12) != null && "1".equals(matcher.group(13))) {
                song.setRadioStation(true);
            }

            //set the artwork images
            if (matcher.group(15) != null) {
                song.setImageUrl(SerializationUtils.decode(matcher.group(15)));
            } else {
                String artId = song.getId();
                if (matcher.group(17) != null) {
                    artId = matcher.group(17);
                }

                song.setImageThumbnailUrl("http://" + host + ":" + httpPort + "/music/" + artId + "/cover_50x50_o");
                song.setImageUrl("http://" + host + ":" + httpPort + "/music/" + artId + "/cover_320x320_o");

            }
            if (matcher.group(18) != null) {
                song.setServerPath(SerializationUtils.decode(matcher.group(19)));
            }
            song.setUrl("http://" + host + ":" + httpPort + "/music/" + song.getId() + "/download");

            try {
                Float duration = Float.parseFloat(matcher.group(11));
                song.setDurationInSeconds(duration.intValue());
            } catch (NumberFormatException e) {
            }
            return song;
        }
    };

    /**
     * Connect to the squeezecenter server and log in if required. Will throw an
     * {@link ApplicationException} if the connection fails.
     */
    public void connect() {
        try {
            clientSocket = new Socket(host, cliPort);
            clientWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            clientSocket.setSoTimeout(10 * 1000);
        } catch (Exception e) {
            throw new ApplicationException("Cannot connect to host '" + host + "' at port '" + cliPort, e);
        }
        if (username != null && password != null) {
            executeCommand("login " + username + " " + password);
            ImageLoader.getInstance().setCredentials(username, password);
        }
        //Check for a valid 'version' response to make sure we are actually connected.
        String response = executeCommand("version ?");
        Matcher matcher = null;
        if (response != null) {
            matcher = versionResponsePattern.matcher(response);
        }
        if (response == null || !matcher.matches()) {
            clientSocket = null;
            clientWriter = null;
            clientWriter = null;
            throw new ApplicationException("Cannot log into server");
        }

        eventThread = new EventThread(host, cliPort);
        eventThread.setUsername(username);
        eventThread.setPassword(password);
        eventThread.setService(this);

        commandThread = new CommandThread();


        eventThread.start();
        commandThread.start();

    }

    /**
     * Disconnect from the server. Throws an {@link ApplicationException} if an
     * error occours
     */
    public void disconnect() {
        if (clientSocket != null && clientSocket.isConnected()) {
            try {
                clientSocket.close();
                clientReader = null;
                clientWriter = null;
            } catch (Exception e) {
                throw new ApplicationException("Error closing socket", e);
            }
            clientSocket = null;
        }

        if (eventThread != null) {
            eventThread.disconnect();
            eventThread = null;
        }
        if (commandThread != null) {
            commandThread.interrupt();
        }
    }

    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected();
    }

    synchronized private String executeCommand(String command) {
        String response = null;
        Log.d(LOGTAG, "Sending command: " + command);
        if (writeCommand(command)) {
            response = readResponse();
            Log.d(LOGTAG, "Response from squeezeserver: " + response);
        }
        return response;
    }

    private void executeAsyncCommand(final String commandString) {
        Runnable command = new Runnable() {
            public void run() {
                executeCommand(commandString);
            }
        };
        commandQueue.add(command);
    }


    private String readResponse() {
        String response = null;
        try {
            if (clientReader != null) {
                response = clientReader.readLine();
            }
        } catch (SocketTimeoutException sto) {
            Log.e(LOGTAG, "Timeout reading socket, disconnecting from server");
        } catch (IOException e) {
            Log.e(LOGTAG, "error reading response", e);
        }

        //Disconnect from the server if we don't have a response.
        if (response == null) {
            Log.e(LOGTAG, "Error reading response, disconnecting from server");
            this.disconnect();
        }
        return response;
    }

    private boolean writeCommand(String command) {
        boolean written = false;
        try {
            if (clientWriter != null) {
                clientWriter.write(command + "\n");
                clientWriter.flush();
                written = true;
            }
        } catch (IOException e) {
            Log.e(LOGTAG, "Error writing response, disconnecting from server");
            disconnect();
        }
        return written;
    }

    public BrowseResult<Item> browseFolders(Folder parent, int start, int numberOfItems) {
        String command = "musicfolder " + start + " " + numberOfItems;
        if (parent != null) {
            command += " folder_id:" + parent.getId();
        }

        String result = executeCommand(command);
        BrowseResult<Item> browseResult = new BrowseResult<Item>();
        if (result != null) {

            Unserializer<Item> unserializer = new Unserializer<Item>() {
                public Item unserialize(Matcher matcher) {
                    String id = matcher.group(1);
                    String title = SerializationUtils.decode(matcher.group(2));
                    String type = matcher.group(3);
                    Item item = new Item();
                    if (SqueezeDroidConstants.FolderObjectTypes.DIRECTORY.equals(type)) {
                        item = new Folder();
                    } else if (SqueezeDroidConstants.FolderObjectTypes.TRACK.equals(type)) {
                        item = new Song();
                    }

                    item.setId(id);
                    item.setName(title);
                    return item;
                }
            };

            List<Item> items = SerializationUtils.unserializeList(foldersResponsePattern, result, unserializer);
            browseResult.setResutls(items);
            browseResult.setTotalItems(unserializeCount(result));

        }
        return browseResult;
    }

    public BrowseResult<Genre> browseGenres(Item parent, int start, int numberOfItems) {

        String command = "genres " + start + " " + numberOfItems;
        Unserializer<Genre> unserializer = new Unserializer<Genre>() {

            public Genre unserialize(Matcher matcher) {
                Genre genre = new Genre();
                genre.setId(matcher.group(1));
                genre.setName(SerializationUtils.decode(matcher.group(2)));
                return genre;
            }
        };
        BrowseResult<Genre> browseResult = new BrowseResult<Genre>();
        String result = executeCommand(command);
        if (result != null) {
            List<Genre> genres = SerializationUtils.unserializeList(genresResponsePattern, result, unserializer);
            browseResult.setResutls(genres);
            browseResult.setTotalItems(unserializeCount(result));
        }
        return browseResult;
    }


    public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems) {
        return browseAlbums(parent, start, numberOfItems, Sort.TITLE);
    }

    public BrowseResult<Album> browseAlbums(Item parent, int start, int numberOfItems, Sort sort) {
        String command = "albums " + start + " " + numberOfItems;
        if (parent instanceof Artist) {
            command += " artist_id:" + parent.getId();
        }

        if (parent instanceof Genre) {
            command += " genre_id:" + parent.getId();
        }
        if (sort != Sort.TITLE) {
            command += " sort:" + sort.toString().toLowerCase();
        }

        command += " tags:laj";
        String result = executeCommand(command);
        BrowseResult<Album> browseResult = new BrowseResult<Album>();

        if (result != null) {
            List<Album> albums = SerializationUtils.unserializeList(albumsResponsePattern, result, new Unserializer<Album>() {
                public Album unserialize(Matcher matcher) {
                    Album album = new Album();
                    album.setId(matcher.group(1));
                    album.setName(SerializationUtils.decode(matcher.group(2)));
                    album.setArtist(SerializationUtils.decode(matcher.group(6)));
                    if (matcher.group(4) != null) {
                        album.setImageThumbnailUrl("http://" + host + ":" + httpPort + "/music/" + matcher.group(4) + "/cover_50x50_o");
                        album.setImageUrl("http://" + host + ":" + httpPort + "/music/" + matcher.group(4) + "/cover_480x480_o");
                    }
                    return album;
                }
            });

            browseResult.setTotalItems(unserializeCount(result));
            browseResult.setResutls(albums);
        }
        return browseResult;
    }

    public BrowseResult<Artist> browseArtists(Item parent, int start, int numberOfItems) {
        String command = "artists " + start + " " + numberOfItems;
        if (parent instanceof Genre) {
            command += " genre_id:" + parent.getId();
        }

        BrowseResult<Artist> browseResult = new BrowseResult<Artist>();
        String result = executeCommand(command);
        if (result != null) {
            Matcher matcher = artistsResponsePattern.matcher(result);

            List<Artist> artists = new ArrayList<Artist>();
            while (matcher.find()) {
                Artist artist = new Artist();
                artist.setId(matcher.group(1));
                artist.setName(SerializationUtils.decode(matcher.group(2)));
                artists.add(artist);
            }

            browseResult.setResutls(artists);
            browseResult.setTotalItems(unserializeCount(result));
        }
        return browseResult;
    }

    public BrowseResult<Song> browseSongs(Item parent, int start, int numberOfItems) {
        String command = "titles " + start + " " + numberOfItems + " tags:" + SONG_TAGS;
        String parentCommandString = "";
        BrowseResult<Song> browseResult = new BrowseResult<Song>();

        if (parent instanceof Artist) {
            parentCommandString = "artist_id:" + parent.getId();
        } else if (parent instanceof Album) {
            parentCommandString = "album_id:" + parent.getId() + " sort:tracknum";
        } else if (parent instanceof Playlist) {
            command = "playlists tracks " + start + " " + numberOfItems + " tags:" + SONG_TAGS;
            parentCommandString = "playlist_id:" + parent.getId();
        } else if (parent instanceof Genre) {
            parentCommandString = "genre_id:" + parent.getId();
        } else {
            Log.e(LOGTAG, "Unknown item type, will not return all songs." + parent.getClass());
        }

        if (StringUtils.isNotEmpty(parentCommandString)) {
            command += " ";
            command += parentCommandString;
            String result = executeCommand(command);
            if (result != null) {
                List<Song> songs = SerializationUtils.unserializeList(songsResponsePattern, result, songUnserializer);
                Integer count = unserializeCount(result);
                browseResult.setTotalItems(count);
                browseResult.setResutls(songs);
            }
        }

        return browseResult;
    }

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, final Application application, ApplicationMenuItem parent, int start, int numberOfItems) {
        return this.browseApplication(playerId, application, parent, null, start, numberOfItems);
    }

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, final Application application, int start, int numberOfItems) {
        return this.browseApplication(playerId, application, null, null, start, numberOfItems);
    }

    public BrowseResult<ApplicationMenuItem> browseApplication(String playerId, final Application application, ApplicationMenuItem parent, String query, int start, int numberOfItems) {
        String command = playerId + " " + application.getCmd() + " items " + start + " " + numberOfItems;

        if (parent != null) {
            command += " item_id:" + parent.getId();
        }
        if (query != null) {
            command += " search:" + SerializationUtils.encode(query);
        }
        String response = executeCommand(command);
        BrowseResult<ApplicationMenuItem> browseResult = new BrowseResult<ApplicationMenuItem>();

        if (response != null) {
            List<ApplicationMenuItem> items = SerializationUtils.unserializeList(applicationItemPattern, response, new Unserializer<ApplicationMenuItem>() {
                public ApplicationMenuItem unserialize(Matcher matcher) {
                    ApplicationMenuItem item = new ApplicationMenuItem();
                    item.setId(matcher.group(1));
                    item.setName(SerializationUtils.decode(matcher.group(2)));
                    if (matcher.group(3) != null) {
                        item.setType(SerializationUtils.decode(matcher.group(4)));
                    }
                    if (matcher.group(5) != null) {
                        item.setImageThumbnailUrl(SerializationUtils.decode(matcher.group(6)));
                    }
                    item.setPlayable(!matcher.group(7).equals("0"));
                    item.setHasItems(!matcher.group(8).equals("0"));
                    item.setApplication(application);
                    return item;
                }
            });
            browseResult.setResutls(items);
            browseResult.setTotalItems(unserializeCount(response));
        }

        return browseResult;
    }

    public BrowseResult<Playlist> listPlaylists(int start, int numberOfItems) {
        BrowseResult<Playlist> browseResult = new BrowseResult<Playlist>();

        String command = "playlists " + start + " " + numberOfItems;
        String response = executeCommand(command);
        if (response != null) {

            Unserializer<Playlist> playlistUnserializer = new Unserializer<Playlist>() {
                public Playlist unserialize(Matcher matcher) {
                    Playlist list = new Playlist();
                    list.setId(SerializationUtils.decode(matcher.group(1)));
                    list.setName(SerializationUtils.decode(matcher.group(2)));
                    return list;
                }
            };

            List<Playlist> playlists = SerializationUtils.unserializeList(playlistResponsePattern, response, playlistUnserializer);
            browseResult.setResutls(playlists);
            browseResult.setTotalItems(unserializeCount(response));
        }

        return browseResult;

    }

    public BrowseResult<Application> listApplications(int start, int numberOfItems) {
        return listRadioStationsOrApplications(start, numberOfItems, "apps");
    }

    public BrowseResult<Application> listRadioStations(int start, int numberOfItems) {
        return listRadioStationsOrApplications(start, numberOfItems, "radios");
    }

    private BrowseResult<Application> listRadioStationsOrApplications(int start, int numberOfItems, String type) {
        String command = type + " " + start + " " + numberOfItems;
        String response = executeCommand(command);
        BrowseResult<Application> browseResult = new BrowseResult<Application>();
        if (response != null) {
            List<Application> applications = SerializationUtils.unserializeList(appsResponsePattern, response, new Unserializer<Application>() {
                public Application unserialize(Matcher matcher) {
                    Application application = new Application();
                    application.setImageThumbnailUrl(getIconPath(getBaseHttpPath() + "/" + SerializationUtils.decode(matcher.group(1))));
                    Log.d(LOGTAG, "App Thumbnail URL: " + application.getImageThumbnailUrl());
                    application.setImageUrl(getBaseHttpPath() + "/" + SerializationUtils.decode(matcher.group(1)));
                    application.setCmd(SerializationUtils.decode(matcher.group(2)));
                    application.setName(SerializationUtils.decode(matcher.group(4)));
                    return application;
                }
            });
            browseResult.setTotalItems(unserializeCount(response));
            browseResult.setResutls(applications);
        }
        return browseResult;
    }

    Pattern p = Pattern.compile("(.*)\\.([a-z|A-Z]{3})");

    private String getIconPath(String url) {
        String iconPath = url;
        Matcher matcher = p.matcher(url);
        if (matcher.matches() && matcher.groupCount() == 2) {
            iconPath = matcher.group(1) + "_50x50." + matcher.group(2);
        }
        return iconPath;
    }

    private String getBaseHttpPath() {
        return "http://" + host + ":" + httpPort;
    }

    private String getPath(Item item) {
        String path = null;
        String response = executeCommand("songinfo 0 100 track_id:" + item.getId() + " tags:u");
        if (response != null) {
            Matcher matcher = urlPattern.matcher(response);
            if (matcher.find()) {
                path = matcher.group(1);
            }
        }
        return path;
    }

    private Integer unserializeCount(String result) {
        Integer numSongs = 0;
        Matcher countMatcher = countPattern.matcher(result);
        if (countMatcher.find()) {
            String countString = countMatcher.group(1);
            numSongs = Integer.valueOf(countString);
        } else {
            android.util.Log.e(this.getClass().getCanonicalName(), "Cannot find match for count from response '" + result + "'");
        }
        return numSongs;
    }

    public List<Player> getPlayers() {
        return getPlayers(false);
    }

    public List<Player> getPlayers(boolean removeDuplicates) {
        String command = new String("players 0 1000");
        List<Player> players = new ArrayList<Player>();
        String result = executeCommand(command);
        if (result != null) {
            players = SerializationUtils.unserializeList(playersResponsePattern, result, new SerializationUtils.Unserializer<Player>() {
                public Player unserialize(Matcher matcher) {
                    Player player = new Player();
                    player.setId(SerializationUtils.decode(matcher.group(1)));
                    player.setName(SerializationUtils.decode(matcher.group(4)));
                    return player;
                }
            });
        }

        List<Player> groupedPlayers = new ArrayList<Player>();
        List<Player> handledPlayers = new ArrayList<Player>();

        for (Player player : players) {
            if (!removeDuplicates || CollectionUtils.find(handledPlayers, new PlayerIdEqualsPredicate(player.getId())) == null) {
                command = player.getId() + " sync ?";
                String playerSyncResult = executeCommand(command);
                Matcher matcher = syncgroupsResponsePattern.matcher(playerSyncResult);
                if (matcher.find()) {
                    String syncedPlayersString = SerializationUtils.decode(matcher.group(1));
                    String[] syncedPlayersArray = syncedPlayersString.split(",");
                    for (int i = 0; i < syncedPlayersArray.length; i++) {
                        String syncedPlayerId = syncedPlayersArray[i];
                        Player syncedPlayer = (Player) CollectionUtils.find(players, new PlayerIdEqualsPredicate(syncedPlayerId));
                        if (syncedPlayer != null) {
                            player.getSyncronizedPlayers().add(syncedPlayer);
                        }
                    }
                }
                handledPlayers.add(player);
                groupedPlayers.add(player);
            }
        }
        return groupedPlayers;
    }

    public Player getPlayer(String playerId) {
        List<Player> players = getPlayers();
        return (Player) CollectionUtils.find(players, new PlayerIdEqualsPredicate(playerId));
    }

    public PlayerStatus getPlayerStatus(String playerId) {
        String command = new String(playerId + " status - 1 tags:" + SONG_TAGS);
        String result = executeCommand(command);

        PlayerStatus status = new PlayerStatus();
        if (result != null) {

            Song song = SerializationUtils.unserialize(songsResponsePattern, result, songUnserializer);
            status.setCurrentSong(song);


            Matcher statusMatcher = playerStatusResponsePattern.matcher(result);
            if (status != null && statusMatcher.find() && statusMatcher.group(1) != null) {
                Log.d(LOGTAG, "Status: " + statusMatcher.group(1));
                Log.d(LOGTAG, "Time: " + statusMatcher.group(3));
                Log.d(LOGTAG, "Volume: " + statusMatcher.group(4));
                Log.d(LOGTAG, "Repeat: " + statusMatcher.group(5));
                Log.d(LOGTAG, "Shuffle: " + statusMatcher.group(6));
                Log.d(LOGTAG, "Playlist Index: " + statusMatcher.group(7));

                status.setStatus(statusMatcher.group(1));
                status.setCurrentIndex(Integer.parseInt(statusMatcher.group(7)));
                String positionString = statusMatcher.group(3);
                try {
                    if (positionString != null) {
                        Double d = Double.parseDouble(positionString);
                        status.setCurrentPosition(d.intValue());
                    }
                    if (statusMatcher.group(4) != null) {
                        Double d = Double.parseDouble(statusMatcher.group(4));
                        status.setVolume(d.intValue());
                    }
                } catch (NumberFormatException nfd) {/* Invalid, don't set volume. */
                }

                status.setRepeatMode(RepeatMode.intToRepeatModeMap.get(statusMatcher.group(5)));
                status.setShuffleMode(ShuffleMode.intToShuffleModeMap.get(statusMatcher.group(6)));
            }

        }
        return status;

    }

    public BrowseResult<Song> getCurrentPlaylist(String playerId, Integer start, Integer numberOfItems) {
        String command = playerId + " status " + start + " " + numberOfItems + " tags:" + SONG_TAGS;
        String result = executeCommand(command);

        BrowseResult<Song> browseResult = new BrowseResult<Song>();
        if (result != null) {
            List<Song> songs = SerializationUtils.unserializeList(songsResponsePattern, result, songUnserializer);
            browseResult.setResutls(songs);

            Matcher countMatcher = playlistCountPattern.matcher(result);
            if (countMatcher.find()) {
                String countString = countMatcher.group(1);
                browseResult.setTotalItems(Integer.valueOf(countString));
            } else {
                android.util.Log.e(this.getClass().getCanonicalName(), "Cannot find match for count from status response '" + result + "'");
            }
        }
        return browseResult;
    }

    /**
     * Search for {@link Song}s, {@link Artist}s, {@link Album}s and {@link Genre}s
     * that match the passed searchTerm.
     *
     * @param searchTerm
     * @return
     */
    public SearchResult search(String searchTerm, int numResultsPerCategory) {
        String result = executeCommand("search 0 " + numResultsPerCategory + " term:" + SerializationUtils.encode(searchTerm));

        SearchResult searchResult = SerializationUtils.unserialize(searchResultResponsePattern, result, new Unserializer<SearchResult>() {
            public SearchResult unserialize(Matcher matcher) {
                SearchResult searchResult = new SearchResult();
                searchResult.setTotalResults(parseIntIfExists(matcher.group(1), 0));
                searchResult.setTotalArtists(parseIntIfExists(matcher.group(2), 0));
                searchResult.setTotalAlbums(parseIntIfExists(matcher.group(3), 0));
                searchResult.setTotalGenres(parseIntIfExists(matcher.group(4), 0));
                searchResult.setTotalSongs(parseIntIfExists(matcher.group(5), 0));
                return searchResult;
            }
        });

        if (searchResult == null) {
            searchResult = new SearchResult();
        }

        List<Song> songs = SerializationUtils.unserializeList(songSearchResultResponsePattern, result, new Unserializer<Song>() {
            public Song unserialize(Matcher matcher) {
                Song song = new Song();
                song.setId(SerializationUtils.decode(matcher.group(1)));
                song.setName(SerializationUtils.decode(matcher.group(2)));
                return song;
            }
        });

        if (songs != null) {
            searchResult.setSongs(songs);
        }

        List<Album> albums = SerializationUtils.unserializeList(albumSearchResultResponsePattern, result, new Unserializer<Album>() {
            public Album unserialize(Matcher matcher) {
                Album album = new Album();
                album.setId(SerializationUtils.decode(matcher.group(1)));
                album.setName(SerializationUtils.decode(matcher.group(2)));
                return album;
            }
        });
        if (albums != null) {
            searchResult.setAlbums(albums);
        }
        List<Artist> artists = SerializationUtils.unserializeList(artistSearchResultResponsePattern, result, new Unserializer<Artist>() {
            public Artist unserialize(Matcher matcher) {
                Artist artist = new Artist();
                artist.setId(SerializationUtils.decode(matcher.group(1)));
                artist.setName(SerializationUtils.decode(matcher.group(2)));
                return artist;
            }
        });
        if (artists != null) {
            searchResult.setArtists(artists);
        }

        List<Genre> genres = SerializationUtils.unserializeList(genreSearchResultResponsePattern, result, new Unserializer<Genre>() {
            public Genre unserialize(Matcher matcher) {
                Genre genre = new Genre();
                genre.setId(SerializationUtils.decode(matcher.group(1)));
                genre.setName(SerializationUtils.decode(matcher.group(2)));
                return genre;
            }
        });
        if (genres != null) {
            searchResult.setGenres(genres);
        }

        return searchResult;
    }

    public List<Song> getSongsForItem(Item item) {
        List<Song> results = new ArrayList<Song>();
        if (item instanceof Song) {
            results.add((Song) item);
        } else {
            BrowseResult<Song> result = browseSongs(item, 0, 10000);
            if (result != null) {
                results.addAll(result.getResutls());
            }
        }
        return results;

    }

    private int parseIntIfExists(String number, int defaultValue) {
        int value = defaultValue;

        if (number != null) {
            try {
                value = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                Log.e(LOGTAG, "Error parsing number '" + number + "'", e);
            }
        }
        return value;
    }

    public void addItem(String playerId, Item item) {
        addToPlaylist(playerId, item, "add");
    }

    public void playItem(String playerId, Item item) {
        addToPlaylist(playerId, item, "play");
    }

    public void playItemNext(String playerId, Item item) {
        addToPlaylist(playerId, item, "insert");
    }

    private void addToPlaylist(String playerId, Item item, String action) {
        //The action for multiple items.  Play is different...
        String multipleItemAction = action + "tracks";
        if ("play".equals(action)) {
            multipleItemAction = "loadtracks";
        }

        String command = "";

        //Handle applications - use the 'application' command
        if (item instanceof ApplicationMenuItem) {
            ApplicationMenuItem applicationMenuItem = (ApplicationMenuItem) item;
            command = playerId + " " + applicationMenuItem.getApplication().getCmd() + " playlist " + action + " item_id:" + item.getId();
        }

        String extraParams = getParamName(item);
        //Handle multiple items (i.e. albums, artists, etc).  Use the 'playlist loadx' command
        if (extraParams != null) {
            command = playerId + " playlist " + multipleItemAction + " " + extraParams + "=" + item.getId();
            executeAsyncCommand(command);
        } else {
            //Fall back to the 'play' command
            String path = getPath(item);
            if (path != null) {
                command = playerId + " playlist " + action + " " + path;
            }
        }

        //If it's not empty, execute the command...
        if (StringUtils.isNotEmpty(command)) {
            executeAsyncCommand(command);
        }
    }


    private String getParamName(Item item) {
        String extraParams = null;
        if (item instanceof Album) {
            extraParams = "album.id";
        } else if (item instanceof Artist) {
            extraParams = "contributor.id";
        } else if (item instanceof Artist) {
            extraParams = "track.id";
        } else if (item instanceof Genre) {
            extraParams = "genre.id";
        }

        return extraParams;
    }

    public void jump(String playerId, String position) {
        executeAsyncCommand(playerId + " playlist index " + position);
    }

    public void togglePause(String playerId) {
        executeAsyncCommand(playerId + " pause");
    }

    public void pause(String playerId) {
        executeAsyncCommand(playerId + " pause 1");
    }

    public void play(String playerId) {
        executeAsyncCommand(playerId + " play");
    }

    public void stop(String playerId) {
        executeAsyncCommand(playerId + " stop");
    }

    public void removeAllItemsByArtist(String playerId, String artistId) {
        executeAsyncCommand(playerId + " playlistcontrol cmd:delete artist_id:" + artistId);
    }

    public void removeAllItemsInAlbum(String playerId, String albumId) {
        executeAsyncCommand(playerId + " playlistcontrol cmd:delete album_id:" + albumId);
    }

    public void removeItem(String playerId, int playlistIndex) {
        executeAsyncCommand(playerId + " playlist delete " + playlistIndex);
    }

    public void clearPlaylist(String playerId) {
        executeAsyncCommand(playerId + " playlist clear");
    }

    public void togglePower(String playerId) {
        executeAsyncCommand(playerId + " power");
    }


    public void subscribe(final String playerId, final PlayerStatusHandler handler) {
        Runnable r = new Runnable() {
            public void run() {
                eventThread.subscribe(playerId, handler);
            }
        };
        commandQueue.add(r);
    }

    public void unsubscribe(final String playerId, final PlayerStatusHandler handler) {
        Runnable r = new Runnable() {
            public void run() {
                if (eventThread != null) {
                    eventThread.unsubscribe(playerId, handler);
                }
            }
        };
        commandQueue.add(r);
    }

    public void unsubscribeAll(final PlayerStatusHandler handler) {
        Runnable r = new Runnable() {
            public void run() {
                if (eventThread != null) {
                    eventThread.unsubscribe(handler);
                }
            }
        };
        commandQueue.add(r);
    }

    public void seekTo(String playerId, int time) {
        executeAsyncCommand(playerId + " time " + time);
    }

    public void changeVolume(String playerId, int volumeLevel) {
        executeAsyncCommand(playerId + " mixer volume " + volumeLevel);
    }

    public void synchronize(String playerId, String playerIdToSyncTo) {
        executeAsyncCommand(playerIdToSyncTo + " sync " + playerId);
    }

    public void unsynchronize(String playerId) {
        executeAsyncCommand(playerId + " sync -");
    }

    public void setShuffleMode(String playerId, ShuffleMode mode) {
        executeAsyncCommand(playerId + " playlist shuffle " + mode.getId());
    }

    public void setRepeatMode(String playerId, RepeatMode mode) {
        executeAsyncCommand(playerId + " playlist repeat " + mode.getId());
    }

    public void unsubscribe(final ServerStatusHandler handler) {
        Runnable r = new Runnable() {
            public void run() {
                if (eventThread != null) {
                    eventThread.unsubscribe(handler);
                }
            }
        };
        commandQueue.add(r);
    }

    public void subscribe(final ServerStatusHandler handler) {
        Runnable r = new Runnable() {
            public void run() {
                eventThread.subscribe(handler);
            }
        };
        commandQueue.add(r);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}