package com.lvrenyang.pos;

import java.io.UnsupportedEncodingException;

public class Cmd {

	public static class PCmd {
		public static byte[] test = { 0x03, (byte) 0xFF, 0x20, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x08, 0x00, (byte) 0xD4, 0x18, 0x44, 0x45,
				0x56, 0x49, 0x43, 0x45, 0x3F, 0x3F };
		public static byte[] testrtstatus = { 0x10, 0x04, 0x01 };
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

	// / <summary>
	// / ����������ӡ������ָ��
	// / </summary>
	public static class ESCCmd {

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

		public static byte[] CR = { 0x0d };

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

		/**
		 * ѡ����뷽ʽ 0 ����� 1 �м���� 2 �Ҷ���
		 */
		public static byte[] ESC_a_n = { 0x1b, 0x61, 0x00 };

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

	public static class Constant {

		private static final String NOTSETKEYHINT = "��ӭʹ�á�\n";

		public static byte[] getNotSetKeyHint() {
			try {
				return NOTSETKEYHINT.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return new byte[0];
			}
		}

		public static final int LAN_ADDR = 0x193000;

		// ��һ�β����ĵ�ַ
		public static final int LAN_INFO_ADDR = LAN_ADDR;
		public static final int LAN_INFO_LEN = 13;

		public static final int PRN_INFO_ADDR = LAN_INFO_ADDR + LAN_INFO_LEN;
		public static final int PRN_INFO_LEN = 40;

		public static final int OEM_NAME_LEN = 40;
		public static final int OEM_INFO_ADDR = (PRN_INFO_ADDR + PRN_INFO_LEN);
		public static final int OEM_INFO_LEN = (OEM_NAME_LEN * 4 + 2);

		public static final int USER_INFO_ADDR = (OEM_INFO_ADDR + OEM_INFO_LEN);
		public static final int USER_INFO_LEN = 50;

		public static final int BT_INFO_ADDR = (USER_INFO_ADDR + USER_INFO_LEN);
		public static final int BT_INFO_LEN = 30;

		public static final int IRD_INFO_ADDR = (BT_INFO_ADDR + BT_INFO_LEN);
		public static final int IRD_INFO_LEN = 30;

		public static final int FAC_INFO_ADDR = (IRD_INFO_ADDR + IRD_INFO_LEN);
		public static final int FAC_INFO_LEN = 68;

		public static final int USER_INFO2_ADDR = (FAC_INFO_ADDR + FAC_INFO_LEN);
		public static final int USER_INFO2_LEN = 20;
		// ���һ�β���

		public static final int PARA_LEN = (USER_INFO2_ADDR + USER_INFO2_LEN - LAN_INFO_ADDR);

		public static final int BT_MAX_NAME_LEN = 12;
		public static final int BT_MAX_PWD_LEN = 15;

		public static final int FAC_MAX_NAME_LEN = 32;
		public static final int FAC_MAX_SN_LEN = 29;

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

		public static final int ALIGN_LEFT = 0x00;
		public static final int ALIGN_CENTER = 0x01;
		public static final int ALIGN_RIGHT = 0x02;

		public static final int FONTSTYLE_NORMAL = 0x00;
		public static final int FONTSTYLE_BOLD = 0x08;
		public static final int FONTSTYLE_UNDERLINE1 = 0x80;
		public static final int FONTSTYLE_UNDERLINE2 = 0x100;
		public static final int FONTSTYLE_UPSIDEDOWN = 0x200;
		public static final int FONTSTYLE_BLACKWHITEREVERSE = 0x400;
		public static final int FONTSTYLE_TURNRIGHT90 = 0x1000;

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

