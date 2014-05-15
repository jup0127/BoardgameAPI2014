package kr.ac.kookmin.cs.distboard.util;

import android.util.Log;
import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Mediator;
import kr.ac.kookmin.cs.distboard.Player;

public class AsynchronousNotAvailableThread extends Thread{
    
    private static final String TAG = "20083271:AsynchronousNotAvailableThread";
    private Object notAvailableObject = null;

    public AsynchronousNotAvailableThread(Object notAvailableObject) {
        super();
        this.notAvailableObject = notAvailableObject;
    }
    
    public void run(){
        if(notAvailableObject instanceof Player){
            Log.i(TAG, "����� �� ���� �÷��̾� : " + ((Player)notAvailableObject).getName());
            Mediator.getParticipantListener().onNotAvailablePlayer((Player)notAvailableObject);
        }else if(notAvailableObject instanceof GameTool){
            Log.i(TAG, "����� �� ���� ���� ���� : " + ((GameTool)notAvailableObject));//�̸����� �ٲ��ٰ�
            Mediator.getParticipantListener().onNotAvaliableGameTool((GameTool)notAvailableObject);
        }
    }
}
