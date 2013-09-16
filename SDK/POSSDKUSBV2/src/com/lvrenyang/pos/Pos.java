package com.lvrenyang.pos;

import java.io.File;
import java.io.RandomAccessFile;

import com.lvrenyang.rw.PL2303Driver;
import com.lvrenyang.rw.USBSerialPort;
import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.ErrorCode;

import android.content.Context;

/**
 * ����̳���GMPos ע�⣬GMPos�в�û��POS_Write��POS_Read�ľ���ʵ�� ����Override����ʵ����Ч
 * Pos��������������������Ҫ��д�Ĳ��� ����ǻ����࣬�����Ķ���Ҫ�õ�������� com.lvrenyang.rw���Ǻ;���������صġ�
 * 
 * @author Administrator
 * 
 */
public class Pos extends GMPos {

	Context mContext;
	USBSerialPort serialPort;
	PL2303Driver mSerial;

	public Pos(Context context, USBSerialPort serialPort, PL2303Driver mSerial) {
		mContext = context;
		this.serialPort = serialPort;
		this.mSerial = mSerial;

	}

	@Override
	public int POS_Write(byte[] buffer, int offset, int count, int timeout) {
		if (mSerial == null)
			return ErrorCode.NULLPOINTER;

		int cnt = mSerial.pl2303_write(serialPort, buffer, offset, count,
				timeout);
		if (rwSaveToFile)
			POS_WriteToFile(buffer, offset, cnt, writeSaveTo);
		return cnt;
	}

	@Override
	public int POS_Read(byte[] buffer, int offset, int count, int timeout) {
		if (mSerial == null)
			return ErrorCode.NULLPOINTER;

		int cnt = mSerial.pl2303_read(serialPort, buffer, offset, count,
				timeout);
		if (rwSaveToFile)
			POS_WriteToFile(buffer, offset, cnt, readSaveTo);
		return cnt;
	}

	@Override
	public boolean POS_IsOpen() {
		if (mSerial == null)
			return false;
		return mSerial.pl2303_isOpen(serialPort);
	}

}

class GMPos {

	public int timeout = 500;

	public boolean rwSaveToFile = false;
	public String readSaveTo;
	public String writeSaveTo;

	public int POS_Write(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	public int POS_Read(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	public boolean POS_IsOpen() {

		return false;
	}

	public void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * �����ݴ浽�ļ�
	 */
	public void POS_WriteToFile(byte[] buffer, int offset, int count,
			String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == buffer)
			return;
		if (offset < 0 || count <= 0)
			return;

		byte[] data = new byte[count];
		DataUtils.copyBytes(buffer, offset, data, 0, count);

		String str = DataUtils.bytesToStr(data) + "\r\n";
		// ÿ��д��ʱ��������д
		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(str.getBytes());
			raf.close();
		} catch (Exception e) {
		}
	}

	public void POS_WriteToFile(String text, String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == text)
			return;
		if ("".equals(text))
			return;

		String str = text + "\r\n";
		// ÿ��д��ʱ��������д
		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(str.getBytes());
			raf.close();
		} catch (Exception e) {
		}
	}

	public void POS_SaveToFile(boolean saveToFile, String readSaveTo,
			String writeSaveTo) {
		this.rwSaveToFile = saveToFile;
		this.readSaveTo = readSaveTo;
		this.writeSaveTo = writeSaveTo;
	}

	/**
	 * ������ǰ�Ĳ����ʷ�������
	 * 
	 * @param baudrate
	 */
	public void POS_SetBaudrate(int baudrate) {
		int baudrates[] = { 9600, 19200, 38400, 57600, 115200, 230400 };
		int i;
		for (i = 0; i < baudrates.length; i++)
			if (baudrates[i] == baudrate)
				break;
		if (i == baudrates.length)
			return;
		byte[] data = Cmd.PCmd.setBaudrate;
		data[4] = (byte) (baudrate & 0xff);
		data[5] = (byte) ((baudrate >> 8) & 0xff);
		data[6] = (byte) ((baudrate >> 16) & 0xff);
		data[7] = (byte) ((baudrate >> 24) & 0xff);
		data[10] = DataUtils.bytesToXor(data, 0, 10);
		POS_Write(data, 0, data.length, timeout);
	}
}