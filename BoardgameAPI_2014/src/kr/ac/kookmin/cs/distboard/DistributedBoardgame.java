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

/**
 * 분산환경의 보드게임을 추상화합니다.
 * version 1.0
 * @author jeon2
 *
 */
public class DistributedBoardgame {//singleton
	
	public static final String TAG = "20083271:DistributedBoardgame";
	
	private static DistributedBoardgame instance = new DistributedBoardgame();
	
	private static final int MILLISECOND_DEFAULT_TIMEOUT = 9999000;
	
	
	//상태 상수
	public static final int NONE = -1;
	
	/**
	 * 보드게임의 현재 상태가 호스트와 클라이언트 선택 상태라는 것을 나타냄
	 */
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	/**
     * 보드게임의 현재 상태가 호스트의 준비 상태라는 것을 나타냄
     */
	public static final int HOST_PREPARE_MODE = 1;
	/**
     * 보드게임의 현재 상태가 클라이언트의 합류중인 상태라는 것을 나타냄
     */
	public static final int CLIENT_JOIN_MODE = 2;
	/**
     * 보드게임의 현재 상태가 호스트또는 클라이언트가 게임중이라는 상태를 나타냄
     */
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
	private Player[] players = null;
	
	private DistributedBoardgame(){
		
	}
	
