package com.example.findhim;

import java.io.File;
import java.util.ArrayList;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowTrackFragment extends Fragment implements OnItemClickListener {
	public static final String EXTRA_KEY_DEVICEID = "device_id";
	private static final String TAG = "ShowTrackFragment";
	private String mCookie;
	private GridView mGridView; // ��ʾ�����豸��GridView�ؼ�
	private DBManager mDmg; // ���ݿ�������
	private ArrayList<Device> mDevices; // ���е��豸����
	private GridAdapter mAdapter; // ������

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "ShowTrackFragment onCreate");
		// ��ȡLoginActivity���͹�����Cookie
		mCookie = getActivity().getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDmg = new DBManager(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "oneCreateView ShowTrackFragment");
		View view = inflater.inflate(R.layout.fragment_show_track, null);
		mGridView = (GridView) view.findViewById(R.id.gridView_show_track); // ʵ����
		mDevices = mDmg.getDevices(); // �����ݿ���ʵ�����豸���󼯺�
		mAdapter = new GridAdapter(mDevices);
		mGridView.setAdapter(mAdapter); // ����������
		mGridView.setOnItemClickListener(this); // ���ü�����
		return view;
	}

	/**
	 * ���´�Fragmentҳ��
	 */
	public void updateFragment() {
		Log.i(TAG, "updateFragment");
		mDevices = mDmg.getDevices();// �Ƿ���mDevices������Ӱ�죿
		mAdapter = new GridAdapter(mDevices);
		mGridView.setAdapter(mAdapter); // ����������
	}

	private class GridAdapter extends ArrayAdapter<Device> {

		public GridAdapter(ArrayList<Device> devices) {
			super(getActivity(), 0, devices); // 0��ʾitemʹ���Զ��岼��
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position >= mDevices.size()) {
				return null;
			}
			if (convertView == null) {
				// itemʹ���Զ��岼��
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.gridview_item_show_track, null);
			}
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.showTrack_item_imageView);
			TextView textView = (TextView) convertView
					.findViewById(R.id.showTrack_item_textView);
			String id = mDevices.get(position).getDeviceId();
			String name = mDevices.get(position).getDeviceName();
			String photo = mDevices.get(position).getDevicePhoto();

			if (photo != null) { // ����ж�Ӧ��ͼƬ����ʾͼƬ
				File photoFile = mDevices.get(position).getPhotoFile();
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true; // ������bitmap���󣬷������أ����ǿ�����������߲�ѯλͼ
				BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
				options.inSampleSize = 4; // ͼΪԭͼ����1/4
				options.inJustDecodeBounds = false;
				Bitmap bm = BitmapFactory.decodeFile(
						photoFile.getAbsolutePath(), options);
				
				Bitmap rbm = PictureUtils.toRoundBitmap(bm); // ��ͼƬת����Բ��
				imageView.setImageBitmap(rbm); // ����ͼƬ
				
			} else {
				imageView.setImageResource(R.drawable.icon_default);
			}
			if (name != null) { // ������Ʋ�Ϊ����ʾ���֣�������ʾid
				textView.setText(name);
			} else {
				textView.setText(id);
			}
			return convertView;
		}

	}

	/**
	 * GridView item�ļ�����
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ShowMapActivity.class);
		// ����Cookie��Ҫ��ʾ���豸id��ShowMapActivity
		intent.putExtra(LoginActivity.EXTRA_KEY_COOKIE, mCookie);
		intent.putExtra(this.EXTRA_KEY_DEVICEID, mDevices.get(position)
				.getDeviceId());
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "ShowTrackFragment onDestroy");
		mDmg.destroyDB();
		super.onDestroy();
	}
}
