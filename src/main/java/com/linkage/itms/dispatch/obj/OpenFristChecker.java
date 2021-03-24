package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;


public class OpenFristChecker extends BaseChecker {

    private static final Logger logger = LoggerFactory
            .getLogger(OpenFristChecker.class);

    /**
     * enable	是否启用SDN功能
     * controllerAddress	SDN控制器的主用地址
     * backupEnable	控制器主备功能启用开关
     * backupControllerAddress	SDN控制器的备用地址
     */

    private String enable;
    private String controllerAddress;
    private String backupEnable;
    private String backupControllerAddress;

    private Map<String,String> parameMap = new HashMap<String, String>(){{
        put("enable","InternetGatewayDevice.SDN.Enable");
        put("backupEnable","InternetGatewayDevice.SDN.BackupEnable");
        put("controllerAddress","InternetGatewayDevice.SDN.ControllerAddress");
        put("backupControllerAddress","InternetGatewayDevice.SDN.BackupControllerAddress");
    }};


    private ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();


    public OpenFristChecker(String inXml) {
        callXml = inXml;
    }

    public ArrayList<ParameValueOBJ> getObjList() {
        return objList;
    }

    public void setObjList(ArrayList<ParameValueOBJ> objList) {
        this.objList = objList;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getControllerAddress() {
        return controllerAddress;
    }

    public void setControllerAddress(String controllerAddress) {
        this.controllerAddress = controllerAddress;
    }

    public String getBackupEnable() {
        return backupEnable;
    }

    public void setBackupEnable(String backupEnable) {
        this.backupEnable = backupEnable;
    }

    public String getBackupControllerAddress() {
        return backupControllerAddress;
    }

    public void setBackupControllerAddress(String backupControllerAddress) {
        this.backupControllerAddress = backupControllerAddress;
    }

    @Override
	public boolean check() {

        logger.debug("OpenFristChecker==>check()");

        SAXReader reader = new SAXReader();
        Document document = null;
        boolean flag = true;
        try {


            document = reader.read(new StringReader(callXml));
            Element root = document.getRootElement();

            cmdId = root.elementTextTrim("CmdID");
            cmdType = root.elementTextTrim("CmdType");
            clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

            Element param = root.element("Param");
            userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
            userInfo = param.elementTextTrim("UserInfo");
            enable = param.elementTextTrim("enable");
            controllerAddress = param.elementTextTrim("controllerAddress");
            backupEnable = param.elementTextTrim("backupEnable");
            backupControllerAddress = param.elementTextTrim("backupControllerAddress");

            for(Map.Entry<String, String> entry : parameMap.entrySet()){
                String mapKey = entry.getKey();
                String name = entry.getValue();
                String value = param.elementTextTrim(mapKey);
                ParameValueOBJ obj = new ParameValueOBJ();

                if (null == value || "".equals(value)) {
                    result = 2002;
                    resultDesc = "节点"+name+"<Value>的值不能为空";
                    flag = false;
                    break;
                }
                obj.setName(name);
                obj.setValue(value);
                obj.setType("1");
                objList.add(obj);
            }





            if (!flag) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = 1;
            resultDesc = "入参格式错误";
            return false;
        }


        // 参数合法性检查
        if (false == baseCheck() || false == userInfoTypeCheck()
                || false == userInfoCheck()) {
            return false;
        }

        // userInfoType == 6 表示 userInfo 入的是设备序列号，如果是设备序列号，那么设备序列号至少为后6位
        if (6 == userInfoType) {
            if (userInfo.length() < 6) {
                result = 1007;
                resultDesc = "设备序列号长度不能小于6位";
                return false;
            }
        }

        result = 0;
        resultDesc = "节点值设置成功";

        return true;
    }


    /**
     * 回参
     */
    @Override
	public String getReturnXml() {
        logger.debug("getReturnXml()");
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");
        Element root = document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        // 结果代码
        root.addElement("RstCode").addText("" + result);
        // 结果描述
        root.addElement("RstMsg").addText("" + resultDesc);

        return document.asXML();
    }


    public static void main(String[] args) {
        StringBuffer inParam = new StringBuffer();
        inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
        inParam.append("<root>\n");
        inParam.append("	<CmdID>123456789012345</CmdID>       \n");
        inParam.append("	<CmdType>CX_01</CmdType>           \n");
        inParam.append("	<ClientType>3</ClientType>         \n");
        inParam.append("	<Param>                            \n");
        inParam.append("		<UserInfoType>2</UserInfoType>      \n");
        inParam.append("		<UserInfo>123456789</UserInfo>  \n");
        inParam.append("		<enable></enable> \n");
        inParam.append("		<controllerAddress>22</controllerAddress>  \n");
        inParam.append("		<backupEnable>3</backupEnable>  \n");
        inParam.append("		<backupControllerAddress>4</backupControllerAddress>  \n");
        inParam.append("	</Param>  \n");
        inParam.append("</root>  \n");

        OpenFristChecker checker = new OpenFristChecker(inParam.toString());
        if (false == checker.check()) {

            System.out.println(checker.getReturnXml());
        }
    }
}
