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

//시스템 차원에서 
public class GameToolSystemManager {
	
	private static final String TAG = "20083271:GameToolSystemManager";
	private static GameToolSystemManager instance = new GameToolSystemManager();
	
	private boolean initialized = false;
	
	//한번에 여러 롤 받아서 처리하기 용 변수들 선언
	//private int diceAtOnceCounter = 0;
	//private int yutsAtOnceCounter = 0;
	
	//낮은 수준의 장치 리스트
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
			Log.e(TAG, "초기화되지 않음");
		}
	}
	
	//
	public void onDicePlusRoll(Die die, RollData rollData){//rollData안에는 많은 정보가 포함되어있다.
		if(DistributedBoardgame.getInstance().isGetDieValuesAtOnce() == false){//한번에 처리해야되는 상황이 아니면
			//바로 보고
			GameTool.getGameToolListener().onDiceRoll(null, new GameToolData(rollData.face, null));
		}else{//한번에 처리해야되는 상황이면
			
			/**
			 * 중복검사 필요(한 주사위 두번던지기 방지)
			 */
		    
			tempDiceGameToolDatas.add(new GameToolData(rollData.face, null));
			
			//모든 주사위가 굴려졌다면
			if(tempDiceGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfDiceIntention()){
				GameTool.getGameToolListener().onDiceRolls(null, ArrayListConverter.gameToolDataArrayListToArray(tempDiceGameToolDatas));
				//tempDiceGameToolDatas.clear();
				tempDiceGameToolDatas.clear();//이것때문에 오류가 발생할 수도 있어
			}
		}
	}
	
	public void onElectricYutRoll(BluetoothDevice yutDevice, int face){//rollData의 안에 많은 데이터가 잇는것을 감안해 많은 데이터가 인자로 올 예정
	    
	    Log.i(TAG, "onElectricYutRoll FACE : " + face);
	    
	    if(DistributedBoardgame.getInstance().isGetYutValuesAtOnce() == false){//한번에 처리해야되는 상황이 아니면
            //바로 보고
            GameTool.getGameToolListener().onYutRoll(null, new GameToolData(face, null));
        }else{//한번에 처리해야되는 상황이면
            
            /**
             * 중복검사 필요(한 주사위 두번던지기 방지)
             */
            tempYutGameToolDatas.add(new GameToolData(face, null));
            
            //모든 주사위가 굴려졌다면
            if(tempYutGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfYutsIntentin()){
                GameTool.getGameToolListener().onYutRolls(null, ArrayListConverter.gameToolDataArrayListToArray(tempYutGameToolDatas));
                tempYutGameToolDatas.clear();
            }
        }
	}
	
	public void onLocalVirtualDiceRoll(int[] face){//rollData의 안에 많은 데이터가 잇는것을 감안해 많은 데이터가 인자로 올 예정
		
		Log.i(TAG, "로컬 가상 주사위 굴려짐");
		
		//로컬이 호스트
		if(DistributedBoardgame.getInstance().getMode() == Mode.HOST){
		    onRemoteVirtualDiceRoll(DistributedBoardgame.getInstance().getMe(), face);
		    return;
		}
		
		//아니면 호스트로 전송
		Log.i(TAG, "클라이언트라면 호스트로 가상 주사위값 배열 전송 : " + DistributedBoardgame.getInstance().getMode());
		
		if(DistributedBoardgame.getInstance().getMode() == Mode.CLIENT)
			RequestReplyManager.getInstance().sendReply(DistributedBoardgame.getInstance().getHost(), Reply.ROLL_DICE_RESULT, face);
		
		Log.d(TAG, "가상 주사위값 배열 전송 완료");	
		
		Log.i(TAG, "로컬 가상 주사위 굴려짐 메서드 끝남");
	}
	
	public void onRemoteVirtualDiceRoll(Player player, int[] face){
		Log.i(TAG, "리모트 가상 주사위 굴려짐 주사위 길이는: " + face.length);
		//클라이언트에서 온거니까(호스트 일 수도 있어)
		//적절히 처리후 게임툴 리스너 작동
		if(DistributedBoardgame.getInstance().isGetDieValuesAtOnce() == false){//한번에 처리해야되는 상황이 아니면
			//바로 보고
			Log.d(TAG, "따로 처리하는 상황");
			for(int i = 0 ; i < face.length ; i++){
				GameTool.getGameToolListener().onDiceRoll(player, new GameToolData(face[i], player));
			}
		}else{//한번에 처리해야되는 상황이면
			Log.d(TAG, "한번에 처리하는 상황");
			/**
			 * 중복검사 필요(한 주사위 두번던지기 방지)
			 */
			for(int i = 0 ; i < face.length ; i++){
				tempDiceGameToolDatas.add(new GameToolData(face[i], player));
			}
			
			//모든 주사위가 굴려졌다면
			if(tempDiceGameToolDatas.size() == DistributedBoardgame.getInstance().getNumOfDiceIntention()){
				GameTool.getGameToolListener().onDiceRolls(player, ArrayListConverter.gameToolDataArrayListToArray(tempDiceGameToolDatas));
				tempDiceGameToolDatas.clear();
			}
		}
	}
}
