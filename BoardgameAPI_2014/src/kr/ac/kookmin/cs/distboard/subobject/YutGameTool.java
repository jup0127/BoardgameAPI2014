package kr.ac.kookmin.cs.distboard.subobject;

import kr.ac.kookmin.cs.distboard.GameTool;

public final class YutGameTool extends GameTool {
    
    private int face = 0;
    

    public void setFace(int face) {
        this.face = face;
    }

    public int getFace(){
        return face;
    }
    
    public int getRolledFace(){
        return 0;
    }
}
