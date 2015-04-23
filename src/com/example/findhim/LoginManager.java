package com.example.findhim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

public class LoginManager {
	protected static final String TAG = "LoginManager";
	private String mCookieString;
	private ArrayList<String> mDevicesId; // ���û����豸���б�

	// ����AsyncTask,ʵ�ֵ�½
	public interface ICallBack {
		/* ��¼�ɹ�ʱ���õĽӿ� */
		public void onSuccess();

		/* ��½�����н�"��¼"��Ϊ"���ڵ�¼.." */
		public void onSetLoginUI();

		/* ��¼ʧ��ʱ���õĽӿ� */
		public void onFailed(String error);
	}

	public void login(final String idString, final String pwdString,
			final ICallBack callBack) {
		new AsyncTask<Void, Void, String>() {
			/* ��ʼִ���첽�߳� */
			@Override
			protected void onPreExecute() {
				callBack.onSetLoginUI();
			}

			/* ��̨���񣺷��ز������Ͷ�Ӧ�������������� */
			@Override
			protected String doInBackground(Void... params) {
				URL url = null;
				String loginResult = null;
				HttpURLConnection conn = null;
				try {
					url = new URL(
							"http://mandmlee.nat123.net:40192/findHimm/Login?id="
									+ idString + "&pwd=" + pwdString);
					Log.i(TAG, "url: " + url.toString());
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true);
					conn.connect();
					if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) { // �����Ӳ��ɹ�ʱ
						loginResult = "���ӷ�����ʧ��";
					} else {
						// ��ȡ������Ϣ����֤�û��Ƿ���ȷ
						InputStream in = conn.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in, "GBK"));
						loginResult = reader.readLine();
						//Log.i(TAG, "loginResult: " + loginResult); 
						if (loginResult.equals("pwd is wrong")) {
							loginResult = "�������";
						} else if (loginResult.equals("no such user")) {
							loginResult = "�޴��û�";
						} else {
							// ��¼�ɹ�����ȡCookie
							Map<String, List<String>> map = conn
									.getHeaderFields();
							String s = map.get("Set-Cookie").toString();
							mCookieString = s.substring(1, 44);
							// Log.i(TAG, "Cookie: " + mCookieString);
							// ��ȡ���û����豸���б�
							mDevicesId = new ArrayList<String>();
							String deviceId;
							while ((deviceId = reader.readLine()) != null) {
								Log.i(TAG, "�豸��: " + deviceId);
								mDevicesId.add(deviceId);
							}
						}
						in.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				return loginResult;
			}

			/* �����������󣬿ɴ���UI */
			protected void onPostExecute(String loginResult) {
				if (loginResult.equals("success")) {// ��¼�ɹ�
					callBack.onSuccess();
				} else {
					callBack.onFailed(loginResult);
				}
			}

		}.execute();
	}

	public String getCookie() {
		return mCookieString;
	}

	public ArrayList<String> getDevicesId() {
		return mDevicesId;
	}
}
