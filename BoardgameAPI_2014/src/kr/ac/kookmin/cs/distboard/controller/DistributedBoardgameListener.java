package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.enumeration.Mode;

public interface DistributedBoardgameListener {
	
	//ȣ��Ʈ �Ǵ� Ŭ���̾�Ʈ
	public abstract void onGameStartable(Mode mode);
	
	//Client
	public abstract void onGameResumable();
}
