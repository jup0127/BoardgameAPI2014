package kr.ac.kookmin.cs.distboard.system;

import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.activity.AssistanceActivity;
import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import kr.ac.kookmin.cs.distboard.subobject.DicePlusGameTool;
import kr.ac.kookmin.cs.distboard.subobject.YutGameTool;
import us.dicepl.android.sdk.Die;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SubjectDeviceMapper {
	
	private static final String TAG = "20083271:SubjectDeviceMapper";
	private static SubjectDeviceMapper instance = new SubjectDeviceMapper();
	
	private boolean isAvaliable = false;//��� ������ �Ϸ�Ǿ�����?(��� ��ġ�� ��ϵǾ�����)
	private boolean initialized = false;//�ʱ�ȭ�Ǿ�����(�������ؼ�)
	
	
	private int minPlayers;
	private int maxPlayers;
	
	private int exactPlayers;
	private int exactYutGameTools;
	private int exactDicePluses;
	
	private Player[] players = null;
	private BluetoothDevice[] clientDevices = null;
	
	private YutGameTool[] yutGameTools = null;
	private BluetoothDevice[] yutDevices = null;
	
	private DicePlusGameTool[] dicePlusGameTools = null;
	private Die[] dice = null;
	
	//constructor
	private SubjectDeviceMapper(){
		
	}
	
	//���⼭ �ڵ鷯�� ���� �Ϸ� �����!
	public void initialize(int minPlayers, int maxPlayers, int exactDicePluses, int exactYutGameTools){//�̱��� ���� ���� 1ȸ �ݵ�� �ʱ�ȭ�� ��
		Log.i(TAG, "���� �ʱ�ȭ");
		isAvaliable = false;//�̱��� ���ɿ�
		initialized = true;//�ʱ�ȭ��.
		
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		
		this.exactPlayers = maxPlayers; 
		this.exactDicePluses = exactDicePluses;
		this.exactYutGameTools = exactYutGameTools;
		
		this.players = null;//�̱��� ���ɿ�
		this.clientDevices = null;//�̱��� ���ɿ�
		
		this.yutGameTools = null;//�̱��� ���ɿ�
		this.yutDevices = null;//�̱��� ���ɿ�
		
		this.dicePlusGameTools = null;//�̱��� ���ɿ�
		this.dice = null;//�̱��� ���ɿ�
	}
	
	//static method
	
	public static SubjectDeviceMapper getInstance(){
		return instance;
	}
	
	//method
	
	public synchronized boolean isAvailable(){
		
		
		if(	players == null || clientDevices == null || yutGameTools == null ||
				yutDevices == null || dicePlusGameTools == null || dice == null){
			isAvaliable = false;
			return false;
				
		}else if(players.length == clientDevices.length && yutGameTools.length == yutDevices.length && dicePlusGameTools.length == dice.length){
			isAvaliable = true;
			Log.i(TAG, "��� ����");
		}else{
			Log.e(TAG, "��� �Ұ���");
		}
				
		
		
		return isAvaliable;
	}
	
	//called by bluetoothManager Or DicePlusManager
	
	public synchronized void registerClientDevices(BluetoothDevice[] clientDevices){
		Log.i(TAG, "Ŭ���̾�Ʈ �迭 ���");
		if(initialized == false){
			Log.e(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
			return;
		}
		
		if(clientDevices == null){
			Log.e(TAG, "Ŭ���̾�Ʈ ��ġ ����� null�� �� �� �����ϴ�.");
			return;
		}
		
		if(clientDevices.length >= minPlayers && clientDevices.length <= maxPlayers  || Mediator.getInstance().getMode() == Mode.CLIENT){
			this.clientDevices = clientDevices;
			
			this.players = new Player[clientDevices.length];
			
			for(int i = 0 ; i < clientDevices.length ; i++){
				this.players[i] = new Player();
			}
			
			//�� ������κ��� ���۰� ��밡���ϸ� �������� ������ �ݿ�
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "���� �����ʴ� ���� ���� : Ŭ���̾�Ʈ���");
		}
	}
	
	public synchronized void registerYutDevices(BluetoothDevice[] yutDevices){
		
		Log.i(TAG, "�� �迭 ���");
		
		if(initialized == false)
			Log.e(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
		
		if(yutDevices == null)
			Log.e(TAG, "�� ��ġ����� null�� �� �� �����ϴ�.");
		
		if(yutDevices.length == exactYutGameTools || Mediator.getInstance().getMode() == Mode.CLIENT){
			this.yutDevices = yutDevices;
		
			this.yutGameTools = new YutGameTool[yutDevices.length];
		
			for(int i = 0 ; i < yutDevices.length ; i++){
				this.yutGameTools[i] = new YutGameTool();
			}
			
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "���� ���� �ʴ� ���� ���� : yut");
		}
	}
	
	public synchronized void registerDice(Die[] dice){
		Log.i(TAG, "�ֻ��� �迭 ���");
		if(initialized == false)
			Log.e(TAG, "�ʱ�ȭ���� �ʾҽ��ϴ�.");
		
		if(dice == null)
			Log.e(TAG, "�ֻ��� �迭�� null�� �� �� �����ϴ�.");
		
		if(dice.length == exactDicePluses || Mediator.getInstance().getMode() == Mode.CLIENT){//�߿��� ��ũ��
			this.dice = dice;
			this.dicePlusGameTools = new DicePlusGameTool[dice.length];
			
			for(int i = 0 ; i < dice.length ; i++){
				this.dicePlusGameTools[i] = new DicePlusGameTool();
			}
			
			checkIsAvaiableAndCommit();
			
		}else{
			Log.e(TAG, "���� �����ʴ� ���� ���� : dice+");
		}
	}

	//��ϵ� ������ ��� ��ϵǾ����� Ȯ�� �� �Ϸ� ����
	private void checkIsAvaiableAndCommit(){
	    Log.i(TAG, "checkIsAvaiableAndCommit() ����");
		if(isAvailable() == true){
		    Log.i(TAG, "checkIsAvaiableAndCommit() : ���۰�����!");
			Log.i(TAG, "���� ��� �ݿ� - ���� ���ᱸ�� �Ϸ�");
			Log.i(TAG, "�� Ŭ���̾�Ʈ(ȣ��Ʈ ��) :" + players.length);
			Log.i(TAG, "�� �� �� :" + yutGameTools.length);
			Log.i(TAG, "�� �ֻ��� �� :" + dicePlusGameTools.length);
			
			
			//�Ϸ� ���� // ��� �����ڰ� �ϴ°� �´°Ű���.. ���߿� �ٲ���, ���� �Ŵ����� �ڵ鷯��..
			if(Mediator.getInstance().getMode() == Mode.HOST){
			    Log.i(TAG, "GAME_IS_STARTABLE �ڵ鷯 �޽��� ���� ���� ���� : " + Mediator.getInstance().getMode());
			    Message message = Mediator.getInstance().getHandler().obtainMessage(AssistanceActivity.GAME_IS_STARTABLE);
			    message.sendToTarget();
			}
		}
	}
	
	//�����߿� ���۰� ������� ���ϵ���
	public synchronized void lostPlayer(){
	    clientDevices = null;
	}
	
	public synchronized void lostDicePlus(){
	    yutDevices = null;
	}
	
	public synchronized void lostElectricYut(){
	    dice = null;
	}
	
	//������ �ν��Ͻ� �ٲ���� ��
	public void replaceClientDevice(BluetoothDevice replaceDevice){//��ü�� ����̽�
	    for(int i = 0 ; i < clientDevices.length ; i++){
	        if(clientDevices[i].getAddress().equals(replaceDevice.getAddress())){
	            Log.i(TAG, clientDevices[i].hashCode() + "�� " + replaceDevice.hashCode() + "�� ��ü��");
	            clientDevices[i] = replaceDevice;
	            
	            return;
	        }
	    }
	}
	
	public void replaceDie(Die replaceDie){
	    for(int i = 0 ; i < dice.length ; i++){
            if(dice[i].getAddress().equals(replaceDie.getAddress())){
                Log.i(TAG, dice[i].hashCode() + "�� " + replaceDie.hashCode() + "�� ��ü��");
                dice[i] = replaceDie;
                return;
            }
        }
	}
	
	public void replaceYutDevice(BluetoothDevice replaceYutDevice){
        for(int i = 0 ; i < yutDevices.length ; i++){
            if(yutDevices[i].getAddress().equals(replaceYutDevice.getAddress())){
                Log.i(TAG, yutDevices[i].hashCode() + "�� " + replaceYutDevice.hashCode() + "�� ��ü��");
                yutDevices[i] = replaceYutDevice;
                return;
            }
        }
    }
	
	//called by mediator
	
	//�÷��̾ ����, ��밡������ ������ null
	public Player[] getPlayers(){
		if(isAvailable()){
			return this.players;
		}
		Log.e(TAG, "��밡������ ����");
		return null;
	}
	
	//�������� ����, ��밡������ ������ null
	public YutGameTool[] getYutGameTools(){
		if(isAvailable()){
				return this.yutGameTools;
		}
		Log.e(TAG, "��밡������ ����");
		return null;
	}
	
	//�������� ����, ��밡������ ������ null
	public DicePlusGameTool[] getDicePlusGameTools(){
		if(isAvailable()){
					return this.dicePlusGameTools;
		}
		Log.e(TAG, "��밡������ ����");
		return null;
	}
	
	//mapping method - device to subject
	//���ν����ϰų� ���ε� ��ü�� null �̸� null ����
	
	public Object map(BluetoothDevice device){
		if(isAvailable()){
			//���� Ŭ���̾�Ʈ �Ⱦ��
			for(int i = 0 ; i < clientDevices.length ; i++){
				if(device.equals(clientDevices[i]) == true){
					return players[i];//�÷��̾� ����
				}
			}
			//�� �Ⱦ
			for(int i = 0 ; i < yutDevices.length ; i++){
				if(device.equals(yutDevices[i]) == true){
					return yutGameTools[i];//�� ����
				}
			}
		}
		return null;
	}
	
	public DicePlusGameTool map(Die die){
		if(isAvailable()){
			
			//�ֻ��� ��ġ�� �Ⱦ��
			for(int i = 0 ; i < dice.length ; i++){
				if(die.equals(dice[i]) == true){
					return dicePlusGameTools[i];//�÷��̾� ����
				}
			}
		}else{
			Log.e(TAG, "��밡������ ����");
		}
		return null;
	}
	
	//mapping method - subject to device
	//���ν����ϰų� ���ε� ��ü�� null �̸� null ����
	
	public BluetoothDevice map(Player player){
		if(isAvailable()){
			//�÷��̾� �Ⱦ��
			for(int i = 0 ; i < players.length ; i++){
				if(player.equals(players[i]) == true){
					return clientDevices[i];//�÷��̾� ����
				}
			}
			
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}
	
	public BluetoothDevice map(YutGameTool yutGameTool){
		if(isAvailable()){
			//���� Ŭ���̾�Ʈ �Ⱦ��
			for(int i = 0 ; i < yutGameTools.length ; i++){
				if(yutGameTool.equals(yutGameTools[i]) == true){
					return yutDevices[i];//�÷��̾� ����
				}
			}
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}

	public Die map(DicePlusGameTool dicePlusGameTool){
		if(isAvailable()){
			//���� Ŭ���̾�Ʈ �Ⱦ��
			for(int i = 0 ; i < dicePlusGameTools.length ; i++){
				if(dicePlusGameTool.equals(dicePlusGameTools[i]) == true){
					return dice[i];//�÷��̾� ����
				}
			}
		}else{
			Log.e(TAG, "Mapper is not available.");
		}
		return null;
	}
	
	
	//���� ������ �� ����, ���� �����ϱ�����.
	public void clear(){
		
	}

	
	
	//setter Mapper
	
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public void setExactPlayers(int exactPlayers){
	    this.exactPlayers = exactPlayers;
	}

	public void setExactYutGameTools(int exacetYutGameTools) {
		this.exactYutGameTools = exacetYutGameTools;
	}

	public void setExactDicePluses(int exacetDicePluses) {
		this.exactDicePluses = exacetDicePluses;
	}
	
}