	/**
	 * 현시점의 안드로이드 컨텍스트를 인자로하여 DistributedBoardgame의 싱글톤 인스턴스를 리턴합니다.
	 * 이 메서드는 모든 인스턴스 변수를 초기화하기 때문에 어플리에키션 실행 후 최초 한번만 불려져야합니다.
	 * 이 메서드가 두 번째 불려질 때는 단순히 기존에 초기화되었던 인스턴스를 반환합니다.
	 * 
	 * @param context 안드로이드 어플리케이션 컨텍스트
	 * @return DistributedBoardgame의 싱글톤 인스턴스
	 */
	public static DistributedBoardgame getInstance(Context context){//싱글톤 조심용 : 최초 1회 부름
		
	    if(instance.initialized == true)
	        return instance;
	    
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

	
	/**
	 * 초기화된 DistributedBoardgame의 싱글톤 인스턴스를 반환합니다.
	 * 이 메서드를 호출하기 전 getInstance(Context context) 메서드가 한 번 호출된 상태이어야 합니다.
	 * @return 초기화된 DistributedBoardgame의 싱글톤 인스턴스
	 */
	public static DistributedBoardgame getInstance(){
		if (instance.initialized == false){
			Log.e(TAG, "is not initialized");
			//null 리턴하면 destroy시 null pointer exception
		}
		
		return instance;
	}

	/**
	 * 분산 보드게임을 초기화합니다. 이 메서드가 호출되면
	 * 호출한 안드로이드 액티비티 위에 보드게임 초기화에 관련된 액티비티가 나타납니다.
	 * @param minPlayers 보드게임에 필요한 최소인원
	 * @param maxPlayers 보드게임의 최대인원 
	 * @param exactDice 보드게임에 사용할 주사위 개수
	 * @param exactYuts 보드게임에 사용할 윷 개수
	 */
	public void initializeBoardgame(
			int minPlayers, 
			int maxPlayers, 
			int exactDice, 
			int exactYuts){
		
	    if(minPlayers > maxPlayers || minPlayers < 0){
	        Log.e(TAG, "적절하지 않은 플레이어 수 규정");
	        return;
	    }
	
	    if(exactDice > 0 && exactYuts > 0){
	        Log.e(TAG, "윷과 주사위를 동시에 사용할 수 없습니다.");
	        return;
	    }
	    
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

	/**
	 * 주사위의 굴려짐 이벤트를 초기화에 명세한 주사위 개수만큼 모였을 때 처리할 지 결정합니다.
	 * 만약 인자의 값이 'true'라면 모든 주사위가 굴려졌을때 이벤트에대한 리스너가 동작합니다.
	 * 만약 인자의 값이 'false'라면 주사위 하나가 굴려질 때 마다 이벤트에대한 리스너가 동작합니다.
	 * @param diceAtOnce 주사위의 굴려짐 이벤트를 한번에 처리할지의 여부
	 */
	public void setDiceAtOnce(boolean diceAtOnce) {
		this.diceAtOnce = diceAtOnce;
	}

	
	/**
	 * 아직 지원하지 않는 메소드
	 * @param yutsAtOnce
	 */
	public void setYutsAtOnce(boolean yutsAtOnce) {
		this.yutsAtOnce = yutsAtOnce;
	}

	/**
	 * 현재 안드로이드 디바이스를 사용하는 플레이어를 추상화한 객체를 리턴합니다.
	 * @return 이 장치를 사용하는 플레이어
	 */
	public Player getMe(){
		return Player.getThisPlayer();
	}
	
	
	
	//called by system
	
	/**
     * 개발자가 호출하지 않습니다.
     * @return 
     */
    public DistributedBoardgameListener getDistributedBoardgameListener(){
        return instance.distributedBoardgameListener;
    }
	
    /**
     * 개발자가 호출하지 않습니다.
     * @param dicePlusGameTools
     */
	void setDicePlusGameTools(DicePlusGameTool[] dicePlusGameTools) {
		if(this.dicePlusGameTools != null){
			Log.e(TAG,"aleady have dicePlusGameTools.");
		}
		this.dicePlusGameTools = dicePlusGameTools;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * @param yutGameTools
	 */
	void setYutGameTools(YutGameTool[] yutGameTools) {
		if(this.yutGameTools != null){
			Log.e(TAG,"aleady have yutGameTools.");
		}
		this.yutGameTools = yutGameTools;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * @param players
	 */
	void setPlayers(Player[] players) {
		if(this.players != null){
			Log.e(TAG,"aleady have players.");
		}
		this.players = players;
	}
	
	//getters and setters
	
	/**
	 * 보드게임의 현재 상태를 리턴합니다.
	 * 반환 값은 클래스변수 HOST_CLIENT_SELECT_MODE, HOST_PREPARE_MODE, CLIENT_JOIN_MODE, MIDDLE_OF_GAME를 참조하세요.
	 * @return 보드게임의 현재 상태
	 */
	public int getState(){
		return state;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * @param state
	 */
	public void setState(int state){
		Log.i(TAG, "게임 모드 바뀜 : " + state);
		this.state = state;
	}
	
	/**
	 * 의도된 사용할 주사위 개수를 리턴합니다. 이 값은 이 객체의 초기화에 결정됩니다.
	 * @return 의도된 사용할 주사위의 개수
	 */
	public int getNumOfDiceIntention() {
		return numOfDiceIntention;
	}

	/**
     * 아직 지원하지 않는 메소드
     */
	public int getNumOfYutIntentin() {
		return numOfYutIntentin;
	}

	public Context getContext(){
		return context;
	}

	/**
	 * 현재 안드로이드 장치에서 동작하고 있는 보드게임의 모드를 반환합니다.
	 * 반환 값은 클래수변수
	 * @return
	 */
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
	
	/**
	 * 분산 보드게임 리스너를 등록합니다. 해당 리스너는 게임의 시작 또는 제개에 관련된 이벤트를 처리하는 메서드를 포함합니다.
	 * 이 메서드가 호출된 이후 시점부터 등록된 리스너에의해 해당 이벤트가 처리됩니다.
	 * @param distributedBoardgameListener 등록할 분산보드게임 리스너
	 */
	public void registerDistributedBoardgameListener(DistributedBoardgameListener distributedBoardgameListener){
		instance.distributedBoardgameListener = distributedBoardgameListener;
	}
	
	/**
     * 게임 도구 리스너를 등록합니다. 해당 리스너는 게임 도구에 관련된 이벤트를 처리하는 메서드를 포함합니다.
     * 이 메서드가 호출된 이후 시점부터 등록된 리스너에의해 해당 이벤트가 처리됩니다.
     * @param gameToolListener 등록할 게임 도구 리스너
     */
	public void registerGameToolListener(GameToolListener gameToolListener){
		GameTool.registerGameToolListener(gameToolListener);
	}
	
	/**
     * 참여자 리스너를 등록합니다. 해당 리스너는 참여자의 상태에 관련된 이벤트를 처리하는 메서드를 포함합니다.
     * 이 메서드가 호출된 이후 시점부터 등록된 리스너에의해 해당 이벤트가 처리됩니다.
     * @param participantListener 등록할 참여자 리스너
     */
	public void registerParticipantListener(ParticipantListener participantListener){
		Mediator.registerParticipantListener(participantListener);
	}
	
	/**
     * 플레이어 리스너를 등록합니다. 해당 리스너는 플레이어의 상태에 관련된 이벤트를 처리하는 메서드를 포함합니다.
     * 이 메서드가 호출된 이후 시점부터 등록된 리스너에의해 해당 이벤트가 처리됩니다.
     * @param playerListener 등록할 분산보드게임 리스너
     */
	public void registerPlayerListener(PlayerListener playerListener){
		Player.registerPlayerListenr(playerListener);
	}
	
	//등록 해제
	/**
	 * 등록한 분산 보드게임 리스너를 해제합니다.
	 * 이 메서드가 호출된 이후 시점부터 등록되었었던 리스너가 동작하지 않습니다.
	 */
	public void unregisterDistributedBoardgameListener(){
		instance.distributedBoardgameListener = new DistributedBoardgameAdapter();
	}
	
	/**
     * 등록한 게임 도구 리스너를 해제합니다.
     * 이 메서드가 호출된 이후 시점부터 등록되었었던 리스너가 동작하지 않습니다.
     */
	public void unRegisterGameToolListener(){
		GameTool.unRegisterGameToolListener();
	}
	
	/**
     * 등록한 참여자 리스너를 해제합니다.
     * 이 메서드가 호출된 이후 시점부터 등록되었었던 리스너가 동작하지 않습니다.
     */
	public void unregisterParticipantListener(){
		Mediator.unregisterParticipantListener();
	}
	
	/**
     * 등록한 플레이어 리스너를 해제합니다.
     * 이 메서드가 호출된 이후 시점부터 등록되었었던 리스너가 동작하지 않습니다.
     */
	public void unRegisterPlayerListener(){
		Player.unRegisterPlayerListenr();
	}
	
	
	
	//종료시 메모리 관련 누수될수 있는것들 해제하는등 처리
	
	/**
	 * 어플리케이션이 종료하기 직전에 이 메서드가 호출되어야합니다.
	 * 분산보드게임 연결 구성에 사용되었던 자원들의 할당을 해제하고 
	 * 사용된 클래스들의 싱글톤 객체를 초기화되지 않은 상태로 돌려놓습니다.
	 * 이 메서드를 안드로이드 Activity 인스턴스의 onDestroy()안에 기입합니다.
	 * 어플리케이션 종료 이전에 이 메서드가 호출되지 않았다면 어플리케이션이 새로 호출되었을때 문제를 발생 시킬 수 있습니다.
	 */
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
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * @param args
	 */
	public static void main(String[] args){
		//Log.w(TAG, "이 메서드를 호출하지 마세요.");
		System.err.println("이 메서드를 호출하지 마세요");
	}
}
