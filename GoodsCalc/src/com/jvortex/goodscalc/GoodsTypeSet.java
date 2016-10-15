package com.jvortex.goodscalc;

import java.util.Date;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jvortex.common.MyDbUtils;
import com.jvortex.common.PinYingUtils;

public class GoodsTypeSet extends Activity {
	TextView goodsTypeAddView;
	Cursor currentCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goods_type_set);
		goodsTypeAddView=(TextView)(findViewById(R.id.goods_type_add));
		goodsTypeAddView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final EditText tmpTextView=new EditText(GoodsTypeSet.this);
				tmpTextView.setHint("请输入货物名称");
				new AlertDialog.Builder(GoodsTypeSet.this).setTitle("请输入货物类型")
				.setView(tmpTextView).setPositiveButton("确定",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						SQLiteDatabase db = MyDbUtils.getDb(GoodsTypeSet.this);
						String contentText=tmpTextView.getText().toString();
						//根据名字生成拼音
						String pinying;
						try {
							pinying=PinYingUtils.chineneToSpell(contentText.trim());
						} catch (BadHanyuPinyinOutputFormatCombination e) {
							e.printStackTrace();
							pinying=contentText;
						}
						//判断数据库中是否已经存在,已存在的不再添加
						String existSql="select count(1) from gc_goods_type gt where gt.type_py=?";
						Cursor existCursor = db.rawQuery(existSql, new String[]{pinying});
						int count = existCursor.getCount();
						if(count>0){
							existCursor.moveToNext();
							int existCount = existCursor.getInt(0);
							if(existCount>0){
								Toast.makeText(GoodsTypeSet.this, "名字与现有类型冲突，请换一个名字！", 5000).show();
								return;
							}
						}
						existCursor.close();
						String insertSql="insert into gc_goods_type (id,is_del,last_use_time,type_name,type_py) values(?,?,?,?,?)";
						db.execSQL(insertSql, new Object[]{null,0,new Date(),contentText,pinying});
						Toast.makeText(GoodsTypeSet.this, "添加成功！", 5000).show();
						flashGoodsType();
					}
				}).setNegativeButton("取消", null).show();
			}
		});
		
		ImageView backView=(ImageView)findViewById(R.id.goodsTypeGoBack);
		backView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GoodsTypeSet.this.finish();
				
			}
		});
		
		flashGoodsType();
	}

	private void flashGoodsType() {
		SQLiteDatabase db = MyDbUtils.getDb(GoodsTypeSet.this);
		String querySql="select gt.id _id,gt.type_name,gt.type_py from gc_goods_type gt order by gt.last_use_time desc";
		Cursor goodsType = db.rawQuery(querySql, null);
		GoodsCursorAdapter goodsCursorAdapter=new GoodsCursorAdapter(GoodsTypeSet.this, R.layout.goods_type_list, goodsType, new String[]{"type_name"}, new int[]{R.id.goodsTypeName});
		ListView goodsTypeListView = (ListView)findViewById(R.id.goodsTypeListView);
		goodsTypeListView.setAdapter(goodsCursorAdapter);
		goodsTypeListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				currentCursor.moveToPosition(arg2);
				new AlertDialog.Builder(GoodsTypeSet.this).setTitle("请确认")
				.setMessage("删除此类型后，会把此类型下的货物记录也删除掉，确认删除吗？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String typePy = currentCursor.getString(currentCursor.getColumnIndex("type_py"));
						String delSql="delete from gc_goods_type where type_py=?";
						SQLiteDatabase db = MyDbUtils.getDb(GoodsTypeSet.this);
						db.execSQL(delSql, new Object[]{typePy});
						String delGoodsValue="delete from gc_goods_pervalue where goods_type_py=?";
						db.execSQL(delGoodsValue, new Object[]{typePy});
						flashGoodsType();
					}
				}).setNegativeButton("否", null).show();
				return false;
			}});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.goods_type_set, menu);
		return true;
	}
	

	class GoodsCursorAdapter extends SimpleCursorAdapter{
		
		public GoodsCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			currentCursor=cursor;
			/*TextView textView=(TextView)(view.findViewById(R.id.goodsTypeDelBtn));
			textView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(GoodsTypeSet.this).setTitle("请确认")
					.setMessage("删除此类型后，会把此类型下的货物记录也删除掉，确认删除吗？")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String typePy = currentCursor.getString(currentCursor.getColumnIndex("type_py"));
							String delSql="delete from gc_goods_type where type_py=?";
							SQLiteDatabase db = MyDbUtils.getDb(GoodsTypeSet.this);
							db.execSQL(delSql, new Object[]{typePy});
							String delGoodsValue="delete from gc_goods_pervalue where goods_type_py=?";
							db.execSQL(delGoodsValue, new Object[]{typePy});
							flashGoodsType();
						}
					}).setNegativeButton("否", null).show();
				}
			});*/
			super.bindView(view, context, cursor);
		}
	}
}
