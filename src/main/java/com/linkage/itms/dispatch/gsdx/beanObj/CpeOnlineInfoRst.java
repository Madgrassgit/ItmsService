package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;

public class CpeOnlineInfoRst  implements Serializable {

    private CpeInfo cpeInfo;

    private int iOpRst;


    public CpeInfo getCpeInfo() {
        return cpeInfo;
    }

    public void setCpeInfo(CpeInfo cpeInfo) {
        this.cpeInfo = cpeInfo;
    }

    public int getiOpRst() {
        return iOpRst;
    }

    public void setiOpRst(int iOpRst) {
        this.iOpRst = iOpRst;
    }

    @Override
    public String toString() {
        return "CpeOnlineInfoRst{" +
                "cpeInfo=" + cpeInfo +
                ", iOpRst=" + iOpRst +
                '}';
    }
}
