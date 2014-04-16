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
 * �л�ȯ���� ��������� �߻�ȭ�մϴ�.
 * version 1.0
 * @author jeon2
 *
 */
public class DistributedBoardgame {//singleton
	
	public static final String TAG = "20083271:DistributedBoardgame";
	
	private static DistributedBoardgame instance = new DistributedBoardgame();
	
	private static final int MILLISECOND_DEFAULT_TIMEOUT = 9999000;
	
	
	//���� ���
	public static final int NONE = -1;
	
	/**
	 * ��������� ���� ���°� ȣ��Ʈ�� Ŭ���̾�Ʈ ���� ���¶�� ���� ��Ÿ��
	 */
	public static final int HOST_CLIENT_SELECT_MODE = 0;
	/**
     * ��������� ���� ���°� ȣ��Ʈ�� �غ� ���¶�� ���� ��Ÿ��
     */
	public static final int HOST_PREPARE_MODE = 1;
	/**
     * ��������� ���� ���°� Ŭ���̾�Ʈ�� �շ����� ���¶�� ���� ��Ÿ��
     */
	public static final int CLIENT_JOIN_MODE = 2;
	/**
     * ��������� ���� ���°� ȣ��Ʈ�Ǵ� Ŭ���̾�Ʈ�� �������̶�� ���¸� ��Ÿ��
     */
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
	private Player[] players = null;
	
	private DistributedBoardgame(){
		
	}
	
