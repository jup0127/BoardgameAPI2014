package kr.ac.kookmin.cs.distboard.system;

import java.util.ArrayList;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.controller.GameToolListener;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Reply;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.GameToolData;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.util.ArrayListConverter;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//�ý��� �������� 
public class GameToolSystemManager {
	
	private static final String TAG = "20083271:GameToolSystemManager";
	private static GameToolSystemManager instance = new GameToolSystemManager();
	
	private boolean initialized = false;
	
	//�ѹ��� ���� �� �޾Ƽ� ó���ϱ� �� ������ ����
	//private int diceAtOnceCounter = 0;
	//private int yutsAtOnceCounter = 0;
	
	//���� ������ ��ġ ����Ʈ
	private ArrayList<GameToolData> tempDiceGameToolDatas = new ArrayList<GameToolData>();
	private ArrayList<GameToolData> tempYutGameToolDatas = new ArrayList<GameToolData>();
	
	private GameToolSystemManager(){
		
	}
	
	public static GameToolSystemManager getInstance(){
		return instance;
	}
	
	public void initialize(){
		initialized = true;
		
		//diceAtOnceCounter = 0;
		//yutsAtOnceCounter = 0;
		tempDiceGameToolDatas = new ArrayList<GameToolData>();
		tempYutGameToolDatas = new ArrayList<GameToolData>();
	}
	
	public void checkInitialized(){
		if(initialized == false){
			Log.e(TAG, "�ʱ�ȭ���� ����");
		}
	}
	
	//
	public void onDicePlusRoll(Die die, RollData rollData){//rollData�ȿ��� ���� ������ ���ԵǾ��ִ�.
		if(DistributedBoardgame.getInstance().isGetDieValuesAtOnce() == false){//�ѹ��� ó���ؾߵǴ� ��Ȳ�� �ƴϸ�
			//�ٷ� ����
			GameTool.getGameToolListener().onDiceRoll(null, new GameToolData(rollData.face, null));
		}else{//�ѹ��� ó���ؾߵǴ� ��Ȳ�̸�
			
			/**
			 * �ߺ��˻� �ʿ�(�� �ֻ��� �ι������� ����)
			 */
		    
			tempDiceGameToolDatas.add(new GameToolData(rollData.face, null));
			
			//��� �ֻ����� �������ٸ�
			if(tempDiceGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfDiceIntention()){
				GameTool.getGameToolListener().onDiceRolls(null, ArrayListConverter.gameToolDataArrayListToArray(tempDiceGameToolDatas));
				//tempDiceGameToolDatas.clear();
				tempDiceGameToolDatas.clear();//�̰Ͷ����� ������ �߻��� ���� �־�
			}
		}
	}
	
	public void onElectricYutRoll(BluetoothDevice yutDevice, int face){//rollData�� �ȿ� ���� �����Ͱ� �մ°��� ������ ���� �����Ͱ� ���ڷ� �� ����
	    
	    Log.i(TAG, "onElectricYutRoll FACE : " + face);
	    
	    if(DistributedBoardgame.getInstance().isGetYutValuesAtOnce() == false){//�ѹ��� ó���ؾߵǴ� ��Ȳ�� �ƴϸ�
            //�ٷ� ����
            GameTool.getGameToolListener().onYutRoll(null, new GameToolData(face, null));
        }else{//�ѹ��� ó���ؾߵǴ� ��Ȳ�̸�
            
            /**
             * �ߺ��˻� �ʿ�(�� �ֻ��� �ι������� ����)
             */
            tempYutGameToolDatas.add(new GameToolData(face, null));
            
            //��� �ֻ����� �������ٸ�
            if(tempYutGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfYutsIntentin()){
                GameTool.getGameToolListener().onYutRolls(null, ArrayListConverter.gameToolDataArrayListToArray(tempYutGameToolDatas));
                tempYutGameToolDatas.clear();
            }
        }
	}
	
	public void onLocalVirtualDiceRoll(int[] face){//rollData�� �ȿ� ���� �����Ͱ� �մ°��� ������ ���� �����Ͱ� ���ڷ� �� ����
		
		Log.i(TAG, "���� ���� �ֻ��� ������");
		
		//������ ȣ��Ʈ
		if(DistributedBoardgame.getInstance().getMode() == Mode.HOST){
		    onRemoteVirtualDiceRoll(DistributedBoardgame.getInstance().getMe(), face);
		    return;
		}
		
		//�ƴϸ� ȣ��Ʈ�� ����
		Log.i(TAG, "Ŭ���̾�Ʈ��� ȣ��Ʈ�� ���� �ֻ����� �迭 ���� : " + DistributedBoardgame.getInstance().getMode());
		
		if(DistributedBoardgame.getInstance().getMode() == Mode.CLIENT)
			RequestReplyManager.getInstance().sendReply(DistributedBoardgame.getInstance().getHost(), Reply.ROLL_DICE_RESULT, face);
		
		Log.d(TAG, "���� �ֻ����� �迭 ���� �Ϸ�");	
		
		Log.i(TAG, "���� ���� �ֻ��� ������ �޼��� ����");
	}
	
	public void onRemoteVirtualDiceRoll(Player player, int[] face){
		Log.i(TAG, "����Ʈ ���� �ֻ��� ������ �ֻ��� ���̴�: " + face.length);
		//Ŭ���̾�Ʈ���� �°Ŵϱ�(ȣ��Ʈ �� ���� �־�)
		//������ ó���� ������ ������ �۵�
		if(DistributedBoardgame.getInstance().isGetDieValuesAtOnce() == false){//�ѹ��� ó���ؾߵǴ� ��Ȳ�� �ƴϸ�
			//�ٷ� ����
			Log.d(TAG, "���� ó���ϴ� ��Ȳ");
			for(int i = 0 ; i < face.length ; i++){
				GameTool.getGameToolListener().onDiceRoll(player, new GameToolData(face[i], player));
			}
		}else{//�ѹ��� ó���ؾߵǴ� ��Ȳ�̸�
			Log.d(TAG, "�ѹ��� ó���ϴ� ��Ȳ");
			/**
			 * �ߺ��˻� �ʿ�(�� �ֻ��� �ι������� ����)
			 */
			for(int i = 0 ; i < face.length ; i++){
				tempDiceGameToolDatas.add(new GameToolData(face[i], player));
			}
			
			//��� �ֻ����� �������ٸ�
			if(tempDiceGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfDiceIntention()){
				GameTool.getGameToolListener().onDiceRolls(player, ArrayListConverter.gameToolDataArrayListToArray(tempDiceGameToolDatas));
				tempDiceGameToolDatas.clear();
			}
		}
	}
}
