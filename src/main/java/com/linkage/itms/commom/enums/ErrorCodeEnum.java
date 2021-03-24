package com.linkage.itms.commom.enums;

/**
 * 错误枚举类
 */
public enum ErrorCodeEnum {

    SUCCESS(0,"成功"),
    INVALID_PARAM(1,"数据格式错误"),
    SYSTEM_ERROR(1000,"该节点路径不存在，返回空"),
    INVALID_USERINFO_TYPE(1001,"用户信息类型非法"),
    INVALID_USERINFO(1002,"用户信息不合法"),
    USER_NO_DEVICE(1003,"用户未绑定设备"),
    USER_NOT_EXIST(1004,"此用户未绑定"),
    DEVICE_NOT_ONLINE(1012,"设备不在线"),
    DEVICE_IS_BUSY(1013,"设备正在被操作，不能正常交互"),
    MORE_DEVICE(1014,"查询出多个设备"),
    PATH_VALUE_NULL(1005,"路径获取值返回空");




    private int code;
    private String desc;

    ErrorCodeEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
