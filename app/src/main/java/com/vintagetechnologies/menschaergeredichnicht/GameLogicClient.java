package com.vintagetechnologies.menschaergeredichnicht;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.vintagetechnologies.menschaergeredichnicht.implementation.ActualGame;
import com.vintagetechnologies.menschaergeredichnicht.implementation.RealDice;
import com.vintagetechnologies.menschaergeredichnicht.networking.Device;
import com.vintagetechnologies.menschaergeredichnicht.networking.DeviceList;
import com.vintagetechnologies.menschaergeredichnicht.networking.Network;
import com.vintagetechnologies.menschaergeredichnicht.networking.kryonet.MyClient;
import com.vintagetechnologies.menschaergeredichnicht.networking.kryonet.NetworkListener;
import com.vintagetechnologies.menschaergeredichnicht.structure.DiceNumber;
import com.vintagetechnologies.menschaergeredichnicht.structure.Player;

import java.util.Set;

import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.DATAHOLDER_GAMELOGIC;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.DATAHOLDER_GAMESETTINGS;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.MESSAGE_DELIMITER;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_CLIENT_PLAYER_NAME;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_CURRENT_PLAYER;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_PLAYER_HAS_CHEATED;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_PLAYER_NAME;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_STATUS_MESSAGE;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_TOAST;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_WAIT_FOR_MOVE;
import static com.vintagetechnologies.menschaergeredichnicht.networking.Network.TAG_WAIT_FOR_ROLL;

/**
 * Created by Fabio on 04.05.17.
 * <p>
 * Implements the game rules and logic. Sends and receives messages to/from other players.
 */
public class GameLogicClient extends GameLogic implements NetworkListener {


    private static final String TAG = GameLogic.class.getSimpleName();

    private MyClient myClient;

    private Client client;

    private Device hostDevice;


    public GameLogicClient(Activity activity, MyClient myClient) {
        super(activity, false);
        this.myClient = myClient;
        this.client = myClient.getClient();

        myClient.addListener(this);

        DataHolder.getInstance().save(DATAHOLDER_GAMELOGIC, this);
    }


    @Override
    public void onReceived(Connection connection, Object object) {
        parseMessage(connection, object);
    }

    /**
     * Called when the connection to the host was established.
     *
     * @param connection
     */
    @Override
    public void onConnected(Connection connection) {

        hostDevice = new Device(connection.getID(), true);
        getDevices().add(hostDevice);

        // send name to host
        GameSettings gameSettings = (GameSettings) DataHolder.getInstance().retrieve(DATAHOLDER_GAMESETTINGS);
        sendToHost(TAG_PLAYER_NAME + MESSAGE_DELIMITER + gameSettings.getPlayerName());
    }

    @Override
    public void onDisconnected(Connection connection) {
        Toast.makeText(getActivity().getApplicationContext(),
                getActivity().getString(R.string.hostEndedGame), Toast.LENGTH_LONG).show();

        getDevices().clear();

        leaveGame();
    }

