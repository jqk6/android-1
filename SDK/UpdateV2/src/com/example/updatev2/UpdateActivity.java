package com.example.updatev2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import btManager.Pos;
import btManager.UpdateThread;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends Activity {

	private Button button01;
	private Button button02;
	private Button button04;
	private Button button05;
	private Button btSelectFile;
	private Button btSet;
	private Button btSelectFont;
	private Button btUpdateFont;
	private LinearLayout linearLayout01; // ��Ӳ���
	private ProgressBar porgressBar01;
	private TextView textView01;
	private BluetoothAdapter myBTAdapter;
	private boolean autoDisconnectedSearch;
	private boolean autoPairing;
	private static String mainDir = "/printer/";
	private static String defaultFirmDir = "/printer/program/";
	private static String defaultFontDir = "/printer/font/";
	private static String programPath = "";
	private static String fontPath = "";
	private static final String PREFERENCES_FILE = "PREFERENCES_FILE";
	private SharedPreferences mSharedPreferences;
	/**
	 * ϵͳ���� ���к�
	 */
	public static String[] SystemInfo = new String[] { "MyPrinter", "0000" };

	/**
	 * �������� ��������
	 */
	public static String[] btParams = new String[] { "MyPrinter", "0000" };

	/**
	 * ���ڲ����� ����ҳ Ũ�� Ĭ������ ���б�� ���еȴ�ʱ�� �Զ��ػ�ʱ�� ��ֽ�������� �ڱ�Ѱ��������
	 */
	public static String[] otherParams = new String[] { "115200",
			Pos.Constant.strcodepages[0], Pos.Constant.strdarkness[1],
			Pos.Constant.strdefaultfont[0], Pos.Constant.strlinefeed[0],
			"1800", "3600", "200", "100" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);

		// preference
		mSharedPreferences = this.getSharedPreferences(PREFERENCES_FILE, 0);
		programPath = mSharedPreferences.getString("programPath", "");
		fontPath = mSharedPreferences.getString("fontPath", "");

		if (programPath != "")
			Toast.makeText(getApplicationContext(), "�ϴ�ѡ��ĳ����ļ���" + programPath,
					Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(getApplicationContext(), "����������������ѡ�����",
					Toast.LENGTH_SHORT).show();
			// setPath(defaultFirmDir);
		}
		if (fontPath != "")
			Toast.makeText(getApplicationContext(), "�ϴ�ѡ��������ļ���" + fontPath,
					Toast.LENGTH_SHORT).show();
		else {
			Toast.makeText(getApplicationContext(), "�����������壬����ѡ������",
					Toast.LENGTH_SHORT).show();
			// setFont(defaultFontDir);
		}
		button01 = (Button) findViewById(R.id.button1);
		button02 = (Button) findViewById(R.id.button2);
		button04 = (Button) findViewById(R.id.button4);
		button05 = (Button) findViewById(R.id.button5);
		btSelectFile = (Button) findViewById(R.id.btselectfile);
		btSet = (Button) findViewById(R.id.btset);
		btSelectFont = (Button) findViewById(R.id.btselectfont);
		btUpdateFont = (Button) findViewById(R.id.btUpdateFont);
		linearLayout01 = (LinearLayout) findViewById(R.id.linearLayout01);
		porgressBar01 = (ProgressBar) findViewById(R.id.progressBar1);
		textView01 = (TextView) findViewById(R.id.textView1);
		autoPairing = false;
		autoDisconnectedSearch = false;
		Pos.POS_AutoPairing(autoPairing, "0000");
		Pos.APP_Init(getApplicationContext());
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					Toast.makeText(getApplicationContext(), "�������",
							Toast.LENGTH_SHORT).show();
					if (!Pos.POS_Connecting()) {
						textView01.setText("");
						porgressBar01.setVisibility(View.INVISIBLE);
						button01.setEnabled(true);
					}
				} else if (action
						.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
					linearLayout01.removeAllViews();
					textView01.setText("��������");
					porgressBar01.setVisibility(View.VISIBLE);
					button01.setEnabled(false);
				} else if (action
						.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {

				} else if (action.equals(Pos.ACTION_DISCONNECTED)) {
					Pos.POS_Close();
					Toast.makeText(getApplicationContext(), "���ӶϿ�",
							Toast.LENGTH_SHORT).show();
					textView01.setText("");
					textView01.setTextColor(Color.BLUE);
					porgressBar01.setVisibility(View.INVISIBLE);
					button01.setEnabled(true);
					button02.setEnabled(false);
					button04.setEnabled(false);
					btSet.setEnabled(false);
					btUpdateFont.setEnabled(false);
					button05.setEnabled(false);
					if (autoDisconnectedSearch) {
						if (myBTAdapter != null)
							if (!myBTAdapter.isDiscovering())
								myBTAdapter.startDiscovery();
					}
				} else if (action.equals(Pos.ACTION_CONNECTED)) {
					textView01.setText("");
					porgressBar01.setVisibility(View.INVISIBLE);
					textView01.setTextColor(Color.RED);
					textView01.setText("�Ѿ�����");
					porgressBar01.setVisibility(View.VISIBLE);
					button01.setEnabled(false);
					button02.setEnabled(true);
					button04.setEnabled(true);
					btSet.setEnabled(true);
					btUpdateFont.setEnabled(true);
					button05.setEnabled(true);
				} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					// ���ﲻ��ֻ��һ����ť��Ӧ��Ҫ���ÿ�һ�����ͼ
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (device == null)
						return;
					final String address = device.getAddress();
					String name = device.getName();
					if (name == null)
						name = "�����豸";
					else if (name.equals(address))
						name = "�����豸";
					Button button = new Button(context);
					button.setText(name + ": " + address);
					button.setGravity(android.view.Gravity.CENTER_VERTICAL
							| Gravity.LEFT);
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							// ֻ��û��������û�����ã�������ܸı�״̬
							Pos.POS_Open(address);
						}
					});
					button.getBackground().setAlpha(100);
					linearLayout01.addView(button);
				} else if (action
						.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

				} else if (action.equals(Pos.ACTION_BEGINCONNECTING)) {
					textView01.setText("");
					porgressBar01.setVisibility(View.INVISIBLE);
					button01.setEnabled(false);
					button02.setEnabled(false);
					button04.setEnabled(false);
					btSet.setEnabled(false);
					btUpdateFont.setEnabled(false);
					button05.setEnabled(false);
					textView01.setText("��������");
					porgressBar01.setVisibility(View.VISIBLE);
				} else if (action.equals(Pos.ACTION_CONNECTINGFAILED)) {
					Toast.makeText(getApplicationContext(), "����ʧ��",
							Toast.LENGTH_SHORT).show();
					textView01.setText("");
					porgressBar01.setVisibility(View.INVISIBLE);
					button01.setEnabled(true);
				} else if (action.equals(UpdateThread.ACTION_DEBUGINFO)) {
					textView01.setText(intent
							.getStringExtra(UpdateThread.EXTRA_DEBUGINFO));
				} else if (action.equals(UpdateThread.ACTION_ENDUPDATE)) {
					if (Pos.Connected) {
						button01.setEnabled(false);
						button02.setEnabled(true);
						button04.setEnabled(true);
						btSet.setEnabled(true);
						btUpdateFont.setEnabled(true);
						button05.setEnabled(true);
					} else {
						button01.setEnabled(true);
						button02.setEnabled(false);
						button04.setEnabled(false);
						btSet.setEnabled(false);
						btUpdateFont.setEnabled(false);
						button05.setEnabled(false);
					}
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		intentFilter.addAction(Pos.ACTION_DISCONNECTED);
		intentFilter.addAction(Pos.ACTION_CONNECTED);
		intentFilter.addAction(Pos.ACTION_BEGINCONNECTING);
		intentFilter.addAction(Pos.ACTION_CONNECTINGFAILED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(UpdateThread.ACTION_DEBUGINFO);
		intentFilter.addAction(UpdateThread.ACTION_ENDUPDATE);
		this.registerReceiver(broadcastReceiver, intentFilter);

		button01.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (myBTAdapter != null)
					if (!myBTAdapter.isDiscovering())
						myBTAdapter.startDiscovery();
			}
		});

		button02.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Pos.POS_Close();
			}

		});

		button04.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				button01.setEnabled(false);
				button02.setEnabled(false);
				button04.setEnabled(false);
				btSet.setEnabled(false);
				btUpdateFont.setEnabled(false);
				button05.setEnabled(false);
				Pos.POS_Update(programPath);
			}

		});

		button05.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				printTest();
			}

		});

		btSelectFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setPath(defaultFirmDir);
			}

		});

		btSelectFont.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setFont(defaultFontDir);
			}

		});

		btSet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("onClick", "onClick");
				btSetParams();
			}

		});

		btUpdateFont.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				button01.setEnabled(false);
				button02.setEnabled(false);
				button04.setEnabled(false);
				btSet.setEnabled(false);
				btUpdateFont.setEnabled(false);
				button05.setEnabled(false);
				Pos.POS_FontUpdateMutiPackage(fontPath);
			}

		});

		button02.setEnabled(false);
		button04.setEnabled(false);
		btSet.setEnabled(false);
		btUpdateFont.setEnabled(false);
		button05.setEnabled(false);

		File dir = new File(Environment.getExternalStorageDirectory() + mainDir);
		if (!dir.exists())
			dir.mkdir();
		dir = new File(Environment.getExternalStorageDirectory()
				+ defaultFirmDir);
		if (!dir.exists())
			dir.mkdir();
		dir = new File(Environment.getExternalStorageDirectory()
				+ defaultFontDir);
		if (!dir.exists())
			dir.mkdir();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd",
				Locale.CHINA);
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		SystemInfo[1] = formatter.format(curDate);
	}

	@Override
	protected void onResume() {
		super.onResume();
		myBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!myBTAdapter.isEnabled()) {
			myBTAdapter.enable();
		}
		if (myBTAdapter.isDiscovering()) {
			myBTAdapter.cancelDiscovery();
		}
		if (!Pos.Connecting && Pos.Connected) {
			button01.setEnabled(false);
			button02.setEnabled(true);
			button04.setEnabled(true);
			btSet.setEnabled(true);
			btUpdateFont.setEnabled(true);
			button05.setEnabled(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// �����˳��Ի���
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// ���öԻ������
			isExit.setTitle("ϵͳ��ʾ");
			// ���öԻ�����Ϣ
			isExit.setMessage("Ҫ�˳�������");
			// ���ѡ��ť��ע�����
			isExit.setButton(DialogInterface.BUTTON_POSITIVE, "�˳�", listener);
			isExit.setButton(DialogInterface.BUTTON_NEGATIVE, "ȡ��", listener);
			// ��ʾ�Ի���
			isExit.show();

		}
		return false;

	}

	/** �����Ի��������button����¼� */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����
				System.exit(0);
				break;
			case DialogInterface.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		Pos.APP_UnInit();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// menu.add(0, 8, 0, "���������豸");
		// menu.add(0, 9, 0, autoDisconnectedSearch ? "ȡ�����ӶϿ�ʱ���Զ�����"
		// : "����������ӶϿ�ʱ�Զ�����");
		// menu.add(0, 10, 0, autoPairing ? "ȡ���Զ����" : "��������Զ����");
		// menu.add(0, 11, 0, "ѡ�������̼�");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case 8:
			Toast.makeText(getApplicationContext(), "����ȡ���������",
					Toast.LENGTH_SHORT).show();
			new ClearPairingDevices().start();
			Toast.makeText(getApplicationContext(), "���ȡ�����",
					Toast.LENGTH_SHORT).show();
			return true;
		case 9:
			if (autoDisconnectedSearch) {
				item.setTitle("����������ӶϿ�ʱ�Զ�����");
			} else {
				item.setTitle("ȡ�����ӶϿ�ʱ���Զ�����");
			}
			autoDisconnectedSearch = !autoDisconnectedSearch;
			return true;

		case 10:
			if (autoPairing) {
				item.setTitle("��������Զ����");
			} else {
				item.setTitle("ȡ���Զ����");
			}
			autoPairing = !autoPairing;
			Pos.POS_AutoPairing(autoPairing, "0000");
			return true;
		case 11:
			setPath(defaultFirmDir);
			return true;
		default:
			return true;
		}
	}

	private void printTest() {
		Time mTime = new Time();
		mTime.setToNow();
		Pos.POS_Reset();
		Pos.POS_S_TextOut("����ҳ", 156, 0, 0, 0, 0x80);
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("Width = 2; Height = 2;", 0, 1, 1, 0, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("BlackWhiteReverse", 0, 0, 0, 0, 0x400);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("small font", 0, 0, 0, 1, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("UPC-A", 0, 0, 0, 0, 0);
		Pos.POS_FeedLine();
		Pos.POS_S_SetBarcode("01234567890", 0, 0x41, 3, 100, 0x00, 0x02);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut(mTime.format("%Y-%m-%d %H:%M:%S"), 0, 0, 0, 0, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
	}

	private List<String> getData(String subdir) {

		File dir = new File(Environment.getExternalStorageDirectory() + subdir);

		return new ListFiles().getFiles(dir, ".bin");

	}

	private void setPath(String subdir) {
		final List<String> data = getData(subdir);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("��ѡ���ļ���").setSingleChoiceItems(
				new ArrayAdapter<String>(this, R.layout.textview, data), -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						programPath = data.get(which);
						SharedPreferences.Editor editor = UpdateActivity.this
								.getSharedPreferences(PREFERENCES_FILE, 0)
								.edit();
						editor.putString("programPath", programPath);
						editor.commit();
						Toast.makeText(UpdateActivity.this,
								"ѡ�����ļ���" + programPath, Toast.LENGTH_SHORT)
								.show();
						/**
						 * �ֶ����÷��ؼ� onKeyDown(KeyEvent.KEYCODE_BACK, null);
						 */
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	private void setFont(String subdir) {
		final List<String> data = getData(subdir);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("��ѡ���ļ���").setSingleChoiceItems(
				new ArrayAdapter<String>(this, R.layout.textview, data), -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						fontPath = data.get(which);
						SharedPreferences.Editor editor = UpdateActivity.this
								.getSharedPreferences(PREFERENCES_FILE, 0)
								.edit();
						editor.putString("fontPath", fontPath);
						editor.commit();
						Toast.makeText(UpdateActivity.this,
								"ѡ�����ļ���" + fontPath, Toast.LENGTH_SHORT).show();
						/**
						 * �ֶ����÷��ؼ� onKeyDown(KeyEvent.KEYCODE_BACK, null);
						 */
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	class ClearPairingDevices extends Thread {
		@Override
		public void run() {
			Pos.POS_RemoveBondedDevices();
		}
	}

	/**
	 * ��ʾ�����ڶ�ȡ��ӡ������ ��ȡ���ת������ҳ�� ��������������
	 */
	void btSetParams() {
		readParams();
		Intent intent = new Intent(UpdateActivity.this, SetParamsActivity.class);
		startActivity(intent);
	}

	/**
	 * ��ȡ��ӡ�������������btParams, otherParams�ֶ�
	 */
	void readParams() {

	}

	/**
	 * ���ã������ο�
	 */
	void btSetOnClickHandlerBefore() {
		LayoutInflater factory = LayoutInflater.from(UpdateActivity.this);
		View view = factory.inflate(R.layout.setparam, null);
		Button btSetParam = (Button) view.findViewById(R.id.btsetparam);
		final EditText etBtName = (EditText) view.findViewById(R.id.etBtName);
		etBtName.setText(mSharedPreferences.getString("etBtName", "setname"));
		final EditText etBtPwd = (EditText) view.findViewById(R.id.etpwd);
		etBtPwd.setText(mSharedPreferences.getString("etBtPwd", "0000"));

		final EditText etBaudrate = (EditText) view
				.findViewById(R.id.etBaudrate);
		etBaudrate.setText(mSharedPreferences.getString("etBaudrate", "9600"));
		final CheckBox cbBaudrate = (CheckBox) view
				.findViewById(R.id.cbBaudrate);
		final CheckBox cbBt = (CheckBox) view.findViewById(R.id.cbBt);
		final EditText etLanguage = (EditText) view
				.findViewById(R.id.etLanguage);
		etLanguage.setText(mSharedPreferences.getString("etLanguage", "255"));
		final EditText etConcertration = (EditText) view
				.findViewById(R.id.etConcertration);
		etConcertration.setText(mSharedPreferences.getString("etConcertration",
				"2"));
		final EditText etDefaultfont = (EditText) view
				.findViewById(R.id.etDefaultfont);
		etDefaultfont.setText(mSharedPreferences
				.getString("etDefaultfont", "0"));
		final EditText etLfcr = (EditText) view.findViewById(R.id.etLfcr);
		etLfcr.setText(mSharedPreferences.getString("etLfcr", "0"));
		final EditText etIdletime = (EditText) view
				.findViewById(R.id.etIdletime);
		etIdletime.setText(mSharedPreferences.getString("etIdletime", "600"));
		final EditText etPwofftime = (EditText) view
				.findViewById(R.id.etPwofftime);
		etPwofftime
				.setText(mSharedPreferences.getString("etPwofftime", "3600"));
		final EditText etMaxfeedlength = (EditText) view
				.findViewById(R.id.etMaxfeedlength);
		etMaxfeedlength.setText(mSharedPreferences.getString("etMaxfeedlength",
				"300"));
		final EditText etBlackmarklength = (EditText) view
				.findViewById(R.id.etBlackmarklength);
		etBlackmarklength.setText(mSharedPreferences.getString(
				"etBlackmarklength", "200"));

		AlertDialog.Builder builder = new AlertDialog.Builder(
				UpdateActivity.this);
		builder.setTitle("���ô�ӡ��").setView(view);
		final AlertDialog dialog = builder.create();

		btSetParam.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (cbBt.isChecked()) {
					String strname = etBtName.getText().toString();
					String strpwd = etBtPwd.getText().toString();
					byte[] name = strname.getBytes();
					byte[] pwd = strpwd.getBytes();

					if (pwd.length > 4 || name.length > 12) {
						Toast.makeText(getApplicationContext(),
								"���ֲ��ܳ���12���ֽ�.����Ϊ4���ֽ�", Toast.LENGTH_SHORT)
								.show();
					} else if (Pos.POS_Connected()) {
						Pos.POS_SetBluetooth(name, pwd);
						SharedPreferences.Editor editor = UpdateActivity.this
								.getSharedPreferences(PREFERENCES_FILE, 0)
								.edit();
						editor.putString("etBtName", strname);
						editor.putString("etBtPwd", strpwd);
						editor.commit();
					}
				}

				if (cbBaudrate.isChecked()) {
					try {

						String strbaudrate = etBaudrate.getText().toString()
								.trim();
						int nBaudrate = Integer.parseInt(strbaudrate);
						if ((nBaudrate != 9600) && (nBaudrate != 19200)
								&& (nBaudrate != 38400) && (nBaudrate != 57600)
								&& (nBaudrate != 115200))
							throw new Exception(
									"������һ��Ϊ9600��19200��38400��57600��115200���е�һ������������ȷ��ֵ��");
						Pos.Pro.setPrintParam[12] = (byte) ((nBaudrate & 0xff0000) >> 24);
						Pos.Pro.setPrintParam[13] = (byte) ((nBaudrate & 0xff0000) >> 16);
						Pos.Pro.setPrintParam[14] = (byte) ((nBaudrate & 0xff00) >> 8);
						Pos.Pro.setPrintParam[15] = (byte) (nBaudrate & 0xff);

						String strlanguage = etLanguage.getText().toString()
								.trim();
						int nLanguage = Integer.parseInt(strlanguage);
						if ((nLanguage > 255) || (nLanguage < 0))
							throw new Exception(
									"���Ĵ���ҳΪ255�������Ҫ���ó����ģ�ֻ����language��������255���ɡ�����ϸ�����ã���μ�������");
						Pos.Pro.setPrintParam[16] = (byte) (nLanguage & 0xff);

						String strconcertration = etConcertration.getText()
								.toString().trim();
						int nConcertration = Integer.parseInt(strconcertration);
						if (nConcertration < 0 || nConcertration > 2)
							throw new Exception(
									"concertration �������Ũ�ȡ���ѡֵΪ0��1��2(2����Ũ)��");
						Pos.Pro.setPrintParam[17] = (byte) (nConcertration & 0xff);

						String strdefaultfont = etDefaultfont.getText()
								.toString().trim();
						int nDefaultfont = Integer.parseInt(strdefaultfont);
						if (nDefaultfont < 0 || nDefaultfont > 4)
							throw new Exception(
									"Ĭ�������ѡֵΪ0��1��2��3��4��\n0--12x24\n1--9x24\n2--9x17\n3--8x16\n4--16x18");
						Pos.Pro.setPrintParam[18] = (byte) (nDefaultfont & 0xff);

						String strlfcr = etLfcr.getText().toString().trim();
						int nLfcr = Integer.parseInt(strlfcr);
						if ((nLfcr < 0) || (nLfcr > 1))
							throw new Exception(
									"���������ѡֵΪ0��1��\n0---0A����\n1--0D����");
						Pos.Pro.setPrintParam[19] = (byte) (nLfcr & 0xff);

						String stridletime = etIdletime.getText().toString()
								.trim();
						int nIdletime = Integer.parseInt(stridletime);
						if ((nIdletime < 0) || (nIdletime > 65535))
							throw new Exception("���еȴ�ʱ���ѡֵΪ0-65535.(��λ��)");
						Pos.Pro.setPrintParam[20] = (byte) ((nIdletime & 0xff00) >> 8);
						Pos.Pro.setPrintParam[21] = (byte) (nIdletime & 0xff);

						String strpwofftime = etPwofftime.getText().toString()
								.trim();
						int nPwofftime = Integer.parseInt(strpwofftime);
						if ((nPwofftime < 0) || (nPwofftime > 65535))
							throw new Exception("�Զ��ػ�ʱ���ѡֵΪ0-65535.(��λ��)");
						Pos.Pro.setPrintParam[22] = (byte) ((nPwofftime & 0xff00) >> 8);
						Pos.Pro.setPrintParam[23] = (byte) (nPwofftime & 0xff);

						String strmaxfeedlength = etMaxfeedlength.getText()
								.toString().trim();
						int nMaxfeedlength = Integer.parseInt(strmaxfeedlength);
						if ((nMaxfeedlength < 0) || (nMaxfeedlength > 65535))
							throw new Exception("��ֽ���������ֽ�����ѡֵΪ0-65535.(��λ����)");
						Pos.Pro.setPrintParam[24] = (byte) ((nMaxfeedlength & 0xff) >> 8);
						Pos.Pro.setPrintParam[25] = (byte) (nMaxfeedlength & 0xff);

						String strblackmarklength = etBlackmarklength.getText()
								.toString().trim();
						int nBlackmarklength = Integer
								.parseInt(strblackmarklength);
						if ((nBlackmarklength < 0)
								|| (nBlackmarklength > 65535))
							throw new Exception("�ڱ����Ѱ�Ҿ����ѡֵΪ0-65535.(��λ����)");
						Pos.Pro.setPrintParam[26] = (byte) ((nBlackmarklength & 0xff) >> 8);
						Pos.Pro.setPrintParam[27] = (byte) (nBlackmarklength & 0xff);

						if (Pos.POS_Connected()) {
							Pos.POS_SetPrintParam(Pos.Pro.setPrintParam);

							SharedPreferences.Editor editor = UpdateActivity.this
									.getSharedPreferences(PREFERENCES_FILE, 0)
									.edit();
							editor.putString("etBaudrate", strbaudrate);
							editor.putString("etLanguage", strlanguage);
							editor.putString("etConcertration",
									strconcertration);
							editor.putString("etDefaultfont", strdefaultfont);
							editor.putString("etLfcr", strlfcr);
							editor.putString("etPwofftime", strpwofftime);
							editor.putString("etMaxfeedlength",
									strmaxfeedlength);
							editor.putString("etBlackmarklength",
									strblackmarklength);

							editor.commit();
						} else
							throw new Exception("���ӶϿ�������������");

					} catch (NumberFormatException e) {
						Toast.makeText(getApplicationContext(), "��ʽ����,�����������",
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getApplicationContext(), e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				}

				dialog.dismiss();

			}

		});

		dialog.show();
	}
}
