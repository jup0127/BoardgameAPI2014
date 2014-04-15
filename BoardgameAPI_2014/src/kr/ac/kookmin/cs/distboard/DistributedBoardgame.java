package kr.ac.kookmin.cs.distboard;

import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceController;
import kr.ac.kookmin.cs.distboard.controller.DistributedBoardgameAdapter;
import kr.ac.kookmin.cs.distboard.controller.DistributedBoardgameListener;
import kr.ac.kookmin.cs.distboard.controller.GameToolListener;
import kr.ac.kookmin.cs.distboard.controller.ParticipantListener;
import kr.ac.kookmin.cs.distboard.controller.PlayerListener;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.system.BluetoothManager;
import kr.ac.kookmin.cs.distboard.system.CandidateManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.EmulatorReceiver;
import kr.ac.kookmin.cs.distboard.system.GameToolSystemManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

public class DistributedBoardgame {//singleton
	
	public static final String TAG = "20083271:DistributedBoardgame";
	
	private static DistributedBoardgame instance = new DistributedBoardgame();
	
	private static final int MILLISECOND_DEFAULT_TIMEOUT = 9999000;
	
	
	//���� ���
	public static final int NONE = -1;
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	public static final int HOST_PREPARE_MODE = 1;
	public static final int CLIENT_JOIN_MODE = 2;
	public static final int MIDDLE_OF_GAME = 3;
	
	private boolean initialized = false;
	
	private DistributedBoardgameListener distributedBoardgameListener = null;
	private Mode mode = Mode.NONE;//ȣ��Ʈ VS Ŭ���̾�Ʈ
	private Context context = null;
	
	private int state = NONE;//������ ����
	
	private int numOfDiceIntention = 0;//�ǵ��� �ֻ��� ����
	private int numOfYutIntentin = 0;//�ǵ��� �� ����
	private boolean diceAtOnce = false;
	private boolean yutsAtOnce = false;
	
	private Mediator mediator = null;//singleton
	private DicePlusGameTool[] dicePlusGameTools = null;
	private YutGameTool[] yutGameTools = null;
	private Player host = null;
	private Player[] players = null;
	
	private DistributedBoardgame(){
		
	}
	
	public static DistributedBoardgame getInstance(Context context){//�̱��� ���ɿ� : ���� 1ȸ �θ�
		
		instance.initialized = true;//�̱��� ���ɿ�
		
		instance.distributedBoardgameListener = new DistributedBoardgameAdapter();
		instance.state = DistributedBoardgame.NONE;
		instance.mode = Mode.NONE;//�̱��� ���ɿ�
		instance.context = context;
		instance.mediator = null;//�̱��� ���ɿ�
		instance.dicePlusGameTools = null;//�̱��� ���ɿ�
		instance.yutGameTools = null;//�̱��� ���ɿ�
		instance.players = null;//�̱��� ���ɿ�
		
		return instance;
	}

	public static DistributedBoardgame getInstance(){
		if (instance.initialized == false){
			Log.w(TAG, "is not initialized");
			//return null;//����ó��
		}
		
		return instance;
	}

	public void initializeBoardgame(
			int minPlayers, 
			int maxPlayers, 
			int exactDice, 
			int exactYuts){
		
		numOfDiceIntention = exactDice;
		numOfYutIntentin = exactYuts;
		
		diceAtOnce = false;
		yutsAtOnce = false;
		
		//��� �ý��� �̱��� ��ü �ʱ�ȭ
		//�ν��Ͻ� �����鼭 �ʱ�ȭ�ϴ°͵��� ���� �����ϴ��� ó���ؾߵ�...............

		DicePlusManager.getInstance().initialize();
		ElectricYutManager.getInstance().initialize();
		EmulatorReceiver.getInstance().initialize();
		CandidateManager.getInstance().initialize();
		GameToolSystemManager.getInstance().initialize();
		RequestReplyManager.getInstance().initialize();
		SubjectDeviceMapper.getInstance().initialize(minPlayers, maxPlayers, exactDice, exactYuts);
		//CommunicationStateManager.getInstance().initialize(handler); ��Ƽ��Ƽ�� �ʱ�ȭ���ٰ�.
		this.mediator = Mediator.getInstance(minPlayers, maxPlayers, exactDice, exactYuts);
		this.mediator.negotiate(MILLISECOND_DEFAULT_TIMEOUT);
		
	}
	
	//called by developer
	
	//�ѹ��� ó���ؼ� �����ʷ� ������

	public void setDiceAtOnce(boolean diceAtOnce) {
		this.diceAtOnce = diceAtOnce;
	}

