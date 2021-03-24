package com.linkage.itms.dispatch.gsdx.holders;

import com.linkage.itms.dispatch.gsdx.beanObj.Para;

public class getDiagnoseResultHolder implements javax.xml.rpc.holders.Holder {
    public Para[] value;
    

    public getDiagnoseResultHolder() {
    }

    public getDiagnoseResultHolder(Para[] value) {
        this.value = value;
    }
}
