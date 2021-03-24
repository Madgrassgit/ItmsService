package com.linkage.stbms.pic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * 自定义游标类，内部有Vector实现。
 * 
 * @author yuht
 * @version 1.00, 1/5/2006
 * @see DataSetBean
 * @since Liposs 2.1
 */
public class Cursor {
    private Vector rows = null;

    // 排序类型
    /**
     * 字符串类型数据
     */
    public final int STRING_TYPE = 0;

    /**
     * 长整形类型数据
     */
    public final int LONG_TYPE = 2;

    /**
     * 双精度类型数据
     */
    public final int DOUBLE_TYPE = 3;

    // 操作类型
    /**
     * 比较方式：大于
     */
    public final int OPER_MORE = 1;

    /**
     * 比较方式：小于
     */
    public final int OPER_LESS = 2;

    private int currentRow;

    /**
     * 缺省构造函数，初试化Vector
     * 
     */
    public Cursor() {
        rows = new Vector();
        currentRow = -1;
    }

    /**
     * 在游标中加入一条记录
     * 
     * @param map
     *            实现Map接口的记录对象
     */
    public void add(Map map) {
        rows.add(map);
    }

    /**
     * 从游标中取得下一条记录
     * 
     * @return 返回一个实现Map接口的对象,如果超出游标范围返回null
     */
    public Map getNext() {
        if (currentRow < rows.size() - 1) {
            currentRow++;
            return (Map) rows.elementAt(currentRow);
        } else
            return null;
    }

    /**
     * 从游标中取得上一条记录
     * 
     * @return 返回一个实现Map接口的对象,如果超出游标范围返回null
     */
    public Map getPrior() {
        if (currentRow > 0) {
            currentRow--;
            return (Map) rows.elementAt(currentRow);
        } else
            return null;
    }

    /**
     * 从游标中取得以索引号定义的记录
     * 
     * @param index
     *            记录索引号，从0开始。整形值
     * @return 返回一个实现Map接口的对象,如果超出游标范围返回null
     */
    public Map getRecord(int index) {
        if ((index >= 0) && (index < rows.size()))
            return (Map) rows.elementAt(index);
        else
            return null;
    }

    /**
     * 游标复位
     * 
     */
    public void Reset() {
        currentRow = -1;
    }

    /**
     * 获取游标大小
     * 
     * @return 返回整形值
     */
    public int getRecordSize() {
        return rows.size();
    }
    /**
     * 添加Cursor
     * @param cursor
     */
    public void addCursor(Cursor cursor){
        if(cursor != null && cursor.getRecordSize() > 0){
            this.rows.addAll(cursor.getVector());
        }
    }
    /**
     * 得到所有记录。
     * @see addCursor方法
     * @return Vector
     */
    protected Vector getVector(){
        return this.rows;
    }

    /**
     * 对字符字段<code>field</code>进行分组，存放Cursor数组中。
     * 在分组前必须对Cursor进行分组字段的排序，有两种排序选择：
     * <UL>
     * <LI>SQL语句中用 ORDER BY，即存放到Cursor数据已经是排过序的
     * <LI>调用Cursor相关排序方法：sort 来进行排序
     * </UL>
     * 
     * @param field
     *            分组字段名称
     * @return Cursor数组，每一组一个Cursor列表
     */
    public Cursor[] group(String field) {
        Reset();
        ArrayList list = new ArrayList();
        Map m;
        String v1 = null, v2;
        for (int i = 0; i < rows.size(); i++) {
            m = getRecord(i);
            v2 = (String) m.get(field);
            if (v1 == null || !v1.equals(v2)) {
                Cursor c = new Cursor();
                c.add(m);
                list.add(c);
            } else {
                ((Cursor)list.get(list.size()-1)).add(m);
            }
            v1 = v2;
        }
        if(list.size()>0){
            Cursor[] returnC = new Cursor[list.size()];
            list.toArray(returnC);
            
            return returnC;
        }
        return null;     
    }

    public Cursor groupBySum(String field1, String field2, String aliasName, int data) {
        Cursor[] arr = group(field1);
        Cursor c = new Cursor();
        HashMap m;
        for (int i = 0; i < arr.length; i++) {
            m = arr[i].sum(field2, aliasName, data);
            m.put(field1, (String) arr[i].getRecord(0).get(field1));
            c.add(m);
        }

        return c;
    }

    private boolean search(Vector vector, String field, String value) {
        Map h;
        String fv;
        for (int i = 0; i < vector.size(); i++) {
            h = getRecord(i);
            fv = (String) h.get(field);
            if (fv.equals(value))
                return true;
        }
        return false;
    }

    public void group(String field1, String field2) {
        Vector tmp = new Vector();
        Map h;
        long v;
        String fv1, fv2;
        for (int i = 0; i < rows.size(); i++) {
            h = getRecord(i);
            v = Long.parseLong((String) h.get(field2));
            fv1 = (String) h.get(field1);
            if (!search(tmp, field1, fv1)) {
                for (int j = i + 1; j < rows.size(); j++) {
                    h = getRecord(j);
                    fv2 = (String) h.get(field1);
                    if (fv1.equals(fv2)) {
                        v += Long.parseLong((String) h.get(field2));
                    }
                }
                Hashtable tmph = new Hashtable();
                tmph.put(field1, fv1);
                tmph.put(field2, new Long(v).toString());
                tmp.add(tmph);
            }
        }

        rows = tmp;
        tmp = null;
    }

