package com.jvortex.goodscalc;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jvortex.common.MyDbUtils;

public class AddNoteActivity extends Activity {
	Context ctx=null;
	private String dateStr=null;
	private String yearStr=null;
	private String monthStr=null;
	private String goodsType=null;
	private String typeName=null;
	TextView titleView;
	ListView  goodsValListView;
	Cursor currCursor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_note);
		Intent intent=getIntent();
		Bundle extras = intent.getExtras();
		dateStr=extras.getString("dateStr");
		goodsType=extras.getString("goodsType");
		typeName=extras.getString("typeName");
		titleView = (TextView)findViewById(R.id.title);
		titleView.setText(dateStr);
		TextView goodsTypeNameTitle=(TextView)(findViewById(R.id.goodsTypeNameTitle));
		goodsTypeNameTitle.setText("("+typeName+")");
		yearStr=StringUtils.substring(dateStr, 0, 4);
		monthStr=StringUtils.substring(dateStr,4,6);
		flashValueList();
		ctx=getApplicationContext();
		
		
		
		TextView saveView=(TextView)findViewById(R.id.addNote_save);
		saveView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText valPerView = (EditText)findViewById(R.id.valPer);
				TimePicker timePicker=(TimePicker)findViewById(R.id.valTime);
				EditText memoEdit = (EditText)findViewById(R.id.memoPer);
				Integer currentHour = timePicker.getCurrentHour();
				Integer currentMinute = timePicker.getCurrentMinute();
				String valMemo= memoEdit.getText().toString();
				dateStr=titleView.getText().toString();
				yearStr=StringUtils.substring(dateStr, 0, 4);
				monthStr=StringUtils.substring(dateStr,4,6);
				String pervalue = valPerView.getText().toString();
				boolean empty = StringUtils.isEmpty(pervalue);
				if(empty){
					Toast.makeText(AddNoteActivity.this,"吨数不能为空！",5000).show();
				}else{
					String[] pervalues = pervalue.split("\\s+");
					SQLiteDatabase database = MyDbUtils.getDb(getApplicationContext());
					String insertSql="insert into gc_goods_pervalue (id,year_str,month_str,date_full_str,hour_str,minite_str,pervalue,goods_type_py,val_memo) values(?,?,?,?,?,?,?,?,?)";
					Boolean isError=false;
					for (String value : pervalues) {
						try {
							Float.parseFloat(value);
						} catch (NumberFormatException e) {
							e.printStackTrace();
							Toast.makeText(AddNoteActivity.this,"输入有误，请核对！",5000).show();
							isError=true;
							break;
						}
						//database.execSQL(insertSql,new Object[]{null,Integer.parseInt(yearStr),Integer.parseInt(monthStr),Integer.parseInt(dateStr),currentHour,currentMinute,Float.parseFloat(value),goodsType,valMemo});
					}
					if(isError){
						return;
					}else{
						for (String value : pervalues) {
							database.execSQL(insertSql,new Object[]{null,Integer.parseInt(yearStr),Integer.parseInt(monthStr),Integer.parseInt(dateStr),currentHour,currentMinute,Float.parseFloat(value),goodsType,valMemo});
						}
					}
					Toast.makeText(AddNoteActivity.this,"保存成功！",5000).show();
					valPerView.setText("");
					memoEdit.setText("");
					flashValueList();
					//rawQueryCursor.close();
					database.close();
				}
			}

			
		});
		
		
		ImageView backView=(ImageView)findViewById(R.id.goBack);
		backView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AddNoteActivity.this.finish();
				
			}
		});
		
		
		goodsValListView = (ListView)findViewById(R.id.goodsValListViewPerDay);
		goodsValListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/*arg1.getId();
				if(arg1.getId()==R.id.delBtn){
					Toast.makeText(AddNoteActivity.this,"删除成功！",5000).show();
				}
				currCursor.moveToPosition(arg2);
				int id = currCursor.getInt(currCursor.getColumnIndex("_id"));
				float pervalue = currCursor.getFloat(currCursor.getColumnIndex("pervalue"));
				SQLiteCursor cursor=(SQLiteCursor)goodsValListView.getItemAtPosition(arg2);
				int id = cursor.getInt(cursor.getColumnIndex("_id"));
				SQLiteDatabase db = MyDbUtils.getDb(getApplicationContext());
				db.execSQL("delete from gc_goods_pervalue where id=?", new Object[]{id});
				Toast.makeText(AddNoteActivity.this,"删除成功！",5000).show();
				flashValueList();*/
			}
		});
		
		goodsValListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				currCursor.moveToPosition(arg2);
				new AlertDialog.Builder(AddNoteActivity.this).setTitle("请确认")
				.setMessage("确认删除吗?").setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						int id = currCursor.getInt(currCursor.getColumnIndex("_id"));
						 SQLiteDatabase db = MyDbUtils.getDb(getApplicationContext());
						db.execSQL("delete from gc_goods_pervalue where id=?", new Object[]{id});
						Toast.makeText(AddNoteActivity.this,"删除成功！",5000).show();
						flashValueList();
					}
				}).setNegativeButton("否", null).show();
				return false;
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_note, menu);
		return true;
	}

	private void flashValueList() {
		SQLiteDatabase database = MyDbUtils.getDb(getApplicationContext());
		String selectSql="select gp.id _id,gp.hour_str||'时'||gp.minite_str||'分' hour_minite_str,gp.pervalue,gp.val_memo  from gc_goods_pervalue gp where gp.date_full_str=? and gp.goods_type_py=? order by hour_str, minite_str desc";
		Cursor rawQueryCursor = database.rawQuery(selectSql, new String[]{dateStr,goodsType});
		MyCursorAdapter adapter=new MyCursorAdapter(AddNoteActivity.this, R.layout.value_per_view, rawQueryCursor, new String[]{"hour_minite_str","pervalue","val_memo"},new int[]{R.id.hourMiniteStr,R.id.valPer,R.id.valMemo});
		ListView  goodsValListView= (ListView)findViewById(R.id.goodsValListViewPerDay);
		goodsValListView.setAdapter(adapter);
	}
	
	public class MyCursorAdapter extends SimpleCursorAdapter{
		int position=-1;
		private LayoutInflater mInflater;
		
		
		
		public MyCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			mInflater=LayoutInflater.from(context);
			position=c.getPosition();
		}

		
		


		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//TextView deleteView = (TextView)(view.findViewById(R.id.delBtn));
			currCursor=cursor;
			/*deleteView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(AddNoteActivity.this).setTitle("请确认")
					.setMessage("确认删除吗?").setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							int id = currCursor.getInt(currCursor.getColumnIndex("_id"));
							 SQLiteDatabase db = MyDbUtils.getDb(getApplicationContext());
							db.execSQL("delete from gc_goods_pervalue where id=?", new Object[]{id});
							Toast.makeText(AddNoteActivity.this,"删除成功！",5000).show();
							flashValueList();
						}
					}).setNegativeButton("否", null).show();
					
					 
				}
			});*/
			super.bindView(view, context, cursor);
		}
		
		
		
	}
	
}
