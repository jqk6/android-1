package com.lvrenyang.rw;

import com.lvrenyang.rw.TTYTermios.FlowControl;
import com.lvrenyang.utils.ErrorCode;

public class PL2303Driver extends USBSerialDriver {

	private USBDeviceId id[] = { new USBDeviceId(0x067b, 0x2303) };

	private static final int SET_LINE_REQUEST_TYPE = 0x21;
	private static final int SET_LINE_REQUEST = 0x20;

	private static final int SET_CONTROL_REQUEST_TYPE = 0x21;
	private static final int SET_CONTROL_REQUEST = 0x22;
	private static final int CONTROL_DTR = 0x01;
	private static final int CONTROL_RTS = 0x02;

	private static final int GET_LINE_REQUEST_TYPE = 0xa1;
	private static final int GET_LINE_REQUEST = 0x21;

	private static final int VENDOR_WRITE_REQUEST_TYPE = 0x40;
	private static final int VENDOR_WRITE_REQUEST = 0x01;

	private static final int VENDOR_READ_REQUEST_TYPE = 0xc0;
	private static final int VENDOR_READ_REQUEST = 0x01;

	enum pl2303_type {
		type_0, /* don't know the difference between type 0 and */
		type_1, /* type 1, until someone from prolific tells us... */
		HX, /* HX version of the pl2303 chip */
	};

	/**
	 * pl2303 оƬ���ͣ���type_0,type_1,type_HX������
	 * Ĭ��Ϊtype_HX
	 */
	public pl2303_type type = pl2303_type.HX;

	int pl2303_vendor_read(USBSerialPort serial, int value, int index,
			byte[] buffer) {
		if (null == serial)
			return ErrorCode.INVALPARAM;
		return ctl(serial.port, VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST,
				value, index, buffer, 1, 100);
	}

	int pl2303_vendor_write(USBSerialPort serial, int value, int index) {
		if (null == serial)
			return ErrorCode.INVALPARAM;
		return ctl(serial.port, VENDOR_WRITE_REQUEST_TYPE,
				VENDOR_WRITE_REQUEST, value, index, null, 0, 100);
	}

	int set_control_lines(USBSerialPort serial, int value) {
		if (null == serial)
			return ErrorCode.INVALPARAM;
		return ctl(serial.port, SET_CONTROL_REQUEST_TYPE, SET_CONTROL_REQUEST,
				value, 0, null, 0, 100);
	}

	int probe(USBPort port) {
		return super.probe(port, id);
	}

	@Override
	int attach(USBSerialPort serial) {
		// TODO Auto-generated method stub
		if (null == serial)
			return ErrorCode.INVALPARAM;

		type = pl2303_type.type_0;
		byte[] buf = new byte[256];

		if (serial.port.mUsbDevice.getDeviceClass() == 0x02)
			type = pl2303_type.type_0;
		else if (serial.port.mUsbEndpointOut.getMaxPacketSize() == 0x40)
			type = pl2303_type.HX;
		else if (serial.port.mUsbDevice.getDeviceClass() == 0x00)
			type = pl2303_type.type_1;
		else if (serial.port.mUsbDevice.getDeviceClass() == 0xFF)
			type = pl2303_type.type_1;

		pl2303_vendor_read(serial, 0x8484, 0, buf);
		pl2303_vendor_write(serial, 0x0404, 0);
		pl2303_vendor_read(serial, 0x8484, 0, buf);
		pl2303_vendor_read(serial, 0x8383, 0, buf);
		pl2303_vendor_read(serial, 0x8484, 0, buf);
		pl2303_vendor_write(serial, 0x0404, 1);
		pl2303_vendor_read(serial, 0x8484, 0, buf);
		pl2303_vendor_read(serial, 0x8383, 0, buf);
		pl2303_vendor_write(serial, 0, 1);
		pl2303_vendor_write(serial, 1, 0);
		if (type == pl2303_type.HX)
			pl2303_vendor_write(serial, 2, 0x44);
		else
			pl2303_vendor_write(serial, 2, 0x24);

		return 0;
	}

	@Override
	int release(USBSerialPort serial) {
		// TODO Auto-generated method stub
		if (null == serial)
			return ErrorCode.INVALPARAM;

		return 0;
	}