    @Override
    protected void parseMessage(Connection connection, Object object) {

        if (object instanceof Player) {    // client received game update

            Player player = (Player) object;

            ActualGame.refreshPlayer(player);

        } else if (object instanceof RealDice){

            RealDice.setRealDice((RealDice) object);
            ActualGame.getInstance().getGameLogic().setDice(RealDice.get());

        } else if (object instanceof GameSettings) {

            GameSettings remoteGameSettings = (GameSettings) object;

            getGameSettings().apply(remoteGameSettings);

        } else if (object instanceof DiceNumber) {

            final Spieloberflaeche activity = (Spieloberflaeche) getActivity();
            final int dn = ((DiceNumber) object).getNumber() -1;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageButton btnWuerfel = (ImageButton) activity.findViewById(R.id.imageButton_wuerfel);
                    btnWuerfel.setImageResource(activity.getDiceImage(dn));
                }
            });

        } else if (object instanceof String) {

            String[] data = ((String) object).split(MESSAGE_DELIMITER);
            String tag = data[0];
            String value = null;

			if(data.length > 1){
				value = data[1];
			}

            if (TAG_PLAYER_NAME.equals(tag)) {    // when receiving the name of the host

                hostDevice.setName(value);

            } else if (TAG_TOAST.equals(tag)) {    // show a toast

                String message = value;
                Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();

            } else if (TAG_CLIENT_PLAYER_NAME.equals(tag)) {    // if hosts send the name of a client

                DeviceList deviceList = getDevices();
                String deviceName = data[1];
                int id = Integer.parseInt(data[2]);

                if (!deviceList.contains(deviceName))
                    deviceList.add(new Device(id, deviceName, false));

                deviceList.getPlayer(deviceName).setId(id);

            } else if (TAG_STATUS_MESSAGE.equals(tag)) {

                if (hasGameStarted()) {
                    Spieloberflaeche activity = (Spieloberflaeche) getActivity();
                    activity.setStatus(value);
                }

            } else if (TAG_CURRENT_PLAYER.equals(tag)) {

                Spieloberflaeche activity = (Spieloberflaeche) getActivity();

                // name of the players who's turn it is.
                String currentPlayerName = value;

                Player currentPlayer = ActualGame.getInstance().getGameLogic().getCurrentPlayer();
                currentPlayer.getSchummeln().setPlayerCheating(false); //Damit der Würfel weiß, dass noch nicht geschummelt wurde.
                currentPlayer.getSchummeln().setCheated(false);//Damit man weiß, dass wärend des zuges noch nicht geschummelt wurde.

				// enable/disable controls for "Würfeln, Aufdecken, Schummeln"
                if (currentPlayerName != null && currentPlayerName.equals(getGameSettings().getPlayerName())) {
                    activity.setDiceEnabled(true);
                    activity.setRevealEnabled(false);
                    activity.setSensorOn(true);
                } else {
                    activity.setDiceEnabled(false);
                    activity.setRevealEnabled(true);
                    activity.setSensorOn(false);
                }

            } else if (TAG_WAIT_FOR_MOVE.equals(tag)) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ActualGame.getInstance().getGameLogic()._findPossibleToMove();
                        ActualGame.getInstance().waitForMovePiece();
                    }
                }, "PlayThread");
                ActualGame.getInstance().getGameLogic().setClientPlayThread(thread);
                thread.start();

            }else if (TAG_WAIT_FOR_ROLL.equals(tag)){
                GameSettings gameSettings = DataHolder.getInstance().retrieve(Network.DATAHOLDER_GAMESETTINGS, GameSettings.class);

                if(gameSettings.getPlayerName().equals(value)){
                    new Thread(){
                        @Override
                        public void run() {
                            RealDice.get().waitForRoll();
                        }
                    }.start();

                }
            }

        } else {
            Log.w(TAG, "Received unknown message from host.");
        }
    }


    @Override
    public void startGame() {
        // show main menu
        Intent intent = new Intent(getActivity(), Spieloberflaeche.class);
        getActivity().startActivity(intent);

        setGameStarted(true);
    }

    @Override
    public void leaveGame() {
        Log.i(TAG, "Stopping client...");
        client.stop();

        // show main menu
        Intent intent = new Intent(getActivity(), Hauptmenue.class);
        getActivity().startActivity(intent);

        getActivity().finish();
    }


    /**
     * Send a message to the host.
     *
     * @param message The message to be sent.
     */
    public void sendToHost(final Object message) throws IllegalArgumentException {
        if (message == null)
            throw new IllegalArgumentException("Message must not be null.");


        // send in own thread
        Thread sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                client.sendTCP(message);
            }
        }, "Sending");

        sendingThread.start();
    }


    @Override
    public void setActivity(Activity activity) {
        super.setActivity(activity);
        myClient.setCallingActivity(activity);
    }
}
