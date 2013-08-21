package com.example.updatev2;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import btManager.Pos;
import btManager.UpdateThread;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class TextPrintActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private ScrollView scrollViewPageList;
	private Button btConnect, btInput, btSMS, btContact, btText, btNet;
	private ListView lvSms, lvContact, lvText;
	private List<Map<String, Object>> listTextData, listSmsData,
			listContactData;
	private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7;
	private TextView[] tv;
	private Button btSearch, btDisconnect;
	private TextView tvInfo;
	private ProgressBar pbar;
	private LinearLayout llDevices;
	private LinearLayout llConnect, llInput, llSms, llContact, llText, llNet;
	private LinearLayout[] ll;
	private WebView wv;
	private EditText etweb;
	private Button btweb, btPrintWeb;
	private Button btTextPrint, btRead, btCheckKey, btSetKey, btSetBarcode,
			btFontSize;
	private RadioButton rbAscii, rbHex, rbBarcode, rbQRcode;
	private EditText etTextPrint;
	private TextView tvRead;

	private BluetoothAdapter myBTAdapter;
	private static final String TITLE = "TITLE";
	private static final String INFO = "INFO";
	private static final String PATH = "PATH";
	private static final int SMS = 0x101;
	private static final int CONTACT = 0x102;
	private static final int TEXT = 0x103;
	private static final int NET = 0x104;
	private int selectedViewId = 0;

	/**
	 * 0x41-0x49
	 */
	public static int barcodeType = Pos.Constant.BARCODE_TYPE_UPC_A;
	public static int barcodeFontType = Pos.Constant.BARCODE_FONTTYPE_STANDARD;
	public static int barcodeFontPosition = Pos.Constant.BARCODE_FONTPOSITION_NO;
	public static int barcodeOrgx = 0;
	public static int barcodeWidthX = 2;
	public static int barcodeHeight = 160;

	private static int nFontSize = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mytextprinter);
		scrollViewPageList = (ScrollView) findViewById(R.id.scrollViewPageList);
		llConnect = (LinearLayout) findViewById(R.id.linearLayout01);
		llInput = (LinearLayout) findViewById(R.id.linearLayout04);
		llSms = (LinearLayout) findViewById(R.id.linearLayout02);
		llContact = (LinearLayout) findViewById(R.id.linearLayout05);
		llText = (LinearLayout) findViewById(R.id.linearLayout06);
		llNet = (LinearLayout) findViewById(R.id.linearLayout03);
		llDevices = (LinearLayout) findViewById(R.id.linearLayoutDevice);
		ll = new LinearLayout[] { llConnect, llInput, llSms, llContact, llText,
				llNet };
		btConnect = (Button) findViewById(R.id.btconnect);
		btInput = (Button) findViewById(R.id.btinput);
		btSMS = (Button) findViewById(R.id.btsms);
		btContact = (Button) findViewById(R.id.btcontact);
		btText = (Button) findViewById(R.id.bttext);
		btNet = (Button) findViewById(R.id.btnet);
		btSearch = (Button) findViewById(R.id.btsearch);
		btDisconnect = (Button) findViewById(R.id.btdisconnect);
		btConnect.setOnClickListener(this);
		btInput.setOnClickListener(this);
		btSMS.setOnClickListener(this);
		btContact.setOnClickListener(this);
		btText.setOnClickListener(this);
		btNet.setOnClickListener(this);
		btSearch.setOnClickListener(this);
		btDisconnect.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
		tv4 = (TextView) findViewById(R.id.textView4);
		tv5 = (TextView) findViewById(R.id.textView5);
		tv6 = (TextView) findViewById(R.id.textView6);
		tv7 = (TextView) findViewById(R.id.textView7);
		tv = new TextView[] { tv1, tv2, tv3, tv4, tv5, tv6, tv7 };
		tvInfo = (TextView) findViewById(R.id.textViewInfo);
		pbar = (ProgressBar) findViewById(R.id.progressBar1);

		wv = (WebView) findViewById(R.id.webView1);
		initWebView(wv);
		etweb = (EditText) findViewById(R.id.etweb);
		etweb.setText("vip.cxcard.com/vip19/phone/ConsumeInformation.asp?dh=P112013410161153114");
		btweb = (Button) findViewById(R.id.btweb);
		btPrintWeb = (Button) findViewById(R.id.btprint);
		btweb.setOnClickListener(this);
		btPrintWeb.setOnClickListener(this);

		lvSms = (ListView) findViewById(R.id.listView1);
		lvContact = (ListView) findViewById(R.id.listView2);
		lvText = (ListView) findViewById(R.id.listView3);
		new GetResourceThread().start();

		btTextPrint = (Button) findViewById(R.id.bttextprint);
		btRead = (Button) findViewById(R.id.btread);
		btCheckKey = (Button) findViewById(R.id.btcheckkey);
		btSetKey = (Button) findViewById(R.id.btsetkey);
		btSetBarcode = (Button) findViewById(R.id.btbarcode);
		btFontSize = (Button) findViewById(R.id.buttonFontSize);
		rbAscii = (RadioButton) findViewById(R.id.radioAscii);
		rbHex = (RadioButton) findViewById(R.id.radioHex);
		rbBarcode = (RadioButton) findViewById(R.id.radioBarcode);
		rbQRcode = (RadioButton) findViewById(R.id.radioQRcode);
		etTextPrint = (EditText) findViewById(R.id.ettextprint);
		tvRead = (TextView) findViewById(R.id.tvread);
		btTextPrint.setOnClickListener(this);
		btRead.setOnClickListener(this);
		btCheckKey.setOnClickListener(this);
		btSetKey.setOnClickListener(this);
		rbAscii.setOnClickListener(this);
		rbHex.setOnClickListener(this);
		rbBarcode.setOnClickListener(this);
		rbQRcode.setOnClickListener(this);
		btSetBarcode.setOnClickListener(this);
		btFontSize.setOnClickListener(this);

		Pos.APP_Init(getApplicationContext());
		Pos.POS_BeginSavingFile(Environment.getExternalStorageDirectory()
				+ "/textprint.log");
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					Toast.makeText(getApplicationContext(), "�������",
							Toast.LENGTH_SHORT).show();
					if (!Pos.POS_Connecting()) {
						tvInfo.setText("");
						pbar.setVisibility(View.INVISIBLE);
						btSearch.setEnabled(true);
					}
				} else if (action
						.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
					llDevices.removeAllViews();
					tvInfo.setText("��������");
					pbar.setVisibility(View.VISIBLE);
					btSearch.setEnabled(false);
				} else if (action
						.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {

				} else if (action.equals(Pos.ACTION_DISCONNECTED)) {
					Pos.POS_Close();
					Toast.makeText(getApplicationContext(), "���ӶϿ�",
							Toast.LENGTH_SHORT).show();
					tvInfo.setText("");
					tvInfo.setTextColor(Color.BLUE);
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(true);
					btDisconnect.setEnabled(false);
				} else if (action.equals(Pos.ACTION_CONNECTED)) {
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					tvInfo.setTextColor(Color.RED);
					tvInfo.setText("�Ѿ�����");
					pbar.setVisibility(View.VISIBLE);
					btSearch.setEnabled(false);
					btDisconnect.setEnabled(true);

					/**
					 * ֻ����ʾ�ú�����ô�ã�ʵ���ϲ�����Ҫ���øú�������Ϊ����֮ǰ�Ѿ�����ͨѶ���ԣ����ͨѶ���ɹ����ǲ��ᷢ�͸ù㲥�ġ�
					 */
					// Pos.POS_ConnectionTest(3);

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
					llDevices.addView(button);
				} else if (action
						.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				} else if (action.equals(Pos.ACTION_BEGINCONNECTING)) {
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(false);
					btDisconnect.setEnabled(false);
					tvInfo.setText("��������");
					pbar.setVisibility(View.VISIBLE);
				} else if (action.equals(Pos.ACTION_CONNECTINGFAILED)) {
					Toast.makeText(getApplicationContext(), "����ʧ��",
							Toast.LENGTH_SHORT).show();
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(true);
				} else if (action.equals(UpdateThread.ACTION_DEBUGINFO)) {
					tvInfo.setText(intent
							.getStringExtra(UpdateThread.EXTRA_DEBUGINFO));
				} else if (action.equals(UpdateThread.ACTION_ENDUPDATE)) {

				} else if (action.equals(Pos.ACTION_TESTSUCCESS)) {
					Toast.makeText(getApplicationContext(), "ͨѶ����",
							Toast.LENGTH_SHORT).show();
				} else if (action.equals(Pos.ACTION_TESTFAILED)) {
					Toast.makeText(getApplicationContext(), "ͨѶʧ��",
							Toast.LENGTH_SHORT).show();
				} else if (action
						.equals(GetResourceThread.ACTION_RESOURCEREADY)) {

					Log.i("BroadcastReceiver", "in ACTION_RESOURCEREADY");

					lvSms.setAdapter(new SimpleAdapter(TextPrintActivity.this,
							listSmsData, R.layout.textlist, new String[] {
									TITLE, INFO }, new int[] { R.id.tvTitle,
									R.id.tvInfo }));
					lvSms.setOnItemClickListener(TextPrintActivity.this);

					lvContact.setAdapter(new SimpleAdapter(
							TextPrintActivity.this, listContactData,
							R.layout.textlist, new String[] { TITLE, INFO },
							new int[] { R.id.tvTitle, R.id.tvInfo }));
					lvContact.setOnItemClickListener(TextPrintActivity.this);

					lvText.setAdapter(new SimpleAdapter(TextPrintActivity.this,
							listTextData, R.layout.textlist, new String[] {
									TITLE, INFO }, new int[] { R.id.tvTitle,
									R.id.tvInfo }));
					lvText.setOnItemClickListener(TextPrintActivity.this);
					Log.i("BroadcastReceiver", "end ACTION_RESOURCEREADY");
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
		intentFilter.addAction(Pos.ACTION_TESTFAILED);
		intentFilter.addAction(Pos.ACTION_TESTSUCCESS);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(UpdateThread.ACTION_DEBUGINFO);
		intentFilter.addAction(UpdateThread.ACTION_ENDUPDATE);
		intentFilter.addAction(GetResourceThread.ACTION_RESOURCEREADY);
		this.registerReceiver(broadcastReceiver, intentFilter);

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
			btSearch.setEnabled(false);
			btDisconnect.setEnabled(true);

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (selectedViewId != NET) {
				// �����˳��Ի���
				AlertDialog isExit = new AlertDialog.Builder(this).create();
				// ���öԻ������
				isExit.setTitle("��ܰ��ʾ");
				// ���öԻ�����Ϣ
				isExit.setMessage("����Ҫ��ӡ�ı�����");
				// ���ѡ��ť��ע�����
				isExit.setButton(DialogInterface.BUTTON_POSITIVE, "�˳�",
						listener);
				isExit.setButton(DialogInterface.BUTTON_NEGATIVE, "ȡ��",
						listener);
				// ��ʾ�Ի���
				isExit.show();
			} else if (selectedViewId == NET && !wv.canGoBack()) {
				AlertDialog isExit = new AlertDialog.Builder(this).create();
				// ���öԻ������
				isExit.setTitle("��ܰ��ʾ");
				// ���öԻ�����Ϣ
				isExit.setMessage("����Ҫ�����ҳ����");
				// ���ѡ��ť��ע�����
				isExit.setButton(DialogInterface.BUTTON_POSITIVE, "�˳�",
						listener);
				isExit.setButton(DialogInterface.BUTTON_NEGATIVE, "ȡ��",
						listener);
				// ��ʾ�Ի���
				isExit.show();
			} else {
				if (wv.canGoBack())
					wv.goBack();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	/** �����Ի��������button����¼� */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����
				if (selectedViewId == NET) {
					scrollViewPageList.setVisibility(View.VISIBLE);
					selectedViewId = 0;
					setBackgroundAndVisible(0);
				} else {
					Pos.APP_UnInit();
					System.exit(0);
				}
				break;
			case DialogInterface.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btconnect: {
			selectedViewId = 0;
			setBackgroundAndVisible(0);
			break;
		}
		case R.id.btinput: {
			selectedViewId = 0;
			setBackgroundAndVisible(1);
			break;
		}
		case R.id.btsms: {
			setBackgroundAndVisible(2);
			selectedViewId = SMS;
			break;
		}
		case R.id.btcontact: {
			setBackgroundAndVisible(3);
			selectedViewId = CONTACT;
			break;
		}
		case R.id.bttext: {
			setBackgroundAndVisible(4);
			selectedViewId = TEXT;
			break;
		}

		case R.id.btnet: {
			selectedViewId = NET;
			setBackgroundAndVisible(5);

			break;
		}

		case R.id.btsearch: {
			if (myBTAdapter != null)
				if (!myBTAdapter.isDiscovering())
					myBTAdapter.startDiscovery();
			break;
		}

		case R.id.btdisconnect: {
			Pos.POS_Close();
			break;
		}

		case R.id.btweb: {
			String url = "http://" + etweb.getText().toString();
			wv.loadUrl(url);
			break;
		}

		case R.id.btprint: {
			/**
			 * ��ӡ���а���ı�����
			 */
			String tmp = paste(getApplicationContext());
			Pos.POS_S_TextOut(tmp, 0, 0, 0, 0, 0x00);
			Pos.POS_FeedLine();
			Pos.POS_FeedLine();
			break;
		}

		case R.id.bttextprint: {

			if (rbAscii.isChecked()) {
				String tmp = etTextPrint.getText().toString();
				if (tmp != null) {
					Pos.POS_S_TextOut(tmp, 0, 0, 0, nFontSize, 0x00);
					Pos.POS_FeedLine();
				}
			} else if (rbHex.isChecked()) {
				String tmp = etTextPrint.getText().toString();
				if (tmp != null) {
					Pos.POS_Write(Pos.StrToCmdBytes(tmp));
				}
			} else if (rbBarcode.isChecked()) {
				String tmp = etTextPrint.getText().toString();
				if (tmp != null) {
					switch (barcodeType) {
					case Pos.Constant.BARCODE_TYPE_UPC_A:
					case Pos.Constant.BARCODE_TYPE_UPC_E:
						if ((tmp.length() == 11) || (tmp.length() == 12))
							Pos.POS_S_SetBarcode(tmp, barcodeOrgx, barcodeType,
									barcodeWidthX, barcodeHeight,
									barcodeFontType, barcodeFontPosition);
						else
							Toast.makeText(getApplicationContext(),
									"UPC A(E)���볤��Ϊ11-12���ַ�0-9",
									Toast.LENGTH_SHORT).show();
						break;

					case Pos.Constant.BARCODE_TYPE_EAN13:
						if ((tmp.length() == 12) || (tmp.length() == 13))
							Pos.POS_S_SetBarcode(tmp, barcodeOrgx, barcodeType,
									barcodeWidthX, barcodeHeight,
									barcodeFontType, barcodeFontPosition);
						else
							Toast.makeText(getApplicationContext(),
									"BARCODE_TYPE_EAN13 ���볤��Ϊ12-13���ַ�0-9",
									Toast.LENGTH_SHORT).show();
						break;

					case Pos.Constant.BARCODE_TYPE_EAN8:
						if ((tmp.length() == 7) || (tmp.length() == 8))
							Pos.POS_S_SetBarcode(tmp, barcodeOrgx, barcodeType,
									barcodeWidthX, barcodeHeight,
									barcodeFontType, barcodeFontPosition);
						else
							Toast.makeText(getApplicationContext(),
									"BARCODE_TYPE_EAN8 ���볤��Ϊ7-8���ַ�0-9",
									Toast.LENGTH_SHORT).show();
						break;

					default:
						Pos.POS_S_SetBarcode(tmp, barcodeOrgx, barcodeType,
								barcodeWidthX, barcodeHeight, barcodeFontType,
								barcodeFontPosition);
						break;
					}
				}
			} else if (rbQRcode.isChecked()) {
				String tmp = etTextPrint.getText().toString();
				if (tmp != null) {
					Pos.POS_S_SetQRcode(tmp, 0, 3, 2);
				} else {
					Toast.makeText(getApplicationContext(), "������QR������",
							Toast.LENGTH_SHORT).show();
				}
			}

			break;
		}

		/**
		 * ֻ�ǰ�16����������ʾ����
		 */
		case R.id.btread: {
			tvRead.setText(Pos.POS_ReadAllToString());

			break;
		}

		case R.id.btsetkey: {
			// ���Ե���һ�������û�����
			final EditText inputKey = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("������Կ").setView(inputKey)
					.setNegativeButton("Cancel", null);
			builder.setPositiveButton("Set",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							byte[] key = inputKey.getText().toString()
									.getBytes();

							if (key != null) {
								Pos.POS_SetKey(key);
							}
						}
					});
			builder.show();
			// ���óɹ�������UpdateThread.ACTION_DEBUGINFO����֪ͨ
			break;
		}

		case R.id.btcheckkey: {
			// ���Ե���һ�������û�����
			final EditText inputKey = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("������Կ").setView(inputKey)
					.setNegativeButton("Cancel", null);
			builder.setPositiveButton("Check",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							byte[] key = inputKey.getText().toString()
									.getBytes();
							if (key != null) {
								byte[] data = Pos.getRandomBytes(8);
								Pos.POS_CheckKey(key, data);
							}
						}
					});
			builder.show();
			// ��ԿУ��ɹ����Ҳ����UpdateThread.ACTION_DEBUGINFO����֪ͨ������ͨ�������ù㲥�����ﵽ�������ӵ�Ŀ�ġ�
			break;
		}

		case R.id.btbarcode: {
			Intent intent = new Intent(TextPrintActivity.this,
					SetBarcodeActivity.class);
			startActivity(intent);
			break;
		}

		case R.id.buttonFontSize: {
			AlertDialog dialog = new AlertDialog.Builder(TextPrintActivity.this)
					.setTitle(R.string.fontsize12x24)
					.setItems(R.array.fontsize,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.fontsize);
									btFontSize.setText(items[which]);
									nFontSize = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.radioAscii: {
			Toast.makeText(this, "ѡ�����ı�ģʽ", Toast.LENGTH_SHORT).show();
			break;
		}

		case R.id.radioHex: {
			Toast.makeText(this, "ѡ��������ģʽ", Toast.LENGTH_SHORT).show();
			break;
		}

		case R.id.radioBarcode: {
			Toast.makeText(this, "ѡ��������ģʽ", Toast.LENGTH_SHORT).show();
			break;
		}

		case R.id.radioQRcode: {
			Toast.makeText(this, "ѡ����QR��ģʽ", Toast.LENGTH_SHORT).show();
			break;
		}

		default:
			break;
		}
	}

	/**
	 * 
	 * @param n1
	 *            0-5
	 */
	private void setBackgroundAndVisible(int n1) {
		for (int i = 0; i < tv.length; i++)
			tv[i].setBackgroundResource(R.drawable.fenge_normal);

		tv[n1].setBackgroundResource(R.drawable.fenge_selected);
		tv[n1 + 1].setBackgroundResource(R.drawable.fenge_selected);

		if (n1 == 5) {
			scrollViewPageList.setVisibility(View.GONE);
		}

		for (int i = 0; i < ll.length; i++) {
			if (i == n1)
				ll[i].setVisibility(View.VISIBLE);
			else
				ll[i].setVisibility(View.INVISIBLE);
		}

	}

	private List<Map<String, Object>> getTextData() {
		String[] extensions = new String[] { ".txt", ".log" };
		List<String> listFile = new ArrayList<String>();
		listFile.addAll(new ListFiles().getFiles(
				Environment.getExternalStorageDirectory(), extensions));

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < listFile.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			String tempPath = listFile.get(i);
			map.put(PATH, tempPath);
			map.put(TITLE, tempPath.substring(tempPath
					.lastIndexOf(File.separatorChar)));
			map.put(INFO, "�ı�");
			list.add(map);
		}
		return list;
	}

	private List<Map<String, Object>> getSmsData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 1; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(TITLE, "**�����Ķ���");
			map.put(INFO, "���");
			list.add(map);
		}
		return list;
	}

	private List<Map<String, Object>> getContactData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 1; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(TITLE, "�й���ͨ");
			map.put(INFO, "10010");
			list.add(map);
		}
		return list;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (selectedViewId) {

		case SMS:
			break;

		case CONTACT:
			break;

		case TEXT:
			Pos.POS_PrintText(listTextData.get(position).get(PATH).toString());
			Pos.POS_FeedLine();
			break;

		default:
			break;
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(WebView webView) {
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);// ��ҳ�������¼�
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);// ��������ӵ�ʱ��������ԭ�������ϼ���URL
				return true;

			}
		});
		JsPrintObj jop = new JsPrintObj();
		webView.addJavascriptInterface(jop, "jop");
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final android.webkit.JsResult result) {

				return true;
			}
		});
	}

	/**
	 * 
	 * @author Administrator
	 * @see ��ҳ�е���java���룬�����ݲ���
	 */
	public class JsPrintObj {
		public String print(String arg1, int arg2) {
			String tmp = "��ӡ��\n";
			try {
				tmp += arg1;
				Pos.POS_Write(arg1.getBytes("GBK"));
				Pos.POS_FeedLine();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				tmp += "�������\n";
			}
			return tmp;
		}
	}

	/**
	 * 
	 * API 11֮ǰ�� android.text.ClipboardManager API
	 * 11֮��android.content.ClipboardManager
	 * 
	 * @param context
	 * @return
	 */
	public String paste(Context context) {
		// �õ������������
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		if (cmb != null)
			return cmb.getText().toString();
		else
			return "���а岻����";
	}

	/**
	 * ����.start()��������ʼ����Դ ��ϻᷢ�͹㲥ACTION_RESOURCEREADY ��׽�ù㲥�����ɿ�ʼ��ʼ����Դ
	 * 
	 * @author lvrenyang
	 * 
	 */
	public class GetResourceThread extends Thread {

		public static final String ACTION_RESOURCEREADY = "ACTION_RESOURCEREADY";
		private static final String TAG = "GetResourceThread";

		@Override
		public void run() {
			Log.i(TAG, "in run()");
			listSmsData = getSmsData();
			listContactData = getContactData();
			listTextData = getTextData();
			TextPrintActivity.this.sendBroadcast(new Intent(
					ACTION_RESOURCEREADY));
			Log.i(TAG, "end run()");
		}
	}
}
