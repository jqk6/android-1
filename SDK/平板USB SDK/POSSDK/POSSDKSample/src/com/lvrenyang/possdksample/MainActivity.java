package com.lvrenyang.possdksample;

import java.util.HashMap;
import java.util.Iterator;

import com.lvrenyang.pos.Pos;
import com.lvrenyang.rw.*;
import com.lvrenyang.rw.TTYTermios.*;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Button buttonTestText, buttonTestBarCode, buttonTestQrCode,
			buttonTestPic;
	private Button buttonDisconnect, buttonConnect;
	private TextView textView1;

	private static USBSerialPort serialPort;
	private static PL2303Driver mSerial;
	private static Pos mPos;
	private static Context mContext;

	private BroadcastReceiver broadcastReceiver;

	private boolean debug_main = true;

	private static final String str1 = "abcdefghijklmnopqrstuvwxyz";
	private static final String strBarCode = "123456789012";
	private static final String strQrCode = "Hello, the beautiful world!";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView1 = (TextView) findViewById(R.id.textView1);
		buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
		buttonDisconnect.setOnClickListener(this);
		buttonDisconnect.setEnabled(false);
		buttonConnect = (Button) findViewById(R.id.buttonConnect);
		buttonConnect.setOnClickListener(this);
		buttonConnect.setEnabled(true);
		buttonTestText = (Button) findViewById(R.id.buttonTestText);
		buttonTestText.setOnClickListener(this);
		buttonTestBarCode = (Button) findViewById(R.id.buttonTestBarCode);
		buttonTestBarCode.setOnClickListener(this);
		buttonTestQrCode = (Button) findViewById(R.id.buttonTestQrCode);
		buttonTestQrCode.setOnClickListener(this);
		buttonTestPic = (Button) findViewById(R.id.buttonTestPic);
		buttonTestPic.setOnClickListener(this);

		mContext = getApplicationContext();
		mSerial = new PL2303Driver();
		serialPort = new USBSerialPort(null, null);
		mPos = new Pos(serialPort, mSerial);

		initBroadcast();
		handleIntent();
		debug_toast("onCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
					textView1.setText("");
					buttonDisconnect.callOnClick();

				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mContext.registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		mContext.unregisterReceiver(broadcastReceiver);
	}

	private void handleIntent() {

	}

	private void debug_toast(String msg) {
		if (debug_main)
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent();
		debug_toast("onNewIntent()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		debug_toast("onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disconnect();
		uninitBroadcast();
		debug_toast("onDestroy");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonConnect:
			connect();
			break;

		case R.id.buttonDisconnect:
			disconnect();
			break;

		case R.id.buttonTestText:
			/**
			 * usb��ӡ�����⴮�ڲ�����ֻ���������壬��������һ�������ԡ�
			 */
			open(115200, Parity.NONE);
			mPos.POS_S_TextOut(str1, 32, 0, 0, 0, 0x00 + 0x400);
			mPos.POS_FeedLine();
			mPos.POS_FeedLine();
			debug_toast("�����ı�");
			close();
			break;

		case R.id.buttonTestBarCode:
			open(115200, Parity.NONE);
			mPos.POS_S_SetBarcode(strBarCode, 32, 0x41, 3, 162, 0x00, 0x02);
			mPos.POS_FeedLine();
			mPos.POS_FeedLine();
			debug_toast("��������");
			close();
			break;

		case R.id.buttonTestQrCode:
			open(115200, Parity.NONE);
			mPos.POS_S_SetQRcode(strQrCode, 6, 4);
			mPos.POS_FeedLine();
			mPos.POS_FeedLine();
			debug_toast("���Զ�ά��");
			close();
			break;

		case R.id.buttonTestPic:
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.iu2);
			if (null == bitmap) {
				debug_toast("ͼƬ�������");
				break;
			}
			open(115200, Parity.NONE);
			/**
			 * ��ͼƬ����ʼλ����12�㴦
			 */
			mPos.POS_S_TextOut("", 12, 0, 0, 0, 0);
			/**
			 * ͼƬ�����ؿ�ȱ���Ϊ8��������
			 */
			mPos.POS_PrintPicture(bitmap, 360, 0);
			mPos.POS_FeedLine();
			mPos.POS_FeedLine();
			debug_toast("����ͼƬ");
			close();
			break;
		}
	}

	/**
	 * �򿪺͹رհ�ť��������������USB�ģ��򿪾���probe���رվ���disconnect
	 * probe�����port�ֶΣ�open�����termios�ֶ����򿪴���
	 * 
	 */

	private void connect() {
		final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		if (deviceList.size() > 0) {
			// ��ʼ��ѡ��Ի��򲼾֣�����Ӱ�ť���¼�
			LinearLayout llSelectDevice = new LinearLayout(this);
			llSelectDevice.setOrientation(LinearLayout.VERTICAL);
			llSelectDevice.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("ѡ���豸").setView(llSelectDevice);
			final AlertDialog dialog = builder.create();

			while (deviceIterator.hasNext()) { // ������if����while��˵����ֻ��֧��һ��device
				final UsbDevice device = deviceIterator.next();
				Button btDevice = new Button(llSelectDevice.getContext());
				btDevice.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				btDevice.setGravity(Gravity.LEFT);
				btDevice.setText("ID: " + device.getDeviceId());
				btDevice.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						PendingIntent mPermissionIntent = PendingIntent
								.getBroadcast(
										MainActivity.this,
										0,
										new Intent(
												MainActivity.this
														.getApplicationInfo().packageName),
										0);
						serialPort.port = new USBPort(mUsbManager,
								MainActivity.this, device, mPermissionIntent);
						int ret = mSerial.pl2303_probe(serialPort);
						if (ret == 0) {
							textView1.setText(System.currentTimeMillis() + ": "
									+ "���ӳɹ� " + mSerial.type + "\n");
							buttonConnect.setEnabled(false);
							buttonDisconnect.setEnabled(true);
						} else {
							textView1.setText(System.currentTimeMillis() + ": "
									+ "����ʧ��(" + ret + ")\n");
							buttonConnect.setEnabled(true);
							buttonDisconnect.setEnabled(false);
						}
					}
				});
				llSelectDevice.addView(btDevice);
			}
			if (llSelectDevice.getChildCount() == 1)
				llSelectDevice.getChildAt(0).callOnClick();
			else
				dialog.show();
		}
	}

	private void open(final int baudrate, final Parity parity) {

		serialPort.termios = new TTYTermios(baudrate, FlowControl.NONE, parity,
				StopBits.ONE, 8);
		int ret = mSerial.pl2303_open(serialPort);
		if (ret == 0)
			textView1.setText(System.currentTimeMillis() + ": " + "�򿪳ɹ�"
					+ serialPort.termios.baudrate + " "
					+ serialPort.termios.parity + "\n");
		else
			textView1.setText(System.currentTimeMillis() + ": " + "��ʧ��(" + ret
					+ ")\n");

	}

	private void close() {
		mSerial.pl2303_close(serialPort);
	}

	private void disconnect() {
		close();
		mSerial.pl2303_disconnect(serialPort);
		textView1.setText("");
		buttonConnect.setEnabled(true);
		buttonDisconnect.setEnabled(false);

	}
}
