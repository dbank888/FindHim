package com.example.findhim;

import com.astuetz.PagerSlidingTabStrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;

/**
 * ʹ��ViewPager��Ŷ��Fragment,ʵ�����һ����л�Fragment
 */
public class MainActivity extends FragmentActivity {
	protected static final String TAG = "MainActivity";
	private PagerSlidingTabStrip mTabs; // ����Tabs
	private ViewPager mViewPager; // ���Fragment������
	private ShowTrackFragment mShowTrackFragment; // �����켣
	private ShowDevicesFragment mShowDevicesFragment; // �������е��豸��Ϣ
	private DisplayMetrics mDm; // ��ǰ��Ļ���ܶ�
	private MyPagerAdapter mAdapter; // ������

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDm = getResources().getDisplayMetrics(); // ��ȡ��ǰ��Ļ���ܶ�

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter); // ��ViewPager������
		mTabs.setViewPager(mViewPager); // ��tabs��viewPager��ϵ����
		setTabsValue(); // ��ʼ��tabs����
	}

	/**
	 * ��PagerSlidingTabStrip�ĸ������Խ��и�ֵ��
	 */
	private void setTabsValue() {
		// ����Tab���Զ��������Ļ��
		mTabs.setShouldExpand(true);
		// ����Tab�ķָ�����͸����
		mTabs.setDividerColor(Color.TRANSPARENT);
		// ����Tab�ײ��ߵĸ߶�
		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, mDm));
		// ����Tab Indicator�ĸ߶�
		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, mDm));
		// ����Tab�������ֵĴ�С
		mTabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, mDm));
		// ����Tab ����������ɫ
		mTabs.setIndicatorColor(Color.parseColor("#45c01a"));
		// ����ѡ��Tab���ֵ���ɫ (�������Զ����һ������)
		mTabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		// ȡ�����Tabʱ�ı���ɫ
		mTabs.setTabBackground(0);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MyPagerAdapter";
		private final String[] titles = { "�켣", "�豸" };

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: // ��ʾ�����켣ҳ��
				if (mShowTrackFragment == null) {
					mShowTrackFragment = new ShowTrackFragment();
				}
				return mShowTrackFragment;
			case 1: // ��ʾ�����豸ҳ��
				if (mShowDevicesFragment == null) {
					mShowDevicesFragment = new ShowDevicesFragment();
				}
				return mShowDevicesFragment;

			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return titles.length;
		}

	}

	/* �����豸�������������Ӧ */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * keyCode: �����µļ�ֵ�������� event: �����¼��Ķ������а��������¼�����ϸ��Ϣ�����¼�����ʱ��ȡ�
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(MainActivity.this).setTitle("ϵͳ��ʾ")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("ȷ���˳�����")
					.setPositiveButton("ȷ��", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { //
							finish();
						}
					}).setNegativeButton("ȡ��", null).show();
		}
		return super.onKeyDown(keyCode, event);
		// false��ʾδ������¼�����Ӧ�ü������� �൱�� return super.
		// true��ʾ��������¼��������������
	}

}
