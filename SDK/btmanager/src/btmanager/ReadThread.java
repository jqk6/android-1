package btmanager;

import java.io.IOException;
import java.io.InputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

// ��ȥ�߳�һֱ�ڶ���ֻ�ǲ�ͬ��������ݽ�����ʽ��ͬ
// Ҫĳͨ������һ���̣߳���ѭ���ж϶�ȡ�Ƿ�ɹ���������Ӧ����
// Ҫĳͨ��һ���㲥���ѽ������ȥ
public class ReadThread extends Thread {

	public static Handler readHandler = null;
	private static Context context;
	private static BroadcastReceiver broadcastReceiver;

	public static final int WHAT_QUIT = 1000;
	public static final int WHAT_READ = 100000;

	public static final String ACTION_READTHREADRECEIVE = "ACTION_READTHREADRECEIVE";
	public static final String EXTRA_READTHREADRECEIVEBYTE = "EXTRA_READTHREADRECEIVEBYTE";

	public static final String ACTION_READTHREADRECEIVES = "ACTION_READTHREADRECEIVES";
	public static final String EXTRA_READTHREADRECEIVEBYTES = "EXTRA_READTHREADRECEIVEBYTES";

	public static final String ACTION_READTHREADRECEIVERESPOND = "ACTION_READTHREADRECEIVERESPOND";
	public static final String EXTRA_PCMDCMD = "EXTRA_PCMDCMD";
	public static final String EXTRA_PCMDPARA = "EXTRA_PCMDPARA";
	public static final String EXTRA_PCMDLENGTH = "EXTRA_PCMDLENGTH";
	public static final String EXTRA_PCMDDATA = "EXTRA_PCMDDATA";

	// EXTRA_PCMDPACKAGE ָ���ͳ�ȥ�������
	public static final String EXTRA_PCMDPACKAGE = "EXTRA_PCMDPACKAGE";
	public static final String EXTRA_READTHREADRECEIVECORRECT = "EXTRA_READTHREADRECEIVECORRECT";

	// ANALYSISMODE_NONE ָ��������ʽΪ�ޣ�ֻ�ǽ��ո�������
	private static final int ANALYSISMODE_LENGTH = 100001;
	// ANALYSISMODE_RECAUOT ָ��������ʽΪ����ͨѶЭ�飬����ͨ��Э��ȷ��
	private static final int ANALYSISMODE_RECAUOT = 100002;
	// ��ǰ��ANALYSISMODE, ��ȡ��ʱ�򣬸��ݵ�ǰ��ģʽ������Ӧ�ĺ���
	private static int ANALYSISMODE = ANALYSISMODE_LENGTH;

	private static final int LBUF_MAXSIZE = 1024;

	// ֻҪ��quit�� �������˳�
	//
	private static boolean needread = false;
	private static boolean reading = false;

	private static boolean debug = false;

