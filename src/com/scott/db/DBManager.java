package com.scott.db;

import java.util.ArrayList;
import java.util.Date;

import com.example.model.Device;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {

	private static final String TAG = "DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;
	private Context mContext;

	public DBManager(Context context) {
		mContext = context;
		helper = new DBHelper(context);
		db = helper.getWritableDatabase(); // ��ȡһ�����ݿ�ʵ��
	}

	/**
	 * ���һ��ֻ����id�ļ�¼
	 */
	public void add(String device_id) {
		db.beginTransaction(); // ��ʼ����
		try {
			db.execSQL("INSERT INTO device VALUES(?,null,null,null,null)",
					new String[] { device_id });
			db.setTransactionSuccessful(); // ��������ɹ����
		} finally {
			db.endTransaction(); // ��������
		}
	}

	/**
	 * �жϴ�id��¼�Ƿ����
	 */
	public boolean isRecordExist(String device_id) {

		Cursor cursor = db.rawQuery("SELECT _id FROM device WHERE _id=?",
				new String[] { device_id });
		if (cursor.moveToNext()) { // �Ƿ����һ����¼
			return true;
		}
		return false;

	}

	/**
	 * �ڱ���ɾ����id�豸��¼
	 */
	public void deleteDevice(String device_id) {
		db.delete("device", "_id=?", new String[] { device_id });
		// ɾ��
	}

	/**
	 * ��ȡ�������еļ�¼����ʵ��������һ��ArrayList<Device>����
	 */
	public ArrayList<Device> getDevices() {
		ArrayList<Device> devices = new ArrayList<Device>();
		Cursor cursor = db.rawQuery("SELECT * FROM device", null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor.getColumnIndex("_id"));
			String name = cursor
					.getString(cursor.getColumnIndex("device_name"));
			String photo = cursor.getString(cursor
					.getColumnIndex("device_photo"));
			Device device = new Device(id, mContext);
			device.setDeviceName(name);
			device.setDevicePhoto(photo);
			devices.add(device); // ���
		}
		return devices;
	}

	/**
	 * ��ȡָ��id���豸����
	 */
	public Device getDevice(String deviceId) {
		Device device = new Device(deviceId, mContext);
		Cursor cursor = db.rawQuery("SELECT * FROM device WHERE _id=?",
				new String[] { deviceId });
		while (cursor.moveToNext()) {
			String name = cursor
					.getString(cursor.getColumnIndex("device_name"));
			String photo = cursor.getString(cursor
					.getColumnIndex("device_photo"));
			Long time = cursor.getLong(cursor.getColumnIndex("device_time"));
			Date dateTime = new Date(time);
			String intro = cursor.getString(cursor
					.getColumnIndex("device_intro"));
			device.setDeviceIntro(intro);
			device.setDeviceTime(dateTime);
			device.setDeviceName(name);
			device.setDevicePhoto(photo);
		}
		return device;
	}

	/**
	 * ����ָ��id��ͼƬ
	 */
	public void updatePhoto(String deviceId, String photoString) {

		ContentValues cv = new ContentValues();
		cv.put("device_photo", photoString);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	/**
	 * ����ָ��id������
	 */
	public void updateName(String deviceId, String deviceName) {

		ContentValues cv = new ContentValues();
		cv.put("device_name", deviceName);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	public void updateTime(String deviceId, Date deviceTime) {

		ContentValues cv = new ContentValues();
		Long time = deviceTime.getTime();
		cv.put("device_time", time);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	public void updateIntro(String deviceId, String deviceIntro) {

		ContentValues cv = new ContentValues();
		cv.put("device_intro", deviceIntro);
		db.update("device", cv, "_id=?", new String[] { deviceId });

	}

	/**
	 * �ر����ݿ�
	 */
	public void destroyDB() {
		db.close();
	}
}
