package com.jvortex.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbUtils {
	private static SQLiteDatabase myDb;
	public static Boolean tableExists (SQLiteDatabase db,String tableName) {
		Boolean result=false;
		if (tableName==null) {
			return false;
		}
		Cursor cursor=null;
		try {
			String sql="select count(1) from Sqlite_master where type='table' and name='"+tableName+"'";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count>0) {
					result=true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}finally{
			if (null!=cursor&&!cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
	}
	
	public static SQLiteDatabase getDb(Context cxt) {
		if (myDb==null||!myDb.isOpen()) {
			myDb = (new MyDbOpenHelper(cxt)).getWritableDatabase();
			tableExistsCheck(myDb);
		}
		return myDb;
	}
	
	public static void tableExistsCheck(SQLiteDatabase db) {
		Boolean tableExists = tableExists(db, "gc_goods_pervalue");
		if(!tableExists){
			String createTableSql="create table gc_goods_pervalue (id integer primary key AutoIncrement,year_str integer,month_str integer,date_full_str integer,hour_str integer,minite_str integer,pervalue NUMERIC(10,2),goods_type_py varchar(50),val_memo varchar(2000))";
			db.execSQL(createTableSql);
		}
		tableExists=tableExists(db,"gc_goods_type");
		if(!tableExists){
			String createTableSql="create table gc_goods_type (id integer primary key AutoIncrement,is_del integer,last_use_time time,type_name varchar(50),type_py varchar(50))";
			db.execSQL(createTableSql);
		}
	}
	
	public static class MyDbOpenHelper extends SQLiteOpenHelper{
		private static final String DATABASE_NAME="goodsscalc.db";
		
		public MyDbOpenHelper(Context cxt){
			super(cxt, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			tableExistsCheck(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			
		}
		
	}
}