	@Override
	int open(USBSerialPort serial) {
		// TODO Auto-generated method stub
		if (null == serial)
			return ErrorCode.INVALPARAM;

		if (type != pl2303_type.HX) {

		} else {
			pl2303_vendor_write(serial, 8, 0);
			pl2303_vendor_write(serial, 9, 0);
		}
		set_termios(serial, null);

		return 0;
	}

	@Override
	int close(USBSerialPort serial) {
		// TODO Auto-generated method stub
		if (null == serial)
			return ErrorCode.INVALPARAM;

		return 0;
	}

	/**
	 * ���termiosnew��Ϊ�գ���ô����termiosnew�е�ֵ����2303 ���ҽ�serial�е�termios��Ϊtermiosnew
	 */
	@Override
	int set_termios(USBSerialPort serial, TTYTermios termiosnew) {
		int i;
		int baud_sup[] = { 75, 150, 300, 600, 1200, 1800, 2400, 3600, 4800,
				7200, 9600, 14400, 19200, 28800, 38400, 57600, 115200, 230400,
				460800, 614400, 921600, 1228800, 2457600, 3000000, 6000000 };

		if (null == serial)
			return ErrorCode.INVALPARAM;

		if (null == termiosnew)
			termiosnew = serial.termios;

		if (null == termiosnew)
			return ErrorCode.INVALPARAM;

		byte[] buf = new byte[7];

		ctl(serial.port, GET_LINE_REQUEST_TYPE, GET_LINE_REQUEST, 0, 0, buf, 7,
				100);

		// �����������ݵõ������ݺͱ����ѡ��������ֶν����޸ģ�����
		// ��ֱ�Ӹ���termios��������ݽ����޸�
		switch (termiosnew.dataBits) {
		case 5:
			buf[6] = 5;
			break;
		case 6:
			buf[6] = 6;
			break;
		case 7:
			buf[6] = 7;
			break;
		default:
			buf[6] = 8;
			break;
		}

		for (i = 0; i < baud_sup.length; i++) {
			if (baud_sup[i] == termiosnew.baudrate)
				break;
		}
		if (i == baud_sup.length)
			termiosnew.baudrate = 9600;
		if (termiosnew.baudrate > 1228800) {
			if (type != pl2303_type.HX)
				termiosnew.baudrate = 1228800;
			else if (termiosnew.baudrate > 6000000)
				termiosnew.baudrate = termiosnew.baudrate;
		}
		if (termiosnew.baudrate <= 115200) {
			buf[0] = (byte) (termiosnew.baudrate & 0xff);
			buf[1] = (byte) ((termiosnew.baudrate >> 8) & 0xff);
			buf[2] = (byte) ((termiosnew.baudrate >> 16) & 0xff);
			buf[3] = (byte) ((termiosnew.baudrate >> 24) & 0xff);
		} else {
			long tmp = 12 * 1000 * 1000 * 32 / termiosnew.baudrate;
			buf[3] = (byte) 0x80;
			buf[2] = 0;
			buf[1] = (byte) (tmp >= 256 ? 1 : 0);
			while (tmp >= 256) {
				tmp >>= 2;
				buf[1] = (byte) ((buf[1] & 0xff) << 1);
			}
			if (tmp > 256) {
				tmp %= 256;
			}
			buf[0] = (byte) tmp;
		}
		switch (termiosnew.stopBits) {
		case ONE:
			buf[4] = 0;
			break;
		case ONEPFIVE:
			buf[4] = 1;
			break;
		case TWO:
			buf[4] = 2;
			break;
		}

		switch (termiosnew.parity) {
		case NONE:
			buf[5] = 0;
			break;
		case ODD:
			buf[5] = 1;
			break;
		case EVEN:
			buf[5] = 2;
			break;
		case MARK:
			buf[5] = 3;
			break;
		case SPACE:
			buf[5] = 4;
			break;
		}

		ctl(serial.port, SET_LINE_REQUEST_TYPE, SET_LINE_REQUEST, 0, 0, buf, 7,
				100);

		switch (termiosnew.flowControl) {
		case NONE:
			set_control_lines(serial, 0);
			break;
		case DTR_RTS:
			set_control_lines(serial, CONTROL_DTR | CONTROL_RTS);
			break;
		}

		buf[0] = buf[1] = buf[2] = buf[3] = buf[4] = buf[5] = buf[6] = 0;

		ctl(serial.port, GET_LINE_REQUEST_TYPE, GET_LINE_REQUEST, 0, 0, buf, 7,
				100);

		if (termiosnew.flowControl == FlowControl.DTR_RTS) {
			if (type == pl2303_type.HX)
				pl2303_vendor_write(serial, 0x0, 0x61);
			else
				pl2303_vendor_write(serial, 0x0, 0x41);
		} else {
			pl2303_vendor_write(serial, 0x0, 0x0);
		}

		return 0;
	}

