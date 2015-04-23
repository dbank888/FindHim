package com.scott.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "findhim";
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = "DBHelper";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// ����������null ��ʾʹ��Ĭ���α����
	}

	// ���ݿ��һ�α�����ʱonCreate�ᱻ����
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "DBHelper onCreate");
		// ����һ��device��
		db.execSQL("CREATE TABLE IF NOT EXISTS device"
				+ "(_id VARCHAR PRIMARY KEY, device_name VARCHAR,device_photo VARCHAR,device_time LONG,device_intro VARCHAR)");
	}

	// ���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "DBHelper onUpgrade");
		db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}

}
