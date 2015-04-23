package com.example.findhim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.example.model.Device;
import com.scott.db.DBManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class CameraActivity extends Activity {

	protected static final String TAG = "CameraActivity";
	private SurfaceView mSurfaceView;
	private Camera mCamera;
	private View mProgressContainer;
	private OrientationEventListener mOrEventListener; // �豸���������
	private Boolean mIsLandscape; // �Ƿ����
	private Device mDevice;
	private DBManager mDmg;

	// ͼ�����ݻ�δ�������ʱ�Ļص�����
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

		public void onShutter() {
			// �ǽ������ɼ�
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};
	// JPEG�汾ͼ�����ʱ�Ļص�����
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// �����ݱ����ڱ��أ�������Activity
			String fileName = mDevice.getDeviceId() + ".jpg";
			FileOutputStream out = null;
			Bitmap oldBitmap = null;
			Bitmap newBitmap = null;
			boolean success = true;
			boolean landscape = false;
			try {
				File rootFile = new File(getFilesDir(), "/images");
				if (!rootFile.exists()) {
					Log.v(TAG, "��ǰĿ¼�����ڣ�������");

					if (rootFile.mkdirs()) { // �����༶Ŀ¼
						Log.v(TAG, "�����ɹ�");
					}
				}
				File photoFile = new File(rootFile, fileName);
				out = new FileOutputStream(photoFile);

				if (!mIsLandscape) { // ����������գ���תͼƬ�󱣴�
					landscape = true;
					oldBitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					Matrix matrix = new Matrix();
					matrix.setRotate(90); // ͼƬ��ת90��
					newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0,
							oldBitmap.getWidth(), oldBitmap.getHeight(),
							matrix, true);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					newBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
					byte[] newData = baos.toByteArray();
					out.write(newData);
				} else {
					Log.i(TAG, "ֱ�ӱ���ͼƬ");
					out.write(data); // ����ͼƬ
				}
			} catch (FileNotFoundException e) {
				success = false;
				e.printStackTrace();

			} catch (IOException e) {
				success = false;
				e.printStackTrace();
			} finally {
				if (landscape) { // ����������ĵģ������ʹ��ͼƬ��Դ
					if (!oldBitmap.isRecycled()) {
						oldBitmap.recycle();
						oldBitmap = null;
					}
					if (!newBitmap.isRecycled()) {
						newBitmap.recycle();
						newBitmap = null;
					}
				}
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (success) { // ���ͼƬ����ɹ�
				File rootFile = new File(getFilesDir(), "/images");
				File photoFile = new File(rootFile, mDevice.getDeviceId()
						+ ".jpg");
				mDmg.updatePhoto(mDevice.getDeviceId(),
						photoFile.getAbsolutePath());
			}
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startOrientationChangeListener();
		setContentView(R.layout.activity_camera);
		String deviceId = getIntent().getStringExtra(
				ShowDevicesFragment.EXTRA_KEY_DEVICE_ID);
		mDmg = new DBManager(this);
		mDevice = mDmg.getDevice(deviceId); // ʵ������ǰ�豸

		mProgressContainer = findViewById(R.id.crime_camera_progressContainer);
		// ��ʼ�����������ɼ�
		mProgressContainer.setVisibility(View.INVISIBLE);
		Button takePictureButton = (Button) findViewById(R.id.crime_camera_takePictureButton);

		takePictureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// ��׽���ͼ��
				mCamera.takePicture(mShutterCallback, null, mJpegCallback);
			}
		});
		mSurfaceView = (SurfaceView) findViewById(R.id.crime_camera_surfaceView);
		SurfaceHolder holder = mSurfaceView.getHolder(); // SurfaceView�Ŀ�����
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // ���û���
		// ��Surface�������ٺ󣬱��뱣֤û���κ�����Ҫ��Surface�Ļ���������
		// ���ԣ���Ҫʵ��SurfaceHolder.Callback�ӿ�
		holder.addCallback(new SurfaceHolder.Callback() {

			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mCamera != null) {
					mCamera.stopPreview();// ��Surface���Ƴ�mCamera
				}
			}

			public void surfaceCreated(SurfaceHolder holder) {

				try {
					if (mCamera != null)
						mCamera.setPreviewDisplay(holder);
					// ����mCamera��Surface
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				// ��ʼ��mCamera
				// ��Surface�״�����Ļ����ʾ��ʱ����ô˷���������surface�ĳߴ��С�����ı�ʱ��
				Parameters parameters = mCamera.getParameters();
				// ��ȡmCamera�Ĳ�������
				Size s = getBestSupportedSize(parameters
						.getSupportedPreviewSizes());
				// ����Ԥ��ͼƬ�ߴ�
				parameters.setPreviewSize(s.width, s.height);
				// ���ò�׽ͼƬ�ߴ�
				s = getBestSupportedSize(parameters.getSupportedPictureSizes());
				parameters.setPictureSize(s.width, s.height);
				mCamera.setParameters(parameters);

				try {
					mCamera.startPreview();
				} catch (Exception e) {
					if (mCamera != null) {
						mCamera.release();
						mCamera = null;
					}
				}
			}

			private Size getBestSupportedSize(List<Size> sizes) {
				// ȡ�����õ�����SIZE
				Size bestSize = sizes.get(0);
				int largestArea = sizes.get(0).height * sizes.get(0).width;
				for (Size s : sizes) {
					int area = s.width * s.height;
					if (area > largestArea) {
						largestArea = area;
						bestSize = s;
					}
				}
				return bestSize;
			}
		});
	}

	private final void startOrientationChangeListener() { // �豸�������
		mOrEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int rotation) {
				if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)
						|| ((rotation >= 135) && (rotation <= 225))) {// portrait
					mIsLandscape = false;
					Log.i(TAG, "����");
				} else if (((rotation > 45) && (rotation < 135))
						|| ((rotation > 225) && (rotation < 315))) {// landscape
					mIsLandscape = true;
					Log.i(TAG, "����");
				}
			}
		};
		mOrEventListener.enable();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onResume() {
		super.onResume();
		// ����Camera
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(0);
			// open(i) since GINGERBREAD
			// i=0 ��ʾ�������

		} else
			mCamera = Camera.open();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause �ͷ����");
		mOrEventListener.disable();
		// �ͷ����
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onDestroy() {
		mDmg.destroyDB();
		super.onDestroy();
	}

}
