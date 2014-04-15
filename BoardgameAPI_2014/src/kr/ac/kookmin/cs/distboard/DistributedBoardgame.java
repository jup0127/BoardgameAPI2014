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
	
	
	//상태 상수
	public static final int NONE = -1;
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	public static final int HOST_PREPARE_MODE = 1;
	public static final int CLIENT_JOIN_MODE = 2;
	public static final int MIDDLE_OF_GAME = 3;
	
	private boolean initialized = false;
	
	private DistributedBoardgameListener distributedBoardgameListener = null;
	private Mode mode = Mode.NONE;//호스트 VS 클라이언트
	private Context context = null;
	
	private int state = NONE;//현재의 상태
	
	private int numOfDiceIntention = 0;//의도한 주사위 개수
	private int numOfYutIntentin = 0;//의도한 윷 개수
	private boolean diceAtOnce = false;
	private boolean yutsAtOnce = false;
	
	private Mediator mediator = null;//singleton
	private DicePlusGameTool[] dicePlusGameTools = null;
	private YutGameTool[] yutGameTools = null;
	private Player host = null;
	private Player[] players = null;
	
	private DistributedBoardgame(){
		
	}
	
	public static DistributedBoardgame getInstance(Context context){//싱글톤 조심용 : 최초 1회 부름
		
		instance.initialized = true;//싱글톤 조심용
		
		instance.distributedBoardgameListener = new DistributedBoardgameAdapter();
		instance.state = DistributedBoardgame.NONE;
		instance.mode = Mode.NONE;//싱글톤 조심용
		instance.context = context;
		instance.mediator = null;//싱글톤 조심용
		instance.dicePlusGameTools = null;//싱글톤 조심용
		instance.yutGameTools = null;//싱글톤 조심용
		instance.players = null;//싱글톤 조심용
		
		return instance;
	}

	public static DistributedBoardgame getInstance(){
		if (instance.initialized == false){
			Log.w(TAG, "is not initialized");
			//return null;//에러처리
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
		
		//모든 시스템 싱글톤 객체 초기화
		//인스턴스 얻으면서 초기화하는것들은 따로 수정하던가 처리해야됨...............

		DicePlusManager.getInstance().initialize();
		ElectricYutManager.getInstance().initialize();
		EmulatorReceiver.getInstance().initialize();
		CandidateManager.getInstance().initialize();
		GameToolSystemManager.getInstance().initialize();
		RequestReplyManager.getInstance().initialize();
		SubjectDeviceMapper.getInstance().initialize(minPlayers, maxPlayers, exactDice, exactYuts);
		//CommunicationStateManager.getInstance().initialize(handler); 액티비티가 초기화해줄것.
		this.mediator = Mediator.getInstance(minPlayers, maxPlayers, exactDice, exactYuts);
		this.mediator.negotiate(MILLISECOND_DEFAULT_TIMEOUT);
		
	}
	
	//called by developer
	
	//한번에 처리해서 리스너로 보낼지

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
		Log.i(TAG, "게임 모드 바뀜 : " + state);
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
			Log.w(TAG, "클라이언트만이 getHost()를 호출할 수 있습니다. null을 리턴합니다.");
			return null;
		}
		return players[0];
	}
	
	public Player[] getPlayers() {
		return players;
	}
	
	public boolean isDicePlusAvailable(){
		//available한지 
		return dicePlusGameTools.length != 0 ?  true : false;
	}
	
	public boolean isElectricYutAvailable(){
		//available한지 
		return yutGameTools.length != 0 ?  true : false;
	}
	
	//등록 메서드 집합(모든 관련 클래스 통합됨)
	
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
	
	//등록 해제
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
	
	
	
	//종료시 메모리 관련 누수될수 있는것들 해제하는등 처리
	
	public void clear(){
		Log.i(TAG,"클리어");
		if(initialized == true){
			//순서중요! null값 만들때 순서 고려할것
			
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
			Log.e(TAG,"초기화되지 않음");
		}
		
	}
	
	//Runnalbe jar용 메인 메서드
	
	public static void main(String[] args){
		//Log.w(TAG, "이 메서드를 호출하지 마세요.");
		System.err.println("이 메서드를 호출하지 마세요");
	}
}
