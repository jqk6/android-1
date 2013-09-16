package com.lvrenyang.rw;

/**
 * TTY�ն˲��������������ʣ������ƣ�У��λ��ֹͣλ������λ�ȡ�
 * @author Administrator
 *
 */
public class TTYTermios {
	/**
	 * �����ʣ�ֻ֧��9600,19200,38400,57600,115200
	 * ���ǣ�SDK���沢û������飬��Ҫ�ͻ��˳������м��
	 */
	public int baudrate = 9600;
	
	/**
	 * �����ƣ�NONE����DTR_RTS
	 */
	public FlowControl flowControl = FlowControl.NONE;
	
	/**
	 * У��λ��֧����У�飬��У�飬żУ�飬����У�飬�հ�У��
	 */
	public Parity parity = Parity.NONE;
	
	/**
	 * ֹͣλ��1,1.5,2
	 */
	public StopBits stopBits = StopBits.ONE;
	/**
	 * ����λ��֧��5,6,7,8������SDK���沢û������飬��Ҫ�ͻ��˳������м��
	 */
	public int dataBits = 8;

	public TTYTermios(int baudrate, FlowControl flowControl, Parity parity,
			StopBits stopBits, int dataBits) {
		this.baudrate = baudrate;
		this.flowControl = flowControl;
		this.parity = parity;
		this.stopBits = stopBits;
		this.dataBits = dataBits;
	}

	/**
	 * ������ö��
	 * @author Administrator
	 *
	 */
	public enum FlowControl {
		NONE, DTR_RTS
	}

	/**
	 * У��λö��
	 * @author Administrator
	 *
	 */
	public enum Parity {
		NONE, ODD, EVEN, SPACE, MARK
	}

	/**
	 * ֹͣλö��
	 * @author Administrator
	 *
	 */
	public enum StopBits {
		ONE, ONEPFIVE, TWO
	}
}
