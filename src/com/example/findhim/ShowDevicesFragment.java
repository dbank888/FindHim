package com.example.findhim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowDevicesFragment extends ListFragment {

	// �л�Fragmentʱ���������ڱ仯

	private static final String TAG = "ShowDevicesFragment";
	public static final String EXTRA_KEY_DEVICE_ID = "device_id";
	private static final int REQUES_CODE = 0;
	private String mCookie;
	private DBManager mDmg; // ���ݿ�������
	private ArrayList<Device> mDevices; // ���е��豸����
	private MyAdapter mMyAdapter; // listView��������
	private View mProgressBar;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG, "ShowDevicesFragment onCreate");
		mCookie = getActivity().getIntent().getStringExtra(
				LoginActivity.EXTRA_KEY_COOKIE);
		mDmg = new DBManager(getActivity());
		mDevices = mDmg.getDevices(); // �����ݿ���ʵ�����豸���󼯺�

		mMyAdapter = new MyAdapter(mDevices);
		setListAdapter(mMyAdapter);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "ShowDevicesFragment onCreateView");
		View view = inflater.inflate(R.layout.fragment_show_device, container,
				false);

		mProgressBar = view.findViewById(R.id.show_device_progressContainer); // ������
		mProgressBar.setVisibility(View.INVISIBLE);// ����Ϊ���ɼ�

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		// ��onCreateView()������ɵ��ò�������ͼ֮ǰ��getListView()��������ֵΪnull

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(listView); // ��listViewע�������Ĳ˵�
		} else {
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); // listView����Ϊ��ѡ
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

				// �ص���������ͼ��ѡ�л��߳����������
				public void onItemCheckedStateChanged(
						android.view.ActionMode mode, int position, long id,
						boolean checked) {

				}

				// ʵ�ֵ���һ���ӿ� ActionMode.Callback ������������Ҫʵ�ֵķ���
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.list_context_menu,
							menu);
					return true;
				}

				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}

				public boolean onActionItemClicked(ActionMode mode,
						MenuItem item) {
					switch (item.getItemId()) {
					case R.id.context_menu_item_delete:

						final ArrayList<Integer> selectedDevices = new ArrayList<Integer>();
						// �洢�����е��豸
						for (int i = mMyAdapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								selectedDevices.add(i); // ���Ҫ�h���豸�ı��
							}
						}
						new AlertDialog.Builder(getActivity()).setTitle("ϵͳ��ʾ")
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setMessage("ȷ��ɾ������ѡ���豸��������Ϣ(������������)��")
								.setPositiveButton("ȷ��", new OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										deleteDevice(selectedDevices);
										// ɾ���豸
									}
								}).setNegativeButton("ȡ��", null).show();
						mode.finish();
						mMyAdapter.notifyDataSetChanged(); // ����listView����ͼ
						return true;
					default:
						return false;
					}
				}

				public void onDestroyActionMode(android.view.ActionMode mode) {

				}
			});
		}
		return view;
	}

	/**
	 * ɾ���ʹ��豸��ص�������Ϣ
	 */
	protected void deleteDevice(final ArrayList<Integer> devices) {

		// �ڷ�������ɾ���豸
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				mProgressBar.setVisibility(View.VISIBLE);// ����Ϊ�ɼ�
			};

			@Override
			protected Boolean doInBackground(Void... params) {
				// ����ɾ�����󵽷�����
				boolean isSuccess = true;
				String urlString = "http://mandmlee.nat123.net:40192/findHimm/DeleteDevices?";
				for (int i = 0; i < devices.size(); i++) {
					int pos = (int) devices.get(i);
					if (i == 0) {
						urlString += "device_id="
								+ mDevices.get(pos).getDeviceId();
					} else {
						urlString += "&device_id="
								+ mDevices.get(pos).getDeviceId();
					}
				}
				Log.i(TAG, "ɾ������url: " + urlString);
				try {
					URL url = new URL(urlString);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.addRequestProperty("Cookie", mCookie);
					conn.setDoInput(true);
					conn.connect();
					if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) { // �����Ӳ��ɹ�ʱ
						Log.i(TAG, "���ӷ�����ʧ��");
						isSuccess = false;
					} else {
						InputStream in = conn.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in));
						// ��ȡ���ص�ֵ
						String resultString = reader.readLine();
						if (resultString.equals("fail")) {
							isSuccess = false;
						}
					}
				} catch (MalformedURLException e) {
					isSuccess = false;
					e.printStackTrace();
				} catch (IOException e) {
					isSuccess = false;
					e.printStackTrace();
				}
				return isSuccess;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == true) { // ����������ϵ��豸ɾ���ɹ�
					// ɾ�������豸��Ϣ
					for (int i = 0; i < devices.size(); i++) {
						int pos = (int) devices.get(i);
						// �����ݿ���ɾ���豸�������Ϣ
						mDmg.deleteDevice(mDevices.get(pos).getDeviceId());
						// ɾ��ͼƬ
						if (mDevices.get(pos).getDevicePhoto() != null) {
							File file = mDevices.get(pos).getPhotoFile();
							file.delete();
						}
						mDevices.remove(pos);
					}
					mProgressBar.setVisibility(View.INVISIBLE);// ����Ϊ���ɼ�
					Toast.makeText(getActivity(), "ɾ���ɹ�", Toast.LENGTH_SHORT)
							.show();
				} else {
					mProgressBar.setVisibility(View.INVISIBLE);// ����Ϊ���ɼ�
					Toast.makeText(getActivity(), "ɾ��ʧ��", Toast.LENGTH_SHORT)
							.show();
				}
				// ���µ�ǰFragment
				mMyAdapter.notifyDataSetChanged();
				// ����ShowTrackFragment
				ShowTrackFragment fragment = (ShowTrackFragment) getActivity()
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":0");
				fragment.updateFragment();
			};

		}.execute();
	}

	@SuppressWarnings("rawtypes")
	private class MyAdapter extends ArrayAdapter {
		private static final String TAG = "MyAdapter";

		@SuppressWarnings("unchecked")
		public MyAdapter(ArrayList<Device> devices) {
			super(getActivity(), 0, devices);
		}

		@SuppressLint("InflateParams")
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.listview_item_show_device, null);
			}

			// ��ʼ��ListView�����
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.showDevice_item_imageView);
			TextView textId = (TextView) convertView
					.findViewById(R.id.showDevice_item_textId);
			TextView textName = (TextView) convertView
					.findViewById(R.id.showDevice_item_textName);

			String id = mDevices.get(position).getDeviceId();
			String name = mDevices.get(position).getDeviceName();
			String photo = mDevices.get(position).getDevicePhoto();

			textId.setText(id);
			if (name != null) { // ���������
				textName.setText(name);
			}

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
			return convertView;
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// �����������豸��Ϣ��activity��
		Intent intent = new Intent(getActivity(), UpdateDeviceInfo.class);
		intent.putExtra(EXTRA_KEY_DEVICE_ID, mDevices.get(position)
				.getDeviceId());
		startActivityForResult(intent, REQUES_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ���µ�ǰҳ��
		if (mDmg == null) {
			mDmg = new DBManager(getActivity());
		}
		mDevices = mDmg.getDevices();
		mMyAdapter.notifyDataSetChanged();
		// ����ShowTrackFragment
		ShowTrackFragment fragment = (ShowTrackFragment) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":0");
		fragment.updateFragment();
	}

	/**
	 * ����һ�������Ĳ˵���ÿ�γ���View(��ע��)���ᵯ���˵�
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i(TAG, "onCreateContextMenu");
		getActivity().getMenuInflater().inflate(R.menu.list_context_menu, menu);
	}

	/**
	 * �����Ĳ˵�����¼�
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG, "onContextItemSelected");
		// MenuItem��һ����ԴID��������ʶ��ѡ�еĲ˵���
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		// ȡ���������item�����Device����
		int position = info.position;
		final ArrayList<Integer> selectedDevices = new ArrayList<Integer>();
		selectedDevices.add(position);

		switch (item.getItemId()) {
		case R.id.context_menu_item_delete:
			new AlertDialog.Builder(getActivity()).setTitle("ϵͳ��ʾ")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("ȷ��ɾ������ѡ���豸��������Ϣ(������������)��")
					.setPositiveButton("ȷ��", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteDevice(selectedDevices);
							// ɾ���豸
						}
					}).setNegativeButton("ȡ��", null).show();
			mMyAdapter.notifyDataSetChanged(); // ����listView����ͼ
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "ShowDevicesFragment onDestroy");
		mDmg.destroyDB();
		super.onDestroy();
	}
}
