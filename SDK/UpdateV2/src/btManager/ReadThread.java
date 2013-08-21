package btManager;

import java.io.IOException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

//�жϣ�������ڶ������÷���Ϣ�����û�ڶ���������Ϣ
public class ReadThread extends Thread {
	public static boolean listen = false;
	// ����listen��Ϣ֮ǰ���ж�isReading�Ƿ��ڶ�
	public static boolean isReading = false;
	public static Handler readHandler = null;
	public static final int WHAT_STARTREADING = 10001;
	public static final int WHAT_QUIT = 1000;

	@Override
	public void run() {
		Looper.prepare();
		readHandler = new ReadHandler();
		Looper.loop();
	}

	private static class Kc_Buf {
		public static int Count = 0;
		public static byte[] Buffer = new byte[512];
	};

	static int KcCmd = 0;
	static int KcPara = 0;

	static void HandleKcUartChar(byte ch) {
		// Log.i("HandleKcUartChar", Integer.toHexString(ch & 0xff));
		if (Kc_Buf.Count == 0) { // Check is the start byte OK
			if (ch == 0x03) {
				Kc_Buf.Buffer[0] = 0x03;
				Kc_Buf.Count = 1;
			}
		} else { // Start to receive
			if (Kc_Buf.Count >= 420) { // Package is too large, which is invalid
				Kc_Buf.Count = 0;
			} else {
				Kc_Buf.Buffer[Kc_Buf.Count++] = ch;
			}
			// check the package flag is valid
			if (Kc_Buf.Buffer[1] != (byte) 0xfe)
				Kc_Buf.Count = 0;
			if (Kc_Buf.Count >= 12) { // package received OK?
										// byte lrc;
				int len;

				len = Kc_Buf.Buffer[8] + Kc_Buf.Buffer[9] * 0x100 + 12;
				if (Kc_Buf.Count >= len) { // package is ready?
					Kc_Buf.Count = len;
					// lrc = CalLrc(Kc_Buf.Buffer,Kc_Buf.Count);
					// if (lrc == 0)
					{ // package is valid
						KcCmd = Kc_Buf.Buffer[2] | (Kc_Buf.Buffer[3] << 8);
						KcPara = (Kc_Buf.Buffer[4] & 0xFF)
								| ((Kc_Buf.Buffer[5] & 0xFF) << 8)
								| ((Kc_Buf.Buffer[6] & 0xFF) << 16)
								| ((Kc_Buf.Buffer[7] & 0xFF) << 24);
					}
					Kc_Buf.Count = 0;
				}
			}
		}
	}

	/**
	 * ���ܽ����������512�ֽڣ�ʹ��ʱ��ֻ���ж�Des_Buf.Count�Ƿ��ǵ���ָ���ĳ��ȣ������ȣ���Ч�飬�����㡣
	 * 
	 * @author lvrenyang
	 * 
	 */
	static class Des_Buf {
		public static int Count = 0;
		public static byte[] Buffer = new byte[512];
	}

	/**
	 * DesCmd = 0x1f1f01
	 */
	static int DesCmd = 0;
	static int DesPara = 0;

	static void HandleDesUartChar(byte rec) {
		switch (Des_Buf.Count) {
		case 0: {
			if (rec == 0x1f) {
				Des_Buf.Buffer[0] = rec;
				Des_Buf.Count = 1;
			} else {
				Des_Buf.Count = 0;
			}
			break;
		}
		case 1: {
			if (rec == 0x1f) {
				Des_Buf.Buffer[1] = rec;
				Des_Buf.Count = 2;
			} else {
				Des_Buf.Count = 0;
			}
			break;
		}
		case 2: {
			if (rec == 0x01) {
				Des_Buf.Buffer[2] = rec;
				Des_Buf.Count = 3;
				DesCmd = 0x1f1f01;
			} else {
				Des_Buf.Count = 0;
			}
			break;
		}
		case 3: {
			Des_Buf.Buffer[3] = rec;
			Des_Buf.Count = 4;
			break;
		}
		case 4: {
			Des_Buf.Buffer[4] = rec;
			Des_Buf.Count = 5;
			DesPara = (Des_Buf.Buffer[3] & 0xFF) + (Des_Buf.Buffer[4] & 0xFF)
					* 0x100;
			break;
		}
		default:
			if (Des_Buf.Count < 0) {
				Des_Buf.Count = 0;
				DesCmd = 0;
				DesPara = 0;
				break;
			}

			/**
			 * ��ʱ�����������ܻ���ֲ�ͬ��������
			 */
			if (Des_Buf.Count < DesPara + 5) {
				Des_Buf.Buffer[Des_Buf.Count++] = rec;
			}

		}
	}

