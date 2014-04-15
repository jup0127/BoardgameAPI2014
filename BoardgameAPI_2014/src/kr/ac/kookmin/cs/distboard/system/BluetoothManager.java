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

public class BluetoothManager {// �̱���

	private static BluetoothManager instance = new BluetoothManager();

	private final static String TAG = "20083271:BluetoothManager";
	private final static boolean PRINT_DEBUG_STATUS = true;

	private boolean initialized = false;// ��尡 �ʱ�ȭ �Ǿ�����

	private Mode mode = Mode.NONE;// ������� �Ŵ����� �۵� ���(ȣ��Ʈ vs Ŭ���̾�Ʈ)
	private int maxClients = 0;
	private int exactYutGameTools = 0;
	private int millisecTimeout = 0;
	
	private boolean hasClientsPerfectlyNominated = false;
	private boolean hasElectricYutPerfectlyNominated = false;

	// �Ʒ��� ȣ��Ʈ�����̶�� �������� ��ġ, Ŭ���̾�Ʈ �����̶�� �Ѱ��� ��ġ�� �ǰ���
	
	private ArrayList<UUID> uuids = new ArrayList<UUID>();

	// Member fields
	private BluetoothAdapter mAdapter = null;

	
	BroadcastReceiver mReceiver = null;
	
	private AcceptThread mAcceptThread;
	private ArrayList<ConnectRemoteAndroidThread> mConnectThreads;
	private ArrayList<ConnectedThread> mConnectedThreads;
	private HashMap<BluetoothDevice, DeviceType> deviceTypeMap;
	
	//���� ������ġ ���
	private ArrayList<BluetoothDevice> lostClientDevices;
	//���� ���� UUID �ε���
	private ArrayList<Integer> lostIndexes;
	
	
	//ȣ��Ʈ�� �ν��Ͻ� ����
	
	private int totalCurrentClients = 0;
	
	
	//Ŭ���̾�Ʈ�� �ν��Ͻ� ����
	
	private int[] clientConnectSuccessList = null; // 0�� ����
													// 1�� ����
													// �ʱⰪ��
													// -1
	private int totalReportOfSuccess = 0;
	private int connectIndexAsClient = -1;//���Ἲ���� ���� ���ٴ� ǥ��, �ٲ��� �� ��,
	//�� ���� �����߿� ���շ��� ���ǰų�, ȣ��Ʈ�� ������ ���� Ŭ���̾�Ʈ �ε����� ������� ����
	private BluetoothDevice underCommunicationHost = null;//���� �������� �� �� ����ϴ� ȣ��Ʈ
	private BluetoothDevice connectingHost = null;//�������� ȣ��Ʈ
	
	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// ������
	private BluetoothManager() {
	}

	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// �ʱ�ȭ �� ��������Ŵ����� �ν��Ͻ��� ����ϴ�.
	public static BluetoothManager getInstance() {
		return instance;
	}

	public void initialize(Mode mode, int maxClients, int exactYutGameTools, int millisecTimeout) {
		if (instance.initialized == true)
			Log.w(TAG, "�̹� �ʱ�ȭ�� ���� �ֽ��ϴ�.");

		mAdapter = BluetoothAdapter.getDefaultAdapter();
		instance.initialized = true;

		/*mReceiver = new BroadcastReceiver() {//��ϵ� ���ķ� ���ú� ����! - ������ �ּ� Ǯ ��
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d(TAG, "��� ����");
				
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					
					Log.d(TAG, "�˸��� �׼��� ���Ե� ���!");
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.NEW_DEVICE_DISCOVERED, device).sendToTarget();
				}
			}
		};*/
		
		
		mAcceptThread = null;
		mConnectedThreads = null;
		//mConnectThreads = new ArrayList<ConnectThread>(); - ������ ���ӿ��ε�, ��� ������ ���� �𸣰���
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
			this.clientConnectSuccessList[i] = -1;// �ʱⰪ -1
		}
		
		totalCurrentClients = 0;
		
		
		totalReportOfSuccess = 0;
		connectIndexAsClient = -1;
		underCommunicationHost = null;
		connectingHost = null;

