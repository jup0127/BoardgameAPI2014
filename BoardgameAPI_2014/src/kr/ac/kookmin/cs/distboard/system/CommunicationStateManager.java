package kr.ac.kookmin.cs.distboard.system;

import kr.ac.kookmin.cs.distboard.DistributedBoardgame;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.protocol.Reply;
import kr.ac.kookmin.cs.distboard.protocol.Request;
import kr.ac.kookmin.cs.distboard.protocol.RequestReplyManager;
import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CommunicationStateManager {
	
	private static final String TAG = "20083271:CommunicationStateManager";
	private static CommunicationStateManager instance = new CommunicationStateManager();
	
	
	//private boolean initialized = false;
	//private Handler handler = null;
	
	private CommunicationStateManager(){
		
	}
	
	public static CommunicationStateManager getInstance(){
		return instance;
	}
	
	/*public void initialize(Handler handler){
		initialized = true;
		this.handler = handler;
	}*/
	
	/*public void checkInitialized(){
		if(initialized == false){
			Log.e(TAG, "�ʱ�ȭ���� ����");
		}
	}*/
	
	
	//called by BluetoothManager
	
	
	//ȣ��Ʈ
	
	
	//ȣ��Ʈ - �ʱ� �������� �Ϸ�Ǹ�
	public void onEstablishComplete(BluetoothDevice[] clientDevices, BluetoothDevice[] yutDevices, Die[] dice){
		
		
		//checkInitialized();
		Log.i(TAG, "��ġ���� �Ϸ��");
		
		if(DistributedBoardgame.getInstance().getMode() != Mode.HOST){
			Log.e(TAG, "ȣ��Ʈ ��ġ�� �ƴ� �ȵ���̵� �ν��Ͻ��� ȣ��");
		}
		
		//���� ���
		SubjectDeviceMapper.getInstance().registerClientDevices(clientDevices);
		SubjectDeviceMapper.getInstance().registerYutDevices(yutDevices);
		SubjectDeviceMapper.getInstance().registerDice(dice);
		
		DicePlusManager.getInstance().setTimeouted();//2014�� 4�� 14�� �߰��� �����Ϸ��� isTimeout�� "true"�� ������ �ʾҴ�.
		ElectricYutManager.getInstance().setTimeouted();//2014�� 4�� 14�� �߰��� �����Ϸ��� isTimeout�� "true"�� ������ �ʾҴ�.
		
		//ȣ��Ʈ ��� �ڱ� ��Ƽ��Ƽ ���� (Ŭ���̾�Ʈ�� ���� ��������..����)

		//��� ������ OK_TO_GO �� Ŭ���̾�Ʈ���� ����
	}
	
	public synchronized void onPlayerEstablishForceComplete(){
        Log.i(TAG, "Ŭ���̾�Ʈ ���� ���� ���� - �����ϼ� ��ư ������");
        Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_FORCE_COMPLETE);
        message.sendToTarget();
    }
	
	//ȣ��Ʈ - Ŭ���̾�Ʈ�Ǵ� ���ڵ����� ������ �����ϸ�
	public void onConnected(BluetoothDevice device){
		//checkInitialized();
		Log.i(TAG, "��ġ ����� : " + device.getAddress());
		//�� ���� ���
		//1. ������
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "ȣ��Ʈ �غ� ��忡�� �����");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_CONNECTED);
			message.obj = device;
			message.sendToTarget();
		}
		//2. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.w(TAG, "ȣ��Ʈ ������ ��忡�� �����");
		}else{
			Log.w(TAG, "� ��Ȳ���� ����Ǿ����� ��");
		}
	}
	
	
	public void onReconnected(BluetoothDevice device){
	    SubjectDeviceMapper.getInstance().replaceClientDevice(device);
		RequestReplyManager.getInstance().sendRequest((Player)SubjectDeviceMapper.getInstance().map(device), Request.OK_TO_RESUME, null);
		Mediator.getParticipantListener().onPlayerRejoin((Player)(SubjectDeviceMapper.getInstance().map(device)));
	}
	
	
	//ȣ��Ʈ - �ʱ� ���� �ð�������
	public void onEstablishTimeOut(){
		Log.i(TAG, "��ü�� ������ Ÿ�Ӿƿ��� : �� ���ø����̼� ����");
		
		//checkInitialized();
		Mediator.getParticipantListener().onPrepareFail(); //�ǹ̾����� ������ ������..
		
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_TIMEOUT);
		message.sendToTarget();
	}
	
	//ȣ��Ʈ - ���� ��ġ�� ���� WAIT �����ϸ�(���������� �������� ��)
	public void onWaitComplete(BluetoothDevice device){
		//checkInitialized();
		//�������߿��� �� ������ ����.
	}
	
	//ȣ��Ʈ - �ش� ��ġ�� �ð����� ���ӾȵǸ�/���ϸ�
	public void onWaitTimeout(BluetoothDevice device){
		//checkInitialized();
		//���ڵ��� ���
		//Ŭ���̾�Ʈ ���
	}
	
	//ȣ��Ʈ - ���� ���� ��ġ�� ���ӽõ��ϸ�
	@Deprecated
	public void onUnexpectedConnectionDetected(BluetoothDevice device){
		//checkInitialized();
	}
	
	
	
	//Ŭ���̾�Ʈ
	
	
	//Ŭ���̾�Ʈ - ȣ��Ʈ�� ���� ������
	public void onConnectionComplete(BluetoothDevice hostDevice){
		
		if(Mediator.getInstance().getMode() != Mode.CLIENT){
			Log.e(TAG, "Ŭ���̾�Ʈ ��ġ�� �ƴ� �ȵ���̵� �ν��Ͻ��� ȣ��");
		}
		//checkInitialized();
		if(hostDevice == null){
			Log.e(TAG, "ȣ��Ʈ ��ġ�� null��");
		}
		
		Log.i(TAG, "ȣ��Ʈ ����� : " + hostDevice.getAddress());
		//�� ���� ���
		//1. ������
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
			Log.i(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : Ŭ���̾�Ʈ �շ� ���");
			
			SubjectDeviceMapper.getInstance().registerClientDevices(new BluetoothDevice[]{hostDevice});//��� �̰��� ���̰� 1
			SubjectDeviceMapper.getInstance().registerYutDevices(new BluetoothDevice[0]);//���� 0
			SubjectDeviceMapper.getInstance().registerDice(new Die[0]);//���� 0
			
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_COMPLETE);
			message.obj = hostDevice;
			message.sendToTarget();
		}
		//2. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : Ŭ���̾�Ʈ ������ ���");
		}else{
			Log.w(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : ����������� ��");
		}
		
	}
	
	//Ŭ���̾�Ʈ - ���� ���н�
	public void onConnectionFail(BluetoothDevice device){
		//checkInitialized();
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_FAILED);
		message.obj = device;//ȣ��Ʈ ��ġ
		message.sendToTarget();
	}
	
	//Ŭ���̾�Ʈ - ������ ������
	public void onReconnectComplete(BluetoothDevice device){
		//checkInitialized();
		if(DistributedBoardgame.getInstance().getState() != DistributedBoardgame.MIDDLE_OF_GAME){
			Log.e(TAG, "�������� �ƴ� ��Ȳ���� ȣ��� �� ���� �Ϸ�");
			return;
		}
		//checkInitialized();
		if(device == null){
			Log.e(TAG, "������ ��ġ�� null��");
			return;
		}
		
		Log.i(TAG, "��ġ �翬��� : " + device.getName());

		Log.i(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : Ŭ���̾�Ʈ �շ� ���");

		//Mediator.getParticipantListener().onPlayerRejoin((Player)(SubjectDeviceMapper.getInstance().map(device)));

	}
	
	//????????????????? - ������ ���н�
	public void onReconnectFailed(){
		//checkInitialized();
		Log.w(TAG, "�翬�� ����");
	}
	
	
	
	//ȣ��Ʈ/Ŭ���̾�Ʈ
	
	//ȣ��Ʈ - Ŭ���̾�Ʈ ���� �����
	public void onConnectionLost(BluetoothDevice device){
		//checkInitialized();
		Log.i(TAG, "Ŭ���̾�Ʈ ���� ���� : " + device.getAddress());
		//�� ���� ���
		//1. ȣ��Ʈ �غ���
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "ȣ��Ʈ �ڵ鷯 ���� : ȣ��Ʈ �غ� ��忡�� ���� ������");
			SubjectDeviceMapper.getInstance().lostPlayer();
			
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_PLAYER_DISCONNECTED);
			message.obj = device;
			message.sendToTarget();
			
            Message message2 = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.GAME_IS_NOT_STARTABLE);
            message2.sendToTarget();
		}
		//2. Ŭ���̾�Ʈ �շ���
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.CLIENT_JOIN_MODE){
			Log.i(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : Ŭ���̾�Ʈ �շ� ��忡�� ���� ������");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.CONNECT_LOST);
			message.obj = device;
			message.sendToTarget();
		}
		//3. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "Ŭ���̾�Ʈ �ڵ鷯 ���� : ȣ��Ʈ/Ŭ���̾�Ʈ �����߿� ���� ������");
			Mediator.getParticipantListener().onPlayerLeave((Player)SubjectDeviceMapper.getInstance().map(device));
		}else{
			Log.w(TAG, "����������� ��");
		}
	}
	
	//������Ʈ �Ǵ� ����Ʈ �迭 ������ ȣ��
	public synchronized void onObjectDelivered(BluetoothDevice device, Object obj){
		RequestReplyManager.getInstance().handleMessage((Player)SubjectDeviceMapper.getInstance().map(device), obj);
		//������ ó��
	}
	
	public synchronized void onBytesDelivered(BluetoothDevice device, byte[] bytes){
		RequestReplyManager.getInstance().handleMessage((Player)SubjectDeviceMapper.getInstance().map(device), bytes);
	}
	
	
	//called by DicePlusManager
	
	
	//Dice+
	
	/*public synchronized void onDicePlusEstablishComplete(Die[] dice){
		//checkInitialized();
		Log.i(TAG, "Dice+ ���� �Ϸ�");
		
		SubjectDeviceMapper.getInstance().registerDice(dice);
	}*/
	
	//Ÿ�Ӿƿ��ǰų� �� ����!!!
	
	
	public synchronized void onDicePlusEstablishComplete(Die[] dice){
		Log.i(TAG, "DICE+ ���� ����");
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_COMPELETE);
		message.obj = dice;
		message.sendToTarget();
	}
	
	public synchronized void onDicePlusEstablishForceComplete(){
		Log.i(TAG, "DICE+ ���� ���� ���� - �����ϼ� ��ư ������");
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_FORCE_COMPLETE);
		message.sendToTarget();
	}
	
	@Deprecated
	//onEstablishTimeOut�� ��ü(onEstablishTimeOut�Ҷ� �Ѳ�����)
 	public synchronized void onDicePlusEstablishFailed(){
		//checkInitialized();
		Log.w(TAG, "Dice+ ���� ����(Ÿ�Ӿƿ�)");//��, ���� Ÿ�Ӿƿ�.
		int state = DistributedBoardgame.getInstance().getState();
		if(state == DistributedBoardgame.HOST_PREPARE_MODE || state == DistributedBoardgame.CLIENT_JOIN_MODE){
			//Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.);
			//message.obj = die;
			//message.sendToTarget();
		}else{
			Log.w(TAG, "�� ������ ������ �����ϸ� �ȵ�");
		}
	}
	
	public synchronized void onDicePlusConnected(Die die){
		//checkInitialized();
		Log.i(TAG, "Dice+ ����� : " + die.getAddress());
		//�� ���� ���
		//1. ������
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
			Log.i(TAG, "DICE+ �ڵ鷯 ���� : ȣ��Ʈ �غ� ���");
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_CONNECTED);
			Log.d(TAG, "die " + die.toString());
			message.obj = die;
			message.sendToTarget();
		}
		//2. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			Log.i(TAG, "DICE+ �ڵ鷯 ���� : ȣ��Ʈ ������ ���");
		}else{
			Log.w(TAG, "DICE+ �ڵ鷯 ���� : ����������� ��");
		}
	}
	
	public synchronized void onDicePlusLost(Die die){
		//checkInitialized();
		Log.i(TAG, "Dice+ ������ : " + die.getAddress());
		//�� ���� ���
		//1. ������
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
		    SubjectDeviceMapper.getInstance().lostDicePlus();
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_DICEPLUS_DISCONNECTED);
			message.obj = die;
			message.sendToTarget();
		}
		//2. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			CommunicationStateManager.getInstance().onDicePlusLost(die);
			
			//�׸��� �ٽ� �籸���Ѵ�.
		}
	}
	
	public synchronized void onDicePlusWaitComplete(Die die){
		//checkInitialized();
	}
	
	public synchronized void onDicePlusWaitTimeOut(Die die){
		//checkInitialized();
	}
	
	public synchronized void onElectricYutEstablishComplete(BluetoothDevice[] yuts){
		Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTALBISH_YUT_COMPLETE);
		message.sendToTarget();
	}
	
	public synchronized void onElectricYutEstablishForceComplete(){
        Log.i(TAG, "������ ���� ���� ���� - �����ϼ� ��ư ������");
        Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_FORCE_COMPLETE);
        message.sendToTarget();
    }
	
	//onEstablishTimeOut�� ��ü(onEstablishTimeOut�Ҷ� �Ѳ�����)
	@Deprecated
	public synchronized void onElectricYutEstablishFailed(){
		Log.w(TAG, "���� �� ���� ����");//��, ���� Ÿ�Ӿƿ�.
	}
	
	public synchronized void onElectricYutConntected(BluetoothDevice device){
		//checkInitialized();
				Log.i(TAG, "���� �� ����� : " + device.getAddress());
				//�� ���� ���
				//1. ������
				if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
					Log.i(TAG, "DICE+ �ڵ鷯 ���� : ȣ��Ʈ �غ� ���");
					Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_CONNECTED);
					message.obj = device;
					message.sendToTarget();
				}
				//2. ������
				else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
					Log.i(TAG, "������ �ڵ鷯 ���� : ȣ��Ʈ ������ ���");
				}else{
					Log.w(TAG, "������ �ڵ鷯 ���� : ����������� ��");
				}
	}
	
	public synchronized void onElectricYutLost(BluetoothDevice device){
		Log.i(TAG, "���� �� ������ : " + device.getAddress());
		//�� ���� ���
		//1. ������
		if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.HOST_PREPARE_MODE){
		    SubjectDeviceMapper.getInstance().lostElectricYut();
			Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.ESTABLISH_YUT_DISCONNECTED);
			message.obj = device;
			message.sendToTarget();
		}
		//2. ������
		else if(DistributedBoardgame.getInstance().getState() == DistributedBoardgame.MIDDLE_OF_GAME){
			
		}
	}
}