	/**
	 * �������� �ȵ���̵� ���ؽ�Ʈ�� ���ڷ��Ͽ� DistributedBoardgame�� �̱��� �ν��Ͻ��� �����մϴ�.
	 * �� �޼���� ��� �ν��Ͻ� ������ �ʱ�ȭ�ϱ� ������ ���ø���Ű�� ���� �� ���� �ѹ��� �ҷ������մϴ�.
	 * �� �޼��尡 �� ��° �ҷ��� ���� �ܼ��� ������ �ʱ�ȭ�Ǿ��� �ν��Ͻ��� ��ȯ�մϴ�.
	 * 
	 * @param context �ȵ���̵� ���ø����̼� ���ؽ�Ʈ
	 * @return DistributedBoardgame�� �̱��� �ν��Ͻ�
	 */
	public static DistributedBoardgame getInstance(Context context){//�̱��� ���ɿ� : ���� 1ȸ �θ�
		
	    if(instance.initialized == true)
	        return instance;
	    
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

	
	/**
	 * �ʱ�ȭ�� DistributedBoardgame�� �̱��� �ν��Ͻ��� ��ȯ�մϴ�.
	 * �� �޼��带 ȣ���ϱ� �� getInstance(Context context) �޼��尡 �� �� ȣ��� �����̾�� �մϴ�.
	 * @return �ʱ�ȭ�� DistributedBoardgame�� �̱��� �ν��Ͻ�
	 */
	public static DistributedBoardgame getInstance(){
		if (instance.initialized == false){
			Log.e(TAG, "is not initialized");
			//null �����ϸ� destroy�� null pointer exception
		}
		
		return instance;
	}

	/**
	 * �л� ��������� �ʱ�ȭ�մϴ�. �� �޼��尡 ȣ��Ǹ�
	 * ȣ���� �ȵ���̵� ��Ƽ��Ƽ ���� ������� �ʱ�ȭ�� ���õ� ��Ƽ��Ƽ�� ��Ÿ���ϴ�.
	 * @param minPlayers ������ӿ� �ʿ��� �ּ��ο�
	 * @param maxPlayers ��������� �ִ��ο� 
	 * @param exactDice ������ӿ� ����� �ֻ��� ����
	 * @param exactYuts ������ӿ� ����� �� ����
	 */
	public void initializeBoardgame(
			int minPlayers, 
			int maxPlayers, 
			int exactDice, 
			int exactYuts){
		
	    if(minPlayers > maxPlayers || minPlayers < 0){
	        Log.e(TAG, "�������� ���� �÷��̾� �� ����");
	        return;
	    }
	
	    if(exactDice > 0 && exactYuts > 0){
	        Log.e(TAG, "���� �ֻ����� ���ÿ� ����� �� �����ϴ�.");
	        return;
	    }
	    
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

	/**
	 * �ֻ����� ������ �̺�Ʈ�� �ʱ�ȭ�� ���� �ֻ��� ������ŭ ���� �� ó���� �� �����մϴ�.
	 * ���� ������ ���� 'true'��� ��� �ֻ����� ���������� �̺�Ʈ������ �����ʰ� �����մϴ�.
	 * ���� ������ ���� 'false'��� �ֻ��� �ϳ��� ������ �� ���� �̺�Ʈ������ �����ʰ� �����մϴ�.
	 * @param diceAtOnce �ֻ����� ������ �̺�Ʈ�� �ѹ��� ó�������� ����
	 */
	public void setDiceAtOnce(boolean diceAtOnce) {
		this.diceAtOnce = diceAtOnce;
	}

	
	/**
	 * ���� �������� �ʴ� �޼ҵ�
	 * @param yutsAtOnce
	 */
	public void setYutsAtOnce(boolean yutsAtOnce) {
		this.yutsAtOnce = yutsAtOnce;
	}

	/**
	 * ���� �ȵ���̵� ����̽��� ����ϴ� �÷��̾ �߻�ȭ�� ��ü�� �����մϴ�.
	 * @return �� ��ġ�� ����ϴ� �÷��̾�
	 */
	public Player getMe(){
		return Player.getThisPlayer();
	}
	
	
	
	//called by system
	
	/**
     * �����ڰ� ȣ������ �ʽ��ϴ�.
     * @return 
     */
    public DistributedBoardgameListener getDistributedBoardgameListener(){
        return instance.distributedBoardgameListener;
    }
	
    /**
     * �����ڰ� ȣ������ �ʽ��ϴ�.
     * @param dicePlusGameTools
     */
	void setDicePlusGameTools(DicePlusGameTool[] dicePlusGameTools) {
		if(this.dicePlusGameTools != null){
			Log.e(TAG,"aleady have dicePlusGameTools.");
		}
		this.dicePlusGameTools = dicePlusGameTools;
	}
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * @param yutGameTools
	 */
	void setYutGameTools(YutGameTool[] yutGameTools) {
		if(this.yutGameTools != null){
			Log.e(TAG,"aleady have yutGameTools.");
		}
		this.yutGameTools = yutGameTools;
	}
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
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
	 * ��������� ���� ���¸� �����մϴ�.
	 * ��ȯ ���� Ŭ�������� HOST_CLIENT_SELECT_MODE, HOST_PREPARE_MODE, CLIENT_JOIN_MODE, MIDDLE_OF_GAME�� �����ϼ���.
	 * @return ��������� ���� ����
	 */
	public int getState(){
		return state;
	}
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * @param state
	 */
	public void setState(int state){
		Log.i(TAG, "���� ��� �ٲ� : " + state);
		this.state = state;
	}
	
	/**
	 * �ǵ��� ����� �ֻ��� ������ �����մϴ�. �� ���� �� ��ü�� �ʱ�ȭ�� �����˴ϴ�.
	 * @return �ǵ��� ����� �ֻ����� ����
	 */
	public int getNumOfDiceIntention() {
		return numOfDiceIntention;
	}

	/**
     * ���� �������� �ʴ� �޼ҵ�
     */
	public int getNumOfYutIntentin() {
		return numOfYutIntentin;
	}

	public Context getContext(){
		return context;
	}

	/**
	 * ���� �ȵ���̵� ��ġ���� �����ϰ� �ִ� ��������� ��带 ��ȯ�մϴ�.
	 * ��ȯ ���� Ŭ��������
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
	
	/**
	 * �л� ������� �����ʸ� ����մϴ�. �ش� �����ʴ� ������ ���� �Ǵ� ������ ���õ� �̺�Ʈ�� ó���ϴ� �޼��带 �����մϴ�.
	 * �� �޼��尡 ȣ��� ���� �������� ��ϵ� �����ʿ����� �ش� �̺�Ʈ�� ó���˴ϴ�.
	 * @param distributedBoardgameListener ����� �л꺸����� ������
	 */
	public void registerDistributedBoardgameListener(DistributedBoardgameListener distributedBoardgameListener){
		instance.distributedBoardgameListener = distributedBoardgameListener;
	}
	
	/**
     * ���� ���� �����ʸ� ����մϴ�. �ش� �����ʴ� ���� ������ ���õ� �̺�Ʈ�� ó���ϴ� �޼��带 �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵ� �����ʿ����� �ش� �̺�Ʈ�� ó���˴ϴ�.
     * @param gameToolListener ����� ���� ���� ������
     */
	public void registerGameToolListener(GameToolListener gameToolListener){
		GameTool.registerGameToolListener(gameToolListener);
	}
	
	/**
     * ������ �����ʸ� ����մϴ�. �ش� �����ʴ� �������� ���¿� ���õ� �̺�Ʈ�� ó���ϴ� �޼��带 �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵ� �����ʿ����� �ش� �̺�Ʈ�� ó���˴ϴ�.
     * @param participantListener ����� ������ ������
     */
	public void registerParticipantListener(ParticipantListener participantListener){
		Mediator.registerParticipantListener(participantListener);
	}
	
	/**
     * �÷��̾� �����ʸ� ����մϴ�. �ش� �����ʴ� �÷��̾��� ���¿� ���õ� �̺�Ʈ�� ó���ϴ� �޼��带 �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵ� �����ʿ����� �ش� �̺�Ʈ�� ó���˴ϴ�.
     * @param playerListener ����� �л꺸����� ������
     */
	public void registerPlayerListener(PlayerListener playerListener){
		Player.registerPlayerListenr(playerListener);
	}
	
	//��� ����
	/**
	 * ����� �л� ������� �����ʸ� �����մϴ�.
	 * �� �޼��尡 ȣ��� ���� �������� ��ϵǾ����� �����ʰ� �������� �ʽ��ϴ�.
	 */
	public void unregisterDistributedBoardgameListener(){
		instance.distributedBoardgameListener = new DistributedBoardgameAdapter();
	}
	
	/**
     * ����� ���� ���� �����ʸ� �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵǾ����� �����ʰ� �������� �ʽ��ϴ�.
     */
	public void unRegisterGameToolListener(){
		GameTool.unRegisterGameToolListener();
	}
	
	/**
     * ����� ������ �����ʸ� �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵǾ����� �����ʰ� �������� �ʽ��ϴ�.
     */
	public void unregisterParticipantListener(){
		Mediator.unregisterParticipantListener();
	}
	
	/**
     * ����� �÷��̾� �����ʸ� �����մϴ�.
     * �� �޼��尡 ȣ��� ���� �������� ��ϵǾ����� �����ʰ� �������� �ʽ��ϴ�.
     */
	public void unRegisterPlayerListener(){
		Player.unRegisterPlayerListenr();
	}
	
	
	
	//����� �޸� ���� �����ɼ� �ִ°͵� �����ϴµ� ó��
	
	/**
	 * ���ø����̼��� �����ϱ� ������ �� �޼��尡 ȣ��Ǿ���մϴ�.
	 * �л꺸����� ���� ������ ���Ǿ��� �ڿ����� �Ҵ��� �����ϰ� 
	 * ���� Ŭ�������� �̱��� ��ü�� �ʱ�ȭ���� ���� ���·� ���������ϴ�.
	 * �� �޼��带 �ȵ���̵� Activity �ν��Ͻ��� onDestroy()�ȿ� �����մϴ�.
	 * ���ø����̼� ���� ������ �� �޼��尡 ȣ����� �ʾҴٸ� ���ø����̼��� ���� ȣ��Ǿ����� ������ �߻� ��ų �� �ֽ��ϴ�.
	 */
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
	
	/**
	 * �����ڰ� ȣ������ �ʽ��ϴ�.
	 * @param args
	 */
	public static void main(String[] args){
		//Log.w(TAG, "�� �޼��带 ȣ������ ������.");
		System.err.println("�� �޼��带 ȣ������ ������");
	}
}