	/**
	 * ������
	 * 
	 * @author root
	 * 
	 */
	static class Para_Buf {
		public static int Count = 0;
		public static byte[] Buffer = new byte[512];
	}

	static final int PARA_PRINT = 0;
	static final int PARA_BLUETOOTH = 1;
	static final int PARA_FONTUPDATE = 2;
	static final int PARA_READFLASH = 3;
	static int WHICHPARA = -1;

	// ��������������������Ҫ�õ���һ�����ͱ�����ʹ��֮ǰ��Ҳ�����ͻ��з��ص�����ʱ�������������㡣
	static int Cmd = 0;
	static int Para = 0;
	static int Reclen = 0; // ���ص������У���8-9λ���ֶΣ���ʾ���ݲ��ֳ��ȡ�
	static byte[] RecData = null;

	// һ�η��Ͷ����������ȫ���������ز��������·���ʹ��ǰ��������
	static final boolean MutiPackageFontUpdate = true;
	static int MutiPackageCount = 8;
	static int[] FontParas = new int[MutiPackageCount];
	static int FontParasCount = 0;

	static void HandleParam(byte rec) {
		Log.i("HandleParam", Integer.toHexString(rec & 0xff));
		switch (Para_Buf.Count) {
		case 0: {
			if (rec == 0x03) {
				Para_Buf.Buffer[0] = rec;
				Para_Buf.Count = 1;
			} else {
				Para_Buf.Count = 0;
				WHICHPARA = -1;
			}
			break;
		}

		case 1: {
			if (rec == (byte) 0xfe) {
				Para_Buf.Buffer[1] = rec;
				Para_Buf.Count = 2;
			} else {
				Para_Buf.Count = 0;
				WHICHPARA = -1;
			}
			break;
		}

		/**
		 * 2 - 3 cmd
		 */
		case 2: {
			if (rec == 0x60) {
				Para_Buf.Buffer[2] = rec;
				Para_Buf.Count = 3;
				WHICHPARA = PARA_PRINT;
			} else if (rec == 0x61) {
				Para_Buf.Buffer[2] = rec;
				Para_Buf.Count = 3;
				WHICHPARA = PARA_BLUETOOTH;
			} else if (rec == 0x63) {
				Para_Buf.Buffer[2] = rec;
				Para_Buf.Count = 3;
				WHICHPARA = PARA_FONTUPDATE;
			} else if (rec == 0x2c) {
				Para_Buf.Buffer[2] = rec;
				Para_Buf.Count = 3;
				WHICHPARA = PARA_READFLASH;
			} else {
				Para_Buf.Count = 0;
				WHICHPARA = -1;
			}
			break;
		}

		case 3: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH:
			case PARA_FONTUPDATE:
			case PARA_READFLASH: {
				if (rec == 0x00) {
					Para_Buf.Buffer[3] = rec;
					Para_Buf.Count = 4;
					Cmd = (Para_Buf.Buffer[2] & 0xff)
							+ ((Para_Buf.Buffer[3] & 0xff) << 8);
				} else {
					Para_Buf.Count = 0;
					WHICHPARA = -1;
				}
				break;
			}
			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}
			break;
		}

