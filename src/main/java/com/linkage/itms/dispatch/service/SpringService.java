
package com.linkage.itms.dispatch.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.FtpClient;
import com.linkage.itms.dao.SpringServiceDao;
import com.linkage.itms.dispatch.obj.SpringServiceChecker;

public class SpringService implements IService
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(SpringService.class);
	/** 单次读取行数 */
	private static final int count = 200;

	/**
	 * 下载文件并解析同步
	 * 
	 * @param param
	 *            xml数据
	 */
	public String work(String param)
	{
		SpringServiceChecker checker = new SpringServiceChecker(param);
		// 参数检证
		if (false == checker.check())
		{
			logger.error("servicename[SpringService]sysName[{}]验证未通过，返回：{}",
					new Object[] { checker.getSysName() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[SpringService]sysName[{}]参数校验通过，入参为：{},{}",
				new Object[] { checker.getSysName(), checker.getFileName(),
						checker.getFilePath() });
		// 目标文件
		String downLoadFile = checker.getFilePath().replaceFirst("/", "")
				+ File.separator + checker.getFileName();
		// 下载到本地的文件
		String downLoadTargetFile = Global.localDir + checker.getFileName();
		logger.debug("downLoadTargetFile ==" + downLoadTargetFile);
		// ftp到服务器上取数据
		FtpClient ftpClient = new FtpClient(Global.ip, Global.username, Global.password);
		// 连接成功
		logger.warn("ccc");
		if (ftpClient.connect())
		{
			logger.warn("[{}]开始下载文件[{}], 目标位置[{}], 需要删除远程文件[{}]", new Object[] { 0,
					downLoadFile, downLoadTargetFile, "否" });
			// 开始下载文件
			ftpClient.get(downLoadFile, downLoadTargetFile, false);
			// 下载成功,释放连接
			ftpClient.disconnect();
		}
		else
		{
			logger.error("ftp连接失败");
			return checker.getReturnXml();
		}
		try
		{
			// excel解析入库
			this.excuteSheet(downLoadTargetFile, checker);
		}
		catch (FileNotFoundException e)
		{
			logger.error("文件没找到" + e);
			return checker.getReturnXml();
		}
		catch (IOException e)
		{
			logger.error("读写异常" + e);
			return checker.getReturnXml();
		}
		logger.warn("servicename[SpringService]filename[{}]filepath[{}]处理结束",
				new Object[] { checker.getFileName(), checker.getFilePath() });
		// 回单
		return checker.getReturnXml();
	}

	/**
	 * 解析入库操作
	 * @param downLoadTargetFile  文件名
	 * @param checker 检证参数
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void excuteSheet(String downLoadTargetFile, SpringServiceChecker checker)
			throws FileNotFoundException, IOException
	{
		logger.debug("开始解析 " + downLoadTargetFile);
		// 账号名
		String enName = "";
		// 帐号中文名
		String chName = "";
		// 属地
		String city_name = "";
		// 部门
		String dept_name = "";
		// 部门全称
		String dept_full_name = "";
		// 邮箱地址
		String email = "";
		// 移动电话
		String mobilephone = "";
		// 办公电话
		String telephone = "";
		// 人员类型
		String employee_type = ""; // 1:代维人员,2：电信人员
		// 部门id
		String dept_id = "";
		// 解析文件
		File target = new File(downLoadTargetFile);
		// 为了兼容2003与2007
		Workbook workbook = null;
		try
		{
			workbook = new XSSFWorkbook(new FileInputStream(target));
		}
		catch (Exception e)
		{
			workbook = new HSSFWorkbook(new FileInputStream(target));
		}
		if (null != target && target.exists())
		{
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			Map<String, String> tempMap = null;
			// 创建工作表
			// 获得sheet
			Sheet sheet = workbook.getSheetAt(0);
			// 获得sheet的总行数
			int rowCount = sheet.getLastRowNum();
			logger.debug("获得sheet的总行数:" + rowCount);
			Cell cellFirst = null;
			Cell cellSecond = null;
			Cell cellThird = null;
			Cell cellFourth = null;
			Cell cellFiveth = null;
			Cell cellSixth = null;
			Cell cellSeventh = null;
			Cell cellEghth = null;
			Cell cellNighth = null;
			Cell cellTen = null;
			Map<String, String> accountInfo = null;
			Map<String, String> excelAccountInfo = null;
			SpringServiceDao dao = new SpringServiceDao();
			// 获取属地id跟属地名 map
			Map<String,String> cityMap = dao.getCityMap();

			// 循环解析每一行，第一行不取
			for (int i = 1; i <= rowCount; i++)
			{
				tempMap = new HashMap<String, String>();
				// 获得行对象
				Row row = sheet.getRow(i);
				cellFirst = row.getCell(0);
				cellSecond = row.getCell(1);
				cellThird = row.getCell(2);
				cellFourth = row.getCell(3);
				cellFiveth = row.getCell(4);
				cellSixth = row.getCell(5);
				cellSeventh = row.getCell(6);
				cellEghth = row.getCell(7);
				cellNighth = row.getCell(8);
				cellTen = row.getCell(9);
				if (null != cellFirst
						&& !StringUtil.IsEmpty(StringUtil.getStringValue(cellFirst
								.getRichStringCellValue())))
				{
					enName = StringUtil
							.getStringValue(cellFirst.getRichStringCellValue());
					// 查询账户名在tab_accountsh和tab_excel_syn_accounts中是否存在,存在,则下一条,反之继续下面步骤
					accountInfo = dao.queryAccount(enName);
					excelAccountInfo = dao.queryExcelAccount(enName);
					if ((accountInfo != null && !accountInfo.isEmpty())
							|| (excelAccountInfo != null && !excelAccountInfo.isEmpty()))
					{
						logger.debug("数据已存在, 智能网管账号:{}", new Object[] { enName });
						continue;
					}
					tempMap.put("enName", enName);
				}
				else
				{
					logger.debug("第{}行数据的用户名为空,请确认是否数据不整合", new Object[] { i+1 });
					continue;
				}
				if (null != cellSecond)
				{
					chName = StringUtil.getStringValue(cellSecond
							.getRichStringCellValue());
					tempMap.put("chName", chName);
				}
				if (null != cellThird)
				{
					city_name = StringUtil.getStringValue(
							cellThird.getRichStringCellValue()).trim();
					if (StringUtil.IsEmpty(city_name))
					{
						logger.debug("第{}行数据的用户的属地为空,请确认是否数据不整合", new Object[] { i+1 });
						continue;
					}
					if("省中心".equals(city_name)){
						city_name = "江苏省中心";
					}
					String city_id = cityMap.get(city_name);
					if(StringUtil.IsEmpty(city_id)){
						logger.debug("第{}行数据的用户的属地不在江苏", new Object[] { i+1 });
						continue;
					}
					tempMap.put("city_id", city_id);
				}
				if (null != cellFourth)
				{
					dept_name = StringUtil.getStringValue(cellFourth
							.getRichStringCellValue());
					tempMap.put("dept_name", dept_name);
				}
				if (null != cellFiveth)
				{
					dept_full_name = StringUtil.getStringValue(cellFiveth
							.getRichStringCellValue());
					tempMap.put("dept_full_name", dept_full_name);
				}
				if (null != cellSixth)
				{
					email = StringUtil.getStringValue(cellSixth.getRichStringCellValue());
					tempMap.put("email", email);
				}
				if (null != cellSeventh)
				{
					mobilephone = StringUtil.getStringValue(cellSeventh
							.getRichStringCellValue());
					tempMap.put("mobilephone", mobilephone);
					
				}
				if (null != cellEghth)
				{
					telephone = StringUtil.getStringValue(cellEghth
							.getRichStringCellValue());
					tempMap.put("telephone", telephone);
				}
				if (null != cellNighth)
				{
					employee_type = StringUtil.getStringValue(cellNighth
							.getRichStringCellValue());
					tempMap.put("employee_type", employee_type);
				}
				if (null != cellTen)
				{
					dept_id = StringUtil.getStringValue(cellTen.getRichStringCellValue());
					tempMap.put("dept_id", dept_id);
				}
				list.add(tempMap);
				// 每 count 行执行一次
				if (i % count == 0)
				{
					dao.insertAccountInfo(list, checker.getFileName(),
							new DateTimeUtil().getLongTime());
					// 清空list内存
					list.clear();
				}
				
			}
			// 不足 count 行 或最后不足 count 行  执行入库
			dao.insertAccountInfo(list, checker.getFileName(),
					new DateTimeUtil().getLongTime());
		}
	}
}
