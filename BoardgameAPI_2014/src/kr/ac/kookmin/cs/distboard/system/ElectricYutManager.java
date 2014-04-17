package kr.ac.kookmin.cs.distboard.system;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.DeviceType;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.system.ClientManager.ConnectThread;
import kr.ac.kookmin.cs.distboard.system.ClientManager.ConnectedThread;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import kr.ac.kookmin.cs.distboard.util.ThreadTimer;


public class ElectricYutManager {
	
	
	private static final String TAG = "20083271:ElectricYutManager";
	
	
	
	//for host
	
	public static final byte REQUEST_SUBSCRIBE_HEADER = 0x00;//body is null
	
	public static final byte REQUEST_RELEASE_HEADER = 0x01;//body is null
	
	
	//for electric yut
	
	
	public static final byte RESPONSE_SUBSCRIBE_HEADER = 0x02;//body is 0x00 or 0x01
	
	
	
	
	
	private static ElectricYutManager instance = new ElectricYutManager();

	private boolean isTimeOuted = false; // Ÿ�Ӿƿ� �Ǿ����� ��Ÿ�� �� �����Ҷ��� �� ���� �ٽ� false �� �ǰ���
										//�� ���� false ��� ���� "���� ���� ��" �̶�� ��.
	
	private boolean hasPerfectlyNominated = false;//null�� �ֻ������� �Ϻ��ϰ� �ĺ�ȭ�Ǿ�����, �̰��� �ٽ� ��ҵ� �� �ִ�.
	
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ThreadTimer threadTimer = null; // �߰�
	private boolean initialized = false;
	private int exactElectricGameToolYut = 0;
	private ArrayList<BluetoothDevice> yuts = new ArrayList<BluetoothDevice>();
	private ArrayList<BluetoothDevice> scaned = new ArrayList<BluetoothDevice>();//�̹� �˻��Ǿ� Ŀ��Ʈ �õ��ߴ� Die - �ӵ������� �ѹ��� 2���� ��ĳ�׵Ȱ� Ŀ��Ʈ�ϴ� ���� �ذ��ϱ�����
	
	//private ArrayList<ConnectElectricYutThread> connectThreads = new ArrayList<ConnectElectricYutThread>();
	private ArrayList<ConnectedElectricYutThread> connectedThreads = new ArrayList<ConnectedElectricYutThread>();
	private ArrayList<BluetoothDevice> lost = new ArrayList<BluetoothDevice>();
	private static UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	
	//����������� scaned������ ��������!!(�ٽ� �����ؾߵǴϱ�.)
	
	private ElectricYutManager() {
		
	}
	
	public static ElectricYutManager getInstance(){
		return instance;
	}
	
	public void initialize(){//�̱��� ���ɿ�
		Log.i(TAG, "������ �Ŵ��� �ʱ�ȭ");
		
		initialized = true;
		
		bluetoothAdapter = null;
		threadTimer = null;
		isTimeOuted = false;
		hasPerfectlyNominated = false;
		exactElectricGameToolYut = 0;
		yuts = new ArrayList<BluetoothDevice>();
		scaned = new ArrayList<BluetoothDevice>();
		lost = new ArrayList<BluetoothDevice>();
		
		//connectThreads = new ArrayList<ConnectElectricYutThread>();
		connectedThreads = new ArrayList<ConnectedElectricYutThread>();
		
	}
	
	public synchronized void establish(int exactElectricGameToolYut, int millisecTimeOut){
		

			Log.i(TAG, "�� ���� ����");

			// �ʱ�ȭ ���� �ʾ����� : �̱��� ���ɿ�
			if (initialized == false) {
				Log.e(TAG, "�ʱ�ȭ ���� ����");
				return;
			}

			// ������ Dice+ ������ 0���̸�
			if (exactElectricGameToolYut == 0 || Mediator.getInstance().getMode() == Mode.CLIENT) {
				// ���� �� �ٷ� ����
				hasPerfectlyNominated = true;
				CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);
				// CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new
				// Die[0]);
				return;
			}

