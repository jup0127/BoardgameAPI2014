package kr.ac.kookmin.cs.distboard.protocol;

import java.io.Serializable;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import kr.ac.kookmin.cs.distboard.system.ClientManager;
import kr.ac.kookmin.cs.distboard.system.DicePlusManager;
import kr.ac.kookmin.cs.distboard.system.ElectricYutManager;
import kr.ac.kookmin.cs.distboard.system.EmulatorReceiver;
import kr.ac.kookmin.cs.distboard.system.GameToolSystemManager;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;
import kr.ac.kookmin.cs.distboard.util.AsynchronousNotAvailableThread;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class RequestReplyManager{
	
	public static final String TAG = "20083271:RequestReplyManager";
	
	private static RequestReplyManager instance = new RequestReplyManager();

	
	//message byte ���̺� ��
	
	
	private boolean initialized = false;
	
	private RequestReplyManager(){
		
	}
	
	public static RequestReplyManager getInstance(){
		return instance;
	}
	
	public void initialize(){
		instance.initialized = true;
	}

	//�޾��� �� ó��
	public void handleMessage(Player player, Object message){
		Log.d(TAG, "�޽��� ����, �ڵ鷯 ó�� ����");
		Request currentRequest = null;
		Reply currentReply = null;
		
		if(message instanceof Request){
			Log.d(TAG, "��û �޽����� �Ǹ�");
			currentRequest = (Request)message;
			
			switch(currentRequest.code){
				case Request.OK_TO_GO:
					Log.d(TAG, "OK_TO_GO�� �Ǹ�");
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_OK_TO_GO).sendToTarget();
					break;
				case Request.OK_TO_RESUME:
					Log.d(TAG, "OK_TO_RESUME���� �Ǹ�");
					Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_OK_TO_RESUME).sendToTarget();
					break;
				case Request.GIVE_OBJECT://
					Log.d(TAG, "GIVE_SITUATION�� �Ǹ�");
					Player.getPlayerListener().onReceiveObject(player, currentRequest.content);
					break;
				case Request.GIVE_SITUATION://
                    Log.d(TAG, "GIVE_SITUATION�� �Ǹ�");
                    ObjectArgument objArgument = ((ObjectArgument)currentRequest.content);
                    if(currentRequest.content instanceof ObjectArgument == false){
                        Log.e(TAG, "GIVE_SITUATION ��Ȳ���� ������Ʈ�� '������Ʈ����'���� �ƴ�");
                        break;
                    }
                    Player.getPlayerListener().onReceiveSituation(player, objArgument.obj , objArgument.arg1, objArgument.arg2);
                    break;
				case Request.APPEAR_DICE_EMULATOR:
					Log.d(TAG, "APPEAR_DICE_EMULATOR�� �Ǹ�");
					EmulatorReceiver.getInstance().appear();
					break;
				case Request.DISSAPEAR_DICE_EMULATOR:
					Log.d(TAG, "DISSAPPEAR_DICE_EMULATOR�� �Ǹ�");
					EmulatorReceiver.getInstance().disappear();
					break;
				case Request.SET_NUM_OF_DICE:
                    Log.d(TAG, "SET_NUM_OF_DICE�� �Ǹ�");
                    EmulatorReceiver.getInstance().setNumberOfDice((Integer)currentRequest.content);
                    break;
				case Request.SET_NUM_OF_YUT:
                    Log.d(TAG, "SET_NUM_OF_YUT�� �Ǹ�");
                    EmulatorReceiver.getInstance().setNumberOfYut((Integer)currentRequest.content);
                    break;
			}

		}else if(message instanceof Reply){
			Log.d(TAG, "���� �޽����� �Ǹ�");
			currentReply = (Reply)message;
			
			switch(currentReply.code){
			case Reply.ROLL_DICE_RESULT:
				Log.d(TAG, "ROLL_DICE_RESULT�� �Ǹ�");
				GameToolSystemManager.getInstance().onRemoteVirtualDiceRoll(player, (int[])currentReply.content);
			}
		}
	}
	
	public void handleMessage(Player player, byte[] bytesMessage){
		
	}
	
	public void sendRequest(Player player, int requestType, Object obj){
		
	    if(isAvaliable(player) == false){
		    new AsynchronousNotAvailableThread(player).start();
		    return;
		}
		
		//Log.i(TAG, "�÷��̾�� ��ü���� ���� : " + SubjectDeviceMapper.getInstance().map(player).getName());
		
		
		if(obj == null){
			Log.w(TAG, "���۵Ǵ� �������� null ! ");
		}
		
		
		if(obj != null && obj instanceof Serializable == false){
			Log.e(TAG, "��ü�� ����ȭ�������� �ʽ��ϴ�. ������ ����մϴ�.");
			return;
		}
		
		
		//�� �ΰ�� ��������.... ������
		
		
		Request request = Request.getRequest(requestType, obj);
		
		Log.i(TAG, SubjectDeviceMapper.getInstance().map(player).getName() + "���� �޽����� �۽�");
		Log.i(TAG, "�۽ŵǴ� ������ : ������Ʈ : " + request.content);
		Log.i(TAG, "�۽ŵǴ� ������ : �ڵ� : " + request.code);
		ClientManager.getInstance().write(SubjectDeviceMapper.getInstance().map(player), request);
		
		
	}
	
	public void sendRequestToAllPlayer(int requestType, Object obj){
	    
		Log.d(TAG, "��� �÷��̾�� ��ü ���� ����");
		Player[] currentPlayers = DistributedBoardgame.getInstance().getPlayers();
		
		
		for(int i = 0 ; i < currentPlayers.length ; i++){
			Log.d(TAG, "�÷��̾� �� : " + currentPlayers.length);
			if(currentPlayers[i] != null){//������ ������ �ʾҴٸ�
				Log.d(TAG, "���� �޽��� ���� ������ Ŭ���̾�Ʈ : " + SubjectDeviceMapper.getInstance().map(currentPlayers[i]).getName());
				RequestReplyManager.getInstance().sendRequest(currentPlayers[i], requestType, obj);
			}
		}
	}
	
	public void sendReply(Player player, int replyType, Object obj){
	    if(isAvaliable(player) == false){
            new AsynchronousNotAvailableThread(player).start();
            return;
        }
	    
		//�� �ΰ�� ��������.... ������
		Reply reply = Reply.getRequest(replyType, obj);
		ClientManager.getInstance().write(SubjectDeviceMapper.getInstance().map(player), reply);
	}

	private boolean isAvaliable(Object obj){
	    Log.i(TAG, "��ü�� ���۰����� �������� �˻�");
	    if(obj instanceof Player){
	        Log.i(TAG, "��ü(�÷��̾�)�� ���۰����� �������� �˻�");
	        return ClientManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((Player)obj));
	    }else if(obj instanceof GameTool){
	        if(obj instanceof DicePlusGameTool){
	            Log.i(TAG, "��ü(DICE+)�� ���۰����� �������� �˻�");
	            return DicePlusManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((DicePlusGameTool)obj));
	        }else if(obj instanceof YutGameTool){
	            Log.i(TAG, "��ü(I-BAR)�� ���۰����� �������� �˻�");
	            return ElectricYutManager.getInstance().isAvailableDevice(SubjectDeviceMapper.getInstance().map((Player)obj));
	        }
	    }
	    Log.e(TAG, "��ü�� ���۰����� �������� �˻� : �� �� ���� Ÿ���� ��ü");
	    return false;
	}
}
