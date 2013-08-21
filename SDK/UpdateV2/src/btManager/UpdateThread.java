package btManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import android.content.Intent;
import android.util.Log;

//�Ƿ���Ҫ�ظ����ظ�����
public class UpdateThread extends Thread {

	public static final int UPDATESTATUS_TEST = 200;
	public static final int UPDATESTATUS_STARTUPDATE = 201;
	public static final int UPDATESTATUS_IMAUPDATE = 202;
	public static final int UPDATESTATUS_OWRUPDATE = 203;

	public static final String ACTION_DEBUGINFO = "ACTION_DEBUGINFO";
	public static final String EXTRA_DEBUGINFO = "EXTRA_DEBUGINFO";
	// public static final String ACTION_STARTUPDATE = "ACTION_STARTUPDATE";
	public static final String ACTION_ENDUPDATE = "ACTION_ENDUPDATE";

	public static int index = 0;// ��������������0��ʼ
	private static int times = 0;// �������������ʼ��,��ô��Щ������.����
	private static int orglen = 0;
	private static byte[] orgdata = null;
	private static int orgoffset = 0;
	public static String programPath = null;
	public static int updateThreadStatus = UPDATESTATUS_TEST;
	private static int retrytimes = 4;
	private static int timeout = 500;

	private static long minInteval = Long.MAX_VALUE;
	private static long maxInteval = Long.MIN_VALUE;
	private static long sumInteval = 0;
	private static int allPackges = 0;

	public static final int TYPE_PROGRAM = 0;
	public static final int TYPE_FONT = 1;
	public static final int TYPE_FONT_MUTIPACKAGE = 2;
	static int TYPE = TYPE_PROGRAM;

	static int fontIndex = 0;
	private static int fontTimes = 0;
	private static int fontOrglen = 0;
	private static byte[] fontOrgdata = null;
	private static int fontOrgoffset = 0;
	public static String fontPath = null;
	public static boolean checkFontData = false;