	public void setYutsAtOnce(boolean yutsAtOnce) {
		this.yutsAtOnce = yutsAtOnce;
	}

	public Player getMe(){
		return Player.getThisPlayer();
	}
	
	public DistributedBoardgameListener getDistributedBoardgameListener(){
		return instance.distributedBoardgameListener;
	}
	
	//called by system
	
	void setDicePlusGameTools(DicePlusGameTool[] dicePlusGameTools) {
		if(this.dicePlusGameTools != null){
			Log.e(TAG,"aleady have dicePlusGameTools.");
		}
		this.dicePlusGameTools = dicePlusGameTools;
	}
	
	void setYutGameTools(YutGameTool[] yutGameTools) {
		if(this.yutGameTools != null){
			Log.e(TAG,"aleady have yutGameTools.");
		}
		this.yutGameTools = yutGameTools;
	}
	
	void setPlayers(Player[] players) {
		if(this.players != null){
			Log.e(TAG,"aleady have players.");
		}
		this.players = players;
	}
	
	//getters and setters
	
	public int getState(){
		return state;
	}
	
	public void setState(int state){
		Log.i(TAG, "���� ��� �ٲ� : " + state);
		this.state = state;
	}
	
	public int getNumOfDiceIntention() {
		return numOfDiceIntention;
	}

	public int getNumOfYutIntentin() {
		return numOfYutIntentin;
	}

	public Context getContext(){
		return context;
	}

	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode){
		this.mode = mode;
	}
	
	public boolean setGetDieValueAtOnce(){
		return diceAtOnce;
	}
	
	public boolean setGetYutValuesAtOnce(){
		return yutsAtOnce;
	}

	public Mediator getMediator() {
		return mediator;
	}

	public DicePlusGameTool[] getDicePlusGameTools() {
		return dicePlusGameTools;
	}

	public YutGameTool[] getYutGameTools() {
		return yutGameTools;
	}

	public Player getHost() {
		if(mode != Mode.CLIENT){
			Log.w(TAG, "Ŭ���̾�Ʈ���� getHost()�� ȣ���� �� �ֽ��ϴ�. null�� �����մϴ�.");
			return null;
		}
		return players[0];
	}
	
	public Player[] getPlayers() {
		return players;
	}
	
	public boolean isDicePlusAvailable(){
		//available���� 
		return dicePlusGameTools.length != 0 ?  true : false;
	}
	
	public boolean isElectricYutAvailable(){
		//available���� 
		return yutGameTools.length != 0 ?  true : false;
	}
	
	//��� �޼��� ����(��� ���� Ŭ���� ���յ�)
	
	public void registerDistributedBoardgameListener(DistributedBoardgameListener distributedBoardgameListener){
		instance.distributedBoardgameListener = distributedBoardgameListener;
	}
	
	public void registerGameToolListener(GameToolListener gameToolListener){
		GameTool.registerGameToolListener(gameToolListener);
	}
	
	public void registerParticipantListener(ParticipantListener participantListener){
		Mediator.registerParticipantListener(participantListener);
	}
	
	public void registerPlayerListener(PlayerListener playerListener){
		Player.registerPlayerListenr(playerListener);
	}
	
	//��� ����
	public void unregisterDistributedBoardgameListener(){
		instance.distributedBoardgameListener = null;
	}
	
	public void unRegisterGameToolListener(){
		GameTool.unRegisterGameToolListener();
	}
	
	public void unregisterParticipantListener(){
		Mediator.unregisterParticipantListener();
	}
	
	public void unRegisterPlayerListener(){
		Player.unRegisterPlayerListenr();
	}
	
	
	
	//����� �޸� ���� �����ɼ� �ִ°͵� �����ϴµ� ó��
	
	public void clear(){
		Log.i(TAG,"Ŭ����");
		if(initialized == true){
			//�����߿�! null�� ���鶧 ���� ����Ұ�
			
			instance.unregisterDistributedBoardgameListener();
			instance.unRegisterGameToolListener();
			instance.unregisterParticipantListener();
			instance.unRegisterPlayerListener();
			
			DicePlusManager.getInstance().clear();
			BluetoothManager.getInstance().clear();
			ElectricYutManager.getInstance().clear();
			EmulatorReceiver.getInstance().clear();
			
			initialized = false;
			Log.i(TAG, "initialized : " + initialized);
		}else{
			Log.e(TAG,"�ʱ�ȭ���� ����");
		}
		
	}
	
	//Runnalbe jar�� ���� �޼���
	
	public static void main(String[] args){
		//Log.w(TAG, "�� �޼��带 ȣ������ ������.");
		System.err.println("�� �޼��带 ȣ������ ������");
	}
}
