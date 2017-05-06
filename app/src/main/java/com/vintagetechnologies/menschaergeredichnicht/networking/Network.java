package com.vintagetechnologies.menschaergeredichnicht.networking;

import android.app.Activity;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.esotericsoftware.kryo.Kryo;
import com.vintagetechnologies.menschaergeredichnicht.GameSettings;
import com.vintagetechnologies.menschaergeredichnicht.structure.Board;
import com.vintagetechnologies.menschaergeredichnicht.structure.Cheat;
import com.vintagetechnologies.menschaergeredichnicht.structure.Colorful;
import com.vintagetechnologies.menschaergeredichnicht.structure.Dice;
import com.vintagetechnologies.menschaergeredichnicht.structure.DiceNumber;
import com.vintagetechnologies.menschaergeredichnicht.structure.EndSpot;
import com.vintagetechnologies.menschaergeredichnicht.structure.Game;
import com.vintagetechnologies.menschaergeredichnicht.structure.GamePiece;
import com.vintagetechnologies.menschaergeredichnicht.structure.Player;
import com.vintagetechnologies.menschaergeredichnicht.structure.PlayerColor;
import com.vintagetechnologies.menschaergeredichnicht.structure.RegularSpot;
import com.vintagetechnologies.menschaergeredichnicht.structure.Spot;
import com.vintagetechnologies.menschaergeredichnicht.structure.StartingSpot;
import com.vintagetechnologies.menschaergeredichnicht.view.BoardView;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Fabio on 28.04.17.
 */

public class Network {

	public static final String MESSAGE_DELIMITER = ";";

	/**
	 * Sender: Used to indicate that the host should send his up to date Game class to the clients.
	 * Receiver: The message data contains/is an up to date Game class.
	 */
	public static final String TAG_SYNCHRONIZE_GAME = "sync";


	/**
	 * Sender/Receiver: Indicates if a player has cheated (message: "true" or "false).
	 */
	public static final String TAG_PLAYER_HAS_CHEATED = "cheated";


	/**
	 * Name of the player.
	 */
	public static final String TAG_PLAYER_NAME = "my_name";


	/**
	 * Indicate that the game should start now.
	 */
	public static final String TAG_START_GAME = "start_game";


	/**
	 * Register classes that will be send.
	 * @param kryo
	 */
	public static void registerKryoClasses(Kryo kryo){
		kryo.register(String.class);
		kryo.register(GameSettings.class);

		// for Game
		kryo.register(Game.class);
		kryo.register(Board.class);
		kryo.register(Player.class);
		kryo.register(Dice.class);
		kryo.register(BoardView.class);
		kryo.register(Paint.class);
		kryo.register(BoardView.class);
		kryo.register(DiceNumber.class);
		kryo.register(GamePiece.class);
		kryo.register(PlayerColor.class);
		kryo.register(Colorful.class);
		kryo.register(Cheat.class);

		kryo.register(Spot.class);
		kryo.register(Spot[].class);
		kryo.register(StartingSpot.class);
		kryo.register(RegularSpot.class);
		kryo.register(EndSpot.class);
	}


	/**
	 * Check if the device is connected (and not currently connecting) to a WiFi network.
	 * @return true if connected, false otherwise.
	 */
	public static boolean isConnectedToWiFiNetwork(Activity activity) {
		ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		//return (info != null && info.isConnectedOrConnecting());
		return info != null && info.isConnected();
	}


	/* other constants*/
	public static final String DATAHOLDER_GAMELOGIC = "GAMELOGIC";
	public static final String DATAHOLDER_GAMESETTINGS = "GAMESETTINGS";

	public static final int WRITE_BUFFER_SIZE = 1000 * 1000 * 10;
	public static final int OBJECT_BUFFER_SIZE = 1000 * 1000 * 10;

}