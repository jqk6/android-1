package btmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Message;

//û��̫�ྲ̬�̺߳�looper֮��ģ������ܸ��ȶ��˰ɡ�
public class Pos {

	public static boolean check = false;

	public static void APP_Init(Context mContext) {
		if (mContext == null)
			return;
		// ȫ��contextֻΪ�㲥�������߳̾��þֲ�context
		//
		new ConnectThread(mContext).start();
		new WriteThread(mContext).start();
		new ReadThread(mContext).start();
	}

	/**
	 * ��Ϊ��Щhandler�߳������Ƚ������п��ܻ�û���������Ѿ�ȥ���ʣ�����������false��ʾ��
	 * 
	 * @return
	 */
	public static boolean APP_UnInit() {
		boolean retval = true;

		if (ConnectThread.connectHandler != null) {
			Message msg = ConnectThread.connectHandler.obtainMessage();
			msg.what = ConnectThread.WHAT_QUIT;
			ConnectThread.connectHandler.sendMessageAtFrontOfQueue(msg);
		} else {
			retval = false;
		}

		if (WriteThread.writeHandler != null) {
			Message msg = WriteThread.writeHandler.obtainMessage();
			msg.what = WriteThread.WHAT_QUIT;
			WriteThread.writeHandler.sendMessageAtFrontOfQueue(msg);
		} else {
			retval = false;
		}

		if (ReadThread.readHandler != null) {
			Pos.POS_ReadQuit();
			Message msg = ReadThread.readHandler.obtainMessage();
			msg.what = ReadThread.WHAT_QUIT;
			ReadThread.readHandler.sendMessageAtFrontOfQueue(msg);
		} else {
			retval = false;
		}

		return retval;

	}

	// �þ�̬����ͬһʱ��ֻ�ܽ���һ��
	/**
	 * �������ֵ������ʾ��Ϣ�Ƿ��ύ��������������޹� ����Ѿ����ӵ��豸�뼴�����ӵ��豸��ͬ�����cancel��ǰ����豸
	 * 
	 * ��Ϊǰ��Ĳ������ܻ������̣߳������з����ж�����
	 * 
	 * @param btAddress
	 * @return
	 */
	public static synchronized boolean POS_Open(String btAddress) {
		if ((btAddress != null) && (ConnectThread.connectHandler != null)) {
			// Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
			if (ConnectThread.isConnected()) {
				if (btAddress.equals(ConnectThread.getConnectedDevice())) {
					return true;
				} else {
					ConnectThread.cancel();
				}
			} else {
				if (btAddress.equals(ConnectThread.getConnectingDevice())) {
					return true;
				} else {
					ConnectThread.cancel();
				}
			}

			Message msg = ConnectThread.connectHandler.obtainMessage();
			msg.what = ConnectThread.WHAT_CONNECTASCLIENT;
			msg.obj = btAddress;
			ConnectThread.connectHandler.sendMessage(msg);
			return true;
		} else {
			return false;
		}
	}

	// �ú�����Ӧ��ӡ����������������
	/**
	 * �������ֵ������ʾ��Ϣ�Ƿ��ύ��������������޹� ����Ѿ����ӵ��豸�뼴�����ӵ��豸��ͬ�����cancel��ǰ����豸
	 * 
	 * ����������ӵ��豸�뼴�����ӵ��豸��ͬ������ִ���κβ�����
	 * 
	 * @param btAddress
	 * @return
	 */
	public static synchronized boolean POS_OpenAsServer(String btAddress) {
		if ((btAddress != null) && (ConnectThread.connectHandler != null)) {
			// Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
			if (ConnectThread.isConnected()) {
				if (btAddress.equals(ConnectThread.getConnectedDevice())) {
					return true;
				} else {
					ConnectThread.cancel();
				}
			} else {
				if (btAddress.equals(ConnectThread.getConnectingDevice())) {
					return true;
				} else {
					ConnectThread.cancel();
				}
			}

			Message msg = ConnectThread.connectHandler.obtainMessage();
			msg.what = ConnectThread.WHAT_CONNECTASSERVICE;
			msg.obj = btAddress;
			ConnectThread.connectHandler.sendMessage(msg);
			return true;
		} else {
			return false;
		}
	}

