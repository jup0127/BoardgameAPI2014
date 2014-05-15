package kr.ac.kookmin.cs.distboard.protocol;

import java.io.Serializable;

public class ObjectArgument implements Serializable{
    
    public Object obj = null;
    public int arg1 = -9999;
    public int arg2 = -9999;
    
    public ObjectArgument(Object obj, int arg1, int arg2) {
        super();
        this.obj = obj;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

}
