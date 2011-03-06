package net.chrislehmann.squeezedroid.view;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import net.chrislehmann.squeezedroid.R;
import net.chrislehmann.squeezedroid.activity.SqueezedroidActivitySupport;
import net.chrislehmann.squeezedroid.model.Player;
import net.chrislehmann.squeezedroid.model.PlayerStatus;
import net.chrislehmann.squeezedroid.service.PlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.ServiceConnectionManager;
import net.chrislehmann.squeezedroid.service.SimplePlayerStatusHandler;
import net.chrislehmann.squeezedroid.service.SqueezeService;

import java.util.HashMap;
import java.util.Map;

public class PlayerSyncPanel extends LinearLayout {

    private static final String LOGTAG = "PlayerSyncPanel";

    private class Syncronization {
        public Syncronization(View view, PlayerStatusHandler volumeHandler, PlayerStatusHandler syncHandler) {
            this.view = view;
            this.volumeHandler = volumeHandler;
            this.syncHandler = syncHandler;
        }

        public View view;
        public PlayerStatusHandler volumeHandler;
        public PlayerStatusHandler syncHandler;
    }

    private Map<String, Syncronization> syncronizations = new HashMap<String, Syncronization>();
    private String selectedPlayerId;

    private SqueezedroidActivitySupport parent;


    public PlayerSyncPanel(SqueezedroidActivitySupport parent) {
        super(parent);
        this.setOrientation(LinearLayout.VERTICAL);
        setBaselineAligned(false);
        this.parent = parent;
    }

    public PlayerSyncPanel(SqueezedroidActivitySupport parent, AttributeSet attrs) {
        super(parent, attrs);
        this.parent = parent;
    }

    public void destroy() {
        for (String id : syncronizations.keySet()) {
            removeSyncronization(id);
        }
        syncronizations.clear();
    }

    private void removeSyncronization(final String playerId) {
        parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
            final Syncronization sync = syncronizations.get(playerId);
            public void runWithService(SqueezeService service) {
                service.unsubscribeAll(sync.syncHandler);
                service.unsubscribeAll(sync.volumeHandler);
                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        removeView(sync.view);
                    }
                });
            }
        });
    }

    public synchronized void setPlayer(final String playerId) {
        destroy();

        parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
            public void runWithService(final SqueezeService service) {
                final Player newPlayer = service.getPlayer(playerId);

                parent.runOnUiThread(new Runnable() {
                    public void run() {
                        addSynchronization(service, newPlayer, true);
                        for (Player syncedPlayer : newPlayer.getSyncronizedPlayers()) {
                            addSynchronization(service, syncedPlayer, false);
                        }
                    }
                });


                selectedPlayerId = newPlayer.getId();
            }
        });

    }

    private void addSynchronization(SqueezeService service, Player syncedPlayer, boolean isPrimary) {
        if (!syncronizations.containsKey(syncedPlayer.getId())) {

            PlayerStatus status = service.getPlayerStatus(syncedPlayer.getId());

            View view = LayoutInflater.from(getContext()).inflate(R.layout.player_sync_control_layout, null);
            SeekBar volumeSeekBar = (SeekBar) view.findViewById(R.id.volume_seek_bar);
            volumeSeekBar.setProgress(status.getVolume());
            volumeSeekBar.setOnSeekBarChangeListener(new OnVolumeChangedListener(syncedPlayer.getId()));

            ImageButton unsyncButton = (ImageButton) view.findViewById(R.id.unsync_button);
            if (isPrimary) {
                unsyncButton.setVisibility(view.INVISIBLE);
            } else {
                unsyncButton.setOnClickListener(new OnUnsyncButtonPressedListener(syncedPlayer));
            }

            PlayerStatusHandler volumeHandler = new VolumeChangedStatusHandler(volumeSeekBar);
            service.subscribe(syncedPlayer.getId(), volumeHandler);

            PlayerStatusHandler syncHandler = new MainPlayerOnSyncHandler();
            syncronizations.put(syncedPlayer.getId(), new Syncronization(view, volumeHandler, syncHandler));
            service.subscribe(syncedPlayer.getId(), syncHandler);

            TextView playerNameLabel = (TextView) view.findViewById(R.id.player_name_text);
            playerNameLabel.setText(syncedPlayer.getName());

            this.addView(view);
        }

    }

    private class OnUnsyncButtonPressedListener implements OnClickListener {
        private Player player;

        public OnUnsyncButtonPressedListener(Player player) {
            this.player = player;
        }

        public void onClick(View v) {
            Toast.makeText(getContext(), player.getName() + " unsynchronized.", Toast.LENGTH_LONG).show();
            parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                public void runWithService(SqueezeService service) {
                    service.unsynchronize(player.getId());
                }
            });
        }
    }

    private class OnVolumeChangedListener implements OnSeekBarChangeListener {
        private String _player;
        private int volume = 0;
        private boolean seeking = false;

        public OnVolumeChangedListener(String player) {
            _player = player;
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            seeking = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seeking) {
                parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                    public void runWithService(SqueezeService service) {
                        service.changeVolume(_player, volume);
                    }
                });
            }
            seeking = false;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                volume = progress;
                //This will happen if the hardware buttons are used
                if (!seeking) {
                    parent.runWithService(new ServiceConnectionManager.SqueezeServiceAwareThread() {
                        public void runWithService(SqueezeService service) {
                            service.changeVolume(_player, volume);
                        }
                    });
                }
            }
        }
    }

    ;

    private class VolumeChangedStatusHandler extends SimplePlayerStatusHandler {
        private SeekBar seekBar;

        public VolumeChangedStatusHandler(SeekBar seekBar) {
            this.seekBar = seekBar;
        }

        @Override
        public void onVolumeChanged(int newVolume) {
            this.seekBar.setProgress(newVolume);
        }
    }

    private class MainPlayerOnSyncHandler extends SimplePlayerStatusHandler {

        @Override
        public void onPlayerSynchronized(final String mainPlayerId, String newPlayerId) {
            Log.d(LOGTAG, "Got selectedPlayerId sync event mainPlayer: " + mainPlayerId + ", new selectedPlayerId: " + newPlayerId);
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    setPlayer(mainPlayerId);
                }
            });

        }

        @Override
        public void onPlayerUnsynchronized() {
            updatePlayer();
        }

        @Override
        public void onDisconnect() {
            updatePlayer();
        }

        private void updatePlayer() {
            parent.runOnUiThread(new Runnable() {
                public void run() {
                    setPlayer(selectedPlayerId);
                }
            });
        }

    }
}
