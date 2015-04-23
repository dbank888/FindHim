package com.example.findhim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Toast;

import com.example.model.Device;
import com.example.utils.PictureUtils;
import com.scott.db.DBManager;

public class UpdateDeviceInfo extends Activity implements OnClickListener,
		DialogInterface.OnClickListener {

	private static final String TAG = "UpdateDeviceInfo";
	private static final int REQUES_TAKE_PHOTO = 0;
	private static final int RESULT_LOAD_IMAGE = 1;
	private ImageView mImageView; // �豸ͷ��
	private TextView mTextView; // �豸id
	private PopupWindow mPop; // ����ѡ�����ͷ��Ĵ�
	private View mPopContent; // ��mPop����ʾ������
	private View mPart; // mPop�ڴ�View�·���ʾ
	private View mUpdateNameRelativeLayout; // ��������һ������
	private TextView mNameTextView; // ��ʾ��Ӧ�豸���ƵĿؼ�
	private View mUpdateTimeRelativeLayout;
	private TextView mTimeTextView;
	private View mUpdateIntroRelativeLayout;
	private TextView mIntroTextView;

	private Button mTakePhotoButton;
	private Button mSelectPhotoButton;
	private Button mCancelButton;
	private Device mDevice;
	private String mDeviceId;
	private DBManager mDmg;
	private EditText mEditText;
	private Date mDate;
	private Date date;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint({ "InflateParams", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_device_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle("����");
		mDeviceId = getIntent().getStringExtra(
				ShowDevicesFragment.EXTRA_KEY_DEVICE_ID);
		mDmg = new DBManager(this);
		mDevice = mDmg.getDevice(mDeviceId); // ʵ������ǰ�豸

		String id = mDevice.getDeviceId();
		String name = mDevice.getDeviceName();
		String photo = mDevice.getDevicePhoto();
		String intro = mDevice.getDeviceIntro();
		mDate = mDevice.getDeviceTime();

		mImageView = (ImageView) findViewById(R.id.updateImageView);
		mTextView = (TextView) findViewById(R.id.updateTextView);

		mTextView.setText(id);
		if (photo != null) { // ����ж�Ӧ��ͼƬ����ʾͼƬ
			File photoFile = mDevice.getPhotoFile();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true; // ������bitmap���󣬷������أ����ǿ�����������߲�ѯλͼ
			BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
			options.inSampleSize = 4; // ͼΪԭͼ����1/4
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),
					options);
			Bitmap rbm = PictureUtils.toRoundBitmap(bm); // ��ͼƬת����Բ��
			mImageView.setImageBitmap(rbm); // ����ͼƬ
		} else {
			mImageView.setImageResource(R.drawable.icon_default);
		}
		mPopContent = getLayoutInflater().inflate(R.layout.item_popupwindows,
				null); // ʵ�������ݶ���
		initPopContent();
		mPart = findViewById(R.id.part);
		mImageView.setOnClickListener(this);

		// ������Ϣ��ʼ��
		mUpdateNameRelativeLayout = findViewById(R.id.update_name_RelativeLayout);
		mUpdateNameRelativeLayout.setOnClickListener(this);
		mNameTextView = (TextView) findViewById(R.id.update_name_textView);
		mNameTextView.setText(name);
		// ��ʱ��
		mUpdateTimeRelativeLayout = findViewById(R.id.update_time_RelativeLayout);
		mUpdateTimeRelativeLayout.setOnClickListener(this);
		mTimeTextView = (TextView) findViewById(R.id.update_time_textView);
		// ת����ʽ
		String dateString = DateFormat.format("EEEE,MMM dd,yyyy", mDate)
				.toString();
		mTimeTextView.setText(dateString);
		// ���
		mUpdateIntroRelativeLayout = findViewById(R.id.update_intro_RelativeLayout);
		mUpdateIntroRelativeLayout.setOnClickListener(this);
		mIntroTextView = (TextView) findViewById(R.id.update_intro_textView);
		mIntroTextView.setText(intro);

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		String nameString = mEditText.getText().toString();
		if (!nameString.equals(mDevice.getDeviceName())) { // ������ݸ�����
			if (mDmg == null)
				mDmg = new DBManager(UpdateDeviceInfo.this);
			mDmg.updateName(mDeviceId, nameString);
			mDevice = mDmg.getDevice(mDeviceId);
			if (nameString != "") {
				mNameTextView.setText(nameString);
			}
		}
	}

	/**
	 * ��ʼ��PopContent����Ŀؼ�
	 */
	private void initPopContent() {
		mTakePhotoButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_camera);
		mSelectPhotoButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_Photo);
		mCancelButton = (Button) mPopContent
				.findViewById(R.id.item_popupwindows_cancel);
		mTakePhotoButton.setOnClickListener(this);
		mSelectPhotoButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
	}

	/**
	 * ��ʼ��mPop
	 */
	public void initPop() {
		int width = mPart.getWidth();
		int height = LayoutParams.WRAP_CONTENT;
		mPop = new PopupWindow(mPopContent, width, height, true);

		// ע��Ҫ�������룬�������������������Ż��ô�����ʧ
		mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.updateImageView:
			Log.i(TAG, "updateImage");
			// ����ͼƬ
			if (mPop == null) {
				initPop();
			}
			if (!mPop.isShowing()) {
				mPop.showAsDropDown(mPart, 0, 0); // ��ʾ��������
			}
			break;
		case R.id.item_popupwindows_camera:
			// ����CameraActivity��
			Intent intent = new Intent(this, CameraActivity.class);
			intent.putExtra(ShowDevicesFragment.EXTRA_KEY_DEVICE_ID,
					mDevice.getDeviceId());
			startActivityForResult(intent, REQUES_TAKE_PHOTO);
			mPop.dismiss();
			break;
		case R.id.item_popupwindows_Photo:
			// ��ʽIntent
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, RESULT_LOAD_IMAGE);
			mPop.dismiss();
			break;
		case R.id.item_popupwindows_cancel:
			// ȡ����ʾ
			mPop.dismiss();
			break;
		case R.id.update_name_RelativeLayout:
			// ���������޸Ķ�Ӧ�豸����
			mEditText = new EditText(this);
			new AlertDialog.Builder(this).setView(mEditText)
					.setPositiveButton("����", this)
					.setNegativeButton("ȡ��", null).show();
			break;
		case R.id.update_time_RelativeLayout:
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDate);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			date = new Date(mDate.getTime());
			View view = getLayoutInflater().inflate(R.layout.dialog_date, null);
			DatePicker datePicker = (DatePicker) view
					.findViewById(R.id.dialog_date);
			datePicker.init(year, month, day, new OnDateChangedListener() {
				// �����ڷ����ı�ʱ�����䱣����Arguments�У�����������ʱ������Ҳ���ᷢ���ı�
				public void onDateChanged(DatePicker view, int year, int month,
						int day) {
					date = new GregorianCalendar(year, month, day).getTime();
				}
			});
			new AlertDialog.Builder(this)
					.setTitle("��ʱ��")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									mDate.setTime(date.getTime());
									// �������ݿ�
									mDmg.updateTime(mDeviceId, mDate);
									// ����UI
									String dateString = DateFormat.format(
											"EEEE,MMM dd,yyyy", mDate)
											.toString();
									mTimeTextView.setText(dateString);
								}

							}).setView(view).show();
			Log.v(TAG, "date was clicked!");
			break;
		case R.id.update_intro_RelativeLayout:
			final EditText editText = new EditText(this);
			new AlertDialog.Builder(this)
					.setView(editText)
					.setPositiveButton("����",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String introString = editText.getText()
											.toString();
									if (!introString.equals(mDevice
											.getDeviceIntro())) { // ������ݸ�����
										if (mDmg == null)
											mDmg = new DBManager(
													UpdateDeviceInfo.this);
										mDmg.updateIntro(mDeviceId, introString);
										mDevice = mDmg.getDevice(mDeviceId);
										if (introString != "") {
											mIntroTextView.setText(introString);
										}
									}
								}
							}).setNegativeButton("ȡ��", null).show();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(this) != null) {
				NavUtils.navigateUpFromSameTask(this);
			}
			finish();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == RESULT_LOAD_IMAGE && data != null) {

			Uri uri = data.getData(); // ��ѡ��ͼƬ��uri
			String[] proj = { MediaStore.Images.Media.DATA };
			@SuppressWarnings("deprecation")
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			if (mDmg == null) {
				mDmg = new DBManager(this);
			}
			Log.v(TAG, "img_path:" + img_path);
			mDmg.updatePhoto(mDeviceId, img_path);

		}

		// ���µ�ǰҳ��
		if (mDmg == null) {
			mDmg = new DBManager(this);
		}
		mDevice = mDmg.getDevice(mDeviceId); // ʵ������ǰ�豸
		String photo = mDevice.getDevicePhoto();
		if (photo != null) { // ����ж�Ӧ��ͼƬ����ʾͼƬ
			File photoFile = mDevice.getPhotoFile();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true; // ������bitmap���󣬷������أ����ǿ�����������߲�ѯλͼ
			BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
			options.inSampleSize = 4; // ͼΪԭͼ����1/4
			options.inJustDecodeBounds = false;
			Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),
					options);
			Bitmap rbm = PictureUtils.toRoundBitmap(bm); // ��ͼƬת����Բ��
			mImageView.setImageBitmap(rbm); // ����ͼƬ
		}
	}

	@Override
	public void onDestroy() {
		mDmg.destroyDB();
		super.onDestroy();
	}

}