	// �Ͽ�����
	// ����Ҫ��һ�������ܹ����ⲿ�Ͽ����ӣ���������������ǱȽϺݵ�
	public static boolean POS_Close() {
		if (ConnectThread.connectHandler != null) {
			// Ϊ����������˳������Ӧ�ø����ɷ�һ����Ϣ��ͨ��ѭ����������
			ConnectThread.cancel();
			return true;
		} else {
			return false;
		}

	}

	// ����д���������û�н������򷵻�false�����򷵻�true��ʾ��Ϣ���͵��˷���
	public static void POS_Write(byte[] data) {
		// ����һ�ݣ��Է��շ����ָ���
		byte[] copy = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			copy[i] = data[i];

		Message msg = WriteThread.writeHandler.obtainMessage();
		msg.what = WriteThread.WHAT_WRITE;
		if (check && !ReadThread.getCheckUp())
			copy = DataUtils.byteArraysToBytes(new byte[][] {
					Cmd.Constant.getNotSetKeyHint(), copy });
		msg.obj = copy;
		WriteThread.writeHandler.sendMessage(msg);
	}

	// �ú���ֻ�迪��һ�Σ��������˳�
	public static void POS_Read() {
		if (!ReadThread.isReading()) {
			ReadThread.setNeedReadFlag(true);
			Message msg = ReadThread.readHandler.obtainMessage();
			msg.what = ReadThread.WHAT_READ;
			ReadThread.readHandler.sendMessage(msg);
		}
	}

	public static void POS_ReadQuit() {
		// �˳�ʱ�����˱�������Ϊ����Ҫ��ȡ�����������˳���
		ReadThread.setNeedReadFlag(false);
	}

	/**
	 * ����һ������ķ���
	 * 
	 * @param data
	 * @param length
	 *            ��Ҫ���صĳ���
	 * @param timeout
	 *            ��ȡ��ʱ
	 */
	public static void POS_Request(byte[] data, int length, int timeout) {

		ReadThread.Request(data, length, timeout);

	}

	/**
	 * ����һ������ķ��أ�������ĸ�ʽ��ѭ����ͨѶЭ�飬���Դ������еõ����ݵĳ���
	 * 
	 * @param data
	 * @param timeout
	 */
	public static void POS_Request(byte[] data, int timeout) {
		ReadThread.Request(data, timeout);
	}

	// ��ȡ����flash
	public static void POS_ReadFlash(int offset, int timeout) {
		if (offset > 0x200000)
			return;

		Cmd.PCmd.readFlash[4] = (byte) (offset & 0xff);
		Cmd.PCmd.readFlash[5] = (byte) ((offset & 0xff00) >> 8);
		Cmd.PCmd.readFlash[6] = (byte) ((offset & 0xff0000) >> 16);
		Cmd.PCmd.readFlash[7] = (byte) ((offset & 0xff000000) >> 24);
		Cmd.PCmd.readFlash[10] = DataUtils
				.bytesToXor(Cmd.PCmd.readFlash, 0, 10);
		ReadThread.Request(Cmd.PCmd.readFlash, timeout);
	}

	public static void POS_CommunicationTest(int timeout) {
		Pos.POS_Request(Cmd.PCmd.test, timeout);

	}