		/**
		 * 4-7 para
		 */
		case 4:
		case 5:
		case 6:
		case 7: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH: {
				if (rec == 0x00)
					Para_Buf.Buffer[Para_Buf.Count++] = rec;
				else {
					Para_Buf.Count = 0;
					WHICHPARA = -1;
				}
				break;
			}
			case PARA_FONTUPDATE: {
				Para_Buf.Buffer[Para_Buf.Count++] = rec;
				if (Para_Buf.Count == 8) {
					Para = (Para_Buf.Buffer[4] & 0xff)
							+ ((Para_Buf.Buffer[5] & 0xff) << 8)
							+ ((Para_Buf.Buffer[6] & 0xff) << 16)
							+ ((Para_Buf.Buffer[7] & 0xff) << 24);
					if (MutiPackageFontUpdate) {
						if (FontParasCount < MutiPackageCount)
							FontParas[FontParasCount++] = Para;
					}
				}
				break;
			}
			case PARA_READFLASH: {
				Para_Buf.Buffer[Para_Buf.Count++] = rec;
				if (Para_Buf.Count == 8) {
					Para = (Para_Buf.Buffer[4] & 0xff)
							+ ((Para_Buf.Buffer[5] & 0xff) << 8)
							+ ((Para_Buf.Buffer[6] & 0xff) << 16)
							+ ((Para_Buf.Buffer[7] & 0xff) << 24);
				}
				break;
			}
			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}
			break;
		}

		case 8:
		case 9: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH:
			case PARA_FONTUPDATE:
			case PARA_READFLASH: {
				Para_Buf.Buffer[Para_Buf.Count++] = rec;
				if (Para_Buf.Count == 10) {
					Reclen = (Para_Buf.Buffer[8] & 0xff)
							+ ((Para_Buf.Buffer[9] & 0xff) << 8);
				}
				break;
			}
			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}
			break;
		}
		case 10: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH: {
				if (rec == (byte) 0x9D)
					Para_Buf.Buffer[Para_Buf.Count++] = rec;
				else {
					Para_Buf.Count = 0;
					WHICHPARA = -1;
				}
				break;
			}
			case PARA_FONTUPDATE:
			case PARA_READFLASH: {
				if (rec == Pos.bytesToXor(Para_Buf.Buffer, 0, 10))
					Para_Buf.Buffer[Para_Buf.Count++] = rec;
				else {
					Para_Buf.Count = 0;
					WHICHPARA = -1;
				}
				break;
			}
			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}
			break;
		}
		case 11: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH:
			case PARA_FONTUPDATE:
			case PARA_READFLASH: {
				Para_Buf.Buffer[Para_Buf.Count++] = rec;
				break;
			}
			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}
			break;
		}

		default: {
			switch (WHICHPARA) {
			case PARA_PRINT:
			case PARA_BLUETOOTH:
			case PARA_FONTUPDATE: {
				if (rec == 0x03) {
					Para_Buf.Count = 0;
					Para_Buf.Buffer[Para_Buf.Count++] = rec;
				} else
					Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}

			case PARA_READFLASH: {
				Para_Buf.Buffer[Para_Buf.Count++] = rec;
				if ((12 + Reclen) == Para_Buf.Count) {
					// ���ݽ������
					byte[] data = new byte[Reclen];
					for (int i = 0; i < data.length; i++)
						data[i] = Para_Buf.Buffer[12 + i];
					RecData = data;
				}
				break;
			}

			default:
				Para_Buf.Count = 0;
				WHICHPARA = -1;
				break;
			}

			break;
		}
		}
	}

	/**
	 * �����������Զ���գ�����Ҫ���״̬ʱ������Լ��ֶ�����
	 * 
	 * @author lvrenyang
	 * 
	 */
	public static class Buffer {

		private static final Object lock2 = new Object();
		private static byte[] buffer = new byte[1024 * 1024 * 4];// 4M��ȡ������
		private static int count = 0;

		public static void write(byte t) {
			synchronized (lock2) {
				if (count < buffer.length)
					buffer[count++] = t;
				else {
					count = 0;
					buffer[count++] = t;
				}

			}
		}

		public static byte[] readAll() {
			synchronized (lock2) {
				if (count == 0)
					return new byte[0];
				else {
					byte[] data = new byte[count];
					for (int i = 0; i < count; i++)
						data[i] = buffer[i];
					count = 0;
					return data;
				}
			}
		}

		public static void clear() {
			synchronized (lock2) {
				count = 0;
			}
		}

		public static int getCount() {
			synchronized (lock2) {
				return count;
			}
		}
	}

	private static class ReadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case WHAT_STARTREADING: {
				listen = true;
				int available = 0;
				while (listen) {
					if (Pos.POS_Connected() && Pos.bis != null) {
						try {
							available = Pos.bis.available();
							if (available > 0) {
								// InputStream��һ�ζ�ȡ������һ��IO����������װ�����BufferedInputStream�����ڻ��������ٶ�ȡ
								byte[] data = new byte[available];
								Pos.bis.read(data);
								for (int i = 0; i < data.length; i++) {
									Buffer.write(data[i]);
									HandleKcUartChar(data[i]);
									HandleDesUartChar(data[i]);
									HandleParam(data[i]);
								}
							}
						} catch (IOException e) {
							Pos.POS_Close();// �����쳣���˳�READING���ȴ���һ�����ӽ���ʱ�ڶ�ȡ����Test��ʱ���ȡ
							break;
						}
					} else {
						Pos.POS_Close();
						break;
					}
				}
				isReading = false;
				break;
			}

			case WHAT_QUIT:
				Looper.myLooper().quit();
				break;

			default:
				break;
			}

		}
	}
}
