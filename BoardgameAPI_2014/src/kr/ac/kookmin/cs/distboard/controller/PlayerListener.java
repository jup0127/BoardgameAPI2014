package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.subobject.Distracter;

public interface PlayerListener {
	// specified to players

	public void onReceiveObject(Player srcPlayer, Object obj);
	
	public void onReceiveSituation(Player srcPlayer, Object situation, int arg1, int arg2);

}
