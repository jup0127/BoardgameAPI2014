package kr.ac.kookmin.cs.distboard;

import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.controller.ParticipantAdapter;
import kr.ac.kookmin.cs.distboard.controller.ParticipantListener;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Mediator{
	
	public static String TAG = "20083271:Mediator";
	
	private static Mediator instance = new Mediator(); 
	
	private static final int DEFAULT_HOST_PREPARE_TIMEOUT = 120;
	private static final int DEFAULT_HOST_WAIT_TIMEOUT = 30;
	private static ParticipantListener participantListener = new ParticipantAdapter();
	
	public int hostPrepareTimeout = DEFAULT_HOST_PREPARE_TIMEOUT;
	public int hostWaitPlayerTimeout = DEFAULT_HOST_WAIT_TIMEOUT;
	
	private boolean initialized = false;
	private Handler handler = null;
	private Mode mode = Mode.NONE;
	private int minPlayers;
	private int maxPlayers;
	private int exactElectricGameToolDicePlus;
	private int exactElectricGameToolYut;
	
	private Mediator(){
		
	}
	
	public static Mediator getInstance(//�ʱ�ȭ�ϸ鼭 �ν��Ͻ��� ����ϴ�.(���� 1ȸ)
			int minPlayers, 
			int maxPlayers, 
			int exactElectricGameToolDicePlus,
			int exactElectricGameToolYut){
		
		//�ʱ�ȭ���� �ʾҴٸ�
		
		instance.mode = Mode.NONE;//�̱��� ����ƽ ���ɿ�
		instance.hostPrepareTimeout = DEFAULT_HOST_PREPARE_TIMEOUT;//�̱��� ����ƽ ���ɿ�
		instance.hostWaitPlayerTimeout = DEFAULT_HOST_WAIT_TIMEOUT;//�̱��� ����ƽ ���ɿ�
		instance.minPlayers = minPlayers;
		instance.maxPlayers = maxPlayers;
		instance.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;
		instance.exactElectricGameToolYut = exactElectricGameToolYut;
			
		instance.initialized = true;
		
		return instance;
	}
	
	public static Mediator getInstance(){
		if(instance.initialized == true)
			return instance;
		else{
			Log.e(TAG, "is not initailized.");
			return null;//����ó���Ұ�
		}
	}
	
	//�̱���� �޼���
	
	public void negotiate(){
		//���� ������ �ð��������� ������
		negotiate(hostPrepareTimeout);
	}
	
	public void negotiate(int hostPrepareTimeout){
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.HOST_CLIENT_SELECT_MODE);
		instance.hostPrepareTimeout = hostPrepareTimeout;
		Log.i(TAG, "negotiate");
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_CLIENT_SELECT_MODE);
		activateActivityAs(intent);
		
	}
	
	//called by Activity
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	public Handler getHandler(){
		return this.handler;
	}
	
	private void prepare(){
		Log.d(TAG, "ȣ��Ʈ �غ� ����");
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.HOST_PREPARE_MODE);
		
		if(exactElectricGameToolDicePlus != 0 && exactElectricGameToolYut != 0){
			Log.w(TAG, "������� ����� 2���� ���ÿ� ���� �ϴ°��� ���� �Ҿ����մϴ�!!!!!!");
			Log.w(TAG, "�������� ���ᱸ�� ��ҵ�.");
			return;
		}
		
		//������� �Ŵ��� ����
		ClientManager.getInstance().initialize(Mode.HOST, maxPlayers, exactElectricGameToolYut, hostPrepareTimeout);
		ClientManager.getInstance().establish();

		//���̽� �÷��� �Ŵ��� ����
		DicePlusManager.getInstance().establish(exactElectricGameToolDicePlus, hostPrepareTimeout);
		
		//���� �� �Ŵ��� ����
		ElectricYutManager.getInstance().establish(exactElectricGameToolYut, hostPrepareTimeout);
		
		//��Ƽ��Ƽ ����
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.HOST_PREPARE_MODE);
		intent.putExtra(AssistanceActivity.MIN_PLAYERS, minPlayers);
		intent.putExtra(AssistanceActivity.MAX_PLAYERS, maxPlayers);
		intent.putExtra(AssistanceActivity.EXACT_YUTS, exactElectricGameToolYut);
		intent.putExtra(AssistanceActivity.EXACT_DICEPLUSES, exactElectricGameToolDicePlus);
		activateActivityAs(intent);
		
	}
	
	private void join(){
		Log.d(TAG, "Ŭ���̾�Ʈ �շ� ����");
		ClientManager.getInstance().initialize(Mode.CLIENT, maxPlayers, exactElectricGameToolYut, hostPrepareTimeout);
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.CLIENT_JOIN_MODE);
		
		//������� �Ŵ��� ����
		//BluetoothDevice[] devices = BluetoothManager.getInstance(Mode.CLIENT).getDiscoveredDevices();
		
		
		//��Ƽ��Ƽ ����
		Intent intent = getDefaultIntent();
		intent.putExtra(AssistanceActivity.ACTIVITY_MODE, AssistanceActivity.CLIENT_JOIN_MODE);
		//intent.putExtra(AssistanceActivity.DISCOVERED_DEVICES, devices);
		activateActivityAs(intent);//��Ƽ��Ƽ�� ���� �ڵ� ȣ��
		
	}

	public void leave(){
		
	}
	
	public void getOut(Player player){
		
	}
	
	public static void registerParticipantListener(ParticipantListener participantListener){
		Mediator.participantListener = participantListener;
	}
	
	public static void unregisterParticipantListener(){
		participantListener = new ParticipantAdapter();
	}

	//called by bluetooth manager
	
	public void setMode(Mode mode){
		//���ڷ� �� ��尡 �������ϰų� ������ ��尡 �ִٸ� ����.
		if(this.mode == Mode.NONE && mode != Mode.NONE)
			this.mode = mode;
		else
			Log.e(TAG, "Unexpected mode");
	}
	
	public Mode getMode(){
		return this.mode;
	}
	
	public void setHostPrepareTimeout(int hostPrepareTimeout){
		this.hostPrepareTimeout = hostPrepareTimeout;
	}
	
	public void setHostWaitPlayerTimeout(int hostWaitPlayerTimeout){
		this.hostWaitPlayerTimeout = hostWaitPlayerTimeout;
	}
	
	//called by Activity
	
	public void completeSelectHostClient(Mode mode){
		this.mode = mode;
		DistributedBoardgame.getInstance().setMode(mode);
		Log.i(TAG, "mode selected : " + mode);
		
		//��� ���� ��Ƽ��Ƽ�� ���� ���õ� ����� ������ �����մϴ�.
		if(mode == Mode.HOST){
			prepare();
		}else if(mode == Mode.CLIENT){
			join();
		}else{
			Log.e(TAG, "mode was not selected.");
		}
		
	}
	
	//called by mapper
	
	public void completeMapping(Player[] players, YutGameTool[] yutGameTools, DicePlusGameTool[] dicePlusGameTools){
		//��������� ������� ����
		Log.i(TAG, "���� �Ϸ�");
		
		Log.i(TAG, "�÷��̾�� " + players);
		Log.i(TAG, "�������� " + yutGameTools);
		Log.i(TAG, "DICE+�� " + dicePlusGameTools);
		
		DistributedBoardgame.getInstance().setPlayers(players);
		DistributedBoardgame.getInstance().setYutGameTools(yutGameTools);
		DistributedBoardgame.getInstance().setDicePlusGameTools(dicePlusGameTools);
		
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.COMPLETE_ACTIVITY);//��Ƽ��Ƽ ����
        RequestReplyManager.getInstance().sendRequestToAllPlayer(Request.OK_TO_GO, null);//��� �ֵ鿡�� �����ص� ���ٴ� ��ȣ ����
        message.sendToTarget();
		
		DistributedBoardgame.getInstance().setState(DistributedBoardgame.MIDDLE_OF_GAME);
			
		//
		if(mode == Mode.HOST){//ȣ��Ʈ�� ���, Ŭ���̾�Ʈ ��쿣 OK_TO_GO ��ٸ���.
			Log.i(TAG, "�� ���� ��Ÿ�ͺ� �۵� : " + Mediator.getInstance().getMode() + " (��� ����)");
			DistributedBoardgame.getInstance().getDistributedBoardgameListener().onGameStartable(mode);
		}
	}
	
	//helper method
	
	private Intent getDefaultIntent(){
		return new Intent(
				DistributedBoardgame.getInstance().getContext(), 
				kr.ac.kookmin.cs.distboard.activity.AssistanceActivity.class);
	}
	
	private void activateActivityAs(Intent intent){
		//��Ƽ��Ƽ Ȱ��ȭ
		DistributedBoardgame.getInstance().getContext().startActivity(intent);
	}

	//static method
	
	public static ParticipantListener getParticipantListener(){
		return Mediator.participantListener;
	}

	//getter and setter 
	
	 public int getMinPlayers() {
	        return minPlayers;
	    }

	    public int getMaxPlayers() {
	        return maxPlayers;
	    }

	    public int getExactElectricGameToolDicePlus() {
	        return exactElectricGameToolDicePlus;
	    }

	    public int getExactElectricGameToolYut() {
	        return exactElectricGameToolYut;
	    }

	    public void setMinPlayers(int minPlayers) {
	        this.minPlayers = minPlayers;
	    }

	    public void setMaxPlayers(int maxPlayers) {
	        this.maxPlayers = maxPlayers;
	    }

	    public void setExactElectricGameToolDicePlus(int exactElectricGameToolDicePlus) {
	        this.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;
	    }

	    public void setExactElectricGameToolYut(int exactElectricGameToolYut) {
	        this.exactElectricGameToolYut = exactElectricGameToolYut;
	    }
	
}