	// ��ʱ��������������request����֤��������
	public static void POS_SetSystemInfo(String name, String sn) {
		if ((name == null) || (sn == null))
			return;

		byte[] bname = name.getBytes();
		byte[] bsn = sn.getBytes();

		if (bname.length > Cmd.Constant.FAC_MAX_NAME_LEN
				|| bsn.length > Cmd.Constant.FAC_MAX_SN_LEN)
			return;
		int nlength = bname.length + bsn.length + 2;
		byte[] zero = { 0 };
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.PCmd.setSystemInfo, bname, zero, bsn, zero });
		data[8] = (byte) (nlength & 0xff);
		data[9] = (byte) ((nlength & 0xff00) >> 8);
		data[10] = DataUtils.bytesToXor(data, 0, 10);
		data[11] = DataUtils.bytesToXor(data, 12, nlength);
		POS_Write(data);
	}

	public static void POS_SetPrintParam(byte[] params) {
		params[10] = DataUtils.bytesToXor(params, 0, 10);
		params[11] = DataUtils.bytesToXor(params, 12, 16);
		POS_Write(params);
	}

	public static void POS_SetBluetooth(byte[] name, byte[] pwd) {
		byte[] zero = { 0x00 };

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.PCmd.setBluetooth, name, zero, pwd, zero });
		int datalength = data.length - 12;
		data[8] = (byte) (datalength & 0xff);
		data[9] = (byte) ((datalength & 0xff00) >> 8);
		data[10] = DataUtils.bytesToXor(data, 0, 10);
		data[11] = DataUtils.bytesToXor(data, 12, datalength);
		POS_Write(data);

	}

	// �Ƿ���debug, Ĭ�ϲ�����
	public static void POS_isDebug(boolean isdebug) {
		ReadThread.setDebug(isdebug);
	}

	// ���Է���������
	public static void POS_ignoreServer() {
		ConnectThread.setIgnoreState(ConnectThread.WHAT_CONNECTASSERVICE);
	}

	public static boolean POS_isServerIgnored() {
		return ConnectThread.getIgnoreState() == ConnectThread.WHAT_CONNECTASSERVICE;
	}

	public static void POS_cleanIgnore() {
		ConnectThread.setIgnoreState(ConnectThread.WHAT_NOIGNORE);
	}

	public static boolean POS_isConnected() {
		return ConnectThread.isConnected();
	}

	public static boolean POS_isConnecting() {
		return ConnectThread.isConnecting();
	}

	public static String POS_getConnectedDevice() {
		return ConnectThread.getConnectedDevice();
	}

	public static String POS_getConnectingDevice() {
		return ConnectThread.getConnectingDevice();
	}

	/**
	 * ������Ϊ����˽�������
	 * 
	 * @return
	 */
	public static boolean POS_isServerConnecting() {
		return ConnectThread.isServerConnecting();
	}

	/**
	 * ��������Ϊ�ͻ��˽�������
	 * 
	 * @return
	 */
	public static boolean POS_isClientConnecting() {
		return ConnectThread.isClientConnecting();
	}

	// ���ţ���ʱ��Ҫpublic�Ա���ԣ����֮���������
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {

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

	// debug�ã���ת�����ͼƬ���浽λͼ
	public static void saveMyBitmap(Bitmap mBitmap) {
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

	// ת�ɻҶ�ͼ
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
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

	private static byte[] pixToCmd(byte[] src, int nWidth, int nMode) {
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
	private static int[][] Floyd16x16 = /* Traditional Floyd ordered dither */
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
	private static int[][] Floyd8x8 = { { 0, 32, 8, 40, 2, 34, 10, 42 },
			{ 48, 16, 56, 24, 50, 18, 58, 26 },
			{ 12, 44, 4, 36, 14, 46, 6, 38 },
			{ 60, 28, 52, 20, 62, 30, 54, 22 },
			{ 3, 35, 11, 43, 1, 33, 9, 41 },
			{ 51, 19, 59, 27, 49, 17, 57, 25 },
			{ 15, 47, 7, 39, 13, 45, 5, 37 },
			{ 63, 31, 55, 23, 61, 29, 53, 21 } };
	// 4*4
	@SuppressWarnings("unused")
	private static int[][] Floyd4x4 = { { 0, 8, 2, 10 }, { 12, 4, 14, 6 },
			{ 3, 11, 1, 9 }, { 15, 7, 13, 5 } };

	/**
	 * ��256ɫ�Ҷ�ͼת��Ϊ2ֵͼ
	 * 
	 * @param orgpixels
	 * @param xsize
	 * @param ysize
	 * @param despixels
	 */
	private static void format_K_dither16x16(int[] orgpixels, int xsize,
			int ysize, byte[] despixels) {
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

	/**
	 * ��256ɫ�Ҷ�ͼת��Ϊ2ֵͼ
	 * 
	 * @param orgpixels
	 * @param xsize
	 * @param ysize
	 * @param despixels
	 */
	private static void format_K_dither8x8(int[] orgpixels, int xsize,
			int ysize, byte[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if (((orgpixels[k] & 0xff) >> 2) > Floyd8x8[x & 7][y & 7])
					despixels[k] = 0;// black
				else
					despixels[k] = 1;

				k++;
			}
		}
	}

	private static int textmodeThreshold = 192;

	/**
	 * ��ARGBͼת��Ϊ2ֵͼ
	 * 
	 * @param orgpixels
	 * @param xsize
	 * @param ysize
	 * @param despixels
	 */
	private static void format_ARGB_textmode(int[] orgpixels, int xsize,
			int ysize, byte[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if (((orgpixels[k] & 0xff) > textmodeThreshold)
						|| (((orgpixels[k] >> 8) & 0xff) > textmodeThreshold)
						|| (((orgpixels[k] >> 16) & 0xff) > textmodeThreshold))
					despixels[k] = 0;// black
				else
					despixels[k] = 1;

				k++;
			}
		}
	}

	private static void format_K_graytextmode(int[] orgpixels, int xsize,
			int ysize, byte[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if ((orgpixels[k] & 0xff) > textmodeThreshold)
					despixels[k] = 0;// black
				else
					despixels[k] = 1;

				k++;
			}
		}
	}

	/**
	 * ��ARGBͼת��Ϊ��ֵͼ��0����ڣ�1�����
	 * 
	 * @param mBitmap
	 * @return
	 */
	public static byte[] bitmapToBWPix(Bitmap mBitmap) {
		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
		Bitmap grayBitmap = toGrayscale(mBitmap);
		grayBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());

		// for the toGrayscale, we need to select a red or green or blue color
		format_K_dither16x16(pixels, grayBitmap.getWidth(),
				grayBitmap.getHeight(), data);

		return data;
	}

	/**
	 * ��ARGBͼʹ��ָ���㷨ת��Ϊ��ֵͼ��0����ڣ�1�����
	 * 
	 * @param mBitmap
	 * @param algorithm
	 * @return
	 */
	private static byte[] bitmapToBWPix(Bitmap mBitmap, int algorithm) {
		int[] pixels = new int[0];
		byte[] data = new byte[0];

		switch (algorithm) {

		case ALGORITHM_DITHER_8x8: {
			// for the toGrayscale, we need to select a red or green or blue
			// color
			Bitmap grayBitmap = toGrayscale(mBitmap);
			pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
			data = new byte[grayBitmap.getWidth() * grayBitmap.getHeight()];
			grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0,
					grayBitmap.getWidth(), grayBitmap.getHeight());
			format_K_dither8x8(pixels, grayBitmap.getWidth(),
					grayBitmap.getHeight(), data);
			break;
		}

		case ALGORITHM_TEXTMODE: {
			// for the toGrayscale, we need to select a red or green or blue
			// color
			pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
			data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
			mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
					mBitmap.getWidth(), mBitmap.getHeight());
			format_ARGB_textmode(pixels, mBitmap.getWidth(),
					mBitmap.getHeight(), data);
			break;
		}

		case ALGORITHM_GRAYTEXTMODE: {
			Bitmap grayBitmap = toGrayscale(mBitmap);
			pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
			data = new byte[grayBitmap.getWidth() * grayBitmap.getHeight()];
			grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0,
					grayBitmap.getWidth(), grayBitmap.getHeight());
			format_K_graytextmode(pixels, grayBitmap.getWidth(),
					grayBitmap.getHeight(), data);
			break;
		}

		case ALGORITHM_DITHER_16x16:
		default: {
			// for the toGrayscale, we need to select a red or green or blue
			// color
			Bitmap grayBitmap = toGrayscale(mBitmap);
			pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
			data = new byte[grayBitmap.getWidth() * grayBitmap.getHeight()];
			grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0,
					grayBitmap.getWidth(), grayBitmap.getHeight());
			format_K_dither16x16(pixels, grayBitmap.getWidth(),
					grayBitmap.getHeight(), data);
			break;
		}
		}
		return data;
	}

	private static void format_K_dither16x16_int(int[] orgpixels, int xsize,
			int ysize, int[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if ((orgpixels[k] & 0xff) > Floyd16x16[x & 15][y & 15])
					despixels[k] = 0xffffffff;// black
				else
					despixels[k] = 0xff000000;

				k++;
			}
		}
	}

	private static void format_K_dither8x8_int(int[] orgpixels, int xsize,
			int ysize, int[] despixels) {
		int k = 0;
		for (int y = 0; y < ysize; y++) {

			for (int x = 0; x < xsize; x++) {

				if (((orgpixels[k] & 0xff) >> 2) > Floyd8x8[x & 7][y & 7])
					despixels[k] = 0xffffffff;// black
				else
					despixels[k] = 0xff000000;

				k++;
			}
		}
	}

	/**
	 * ��ARGBͼ����ָ�����㷨ת��Ϊ��ֵͼ������ԭ���������ϸ��ġ�
	 * 
	 * @param mBitmap
	 * @param algorithm
	 * @return
	 */
	public static int[] bitmapToBWPix_int(Bitmap mBitmap, int algorithm) {
		// for the toGrayscale, we need to select a red or green or blue color
		int[] pixels = new int[0];
		switch (algorithm) {

		case ALGORITHM_DITHER_8x8: {
			// for the toGrayscale, we need to select a red or green or blue
			// color
			Bitmap grayBitmap = toGrayscale(mBitmap);
			pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
			grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0,
					grayBitmap.getWidth(), grayBitmap.getHeight());
			format_K_dither8x8_int(pixels, grayBitmap.getWidth(),
					grayBitmap.getHeight(), pixels);
			break;
		}

		case ALGORITHM_TEXTMODE: {
			// for the toGrayscale, we need to select a red or green or blue
			// color

			break;
		}

		case ALGORITHM_DITHER_16x16:
		default: {
			// for the toGrayscale, we need to select a red or green or blue
			// color
			Bitmap grayBitmap = toGrayscale(mBitmap);
			pixels = new int[grayBitmap.getWidth() * grayBitmap.getHeight()];
			grayBitmap.getPixels(pixels, 0, grayBitmap.getWidth(), 0, 0,
					grayBitmap.getWidth(), grayBitmap.getHeight());
			format_K_dither16x16_int(pixels, grayBitmap.getWidth(),
					grayBitmap.getHeight(), pixels);
			break;
		}
		}

		return pixels;
	}

	/**
	 * ת��Ϊ��ֵλͼ ���ditherָ��Ϊ8����ʹ��8x8������ ����һ��ʹ��16x16
	 * 
	 * @param mBitmap
	 * @param nWidth
	 * @param dither
	 * @return
	 */
	public static final int ALGORITHM_DITHER_16x16 = 16;
	public static final int ALGORITHM_DITHER_8x8 = 8;
	public static final int ALGORITHM_TEXTMODE = 2;
	public static final int ALGORITHM_GRAYTEXTMODE = 1;

	public static Bitmap toBinaryImage(Bitmap mBitmap, int nWidth, int algorithm) {
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		Bitmap rszBitmap = resizeImage(mBitmap, width, height);
		// �ٱ������ŵ�λͼ�Ա����
		// saveMyBitmap(rszBitmap);
		int[] pixels = bitmapToBWPix_int(rszBitmap, algorithm);
		rszBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		return rszBitmap;
	}

	public static void POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode) {

		// ��ת�ڰף��ٵ��ú�������λͼ
		// ��ת�ڰ�
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		Bitmap grayBitmap = toGrayscale(mBitmap);
		Bitmap rszBitmap = resizeImage(grayBitmap, width, height);
		// �ٱ������ŵ�λͼ�Ա����
		// saveMyBitmap(rszBitmap);
		byte[] src = bitmapToBWPix(rszBitmap);// ���ﲻͬ
		byte[] data = pixToCmd(src, width, nMode);
		POS_Write(data);
	}

	public static void POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode,
			int algorithm) {
		// ��ת�ڰף��ٵ��ú�������λͼ
		// ��ת�ڰ�
		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth();
		Bitmap rszBitmap = resizeImage(mBitmap, width, height);
		// �ٱ������ŵ�λͼ�Ա����
		// saveMyBitmap(rszBitmap);
		byte[] src = bitmapToBWPix(rszBitmap, algorithm);// ���ﲻͬ
		byte[] data = pixToCmd(src, width, nMode);
		POS_Write(data);
	}

	// nFontType 0 ��׼ 1 ѹ�� ������ָ��
	public static void POS_S_TextOut(String pszString, int nOrgx,
			int nWidthTimes, int nHeightTimes, int nFontType, int nFontStyle) {
		if (nOrgx > 65535 | nOrgx < 0 | nWidthTimes > 7 | nWidthTimes < 0
				| nHeightTimes > 7 | nHeightTimes < 0 | nFontType < 0
				| nFontType > 4)
			return;

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);

		byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
		byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
		Cmd.ESCCmd.GS_exclamationmark_n[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);

		byte[] tmp_ESC_M_n = Cmd.ESCCmd.ESC_M_n;
		if ((nFontType == 0) || (nFontType == 1))
			tmp_ESC_M_n[2] = (byte) nFontType;
		else
			tmp_ESC_M_n = new byte[0];

		// ������
		// �ݲ�֧��ƽ������
		Cmd.ESCCmd.GS_E_n[2] = (byte) ((nFontStyle >> 3) & 0x01);

		Cmd.ESCCmd.ESC_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);
		Cmd.ESCCmd.FS_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);

		Cmd.ESCCmd.ESC_lbracket_n[2] = (byte) ((nFontStyle >> 9) & 0x01);

		Cmd.ESCCmd.GS_B_n[2] = (byte) ((nFontStyle >> 10) & 0x01);

		Cmd.ESCCmd.ESC_V_n[2] = (byte) ((nFontStyle >> 12) & 0x01);

		byte[] pbString = null;
		try {
			pbString = pszString.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_exclamationmark_n,
				tmp_ESC_M_n, Cmd.ESCCmd.GS_E_n, Cmd.ESCCmd.ESC_line_n,
				Cmd.ESCCmd.FS_line_n, Cmd.ESCCmd.ESC_lbracket_n,
				Cmd.ESCCmd.GS_B_n, Cmd.ESCCmd.ESC_V_n, pbString });

		POS_Write(data);

	}

	public static void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		// byte[] data = Cmd.ESCCmd.LF;
		POS_Write(data);
	}

	public static void POS_S_Align(int align) {
		if (align < 0 || align > 2)
			return;
		byte[] data = Cmd.ESCCmd.ESC_a_n;
		data[2] = (byte) align;
		POS_Write(data);
	}

	public static void POS_SetLineHeight(int nHeight) {
		if (nHeight < 0 || nHeight > 255)
			return;
		byte[] data = Cmd.ESCCmd.ESC_3_n;
		data[2] = (byte) nHeight;
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

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_h_n[2] = (byte) nHeight;
		Cmd.ESCCmd.GS_f_n[2] = (byte) (nHriFontType & 0x01);
		Cmd.ESCCmd.GS_H_n[2] = (byte) (nHriFontPosition & 0x03);
		Cmd.ESCCmd.GS_k_m_n_[2] = (byte) nType;
		Cmd.ESCCmd.GS_k_m_n_[3] = (byte) bCodeData.length;

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_w_n,
				Cmd.ESCCmd.GS_h_n, Cmd.ESCCmd.GS_f_n, Cmd.ESCCmd.GS_H_n,
				Cmd.ESCCmd.GS_k_m_n_, bCodeData });
		POS_Write(data);

	}

	public static void POS_S_SetQRcode(String strCodedata, int nWidthX,
			int nErrorCorrectionLevel) {

		if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1
				| nErrorCorrectionLevel > 4)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.GS_w_n, Cmd.ESCCmd.GS_k_m_v_r_nL_nH, bCodeData });
		POS_Write(data);

	}

	public static void POS_SetKey(byte[] key) {
		byte[] data = Cmd.ESCCmd.DES_SETKEY;
		for (int i = 0; i < key.length; i++) {
			data[i + 5] = key[i];
		}
		// ����DES��Կ����ӡ�����᷵�أ���Ҫ������������
		POS_Write(data);
	}

	public static void POS_SetAutoPairing(boolean enable, String key) {
		if (key.length() != 4)
			return;
		ConnectThread.autoPairing = enable;
		ConnectThread.pairingCode = key;
	}

	/**
	 * ��λ��ӡ��
	 */
	public static void POS_Reset() {
		byte[] data = Cmd.ESCCmd.ESC_ALT;
		POS_Write(data);
	}

	/**
	 * �����ƶ���λ
	 * 
	 * @param nHorizontalMU
	 * @param nVerticalMU
	 */
	public static void POS_SetMotionUnit(int nHorizontalMU, int nVerticalMU) {
		if (nHorizontalMU < 0 || nHorizontalMU > 255 || nVerticalMU < 0
				|| nVerticalMU > 255)
			return;

		byte[] data = Cmd.ESCCmd.GS_P_x_y;
		data[2] = (byte) nHorizontalMU;
		data[3] = (byte) nVerticalMU;
		POS_Write(data);
	}

	/**
	 * �����ַ����ʹ���ҳ
	 * 
	 * @param nCharSet
	 * @param nCodePage
	 */
	public static void POS_SetCharSetAndCodePage(int nCharSet, int nCodePage) {
		if (nCharSet < 0 | nCharSet > 15 | nCodePage < 0 | nCodePage > 19
				| (nCodePage > 10 & nCodePage < 16))
			return;

		Cmd.ESCCmd.ESC_R_n[2] = (byte) nCharSet;
		Cmd.ESCCmd.ESC_t_n[2] = (byte) nCodePage;
		POS_Write(Cmd.ESCCmd.ESC_R_n);
		POS_Write(Cmd.ESCCmd.ESC_t_n);
	}

	/**
	 * �����ַ��Ҽ��
	 * 
	 * @param nDistance
	 */
	public static void POS_SetRightSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESCCmd.ESC_SP_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESCCmd.ESC_SP_n;
		POS_Write(data);
	}

	/**
	 * ���ô�ӡ������
	 * 
	 * @param nWidth
	 */
	public static void POS_S_SetAreaWidth(int nWidth) {
		if (nWidth < 0 | nWidth > 65535)
			return;

		byte nL = (byte) (nWidth % 0x100);
		byte nH = (byte) (nWidth / 0x100);
		Cmd.ESCCmd.GS_W_nL_nH[2] = nL;
		Cmd.ESCCmd.GS_W_nL_nH[3] = nH;
		byte[] data = Cmd.ESCCmd.GS_W_nL_nH;
		POS_Write(data);
	}
}