		public static final int[] ncodepages = { Constant.CODEPAGE_CHINESE,
				Constant.CODEPAGE_BIG5, Constant.CODEPAGE_UTF_8,
				Constant.CODEPAGE_SHIFT_JIS, Constant.CODEPAGE_EUC_KR,
				Constant.CODEPAGE_CP437_Standard_Europe,
				Constant.CODEPAGE_Katakana,
				Constant.CODEPAGE_CP850_Multilingual,
				Constant.CODEPAGE_CP860_Portuguese,
				Constant.CODEPAGE_CP863_Canadian_French,
				Constant.CODEPAGE_CP865_Nordic,
				Constant.CODEPAGE_WCP1251_Cyrillic,
				Constant.CODEPAGE_CP866_Cyrilliec,
				Constant.CODEPAGE_MIK_Cyrillic_Bulgarian,
				Constant.CODEPAGE_CP755_East_Europe_Latvian_2,
				Constant.CODEPAGE_Iran, Constant.CODEPAGE_CP862_Hebrew,
				Constant.CODEPAGE_WCP1252_Latin_I,
				Constant.CODEPAGE_WCP1253_Greek,
				Constant.CODEPAGE_CP852_Latina_2,
				Constant.CODEPAGE_CP858_Multilingual_Latin,
				Constant.CODEPAGE_Iran_II, Constant.CODEPAGE_Latvian,
				Constant.CODEPAGE_CP864_Arabic,
				Constant.CODEPAGE_ISO_8859_1_West_Europe,
				Constant.CODEPAGE_CP737_Greek,
				Constant.CODEPAGE_WCP1257_Baltic, Constant.CODEPAGE_Thai,
				Constant.CODEPAGE_CP720_Arabic, Constant.CODEPAGE_CP855,
				Constant.CODEPAGE_CP857_Turkish,
				Constant.CODEPAGE_WCP1250_Central_Eurpoe,
				Constant.CODEPAGE_CP775, Constant.CODEPAGE_WCP1254_Turkish,
				Constant.CODEPAGE_WCP1255_Hebrew,
				Constant.CODEPAGE_WCP1256_Arabic,
				Constant.CODEPAGE_WCP1258_Vietnam,
				Constant.CODEPAGE_ISO_8859_2_Latin_2,
				Constant.CODEPAGE_ISO_8859_3_Latin_3,
				Constant.CODEPAGE_ISO_8859_4_Baltic,
				Constant.CODEPAGE_ISO_8859_5_Cyrillic,
				Constant.CODEPAGE_ISO_8859_6_Arabic,
				Constant.CODEPAGE_ISO_8859_7_Greek,
				Constant.CODEPAGE_ISO_8859_8_Hebrew,
				Constant.CODEPAGE_ISO_8859_9_Turkish,
				Constant.CODEPAGE_ISO_8859_15_Latin_3, Constant.CODEPAGE_Thai2,
				Constant.CODEPAGE_CP856, Constant.CODEPAGE_Cp874 };

		public static String getCodePageStr(int nCodePage) {
			for (int i = 0; i < ncodepages.length; i++) {
				if (ncodepages[i] == nCodePage)
					return strcodepages[i];
			}
			return "";
		}

		public static int getCodePageInt(String strCodePage) {
			for (int i = 0; i < strcodepages.length; i++) {
				if (strcodepages[i].equals(strCodePage))
					return ncodepages[i];
			}
			return -1;
		}

		public static final int[] nbaudrate = { 4800, 9600, 19200, 38400,
				57600, 115200 };
		public static final String[] strbaudrate = { "4800", "9600", "19200",
				"38400", "57600", "115200", };

		public static String getBaudrateStr(int nBaudrate) {
			for (int i = 0; i < nbaudrate.length; i++) {
				if (nbaudrate[i] == nBaudrate)
					return strbaudrate[i];
			}
			return "";
		}

		public static int getBaudrateInt(String strBaudrate) {
			for (int i = 0; i < strbaudrate.length; i++) {
				if (strbaudrate[i].equals(strBaudrate))
					return nbaudrate[i];
			}
			return -1;
		}

		public static final int[] ndarkness = { 0, 1, 2 };
		public static final String[] strdarkness = { "Level 0", "Level 1",
				"Level 2" };

		public static String getDarknessStr(int nDarkness) {
			for (int i = 0; i < ndarkness.length; i++) {
				if (ndarkness[i] == nDarkness)
					return strdarkness[i];
			}
			return "";
		}

		public static int getDarknessInt(String strDarkness) {
			for (int i = 0; i < strdarkness.length; i++) {
				if (strdarkness[i].equals(strDarkness))
					return ndarkness[i];
			}
			return -1;
		}

		public static final int[] ndefaultfont = { 0, 1, 2, 3, 4 };
		public static final String[] strdefaultfont = { "12x24", "9x24",
				"9x17", "8x16", "16x18" };

		public static String getDefaultFontStr(int nDefaultFont) {
			for (int i = 0; i < ndefaultfont.length; i++) {
				if (ndefaultfont[i] == nDefaultFont)
					return strdefaultfont[i];
			}
			return "";
		}

		public static int getDefaultFontInt(String strDefaultFont) {
			for (int i = 0; i < strdefaultfont.length; i++) {
				if (strdefaultfont[i].equals(strDefaultFont))
					return ndefaultfont[i];
			}
			return -1;
		}

		public static final int[] nlinefeed = { 0, 1 };
		public static final String[] strlinefeed = { "LF", "CRLF" };

		public static String getLineFeedStr(int nLineFeed) {
			for (int i = 0; i < nlinefeed.length; i++) {
				if (nlinefeed[i] == nLineFeed)
					return strlinefeed[i];
			}
			return "";
		}

		public static int getLineFeedInt(String strLineFeed) {
			for (int i = 0; i < strlinefeed.length; i++) {
				if (strlinefeed[i].equals(strLineFeed))
					return nlinefeed[i];
			}
			return -1;
		}
	}
}