    /**
     * 将Cursor按照field1字段分组并按照field2取平均值
     * @param field1 分组统计字段
     * @param field2 取平均值字段
     * @param aliasName 对得到的平均值定义的名称
     * @param data  取品均值字段数据类型 Cursor.LONG_TYPE long型 Cusor.DOUBLE_TYPE double型
     * @return c 分组字段field1及每组按照field2取平均之后以aliasName命名的平均值
     */
    public Cursor groupByAvg(String field1, String field2, String aliasName, int data) {
        Cursor[] arr = group(field1);
        Cursor c = new Cursor();
        HashMap m;
        for (int i = 0; i < arr.length; i++) {
            m = arr[i].avg(field2, aliasName, data);
            m.put(field1,arr[i].getRecord(0).get(field1));
            c.add(m);
        }

        return c;
    }

    public HashMap sum(String field, String aliasName, int data) {
        HashMap m = new HashMap();
        switch (data) {
        case LONG_TYPE:
            m.put(aliasName, Long.toString(longSum(field)));
            break;
        case DOUBLE_TYPE:
            m.put(aliasName, Double.toString(doubleSum(field)));
        }
        return m;
    }
    
    /**
     * 将Cursor中的所有指定的fields字段累加取合
     * @param fields
     * @param aliasName
     * @param data
     * @return
     */
    public HashMap sum(String[] fields, String aliasName, int data) {
        HashMap m = new HashMap();
        switch (data) {
        case LONG_TYPE:
            m.put(aliasName, Long.toString(longSum(fields)));
            break;
        case DOUBLE_TYPE:
            m.put(aliasName, Double.toString(doubleSum(fields)));
        }
        return m;
    }

    public HashMap avg(String field, String aliasName, int data) {
        HashMap m = new HashMap();
        switch (data) {
        case LONG_TYPE:
            m.put(aliasName, Long.toString(longSum(field) / rows.size()));
            break;
        case DOUBLE_TYPE:
            m.put(aliasName, Double.toString(doubleSum(field) / rows.size()));
        }
        return m;
    }

    private long longSum(String field) {
        Map m;
        long total = 0L;
        for (int i = 0; i < rows.size(); i++) {
            m = getRecord(i);
            total += Long.parseLong((String) m.get(field));
        }

        return total;
    }
    
    private long longSum(String[] fields) {
        Map m;
        long total = 0L;
        for (int i = 0; i < rows.size(); i++) {
            m = getRecord(i);
            for(int j=0;j<fields.length;j++) {
                total += Long.parseLong((String) m.get(fields[j]));
            }            
        }

        return total;
    }

    private double doubleSum(String field) {
        Map m;
        double total = 0.0D;
        for (int i = 0; i < rows.size(); i++) {
            m = getRecord(i);
            total += Double.parseDouble((String) m.get(field));
        }

        return total;
    }
    
    private double doubleSum(String[] fields) {
        Map m;
        double total = 0.0D;
        for (int i = 0; i < rows.size(); i++) {
            m = getRecord(i);
            for(int j=0;j<fields.length;j++) {
                total += Double.parseDouble((String) m.get(fields[j]));
            }
        }

        return total;
    }

    /**
     * 对长整形的字段进行DESC排序
     * 
     * @param field
     *            排序字段名称
     */
    public void sort(String field) {
        sort(field, "desc", LONG_TYPE);
    }

    /**
     * 对长整形的字段进行排序，排序方式由<code>seq</code>决定
     * 
     * @param field
     *            排序字段名称
     * @param seq
     *            排序方式
     */
    public void sort(String field, String seq) {
        sort(field, seq, LONG_TYPE);
    }

    /**
     * 对任意字段进行排序，排序字段类型由<code>data</code>决定,排序方式由<code>seq</code>决定
     * 
     * @param field
     *            排序字段名称
     * @param seq
     *            排序方式
     * @param data
     *            排序字段类型
     */
    public void sort(String field, String seq, int data) {
        Map m1, m2;
        String v1, v2;
        for (int i = 0; i < rows.size(); i++) {
            for (int j = i + 1; j < rows.size(); j++) {
                m1 = getRecord(i);
                v1 = (String) m1.get(field);
                m2 = getRecord(j);
                v2 = (String) m2.get(field);
                if (seq.equals("asc")) {
                    if (parseValue(v1, v2, data, OPER_MORE)) {
                        rows.set(i, m2);
                        rows.set(j, m1);
                    }
                } else if (seq.equals("desc")) {
                    if (parseValue(v1, v2, data, OPER_LESS)) {
                        rows.set(i, m2);
                        rows.set(j, m1);
                    }
                } else {
                    if (parseValue(v1, v2, data, OPER_LESS)) {
                        rows.set(i, m2);
                        rows.set(j, m1);
                    }
                }
            }
        }
    }

    private boolean parseValue(String v1, String v2, int data, int oper) {
        boolean re = false;
        switch (data) {
        case STRING_TYPE:
            if (oper == OPER_MORE)
                re = v1.compareTo(v2) > 0;
            else
                re = v1.compareTo(v2) < 0;
            break;
        case LONG_TYPE:
            if (oper == OPER_MORE)
                re = Long.parseLong(v1) > Long.parseLong(v2);
            else
                re = Long.parseLong(v1) < Long.parseLong(v2);
            break;
        case DOUBLE_TYPE:
            if (oper == OPER_MORE)
                re = Double.parseDouble(v1) > Double.parseDouble(v2);
            else
                re = Double.parseDouble(v1) < Double.parseDouble(v2);
            break;
        }

        return re;
    }
}
