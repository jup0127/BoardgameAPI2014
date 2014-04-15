package kr.ac.kookmin.cs.distboard;

import kr.ac.kookmin.cs.distboard.controller.GameToolAdapter;
import kr.ac.kookmin.cs.distboard.controller.GameToolListener;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;

public abstract class GameTool {
	
	private static GameToolListener gameToolListener = new GameToolAdapter();
	
	
	
	public static void registerGameToolListener(GameToolListener gameToolListener){
		GameTool.gameToolListener = gameToolListener;
	}
	
	public static void unRegisterGameToolListener(){
		GameTool.gameToolListener = new GameToolAdapter();
	}
	
	public static GameToolListener getGameToolListener(){
		return GameTool.gameToolListener;
	}

}
