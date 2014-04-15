package kr.ac.kookmin.cs.distboard.util;

import java.util.ArrayList;

import kr.ac.kookmin.cs.distboard.protocol.GameToolData;

import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;

public class ArrayListConverter {
	public static Die[] toDiceArrayListToArray(ArrayList<Die> diceArrayList){
		Die[] dice = new Die[diceArrayList.size()];
		
		for(int i = 0 ; i < diceArrayList.size() ; i++){
			dice[i] = diceArrayList.get(i);
		}
		
		return dice;
		
	}
	
	public static BluetoothDevice[] bluetoothDeviceArrayListToArray(ArrayList<BluetoothDevice> bluetoothDeviceArrayList){
		BluetoothDevice[] devices = new BluetoothDevice[bluetoothDeviceArrayList.size()];
		
		for(int i = 0 ; i < bluetoothDeviceArrayList.size() ; i++){
			devices[i] = bluetoothDeviceArrayList.get(i);
		}
		
		return devices;
		
	}
	
	public static GameToolData[] gameToolDataArrayListToArray(ArrayList<GameToolData> gameToolDataArrayList){
		GameToolData[] gameToolDatas = new GameToolData[gameToolDataArrayList.size()];
		
		for(int i = 0 ; i < gameToolDataArrayList.size() ; i++){
			gameToolDatas[i] = gameToolDataArrayList.get(i);
		}
		
		return gameToolDatas;
		
	}
	
	public static ArrayList<BluetoothDevice> bluetoothDeviceArrayToArrayList(BluetoothDevice[] bluetoothDeviceArrayList){
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		
		for(int i = 0 ; i < bluetoothDeviceArrayList.length ; i++){
			devices.add(bluetoothDeviceArrayList[i]);
		}
		
		return devices;
		
	}
	
}
