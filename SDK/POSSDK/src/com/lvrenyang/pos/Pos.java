package com.lvrenyang.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.lvrenyang.rw.PL2303Driver;
import com.lvrenyang.rw.USBSerialPort;
import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.ErrorCode;

/**
 * ����SDK��Ҫ�ĺ��� ����û�������usb���ӣ����Լ̳�Pos�࣬Ȼ��override��������POS_Write,POS_Read��POS_IsOpen��
 * 
 * @author Administrator
 * 
 */
public class Pos extends GMPos {

	USBSerialPort serialPort;
	PL2303Driver mSerial;

	public Pos(USBSerialPort serialPort, PL2303Driver mSerial) {
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

	/**
	 * ��д��ʱ��msʱ�䣬Ĭ��Ϊ500
	 */
	public int timeout = 500;
	int maxPackageSize = 512;

	boolean rwSaveToFile = false;
	String readSaveTo;
	String writeSaveTo;

	/**
	 * ��POS��ӡ��д������
	 * 
	 * @param buffer
	 * @param offset
	 * @param count
	 * @param timeout
	 * @return ����д������ݳ��ȣ����ߴ�����š�
	 */
	public int POS_Write(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	/**
	 * ��POS��ӡ����������
	 * 
	 * @param buffer
	 * @param offset
	 * @param count
	 * @param timeout
	 * @return ���ض�������ݳ��ȣ����ߴ�����š�
	 */
	public int POS_Read(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	public boolean POS_IsOpen() {

		return false;
	}

	/**
	 * ���ֽ����鰴��16�����ַ����ĸ�ʽ�����ݱ��浽�ļ�
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

	/**
	 * ���ַ���д�뵽�ļ�
	 * 
	 * @param text
	 * @param dumpfile
	 */
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

	/**
	 * ��ͨ��POS_Writeд���POS_Read��ȡ�����ݷֱ����һ�ݵ���Ӧ���ļ���
	 * 
	 * @param saveToFile
	 * @param readSaveTo
	 * @param writeSaveTo
	 */
	public void POS_SaveToFile(boolean saveToFile, String readSaveTo,
			String writeSaveTo) {
		this.rwSaveToFile = saveToFile;
		this.readSaveTo = readSaveTo;
		this.writeSaveTo = writeSaveTo;
	}

	/**
	 * ���ô�ӡ�����ж˿ڲ�����
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

	/**
	 * ��λ��ӡ��
	 */
	public void POS_Reset() {
		byte[] data = Cmd.ESCCmd.ESC_ALT;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * ��ӡ����ǰ��ֽһ��
	 */
	public void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * ���ö��뷽ʽ
	 * 
	 * @param align
	 *            0-����� 1-�м���� 2-�Ҷ���
	 */
	public void POS_S_Align(int align) {
		if (align < 0 || align > 2)
			return;
		byte[] data = Cmd.ESCCmd.ESC_a_n;
		data[2] = (byte) align;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * �����иߵ���������Ϊ0-255
	 * 
	 * @param nHeight
	 */
	public void POS_SetLineHeight(int nHeight) {
		if (nHeight < 0 || nHeight > 255)
			return;
		byte[] data = Cmd.ESCCmd.ESC_3_n;
		data[2] = (byte) nHeight;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * ���ô�ӡ�����ƶ���λ��
	 * 
	 * @param nHorizontalMU
	 *            ��ˮƽ�����ϵ��ƶ���λ����Ϊ 25.4 / nHorizontalMU ���ס� ����Ϊ0��255��
	 * @param nVerticalMU
	 *            ����ֱ�����ϵ��ƶ���λ����Ϊ 25.4 / nVerticalMU ���ס�����Ϊ0��255��
	 */
	public void POS_SetMotionUnit(int nHorizontalMU, int nVerticalMU) {
		if (nHorizontalMU < 0 || nHorizontalMU > 255 || nVerticalMU < 0
				|| nVerticalMU > 255)
			return;

		byte[] data = Cmd.ESCCmd.GS_P_x_y;
		data[2] = (byte) nHorizontalMU;
		data[3] = (byte) nVerticalMU;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * �����ַ����ʹ���ҳ
	 * 
	 * @param nCharSet
	 *            ָ�������ַ�������ͬ�Ĺ����ַ�����0x23��0x7E��ASCII��ֵ��Ӧ�ķ��Ŷ����ǲ�ͬ�ġ� Value Meaning
	 *            0x00-U.S.A 0x01-France 0x02-Germany 0x03-U.K. 0x04-Denmark I
	 *            0x05-Sweden 0x06-Italy 0x07-Spain I 0x08-Japan 0x09-Nonway
	 *            0x0A-Denmark II 0x0B-Spain II 0x0C-Latin America 0x0D-Korea
	 * @param nCodePage
	 *            ָ���ַ��Ĵ���ҳ����ͬ�Ĵ���ҳ��0x80��0xFF��ASCII��ֵ��Ӧ�ķ��Ŷ����ǲ�ͬ�ġ�
	 */
	public void POS_SetCharSetAndCodePage(int nCharSet, int nCodePage) {
		if (nCharSet < 0 | nCharSet > 15 | nCodePage < 0 | nCodePage > 19
				| (nCodePage > 10 & nCodePage < 16))
			return;

		Cmd.ESCCmd.ESC_R_n[2] = (byte) nCharSet;
		Cmd.ESCCmd.ESC_t_n[2] = (byte) nCodePage;
		POS_Write(Cmd.ESCCmd.ESC_R_n, 0, Cmd.ESCCmd.ESC_R_n.length, timeout);
		POS_Write(Cmd.ESCCmd.ESC_t_n, 0, Cmd.ESCCmd.ESC_t_n.length, timeout);
	}

	/**
	 * �����ַ��Ҽ��Ϊn��
	 * 
	 * @param nDistance
	 */
	public void POS_SetRightSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESCCmd.ESC_SP_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESCCmd.ESC_SP_n;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * ���ô�ӡ�����ȣ�һ��ı�Я��ӡ����ʹ�õ�ֽ��Ϊ384�㣬�������384�㣬��ӡ�����ܻ᲻�ϡ�
	 * 
	 * @param nWidth
	 */
	public void POS_S_SetAreaWidth(int nWidth) {
		if (nWidth < 0 | nWidth > 65535)
			return;

		byte nL = (byte) (nWidth % 0x100);
		byte nH = (byte) (nWidth / 0x100);
		Cmd.ESCCmd.GS_W_nL_nH[2] = nL;
		Cmd.ESCCmd.GS_W_nL_nH[3] = nH;
		byte[] data = Cmd.ESCCmd.GS_W_nL_nH;
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * �ѽ�Ҫ��ӡ���ַ������ݷ��͵���ӡ�������У���ָ��X ����ˮƽ���ϵľ�����ʼ��λ�ã�ָ��ÿ���ַ���Ⱥ͸߶ȷ����ϵķŴ��������ͺͷ��
	 * 
	 * @param pszString
	 *            Ҫ��ӡ���ַ�����
	 * @param nOrgx
	 *            ָ�� X ����ˮƽ������ʼ��λ������߽�ĵ����� ����Ϊ 0 �� 65535��
	 * @param nWidthTimes
	 *            ָ���ַ��Ŀ�ȷ����ϵķŴ����� ����Ϊ 0 �� 7��0��ʾ���Ŵ�1��ʾ�Ŵ�1��
	 * @param nHeightTimes
	 *            ָ���ַ��߶ȷ����ϵķŴ���������Ϊ 0 �� 7��
	 * @param nFontType
	 *            ָ���ַ����������� 0x00-��׼ ASCII 0x01-ѹ�� ASCII
	 * @param nFontStyle
	 *            ָ���ַ��������񡣿���Ϊһ�������ɸ��� 0x00-���� 0x08-�Ӵ� 0x80-1��ֵ��»���
	 *            0x100-2��ֵ��»��� 0x200-���ã�ֻ��������Ч�� 0x400-���ԣ��ڵװ��֣� 0x1000-ÿ���ַ�˳ʱ����ת
	 *            90 ��
	 */
	public void POS_S_TextOut(String pszString, int nOrgx, int nWidthTimes,
			int nHeightTimes, int nFontType, int nFontStyle) {
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
			pbString = new byte[0];
		}

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_exclamationmark_n,
				tmp_ESC_M_n, Cmd.ESCCmd.GS_E_n, Cmd.ESCCmd.ESC_line_n,
				Cmd.ESCCmd.FS_line_n, Cmd.ESCCmd.ESC_lbracket_n,
				Cmd.ESCCmd.GS_B_n, Cmd.ESCCmd.ESC_V_n, pbString });

		POS_Write(data, 0, data.length, timeout);

	}

	/**
	 * ���ò���ӡ����
	 * 
	 * @param strCodedata
	 *            Ҫ��ӡ�������ַ�����ÿ���ַ�����ķ�Χ�͸�ʽ��������������йء�
	 * @param nOrgx
	 *            ָ����Ҫ��ӡ�������ˮƽ��ʼ������߽�ľ������������Ϊ 0 ��65535��
	 * @param nType
	 *            ָ����������͡� 0x41-UPC-A 0x42-UPC-C 0x43-JAN13(EAN13)
	 *            0x44-JAN8(EAN8) 0x45-CODE39 0x46-INTERLEAVED 2 OF 5
	 *            0x47-CODEBAR 0x48-CODE93 0x49-CODE 128
	 * @param nWidthX
	 *            ָ������Ļ���Ԫ�ؿ�ȡ�2��6����2��6
	 * @param nHeight
	 *            ָ������ĸ߶ȵ���������Ϊ 1 �� 255 ��Ĭ��ֵΪ162 �㡣
	 * @param nHriFontType
	 *            ָ�� HRI��Human Readable Interpretation���ַ����������͡� 0x00-��׼ASCII
	 *            0x01-ѹ��ASCII
	 * @param nHriFontPosition
	 *            ָ��HRI��Human Readable Interpretation���ַ���λ�á� 0x00-����ӡ
	 *            0x01-ֻ�������Ϸ���ӡ 0x02-ֻ�������·���ӡ 0x03-�����ϡ��·�����ӡ
	 */
	public void POS_S_SetBarcode(String strCodedata, int nOrgx, int nType,
			int nWidthX, int nHeight, int nHriFontType, int nHriFontPosition) {
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
		POS_Write(data, 0, data.length, timeout);

	}

	/**
	 * ���ò���ӡQR�루ʹ��GS�����
	 * 
	 * @param strCodedata
	 *            QR������
	 * @param nWidthX
	 *            QR���ȣ���Χ2-6����������
	 * @param nErrorCorrectionLevel
	 *            ����ȼ�������ȼ���level 1��level 4������ȼ�Խ�ߣ��ܱ��������Խ�١�
	 */
	public void POS_S_SetQRcode(String strCodedata, int nWidthX,
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
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * ����bitmap
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return ���ź��bitmap
	 */
	private Bitmap resizeImage(Bitmap bitmap, int w, int h) {

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

	/**
	 * ��λͼ���浽ָ��·����������
	 * 
	 * @param mBitmap
	 * @param path
	 */
	public void saveMyBitmap(Bitmap mBitmap, String path) {
		File f = new File(path);
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

	/**
	 * ��λͼתΪ256�Ҷ�ͼ
	 * 
	 * @param bmpOriginal
	 * @return ת�����λͼ
	 */
	private Bitmap toGrayscale(Bitmap bmpOriginal) {
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
	private int[] p0 = { 0, 0x80 };
	private int[] p1 = { 0, 0x40 };
	private int[] p2 = { 0, 0x20 };
	private int[] p3 = { 0, 0x10 };
	private int[] p4 = { 0, 0x08 };
	private int[] p5 = { 0, 0x04 };
	private int[] p6 = { 0, 0x02 };

	/**
	 * ÿһ���ֽڴ���һ��2ֵλͼ��һ�����ص㣬 �����8�����ص���ϳ�һ�����ص㣬����������ͷ
	 */
	private byte[] pixToCmd(byte[] src, int nWidth, int nMode) {
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
	private int[][] Floyd16x16 = /* Traditional Floyd ordered dither */
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

	/**
	 * ��256ɫ�Ҷ�ͼת��Ϊ2ֵͼ
	 * 
	 * @param orgpixels
	 * @param xsize
	 * @param ysize
	 * @param despixels
	 */
	private void format_K_dither16x16(int[] orgpixels, int xsize, int ysize,
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

	/**
	 * ��Bitmap��ȡλͼ����ת��Ϊ��ֵͼ��0����ڣ�1�����
	 * 
	 * @param mBitmap
	 * @return
	 */
	private byte[] bitmapToBWPix(Bitmap mBitmap) {
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
	 * ��ӡͼƬ���������������������ȷ���POS_S_TextOut����string����Ϊ""���ɡ�
	 * 
	 * @param mBitmap
	 *            ԭʼλͼ
	 * @param nWidth
	 *            Ҫ��ӡ�Ŀ�ȣ�Ҳ���ǽ�λͼ���ŵ���Ӧ��ȣ��߶Ȼᰴ�������š�
	 * @param nMode
	 *            Ϊ0
	 */
	public void POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode) {

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
		int i;
		for (i = 0; i < data.length - maxPackageSize; i += maxPackageSize) {
			POS_Write(data, i, maxPackageSize, timeout);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		POS_Write(data, i, data.length - i, timeout);
	}
}