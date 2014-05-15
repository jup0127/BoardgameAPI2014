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
            Log.i(TAG, "사용할 수 없는 플레이어 : " + ((Player)notAvailableObject).getName());
            Mediator.getParticipantListener().onNotAvailablePlayer((Player)notAvailableObject);
        }else if(notAvailableObject instanceof GameTool){
            Log.i(TAG, "사용할 수 없는 게임 도구 : " + ((GameTool)notAvailableObject));//이름으로 바꿔줄것
            Mediator.getParticipantListener().onNotAvaliableGameTool((GameTool)notAvailableObject);
        }
    }
}
