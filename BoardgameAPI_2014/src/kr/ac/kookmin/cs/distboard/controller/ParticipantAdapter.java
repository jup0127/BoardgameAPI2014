package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.GameTool;
import kr.ac.kookmin.cs.distboard.Player;

public class ParticipantAdapter implements ParticipantListener{

    /**
     * �÷��̾���� ������ �������� �� ȣ��˴ϴ�.
     * ȣ��Ʈ �ȵ���̵�� Ŭ���̾�Ʈ �ȵ���̵� ��� �� �޼��带 ȣ���մϴ�.
     */
	@Override
	public void onPlayerLeave(Player player) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * ���ӵ������� ������ �������� �� ȣ��˴ϴ�.
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