		Log.i(TAG, "������� �Ŵ����� �ʱ�ȭ�Ǿ����ϴ� : " + mode);
	}


	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// �Ķ������ ��ġ�� ���� Ÿ���� �����Ѵ�.
	// Ÿ�� : ȣ��Ʈ OR Ŭ���̾�Ʈ OR ���ڵ���
	// DeviceType ������ ����
	public synchronized DeviceType getTypeOf(BluetoothDevice device) {
		Log.i(TAG, "Ÿ�Ծ�� �޼��� ����");
		if(deviceTypeMap.containsKey(device) == false){
			Log.w(TAG, "�̹� ������ Ű�� ���� �˻�");
		}
		return deviceTypeMap.get(device);
	}

	//���� ���۰�������, �� ����Ǿ�����
	public synchronized boolean isAvailableDevice(BluetoothDevice device){
	    for(int i = 0 ; i < mConnectedThreads.size() ; i++){
	       if(device.equals(mConnectedThreads.get(i).getRemoteDevice())){
	           return true;
	       }
	    }
	    return false;
	}
	
	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// ����� ��ġ ����Ʈ�� �����մϴ�.
	public BluetoothDevice[] getRemoteDevices() {
		
		ArrayList<ConnectedThread> currentConnectedThread = mConnectedThreads;
		ArrayList<BluetoothDevice> currentRemoteDevices = new ArrayList<BluetoothDevice>();

		for(int i = 0 ; i < currentConnectedThread.size() ; i++){
			if(currentConnectedThread.get(i) != null)//null�ϼ��ִ�. ������ ���ܼ� �߰��� ���۳� �����϶�
				currentRemoteDevices.add(currentConnectedThread.get(i).getRemoteDevice());
			else{ // currentConnectedThread.get(i) �� null
				Log.w(TAG, "currentConnectedThread.get(i)�� null�Դϴ�. �̰��� ������ �� �ֽ��ϴ�.");
			}
		}
		
		return ArrayListConverter.bluetoothDeviceArrayListToArray(currentRemoteDevices);
	}

	// ȣ��Ʈ
	// Ŭ���̾�Ʈ ��ġ ����� ����ϴ�.
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

	// Ŭ���̾�Ʈ
	// ȣ��Ʈ ��ġ�� ����ϴ�.
	public BluetoothDevice getHostDevice() {
		BluetoothDevice[] remoteDevices = getRemoteDevices();
		for(int i = 0 ; i < remoteDevices.length ; i++){
			if(remoteDevices[i] != null && getTypeOf(remoteDevices[i]) == DeviceType.HOST){
				return remoteDevices[i];
			}
		}
		return null;
	}

	// ȣ��Ʈ
	// ���ڰ��ӵ��� ��ġ����� ����ϴ�.
	public BluetoothDevice[] getElectricGameToolDevices() {
		return null;
	}

	// Ŭ���̾�Ʈ
	// �ֺ� ��ġ�� �˻��ؼ� �����մϴ�.
	public BluetoothDevice[] getDiscoveredDevices() {
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
		
		final ArrayList<BluetoothDevice> mPairedList = new ArrayList<BluetoothDevice>();
		
		Log.d(TAG, "���� ��⸦ ã���ϴ�.");
		
		// �� ���ִ� ���
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				mPairedList.add(device);
			}
		}
		
		// ��ġ �˻����̸� �˻� ���
		if (mAdapter.isDiscovering())
			mAdapter.cancelDiscovery();
		
		Log.d(TAG, "�ֺ���ġ �˻��� �����մϴ�.");
		
		// �ֺ� ��ġ �˻� ����
		
		/*mAdapter.startDiscovery();
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		
		DistributedBoardgame.getInstance().getContext().registerReceiver(mReceiver, filter);*///�������Ϳ���-�̰ų����� ������ �������͵� �ּ� Ǯ ��
		
		return ArrayListConverter.bluetoothDeviceArrayListToArray(mPairedList);
	}

	// ȣ��Ʈ
	// ȣ��Ʈ ���忡�� Ŭ���̾�Ʈ�� ��ٸ��� ������ ���ӽõ��մϴ�.
	// �� Ŭ���̾�Ʈ ����� �����մϴ�.
	// �ð� �ʰ��� �����մϴ�.
	// �Ϸ�� �����մϴ�.
	// 0���� Ŭ���̾�Ʈ Ȥ�� 0���� ���� ���ڷ� ���� ���̰�(0)�� ����ִ� �迭�� �����ϸ��.
	public void establish() {
		Log.i(TAG, "������� �Ŵ��� ���� ����");
		
		if(maxClients == 0){
			//���� �� �ٷ� ����
			hasClientsPerfectlyNominated = true;
			CandidateManager.getInstance().nominateClientDevices(new BluetoothDevice[0]);
			//CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new Die[0]);
			return;
		}
		
		//ensureDiscoverable();
		mConnectedThreads = new ArrayList<ConnectedThread>();
		
		
		ThreadTimer timer = new ThreadTimer(ThreadTimer.BLUETOOTH_MANAGER_ESTABLISH_CALL, millisecTimeout);
		timer.start();

		// Ÿ�Ӿƿ� ����

		mAcceptThread = new AcceptThread();
		mAcceptThread.start();
		
		//Log.i(TAG, "������ �ӽ� ��̳���Ʈ ����");
		//CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);
		//Log.i(TAG, "������ �ӽ� ��̳���Ʈ ��");
		
		// asd
	}

	// ȣ��Ʈ
	// �籸��
	// �� �޼���� �θ����� ���� ��
	@Deprecated
	public void reEstablish() {
		// ù establish �� ������������ �̿��Ͽ� �ٽ� ��ٸ�.
		// �̹� ���ӵȳ𸻰� ���Ӿȵǰų� ������ ��ٸ�
		// ������ �������� ��ġ ���ӽõ��� ���� ó��
	}

	// ȣ��Ʈ
	// ���� ���ӵ� ���� ���������� �Ϸ��Ѵ�.(������)
	// ������� �ּ�2�� �ִ�4���� ���ӿ��� 3�����ӽ� �����ϰ������ �� �޼��尡 ȣ��ǰ� ������ 3���� �����ϴ°ŷ� �����ȴ�.
	// �̸޼��� ���Ŀ� getDevices() ȣ���ϸ� Ŭ���̾�Ʈ�� 3ĭ¥�� �迭�̰���
	public void forceClientEstablishment() {
	    Log.i(TAG, "Ŭ���̾�Ʈ ���� ���� ����");
	    
		if(hasClientsPerfectlyNominated == false){
			
			hasClientsPerfectlyNominated = true;//Ȯ���� �ĺ�ȭ�Ǿ����� ���� ����
			
			for(int i = 0 ; i < mConnectedThreads.size() ; i++){
				if(mConnectedThreads.get(i) == null){
				    Log.e(TAG, "�̰��� null�̸� �ȵ� ����Ƚ����� �ε����� : " + i);
					mConnectedThreads.remove(i);
				}
			}
			
			mAcceptThread.cancel();
			
			maxClients = mConnectedThreads.size();//Ŭ���̾�Ʈ �� ���� ����	
			//////////////////////////////////////////////////////////////
			
			Log.i(TAG, "Ŭ���̾�Ʈ ��̳���Ʈ �� : ��ġ ���̴� : " + getClientDevices().length);
			CandidateManager.getInstance().nominateClientDevices(getClientDevices());
			
			CommunicationStateManager.getInstance().onPlayerEstablishForceComplete();
			
		}
	}
	
	@Deprecated
	public synchronized void completeEstablishment(){
		
	}


	// Ÿ�̸Ӱ� ȣ���ؾ��ҵ�
	public void cancel() {
		Log.i(TAG, "������� ���� ���");
	}

	// Ŭ���̾�Ʈ
	// ����
	//������ �ʱ�ȭ(���շ� ����, ����Ǿ��� ȣ��Ʈ ����)
	public void connect(BluetoothDevice device) {
		// �ʿ��Ѱ� �ʱ�ȭ
		for (int i = 0; i < maxClients; i++) {
			instance.clientConnectSuccessList[i] = ConnectRemoteAndroidThread.CONNECT_NONE;// �ʱⰪ
																				// -1
		}
		totalReportOfSuccess = 0;
		connectIndexAsClient = -1;
		underCommunicationHost = null;
		connectingHost = null;
		
		// ����
		
		connectAsClient(device);
		
		
	}

	// CLIENT ������(�׳��������� ��ü��)
	@Deprecated
	public void reconnect() {
		
		// ������ ȣ��Ʈ ��ġ�� �����Ѵ�.
		
	}

	// HOST, CLIENT ����� ��ġ ����� ��� ���� ���õ� ���µ��� ó�� ���·� ����
	// HOST, CLIENT ���� ���δ� ����
	// �� �޼��带 ȣ���ϰ� ���� establish�� ���ε� ���� �ʿ������� �𸣰ڳ�
	@Deprecated
	public void reset() {
		
	}

	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// �ش� ��ġ ���� ���� �ش� ��ġ ��ü�� null��!
	@Deprecated
	public void release(BluetoothDevice device) {
		// ��������
		// �ش� ��ġ null
	}

	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// ��� ��ġ ��������
	// ��� ��ġ ��ü�� null��!
	@Deprecated
	public void releaseAll() {
		// ��������
		// �ش� ��ġ null
	}

	// ȣ��Ʈ-Ŭ���̾�Ʈ
	// �����ġ�� ������� ����, ���ù���
	public synchronized void clear() {
		Log.i(TAG, "������� �Ŵ��� ����");
		initialized = false;
		
		// ������ �� �ؾ��ϴ���
		if(mConnectedThreads != null)
		for(int i = 0 ; i < mConnectedThreads.size() ; i++){
			mConnectedThreads.get(i).cancel();
		}
		
		// ���ù� ���������..
		//DistributedBoardgame.getInstance().getContext().unregisterReceiver(mReceiver);-----������ Ǯ��ߵ�
		// releaseAll()�� �θ����̰�..
		
	}

	@Deprecated
	public void waitDevice(BluetoothDevice device, int MillisecTimeOut) {

		// �̹� ����� ��ġ�� �ٷ� ����
		// ����̽��� Ŭ���̾�Ʈ�� ��ٸ���
		// ������ ������
		// �Ϸ�� ������
	}
	
	// called by ThreadTimer

	// Ÿ�̸Ӱ� �ٵǸ� �̰� ��û�Ҳ�
	public synchronized void onEstablishTimeOut() {
		Log.i(TAG, "������� ���� Ÿ�Ӿƿ�");
		mAcceptThread.cancel();
		if(hasClientsPerfectlyNominated == false){
			Log.w(TAG, "Ÿ�Ӿƿ����� ��̳���Ʈ - ������� Ŭ���̾�Ʈ");
			//CommunicationStateManager.getInstance().onEstablishTimeOut();
			CandidateManager.getInstance().nominateClientEstablishFail();
			
		}
		
	}

	// ����Ʈ �迭�� �ش� ��ġ�� ���
	public synchronized void write(BluetoothDevice device, byte[] bytes) {
		
	}

	// ��ü�� �ش� ��ġ�� ���
	public synchronized void write(BluetoothDevice device, Object obj) {
		if(getConnectedThreadOf(device) instanceof ConnectedRemoteAndroidThread)
			((ConnectedRemoteAndroidThread)getConnectedThreadOf(device)).write(obj);
		else
			Log.e(TAG, "�ش� ��ġ�� Object�� ����� �� �����ϴ�.");
	}
	
	//����� �޼���
	
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
	
	// private �޼���, Ŭ����
	
	
	//null�� ������ ��ҵ鸸 �����Ͽ� ����Ʈ ����
	
	@Deprecated
	private void ensureDiscoverable() {
        Log.d(TAG, "Ž�� �����ϰ� ��, ȣ��Ʈ���");
        
        if(mode != Mode.HOST){
        	Log.e(TAG, "�� ��忡�� ȣ���� �� ���� �޼���");
        	return;
        }
        
        if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            DistributedBoardgame.getInstance().getContext().startActivity(discoverableIntent);
        }
   
	}
	
	//��ġ�� ������, �� ������ ������ ���¸� null����
	private ConnectedThread getConnectedThreadOf(BluetoothDevice device){
		for(int i = 0 ; i < mConnectedThreads.size() ; i++){
			if(mConnectedThreads.get(i) != null){
				if(mConnectedThreads.get(i).getRemoteDevice().equals(device) == true){
					return mConnectedThreads.get(i);
				}
			}else{
				//������ ó��
				//���� �ʿ��� ��
				//����� �� ���� ��
			}
		}
		return null;
	}
	
	//���շ��� �ƴ�, 8�� ��� �õ�
	private void connectAsClient(BluetoothDevice device) {
		//��������
		Log.i(TAG, "Ŭ���̾�Ʈ�ν� ����õ�");
		
		connectingHost = device;
		
		//�ʿ��Ѱ� �ʱ�ȭ
		for(int i = 0 ; i < maxClients ; i++){
			instance.clientConnectSuccessList[i] = ConnectRemoteAndroidThread.CONNECT_NONE;//�ʱⰪ -1
		}
		totalReportOfSuccess = 0;
		
		
		ConnectRemoteAndroidThread currentConnectThread = null;
		mConnectedThreads = new ArrayList<ConnectedThread>();
		mConnectThreads = new ArrayList<ConnectRemoteAndroidThread>();
		
		// ����
		
		for (int i = 0; i < maxClients; i++) {
			Log.d(TAG, "���ὺ���� ������, �̹� �ε���: " + i);
			currentConnectThread = new ConnectRemoteAndroidThread(device, i);
			mConnectThreads.add(currentConnectThread);
		}
		for (int i = 0; i < maxClients; i++) {
			Log.d(TAG, "���ὺ���� ����, �̹� �ε���: " + i);
			mConnectThreads.get(i).start();
		}
		
	}

	//ȣ��Ʈ, Ŭ���̾�Ʈ - ��������
	private synchronized void onConnected(DeviceType deviceType, BluetoothSocket socket, BluetoothDevice device, int connectIndex) {
		
		Log.d(TAG, "����� : " + device.getName());
	
		totalCurrentClients++;
		Log.i(TAG, "���� Ŭ���̾�Ʈ ���� : " + totalCurrentClients);
		
		//������ ���� ��ġ�� ������ ���� ��ġ�� �ٸ��� ������ �ִ�.
		if(socket.getRemoteDevice().equals(device) == false){
			Log.e(TAG, "������ ������ġ�� ������ ��ġ�� �ٸ�.");
		}
		
		// Start the thread to manage the connection and perform transmissions
		
		if (deviceType == DeviceType.CLIENT || deviceType == DeviceType.HOST) {
			Log.i(TAG, "Ŭ���̾�Ʈ �Ǵ� ȣ��Ʈ ����� " + DeviceType.HOST);
			mConnectedThreads.add(new ConnectedRemoteAndroidThread(socket, connectIndex));
			mConnectedThreads.get(mConnectedThreads.size() - 1).start();
			
			//����� ��ġ�� Ÿ���� ������ ����
			if (deviceType == DeviceType.CLIENT) {//ȣ��Ʈ ���̽�
				Log.i(TAG, "ȣ��Ʈ ���̽� �� Ŀ��Ƽ�� : " + device.getName());
				deviceTypeMap.put(device, DeviceType.CLIENT);
				
				if(mode == Mode.HOST && totalCurrentClients == maxClients){
					hasClientsPerfectlyNominated = true;
					CandidateManager.getInstance().nominateClientDevices(getRemoteDevices());
				}
				
				CommunicationStateManager.getInstance().onConnected(device);//�ش� ��ġ�� Ÿ���� Ŭ���̾�Ʈ��, ȣ��Ʈ�ڵ尡 �ԷµǾ����
				
			} else {// deviceType == DeviceType.HOST //Ŭ���̾�Ʈ ���̽�
				Log.i(TAG, "Ŭ���̾�Ʈ ���̽� �� Ŀ��Ƽ�� : " + device.getName());
				
				underCommunicationHost = device;//���� ����ϱ�
				
				deviceTypeMap.put(device, DeviceType.HOST);
				
				hasClientsPerfectlyNominated = true;
				
				CandidateManager.getInstance().nominateClientDevices(getRemoteDevices());
				CandidateManager.getInstance().nominateDice(new Die[0]);//�갡 �����ϴ°� �� �ȸ±���
				CandidateManager.getInstance().nominateYutDevices(new BluetoothDevice[0]);//�갡 �����ϴ°� �� �ȸ±���
				
				//CommunicationStateManager.getInstance().onConnectionComplete(device);
			}
		} else if (deviceType == DeviceType.ELECTRIC_GAME_TOOL) {
			// ä���ֱ�...........
		}
		
		printBluetoothManagerStatusLog();
	}
	
	//ȣ��Ʈ, Ŭ���̾�Ʈ - ������ ������
	private synchronized void onReconnected(BluetoothSocket socket, BluetoothDevice device, int connectIndex){
		Log.d(TAG, "onReconnected ����");
		printBluetoothManagerStatusLog();
		

		Log.d(TAG, "�ٽ� ����� : " + device.getName());
		if(lostClientDevices.indexOf(device) == -1){//�������� ���� ��ġ��
			Log.w(TAG, "������� ���� ������ : " + device.getName());
			Log.i(TAG, "�ٽ� ��ٸ� : " + device.getName());
			
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
	
	//Ŭ���̾�Ʈ
	//�̾ȿ��� Ŭ���̾�Ʈ�� ������п����� ���� �̷������.
	private synchronized void reportSuccessOrFail(int connectIndex, int result) { // �����̳�
																					// ����
																					// ���θ�
																					// �����Ҷ�
																					// ������Ŵ
		clientConnectSuccessList[connectIndex] = result;
		totalReportOfSuccess++;

		boolean hasEveryConnectionFailed = true;

		if (totalReportOfSuccess == maxClients) {
			for (int i = 0; i < maxClients; i++) {
				if (clientConnectSuccessList[i] == ConnectRemoteAndroidThread.CONNECT_FAIL) {

					hasEveryConnectionFailed = true;

				} else { // NONE �Ǵ� SUCCESS ��ǻ� SUCCESS
					hasEveryConnectionFailed = false;
					break;
				}
			}
			if (hasEveryConnectionFailed == true) {
				Log.i(TAG, "���� ���� ���� ����");
				CommunicationStateManager.getInstance().onConnectionFail(connectingHost);//���� ���� ����
				
				/*Log.i(TAG, "�ٽ� ������.." + connectThread.getRemoteDevice().getName());
				connect(connectThread.getRemoteDevice());*/
				
				
			}
		}
	}

	private synchronized void cancelAllConnectThreadExcept(
			ConnectRemoteAndroidThread connectThread) {
		Log.d(TAG,
				"������ ���� ������ �����ϰ� ��� ���, ���� ������ ũ��: " + mConnectThreads.size()
						+ ", ���� �ε���: " + mConnectThreads.indexOf(connectThread));

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

	// ȣ��Ʈ, Ŭ���̾�Ʈ ��� ������ ���
	private synchronized void onConnectionLost(ConnectedThread connectedThread) {
	    int index = 0;//�ӽú���
	   
	    Log.d(TAG, "������ ������"  + connectedThread.getConnectIndex());
	    
	    //0.���� �۾�
	    //Log.d(TAG, "��ġ���� ���� ����, ���� : " + getTypeOf(connectedThread.getRemoteDevice()));
	    //deviceTypeMap.remove(connectedThread.getRemoteDevice()); //�ϴ� �������൵ �� ��, ��������� ������ �Ҹ���
	    	
	    //1. ���� �غ�Ⱓ(Ŭ���̾�Ʈ)
	    if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
	    	Log.d(TAG, "������ ������ : Ŭ���̾�Ʈ �շ����"  + connectedThread.getConnectIndex());
	    	 
	    	totalCurrentClients--;
	    	index = mConnectedThreads.indexOf(connectedThread);
	       	mConnectedThreads.get(index).cancel();
	       	mConnectedThreads.remove(index);//�̰Ͱ�
	       	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());
	    }
	    //2. ���� �غ�Ⱓ(ȣ��Ʈ)
	    else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
	    	Log.d(TAG, "������ ������ : ȣ��Ʈ �غ���"  + connectedThread.getConnectIndex());
	    	
	    	totalCurrentClients--;
	    	
	    	hasClientsPerfectlyNominated = false;
	    	
	    	index = mConnectedThreads.indexOf(connectedThread);
	       	mConnectedThreads.get(index).cancel();
	       	mConnectedThreads.remove(index);//�̰��� ���� -
	       	
	       	//Log.d(TAG, "������ ������ : ȣ��Ʈ ���� �ε��� ȸ�� :" + connectIndexAsClient);
	       	//mAcceptThread.setCurrentAcceptIndex(connectIndexAsClient);//�ε��� ȸ���ؼ� �ٽ� ��ٸ�
	       	
	       	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());

	       	try {
	       		Log.i(TAG, "��������� �ε��� �ٽ� ��ٸ� : " + connectedThread.getConnectIndex());
				mAcceptThread.accept(connectedThread.getConnectIndex());
			} catch (IOException e) {
				e.printStackTrace();
			}
	       	
	       	
	    }
	    //3. ������(ȣ��Ʈ/Ŭ���̾�Ʈ)
	    else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
	    	Log.d(TAG, "���ӻ��¿��� ���� ������" + connectedThread.getConnectIndex());
	    	
	    	CommunicationStateManager.getInstance().onConnectionLost(connectedThread.getRemoteDevice());
	    	
	    	if(mode == Mode.HOST){
	    		
	    		Log.d(TAG , "ȣ��Ʈ ������ ���¿��� ���� ������");
	    		totalCurrentClients--;//���� ���ӵ� Ŭ���̾�Ʈ ���� ����
	    		lostClientDevices.add(connectedThread.getRemoteDevice());//�Ҿ���� ��ġ �߰�
	    		lostIndexes.add(connectedThread.getConnectIndex());
	    		
	    		Log.d(TAG, "����� ��");
	    		
	    		//mAcceptThread = new AcceptThread();
	    		//mAcceptThread.start();
	    		
	    		try {
                    mAcceptThread.accept(connectedThread.getConnectIndex());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	    		
	    		connectedThread.cancel();//�������� ���
	    		connectedThread = null;
	    	}else if(mode == Mode.CLIENT){
	    		
	    		Log.d(TAG , "Ŭ���̾�Ʈ ������ ���¿��� ���� ������");
	    	}else{
	    		Log.w(TAG, "���� ���");
	    	}
	    }else{
	    	Log.e(TAG, "���ǵ��� �ʾҰų�, ������ ���� ���¿��� ���� ������");
	    }
	    
	    printBluetoothManagerStatusLog();
	}
	
	//Ŭ���̾�Ʈ
	//ȣ��Ʈ�� ���� ����(���� ������ ��� ����������ϳ�)
	private synchronized void onConnectionFailed(ConnectThread connectThread) {
		Log.d(TAG, "�������");

		if (connectThread != null) {
			connectThread.cancel();
			
			connectThread = null;
		}else{
			Log.e(TAG, "���� connectThread�� null");
		}
		printBluetoothManagerStatusLog();
	}
		
	private class AcceptThread extends Thread {
		
		private static final String TAG = "20083271:AcceptThread";
		
		private BluetoothServerSocket mmServerSocket;
		
		//private int currentAcceptIndex = -1; // ������ �߰��� ����������� �̰��� ���������ν� ȸ���Ѵ�.
		
		private boolean terminated = false;//���������ϰ������
		
		public AcceptThread() {

		}
		
		public void run() {

			//currentAcceptIndex = -1;//-1�� �ƴѰ�� �ش� ���ʿ� �ش� �ε����� ����� ��
			
			// Create a new listening server socket
			try {
				if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
					Log.i(TAG, "ȣ��Ʈ �غ� ���¿��� ���������� ����");
					for (int i = 0; i < maxClients; i++) {
						if(terminated == true){//��� ����ǥ��(cancel�ȿ�����) ���ָ�
							this.cancel();
							return;//�����尡 �����ұ�?
						}
					
						//mConnectedThreads�� ũ��� �����߿� ���� �پ���� �ʴ´�.
					
						accept(i);
					
					}
				}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
					Log.i(TAG, "ȣ��Ʈ ������ ���¿��� ����������  ����");
					
					if(lostClientDevices.size() < 1 || lostIndexes.size() < 1){//������ �ִ�. "��ٸ�" ������ ��ġ�� ���� ��Ȳ : ���̾ȵ�
						Log.e(TAG, "������ ��ġ�� �����ϴ�. lostClientDevices�� Ȯ���ϼ���.");
					}
							
					//mConnectedThreads�� ũ��� �����߿� ���� �پ���� �ʴ´�.
					
					Log.i(TAG, "������� �ε����� : " + lostIndexes.get(0));
					accept(lostIndexes.get(0));
					
					/////////////////////////////////////////////////
					
				}
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
		}
		
		public void accept(int currentIndex) throws IOException{
			synchronized (BluetoothManager.this) {	//�ᱹ �޼��忡 sync �ϴ°͵� �Ȱ�����.
				Log.i(TAG, "������ ������ �ε��� : " + currentIndex);
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
						onConnected(DeviceType.CLIENT, socket, socket.getRemoteDevice(), currentIndex);//������ Ŭ���̾�Ʈ
					else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME)
						onReconnected(socket, socket.getRemoteDevice(), currentIndex);
				}
				mmServerSocket.close();
				mmServerSocket = null;
				socket = null;
			}

		}
		
		public void cancel() {
			Log.i(TAG, "���� �����尡 ��ҵǾ����ϴ�.");
			try {
				if(mmServerSocket != null)
					mmServerSocket.close();
				terminated = true;
			} catch (IOException e) {
				Log.e(TAG, "������ close() ����", e);
			}
		}
			
		/*public void setCurrentAcceptIndex(int index){
			synchronized (BluetoothManager.this) {
				this.currentAcceptIndex = index;
			}
		}*/
	}
	
	//Ŭ���̾�Ʈ
	//ȣ��Ʈ�� ����
	private class ConnectRemoteAndroidThread extends ConnectThread {
		
		private static final String TAG = "20083271:ConnectThread";
		
		public static final int CONNECT_SUCCESS = 1;
		public static final int CONNECT_FAIL = 0;
		public static final int CONNECT_NONE = -1;
		
		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private int connectIndex;
		
		public ConnectRemoteAndroidThread(BluetoothDevice device, int connectIndex) {
			Log.d(TAG, "���ο� ���� ������, �ε���: " + connectIndex);

			this.connectIndex = connectIndex;
			mmDevice = device;

			try {

				mmSocket = device.createRfcommSocketToServiceRecord(uuids.get(connectIndex));

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
					onConnectionFailed(this);// ���� �õ��ϴٰ� ����
					reportSuccessOrFail(connectIndex, ConnectRemoteAndroidThread.CONNECT_FAIL);
				}

				return;
			}

			synchronized (BluetoothManager.this) {
				Log.i(TAG, "���� ����, �ε���: " + connectIndex);
				
				//���߿� ���շ��� �� ����� ���� ����
				
				/**
				 * �� ������ ù��°�� "������" �϶� ����ؾߵ�
				 */
				connectIndexAsClient = connectIndex;//Ŭ���̾�Ʈ�ν� ��� �ε����� uuid�� �����ߴ��� ����� ���´�.

				//////////////////���� ��////////
				
				cancelAllConnectThreadExcept(this);
				reportSuccessOrFail(connectIndex, ConnectRemoteAndroidThread.CONNECT_SUCCESS);
				onConnected(DeviceType.HOST, mmSocket, mmDevice, connectIndex);//������ ȣ��Ʈ		
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
			Log.d(TAG, "���� �ȵ���̵� ����� ������ ������");
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
					Log.e(TAG, "���� ������", e);
					onConnectionLost(this);// ���� ��������... ����
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		public void write(Object obj) {
			Log.i(TAG, "��ü ���� : " + obj);
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
			Log.d(TAG, "getConnectIndex ȣ���" + mmSocket.getRemoteDevice().getName() + "�� ���� �ε����� : " + connectIndex);
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