	/** �����ǹ��ͻ��˳���Ա���õ� */
	private boolean probed = false;
	private boolean opened = false;

	/**
	 * USB���ϵ�ʱ�򣬵��øú��������뱣֤serial��port��Ա�Ѿ���ʼ��
	 * 
	 * @param serial
	 * @return ����0��ʾ�ɹ�
	 */
	public int pl2303_probe(USBSerialPort serial) {
		if (null == serial)
			return ErrorCode.INVALPARAM;

		int ret;
		ret = probe(serial.port);
		if (ret == 0) {
			ret = attach(serial);
		}
		if (ret == 0)
			probed = true;
		else
			probed = false;
		return ret;
	}

	/**
	 * ��pl2303����
	 * 
	 * @param serial
	 * @return ����0��ʾ�ɹ�
	 */
	public int pl2303_open(USBSerialPort serial) {
		if (null == serial)
			return ErrorCode.INVALPARAM;

		if (!probed) {
			opened = false;
			return ErrorCode.NOTCONNECTED;
		}

		int ret;
		/** �����������usb������ */
		ret = open(serial);
		if (ret == 0)
			opened = true;
		else
			opened = false;
		return 0;
	}

	/**
	 * �ر�pl2303���ڣ�Ŀǰ�汾sdk�ر�pl2303����ֻ�Ǹı��ڲ����
	 * �����ᷢ��ʵ�����ݣ�ֻ��disconnect�Ż�ʵ�ʶϿ�usb����
	 * 
	 * @param serial
	 * @return ����0��ʾ�ɹ�
	 */
	public int pl2303_close(USBSerialPort serial) {
		opened = false;
		if (null == serial)
			return ErrorCode.INVALPARAM;
		close(serial);
		return 0;
	}

	/**
	 * �Ͽ���USB������
	 * 
	 * @param serial
	 */
	public void pl2303_disconnect(USBSerialPort serial) {
		pl2303_close(serial);

		probed = false;
		if (null == serial)
			return;
		release(serial);
		disconnect(serial.port);
	}

	/**
	 * �����Ƿ��
	 * 
	 * @param serial
	 * @return
	 */
	public boolean pl2303_isOpen(USBSerialPort serial) {
		if (!(probed && opened))
			return false;
		if (serial == null)
			return false;
		if (serial.port == null)
			return false;
		if (serial.port.mUsbDeviceConnection == null)
			return false;

		return true;
	}

	/**
	 * ��pl2303����д����
	 * @param serial
	 * @param buffer д�뻺����
	 * @param offset д������ݱ���
	 * @param count Ҫд������ݳ���
	 * @param timeout д�볬ʱ��msʱ��
	 * @return ʵ��д������ݳ���
	 */
	public int pl2303_write(USBSerialPort serial, byte[] buffer, int offset,
			int count, int timeout) {
		if (null == serial)
			return ErrorCode.INVALPARAM;

		if (!pl2303_isOpen(serial))
			return ErrorCode.NOTOPENED;
		try {
			return write(serial.port, buffer, offset, count, timeout);
		} catch (Exception e) {
			return ErrorCode.EXCEPTION;
		}
	}

	/**
	 * ��pl2303���ڶ�����
	 * @param serial
	 * @param buffer ��ȡ������
	 * @param offset �����ƫ�Ƶ�ַ
	 * @param count Ҫ��������ݳ���
	 * @param timeout ��ȡ��ʱ��msʱ��
	 * @return ʵ�ʶ�ȡ�����ݳ���
	 */
	public int pl2303_read(USBSerialPort serial, byte[] buffer, int offset,
			int count, int timeout) {
		if (null == serial)
			return ErrorCode.INVALPARAM;

		if (!pl2303_isOpen(serial))
			return ErrorCode.NOTOPENED;
		try {
			return read(serial.port, buffer, offset, count, timeout);
		} catch (Exception e) {
			return ErrorCode.EXCEPTION;
		}
	}
}
