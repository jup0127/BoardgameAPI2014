package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Player;

public class ParticipantAdapter implements ParticipantListener{

    /**
     * 플레이어와의 연결이 끊어졌을 때 호출됩니다.
     * 호스트 안드로이드와 클라이언트 안드로이드 모두 이 메서드를 호출합니다.
     */
	@Override
	public void onPlayerLeave(Player player) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * 게임도구와의 연결이 끊어졌을 때 호출됩니다.
	 */
	@Override
	public void onGameToolLeave(GameTool gameTool) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepareFail() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWaitBeBoring(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRetreiveFail(GameTool gameTool) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerRejoin(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameToolRejoin(GameTool gameTool) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onNotAvailablePlayer(Player player) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onNotAvaliableGameTool(GameTool gameTool) {
        // TODO Auto-generated method stub
        
    }

}
