package kr.ac.kookmin.cs.distboard.system;

import java.util.ArrayList;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import kr.ac.kookmin.cs.distboard.util.ThreadTimer;
import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceConnectionListener;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.DiceResponseAdapter;
import us.dicepl.android.sdk.DiceResponseListener;
import us.dicepl.android.sdk.DiceScanningListener;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class DicePlusManager {

	private static final String TAG = "20083271:DicePlusManager";
	
	
	private static DicePlusManager instance = new DicePlusManager();
	private static final int[] developerKey = new int[] { 0x83, 0xed, 0x60, 0x0e, 0x5d, 0x31, 0x8f, 0xe7 };

	
	private boolean isTimeOuted = false; // Ÿ�Ӿƿ� �Ǿ����� ��Ÿ�� �� �����Ҷ��� �� ���� �ٽ� false �� �ǰ���
										//�� ���� false ��� ���� "���� ���� ��" �̶�� ��.
	
	private boolean hasPerfectlyNominated = false;//null�� �ֻ������� �Ϻ��ϰ� �ĺ�ȭ�Ǿ�����, �̰��� �ٽ� ��ҵ� �� �ִ�.
	
	private ThreadTimer threadTimer = null; // �߰�
	private boolean initialized = false;
	private int exactElectricGameToolDicePlus = 0;
	private ArrayList<Die> dice = new ArrayList<Die>();
	private ArrayList<Die> scaned = new ArrayList<Die>();//�̹� �˻��Ǿ� Ŀ��Ʈ �õ��ߴ� Die - �ӵ������� �ѹ��� 2���� ��ĳ�׵Ȱ� Ŀ��Ʈ�ϴ� ���� �ذ��ϱ�����
	//����������� scaned������ ��������!!(�ٽ� �����ؾߵǴϱ�.)
	
	DiceScanningListener scanningListener = new DiceScanningListener() {

		@Override
		public synchronized void onNewDie(Die die) {
			Log.d(TAG, "���ο� Dice+ : " + die.getAddress());
			//dicePluses.add(die);
			if(scaned.indexOf(die) == -1 && isTimeOuted == false && hasPerfectlyNominated == false){//������ ���� �ȵȳ��̸�
				Log.d(TAG, "���� Ž�� ��Ͽ� ���� Dice+ �Ǹ�: " + die.getAddress());
				scaned.add(die);
				DiceController.connect(die);
			}
		}
		

		@Override
		public synchronized void onScanStarted() {
			Log.d(TAG, "��ĵ ���۵�");
			if(isTimeOuted == true || hasPerfectlyNominated == true)
				BluetoothManipulator.cancelScan();
		}

		@Override
		public synchronized void onScanFailed() {
			Log.d(TAG, "��ĵ ����");
			//BluetoothManipulator.startScan()
		}

		@Override
		public synchronized void onScanFinished() {
			Log.d(TAG, "��ĵ ����");
			
			//Ÿ�Ӿƿ� �����ʾ����� �ٽýõ�
			if(isTimeOuted == false && hasPerfectlyNominated == false){
				Log.i(TAG, "����ĵ �õ���");
				BluetoothManipulator.startScan();
			}
			/*if (dice.size() != exactElectricGameToolDicePlus) {
				Log.d(TAG, "Rescan Started");
				BluetoothManipulator.startScan();
			}*/
		}
	};
	
	DiceConnectionListener connectionListener = new DiceConnectionListener() {

		@Override
		public synchronized void onConnectionEstablished(Die die) {
			Log.d(TAG, "DICE+ Connected");

			// Signing up for roll events
			if (dice.indexOf(die) == -1) {// ������ ����ȵȳ��̸�
				dice.add(die);
				CommunicationStateManager.getInstance().onDicePlusConnected(die);

				// ���� ��� �ֻ����� ������ �����׸� ��ĵ
				if (dice.size() == exactElectricGameToolDicePlus && hasPerfectlyNominated == false) {
					Log.i(TAG, "��� Dice+ ���� �Ϸ�");

					hasPerfectlyNominated = true;
					BluetoothManipulator.cancelScan();

					//��̳���Ʈ�� �ٲ� ��
					/*CommunicationStateManager.getInstance()
							.onDicePlusEstablishComplete(
									ArrayListConverter
											.toDiceArrayListToArray(dice));*/
					CandidateManager.getInstance().nominateDice(ArrayListConverter.toDiceArrayListToArray(dice));
					
					

					// ���� ���
					for (int i = 0; i < dice.size(); i++) {
						DiceController.subscribeRolls(dice.get(i));
					}
					Log.i(TAG, "��� Dice+ ���� ��� �Ϸ�");
				}
			}else{
				//�̹� �����ϴ� ���̸�
				// = �ȵǴµ�..
				Log.e(TAG, "�̹� ����� DICE+");
			}
		}

		@Override
		public synchronized void onConnectionFailed(Die die, Exception e) {
			Log.d(TAG, "���� ����", e);
			scaned.remove(die);
			if(isTimeOuted == false)
				BluetoothManipulator.startScan();
		}

		@Override
		public synchronized void onConnectionLost(Die die) {
			Log.d(TAG, "Connection Lost");
			
			//���躸��
			CommunicationStateManager.getInstance().onDicePlusLost(die);

			if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE && initialized == true){//���� ���� ���϶�, �Ǵ� �ʱ�ȭ�Ǿ�����, �ʱ�ȭ�� clear�ÿ� ���� ����� �ٽ� �ҷ����� �ʰ��ϱ� ����.
				//�ش� �ֻ����� ���� - ������ ���� ��Ʈ���̾�� �ȵǱ� ����
				hasPerfectlyNominated = false;
				dice.remove(die);
				scaned.remove(die);
				BluetoothManipulator.startScan();
			}else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME && initialized == true){//�����߿� ����ٸ�
			    Log.e(TAG, "�������϶� DICE+ ���� ����.");
			}
			//BluetoothManipulator.startScan();
		}

	};
	
	DiceResponseListener responseListener = new DiceResponseAdapter() {

		@Override
		public synchronized void onRoll(Die die, RollData rollData, Exception e) {
			super.onRoll(die, rollData, e);

			if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
				Log.d(TAG, "Roll: " + rollData.face);
				SubjectDeviceMapper.getInstance().map(die).setFace(rollData.face);//���̽� �÷��� �������� ������ �����ϰ�
				
				GameToolSystemManager.getInstance().onDicePlusRoll(die, rollData);//���� �ý����� ���� �Ѵ�
			}else{
				Log.w(TAG, "�������� �ƴҶ� ������ �ֻ���");
			}

		}
	};
	
	
	//������
	private DicePlusManager() {
		
	}
	
	public static DicePlusManager getInstance(){
		return instance;
	}
	
	public void initialize(){//�̱��� ���ɿ�
		Log.i(TAG, "DICE+ �ʱ�ȭ");
		
		initialized = true;
		
		
		threadTimer = null;
		isTimeOuted = false;
		hasPerfectlyNominated = false;
		exactElectricGameToolDicePlus = 0;
		dice = new ArrayList<Die>();
		scaned = new ArrayList<Die>();
		
	}
	
	public void establish(int exactElectricGameToolDicePlus, int millisecTimeOut){
		
			Log.i(TAG, "Dice+ ���� ����");

			// �ʱ�ȭ ���� �ʾ����� : �̱��� ���ɿ�
			if (initialized == false) {
				Log.e(TAG, "�ʱ�ȭ ���� ����");
				return;
			}

			// ������ Dice+ ������ 0���̸�
			if (exactElectricGameToolDicePlus == 0) {
				// ���� �� �ٷ� ����
				hasPerfectlyNominated = true;
				CandidateManager.getInstance().nominateDice(new Die[0]);
				// CommunicationStateManager.getInstance().onDicePlusEstablishComplete(new
				// Die[0]);
				return;
			}

			BluetoothManipulator.initiate(DistributedBoardgame.getInstance().getContext());
			DiceController.initiate(developerKey);

			this.exactElectricGameToolDicePlus = exactElectricGameToolDicePlus;

			// Listen to all the state occurring during the discovering process
			// of DICE+
			BluetoothManipulator.registerDiceScanningListener(scanningListener);

			// When connecting to DICE+ you get two responses: a good one and a
			// bad one ;)
			DiceController.registerDiceConnectionListener(connectionListener);

			// Attaching to DICE+ events that we subscribed to.
			DiceController.registerDiceResponseListener(responseListener);

			// Scan for a DICE+
			BluetoothManipulator.startScan();
			Log.i(TAG, "Dice+ ���� ����");

			Log.i(TAG, "Dice+ Ÿ�Ӿƿ� ���� ���� : " + millisecTimeOut);
			threadTimer = new ThreadTimer(
					ThreadTimer.DICE_PLUS_MANAGER_ESTABLISH_CALL,
					millisecTimeOut);
			threadTimer.start();
		
	}
	
	public void discardDicePluses(){
		
		/**
		 * ��� ����ȳ��� �ϳ��̻������� �̳��� �Ҹ���ȵ� ; ���� ���� ��ư ���ִ°ŷ� �����ֱ��ߴµ�..
		 */
		
		Log.i(TAG, "Dice plus �����ϼ� ����");
		
		//���� �ϼ�
		hasPerfectlyNominated = true;
		
		BluetoothManipulator.cancelScan();
		
		Log.e(TAG, "discard�� clear() �̰Ͷ����� ������ ���� �� �־ ���� �α� ; �׸��� �������̳� Ŭ���̾�Ʈ�� �̷�ó�� ������");
		clear();
		
		CandidateManager.getInstance().nominateDice(new Die[0]);
		
		CommunicationStateManager.getInstance().onDicePlusEstablishForceComplete();
		
	}
	
	public synchronized boolean isAvailableDevice(Die die){
        if(dice.indexOf(die) != -1){
            return true;
        }
        return false;
    }
	
	//call by ThreadTimer
	
	public synchronized void onEstablishTimeOut(){
		Log.i(TAG, "Dice+ ���� Ÿ�Ӿƿ���");
		//�� ���ο� ������ connected �Ǵ°Ŵ� ��� ó����?
		
		//Ÿ�Ӿƿ� �ڿ� ��� ���ӽõ� ���
	    isTimeOuted = true;
	    
	    //�ϴ� ��ĵ���
	    BluetoothManipulator.cancelScan();
	    
	    //�ҿ����� �����̸� 
	    if(exactElectricGameToolDicePlus != dice.size() && hasPerfectlyNominated == false){
	    	Log.e(TAG, "�ҿ����� Dice+ ���� : " + dice.size() + "/" + exactElectricGameToolDicePlus);
	    	if(dice.size() > 0){
	    		//��� ���� "�� �ϴ� ��������." : �籸���Ҷ� ���ݸ� �� �����ϸ�ǵ��� �ϱ�����..
	    		//dice.removeAll(dice);
	    	}
	    	
			Log.w(TAG, "Ÿ�Ӿƿ����� ��̳���Ʈ - ���̽� �÷���");
			CandidateManager.getInstance().nominateDicePlusEstablishFail();
			
	    	//CommunicationStateManager.getInstance().onDicePlusEstablishFailed();
	    	
	    }else if(exactElectricGameToolDicePlus == dice.size() && hasPerfectlyNominated == false){
	    	Log.i(TAG, "������ Dice+ ���� : " + dice.size() + "/" + exactElectricGameToolDicePlus);
	    }
	    
	}
	
	//getter
	
	@Deprecated
	public Die[] getDice(){
		return (Die[])dice.toArray();
	}
	
	//��ġ ����
	
	public void release(Die die){
		
		DiceController.disconnectDie(die);
		dice.set(dice.indexOf(die), null);
		//die = null; null �ϸ� ���ۿ��� isAvaiable ��������
	}
	
	public void releaseAll(){
		for(int i = 0 ; i < dice.size() ; i++){
			DiceController.disconnectDie(dice.get(i));
			dice.set(i, null);
			//dice.set(i, null); null �ϸ� ���ۿ��� isAvaiable ��������
		}
	}
	
	//���߰ų� �ı��� �̰� ȣ��
	public synchronized void clear(){
		
		Log.i(TAG, "Dice+ ����");
		initialized = false;
		isTimeOuted = true;
		
		if(Mediator.getInstance().getMode() == Mode.HOST ){
			Log.i(TAG, "ȣ��Ʈ ��忡�� ����");
			
			BluetoothManipulator.cancelScan();
			
			BluetoothManipulator.unregisterDiceScanningListener(scanningListener);
			DiceController.unregisterDiceConnectionListener(connectionListener);
			DiceController.unregisterDiceResponseListener(responseListener);

			Log.i(TAG, "ȣ��Ʈ ��忡�� cancelScan, unregister() �Ϸ�");
			
			for(int i = 0 ; i < dice.size() ; i++){
				DiceController.disconnectDie(dice.get(i));//initialized false�� ���̻� scan ȣ�� ���ϵ��� ����.
				//dice.set(i, null); null �ϸ� ���ۿ��� isAvaiable ��������
			}
		
			//�ֻ��� ����Ʈ�� ��� ���� ����
			dice.removeAll(dice);
		}
	}
	
	public synchronized void setTimeouted(){
        this.isTimeOuted = true;
    }
}