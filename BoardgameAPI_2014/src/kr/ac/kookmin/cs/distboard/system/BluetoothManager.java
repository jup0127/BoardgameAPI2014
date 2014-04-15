package kr.ac.kookmin.cs.distboard.system;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import bli.m;

import us.dicepl.android.sdk.Die;
import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.enumeration.DeviceType;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import kr.ac.kookmin.cs.distboard.util.ThreadTimer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothManager {// 싱글톤

	private static BluetoothManager instance = new BluetoothManager();

	private final static String TAG = "20083271:BluetoothManager";
	private final static boolean PRINT_DEBUG_STATUS = true;

	private boolean initialized = false;// 모드가 초기화 되었는지

	private Mode mode = Mode.NONE;// 블루투스 매니저의 작동 모드(호스트 vs 클라이언트)
	private int maxClients = 0;
	private int exactYutGameTools = 0;
	private int millisecTimeout = 0;
	
	private boolean hasClientsPerfectlyNominated = false;
	private boolean hasElectricYutPerfectlyNominated = false;

	// 아래는 호스트입장이라면 여러개의 장치, 클라이언트 입장이라면 한개의 장치가 되겠지
	
	private ArrayList<UUID> uuids = new ArrayList<UUID>();

	// Member fields
	private BluetoothAdapter mAdapter = null;

	
	BroadcastReceiver mReceiver = null;
	
	private AcceptThread mAcceptThread;
	private ArrayList<ConnectRemoteAndroidThread> mConnectThreads;
	private ArrayList<ConnectedThread> mConnectedThreads;
	private HashMap<BluetoothDevice, DeviceType> deviceTypeMap;
	
	//연결 끊긴장치 목록
	private ArrayList<BluetoothDevice> lostClientDevices;
	//연결 끊긴 UUID 인덱스
	private ArrayList<Integer> lostIndexes;
	
	
	//호스트용 인스턴스 변수
	
	private int totalCurrentClients = 0;
	
	
	//클라이언트용 인스턴스 변수
	
	private int[] clientConnectSuccessList = null; // 0은 실패
													// 1은 성공
													// 초기값은
													// -1
	private int totalReportOfSuccess = 0;
	private int connectIndexAsClient = -1;//연결성공한 적이 없다는 표시, 바꾸지 말 것,
	//위 값은 게임중에 재합류시 사용되거나, 호스트의 구성중 끊긴 클라이언트 인덱스를 얻기위해 사용됨
	private BluetoothDevice underCommunicationHost = null;//연결 성공했을 때 그 통신하던 호스트
	private BluetoothDevice connectingHost = null;//연결중인 호스트
	
	// 호스트-클라이언트
	// 생성자
	private BluetoothManager() {
	}

	// 호스트-클라이언트
	// 초기화 된 블루투스매니저의 인스턴스를 얻습니다.
	public static BluetoothManager getInstance() {
		return instance;
	}

	public void initialize(Mode mode, int maxClients, int exactYutGameTools, int millisecTimeout) {
		if (instance.initialized == true)
			Log.w(TAG, "이미 초기화된 적이 있습니다.");

		mAdapter = BluetoothAdapter.getDefaultAdapter();
		instance.initialized = true;

		/*mReceiver = new BroadcastReceiver() {//등록된 이후로 리시브 시작! - 쌍으로 주석 풀 것
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d(TAG, "방송 수신");
				
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					
					Log.d(TAG, "알맞은 액션이 포함된 방송!");
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.NEW_DEVICE_DISCOVERED, device).sendToTarget();
				}
			}
		};*/
		
		
		mAcceptThread = null;
		mConnectedThreads = null;
		//mConnectThreads = new ArrayList<ConnectThread>(); - 윷가락 접속용인데, 어떻게 쓰일지 아직 모르겠음
		deviceTypeMap = new HashMap<BluetoothDevice, DeviceType>();
		lostClientDevices = new ArrayList<BluetoothDevice>();
		lostIndexes = new ArrayList<Integer>();
		
		this.mode = mode;
		this.maxClients = maxClients;
		this.exactYutGameTools = exactYutGameTools;
		this.millisecTimeout = millisecTimeout;

		//instance.remoteDevices = new ArrayList<BluetoothDevice>();
		//instance.deviceTypes = new ArrayList<DeviceType>();

		this.uuids.add(UUID
				.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
		this.uuids.add(UUID
				.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
		this.uuids.add(UUID
				.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
		this.uuids.add(UUID
				.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
		this.uuids.add(UUID
				.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
		this.uuids.add(UUID
				.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
		this.uuids.add(UUID
				.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
		this.uuids.add(UUID
				.fromString("1e14d4df-9c8b-4db2-81e5-c937564c86e3"));

		this.clientConnectSuccessList = new int[maxClients];

		for (int i = 0; i < maxClients; i++) {
			this.clientConnectSuccessList[i] = -1;// 초기값 -1
		}
		
		totalCurrentClients = 0;
		
		
		totalReportOfSuccess = 0;
		connectIndexAsClient = -1;
		underCommunicationHost = null;
		connectingHost = null;

		Log.i(TAG, "블루투스 매니저가 초기화되었습니다 : " + mode);
	}


	// 호스트-클라이언트
	// 파라미터의 장치에 대한 타입을 리턴한다.
	// 타입 : 호스트 OR 클라이언트 OR 전자도구
	// DeviceType 열거형 참조
	public synchronized DeviceType getTypeOf(BluetoothDevice device) {
		Log.i(TAG, "타입얻기 메서드 진입");
		if(deviceTypeMap.containsKey(device) == false){
			Log.w(TAG, "이미 지워진 키에 대한 검색");
		}
		return deviceTypeMap.get(device);
	}

	//현재 전송가능한지, 즉 연결되었는지
	public synchronized boolean isAvailableDevice(BluetoothDevice device){
	    for(int i = 0 ; i < mConnectedThreads.size() ; i++){
	       if(device.equals(mConnectedThreads.get(i).getRemoteDevice())){
	           return true;
	       }
	    }
	    return false;
	}
	
	// 호스트-클라이언트
	// 연결된 장치 리스트를 리턴합니다.
	public BluetoothDevice[] getRemoteDevices() {
		
		ArrayList<ConnectedThread> currentConnectedThread = mConnectedThreads;
		ArrayList<BluetoothDevice> currentRemoteDevices = new ArrayList<BluetoothDevice>();

		for(int i = 0 ; i < currentConnectedThread.size() ; i++){
			if(currentConnectedThread.get(i) != null)//null일수있다. 구성중 끊겨서 중간에 구멍난 상태일때
				currentRemoteDevices.add(currentConnectedThread.get(i).getRemoteDevice());
			else{ // currentConnectedThread.get(i) 가 null
				Log.w(TAG, "currentConnectedThread.get(i)가 null입니다. 이것은 위험할 수 있습니다.");
			}
		}
		
		return ArrayListConverter.bluetoothDeviceArrayListToArray(currentRemoteDevices);
	}

	// 호스트
	// 클라이언트 장치 목록을 얻습니다.
	public BluetoothDevice[] getClientDevices() {
		
		BluetoothDevice[] remoteDevices = getRemoteDevices();
		ArrayList<BluetoothDevice> clientsDeviceArrayList = new ArrayList<BluetoothDevice>();
		
		for(int i = 0 ; i < remoteDevices.length ; i++){
			if(remoteDevices[i] != null && getTypeOf(remoteDevices[i]) == DeviceType.CLIENT){
				clientsDeviceArrayList.add(remoteDevices[i]);
			}
		}
		
		return ArrayListConverter.bluetoothDeviceArrayListToArray(clientsDeviceArrayList);
	}

	// 클라이언트
	// 호스트 장치를 얻습니다.
	public BluetoothDevice getHostDevice() {
		BluetoothDevice[] remoteDevices = getRemoteDevices();
		for(int i = 0 ; i < remoteDevices.length ; i++){
			if(remoteDevices[i] != null && getTypeOf(remoteDevices[i]) == DeviceType.HOST){
				return remoteDevices[i];
			}
		}
		return null;
	}

	// 호스트
	// 전자게임도구 장치목록을 얻습니다.
	public BluetoothDevice[] getElectricGameToolDevices() {
		return null;
	}

	// 클라이언트
	// 주변 장치를 검색해서 리턴합니다.
	public BluetoothDevice[] getDiscoveredDevices() {
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
		
		final ArrayList<BluetoothDevice> mPairedList = new ArrayList<BluetoothDevice>();
		
		Log.d(TAG, "페어링된 기기를 찾습니다.");
		
		// 페어링 되있는 기기
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				mPairedList.add(device);
			}
		}
		
		// 장치 검색중이면 검색 취소
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
		
		Log.d(TAG, "주변장치 검색을 시작합니다.");
		
		// 주변 장치 검색 시작
		
		/*mAdapter.startDiscovery();
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		
		DistributedBoardgame.getInstance().getContext().registerReceiver(mReceiver, filter);*///언레지스터에러-이거넣으면 쌍으로 언레지스터도 주석 풀 것
		
		return ArrayListConverter.bluetoothDeviceArrayListToArray(mPairedList);
	}

	// 호스트
	// 호스트 입장에서 클라이언트는 기다리고 도구는 접속시도합니다.
	// 매 클라이언트 연결시 보고합니다.
	// 시간 초과시 보고합니다.
	// 완료시 보고합니다.
	// 0명의 클라이언트 혹은 0개의 윷이 인자로 오면 길이가(0)인 비어있는 배열을 리턴하면됨.
	public void establish() {
		Log.i(TAG, "블루투스 매니저 구성 시작");
		
		if(maxClients == 0){
			//보고 후 바로 리턴
			hasClientsPerfectlyNominated = true;
			CandidateManager.getInstance().nominateClientDevices(new BluetoothDevice[0]);
			//CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new Die[0]);
			return;
		}
		
		//ensureDiscoverable();
		mConnectedThreads = new ArrayList<ConnectedThread>();
		
		
		ThreadTimer timer = new ThreadTimer(ThreadTimer.BLUETOOTH_MANAGER_ESTABLISH_CALL, millisecTimeout);
		timer.start();

		// 타임아웃 설정

		mAcceptThread = new AcceptThread();
		mAcceptThread.start();
		
		//Log.i(TAG, "전자윷 임시 노미네이트 시작");
		//CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);
		//Log.i(TAG, "전자윷 임시 노미네이트 끝");
		
		// asd
	}

	// 호스트
	// 재구성
	// 이 메서드는 부를일이 없을 듯
	@Deprecated
	public void reEstablish() {
		// 첫 establish 의 설정정보들을 이용하여 다시 기다림.
		// 이미 접속된놈말고 접속안되거나 끊긴놈들 기다림
		// 기존에 정보없던 장치 접속시도시 따로 처리
	}

	// 호스트
	// 현재 접속된 놈들로 구성설정을 완료한다.(강제로)
	// 예를들어 최소2명 최대4명인 게임에서 3명접속시 게임하고싶을때 이 메서드가 호출되고 게임은 3명이 진행하는거로 설정된다.
	// 이메서드 이후에 getDevices() 호출하면 클라이언트는 3칸짜리 배열이겟지
	public void forceClientEstablishment() {
	    Log.i(TAG, "클라이언트 강제 구성 진입");
	    
		if(hasClientsPerfectlyNominated == false){
			
			hasClientsPerfectlyNominated = true;//확실히 후보화되었음을 강제 인증
			
			for(int i = 0 ; i < mConnectedThreads.size() ; i++){
				if(mConnectedThreads.get(i) == null){
				    Log.e(TAG, "이것이 null이면 안되 연결된스레드 인덱스는 : " + i);
					mConnectedThreads.remove(i);
				}
			}
			
			mAcceptThread.cancel();
			
			maxClients = mConnectedThreads.size();//클라이언트 수 강제 조작	
			//////////////////////////////////////////////////////////////
			
			Log.i(TAG, "클라이언트 노미네이트 중 : 장치 길이는 : " + getClientDevices().length);
			CandidateManager.getInstance().nominateClientDevices(getClientDevices());
			
			CommunicationStateManager.getInstance().onPlayerEstablishForceComplete();
			
		}
	}
	
	@Deprecated
	public synchronized void completeEstablishment(){
		
	}


	// 타이머가 호출해야할듯
	public void cancel() {
		Log.i(TAG, "블루투스 구성 취소");
	}

	// 클라이언트
	// 접속
	//무조껀 초기화(재합류 무시, 연결되었던 호스트 무시)
	public void connect(BluetoothDevice device) {
		// 필요한것 초기화
		for (int i = 0; i < maxClients; i++) {
			instance.clientConnectSuccessList[i] = ConnectRemoteAndroidThread.CONNECT_NONE;// 초기값
																				// -1
		}
		totalReportOfSuccess = 0;
		connectIndexAsClient = -1;
		underCommunicationHost = null;
		connectingHost = null;
		
		// 접속
		
		connectAsClient(device);
		
		
	}

	// CLIENT 재접속(그냥접속으로 대체됨)
	@Deprecated
	public void reconnect() {
		
		// 기존의 호스트 장치로 접속한다.
		
	}

	// HOST, CLIENT 연결된 장치 목록을 모두 비우고 관련된 상태들을 처음 상태로 리셋
	// HOST, CLIENT 인지 여부는 제외
	// 이 메서드를 호출하고 나서 establish할 것인데 아직 필요한지는 모르겠네
	@Deprecated
	public void reset() {
		
	}

	// 호스트-클라이언트
	// 해당 장치 연결 해제 해당 장치 객체를 null로!
	@Deprecated
	public void release(BluetoothDevice device) {
		// 연결해제
		// 해당 장치 null
	}

	// 호스트-클라이언트
	// 모든 장치 연결해제
	// 모든 장치 객체를 null로!
	@Deprecated
	public void releaseAll() {
		// 연결해제
		// 해당 장치 null
	}

	// 호스트-클라이언트
	// 모든장치와 연결상태 해제, 리시버등
	public synchronized void clear() {
		Log.i(TAG, "블루투스 매니저 정리");
		initialized = false;
		
		// 종료할 때 해야하는일
		if(mConnectedThreads != null)
		for(int i = 0 ; i < mConnectedThreads.size() ; i++){
			mConnectedThreads.get(i).cancel();
		}
		
		// 리시버 등록해제등..
		//DistributedBoardgame.getInstance().getContext().unregisterReceiver(mReceiver);-----쌍으로 풀어야됨
		// releaseAll()도 부를것이고..
		
	}

	@Deprecated
	public void waitDevice(BluetoothDevice device, int MillisecTimeOut) {

		// 이미 연결된 장치면 바로 리턴
		// 디바이스가 클라이언트면 기다리고
		// 도구면 접속함
		// 완료시 보고함
	}
	
	// called by ThreadTimer

	// 타이머가 다되면 이걸 요청할껄
	public synchronized void onEstablishTimeOut() {
		Log.i(TAG, "블루투스 구성 타임아웃");
		mAcceptThread.cancel();
		if(hasClientsPerfectlyNominated == false){
			Log.w(TAG, "타임아웃됨을 노미네이트 - 블루투스 클라이언트");
			//CommunicationStateManager.getInstance().onEstablishTimeOut();
			CandidateManager.getInstance().nominateClientEstablishFail();
			
		}
		
	}

	// 바이트 배열을 해당 장치로 기록
	public synchronized void write(BluetoothDevice device, byte[] bytes) {
		
	}

	// 객체를 해당 장치로 기록
	public synchronized void write(BluetoothDevice device, Object obj) {
		if(getConnectedThreadOf(device) instanceof ConnectedRemoteAndroidThread)
			((ConnectedRemoteAndroidThread)getConnectedThreadOf(device)).write(obj);
		else
			Log.e(TAG, "해당 장치에 Object를 기록할 수 없습니다.");
	}
	
	//디버깅 메서드
	
	public void printBluetoothManagerStatusLog(){
		
		if(!PRINT_DEBUG_STATUS) 
			return;
		
		Log.d(TAG, "-------printStatusLog-------");
		
		Log.d(TAG, "mode : " + mode);
		Log.d(TAG, "hasClientPerfectlyNominated : " + hasClientsPerfectlyNominated);
		Log.d(TAG, "hasElectricYutPerfectlyNominated : " + hasElectricYutPerfectlyNominated);
		
		if(mConnectThreads != null){
			Log.d(TAG, "mConnectThreads is not null and size : " + mConnectThreads.size());
			for(int i = 0 ; i < mConnectThreads.size() ; i++){
				Log.d(TAG, "mConnectThreads[" + i + "] : " + (mConnectThreads.get(i) != null ? mConnectThreads.get(i).getRemoteDevice().getName() : null ));
			}
		}
		if(mConnectedThreads != null){
			Log.d(TAG, "mConnectedThreads is not null and size : " + mConnectedThreads.size());
			for(int i = 0 ; i < mConnectedThreads.size() ; i++){
				Log.d(TAG, "mConnectedThreads[" + i + "] : " + (mConnectedThreads.get(i) != null ? mConnectedThreads.get(i).getRemoteDevice().getName() : null ));
			}
		}
		if(deviceTypeMap != null){
			Log.d(TAG, "deviceTypeMap is not null and size : " + deviceTypeMap.size());
		}
		
		if(lostClientDevices != null)
			Log.d(TAG, "lostDevices is not null and size : " + lostClientDevices.size());
		
		Log.d(TAG, "totalCurrentClients : " + totalCurrentClients);
		
		if(clientConnectSuccessList != null)
			Log.d(TAG, "clientConnectSuccessList is not null and size : " + clientConnectSuccessList.length);
		
		Log.d(TAG, "totalReportOfSuccess : " + totalReportOfSuccess);
		Log.d(TAG, "connectIndexAsClient : " + connectIndexAsClient);
		Log.d(TAG, "underCommunicationHost : " + (underCommunicationHost != null ? underCommunicationHost.getName() : null));
		Log.d(TAG, "connectingHost : " + connectingHost);
		
		if(lostClientDevices != null){
			Log.d(TAG, "lostClientDevices is not null and size : " + lostClientDevices.size());
			for(int i = 0 ; i < lostClientDevices.size() ; i++){
				Log.d(TAG, "lostClientDevices[" + i + "] : " + (lostClientDevices.get(i) != null ? lostClientDevices.get(i).getName() : null ));
			}
		}
		
		if(lostIndexes != null){
			Log.d(TAG, "lostIndexes is not null and size : " + lostIndexes.size());
			for(int i = 0 ; i < lostIndexes.size() ; i++){
				Log.d(TAG, "lostIndexes[" + i + "] : " + lostIndexes.get(i));
			}
		}
		
		Log.d(TAG, "----------------------------");
	}
	
	// private 메서드, 클래스
	
	
	//null을 제외한 요소들만 추출하여 리스트 리턴
	
	@Deprecated
	private void ensureDiscoverable() {
        Log.d(TAG, "탐색 가능하게 함, 호스트모드");
        
        if(mode != Mode.HOST){
        	Log.e(TAG, "이 모드에서 호출할 수 없는 메서드");
        	return;
        }
        
        if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            DistributedBoardgame.getInstance().getContext().startActivity(discoverableIntent);
        }
   
	}
	
	//장치가 없으면, 즉 연결이 끊어진 상태면 null리턴
	private ConnectedThread getConnectedThreadOf(BluetoothDevice device){
		for(int i = 0 ; i < mConnectedThreads.size() ; i++){
			if(mConnectedThreads.get(i) != null){
				if(mConnectedThreads.get(i).getRemoteDevice().equals(device) == true){
					return mConnectedThreads.get(i);
				}
			}else{
				//적절한 처리
				//보고가 필요할 듯
				//사용할 수 없을 때
			}
		}
		return null;
	}
	
	//재합류가 아님, 8번 모두 시도
	private void connectAsClient(BluetoothDevice device) {
		//최초접속
		Log.i(TAG, "클라이언트로써 연결시도");
		
		connectingHost = device;
		
		//필요한것 초기화
		for(int i = 0 ; i < maxClients ; i++){
			instance.clientConnectSuccessList[i] = ConnectRemoteAndroidThread.CONNECT_NONE;//초기값 -1
		}
		totalReportOfSuccess = 0;
		
		
		ConnectRemoteAndroidThread currentConnectThread = null;
		mConnectedThreads = new ArrayList<ConnectedThread>();
		mConnectThreads = new ArrayList<ConnectRemoteAndroidThread>();
		
		// 접속
		
		for (int i = 0; i < maxClients; i++) {
			Log.d(TAG, "연결스레드 생성중, 이번 인덱스: " + i);
			currentConnectThread = new ConnectRemoteAndroidThread(device, i);
			mConnectThreads.add(currentConnectThread);
		}
		for (int i = 0; i < maxClients; i++) {
			Log.d(TAG, "연결스레드 시작, 이번 인덱스: " + i);
			mConnectThreads.get(i).start();
		}
		
	}

	//호스트, 클라이언트 - 최초접속
	private synchronized void onConnected(DeviceType deviceType, BluetoothSocket socket, BluetoothDevice device, int connectIndex) {
		
		Log.d(TAG, "연결됨 : " + device.getName());
	
		totalCurrentClients++;
		Log.i(TAG, "현재 클라이언트 총합 : " + totalCurrentClients);
		
		//소켓의 원격 장치와 인자의 원격 장치가 다르면 문제가 있다.
		if(socket.getRemoteDevice().equals(device) == false){
			Log.e(TAG, "소켓의 원격장치와 인자의 장치가 다름.");
		}
		
		// Start the thread to manage the connection and perform transmissions
		
		if (deviceType == DeviceType.CLIENT || deviceType == DeviceType.HOST) {
			Log.i(TAG, "클라이언트 또는 호스트 연결됨 " + DeviceType.HOST);
			mConnectedThreads.add(new ConnectedRemoteAndroidThread(socket, connectIndex));
			mConnectedThreads.get(mConnectedThreads.size() - 1).start();
			
			//연결된 장치의 타입의 종류에 대해
			if (deviceType == DeviceType.CLIENT) {//호스트 케이스
				Log.i(TAG, "호스트 케이스 온 커넥티드 : " + device.getName());
				deviceTypeMap.put(device, DeviceType.CLIENT);
				
				if(mode == Mode.HOST && totalCurrentClients == maxClients){
					hasClientsPerfectlyNominated = true;
					CandidateManager.getInstance().nominateClientDevices(getRemoteDevices());
				}
				
				CommunicationStateManager.getInstance().onConnected(device);//해당 장치의 타입이 클라이언트니, 호스트코드가 입력되어야함
				
			} else {// deviceType == DeviceType.HOST //클라이언트 케이스
				Log.i(TAG, "클라이언트 케이스 온 커넥티드 : " + device.getName());
				
				underCommunicationHost = device;//접속 기억하기
				
				deviceTypeMap.put(device, DeviceType.HOST);
				
				hasClientsPerfectlyNominated = true;
				
				CandidateManager.getInstance().nominateClientDevices(getRemoteDevices());
				CandidateManager.getInstance().nominateDice(new Die[0]);//얘가 보고하는게 좀 안맞긴해
				CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);//얘가 보고하는게 좀 안맞긴해
				
				//CommunicationStateManager.getInstance().onConnectionComplete(device);
			}
		} else if (deviceType == DeviceType.ELECTRIC_GAME_TOOL) {
			// 채워넣기...........
		}
		
		printBluetoothManagerStatusLog();
	}
	
	//호스트, 클라이언트 - 게임중 재접속
	private synchronized void onReconnected(BluetoothSocket socket, BluetoothDevice device, int connectIndex){
		Log.d(TAG, "onReconnected 진입");
		printBluetoothManagerStatusLog();
		

		Log.d(TAG, "다시 연결됨 : " + device.getName());
		if(lostClientDevices.indexOf(device) == -1){//끊어진적 없는 장치면
			Log.w(TAG, "기대하지 않은 재접속 : " + device.getName());
			Log.i(TAG, "다시 기다림 : " + device.getName());
			
			try {
                mAcceptThread.accept(connectIndex);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			
			return ;
		}
		
		mConnectedThreads.add(new ConnectedRemoteAndroidThread(socket, connectIndex));
		lostClientDevices.remove(device);
		lostIndexes.remove((Object)connectIndex);
		
		
		CommunicationStateManager.getInstance().onReconnected(device);
		
	}
	
	//클라이언트
	//이안에서 클라이언트의 연결실패에대한 보고가 이루어진다.
	private synchronized void reportSuccessOrFail(int connectIndex, int result) { // 성공이나
																					// 실패
																					// 여부를
																					// 보고할때
																					// 증가시킴
		clientConnectSuccessList[connectIndex] = result;
		totalReportOfSuccess++;

		boolean hasEveryConnectionFailed = true;

		if (totalReportOfSuccess == maxClients) {
			for (int i = 0; i < maxClients; i++) {
				if (clientConnectSuccessList[i] == ConnectRemoteAndroidThread.CONNECT_FAIL) {

					hasEveryConnectionFailed = true;

				} else { // NONE 또는 SUCCESS 사실상 SUCCESS
					hasEveryConnectionFailed = false;
					break;
				}
			}
			if (hasEveryConnectionFailed == true) {
				Log.i(TAG, "연결 실패 최종 보고");
				CommunicationStateManager.getInstance().onConnectionFail(connectingHost);//최종 실패 보고
				
				/*Log.i(TAG, "다시 연결중.." + connectThread.getRemoteDevice().getName());
				connect(connectThread.getRemoteDevice());*/
				
				
			}
		}
	}

	private synchronized void cancelAllConnectThreadExcept(
			ConnectRemoteAndroidThread connectThread) {
		Log.d(TAG,
				"성공한 연결 스레드 제외하고 모두 취소, 연결 스레드 크기: " + mConnectThreads.size()
						+ ", 제외 인덱스: " + mConnectThreads.indexOf(connectThread));

		int exceptIndex = -1;
		if (mConnectThreads != null) {
			exceptIndex = mConnectThreads.indexOf(connectThread);
		}

		for (int i = 0; i < mConnectThreads.size(); i++) {
			if (exceptIndex != i) {
				if (mConnectThreads.get(i) != null) {
					mConnectThreads.get(i).cancel();
					mConnectThreads.set(i, null);
				}
			}
		}
	}

	// 호스트, 클라이언트 모두 가능한 경우
	private synchronized void onConnectionLost(ConnectedThread connectedThread) {
	    int index = 0;//임시변수
	   
	    Log.d(TAG, "연결이 끊어짐"  + connectedThread.getConnectIndex());
	    
	    //0.공통 작업
	    //Log.d(TAG, "장치종류 맵핑 해제, 종류 : " + getTypeOf(connectedThread.getRemoteDevice()));
	    //deviceTypeMap.remove(connectedThread.getRemoteDevice()); //일단 안지워줘도 될 듯, 보드게임이 참조는 할만해
	    	
	    //1. 게임 준비기간(클라이언트)
	    if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
	    	Log.d(TAG, "연결이 끊어짐 : 클라이언트 합류모드"  + connectedThread.getConnectIndex());
	    	 
	    	totalCurrentClients--;
	    	index = mConnectedThreads.indexOf(connectedThread);
	       	mConnectedThreads.get(index).cancel();
	       	mConnectedThreads.remove(index);//이것과
	       	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());
	    }
	    //2. 게임 준비기간(호스트)
	    else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
	    	Log.d(TAG, "연결이 끊어짐 : 호스트 준비모드"  + connectedThread.getConnectIndex());
	    	
	    	totalCurrentClients--;
	    	
	    	hasClientsPerfectlyNominated = false;
	    	
	    	index = mConnectedThreads.indexOf(connectedThread);
	       	mConnectedThreads.get(index).cancel();
	       	mConnectedThreads.remove(index);//이것의 차이 -
	       	
	       	//Log.d(TAG, "연결이 끊어짐 : 호스트 수락 인덱스 회귀 :" + connectIndexAsClient);
	       	//mAcceptThread.setCurrentAcceptIndex(connectIndexAsClient);//인덱스 회귀해서 다시 기다림
	       	
	       	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());

	       	try {
	       		Log.i(TAG, "연결끊어진 인덱스 다시 기다림 : " + connectedThread.getConnectIndex());
				mAcceptThread.accept(connectedThread.getConnectIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
	       	
	       	
	    }
	    //3. 게임중(호스트/클라이언트)
	    else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
	    	Log.d(TAG, "게임상태에서 연결 끊어짐" + connectedThread.getConnectIndex());
	    	
	    	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());
	    	
	    	if(mode == Mode.HOST){
	    		
	    		Log.d(TAG , "호스트 게임중 상태에서 연결 끊어짐");
	    		totalCurrentClients--;//현재 접속된 클라이언트 총합 변경
	    		lostClientDevices.add(connectedThread.getRemoteDevice());//잃어버린 장치 추가
	    		lostIndexes.add(connectedThread.getConnectIndex());
	    		
	    		Log.d(TAG, "재수락 중");
	    		
	    		//mAcceptThread = new AcceptThread();
	    		//mAcceptThread.start();
	    		
	    		try {
                    mAcceptThread.accept(connectedThread.getConnectIndex());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	    		
	    		connectedThread.cancel();//끊어진놈 취소
	    		connectedThread = null;
	    	}else if(mode == Mode.CLIENT){
	    		
	    		Log.d(TAG , "클라이언트 게임중 상태에서 연결 끊어짐");
	    	}else{
	    		Log.w(TAG, "언노운 모드");
	    	}
	    }else{
	    	Log.e(TAG, "정의되지 않았거나, 기대되지 않은 상태에서 연결 끊어짐");
	    }
	    
	    printBluetoothManagerStatusLog();
	}
	
	//클라이언트
	//호스트로 연결 실패(전자 도구의 경우 생각해줘야하나)
	private synchronized void onConnectionFailed(ConnectThread connectThread) {
		Log.d(TAG, "연결실패");

		if (connectThread != null) {
			connectThread.cancel();
			
			connectThread = null;
		}else{
			Log.e(TAG, "인자 connectThread가 null");
		}
		printBluetoothManagerStatusLog();
	}
		
	private class AcceptThread extends Thread {
		
		private static final String TAG = "20083271:AcceptThread";
		
		private BluetoothServerSocket mmServerSocket;
		
		//private int currentAcceptIndex = -1; // 구성중 중간에 끊어진놈들은 이값을 조작함으로써 회귀한다.
		
		private boolean terminated = false;//강제종료하고싶을때
		
		public AcceptThread() {

		}
		
		public void run() {

			//currentAcceptIndex = -1;//-1이 아닌경우 해당 차례에 해당 인덱스로 계산할 것
			
			// Create a new listening server socket
			try {
				if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
					Log.i(TAG, "호스트 준비 상태에서 수락스레드 시작");
					for (int i = 0; i < maxClients; i++) {
						if(terminated == true){//어디서 종료표시(cancel안에있음) 해주면
							this.cancel();
							return;//스레드가 리턴할까?
						}
					
						//mConnectedThreads의 크기는 구성중에 절대 줄어들지 않는다.
					
						accept(i);
					
					}
				}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
					Log.i(TAG, "호스트 게임중 상태에서 수락스레드  시작");
					
					if(lostClientDevices.size() < 1 || lostIndexes.size() < 1){//문제가 있다. "기다릴" 끊어진 장치가 없는 상황 : 말이안되
						Log.e(TAG, "끊어진 장치가 없습니다. lostClientDevices를 확인하세요.");
					}
							
					//mConnectedThreads의 크기는 구성중에 절대 줄어들지 않는다.
					
					Log.i(TAG, "재수락중 인덱스는 : " + lostIndexes.get(0));
					accept(lostIndexes.get(0));
					
					/////////////////////////////////////////////////
					
				}
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
		}
		
		public void accept(int currentIndex) throws IOException{
			synchronized (BluetoothManager.this) {	//결국 메서드에 sync 하는것도 똑같을듯.
				Log.i(TAG, "현재의 수락중 인덱스 : " + currentIndex);
				mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("BluetoothManagerIn?secure", uuids.get(currentIndex));
				BluetoothSocket socket = null;
				
				try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				if(mmServerSocket != null)
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "current accept() failed", e);
					return;
				}

				// If a connection was accepted
				if (socket != null) {
					if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE || DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE)
						onConnected(DeviceType.CLIENT, socket, socket.getRemoteDevice(), currentIndex);//상대방은 클라이언트
					else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME)
						onReconnected(socket, socket.getRemoteDevice(), currentIndex);
				}
				mmServerSocket.close();
				mmServerSocket = null;
				socket = null;
			}

		}
		
		public void cancel() {
			Log.i(TAG, "수락 스레드가 취소되었습니다.");
			try {
				if(mmServerSocket != null)
					mmServerSocket.close();
				terminated = true;
			} catch (IOException e) {
				Log.e(TAG, "서버의 close() 실패", e);
			}
		}
			
		/*public void setCurrentAcceptIndex(int index){
			synchronized (BluetoothManager.this) {
				this.currentAcceptIndex = index;
			}
		}*/
	}
	
	//클라이언트
	//호스트로 접속
	private class ConnectRemoteAndroidThread extends ConnectThread {
		
		private static final String TAG = "20083271:ConnectThread";
		
		public static final int CONNECT_SUCCESS = 1;
		public static final int CONNECT_FAIL = 0;
		public static final int CONNECT_NONE = -1;
		
		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private int connectIndex;
		
		public ConnectRemoteAndroidThread(BluetoothDevice device, int connectIndex) {
			Log.d(TAG, "새로운 연결 스레드, 인덱스: " + connectIndex);

			this.connectIndex = connectIndex;
			mmDevice = device;

			try {

				mmSocket = device.createRfcommSocketToServiceRecord(uuids.get(connectIndex));

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
				synchronized (BluetoothManager.this) {
					mmSocket.connect();
				}
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + " socket during connection failure", e2);
				}
				// ////////////////////////////
				synchronized (BluetoothManager.this) {
					Log.i(TAG, "connect fail " + connectIndex + " " + e);
					onConnectionFailed(this);// 연결 시도하다가 실패
					reportSuccessOrFail(connectIndex, ConnectRemoteAndroidThread.CONNECT_FAIL);
				}

				return;
			}

			synchronized (BluetoothManager.this) {
				Log.i(TAG, "연결 성공, 인덱스: " + connectIndex);
				
				//나중에 재합류할 때 사용할 정보 저장
				
				/**
				 * 이 정보중 첫번째는 "게임중" 일때 사용해야됨
				 */
				connectIndexAsClient = connectIndex;//클라이언트로써 몇번 인덱스의 uuid로 성공했는지 기억해 놓는다.

				//////////////////저장 끝////////
				
				cancelAllConnectThreadExcept(this);
				reportSuccessOrFail(connectIndex, ConnectRemoteAndroidThread.CONNECT_SUCCESS);
				onConnected(DeviceType.HOST, mmSocket, mmDevice, connectIndex);//상대방은 호스트		
			}
		}
		
		public void cancel() {
			try {
				mmSocket.close();
				reportSuccessOrFail(connectIndex, ConnectRemoteAndroidThread.CONNECT_FAIL);
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
		
		public BluetoothDevice getRemoteDevice(){
			return mmDevice;
		}
		
	}
	
	private class ConnectedRemoteAndroidThread extends ConnectedThread {

		private static final String TAG = "20083271:ConnectedRemoteAndroidThread";

		private int connectIndex = -1;
		// private BluetoothDevice mmDevice;
		private BluetoothSocket mmSocket;

		private ObjectInputStream mmInStream;
		private ObjectOutputStream mmOutStream;

		public ConnectedRemoteAndroidThread(BluetoothSocket socket, int connectIndex) {
			Log.d(TAG, "원격 안드로이드 연결된 스레드 생성자");
			// mmDevice = device;
			mmSocket = socket;
			this.connectIndex = connectIndex;
			ObjectInputStream tmpIn = null;
			ObjectOutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpOut = new ObjectOutputStream(socket.getOutputStream());
				tmpOut.flush();
				tmpIn = new ObjectInputStream(socket.getInputStream());
				

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
					CommunicationStateManager.getInstance().onObjectDelivered(mmSocket.getRemoteDevice(),mmInStream.readObject());

				} catch (IOException e) {
					Log.e(TAG, "연결 끊어짐", e);
					onConnectionLost(this);// 연결 끊어지면... 실행
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		public void write(Object obj) {
			Log.i(TAG, "객체 쓰기 : " + obj);
			try {
			    
			    synchronized (obj){
			        mmOutStream.flush();
			        mmOutStream.writeObject(obj);
			        mmOutStream.flush();
			    }
				// mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1,
				// buffer).sendToTarget();
				// Share the sent message back to the UI Activity
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

		@Override
		public BluetoothDevice getRemoteDevice() {
			
			return mmSocket.getRemoteDevice();
		}
		
		public int getConnectIndex(){
			Log.d(TAG, "getConnectIndex 호출됨" + mmSocket.getRemoteDevice().getName() + "의 연결 인덱스는 : " + connectIndex);
			return connectIndex;
		}
	}
	
	abstract class ConnectedThread extends Thread {
		public abstract BluetoothDevice getRemoteDevice();
		//public abstract void write(Object obj);
		public abstract void cancel();
		public abstract int getConnectIndex();
	}
	
	abstract class ConnectThread extends Thread{
		public abstract BluetoothDevice getRemoteDevice();
		public abstract void cancel();
	}
	
}