	// ������һ���߳�
	public UpdateThread(String programPath) {
		TYPE = TYPE_PROGRAM;
		if (UpdateThread.programPath != null) {
			if (UpdateThread.programPath == programPath && orgdata != null)
				return;
		}
		Intent intent = new Intent(ACTION_DEBUGINFO);
		UpdateThread.programPath = programPath;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(programPath);
			int binflen = fis.available();
			times = (binflen + 255) / 256;
			orglen = times * 256;
			orgdata = new byte[orglen];
			fis.read(orgdata, 0, binflen);
			fis.close();
		} catch (FileNotFoundException e) {
			intent.putExtra(EXTRA_DEBUGINFO, "����ʧ��: �ļ�δ����");
			fis = null;
			orgdata = null;
			System.gc();
			Pos.mContext.sendBroadcast(intent);
		} catch (IOException e) {
			intent.putExtra(EXTRA_DEBUGINFO, "����ʧ��: IO�쳣");
			fis = null;
			orgdata = null;
			System.gc();
			Pos.mContext.sendBroadcast(intent);
		}
	}

	public UpdateThread(String fontPath, int type) {
		TYPE = type;
		if ((TYPE == TYPE_FONT) || (TYPE == TYPE_FONT_MUTIPACKAGE)) {
			if (fontPath != null) {
				if ((UpdateThread.fontPath == fontPath) && fontOrgdata != null)
					return;
			}
			Intent intent = new Intent(ACTION_DEBUGINFO);
			UpdateThread.fontPath = fontPath;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fontPath);
				int binflen = fis.available();
				fontTimes = (binflen + 255) / 256;
				fontOrglen = fontTimes * 256;
				fontOrgdata = new byte[fontOrglen];
				fis.read(fontOrgdata, 0, binflen);
				fis.close();
			} catch (FileNotFoundException e) {
				intent.putExtra(EXTRA_DEBUGINFO, "����ʧ��: �ļ�δ����");
				fis = null;
				fontOrgdata = null;
				System.gc();
				Pos.mContext.sendBroadcast(intent);
			} catch (IOException e) {
				intent.putExtra(EXTRA_DEBUGINFO, "����ʧ��: IO�쳣");
				fis = null;
				fontOrgdata = null;
				System.gc();
				Pos.mContext.sendBroadcast(intent);
			}
		}
	}

	@Override
	public void run() {

		minInteval = Long.MAX_VALUE;
		maxInteval = Long.MIN_VALUE;
		sumInteval = 0;
		allPackges = 0;
		if (TYPE == TYPE_PROGRAM) {
			if (updateThreadStatus == UPDATESTATUS_TEST) {
				updateThreadStatus = UPDATESTATUS_STARTUPDATE;
			}

			if (updateThreadStatus == UPDATESTATUS_STARTUPDATE) {
				index = 0;
				for (int i = 0; i < retrytimes; i++) {
					if (update(UPDATESTATUS_STARTUPDATE))
						break;
				}
			}

			if (updateThreadStatus == UPDATESTATUS_IMAUPDATE) {
				update(UPDATESTATUS_IMAUPDATE);
			}

			if (updateThreadStatus == UPDATESTATUS_OWRUPDATE) {
				for (int i = 0; i < retrytimes; i++) {
					if (update(UPDATESTATUS_OWRUPDATE))
						break;
				}
			}
			Pos.mContext.sendBroadcast(new Intent(ACTION_ENDUPDATE));
		} else if (TYPE == TYPE_FONT) {
			fontIndex = 0;// ��Ҫ����������
			fontUpdate();
			Pos.mContext.sendBroadcast(new Intent(ACTION_ENDUPDATE));
		} else if (TYPE == TYPE_FONT_MUTIPACKAGE) {
			fontIndex = 0;// ��Ҫ����������
			fontUpdateMuti(8);// ÿ�η��͵İ���
			if (checkFontData)
				checkFont(fontOrgdata.clone()); // Ч�飬��ȡ����flash���ݣ������Ƿ񶼶�
			Pos.mContext.sendBroadcast(new Intent(ACTION_ENDUPDATE));
		}
	}

	private boolean update(int updateStatus) {
		switch (updateStatus) {
		case UPDATESTATUS_STARTUPDATE: {

			Log.i("UPDATESTATUS_STARTUPDATE",
					"Pos.POS_Write(Pos.Pro.startUpdate)");

			Intent intent = new Intent(ACTION_DEBUGINFO);
			Pos.POS_Write(Pos.Pro.startUpdate);
			long time = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - time > timeout) {
					intent.putExtra(EXTRA_DEBUGINFO, "����ʧ��! ");
					Pos.mContext.sendBroadcast(intent);
					return false;
				}
				if ((ReadThread.KcCmd == 0x2f)) {
					ReadThread.KcCmd = 0;
					updateThreadStatus = UPDATESTATUS_IMAUPDATE;
					intent.putExtra(EXTRA_DEBUGINFO, "��ʼ����: ");
					Pos.mContext.sendBroadcast(intent);
					return true;
				}
			}
		}

		case UPDATESTATUS_IMAUPDATE: {
			int i;
			for (; index < times; index++) {
				for (i = 0; i < retrytimes; i++) {
					if (imaUpdate(index) == true)
						break;
				}

				if (i == retrytimes) {
					Intent intent = new Intent(ACTION_DEBUGINFO);
					intent.putExtra(EXTRA_DEBUGINFO, "����ʧ�ܣ��밴UPDATE���ԣ�");
					Pos.mContext.sendBroadcast(intent);
					return false;
				}
			}
			updateThreadStatus = UPDATESTATUS_OWRUPDATE;
			return true;
		}

		case UPDATESTATUS_OWRUPDATE: {
			Pos.POS_Write(Pos.Pro.endUpdate);
			long time = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - time > timeout) {
					// debug
					Log.i("UPDATESTATUS_OWRUPDATE:failed", "timeout: "
							+ timeout);

					Intent intent = new Intent(ACTION_DEBUGINFO);
					intent.putExtra(EXTRA_DEBUGINFO, "����ʧ�ܣ��밴UPDATE���ԣ�");
					Pos.mContext.sendBroadcast(intent);
					return false;
				}

				if (ReadThread.KcCmd == 0x2f) {
					ReadThread.KcCmd = 0;

					// debug
					Log.i("UPDATESTATUS_OWRUPDATE:success", "\n��ʱ: "
							+ sumInteval / 1000 + "��");

					updateThreadStatus = UPDATESTATUS_STARTUPDATE;
					Intent intent = new Intent(ACTION_DEBUGINFO);
					intent.putExtra(EXTRA_DEBUGINFO, "�������!" + "\n��ʱ: "
							+ sumInteval / 1000 + "��" + "\nʵ����: " + allPackges);
					Pos.mContext.sendBroadcast(intent);
					return true;
				}
			}

		}

		default:
			return false;
		}
	}

	// ����ÿһ��С����
	private boolean imaUpdate(int index) {

		orgoffset = index * 256;
		byte[] perdata = new byte[268];// ��Ҫ���͵����������,��������ȷ��ÿһ��perdate�����ᱻ���ǵ�
		perdata[0] = 0x03;
		perdata[1] = (byte) 0xff;
		perdata[2] = 0x2e;
		perdata[3] = 0x00;
		perdata[4] = (byte) (orgoffset & 0xff);// ��λ�ֽ���ǰ��
		perdata[5] = (byte) ((orgoffset >> 8) & 0xff);
		perdata[6] = (byte) ((orgoffset >> 16) & 0xff);
		perdata[7] = (byte) ((orgoffset >> 24) & 0xff);
		perdata[8] = 0x00;
		perdata[9] = 0x01;
		perdata[10] = bytesToXor(perdata, 0, 10);
		perdata[11] = bytesToXor(orgdata, orgoffset, 256);
		copyBytes(orgdata, orgoffset, perdata, 12, 256);

		long time = System.currentTimeMillis();
		Pos.POS_Write(perdata);
		allPackges++;
		long inteval = -1;
		while (true) {
			if (System.currentTimeMillis() - time > timeout) {
				// debug
				Log.i("imaUpdate:failed", "index = " + index + " - timeout: "
						+ timeout);
				return false;
			}

			if ((ReadThread.KcCmd == 0x2e) && (ReadThread.KcPara == orgoffset)) {

				ReadThread.KcCmd = 0;
				ReadThread.KcPara = 0;
				// ����յ���ȷ�����ݣ���ô��ҪreadAll.updateStatus���Ϊimaupdate

				inteval = System.currentTimeMillis() - time;
				if (inteval < minInteval)
					minInteval = inteval;
				if (inteval > maxInteval)
					maxInteval = inteval;
				sumInteval += inteval;
				// debug
				Log.i("imaUpdate:success", "index = " + index + " - time: "
						+ inteval);

				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "���½���: " + times + ": "
						+ (index + 1) + "("
						+ (int) ((float) (index + 1) / times * 100) + "%" + ")"
						+ "(" + inteval + ")");
				Pos.mContext.sendBroadcast(intent);
				return true;

			}
		}

	}

	private boolean fontUpdate() {
		int i;
		for (; fontIndex < fontTimes; fontIndex++) {
			for (i = 0; i < retrytimes; i++) {
				if (fontUpdate(fontIndex) == true)
					break;
			}

			if (i == retrytimes) {
				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "�������ʧ�ܣ��밴UPDATE���ԣ�");
				Pos.mContext.sendBroadcast(intent);
				return false;
			}
		}

		if (fontIndex == fontTimes) {
			Intent intent = new Intent(ACTION_DEBUGINFO);
			intent.putExtra(EXTRA_DEBUGINFO, "����������!" + "\n��ʱ: " + sumInteval
					/ 1000 + "��" + "\nʵ����: " + allPackges);
			Pos.mContext.sendBroadcast(intent);
		}
		return true;
	}

	private boolean fontUpdate(int index) {
		fontOrgoffset = index * 256;
		byte[] perdata = new byte[268];// ��Ҫ���͵����������,��������ȷ��ÿһ��perdate�����ᱻ���ǵ�
		perdata[0] = 0x03;
		perdata[1] = (byte) 0xff;
		perdata[2] = 0x63;
		perdata[3] = 0x00;
		perdata[4] = (byte) (fontOrgoffset & 0xff);// ��λ�ֽ���ǰ��
		perdata[5] = (byte) ((fontOrgoffset >> 8) & 0xff);
		perdata[6] = (byte) ((fontOrgoffset >> 16) & 0xff);
		perdata[7] = (byte) ((fontOrgoffset >> 24) & 0xff);
		perdata[8] = 0x00;
		perdata[9] = 0x01;
		perdata[10] = bytesToXor(perdata, 0, 10);
		perdata[11] = bytesToXor(fontOrgdata, fontOrgoffset, 256);
		copyBytes(fontOrgdata, fontOrgoffset, perdata, 12, 256);

		long time = System.currentTimeMillis();
		Pos.POS_Write(perdata);
		allPackges++;
		long inteval = -1;
		while (true) {
			if (System.currentTimeMillis() - time > timeout) {
				// debug
				Log.i("fontUpdate:failed", "index = " + index + " - timeout: "
						+ timeout);
				return false;
			}

			if ((ReadThread.Cmd == 0x63) && (ReadThread.Para == fontOrgoffset)) {

				ReadThread.Cmd = 0;
				ReadThread.Para = 0;
				// ����յ���ȷ�����ݣ���ô��ҪreadAll.updateStatus���Ϊimaupdate

				inteval = System.currentTimeMillis() - time;
				if (inteval < minInteval)
					minInteval = inteval;
				if (inteval > maxInteval)
					maxInteval = inteval;
				sumInteval += inteval;
				// debug
				Log.i("fontUpdate:success", "index = " + index + " - time: "
						+ inteval);

				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "���½���: " + fontTimes + ": "
						+ (fontIndex + 1) + "("
						+ (int) ((float) (fontIndex + 1) / fontTimes * 100)
						+ "%" + ")" + "(" + inteval + ")");
				Pos.mContext.sendBroadcast(intent);
				return true;

			}
		}
	}

	private boolean fontUpdateMuti(int MutiPackageCount) {
		ReadThread.MutiPackageCount = MutiPackageCount;

		int i;
		int mTimes = fontTimes / MutiPackageCount;
		int nCount = fontTimes % MutiPackageCount;
		for (int j = 0; j < mTimes; j++) {
			for (i = 0; i < retrytimes; i++) {
				if (fontUpdate(fontIndex, MutiPackageCount) == true) {
					fontIndex += MutiPackageCount;
					break;
				}
			}
			if (i == retrytimes) {
				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "�������ʧ�ܣ��밴UPDATE���ԣ�");
				fontIndex = 0;// ʧ����Ҫ����������
				Pos.mContext.sendBroadcast(intent);
				return false;
			}
		}

		if (nCount != 0) {
			for (i = 0; i < retrytimes; i++) {
				if (fontUpdate(fontIndex, nCount) == true) {
					fontIndex += nCount;
					break;
				}
			}

			if (i == retrytimes) {
				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "�������ʧ�ܣ��밴UPDATE���ԣ�");
				fontIndex = 0;// ʧ����Ҫ����������
				Pos.mContext.sendBroadcast(intent);
				return false;
			}
		}

		if (fontIndex == fontTimes) {
			Intent intent = new Intent(ACTION_DEBUGINFO);
			intent.putExtra(EXTRA_DEBUGINFO, "����������!" + "\n��ʱ: " + sumInteval
					/ 1000 + "��" + "\nʵ����: " + allPackges);
			Pos.mContext.sendBroadcast(intent);
		}
		return true;
	}

	// �������
	private boolean fontUpdate(int index, int count) {
		byte[] data = new byte[268 * count];
		for (int i = 0; i < count; i++) {
			fontOrgoffset = (index + i) * 256;
			byte[] perdata = new byte[268];// ��Ҫ���͵����������,��������ȷ��ÿһ��perdate�����ᱻ���ǵ�
			perdata[0] = 0x03;
			perdata[1] = (byte) 0xff;
			perdata[2] = 0x63;
			perdata[3] = 0x00;
			perdata[4] = (byte) (fontOrgoffset & 0xff);// ��λ�ֽ���ǰ��
			perdata[5] = (byte) ((fontOrgoffset >> 8) & 0xff);
			perdata[6] = (byte) ((fontOrgoffset >> 16) & 0xff);
			perdata[7] = (byte) ((fontOrgoffset >> 24) & 0xff);
			perdata[8] = 0x00;
			perdata[9] = 0x01;
			perdata[10] = bytesToXor(perdata, 0, 10);
			perdata[11] = bytesToXor(fontOrgdata, fontOrgoffset, 256);
			copyBytes(fontOrgdata, fontOrgoffset, perdata, 12, 256);
			copyBytes(perdata, 0, data, i * 268, 268);
		}
		long time = System.currentTimeMillis();
		// д����֮ǰ���Ȱ���Ҫ�õ�����������
		ReadThread.FontParasCount = 0;
		Pos.POS_Write(data);
		allPackges += count;
		long inteval = -1;
		while (true) {
			if (System.currentTimeMillis() - time > timeout * count) {
				// debug
				Log.i("fontUpdate:failed", "index = " + index + " - timeout: "
						+ timeout * count);
				return false;
			}

			if ((ReadThread.Cmd == 0x63)
					&& (ReadThread.FontParasCount == count)) {
				// ����֮���ùܣ������Լ�����
				inteval = System.currentTimeMillis() - time;
				if (inteval < minInteval)
					minInteval = inteval;
				if (inteval > maxInteval)
					maxInteval = inteval;
				sumInteval += inteval;
				// debug
				Log.i("fontUpdate:success", "index = " + index + " - time: "
						+ inteval);

				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(EXTRA_DEBUGINFO, "���½���: " + fontTimes + ": "
						+ (fontIndex + 1) + "("
						+ (int) ((float) (fontIndex + 1) / fontTimes * 100)
						+ "%" + ")" + "(" + inteval + ")");
				Pos.mContext.sendBroadcast(intent);
				return true;

			}
		}
	}

	private boolean checkFont(byte[] data) {
		Intent intent = new Intent(ACTION_DEBUGINFO);
		intent.putExtra(EXTRA_DEBUGINFO, "����Ч��!");
		Pos.mContext.sendBroadcast(intent);

		Log.i("checkFont", "����Ч��");
		int i;
		int offset;
		int MAXFLASHSIZE = 0x200000;
		int maxOffset = data.length < MAXFLASHSIZE ? data.length : MAXFLASHSIZE;

		byte[] recFlashData = null;
		for (offset = 0; offset < maxOffset; offset += 0x100) {

			for (i = 0; i < retrytimes; i++) {
				if ((recFlashData = readFlash(offset)) != null) {
					byte[] localFontData = new byte[recFlashData.length];
					copyBytes(data, offset, localFontData, 0,
							localFontData.length);
					if (WriteThread.bytesEquals(recFlashData, localFontData))
						break;
				}
			}

			if (i == retrytimes) {
				intent.putExtra(EXTRA_DEBUGINFO, "У��ʧ�ܣ������ԣ�");
				Pos.mContext.sendBroadcast(intent);
				return false;
			}
		}

		if (offset == maxOffset) {
			intent.putExtra(EXTRA_DEBUGINFO, "У��ɹ�");
			Pos.mContext.sendBroadcast(intent);
		}
		return true;
	}

	private byte[] readFlash(int offset) {

		long time = System.currentTimeMillis();
		// д����֮ǰ���Ȱ���Ҫ�õ�����������
		ReadThread.Para_Buf.Count = 0;
		Pos.Pro.readFlash[4] = (byte) (offset & 0xff);
		Pos.Pro.readFlash[5] = (byte) ((offset & 0xff00) >> 8);
		Pos.Pro.readFlash[6] = (byte) ((offset & 0xff0000) >> 16);
		Pos.Pro.readFlash[7] = (byte) ((offset & 0xff000000) >> 24);
		Pos.Pro.readFlash[10] = Pos.bytesToXor(Pos.Pro.readFlash, 0, 10);
		Pos.POS_Write(Pos.Pro.readFlash);
		long inteval = -1;
		while (true) {
			if (System.currentTimeMillis() - time > timeout) {
				// debug
				Log.i("ReadFlash:failed", "offset = " + offset + " - timeout: "
						+ timeout);
				return null;
			}

			if ((ReadThread.Cmd == 0x2c) && (ReadThread.RecData != null)) {

				byte[] retdata = ReadThread.RecData.clone();

				ReadThread.Cmd = 0;
				ReadThread.Para = 0;
				ReadThread.Reclen = 0; // ���ص������У���8-9λ���ֶΣ���ʾ���ݲ��ֳ��ȡ�
				ReadThread.RecData = null;

				// ����֮���ùܣ������Լ�����
				inteval = System.currentTimeMillis() - time;
				if (inteval < minInteval)
					minInteval = inteval;
				if (inteval > maxInteval)
					maxInteval = inteval;
				sumInteval += inteval;
				// debug
				Log.i("ReadFlash:success", "offset = " + offset + " - time: "
						+ inteval);

				Intent intent = new Intent(ACTION_DEBUGINFO);
				intent.putExtra(
						EXTRA_DEBUGINFO,
						"Ч�����: "
								+ (offset + 256)
								/ 256
								+ " offset: "
								+ Integer.toHexString(offset).toUpperCase(
										Locale.getDefault()));
				Pos.mContext.sendBroadcast(intent);
				return retdata;

			}
		}
	}

	private static byte bytesToXor(byte[] data, int start, int length) {
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

	/**
	 * 
	 * @param orgdata
	 * @param orgstart
	 * @param desdata
	 * @param desstart
	 * @param copylen
	 */
	private static void copyBytes(byte[] orgdata, int orgstart, byte[] desdata,
			int desstart, int copylen) {
		for (int i = 0; i < copylen; i++) {
			desdata[desstart + i] = orgdata[orgstart + i];
		}
	}
}
