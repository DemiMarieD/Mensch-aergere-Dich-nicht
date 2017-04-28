package com.vintagetechnologies.menschaergeredichnicht;

import com.vintagetechnologies.menschaergeredichnicht.networking.Device;
import com.vintagetechnologies.menschaergeredichnicht.networking.DeviceList;
import com.vintagetechnologies.menschaergeredichnicht.networking.WifiListener;
import com.vintagetechnologies.menschaergeredichnicht.networking.WifiReceiver;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Fabio on 28.04.17.
 */

public class NetworkingTest {

	private GameSettings gameSettings;
	private Device device;
	private DeviceList deviceList;
	private WifiReceiver wifiReceiver;

	@Before
	public void before() {
		gameSettings = new GameSettings();
		deviceList = new DeviceList();
		wifiReceiver = new WifiReceiver();
	}


	@Test
	public void testGameSettings(){

		// test default values
		assertEquals("Music must be enabled by default", true, gameSettings.isMusicEnabled());
		assertEquals("Cheating must be enabled by default", true, gameSettings.isCheatingEnabled());
		assertEquals("Board design must be CLASSIC by default", GameSettings.BoardDesign.CLASSIC, gameSettings.getBoardDesign());


		// test custom settings
		gameSettings.setPlayerName("Max");
		assertEquals("Player name set to 'Max'", "Max", gameSettings.getPlayerName());

		gameSettings.setBoardDesign(GameSettings.BoardDesign.VINTAGE);
		assertEquals("Board design must be VINTAGE by default", GameSettings.BoardDesign.VINTAGE, gameSettings.getBoardDesign());

		gameSettings.setCheatingEnabled(false);
		assertEquals("Cheating must be disabled", false, gameSettings.isCheatingEnabled());

		gameSettings.setMusicEnabled(false);
		assertEquals("Music must be diabled", false, gameSettings.isMusicEnabled());
	}

	@Test
	public void testDeviceList(){

		assertEquals("There can't be a host when no devices added", null, deviceList.getHost());
		assertEquals("At first the connection count must be zero", 0, deviceList.getCountConnectedDevices());

		// add two devices
		Device device1 = new Device("1", "Fabio", true);
		Device device2 = new Device("2", "Daniel", false);
		deviceList.addDevice(device1);
		deviceList.addDevice(device2);

		// check connected device count
		assertEquals("Two connected devices", 2, deviceList.getCountConnectedDevices());

		// check host
		assertEquals("Host is device1", device1, deviceList.getHost());

		// get device by id
		assertEquals(device2, deviceList.getDeviceByPlayerID("2"));

		// get device by name
		assertEquals(device1, deviceList.getDeviceByPlayerName("Fabio"));
		assertEquals("Device with name 'Max' does not exist", null, deviceList.getDeviceByPlayerName("Max"));

		// check if device is host
		assertEquals(false, deviceList.isHost("2"));
		assertEquals(true, deviceList.isHost("1"));

		// remove device
		deviceList.removeDeviceByID("2");

		// check if device was removed
		assertEquals(null, deviceList.getDeviceByPlayerID("2"));

		// get list
		assertNotNull("Device list must not be null", deviceList.getList());
		assertEquals("Device list must not be empty", 1, deviceList.getList().size());
	}

	@Test
	public void testDataHolder(){

		// get non existing item
		assertNull(DataHolder.getInstance().retrieve("MYITEM"));

		// save object
		Device device = new Device("1", "Fabio", false);
		DataHolder.getInstance().save("ITEM", device);

		// get saved item
		assertEquals(device, DataHolder.getInstance().retrieve("ITEM"));
	}

	@Test
	public void testWifiListenerAndReceiver(){

		boolean errorInitialisingNewWifiReceiver = false;

		try {
			wifiReceiver.addReceiver(new WifiListener() {
				@Override
				public void hasWiFiConnectionEstablished() {

				}

				@Override
				public void hasWiFiConnectionLost() {

				}
			});

		} catch (Exception e){
			errorInitialisingNewWifiReceiver = true;
		}

		assertEquals("There should not be an error happened", false, errorInitialisingNewWifiReceiver);
	}

	@Test
	public void testDevice(){
		device = new Device("4", "Max", true);

		// test custom settings
		assertEquals("Id must be 4", "4", device.getId());
		assertEquals("Name is Max", "Max", device.getName());
		assertEquals("Is host", true, device.isHost());

		device.setId("abc");
		assertEquals("Id must be abc", "abc", device.getId());

		device.setName("Dev");
		assertEquals("Name is Dev", "Dev", device.getName());

		device.setHost(false);
		assertEquals("Is not host", false, device.isHost());
	}

}
