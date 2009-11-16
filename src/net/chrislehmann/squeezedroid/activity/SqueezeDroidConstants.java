package net.chrislehmann.squeezedroid.activity;

public class SqueezeDroidConstants
{
   public static class RequestCodes
   {
      public static final int REQUEST_SHOW_SETTINGS = 110;
      public static final int REQUEST_CONNECT = 111;
      public static final int REQUEST_CHOOSE_PLAYER = 112;
      public static final int REQUEST_BROWSE = 113;
   }
   public static class ResultCodes
   {
      public static final int RESULT_DONE = 999;
   }
   
   public static class IntentDataKeys
   {
      protected static final String KEY_SELECTED_PLAYER = "keySelectedPlayer";
      protected static final String KEY_DIALOG_NAME = "dialogName";
      protected static final String KEY_PLAYERLIST_INCLUDE_SELECTED_PLAYER = "includeSelectedlayer";
      protected static final String KEY_PLAYERLUSR_REMOVE_DUPLICATE_PLAYERS = "removeDuplicatePlayers";
      protected static final String KEY_PLAYERLIST_EMPTY_PLAYER_NAME = "emptyPlayerName";
   }
   public static class Actions
   {
      public static final String ACTION_CHOOSE_PLAYER = "net.chrislehmann.squeezedroid.action.ChoosePlayer";
      public static final String ACTION_EDIT_PREFERENCES = "net.chrislehmann.squeezedroid.action.EditPreferences";
      public static final String ACTION_CONNECT = "net.chrislehmann.squeezedroid.action.ConnectToServer";
      
   }
   public static class Preferences
   {
      public static final String LAST_SELECTED_PLAYER = "last_selected_player";
   }

   public static class FolderObjectTypes
   {
      public static final String DIRECTORY = "folder";
      public static final String TRACK = "track";
      public static final String PLAYLIST = "playlist";
      public static final String UNKNOWN = "unknown";

   
   }

}
