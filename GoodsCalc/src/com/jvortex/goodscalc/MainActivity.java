package com.jvortex.goodscalc;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jvortex.common.MyDbUtils;
import com.jvortex.common.SpinnerOption;

public class MainActivity extends Activity {
	TextView currDayText;
	TextView currMonthText;
	Spinner goodsTypeSpinner;
	List<SpinnerOption> goodsTypeOptions;
	Date startDate=null;
	Date endDate=null;
	
	private void calcTimeCycle(Date currentDate) {
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(currentDate);
		Integer currentYear=calendar.get(Calendar.YEAR);
		Integer currentMonth=calendar.get(Calendar.MONTH);
		Integer currentDay=calendar.get(Calendar.DAY_OF_MONTH);
		if(currentDay>=21){
			calendar.set(currentYear, currentMonth, 21);
			startDate=calendar.getTime();
			Calendar endCalendar=Calendar.getInstance() ;
			endCalendar.setTime(startDate);
			endCalendar.add(Calendar.MONTH, 1);
			endCalendar.add(Calendar.DAY_OF_MONTH, -1);
			endDate=endCalendar.getTime();
		}else{
			calendar.set(currentYear, currentMonth, 20);
			endDate=calendar.getTime();
			Calendar startCalendar=Calendar.getInstance() ;
			startCalendar.setTime(endDate);
			startCalendar.add(Calendar.MONTH, -1);
			startCalendar.add(Calendar.DAY_OF_MONTH, 1);
			startDate=startCalendar.getTime();
		}
	}
	
	
	
