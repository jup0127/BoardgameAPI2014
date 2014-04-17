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

	private boolean isTimeOuted = false; // 타임아웃 되었는지 나타냄 재 구성할때는 이 값이 다시 false 가 되겠지
										//이 값이 false 라는 것은 "연결 구성 중" 이라는 것.
	
	private boolean hasPerfectlyNominated = false;//null인 주사위없이 완벽하게 후보화되었으면, 이것은 다시 취소될 수 있다.
	
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ThreadTimer threadTimer = null; // 추가
	private boolean initialized = false;
	private int exactElectricGameToolYut = 0;
	private ArrayList<BluetoothDevice> yuts = new ArrayList<BluetoothDevice>();
	private ArrayList<BluetoothDevice> scaned = new ArrayList<BluetoothDevice>();//이미 검색되어 커넥트 시도했던 Die - 속도때문에 한번에 2개의 스캐닝된것 커넥트하는 문제 해결하기위함
	
	//private ArrayList<ConnectElectricYutThread> connectThreads = new ArrayList<ConnectElectricYutThread>();
	private ArrayList<ConnectedElectricYutThread> connectedThreads = new ArrayList<ConnectedElectricYutThread>();
	private ArrayList<BluetoothDevice> lost = new ArrayList<BluetoothDevice>();
	private static UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	
	//연결끊어지면 scaned에서도 지워주자!!(다시 연결해야되니까.)
	
	private ElectricYutManager() {
		
	}
	
	public static ElectricYutManager getInstance(){
		return instance;
	}
	
	public void initialize(){//싱글톤 조심용
		Log.i(TAG, "전자윷 매니저 초기화");
		
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
		

			Log.i(TAG, "윷 구성 시작");

			// 초기화 되지 않았으면 : 싱글톤 조심용
			if (initialized == false) {
				Log.e(TAG, "초기화 되지 않음");
				return;
			}

			// 연결할 Dice+ 개수가 0개이면
			if (exactElectricGameToolYut == 0 || Mediator.getInstance().getMode() == Mode.CLIENT) {
				// 보고 후 바로 리턴
				hasPerfectlyNominated = true;
				CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);
				// CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new
				// Die[0]);
				return;
			}

			this.exactElectricGameToolYut = exactElectricGameToolYut;

			Log.i(TAG, "전자 윷 구성 시작");

			// /윷 용 장치 검색////////

			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

			Log.i(TAG, "리시버 등록 시작");

			DistributedBoardgame.getInstance().getContext().registerReceiver(ElectricYutManager.getInstance().getYutReceiver(),filter);

			filter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

			DistributedBoardgame.getInstance()
					.getContext().registerReceiver(
							ElectricYutManager.getInstance().getYutReceiver(),
							filter);

			Log.i(TAG, "리시버 등록 완료, 탐색 시작");
		
			if(bluetoothAdapter.isDiscovering()){
			Log.i(TAG, "이미 탐색중임.. 검색 취소");
			bluetoothAdapter.cancelDiscovery();
			}
		
			Log.i(TAG, "블루투스 장치 탐색 시작");
			bluetoothAdapter.startDiscovery();
		
			///////////////////////////////////////////////////////////

	    	Log.i(TAG, "전자 윷 타임아웃 측정 시작 : " + millisecTimeOut);
	    	threadTimer = new ThreadTimer(ThreadTimer.ELECTRIC_YUT_MANAGER_ESTABLISH_CALL, millisecTimeOut);
	    	threadTimer.start();
	    
		
	}

	public void onEstablishTimeOut(){
		Log.i(TAG, "타임아웃됨");
		isTimeOuted = true;
		
		bluetoothAdapter.cancelDiscovery();
		
		 if(exactElectricGameToolYut != yuts.size() && hasPerfectlyNominated == false){
		    	Log.e(TAG, "불완전한 전자 윷 구성 : " + yuts.size() + "/" + exactElectricGameToolYut);
		    	if(yuts.size() > 0){
		    		//모두 삭제 "는 일단 하지말자." : 재구성할때 조금만 더 구성하면되도록 하기위해..
		    		//dice.removeAll(dice);
		    	}
		    	
				Log.w(TAG, "타임아웃됨을 노미네이트 - 전자 윷");
				CandidateManager.getInstance().nominateElectricYutEstablishFail();
		 }
		
	}
	
	public void discardYuts(){
		//강제 완성
		Log.i(TAG, "Electric yut 강제완성 진입");
		
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
		Log.i(TAG, "장치로 스레드얻기 진입 : " + device.getName());
		for(int i = 0 ; i < connectedThreads.size() ; i++){
			if(connectedThreads.get(i) != null){
				if(connectedThreads.get(i).getRemoteDevice().equals(device) == true){
					Log.i(TAG, "장치로 스레드얻기 완료 스레드안의 장치는 : " + connectedThreads.get(i).getRemoteDevice().getName());
					return connectedThreads.get(i);
				}
			}else{
				//적절한 처리
				//보고가 필요할 듯
				//사용할 수 없을 때
			}
		}
		
		return null;
	}
	
	public void write(BluetoothDevice device){
		
	}
	
	public synchronized void clear(){
	    
	    initialized = false;
	    isTimeOuted = true;
	    
		Log.i(TAG, "전자 윷 정리");
		if(Mediator.getInstance().getMode() == Mode.HOST && exactElectricGameToolYut != 0){//이래야 적어도 등록한 적이 있는 상태에서 취소함
			Log.i(TAG, "호스트 모드에서 정리");
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
	//private 메서드, 클래스
	
	
	private void onNewElectricYut(BluetoothDevice device){
		Log.i(TAG, "새로운 전자 윷 발견");
		if(scaned.indexOf(device) == -1 && isTimeOuted == false && hasPerfectlyNominated == false){
			Log.d(TAG, "기존 탐색 목록에 없던 전자 윷 판명" + device.getName() + " " + device.getAddress());
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
		Log.i(TAG, "연결중.. " + device.getName() + " " + device.getAddress());
		ConnectElectricYutThread currentConnectingThread = null;
		
		currentConnectingThread = new ConnectElectricYutThread(device);
		currentConnectingThread.start();
	}
	
	private synchronized void onConnected(DeviceType deviceType, BluetoothSocket socket, BluetoothDevice device){
		Log.i(TAG, "연결됨 : " + device.getName());
		
		if(yuts.indexOf(device) == -1){
			yuts.add(device);
			connectedThreads.add(new ConnectedElectricYutThread(socket));
			connectedThreads.get(connectedThreads.size() - 1 ).start();
			
			CommunicationStateManager.getInstance().onElectricYutConntected(device);
			
			if(yuts.size() == exactElectricGameToolYut && hasPerfectlyNominated == false){
				Log.i(TAG, "모든 전자 윷 연결 완료");
				
				hasPerfectlyNominated = true;
				
				bluetoothAdapter.cancelDiscovery();
				
				CandidateManager.getInstance().nominateYutDevices(ArrayListConverter.bluetoothDeviceArrayListToArray(yuts));
				
				for(int i = 0; i < yuts.size() ; i++){
					//구독등록!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				}
				
				Log.w(TAG ,"모든 전자 윷 구독 등록 완료 : 아직 미완성");
				
			}
		}else{
			Log.e(TAG, "이미 연결된 전자 윷");
		}
	}
	
	private synchronized void onConnectionLost(ConnectedElectricYutThread connectedElectricYutThread){
		Log.i(TAG, "연결 끊어짐 : " + connectedElectricYutThread.getRemoteDevice().getName());
		
		CommunicationStateManager.getInstance().onElectricYutLost(connectedElectricYutThread.getRemoteDevice());
		
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE || initialized == true){
			Log.i(TAG, "구성중 연결 끊어진 것으로 판명");
			hasPerfectlyNominated = false;
			scaned.remove(connectedElectricYutThread.getRemoteDevice());
			yuts.remove(connectedElectricYutThread.getRemoteDevice());
			connectedThreads.remove(connectedElectricYutThread);
			lost.add(connectedElectricYutThread.getRemoteDevice());
			
			if(bluetoothAdapter.isDiscovering())
				bluetoothAdapter.cancelDiscovery();
			bluetoothAdapter.startDiscovery();
		}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME && initialized == true){
			Log.i(TAG, "게임중에 전자윷 끊어짐");
		}
	}
	
	private synchronized void onConnectionFailed(ConnectElectricYutThread connectElectricYutThread){
		Log.d(TAG, "연결실패 : " + connectElectricYutThread.getRemoteDevice().getName());
		
		scaned.remove(connectElectricYutThread.getRemoteDevice());

		if (connectElectricYutThread != null) {
			connectElectricYutThread.cancel();
			connectElectricYutThread = null;
		}else{
			Log.e(TAG, "인자 connectThread가 null");
		}
		
		
	}
	
  	private class ConnectElectricYutThread extends Thread{
		
		private static final String TAG = "20083271:ConnectElectricYutThread";

		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		
		public ConnectElectricYutThread(BluetoothDevice device) {
			Log.d(TAG, "새로운 연결 스레드");

			mmDevice = device;

			try {

				mmSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);

				if (mmSocket != null) {
					Log.i(TAG, "소켓 생성됨.");
				} else {
					Log.e(TAG, "소켓 생성되지 않음");
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
				Log.e(TAG, "장치에 연결할 수 없습니다 : " + mmSocket.getRemoteDevice().getName());
				onConnectionFailed(this);
				return;
			}

			synchronized (ElectricYutManager.this) {
				Log.i(TAG, "연결 성공");
				
				//나중에 재합류할 때 사용할 정보 저장
				
				/**
				 * 이 정보중 첫번째는 "게임중" 일때 사용해야됨
				 */
				
				if(mmSocket == null)
					Log.w(TAG, "소켓이 null입니다.");
				
				onConnected(DeviceType.ELECTRIC_GAME_TOOL, mmSocket, mmDevice);//상대방은 호스트		
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
			
			Log.d(TAG, "원격 안드로이드 연결된 스레드 생성자");
			
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
					Log.e(TAG, "연결 끊어짐", e);
					onConnectionLost(this);// 연결 끊어지면... 실행
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void write(byte[] buffer) {
			Log.i(TAG, "객체 쓰기 : " + buffer);
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
					Log.i(TAG, "이미 페어링됨 : " + device.getName() + "(" + device.getAddress() + ")");
				}else if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					Log.i(TAG, "새로운 장치 : " + device.getName() + "(" + device.getAddress() + ")");
				}
				
				if(device.getName().equals("Ibar")){
					Log.i(TAG, "새로운 장치가 Ibar");
					onNewElectricYut(device);
				}else{
					Log.i(TAG, "새로운 장치가 Ibar 아님" + device.getName());
				}
				
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				Log.i(TAG, "장치 검색 종료");
				onDiscoverFinished();
			}
		}
	};
}
