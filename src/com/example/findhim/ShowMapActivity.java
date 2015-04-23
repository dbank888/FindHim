package com.example.findhim;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.utils.NetworkState;

import android.support.v4.app.NavUtils;
import android.text.TextPaint;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowMapActivity extends Activity {

	protected static final String TAG = "ShowMapActivity";
	private static final String KEY_IsShowTrack = "isShowTrack";
	private static final String KEY_HistoryPoints = "historyPoints";
	private static final String KEY_IsShowSatellite = "isShowSatellite";
	private MapView mMapView = null; // �ٶȵ�ͼ�����ؼ�
	private BaiduMap mBaiduMap = null; // �ٶȵ�ͼ�Ŀ�����
	private View mProgressContainer; // �������ؼ�
	private List<LatLng> mHistoryPoints; // ��ʷ�켣
	private LatLng mCurrentLocation; // ��ǰλ��
	private boolean mIsShowTrack; // �Ƿ���ʾ�켣
	private boolean mIsShowSatellite; // �Ƿ���ʾ���ǵ�ͼ
	private String mCookieString; // �����֤
	private String mDeviceId; // ��ǰҪ��ʾ���豸��
	private GetInfoFromServer mGetInfoFromServer; // ��ȡ��Ϣ����

	Handler draw_current_handler = new Handler();
	Runnable draw_current_thread = new Runnable() {
		public void run() {
			if (mGetInfoFromServer.isWorkDone()) { // �����Ѿ���������
				mHistoryPoints = mGetInfoFromServer.getHistoryPoints();
				if (mHistoryPoints.isEmpty()) { // ���켣Ϊ��ʱ
					Toast.makeText(ShowMapActivity.this, "����δ�����κ��㼣",
							Toast.LENGTH_SHORT).show();
				} else {
					mCurrentLocation = mHistoryPoints
							.get(mHistoryPoints.size() - 1);
					mBaiduMap.clear();
					mProgressContainer.setVisibility(View.INVISIBLE); // ���ؽ�����
					Toast.makeText(ShowMapActivity.this, "���سɹ�",
							Toast.LENGTH_SHORT).show();
					DrawCurrentPoint();
					if (mIsShowTrack == true) {
						DrawTrack();
					}
				}
				draw_current_handler.removeCallbacks(draw_current_thread);
			} else {
				draw_current_handler.postDelayed(draw_current_thread, 1);
			}
		}
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		// requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������
		setContentView(R.layout.activity_show_map);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Log.i(TAG, "onCreate");
		mCookieString = getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDeviceId = getIntent().getStringExtra(
				ShowTrackFragment.EXTRA_KEY_DEVICEID);

		mIsShowTrack = false;
		mIsShowSatellite = false;
		mProgressContainer = (View) findViewById(R.id.show_map_progressContainer);
		// ���ý��������ɼ�
		mProgressContainer.setVisibility(View.INVISIBLE);
		mMapView = (MapView) findViewById(R.id.bmapView);// ��ȡ��ͼ�ؼ�����
		mBaiduMap = mMapView.getMap();

		// �����ǰ��
		mHistoryPoints = new ArrayList<LatLng>();
		if (!NetworkState.isNetworkConnected(getApplication())) { // ����豸δ��������
			Toast.makeText(ShowMapActivity.this, "δ��������", Toast.LENGTH_SHORT)
					.show();
		} else {
			// �������ӷ�������ȡ��Ϣ
			mGetInfoFromServer = new GetInfoFromServer(mCookieString, mDeviceId);
			mProgressContainer.setVisibility(View.VISIBLE); // ��ʾ������
			mGetInfoFromServer.startWork();
			draw_current_handler.post(draw_current_thread); // �ύ��ͼHandler
		}

	}

	/* �����ǰλ�� */
	public void DrawCurrentPoint() {
		Log.i(TAG, "DrawCurrentPoint");
		// ������������
		// �ڵ�ͼ���ҵ���λ��
		MapStatus mMapStatus = new MapStatus.Builder().target(mCurrentLocation)
				.zoom(12).build(); // �����ͼ״̬
		// ����MapStatusUpdate�����Ա�������ͼ״̬��Ҫ�����ı仯
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// �ı��ͼ״̬
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		TextView tv = new TextView(ShowMapActivity.this);
		tv.setBackgroundResource(R.drawable.location_tips);
		tv.setPadding(10, 10, 10, 0);
		tv.setText("��ǰλ��");
		tv.setTextColor(0xffff0000);
		TextPaint tp = tv.getPaint();
		tp.setFakeBoldText(true); // ����
		InfoWindow infoWindow = new InfoWindow(tv, mCurrentLocation, -47);
		mBaiduMap.showInfoWindow(infoWindow);

		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_current_pt);// ����Markerͼ��
		// ����MarkerOption,�����ڵ�ͼ�����Marker
		OverlayOptions option = new MarkerOptions().position(mCurrentLocation)
				.icon(bitmap);
		mBaiduMap.addOverlay(option); // ��Ӹ�����ѡ��
	}

	/* �����ʷ�켣 */
	public void DrawTrack() {
		Log.i(TAG, "DrawTrack");
		LatLng startPoint = mHistoryPoints.get(0);
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_start_pt);// �������ͼ��
		OverlayOptions option = new MarkerOptions().position(startPoint).icon(
				bitmap);
		mBaiduMap.addOverlay(option); // ��Ӹ�����ѡ��
		// �����û������߶ε�Option����
		OverlayOptions PolylineOption = new PolylineOptions().points(
				mHistoryPoints).color(0xffff0000);
		// �ڵ�ͼ�����ֱ��Option,������ʾ
		mBaiduMap.addOverlay(PolylineOption);
	}

	/* �����÷����仯ʱ��������������Activity�����ǻ�ص��˷������û����н��ж���Ļ��ת����д��� */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_map, menu);
		MenuItem track = menu.findItem(R.id.menu_item_track);
		MenuItem mapType = menu.findItem(R.id.menu_item_map_type);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_track:
			if (item.getTitle().toString().equals("��ʾ�켣")) {
				mIsShowTrack = true;
				item.setTitle(R.string.hide_track);
				DrawTrack();
			} else {
				item.setTitle(R.string.show_track);
				mIsShowTrack = false;
				mBaiduMap.clear();
				DrawCurrentPoint();
			}
			return true;
		case R.id.menu_item_map_type:
			if (item.getTitle().toString().equals("���ǵ�ͼ")) {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				mIsShowSatellite = true;
				item.setTitle("��ͨ��ͼ");
			} else {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				mIsShowSatellite = false;
				item.setTitle("���ǵ�ͼ");
			}
			return true;
		case R.id.menu_item_fresh:
			if (!NetworkState.isNetworkConnected(getApplication())) { // ����豸δ��������
				Toast.makeText(ShowMapActivity.this, "δ��������",
						Toast.LENGTH_SHORT).show();
			} else {
				if (mGetInfoFromServer == null
						|| mGetInfoFromServer.isWorkDone()) {
					mGetInfoFromServer = new GetInfoFromServer(mCookieString,
							mDeviceId);
					mProgressContainer.setVisibility(View.VISIBLE); // ��ʾ������
					mGetInfoFromServer.startWork();
				}
				draw_current_handler.post(draw_current_thread);
			}
			return true;
		case android.R.id.home:
			if(NavUtils.getParentActivityName(this)!=null){
				NavUtils.navigateUpFromSameTask(this);
			}
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		Log.i(TAG, "onDestroy");
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		Log.i(TAG, "onResume");
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		Log.i(TAG, "onPause");
		mMapView.onPause();
	}
}