	private void flashCycleTime(Date currentDate) {
		calcTimeCycle(currentDate);
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMdd");
		String thisMonthCycle=simpleDateFormat.format(startDate)+"-"+simpleDateFormat.format(endDate);
		currMonthText = (TextView)findViewById(R.id.currMonthText);
		currMonthText.setText(thisMonthCycle);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		goodsTypeSpinner=(Spinner)(findViewById(R.id.goodsTypeSpinner));
		CalendarView calendarView = (CalendarView)findViewById(R.id.calendarView);
		calendarView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				Long date = ((CalendarView)arg0).getDate();
				Toast.makeText(MainActivity.this,date.toString(), 5000).show();
				return false;
			}
		});
		//填充标题
		long date = calendarView.getDate();
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
		String yearMonthStr = simpleDateFormat.format(date);
		simpleDateFormat.applyPattern("yyyyMMdd");
		String dateFullStr = simpleDateFormat.format(date);
		try {
			Date currentDate=simpleDateFormat.parse(dateFullStr);
			flashCycleTime(currentDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		currDayText = (TextView)findViewById(R.id.currDayText);
		currDayText.setText(dateFullStr);
		flashTotalVal(dateFullStr);
		
		
		
		////日历控件事件
		calendarView.setOnDateChangeListener(new OnDateChangeListener() {
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month,
					int dayOfMonth) {
				Calendar calendar=Calendar.getInstance();
				calendar.set(year, month, dayOfMonth);
				Date currentDate=calendar.getTime();
				String yearStr=String.valueOf(year);
				String monthStr=StringUtils.leftPad(String.valueOf(month+1), 2,'0');
				String dayStr=StringUtils.leftPad(String.valueOf(dayOfMonth), 2,'0');
				flashCycleTime(currentDate);
				String dayFullStr = yearStr+monthStr+dayStr;
				currDayText.setText(dayFullStr);
				flashTotalVal(dayFullStr);
			}

			
		});
		
		//添加按钮事件
		ImageView addImage = (ImageView)findViewById(R.id.list_note_add);
		addImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				SpinnerOption spinnerOption = (SpinnerOption)(goodsTypeSpinner.getSelectedItem());
				if(spinnerOption==null){
					Toast.makeText(MainActivity.this, "请选择货物类型", 5000).show();
					return;
				}
				String typePy = spinnerOption.getValue();
				String typeName=spinnerOption.getText();
				TextView currDayText = (TextView)findViewById(R.id.currDayText);
				Intent intent=new Intent(MainActivity.this, AddNoteActivity.class);
				Bundle bundle=new Bundle();
				bundle.putString("dateStr",currDayText.getText().toString());
				bundle.putString("goodsType", typePy);
				bundle.putString("typeName", typeName);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);
			}
		});
		
		
		
		
		flashGoodsType();
		
		
		
		goodsTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//更新最近使用时间
				String value = ((SpinnerOption)goodsTypeOptions.get(arg2)).getValue();
				String updateSql="update gc_goods_type set last_use_time=? where type_py=?";
				SQLiteDatabase db2 = MyDbUtils.getDb(MainActivity.this);
				db2.execSQL(updateSql,new Object[]{new Date(),value});
				flashTotalVal(null);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}



	/**
	 * 刷新货物类型
	 */
	private void flashGoodsType() {
		SQLiteDatabase db = MyDbUtils.getDb(MainActivity.this);
		String querySql="select gt.id _id,gt.type_name,gt.type_py from gc_goods_type gt order by gt.last_use_time desc";
		Cursor goodsType = db.rawQuery(querySql, null);
		goodsTypeOptions=new ArrayList<SpinnerOption>();
		String oldTypePy=getDefaultGoodsType();
		int selectedTypePosition=0;
		while(goodsType.moveToNext()){
			String typePy = goodsType.getString(goodsType.getColumnIndex("type_py"));
			String typeName = goodsType.getString(goodsType.getColumnIndex("type_name"));
			SpinnerOption spinnerOption=new SpinnerOption(typePy, typeName);
			goodsTypeOptions.add(spinnerOption);
			if (typePy.equals(oldTypePy)) {
				selectedTypePosition=goodsType.getPosition();
			}
		}
		ArrayAdapter<SpinnerOption> arrayAdapter=new ArrayAdapter<SpinnerOption>(this,android.R.layout.simple_spinner_item,goodsTypeOptions);
		goodsTypeSpinner.setAdapter(arrayAdapter);
		goodsTypeSpinner.setSelection(selectedTypePosition);
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==1) {
			flashTotalVal(null);//添加记录页面返回功能处理方法 
		}
		if (requestCode==2) {
			flashGoodsType();//刷新货物类型
			flashTotalVal(null);//类型设置页面返回处理方法 
		}
		
	}



	private void flashTotalVal(String dayFullStr) {
		SQLiteDatabase sqLiteDatabase = MyDbUtils.getDb(getApplicationContext());
		if(dayFullStr==null){
			dayFullStr=currDayText.getText().toString();
		}
		String goodsTypePy = getDefaultGoodsType();
		
		//求当月总车数总量
		Cursor rawQuery = sqLiteDatabase.rawQuery("select COUNT(1) CNT,round(SUM(GP.pervalue),2) SUM_VAL from gc_goods_pervalue gp WHERE GP.date_full_str=? and GP.goods_type_py=?", new String[]{dayFullStr,goodsTypePy});
		if(rawQuery.moveToNext()){
			int count = rawQuery.getInt(0);
			float sumValue = rawQuery.getFloat(1);
			TextView dayInfoView= (TextView)findViewById(R.id.dayInfoShow);
			dayInfoView.setText("共"+count+"车"+sumValue+"吨");
			rawQuery.close();
		}
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
		String startTime = simpleDateFormat.format(startDate);
		String endTime = simpleDateFormat.format(endDate);
		rawQuery = sqLiteDatabase.rawQuery("select COUNT(1) CNT,round(SUM(GP.pervalue),2) SUM_VAL from gc_goods_pervalue gp WHERE GP.date_full_str>=? AND GP.date_full_str<? AND GP.goods_type_py=?", new String[]{startTime,endTime,goodsTypePy});
		if(rawQuery.moveToNext()){
			int count = rawQuery.getInt(0);
			Float sumValue = rawQuery.getFloat(1);
			TextView monthInfoView= (TextView)findViewById(R.id.monthInfoShow);
			monthInfoView.setText("共"+count+"车"+sumValue+"吨");
			TextView  average=(TextView)findViewById(R.id.averageValue);
			if (count>0) {
				float f = sumValue/count;
				BigDecimal bigDecimal=new BigDecimal(f);
				BigDecimal tmp=bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP);
				average.setText(currMonthText.getText()+"平均每车"+tmp.doubleValue()+"吨");
			}else{
				average.setText("");
			}
		}
		rawQuery.close();
	}



	private String getDefaultGoodsType() {
		String tmpTypePy="shizi";
		Object selectedItem = goodsTypeSpinner.getSelectedItem();
		if (selectedItem==null) {//系统刚启动时，找不到相关的选择信息，可以从数据库中查找最近使用类型
			String getLastGoodsType="select * from gc_goods_type gt order by gt.last_use_time desc limit 0,1";
			Cursor lastGoodsTypeCursor =MyDbUtils.getDb(MainActivity.this).rawQuery(getLastGoodsType, null);
			if(lastGoodsTypeCursor.moveToNext()){
				tmpTypePy=lastGoodsTypeCursor.getString(lastGoodsTypeCursor.getColumnIndex("type_py"));
			}
			lastGoodsTypeCursor.close();
		}else{
			tmpTypePy = ((SpinnerOption)selectedItem).getValue();
		}
		return tmpTypePy;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.goodsTypeSet:
			Intent intent=new Intent(MainActivity.this,GoodsTypeSet.class);
			startActivityForResult(intent,2);
			break;

		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	

}
