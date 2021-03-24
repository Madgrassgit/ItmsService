package com.linkage.itms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.linkage.itms.commom.util.DateTimeUtil;

public class DateTest {
	public static void main(String[] args) {
		DateTimeUtil d=new DateTimeUtil();
		System.out.println(d.getLastDayStart("2014-09-02"));
		System.out.println(d.getLastDayEnd("2014-10-01"));
		System.out.println(new DateTimeUtil(1565169441 * 1000L).getLongDate());
	//	System.out.println(d.getLastDayEnd("2015-03-18"));
		//System.out.println(d.getLastDayStart("2014-09-10"));
	//	System.out.println(d.getLastDayEnd("2014-09-09")+6*3600);
	//	System.out.println(d.getLastDayEnd("2014-09-09")+8*3600);
		//System.out.println(d.getLastDayEnd("2014-07-01"));
		//System.out.println(d.getLastDayEnd("2015-03-19"));
		//System.out.println(d.getLastDayStart("2014-09-10"));
		//System.out.println(d.getLongDate(d.getLastDayEnd(d.getFirtDayOfLast2Month("2014-09-09"))));
		//System.out.println(d.getLongDate());
		//System.out.println(d.getLongDate(1409677200));
		//System.out.println(d.getLongDate(1409720400));
		//System.out.println(d.getLongDate(1405673592));
		//System.out.println(d.getLongDate(1426577029));
	//BigDecimal  b =new  BigDecimal((Float.parseFloat("74675")-Float.parseFloat("74135"))/Float.parseFloat("74135")*100);  
		   //小数保留3位
	//System.out.println(b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue()+"%");
	 /* ArrayList<String> list=null;
	  for(String s:list){
		  System.out.println(s);
	  }*/
		
	}

}

	