			this.exactElectricGameToolYut = exactElectricGameToolYut;

			Log.i(TAG, "���� �� ���� ����");

			// /�� �� ��ġ �˻�////////

			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

			Log.i(TAG, "���ù� ��� ����");

			DistributedBoardgame.getInstance().getContext().registerReceiver(ElectricYutManager.getInstance().getYutReceiver(),filter);

			filter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

			DistributedBoardgame.getInstance()
					.getContext().registerReceiver(
							ElectricYutManager.getInstance().getYutReceiver(),
							filter);

			Log.i(TAG, "���ù� ��� �Ϸ�, Ž�� ����");
		
			if(bluetoothAdapter.isDiscovering()){
			Log.i(TAG, "�̹� Ž������.. �˻� ���");
			bluetoothAdapter.cancelDiscovery();
			}
		
			Log.i(TAG, "������� ��ġ Ž�� ����");
			bluetoothAdapter.startDiscovery();
		
			///////////////////////////////////////////////////////////

	    	Log.i(TAG, "���� �� Ÿ�Ӿƿ� ���� ���� : " + millisecTimeOut);
	    	threadTimer = new ThreadTimer(ThreadTimer.ELECTRIC_YUT_MANAGER_ESTABLISH_CALL, millisecTimeOut);
	    	threadTimer.start();
	    
		
	}

	public void onEstablishTimeOut(){
		Log.i(TAG, "Ÿ�Ӿƿ���");
		isTimeOuted = true;
		
		bluetoothAdapter.cancelDiscovery();
		
		 if(exactElectricGameToolYut != yuts.size() && hasPerfectlyNominated == false){
		    	Log.e(TAG, "�ҿ����� ���� �� ���� : " + yuts.size() + "/" + exactElectricGameToolYut);
		    	if(yuts.size() > 0){
		    		//��� ���� "�� �ϴ� ��������." : �籸���Ҷ� ���ݸ� �� �����ϸ�ǵ��� �ϱ�����..
		    		//dice.removeAll(dice);
		    	}
		    	
				Log.w(TAG, "Ÿ�Ӿƿ����� ��̳���Ʈ - ���� ��");
				CandidateManager.getInstance().nominateElectricYutEstablishFail();
		 }
		
	}
	
	public void discardYuts(){
		//���� �ϼ�
		Log.i(TAG, "Electric yut �����ϼ� ����");
		
		hasPerfectlyNominated = true;
		bluetoothAdapter.cancelDiscovery();
		clear();
		CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);
		
		CommunicationStateManager.getInstance().onElectricYutEstablishForceComplete();
	}
	
	public synchronized boolean isAvailableDevice(BluetoothDevice device){
	    if(yuts.indexOf(device) != -1){
	        return true;
	    }
	    return false;
	}
	
 	public ConnectedElectricYutThread getConnectedElectricYutThreadOf(BluetoothDevice device){
		Log.i(TAG, "��ġ�� �������� ���� : " + device.getName());
		for(int i = 0 ; i < connectedThreads.size() ; i++){
			if(connectedThreads.get(i) != null){
				if(connectedThreads.get(i).getRemoteDevice().equals(device) == true){
					Log.i(TAG, "��ġ�� �������� �Ϸ� ��������� ��ġ�� : " + connectedThreads.get(i).getRemoteDevice().getName());
					return connectedThreads.get(i);
				}
			}else{
				//������ ó��
				//���� �ʿ��� ��
				//����� �� ���� ��
			}
		}
		
		return null;
	}
	
	public void write(BluetoothDevice device){
		
	}
	
	public synchronized void clear(){
	    
	    initialized = false;
	    isTimeOuted = true;
	    
		Log.i(TAG, "���� �� ����");
		if(Mediator.getInstance().getMode() == Mode.HOST && exactElectricGameToolYut != 0){//�̷��� ��� ����� ���� �ִ� ���¿��� �����
			Log.i(TAG, "ȣ��Ʈ ��忡�� ����");
			DistributedBoardgame.getInstance().getContext().unregisterReceiver(mReceiver);
		}
		for(int i = 0 ; i < connectedThreads.size() ; i++){
		    connectedThreads.get(i).cancel();
		}
	}
	
	public BroadcastReceiver getYutReceiver(){
		return mReceiver;
	}
	
	public synchronized void setTimeouted(){
	    this.isTimeOuted = true;
	}
	//private �޼���, Ŭ����
	
	
	private void onNewElectricYut(BluetoothDevice device){
		Log.i(TAG, "���ο� ���� �� �߰�");
		if(scaned.indexOf(device) == -1 && isTimeOuted == false && hasPerfectlyNominated == false){
			Log.d(TAG, "���� Ž�� ��Ͽ� ���� ���� �� �Ǹ�" + device.getName() + " " + device.getAddress());
			scaned.add(device);
			connect(device);
		}
	}
	
	private void onDiscoverFinished(){
		if(isTimeOuted == false && hasPerfectlyNominated == false){
			bluetoothAdapter.cancelDiscovery();
			bluetoothAdapter.startDiscovery();
		}
	}
	
	private synchronized void connect(BluetoothDevice device){
		Log.i(TAG, "������.. " + device.getName() + " " + device.getAddress());
		ConnectElectricYutThread currentConnectingThread = null;
		
		currentConnectingThread = new ConnectElectricYutThread(device);
		currentConnectingThread.start();
	}
	
	private synchronized void onConnected(DeviceType deviceType, BluetoothSocket socket, BluetoothDevice device){
		Log.i(TAG, "����� : " + device.getName());
		
		if(yuts.indexOf(device) == -1){
			yuts.add(device);
			connectedThreads.add(new ConnectedElectricYutThread(socket));
			connectedThreads.get(connectedThreads.size() - 1 ).start();
			
			CommunicationStateManager.getInstance().onElectricYutConntected(device);
			
			if(yuts.size() == exactElectricGameToolYut && hasPerfectlyNominated == false){
				Log.i(TAG, "��� ���� �� ���� �Ϸ�");
				
				hasPerfectlyNominated = true;
				
				bluetoothAdapter.cancelDiscovery();
				
				CandidateManager.getInstance().nominateYutDevices(ArrayListConverter.bluetoothDeviceArrayListToArray(yuts));
				
				for(int i = 0; i < yuts.size() ; i++){
					//�������!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				}
				
				Log.w(TAG ,"��� ���� �� ���� ��� �Ϸ� : ���� �̿ϼ�");
				
			}
		}else{
			Log.e(TAG, "�̹� ����� ���� ��");
		}
	}
	
	private synchronized void onConnectionLost(ConnectedElectricYutThread connectedElectricYutThread){
		Log.i(TAG, "���� ������ : " + connectedElectricYutThread.getRemoteDevice().getName());
		
		CommunicationStateManager.getInstance().onElectricYutLost(connectedElectricYutThread.getRemoteDevice());
		
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE || initialized == true){
			Log.i(TAG, "������ ���� ������ ������ �Ǹ�");
			hasPerfectlyNominated = false;
			scaned.remove(connectedElectricYutThread.getRemoteDevice());
			yuts.remove(connectedElectricYutThread.getRemoteDevice());
			connectedThreads.remove(connectedElectricYutThread);
			lost.add(connectedElectricYutThread.getRemoteDevice());
			
			if(bluetoothAdapter.isDiscovering())
				bluetoothAdapter.cancelDiscovery();
			bluetoothAdapter.startDiscovery();
		}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME && initialized == true){
			Log.i(TAG, "�����߿� ������ ������");
		}
	}
	
	private synchronized void onConnectionFailed(ConnectElectricYutThread connectElectricYutThread){
		Log.d(TAG, "������� : " + connectElectricYutThread.getRemoteDevice().getName());
		
		scaned.remove(connectElectricYutThread.getRemoteDevice());

		if (connectElectricYutThread != null) {
			connectElectricYutThread.cancel();
			connectElectricYutThread = null;
		}else{
			Log.e(TAG, "���� connectThread�� null");
		}
		
		
	}
	
  	private class ConnectElectricYutThread extends Thread{
		
		private static final String TAG = "20083271:ConnectElectricYutThread";

		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		
		public ConnectElectricYutThread(BluetoothDevice device) {
			Log.d(TAG, "���ο� ���� ������");

			mmDevice = device;

			try {

				mmSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);

				if (mmSocket != null) {
					Log.i(TAG, "���� ������.");
				} else {
					Log.e(TAG, "���� �������� ����");
				}

			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
		}
		
		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");

			try {
				synchronized (ElectricYutManager.this) {
					mmSocket.connect();
				}
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + " socket during connection failure", e2);
				}
				Log.e(TAG, "��ġ�� ������ �� �����ϴ� : " + mmSocket.getRemoteDevice().getName());
				onConnectionFailed(this);
				return;
			}

			synchronized (ElectricYutManager.this) {
				Log.i(TAG, "���� ����");
				
				//���߿� ���շ��� �� ����� ���� ����
				
				/**
				 * �� ������ ù��°�� "������" �϶� ����ؾߵ�
				 */
				
				if(mmSocket == null)
					Log.w(TAG, "������ null�Դϴ�.");
				
				onConnected(DeviceType.ELECTRIC_GAME_TOOL, mmSocket, mmDevice);//������ ȣ��Ʈ		
			}
		}

		public BluetoothDevice getRemoteDevice() {
			return mmDevice;
		}

		public void cancel() {
			if(mmSocket != null){
				try {
					mmSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mmSocket= null;
			}
			// TODO Auto-generated method stub
		}
	}

 	private class ConnectedElectricYutThread extends Thread{

		private static final String TAG = "20083271:ConnectedRemoteAndroidThread";

		//private int connectIndex = -1;
		// private BluetoothDevice mmDevice;
		private BluetoothSocket mmSocket;

		private byte[] mmTempBuffer;
		private InputStream mmInStream;
		private OutputStream mmOutStream;

		public ConnectedElectricYutThread(BluetoothSocket socket) {
			
			Log.d(TAG, "���� �ȵ���̵� ����� ������ ������");
			
			mmSocket = socket;
			mmTempBuffer = new byte[1024];
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpOut = mmSocket.getOutputStream();
				tmpIn = mmSocket.getInputStream();

			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;

		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					mmInStream.read(mmTempBuffer);
					CommunicationStateManager.getInstance().onBytesDelivered(mmSocket.getRemoteDevice(), mmTempBuffer);

				} catch (IOException e) {
					Log.e(TAG, "���� ������", e);
					onConnectionLost(this);// ���� ��������... ����
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void write(byte[] buffer) {
			Log.i(TAG, "��ü ���� : " + buffer);
			try {
				mmOutStream.write(buffer);
				mmOutStream.flush();
				// mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1,
				// buffer).sendToTarget();
				// Share the sent message back to the UI Activity
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
			    if(mmSocket != null)
			        mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

		public BluetoothDevice getRemoteDevice() {
			return mmSocket.getRemoteDevice();
		}
	}

 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if(device.getBondState() == BluetoothDevice.BOND_BONDED){
					Log.i(TAG, "�̹� ���� : " + device.getName() + "(" + device.getAddress() + ")");
				}else if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					Log.i(TAG, "���ο� ��ġ : " + device.getName() + "(" + device.getAddress() + ")");
				}
				
				if(device.getName().equals("Ibar")){
					Log.i(TAG, "���ο� ��ġ�� Ibar");
					onNewElectricYut(device);
				}else{
					Log.i(TAG, "���ο� ��ġ�� Ibar �ƴ�" + device.getName());
				}
				
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				Log.i(TAG, "��ġ �˻� ����");
				onDiscoverFinished();
			}
		}
	};
}
