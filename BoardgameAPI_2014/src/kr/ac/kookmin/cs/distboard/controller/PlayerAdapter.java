package kr.ac.kookmin.cs.distboard.controller;

import kr.ac.kookmin.cs.distboard.Player;
import kr.ac.kookmin.cs.distboard.subobject.Distracter;

public class PlayerAdapter implements PlayerListener{

	@Override
	public void onReceiveObject(Player srcPlayer, Object object) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onReceiveSituation(Player srcPlayer, Object situation,
            int arg1, int arg2) {
        // TODO Auto-generated method stub
    }
}
