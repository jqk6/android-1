package btManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

//û��̫�ྲ̬�̺߳�looper֮��ģ������ܸ��ȶ��˰ɡ�
public class Pos {

	// ��Ҫ�Ż�
	public static final Object lock = new Object();
	public static volatile BluetoothSocket cSocket;
	public static volatile OutputStream os;
	public static volatile BufferedInputStream bis;
	public static boolean Connecting = false;
	public static boolean Connected = false;
	private static boolean autoPairing = false;
	private static String pairingCode = "0000";
	private static BluetoothAdapter myBTAdapter = BluetoothAdapter
			.getDefaultAdapter();
	public static String savingFilePath = "";
	public static FileOutputStream fos = null;
	public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
	public static final String ACTION_CONNECTEDUNTEST = "ACTION_CONNECTEDUNTEST";
	public static final String ACTION_DISCONNECTED = "ACTION_DISCONNECTED";
	public static final String ACTION_BEGINCONNECTING = "ACTION_BEGINCONNECTING";
	public static final String ACTION_CONNECTINGFAILED = "ACTION_CONNECTINGFAILED";
	public static final String ACTION_TESTSUCCESS = "ACTION_TESTSUCCESS";
	public static final String ACTION_TESTFAILED = "ACTION_TESTFAILED";
	public static Context mContext;