	// checkup���յ����ӽ����Ĺ㲥ʱ���ͻ����checkup
	// ���check��ȷ����û���⣬check����ȷ���ͽ�checkup��false;
	private static boolean checkup = true;
	private static int checkuptimeout = 500;
	private static int checkupretrytimes = 3;
	private static int checkupcount = 0;
	private static byte[] defaultkey = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
			0x37, 0x38 };
	private static byte[] randomdata;

	public ReadThread(Context context) {
		ReadThread.context = context;
		initBroadcast();
	}

	@Override
	public void run() {
		Looper.prepare();
		readHandler = new ReadHandler();
		Looper.loop();
	}

	private static class ReadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			/**
			 * ���ȶ�ȥ���ݣ���������֮�󣬷��ͳ�ȥ �÷�������֮�󣬾���Զ�����˳�
			 */
			case WHAT_READ: {
				setReading(true);

				while (getNeedReadFlag()) {
					int rec = readOne();
					if (getDebug() && (rec != -1)) {
						Intent intent = new Intent(ACTION_READTHREADRECEIVE);
						intent.putExtra(EXTRA_READTHREADRECEIVEBYTE, (byte) rec);
						intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, true);
						String address = ConnectThread.getConnectedDevice();
						if (address != null) {
							if (BluetoothAdapter.checkBluetoothAddress(address))
								intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
										BluetoothAdapter.getDefaultAdapter()
												.getRemoteDevice(address));
						}
						sendBroadcast(intent);
					}
					analysisData(rec);
				}

				setReading(false);
				break;
			}

			case WHAT_QUIT:
				uninitBroadcast();
				Looper.myLooper().quit();
				break;

			default:
				break;
			}

		}
	}

	// ָ�����ճ���
	// ��ʼ����Ӧ���ݶ�
	static void Request(byte[] data, int length, int timeout) {
		ANALYSISMODE = ANALYSISMODE_LENGTH;
		lbuf.constructtime = System.currentTimeMillis();
		lbuf.timeout = timeout;
		lbuf.length = length;
		lbuf.buf_count = 0;
		lbuf.cmd = data;
		lbuf.requested = false;
		lbuf.lbufHandlerStart();
	}

	// ���������Զ�ʶ��
	// ��������������̺߳�read�̲߳�����ͬ·��
	// ���������ã���readThread����Ч
	static void Request(byte[] data, int timeout) {
		ANALYSISMODE = ANALYSISMODE_RECAUOT;
		rcbuf.constructtime = System.currentTimeMillis();
		rcbuf.timeout = timeout;
		rcbuf.cmd = data;
		rcbuf.cpk.clean();
		rcbuf.requested = false;
		rcbuf.rcbufHandlerStart();
	}

	private static int getAnalysisMode() {
		return ANALYSISMODE;
	}

	static void setNeedReadFlag(boolean readornot) {
		needread = readornot;
	}

	static boolean getNeedReadFlag() {
		return needread;
	}

	static void setDebug(boolean debugornot) {
		debug = debugornot;
	}

	static boolean getDebug() {
		return debug;
	}

	static boolean isReading() {
		return reading;
	}

	static void setReading(boolean readingornot) {
		reading = readingornot;
	}

	// ��Ȼ�㲥����Ҫ�����淢�ͣ�����connectthreadһ���������ӵ�ʱ����
	private static void analysisData(int rec) {

		switch (getAnalysisMode()) {

		case ANALYSISMODE_LENGTH: {

			// �������������棬˵�������Ѿ�������ϣ����յ����ݿ��Ժ���
			if (!lbuf.lbufHandlerEnd()) {
				if (!lbuf.requested) {
					Pos.POS_Write(lbuf.cmd);
					lbuf.requested = true;
					break;
				}
				lbuf.lbufHandler(rec);
			}

			break;
		}

		case ANALYSISMODE_RECAUOT: {

			if (!rcbuf.rcbufHandlerEnd()) {
				if (!rcbuf.requested) {
					Pos.POS_Write(rcbuf.cmd);
					rcbuf.requested = true;
					break;
				}
				rcbuf.rcbufHandler(rec);
			}
			break;
		}

		}
	}

	// no block
	private static int readOne() {
		if (!ConnectThread.isConnected())
			return -1;
		InputStream is = ConnectThread.getInputStream();
		try {
			if (is != null) {

				if (is.available() > 0)
					return is.read();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (is != null)
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
		return -1;
	}

	private static void sendBroadcast(Intent intent) {
		if (context != null)
			context.sendBroadcast(intent);
	}

	// ANALYSISMODE_REQUEST���������ݽṹ
	private static class lbuf {
		private static boolean requested = true;

		private static final int handleSuccess = 1;
		private static final int handleOngoing = 2;
		private static final int handleFailed = 3;
		public static int handleStatus = handleSuccess;

		public static byte[] cmd;
		public static byte[] buf = new byte[LBUF_MAXSIZE];
		// ��Ҫ���ص����ݵĳ���
		public static int length = 0;
		public static int buf_count = 0;
		public static long constructtime = 0;
		public static int timeout = 0;

		public static void lbufHandler(int rec) {
			if ((System.currentTimeMillis() - lbuf.constructtime > lbuf.timeout)) {
				handleStatus = handleFailed;
				Intent intent = new Intent(ACTION_READTHREADRECEIVES);
				intent.putExtra(EXTRA_PCMDPACKAGE, cmd);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_READTHREADRECEIVEBYTES, new byte[0]);
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, false);
				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				sendBroadcast(intent);
				return;
			}

			if (rec != -1)
				buf[buf_count++] = (byte) rec;

			if (lbuf.buf_count == lbuf.length) {
				handleStatus = handleSuccess;
				Intent intent = new Intent(ACTION_READTHREADRECEIVES);
				intent.putExtra(EXTRA_PCMDPACKAGE, cmd);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_READTHREADRECEIVEBYTES,
						DataUtils.getSubBytes(buf, 0, length));
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, true);
				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				sendBroadcast(intent);
				return;
			}
		}

		public static boolean lbufHandlerEnd() {
			return handleStatus != handleOngoing;
		}

		public static void lbufHandlerStart() {
			handleStatus = handleOngoing;
		}

	}

	// UpdateThread�õ�
	public static boolean lbufhandlerSuccess() {
		return lbuf.handleStatus == lbuf.handleSuccess;
	}

	// ANALYSISMODE_REQUESTCOMMAND���������ݽṹ
	private static class rcbuf {

		// �Ƿ���������һ��������ջ�������Ȼ���ٷ������ݣ��Ž��ñ�������
		private static boolean requested = true;

		private static final int handleSuccess = 1;
		private static final int handleOngoing = 2;
		private static final int handleFailed = 3;
		public static int handleStatus = handleSuccess;

		// ����ʼʱ��
		public static long constructtime = 0;
		// ��ʱ
		public static int timeout = 0;

		// ��Ҫ���͵�����
		public static byte[] cmd;

		private static class cpk {

			private static final byte PACKAGESTART = 0x03;
			private static final byte PACKAGEDIRIN = (byte) 0xFE;

			// �����ʼ�յ�0x03������started
			private static byte started = 0;

			// �յ�0xfe����direction in
			private static byte direction = 0;

			// ����
			private static byte[] cmd = new byte[2];
			private static int cmd_bslen = 0;

			// ����
			private static byte[] para = new byte[4];
			private static int para_bslen = 0;

			// ��Ҫ���ص����ݵĳ���
			private static volatile byte[] length = new byte[2];
			private static volatile int length_bslen = 0;

			// Ӧ����ȫ��������������ж��Ƿ�ɹ�
			private static byte checkSumH = 0;
			private static boolean cshReceived = false;

			private static byte checkSumD = 0;
			private static boolean csdReceived = false;

			// ���յ���������ݻ�����
			private static byte[] buf = new byte[0];
			private static int buf_bslen = 0;

			public static void clean() {
				started = 0;
				direction = 0;
				cmd_bslen = 0;
				para_bslen = 0;
				length_bslen = 0;
				buf_bslen = 0;
				cshReceived = false;
				csdReceived = false;
			}

			public static byte[] getCmdArray() {
				return DataUtils.byteArraysToBytes(new byte[][] {
						{ started, direction }, cmd, para, length,
						{ checkSumH, checkSumD } });
			}

			public static boolean check() {
				byte[] cmd = getCmdArray();
				return (cmd[10] == DataUtils.bytesToXor(cmd, 0, 10))
						&& (cmd[11] == DataUtils.bytesToXor(buf, 0, buf.length));
			}

			public static void sendCmdBroadcast(boolean extraCorrect) {
				Intent intent = new Intent(ACTION_READTHREADRECEIVERESPOND);
				int cmd;
				int para;
				int length;
				byte[] data = new byte[0];

				// ԭ�����
				intent.putExtra(EXTRA_PCMDPACKAGE, rcbuf.cmd);
				if (extraCorrect) {
					cmd = (cpk.cmd[0] & 0xff) + ((cpk.cmd[1] & 0xff) << 8);
					para = (cpk.para[0] & 0xff) + ((cpk.para[1] & 0xff) << 8)
							+ ((cpk.para[2] & 0xff) << 16)
							+ ((cpk.para[3] & 0xff) << 24);
					length = (cpk.length[0] & 0xff)
							+ ((cpk.length[1] & 0xff) << 8);
					data = DataUtils.cloneBytes(buf);
				} else {
					// ��������Ὣԭ�����

					cmd = (rcbuf.cmd[2] & 0xff) + ((rcbuf.cmd[3] & 0xff) << 8);
					para = (rcbuf.cmd[4] & 0xff) + ((rcbuf.cmd[5] & 0xff) << 8)
							+ ((rcbuf.cmd[6] & 0xff) << 16)
							+ ((rcbuf.cmd[7] & 0xff) << 24);
					length = (rcbuf.cmd[8] & 0xff)
							+ ((rcbuf.cmd[9] & 0xff) << 8);
					data = DataUtils.getSubBytes(rcbuf.cmd, 12,
							rcbuf.cmd.length - 12);
				}

				intent.putExtra(EXTRA_PCMDCMD, cmd);
				intent.putExtra(EXTRA_PCMDPARA, para);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_PCMDDATA, data);
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, extraCorrect);

				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				ReadThread.sendBroadcast(intent);
			}

		}

		public static void rcbufHandler(int nrec) {
			if ((System.currentTimeMillis() - rcbuf.constructtime > rcbuf.timeout)) {
				handleStatus = handleFailed;
				cpk.sendCmdBroadcast(false);
				return;
			}

			if (nrec == -1)
				return;

			byte rec = (byte) nrec;

			if (cpk.started != cpk.PACKAGESTART) {
				if (rec == cpk.PACKAGESTART)
					cpk.started = rec;
				else
					cpk.clean();
				return;

			}

			if (cpk.direction != cpk.PACKAGEDIRIN) {
				if (rec == cpk.PACKAGEDIRIN)
					cpk.direction = rec;
				else
					cpk.clean();
				return;
			}

			// ���յ���һ���ֽ�����ʱ��cmd_bslen = 0, < 2
			// ���Ǳ��棬Ȼ��cmd_bslen ++,
			// ���յ��ڶ����ֽ�����ʱ��cmd_bslen = 1, < 2
			// ���Ǳ��棬Ȼ��cmd_bslen ++, ��ʱcmd_bslen = 2��
			// �Ѿ���������������ˣ�����ѭ��û������
			if (cpk.cmd_bslen < cpk.cmd.length) {
				cpk.cmd[cpk.cmd_bslen++] = rec;
				return;
			}

			if (cpk.para_bslen < cpk.para.length) {
				cpk.para[cpk.para_bslen++] = rec;
				return;
			}

			if (cpk.length_bslen < cpk.length.length) {
				cpk.length[cpk.length_bslen++] = rec;
				return;
			}

			if (!cpk.cshReceived) {
				cpk.checkSumH = rec;
				cpk.cshReceived = true;
				return;
			}

			if (!cpk.csdReceived) {
				cpk.checkSumD = rec;
				cpk.csdReceived = true;
				return;
			}

			if (cpk.buf_bslen == 0) {
				// buf_bslen == 0
				cpk.buf = new byte[(cpk.length[0] & 0xff)
						+ ((cpk.length[1] & 0xff) << 8)];

				// �ǻ�û�г�ʼ�������Ѿ��������أ�
				// ˵����û�г�ʼ�������ڳ�ʼ��
				if (cpk.buf.length == 0) {
					// �Ѿ������������Ƿ���ȷ�������жϣ�ֻ�践�����ݼ��ɣ����û�оͷ���new byte[0]
					handleStatus = handleSuccess;
					cpk.sendCmdBroadcast(cpk.check());
					return;
				} else {
					cpk.buf[cpk.buf_bslen++] = rec;
					return;
				}
			} else {
				// �Ѿ���ʼ�����ж��Ƿ����
				if (cpk.buf_bslen < cpk.buf.length) {
					cpk.buf[cpk.buf_bslen++] = rec;

					if (cpk.buf_bslen == cpk.buf.length) {
						handleStatus = handleSuccess;
						cpk.sendCmdBroadcast(cpk.check());
						return;
					} else {
						return;
					}

				}

			}
		}

		public static boolean rcbufHandlerEnd() {
			return handleStatus != handleOngoing;
		}

		public static void rcbufHandlerStart() {
			handleStatus = handleOngoing;
		}
	}

	public static boolean getCheckUp() {
		return checkup;
	}

	public static byte[] getKey() {
		return defaultkey;
	}

	public static void setKey(byte[] newkey) {
		for (int i = 0; i < defaultkey.length; i++)
			defaultkey[i] = newkey[i];
	}

	private static void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (ConnectThread.ACTION_CONNECTED.equals(action)) {
					// ���ӽ���������ָ��checkup
					randomdata = DataUtils.getRandomByteArray(8);
					byte blengthl = (byte) (randomdata.length % 0x100);
					byte blengthh = (byte) (randomdata.length / 0x100);
					byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
							Cmd.ESCCmd.DES_ENCRYPT, { blengthl, blengthh },
							randomdata });
					Request(data, data.length, checkuptimeout);
				} else if (ACTION_READTHREADRECEIVES.equals(action)) {
					// �յ��ظ�������У��
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] cmd = intent.getByteArrayExtra(EXTRA_PCMDPACKAGE);
					int length = intent.getIntExtra(EXTRA_PCMDLENGTH,
							Integer.MAX_VALUE);
					if (cmd == null)
						return;
					if ((cmd.length > 3)
							&& (cmd[0] == Cmd.ESCCmd.DES_ENCRYPT[0])
							&& (cmd[1] == Cmd.ESCCmd.DES_ENCRYPT[1])
							&& (cmd[2] == Cmd.ESCCmd.DES_ENCRYPT[2])) {
						if (recstatus) {
							checkupcount = 0;
							byte[] rcs = intent
									.getByteArrayExtra(ReadThread.EXTRA_READTHREADRECEIVEBYTES);
							byte[] encrypted = DataUtils.getSubBytes(rcs, 5,
									rcs.length - 5);
							/**
							 * �����ݽ��н���
							 */
							DES2 des2 = new DES2();
							// ��ʼ����Կ
							des2.yxyDES2_InitializeKey(defaultkey);
							des2.yxyDES2_DecryptData(encrypted);
							byte[] decodeData = des2.getPlaintext();

							if (DataUtils.bytesEquals(randomdata, decodeData)) {
								checkup = true;
							} else {
								checkup = false;
							}
						} else {
							checkupcount++;
							if (checkupcount < checkupretrytimes)
								Request(cmd, length, checkuptimeout);
							else
								checkup = false;
						}
						if (getDebug())
							Toast.makeText(context, "" + checkup,
									Toast.LENGTH_SHORT).show();
					}
				}
			}

		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectThread.ACTION_CONNECTED);
		intentFilter.addAction(ACTION_READTHREADRECEIVES);
		intentFilter
				.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		context.registerReceiver(broadcastReceiver, intentFilter);
	}

	private static void uninitBroadcast() {
		if (context != null)
			context.unregisterReceiver(broadcastReceiver);
	}
}
