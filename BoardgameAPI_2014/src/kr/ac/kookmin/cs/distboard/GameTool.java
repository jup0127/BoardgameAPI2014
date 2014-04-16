package kr.ac.kookmin.cs.distboard;

import kr.ac.kookmin.cs.distboard.controller.GameToolAdapter;
import kr.ac.kookmin.cs.distboard.controller.GameToolListener;
import kr.ac.kookmin.cs.distboard.system.SubjectDeviceMapper;

public abstract class GameTool {
	
    /**
     * 게임 도구 리스너
     */
	private static GameToolListener gameToolListener = new GameToolAdapter();
	

	/**
	 * 개발자가 호출하지 않습니다.
	 * @param gameToolListener
	 */
	public static void registerGameToolListener(GameToolListener gameToolListener){
		GameTool.gameToolListener = gameToolListener;
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 */
	public static void unRegisterGameToolListener(){
		GameTool.gameToolListener = new GameToolAdapter();
	}
	
	/**
	 * 개발자가 호출하지 않습니다.
	 * @return
	 */
	public static GameToolListener getGameToolListener(){
		return GameTool.gameToolListener;
	}

}
