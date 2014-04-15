package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.enumeration.Mode;
import android.util.Log;

public class DistributedBoardgameAdapter implements DistributedBoardgameListener{
	
	private static final String TAG = "DistributedBoardgameAdapter";
	
	@Override
	public void onGameStartable(Mode mode) {
		Log.w(TAG, "onGameStartable �����ʰ� ��ϵ��� �ʾҳ׿�.");
	}

	@Override
	public void onGameResumable() {
		Log.w(TAG, "onGameResumable �����ʰ� ��ϵ��� �ʾҳ׿�.");
	}
}