	public static synchronized void APP_Init(Context mContext) {
		if (Pos.mContext == null && mContext != null) {
			Pos.mContext = mContext;
			new WriteThread().start();
			new ReadThread().start();
			BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					String action = intent.getAction();
					if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {

						if (cSocket != null) {
							String rmAddress = cSocket.getRemoteDevice()
									.getAddress();
							BluetoothDevice device = intent
									.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							if (device.getAddress().equals(rmAddress))
								POS_Close();
						} else
							POS_Close();

					} else if (action
							.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
						if (autoPairing) {
							BluetoothDevice device = intent
									.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							if (device == null)
								return;
							try {
								ClsUtils.setPin(BluetoothDevice.class, device,
										pairingCode);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							try {
								ClsUtils.cancelBondProcess(
										BluetoothDevice.class, device);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					} else if (action.equals(Pos.ACTION_CONNECTEDUNTEST)) {
						/**
						 * �ж�һ�£��Ƿ���ȷ�������������� ���TEST�ɹ����ͻᷢ�����ӽ����Ĺ㲥�����򣬾ͻ�Close
						 */
						Pos.POS_StartListening();
						Message msg = WriteThread.writeHandler.obtainMessage();
						msg.what = WriteThread.WHAT_CONNECTTEST;
						WriteThread.writeHandler.sendMessage(msg);

					}
				}

			};

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			intentFilter.addAction(Pos.ACTION_CONNECTEDUNTEST);
			intentFilter
					.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
			Pos.mContext.registerReceiver(broadcastReceiver, intentFilter);
		}
	}

	public static synchronized void APP_UnInit() {
		POS_EndSavingFile();
		POS_Close();
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_QUIT;
		WriteThread.writeHandler.sendMessageAtFrontOfQueue(msg);
		Message msg2 = ReadThread.readHandler.obtainMessage();
		msg.what = ReadThread.WHAT_QUIT;
		ReadThread.readHandler.sendMessageAtFrontOfQueue(msg2);
	}

	// �þ�̬����ͬһʱ��ֻ�ܽ���һ��
	public static synchronized void POS_Open(String btAddress) {
		if (!Connecting && cSocket == null) {
			Connecting = true;
			new ConnectThread(btAddress).start();
		}
	}

	// ����⵽���ӶϿ�ʱ��Ҫ���øú���
	public static synchronized void POS_Close() {
		boolean preConnected = Connected;
		Connected = false;
		if (bis != null) {
			try {
				bis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bis = null;

		if (cSocket != null)
			try {
				cSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		cSocket = null;
		os = null;

		if (preConnected)
			Pos.mContext.sendBroadcast(new Intent(ACTION_DISCONNECTED));

	}

	public static void POS_Write(byte[] data) {
		// ����ò�Ʋ��У�Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_WRITE;
		msg.obj = data.clone();
		WriteThread.writeHandler.sendMessage(msg);
	}

	/**
	 * �ᷢ��һ���㲥����֪ͨ����SetKey�Ƿ�ɹ���
	 * 
	 * @param key
	 */
	public static void POS_SetKey(byte[] key) {

		if (key == null)
			return;
		if (key.length != 8)
			return;

		// ����ò�Ʋ��У�Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_SETKEY;
		msg.obj = key.clone();
		WriteThread.writeHandler.sendMessage(msg);
	}

	/**
	 * key ��data������ҪΪ8���ֽ�
	 * 
	 * @param
	 */
	public static void POS_CheckKey(byte[] key, byte[] data) {
		if (key == null)
			return;
		if (key.length != 8)
			return;
		if (data == null)
			return;
		if (data.length != 8)
			return;

		byte[][] keyAndData = new byte[][] { key, data };

		// ����ò�Ʋ��У�Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_CHECKKEY;
		msg.obj = keyAndData.clone();
		WriteThread.writeHandler.sendMessage(msg);
	}

	public static byte[] getRandomBytes(int nlength) {
		byte[] data = new byte[nlength];
		Random rmByte = new Random(System.currentTimeMillis());
		for (int i = 0; i < nlength; i++) {
			// �÷���������������һ�������intֵ����ֵ����[0,n)�����䣬Ҳ����0��n֮������intֵ������0��������n
			data[i] = (byte) rmByte.nextInt(256);
		}
		return data;
	}

	// ��û�а취���������������ʱ�ڵ���
	public static String POS_RmDeviceInfo() {
		if (cSocket != null) {
			BluetoothDevice rmDevice = cSocket.getRemoteDevice();
			String name = rmDevice.getName();
			String address = rmDevice.getAddress();
			if (name == null)
				name = "Address";
			else if (name.equals(address))
				name = "Address";
			return name + ": " + address;
		} else
			return "";
	}

	// ����Ѿ��ڼ�������ô�Ͳ�����ִ��
	public static void POS_StartListening() {
		synchronized (lock) {
			if (!ReadThread.isReading) {
				ReadThread.isReading = true;
				Message msg = ReadThread.readHandler.obtainMessage();
				msg.what = ReadThread.WHAT_STARTREADING;
				ReadThread.readHandler.sendMessage(msg);
			}
		}
	}

	// ���ӶϿ�ʱ���Ի�ֹͣ����
	public static void POS_StopListening() {
		synchronized (lock) {
			ReadThread.listen = false;
		}
	}

	/**
	 * ��������ӡ���˿ڵ�����д��ָ���ļ���ÿ�ζ����½�һ���ļ��� �ú���������֤��ӡ��һ���յ���Щ���ݡ�ֻ�ǰѽ�Ҫ���͵�����д��
	 * 
	 * @param filePath
	 */
	public static void POS_BeginSavingFile(String filePath) {
		if (filePath != null) {
			if (!filePath.equals("")) {
				File file = new File(filePath);
				savingFilePath = filePath;
				try {
					fos = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					savingFilePath = "";
					try {
						if (fos != null)
							fos.close();
					} catch (IOException e1) {
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param data
	 * @param ReadOrWrite
	 *            0��ʾread��1��ʾwrite �Ѿ������ݽ����˼�飬ֱ��ʹ�ü��ɡ�
	 */

	static synchronized void POS_SavingFile(byte[] data, int ReadOrWrite) {
		if (!Pos.savingFilePath.equals("") && Pos.fos != null) {

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy MM dd  HH:mm:ss", Locale.CHINA);
			Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
			String str = formatter.format(curDate);
			try {
				switch (ReadOrWrite) {
				case 0:
					str = "\nRead " + data.length + " Bytes " + str;
					fos.write(str.getBytes());
					fos.write(byteToHexChar(data).getBytes());
					fos.write("\n".getBytes());
					break;
				case 1: {
					str = "\nSend " + data.length + " Bytes " + str;
					fos.write(str.getBytes());
					fos.write(byteToHexChar(data).getBytes());
					fos.write("\n".getBytes());
					break;
				}
				default:
					break;
				}
			} catch (IOException e) {
				POS_EndSavingFile();
			}
		}
	}

	public static void POS_EndSavingFile() {
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		savingFilePath = "";
	}

	public static boolean POS_Connected() {
		return Connected;
	}

	public static boolean POS_Connecting() {
		return Connecting;
	}

	public static void POS_AutoPairing(boolean autoPairing, String pairingCode) {
		Pos.autoPairing = autoPairing;
		Pos.pairingCode = pairingCode;
	}

	public static void POS_RemoveBondedDevices() {
		Set<BluetoothDevice> devices = myBTAdapter.getBondedDevices();
		if (devices == null)
			return;
		if (devices.size() > 0) {
			for (BluetoothDevice device : devices) {
				try {
					ClsUtils.removeBond(device.getClass(), device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// / <summary>
	// / ����������ӡ������ָ��
	// / </summary>
	static class Cmd {

		/**
		 * ������Կ,һ����8���ֽڵ���Կ
		 */
		public static byte[] DES_SETKEY = { 0x1f, 0x1f, 0x00, 0x08, 0x00, 0x01,
				0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 };

		/**
		 * �������� 0x00,0x00���������ݳ��ȡ�����ʹ���������������ŵ����������� ������᷵�ؼ��ܺ�����ݡ���ͬ��ʽ��
		 */
		public static byte[] DES_ENCRYPT = { 0x1f, 0x1f, 0x01 };

		public static byte[] ERROR = { 0x00 };

		// / <summary>
		// / ��λ��ӡ��
		// / </summary>
		public static byte[] ESC_ALT = { 0x1b, 0x40 };

		// / <summary>
		// / ѡ��ҳģʽ
		// / </summary>
		public static byte[] ESC_L = { 0x1b, 0x4c };

		// / <summary>
		// / ҳģʽ��ȡ����ӡ����
		// / </summary>
		public static byte[] ESC_CAN = { 0x18 };

		// / <summary>
		// / ��ӡ���ص���׼ģʽ����ҳģʽ�£�
		// / </summary>
		public static byte[] FF = { 0x0c };

		// / <summary>
		// / ҳģʽ�´�ӡ��������������
		// / ֻ��ҳģʽ����Ч�����������������
		// / </summary>
		public static byte[] ESC_FF = { 0x1b, 0x0c };

		// / <summary>
		// / ѡ���׼ģʽ
		// / </summary>
		public static byte[] ESC_S = { 0x1b, 0x53 };

		// / <summary>
		// / ���ú���������ƶ���λ
		// / �ֱ𽫺����ƶ���λ�������ó�1/xӢ�磬�����ƶ���λ���ó�1/yӢ�硣
		// / ��x��yΪ0ʱ��x��y�����ó�Ĭ��ֵ200��
		// / </summary>
		public static byte[] GS_P_x_y = { 0x1d, 0x50, 0x00, 0x00 };

		// / <summary>
		// / ѡ������ַ�����ֵ����Ϊ0-15��Ĭ��ֵΪ0����������
		// / </summary>
		public static byte[] ESC_R_n = { 0x1b, 0x52, 0x00 };

		// / <summary>
		// / ѡ���ַ������ֵ����Ϊ0-10,16-19��Ĭ��ֵΪ0��
		// / </summary>
		public static byte[] ESC_t_n = { 0x1b, 0x74, 0x00 };

		// / <summary>
		// / ��ӡ������
		// / </summary>
		public static byte[] LF = { 0x0a };

		// / <summary>
		// / �����м��Ϊ[n*���������ƶ���λ]Ӣ��
		// / </summary>
		public static byte[] ESC_3_n = { 0x1b, 0x33, 0x00 };

		// / <summary>
		// / �����ַ��Ҽ�࣬���ַ��Ŵ�ʱ���Ҽ��Ҳ��֮�Ŵ���ͬ����
		// / </summary>
		public static byte[] ESC_SP_n = { 0x1b, 0x20, 0x00 };

		// / <summary>
		// / ��ָ����Ǯ��������Ų����趨�Ŀ������塣
		// / </summary>
		public static byte[] DLE_DC4_n_m_t = { 0x10, 0x14, 0x01, 0x00, 0x01 };

		// / <summary>
		// / ѡ����ֽģʽ��ֱ����ֽ��0Ϊȫ�У�1Ϊ����
		// / </summary>
		public static byte[] GS_V_m = { 0x1d, 0x56, 0x00 };

		// / <summary>
		// / ��ֽ���Ұ��С�
		// / </summary>
		public static byte[] GS_V_m_n = { 0x1d, 0x56, 0x42, 0x00 };

		// / <summary>
		// / ���ô�ӡ�����ȣ���������ڱ�׼ģʽ������Ч��
		// / �������߾�+��ӡ�����ȡ������ɴ�ӡ�������ӡ������Ϊ�ɴ�ӡ����-��߾ࡣ
		// / </summary>
		public static byte[] GS_W_nL_nH = { 0x1d, 0x57, 0x76, 0x02 };

		// / <summary>
		// / ���þ��Դ�ӡλ��
		// / ����ǰλ�����õ��������ף�nL + nH x 256������
		// / �������λ����ָ����ӡ�����⣬���������
		// / </summary>
		public static byte[] ESC_dollors_nL_nH = { 0x1b, 0x24, 0x00, 0x00 };

		// / <summary>
		// / ѡ���ַ���С
		// / 0-3λѡ���ַ��߶ȣ�4-7λѡ���ַ����
		// / ��ΧΪ��0-7
		// / </summary>
		public static byte[] GS_exclamationmark_n = { 0x1d, 0x21, 0x00 };

		// / <summary>
		// / ѡ������
		// / 0 ��׼ASCII����
		// / 1 ѹ��ASCII����
		// / </summary>
		public static byte[] ESC_M_n = { 0x1b, 0x4d, 0x00 };

		// / <summary>
		// / ѡ��/ȡ���Ӵ�ģʽ
		// / n�����λΪ0��ȡ���Ӵ�ģʽ
		// / n���λΪ1��ѡ��Ӵ�ģʽ
		// / ��0x01����
		// / </summary>
		public static byte[] GS_E_n = { 0x1b, 0x45, 0x00 };

		// / <summary>
		// / ѡ��/ȡ���»���ģʽ
		// / 0 ȡ���»���ģʽ
		// / 1 ѡ���»���ģʽ��1���
		// / 2 ѡ���»���ģʽ��2���
		// / </summary>
		public static byte[] ESC_line_n = { 0x1b, 0x2d, 0x00 };

		// / <summary>
		// / ѡ��/ȡ�����ô�ӡģʽ
		// / 0 Ϊȡ�����ô�ӡ
		// / 1 ѡ���ô�ӡ
		// / </summary>
		public static byte[] ESC_lbracket_n = { 0x1b, 0x7b, 0x00 };

		// / <summary>
		// / ѡ��/ȡ���ڰ׷��Դ�ӡģʽ
		// / n�����λΪ0�ǣ�ȡ�����Դ�ӡ
		// / n�����λΪ1ʱ��ѡ���Դ�ӡ
		// / </summary>
		public static byte[] GS_B_n = { 0x1d, 0x42, 0x00 };

		// / <summary>
		// / ѡ��/ȡ��˳ʱ����ת90��
		// / </summary>
		public static byte[] ESC_V_n = { 0x1b, 0x56, 0x00 };

		// / <summary>
		// / ��ӡ����λͼ
		// / 0 ����
		// / 1 ����
		// / 2 ����
		// / 3 ��������
		// / </summary>
		public static byte[] GS_backslash_m = { 0x1d, 0x2f, 0x00 };

		// / <summary>
		// / ��ӡNVλͼ
		// / ��mָ����ģʽ��ӡflash��ͼ��Ϊn��λͼ
		// / 1��n��255
		// / </summary>
		public static byte[] FS_p_n_m = { 0x1c, 0x70, 0x01, 0x00 };

		// / <summary>
		// / ѡ��HRI�ַ��Ĵ�ӡλ��
		// / 0 ����ӡ
		// / 1 �����Ϸ�
		// / 2 �����·�
		// / 3 �����ϡ��·�����ӡ
		// / </summary>
		public static byte[] GS_H_n = { 0x1d, 0x48, 0x00 };

		// / <summary>
		// / ѡ��HRIʹ������
		// / 0 ��׼ASCII����
		// / 1 ѹ��ASCII����
		// / </summary>
		public static byte[] GS_f_n = { 0x1d, 0x66, 0x00 };

		// / <summary>
		// / ѡ������߶�
		// / 1��n��255
		// / Ĭ��ֵ n=162
		// / </summary>
		public static byte[] GS_h_n = { 0x1d, 0x68, (byte) 0xa2 };

		// / <summary>
		// / ����������
		// / 2��n��6
		// / Ĭ��ֵ n=3
		// / </summary>
		public static byte[] GS_w_n = { 0x1d, 0x77, 0x03 };

		// / <summary>
		// / ��ӡ����
		// / 0x41��m��0x49
		// / n��ȡֵ����������m����
		// / </summary>
		public static byte[] GS_k_m_n_ = { 0x1d, 0x6b, 0x41, 0x0c };

		/**
		 * version: 1 <= v <= 17 error correction level: 1 <= r <= 4
		 */
		public static byte[] GS_k_m_v_r_nL_nH = { 0x1d, 0x6b, 0x61, 0x00, 0x02,
				0x00, 0x00 };

		// / <summary>
		// / ҳģʽ�����ô�ӡ����
		// / �������ڱ�׼ģʽ��ֻ�����ڲ���־λ����Ӱ���ӡ
		// / </summary>
		public static byte[] ESC_W_xL_xH_yL_yH_dxL_dxH_dyL_dyH = { 0x1b, 0x57,
				0x00, 0x00, 0x00, 0x00, 0x48, 0x02, (byte) 0xb0, 0x04 };

		// / <summary>
		// / ��ҳģʽ��ѡ���ӡ������
		// / 0��n��3
		// / </summary>
		public static byte[] ESC_T_n = { 0x1b, 0x54, 0x00 };

		// / <summary>
		// / ҳģʽ�������������λ��
		// / ��������ֻ����ҳģʽ����Ч
		// / </summary>
		public static byte[] GS_dollors_nL_nH = { 0x1d, 0x24, 0x00, 0x00 };

		// / <summary>
		// / ҳģʽ�������������λ��
		// / ҳģʽ�£��Ե�ǰ��λ�ο������������ƶ�����
		// / ��������ֻ��ҳģʽ����Ч
		// / </summary>
		public static byte[] GS_backslash_nL_nH = { 0x1d, 0x5c, 0x00, 0x00 };

		// / <summary>
		// / ѡ��/ȡ�������»���ģʽ
		// / </summary>
		public static byte[] FS_line_n = { 0x1c, 0x2d, 0x00 };

	}

	public static class Pro {
		public static byte[] test = { 0x03, (byte) 0xFF, 0x20, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x08, 0x00, (byte) 0xD4, 0x18, 0x44, 0x45,
				0x56, 0x49, 0x43, 0x45, 0x3F, 0x3F };
		public static byte[] startUpdate = { 0x03, (byte) 0xFF, 0x2F, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xD3, 0x00 };
		// ���³�����±�4,5,6,7Ϊ����ƫ�Ƶ�ַ,10Ϊ��ͷУ���,11Ϊ����У���
		public static byte[] imaUpdate = { 0x03, (byte) 0xFF, 0x2E, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0xD3, 0x00 };
		public static byte[] endUpdate = { 0x03, (byte) 0xFF, 0x2F, 0x00,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00, 0x00,
				(byte) 0xD3, 0x00 };

		// ��������
		public static byte[] fontUpdate = { 0x03, (byte) 0xFF, 0x2D, 0x00,
				0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x00, 0x2E, 0x00 };

		public static byte[] setBaudrate = { 0x03, (byte) 0xFF, 0x2B, 0x00,
				(byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x72, 0x00 };

		public static byte[] setPrintParam = { 0x03, (byte) 0xFF, 0x60, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x10, 0x00, (byte) 0x8C, 0x18,
				// 9600������
				(byte) 0x80, 0x25, 0x00, 0x00,

				// ����
				(byte) 0xFF,

				// ����Ũ�ȣ�0��1��2��2����Ũ
				0x02,

				// ��6 byte Ĭ�����壬 0--12x24 1--9x24 2--9x17 3--8x16 4--16x18
				0x00,

				// ��7 byte ������� 0--0x0A 1--0x0D
				0x00,

				// ��8-9 byte���еȴ�ʱ�䣨��λ���룩�����ֽ��ں�
				0x40, 0x00,
				// ��10-11 byte�Զ��ػ�ʱ�䣨��λ���룩�����ֽ��ں�
				(byte) 0xFF, 0x00,

				// ��12-13 byte��ֽ�������ֽ���ȣ���λ�����ף������ֽ� �ں�
				(byte) 0xFF, 0x00,

				// ��14-15 byte�ڱ����Ѱ�Ҿ��루��λ�����ף������ֽ��� ��
				(byte) 0xFF, 0x00 };

		public static byte[] readFlash = { 0x03, (byte) 0xFF, 0x2C, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xD0, 0x00 };

		public static byte[] setBluetooth = { 0x03, (byte) 0xFF, 0x61, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		public static byte[] setSystemInfo = { 0x03, (byte) 0xFF, 0x64, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	}

	public static class Constant {
		public static final int BARCODE_TYPE_UPC_A = 0x41;
		public static final int BARCODE_TYPE_UPC_E = 0x42;
		public static final int BARCODE_TYPE_EAN13 = 0x43;
		public static final int BARCODE_TYPE_EAN8 = 0x44;
		public static final int BARCODE_TYPE_CODE39 = 0x45;
		public static final int BARCODE_TYPE_ITF = 0x46;
		public static final int BARCODE_TYPE_CODEBAR = 0x47;
		public static final int BARCODE_TYPE_CODE93 = 0x48;
		public static final int BARCODE_TYPE_CODE128 = 0x49;

		public static final int BARCODE_FONTPOSITION_NO = 0x00;
		public static final int BARCODE_FONTPOSITION_ABOVE = 0x01;
		public static final int BARCODE_FONTPOSITION_BELOW = 0x02;
		public static final int BARCODE_FONTPOSITION_ABOVEANDBELOW = 0x03;

		public static final int BARCODE_FONTTYPE_STANDARD = 0x00;
		public static final int BARCODE_FONTTYPE_SMALL = 0x01;

		public static final int CODEPAGE_CHINESE = 255;
		public static final int CODEPAGE_BIG5 = 254;
		public static final int CODEPAGE_UTF_8 = 253;
		public static final int CODEPAGE_SHIFT_JIS = 252;
		public static final int CODEPAGE_EUC_KR = 251;
		public static final int CODEPAGE_CP437_Standard_Europe = 0;
		public static final int CODEPAGE_Katakana = 1;
		public static final int CODEPAGE_CP850_Multilingual = 2;
		public static final int CODEPAGE_CP860_Portuguese = 3;
		public static final int CODEPAGE_CP863_Canadian_French = 4;
		public static final int CODEPAGE_CP865_Nordic = 5;
		public static final int CODEPAGE_WCP1251_Cyrillic = 6;
		public static final int CODEPAGE_CP866_Cyrilliec = 7;
		public static final int CODEPAGE_MIK_Cyrillic_Bulgarian = 8;
		public static final int CODEPAGE_CP755_East_Europe_Latvian_2 = 9;
		public static final int CODEPAGE_Iran = 10;
		public static final int CODEPAGE_CP862_Hebrew = 15;
		public static final int CODEPAGE_WCP1252_Latin_I = 16;
		public static final int CODEPAGE_WCP1253_Greek = 17;
		public static final int CODEPAGE_CP852_Latina_2 = 18;
		public static final int CODEPAGE_CP858_Multilingual_Latin = 19;
		public static final int CODEPAGE_Iran_II = 20;
		public static final int CODEPAGE_Latvian = 21;
		public static final int CODEPAGE_CP864_Arabic = 22;
		public static final int CODEPAGE_ISO_8859_1_West_Europe = 23;
		public static final int CODEPAGE_CP737_Greek = 24;
		public static final int CODEPAGE_WCP1257_Baltic = 25;
		public static final int CODEPAGE_Thai = 26;
		public static final int CODEPAGE_CP720_Arabic = 27;
		public static final int CODEPAGE_CP855 = 28;
		public static final int CODEPAGE_CP857_Turkish = 29;
		public static final int CODEPAGE_WCP1250_Central_Eurpoe = 30;
		public static final int CODEPAGE_CP775 = 31;
		public static final int CODEPAGE_WCP1254_Turkish = 32;
		public static final int CODEPAGE_WCP1255_Hebrew = 33;
		public static final int CODEPAGE_WCP1256_Arabic = 34;
		public static final int CODEPAGE_WCP1258_Vietnam = 35;
		public static final int CODEPAGE_ISO_8859_2_Latin_2 = 36;
		public static final int CODEPAGE_ISO_8859_3_Latin_3 = 37;
		public static final int CODEPAGE_ISO_8859_4_Baltic = 38;
		public static final int CODEPAGE_ISO_8859_5_Cyrillic = 39;
		public static final int CODEPAGE_ISO_8859_6_Arabic = 40;
		public static final int CODEPAGE_ISO_8859_7_Greek = 41;
		public static final int CODEPAGE_ISO_8859_8_Hebrew = 42;
		public static final int CODEPAGE_ISO_8859_9_Turkish = 43;
		public static final int CODEPAGE_ISO_8859_15_Latin_3 = 44;
		public static final int CODEPAGE_Thai2 = 45;
		public static final int CODEPAGE_CP856 = 46;
		public static final int CODEPAGE_Cp874 = 47;

		public static final String[] strcodepages = { "CHINESE", "BIG5",
				"UTF-8", "SHIFT-JIS", "EUC-KR",
				"CP437 [U.S.A., Standard Europe]", "Katakana",
				"CP850 [Multilingual]", "CP860 [Portuguese]",
				"CP863 [Canadian-French]", "CP865 [Nordic]",
				"WCP1251 [Cyrillic]", "CP866 Cyrilliec #2",
				"MIK[Cyrillic /Bulgarian]", "CP755 [East Europe Latvian 2]",
				"Iran", "CP862 [Hebrew]", "WCP1252 Latin I", "WCP1253 [Greek]",
				"CP852 [Latina 2]", "CP858 Multilingual Latin)", "Iran II",
				"Latvian", "CP864 [Arabic]", "ISO-8859-1 [West Europe]",
				"CP737 [Greek]", "WCP1257 [Baltic]", "Thai", "CP720[Arabic]",
				"CP855", "CP857[Turkish]", "WCP1250[Central Eurpoe]", "CP775",
				"WCP1254[Turkish]", "WCP1255[Hebrew]", "WCP1256[Arabic]",
				"WCP1258[Vietnam]", "ISO-8859-2[Latin 2]",
				"ISO-8859-3[Latin 3]", "ISO-8859-4[Baltic]",
				"ISO-8859-5[Cyrillic]", "ISO-8859-6[Arabic]",
				"ISO-8859-7[Greek]", "ISO-8859-8[Hebrew]",
				"ISO-8859-9[Turkish]", "ISO-8859-15 [Latin 3]", "Thai2",
				"CP856", "Cp874" };

		public static final int[] ncodepages = { Pos.Constant.CODEPAGE_CHINESE,
				Pos.Constant.CODEPAGE_BIG5, Pos.Constant.CODEPAGE_UTF_8,
				Pos.Constant.CODEPAGE_SHIFT_JIS, Pos.Constant.CODEPAGE_EUC_KR,
				Pos.Constant.CODEPAGE_CP437_Standard_Europe,
				Pos.Constant.CODEPAGE_Katakana,
				Pos.Constant.CODEPAGE_CP850_Multilingual,
				Pos.Constant.CODEPAGE_CP860_Portuguese,
				Pos.Constant.CODEPAGE_CP863_Canadian_French,
				Pos.Constant.CODEPAGE_CP865_Nordic,
				Pos.Constant.CODEPAGE_WCP1251_Cyrillic,
				Pos.Constant.CODEPAGE_CP866_Cyrilliec,
				Pos.Constant.CODEPAGE_MIK_Cyrillic_Bulgarian,
				Pos.Constant.CODEPAGE_CP755_East_Europe_Latvian_2,
				Pos.Constant.CODEPAGE_Iran, Pos.Constant.CODEPAGE_CP862_Hebrew,
				Pos.Constant.CODEPAGE_WCP1252_Latin_I,
				Pos.Constant.CODEPAGE_WCP1253_Greek,
				Pos.Constant.CODEPAGE_CP852_Latina_2,
				Pos.Constant.CODEPAGE_CP858_Multilingual_Latin,
				Pos.Constant.CODEPAGE_Iran_II, Pos.Constant.CODEPAGE_Latvian,
				Pos.Constant.CODEPAGE_CP864_Arabic,
				Pos.Constant.CODEPAGE_ISO_8859_1_West_Europe,
				Pos.Constant.CODEPAGE_CP737_Greek,
				Pos.Constant.CODEPAGE_WCP1257_Baltic,
				Pos.Constant.CODEPAGE_Thai, Pos.Constant.CODEPAGE_CP720_Arabic,
				Pos.Constant.CODEPAGE_CP855,
				Pos.Constant.CODEPAGE_CP857_Turkish,
				Pos.Constant.CODEPAGE_WCP1250_Central_Eurpoe,
				Pos.Constant.CODEPAGE_CP775,
				Pos.Constant.CODEPAGE_WCP1254_Turkish,
				Pos.Constant.CODEPAGE_WCP1255_Hebrew,
				Pos.Constant.CODEPAGE_WCP1256_Arabic,
				Pos.Constant.CODEPAGE_WCP1258_Vietnam,
				Pos.Constant.CODEPAGE_ISO_8859_2_Latin_2,
				Pos.Constant.CODEPAGE_ISO_8859_3_Latin_3,
				Pos.Constant.CODEPAGE_ISO_8859_4_Baltic,
				Pos.Constant.CODEPAGE_ISO_8859_5_Cyrillic,
				Pos.Constant.CODEPAGE_ISO_8859_6_Arabic,
				Pos.Constant.CODEPAGE_ISO_8859_7_Greek,
				Pos.Constant.CODEPAGE_ISO_8859_8_Hebrew,
				Pos.Constant.CODEPAGE_ISO_8859_9_Turkish,
				Pos.Constant.CODEPAGE_ISO_8859_15_Latin_3,
				Pos.Constant.CODEPAGE_Thai2, Pos.Constant.CODEPAGE_CP856,
				Pos.Constant.CODEPAGE_Cp874 };

		public static final int[] nbaudrate = { 9600, 19200, 38400, 57600,
				115200 };
		public static final String[] strbaudrate = { "9600", "19200", "38400",
				"57600", "115200", };
		public static final int[] ndarkness = { 0, 1, 2 };
		public static final String[] strdarkness = { "light", "standard",
				"dark" };
		public static final int[] ndefaultfont = { 0, 1, 2, 3, 4 };
		public static final String[] strdefaultfont = { "12x24", "9x24",
				"9x17", "8x16", "16x18" };
		public static final int[] nlinefeed = { 0, 1 };
		public static final String[] strlinefeed = { "0A", "0D" };
	}

	public static void POS_Reset() {
		byte[] data = Cmd.ESC_ALT;
		POS_Write(data);
	}

	// 0-255
	public static void POS_SetMotionUnit(int nHorizontalMU, int nVerticalMU) {
		if (nHorizontalMU < 0 || nHorizontalMU > 255 || nVerticalMU < 0
				|| nVerticalMU > 255)
			return;

		byte[] data = Cmd.GS_P_x_y;
		data[2] = (byte) nHorizontalMU;
		data[3] = (byte) nVerticalMU;
		POS_Write(data);
	}

	public static void POS_SetCharSetAndCodePage(int nCharSet, int nCodePage) {
		if (nCharSet < 0 | nCharSet > 15 | nCodePage < 0 | nCodePage > 19
				| (nCodePage > 10 & nCodePage < 16))
			return;

		Cmd.ESC_R_n[2] = (byte) nCharSet;
		Cmd.ESC_t_n[2] = (byte) nCodePage;
		POS_Write(Cmd.ESC_R_n);
		POS_Write(Cmd.ESC_t_n);
	}

	public static void POS_FeedLine() {
		byte[] data = Cmd.LF;
		POS_Write(data);
	}

	public static void POS_SetLineSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESC_3_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESC_3_n;
		POS_Write(data);
	}

	public static void POS_SetRightSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESC_SP_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESC_SP_n;
		POS_Write(data);
	}

	public static void POS_S_SetAreaWidth(int nWidth) {
		if (nWidth < 0 | nWidth > 65535)
			return;

		byte nL = (byte) (nWidth % 0x100);
		byte nH = (byte) (nWidth / 0x100);
		Cmd.GS_W_nL_nH[2] = nL;
		Cmd.GS_W_nL_nH[3] = nH;
		byte[] data = Cmd.GS_W_nL_nH;
		POS_Write(data);
	}

	public static void POS_S_TextOut(String pszString, int nOrgx,
			int nWidthTimes, int nHeightTimes, int nFontType, int nFontStyle) {
		if (nOrgx > 65535 | nOrgx < 0 | nWidthTimes > 7 | nWidthTimes < 0
				| nHeightTimes > 7 | nHeightTimes < 0 | nFontType < 0
				| nFontType > 4)
			return;

		Cmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);

		byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
		byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
		Cmd.GS_exclamationmark_n[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);

		Cmd.ESC_M_n[2] = (byte) nFontType;

		// ������
		// �ݲ�֧��ƽ������
		Cmd.GS_E_n[2] = (byte) ((nFontStyle >> 3) & 0x01);

		Cmd.FS_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);

		Cmd.ESC_lbracket_n[2] = (byte) ((nFontStyle >> 9) & 0x01);

		Cmd.GS_B_n[2] = (byte) ((nFontStyle >> 10) & 0x01);

		Cmd.ESC_V_n[2] = (byte) ((nFontStyle >> 12) & 0x01);

		byte[] pbString = null;
		try {
			pbString = pszString.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			try {
				pbString = pszString.getBytes("BIG5");
			} catch (UnsupportedEncodingException e1) {
				return;
			}
		}
		;

		byte[] data = byteArraysToBytes(new byte[][] { Cmd.ESC_dollors_nL_nH,
				Cmd.GS_exclamationmark_n, Cmd.ESC_M_n, Cmd.GS_E_n,
				Cmd.FS_line_n, Cmd.ESC_lbracket_n, Cmd.GS_B_n, Cmd.ESC_V_n,
				pbString });
		POS_Write(data);

	}

	public static void POS_S_SetBarcode(String strCodedata, int nOrgx,
			int nType, int nWidthX, int nHeight, int nHriFontType,
			int nHriFontPosition) {
		if (nOrgx < 0 | nOrgx > 65535 | nType < 0x41 | nType > 0x49
				| nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
		Cmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.GS_h_n[2] = (byte) nHeight;
		Cmd.GS_f_n[2] = (byte) (nHriFontType & 0x01);
		Cmd.GS_H_n[2] = (byte) (nHriFontPosition & 0x03);
		Cmd.GS_k_m_n_[2] = (byte) nType;
		Cmd.GS_k_m_n_[3] = (byte) bCodeData.length;

		byte[] data = byteArraysToBytes(new byte[][] { Cmd.ESC_dollors_nL_nH,
				Cmd.GS_w_n, Cmd.GS_h_n, Cmd.GS_f_n, Cmd.GS_H_n, Cmd.GS_k_m_n_,
				bCodeData });
		POS_Write(data);

	}

	public static void POS_S_SetQRcode(String strCodedata, int nOrgx,
			int nWidthX, int nErrorCorrectionLevel) {

		if (nOrgx < 0 | nOrgx > 65535 | nWidthX < 2 | nWidthX > 6
				| nErrorCorrectionLevel < 1 | nErrorCorrectionLevel > 4)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
		Cmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
		Cmd.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
		Cmd.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);

		byte[] data = byteArraysToBytes(new byte[][] { Cmd.ESC_dollors_nL_nH,
				Cmd.GS_w_n, Cmd.GS_k_m_v_r_nL_nH, bCodeData });
		POS_Write(data);

	}

	public static void POS_CutPaper(int nDistance) {
		if (nDistance < 0 || nDistance > 255)
			return;
		byte[] data = Cmd.GS_V_m_n;
		data[3] = (byte) nDistance;
		POS_Write(data);

	}

	/**
	 * 
	 * @param path
	 *            ��ʱ����ѡ������
	 */
	public static void POS_PrintText(String path) {
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_PRINTTEXT;
		msg.obj = path;
		WriteThread.writeHandler.sendMessage(msg);
	}

	public static void POS_PrintPic(Bitmap mBitmap, int nWidth, int nMode) {
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_PRINTPIC;
		msg.arg1 = nWidth;
		msg.arg2 = nMode;
		msg.obj = mBitmap;
		WriteThread.writeHandler.sendMessage(msg);
	}

	public static void POS_PrintPic(String path, int nWidth, int nMode) {
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_PRINTPICBYSTRING;
		msg.arg1 = nWidth;
		msg.arg2 = nMode;
		msg.obj = path;
		WriteThread.writeHandler.sendMessage(msg);
	}

	protected static void POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode) {

		// ��ת�ڰף��ٵ��ú�������λͼ
		// ��ת�ڰ�
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		Bitmap rszBitmap = resizeImage(mBitmap, width, height);
		// �ٱ������ŵ�λͼ�Ա����
		// saveMyBitmap(rszBitmap);
		byte[] data = pixToCmd(bitmapToBWPix(rszBitmap), width, nMode);
		POS_Write(data);
	}

	// debug�ã���ת�����ͼƬ���浽λͼ
	static void saveMyBitmap(Bitmap mBitmap) {
		File f = new File(Environment.getExternalStorageDirectory().getPath(),
				"Btatotest.jpeg");
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

	}

	// nWidth����Ϊ8�ı���,���ֻ�����ϲ���Ƽ���
	// ֮����Ū��һά���飬����Ϊһά�����ٶȻ��һ��
	private static int[] p0 = { 0, 0x80 };
	private static int[] p1 = { 0, 0x40 };
	private static int[] p2 = { 0, 0x20 };
	private static int[] p3 = { 0, 0x10 };
	private static int[] p4 = { 0, 0x08 };
	private static int[] p5 = { 0, 0x04 };
	private static int[] p6 = { 0, 0x02 };

	static byte[] pixToCmd(byte[] src, int nWidth, int nMode) {
		// nWidth = 384; nHeight = 582;
		int nHeight = src.length / nWidth;
		byte[] data = new byte[8 + (src.length / 8)];
		data[0] = 0x1d;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = (byte) (nMode & 0x01);
		data[4] = (byte) ((nWidth / 8) % 0x100);// (xl+xh*256)*8 = nWidth
		data[5] = (byte) ((nWidth / 8) / 0x100);
		data[6] = (byte) ((nHeight) % 0x100);// (yl+yh*256) = nHeight
		data[7] = (byte) ((nHeight) / 0x100);
		int k = 0;
		for (int i = 8; i < data.length; i++) {
			// ���У�û�м�Ȩ
			data[i] = (byte) (p0[src[k]] + p1[src[k + 1]] + p2[src[k + 2]]
					+ p3[src[k + 3]] + p4[src[k + 4]] + p5[src[k + 5]]
					+ p6[src[k + 6]] + src[k + 7]);
			k = k + 8;
		}
		return data;

	}

	// 16*16
	static int[][] Floyd16x16 = /* Traditional Floyd ordered dither */
	{
			{ 0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42,
					170 },
			{ 192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74,
					234, 106 },
			{ 48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186,
					26, 154 },
			{ 240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250,
					122, 218, 90 },
			{ 12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38,
					166 },
			{ 204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70,
					230, 102 },
			{ 60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182,
					22, 150 },
			{ 252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246,
					118, 214, 86 },
			{ 3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41,
					169 },
			{ 195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73,
					233, 105 },
			{ 51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185,
					25, 153 },
			{ 243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249,
					121, 217, 89 },
			{ 15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37,
					165 },
			{ 207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69,
					229, 101 },
			{ 63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181,
					21, 149 },
			{ 254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245,
					117, 213, 85 } };
	// 8*8
	static int[][] Floyd8x8 = { { 0, 32, 8, 40, 2, 34, 10, 42 },
			{ 48, 16, 56, 24, 50, 18, 58, 26 },
			{ 12, 44, 4, 36, 14, 46, 6, 38 },
			{ 60, 28, 52, 20, 62, 30, 54, 22 },
			{ 3, 35, 11, 43, 1, 33, 9, 41 },
			{ 51, 19, 59, 27, 49, 17, 57, 25 },
			{ 15, 47, 7, 39, 13, 45, 5, 37 },
			{ 63, 31, 55, 23, 61, 29, 53, 21 } };
	// 4*4
	static int[][] Floyd4x4 = { { 0, 8, 2, 10 }, { 12, 4, 14, 6 },
			{ 3, 11, 1, 9 }, { 15, 7, 13, 5 } };

	// ���ڰ׻Ҷ�ͼint[] ת��Ϊ�ڰ׵�����byte[]
	static byte[] bitmapToBWPix(Bitmap mBitmap) {
		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());

		// for the toGrayscale, we need to select a red or green or blue color
		format_K_dither(pixels, mBitmap.getWidth(), mBitmap.getHeight(), data);

		return data;
	}

	static void format_K_dither(int[] orgpixels, int xsize, int ysize,
			byte[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if ((orgpixels[k] & 0xff) > Floyd16x16[x & 15][y & 15])
					despixels[k] = 0;// black
				else
					despixels[k] = 1;

				k++;
			}
		}

	}

	// transmission
	static void format_K_trans(int[] orgpixels, int xsize, int ysize,
			byte[] despixels) {
		int b = 0; // black
		int w = 255; // white
		int t = 127; // average
		int c = 0; // wucha

		int g = 0; // orgpixel
		int k = 0; // orgpixels offset
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if ((g = (orgpixels[k] & 0xff)) > t) {

					orgpixels[k] = 0; // white
					c = g - w;

					if (((y + 1) < ysize) && ((x + 1) < xsize)) {

						orgpixels[k + xsize + 1] += (0.25 * c);
						orgpixels[k + xsize] += (0.375 * c);
						orgpixels[k + 1] += (0.375 * c);
					} else {

						if ((y + 1) < ysize)
							orgpixels[k + xsize] += (0.375 * c);

						if ((x + 1) < xsize)
							orgpixels[k + 1] += (0.375 * c);

					}

				} else {

					orgpixels[k] = 1; // black
					c = g - b;

					if (((y + 1) < ysize) && ((x + 1) < xsize)) {

						orgpixels[k + xsize + 1] += (0.25 * c);
						orgpixels[k + xsize] += (0.375 * c);
						orgpixels[k + 1] += (0.375 * c);
					} else {

						if ((y + 1) < ysize)
							orgpixels[k + xsize] += (0.375 * c);

						if ((x + 1) < xsize)
							orgpixels[k + 1] += (0.375 * c);

					}
				}
				k++;
			}
		}

		for (int i = 0; i < k; i++)
			despixels[i] = (byte) orgpixels[i];
	}

	// ���ţ���ʱ��Ҫpublic�Ա���ԣ����֮���������
	static Bitmap resizeImage(Bitmap bitmap, int w, int h) {

		// load the origial Bitmap
		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;
	}

	// ת�ɻҶ�ͼ
	static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * ������ֽ����鰴˳��ϲ�
	 * 
	 * @param data
	 * @return
	 */
	static byte[] byteArraysToBytes(byte[][] data) {

		int length = 0;
		for (int i = 0; i < data.length; i++)
			length += data[i].length;
		byte[] send = new byte[length];
		int k = 0;
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[i].length; j++)
				send[k++] = data[i][j];
		return send;
	}

	/**
	 * ������1B 40 �������ַ���ת�����ֽ�0x1b,0x40.
	 * 
	 * @param orgStr
	 * @return
	 */
	public static byte[] StrToCmdBytes(String orgStr) {

		if (orgStr == null)
			return new byte[0];

		byte[] orgData = new byte[orgStr.length()];
		int k = 0;
		char tmp;
		char tmp2;
		for (int i = 0; i + 1 < orgStr.length(); i++) {
			tmp = orgStr.charAt(i);
			tmp2 = orgStr.charAt(i + 1);
			if (tmp == ' ' || tmp == '\n' || tmp == '\t' || tmp2 == ' '
					|| tmp2 == '\n' || tmp2 == '\t')
				continue;
			orgData[k++] = TwoCharToByte(tmp, tmp2);
			i++;
		}

		byte[] data = new byte[k];
		for (int i = 0; i < k; i++)
			data[i] = orgData[i];

		return data;

	}

	// ����"1B 40"���ַ���ת���ֽ�1B40
	private static byte TwoCharToByte(char hc, char lc) {
		byte b1 = 0;
		switch (hc) {
		case '0':
			b1 += 0x00;
			break;
		case '1':
			b1 += 0x10;
			break;
		case '2':
			b1 += 0x20;
			break;
		case '3':
			b1 += 0x30;
			break;
		case '4':
			b1 += 0x40;
			break;
		case '5':
			b1 += 0x50;
			break;
		case '6':
			b1 += 0x60;
			break;
		case '7':
			b1 += 0x70;
			break;
		case '8':
			b1 += 0x80;
			break;
		case '9':
			b1 += (byte) 0x90;
			break;
		case 'A':
		case 'a':
			b1 += (byte) 0xA0;
			break;
		case 'B':
		case 'b':
			b1 += (byte) 0xB0;
			break;
		case 'C':
		case 'c':
			b1 += (byte) 0xC0;
			break;
		case 'D':
		case 'd':
			b1 += (byte) 0xD0;
			break;
		case 'E':
		case 'e':
			b1 += (byte) 0xE0;
			break;
		case 'F':
		case 'f':
			b1 += (byte) 0xF0;
			break;
		}
		switch (lc) {
		case '0':
			b1 += 0x00;
			break;
		case '1':
			b1 += 0x01;
			break;
		case '2':
			b1 += 0x02;
			break;
		case '3':
			b1 += 0x03;
			break;
		case '4':
			b1 += 0x04;
			break;
		case '5':
			b1 += 0x05;
			break;
		case '6':
			b1 += 0x06;
			break;
		case '7':
			b1 += 0x07;
			break;
		case '8':
			b1 += 0x08;
			break;
		case '9':
			b1 += 0x09;
			break;
		case 'A':
		case 'a':
			b1 += 0x0A;
			break;
		case 'B':
		case 'b':
			b1 += 0x0B;
			break;
		case 'C':
		case 'c':
			b1 += 0x0C;
			break;
		case 'D':
		case 'd':
			b1 += 0x0D;
			break;
		case 'E':
		case 'e':
			b1 += 0x0E;
			break;
		case 'F':
		case 'f':
			b1 += 0x0F;
			break;
		}
		return b1;
	}

	// ��1B40�������ֽ�ת��"1B 40 "�������ַ����������Ķ�
	private static String byteToHexChar(byte[] b) {
		if (b == null)
			return "";
		StringBuilder ret = new StringBuilder();
		String tmp = "";
		ret.append('\n');
		for (int i = 0; i < b.length; i++) {
			tmp = Integer.toHexString(b[i] & 0xFF);
			if (tmp.length() == 1)
				ret.append('0');
			ret.append(tmp);
			ret.append(' ');
		}
		return ret.toString();
	}

	@SuppressWarnings("unused")
	private static String byteToHexChar(byte[] b, int offset, int length) {
		StringBuilder ret = new StringBuilder();
		String tmp = "";
		ret.append('\n');
		for (int i = offset; (i < b.length) && (i < offset + length); i++) {
			tmp = Integer.toHexString(b[i] & 0xFF);
			if (tmp.length() == 1)
				ret.append('0');
			ret.append(tmp);
			ret.append(' ');
		}
		return ret.toString();
	}

	static byte bytesToXor(byte[] data, int start, int length) {
		if (length == 0)
			return 0;
		else if (length == 1)
			return data[start];
		else {
			int result = data[start] ^ data[start + 1];
			for (int i = start + 2; i < start + length; i++)
				result ^= data[i];
			return (byte) result;
		}
	}

	@SuppressWarnings("unused")
	private static int intsToXor(int[] data, int start, int length) {
		if (length == 0)
			return 0;
		else if (length == 1)
			return data[start];
		else {
			int result = data[start] ^ data[start + 1];
			for (int i = start + 2; i < start + length; i++)
				result ^= data[i];
			return result;
		}

	}

	/**
	 * 
	 * @param orgdata
	 *            Ԫ����
	 * @param orgstart
	 *            Ԫ������ʼ��ַ
	 * @param desdata
	 *            Ŀ��λ��
	 * @param desstart
	 *            Ŀ��λ����ʼ��ַ
	 * @param copylen
	 *            ����
	 */
	public static void copyBytes(byte[] orgdata, int orgstart, byte[] desdata,
			int desstart, int copylen) {
		for (int i = 0; i < copylen; i++) {
			desdata[desstart + i] = orgdata[orgstart + i];
		}
	}

	/**
	 * �ú���������
	 */
	public static void POS_Test() {
		POS_StartListening();// ��ʡ��Դ
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_READTEST;
		WriteThread.writeHandler.sendMessage(msg);
		UpdateThread.updateThreadStatus = UpdateThread.UPDATESTATUS_TEST;
	}

	@SuppressWarnings("unused")
	private static void POS_SetBaudrate(int nBaudrate) {
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_SETBAUDRATE;
		msg.arg1 = nBaudrate;
		WriteThread.writeHandler.sendMessage(msg);
	}

	public static void POS_SetSystemInfo(String name, String sn) {
		if ((name == null) || (sn == null))
			return;

		byte[] bname = name.getBytes();
		byte[] bsn = sn.getBytes();

		if (bname.length > 31 || bsn.length > 31)
			return;
		int nlength = bname.length + bsn.length + 2;
		byte[] zero = { 0 };
		byte[] data = Pos.byteArraysToBytes(new byte[][] {
				Pos.Pro.setSystemInfo, bname, zero, bsn, zero });
		data[8] = (byte) (nlength & 0xff);
		data[9] = (byte) ((nlength & 0xff00) >> 8);
		data[10] = Pos.bytesToXor(data, 0, 10);
		data[11] = Pos.bytesToXor(data, 12, nlength);
		Pos.POS_Write(data);
	}

	public static void POS_SetPrintParam(byte[] params) {
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_SETPRINTPARAM;
		msg.obj = params;
		WriteThread.writeHandler.sendMessage(msg);
	}

	public static void POS_SetBluetooth(byte[] name, byte[] pwd) {
		byte[] zero = { 0x00 };

		byte[] data = byteArraysToBytes(new byte[][] { Pos.Pro.setBluetooth,
				name, zero, pwd, zero });
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_SETBLUETOOTH;
		msg.obj = data;
		WriteThread.writeHandler.sendMessage(msg);
	}

	/**
	 * ͨѶ���ԣ�nRetryTimesΪ��ʱ���Դ�����Ĭ�ϳ�ʱʱ����500ms ��Ҫע�������㲥Pos.ACTION_TESTFAILED
	 * Pos.ACTION_TESTSUCCESS�����ղ��Խ��
	 * 
	 * @param nRetryTimes
	 */
	public static void POS_ConnectionTest(int nRetryTimes) {
		Pos.POS_StartListening();
		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_GUESTTEST;
		msg.arg1 = nRetryTimes;
		WriteThread.writeHandler.sendMessage(msg);

	}

	public static void POS_Update(String path) {
		UpdateThread.updateThreadStatus = UpdateThread.UPDATESTATUS_TEST;
		POS_StartListening();
		new UpdateThread(path).start();
	}

	public static void POS_FontUpdate(String path) {
		POS_StartListening();
		new UpdateThread(path, UpdateThread.TYPE_FONT).start();
	}

	public static void POS_FontUpdateMutiPackage(String path) {
		POS_StartListening();
		new UpdateThread(path, UpdateThread.TYPE_FONT_MUTIPACKAGE).start();
	}

	/**
	 * ��ȡ�����������ݲ�ת�����ַ���
	 * 
	 * @return
	 */
	public static String POS_ReadAllToString() {
		return byteToHexChar(POS_ReadAll());
	}

	/**
	 * ��ȡ�������������ֽ� Ϊ��ȷ���������ɾ���Ӧ������Ҫ��ȡ����֮ǰ���ȵ���һ�θú�������ջ�����
	 * 
	 * @return
	 */
	public static byte[] POS_ReadAll() {
		byte[] data = ReadThread.Buffer.readAll();
		if (data.length != 0) {
			POS_SavingFile(data, 0);
		}
		return data;
	}

	/**
	 * ����һ��Ŀ¼ ������������ļ��� �������Ǹ�����
	 */
	public static void myJob() {
		Log.i("myJob", "myJob");
		Message msg2 = WriteThread.writeHandler.obtainMessage();
		msg2.what = WriteThread.WHAT_MYJOB_CODEPAGES;
		msg2.obj = Environment.getExternalStorageDirectory()
				+ "/ziku/FONTA-28x28-CP-932-Japanese Shift - JIS/FONTA-14x28-CP-932-Japanese Shift - JIS(A0-DF)";
		WriteThread.writeHandler.sendMessage(msg2);
		Message msg3 = WriteThread.writeHandler.obtainMessage();
		msg3.what = WriteThread.WHAT_MYJOB;
		msg3.obj = Environment.getExternalStorageDirectory()
				+ "/ziku/FONTA-28x28-CP-932-Japanese Shift - JIS/FONTA-28x28-CP-932-Japanese Shift - JIS(8740-879F)";
		WriteThread.writeHandler.sendMessage(msg3);
	}

}
