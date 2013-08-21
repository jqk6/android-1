package com.example.updatev2;

import btManager.Pos;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SetBarcodeActivity extends Activity {

	private EditText etBarcodeOrgx, etBarcodeWidthX, etBarcodeHeight,
			etBarcodeFontType, etBarcodeFontPosition;

	private LinearLayout llSelectBarcode;

	private static final String BARCODE_TYPE_UPC_A = "UPC A";
	private static final String BARCODE_TYPE_UPC_E = "UPC E";
	private static final String BARCODE_TYPE_EAN13 = "EAN 13";
	private static final String BARCODE_TYPE_EAN8 = "EAN 8";
	private static final String BARCODE_TYPE_CODE39 = "CODE 39";
	private static final String BARCODE_TYPE_ITF = "ITF";
	private static final String BARCODE_TYPE_CODEBAR = "CODEBAR";
	private static final String BARCODE_TYPE_CODE93 = "CODE 93";
	private static final String BARCODE_TYPE_CODE128 = "CODE 128";

	private static final String[] barcodeTypes = { BARCODE_TYPE_UPC_A,
			BARCODE_TYPE_UPC_E, BARCODE_TYPE_EAN13, BARCODE_TYPE_EAN8,
			BARCODE_TYPE_CODE39, BARCODE_TYPE_ITF, BARCODE_TYPE_CODEBAR,
			BARCODE_TYPE_CODE93, BARCODE_TYPE_CODE128 };
	private static final int[] nBarcodeTypes = {
			Pos.Constant.BARCODE_TYPE_UPC_A, Pos.Constant.BARCODE_TYPE_UPC_E,
			Pos.Constant.BARCODE_TYPE_EAN13, Pos.Constant.BARCODE_TYPE_EAN8,
			Pos.Constant.BARCODE_TYPE_CODE39, Pos.Constant.BARCODE_TYPE_ITF,
			Pos.Constant.BARCODE_TYPE_CODEBAR,
			Pos.Constant.BARCODE_TYPE_CODE93, Pos.Constant.BARCODE_TYPE_CODE128 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setbarcode);

		etBarcodeOrgx = (EditText) findViewById(R.id.editTextBarcodeOrgx);
		etBarcodeWidthX = (EditText) findViewById(R.id.editTextBarcodeWidthX);
		etBarcodeHeight = (EditText) findViewById(R.id.editTextBarcodeHeight);
		etBarcodeFontType = (EditText) findViewById(R.id.editTextBarcodeFontType);
		etBarcodeFontPosition = (EditText) findViewById(R.id.editTextBarcodeFontPosition);

		llSelectBarcode = (LinearLayout) findViewById(R.id.linearLayoutBarcodetype);

		for (int i = 0; i < barcodeTypes.length; i++) {
			Button btBarcodeType = new Button(this);
			btBarcodeType.setGravity(Gravity.LEFT);
			btBarcodeType.setText(barcodeTypes[i]);
			btBarcodeType.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String strBarcodeType = ((Button) v).getText().toString();
					int nOrgx = 0, nType = Pos.Constant.BARCODE_TYPE_UPC_A, nWidthX = 2, nHeight = 160, nHriFontType = Pos.Constant.BARCODE_FONTTYPE_STANDARD, nHriFontPosition = Pos.Constant.BARCODE_FONTPOSITION_BELOW;
					try {
						nOrgx = Integer.parseInt(etBarcodeOrgx.getText()
								.toString().trim());
						nWidthX = Integer.parseInt(etBarcodeWidthX.getText()
								.toString().trim());
						nHeight = Integer.parseInt(etBarcodeHeight.getText()
								.toString().trim());
						nHriFontType = Integer.parseInt(etBarcodeFontType
								.getText().toString().trim());
						nHriFontPosition = Integer
								.parseInt(etBarcodeFontPosition.getText()
										.toString().trim());
					} catch (NumberFormatException e) {
						Toast.makeText(getApplicationContext(),
								"�����ʽ��������������!", Toast.LENGTH_SHORT).show();
						return;
					}

					for (int j = 0; j < barcodeTypes.length; j++) {
						if (strBarcodeType.equals(barcodeTypes[j])) {
							nType = nBarcodeTypes[j];
							if (checkParam(nOrgx, nType, nWidthX, nHeight,
									nHriFontType, nHriFontPosition)) {
								setParam(nOrgx, nType, nWidthX, nHeight,
										nHriFontType, nHriFontPosition);
								Toast.makeText(getApplicationContext(), "���óɹ�",
										Toast.LENGTH_SHORT).show();
								SetBarcodeActivity.this.finish();
							}
							break;
						}
					}
				}

			});
			llSelectBarcode.addView(btBarcodeType);
		}

	}

	/**
	 * �������Ƿ����Ҫ��
	 * 
	 * @param nOrgx
	 * @param nType
	 * @param nWidthX
	 * @param nHeight
	 * @param nHriFontType
	 * @param nHriFontPosition
	 * @return
	 */
	boolean checkParam(int nOrgx, int nType, int nWidthX, int nHeight,
			int nHriFontType, int nHriFontPosition) {

		if ((nOrgx < 0) || (nOrgx > 65535)) {
			Toast.makeText(getApplicationContext(),
					"�����ӡ��ʼλ����ֵȡֵ��Χ0-65535����������ӡ��Χ�����벻��ӡ��", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		if ((nType < 0x41) || (nType > 0x49)) {
			Toast.makeText(
					getApplicationContext(),
					"��������ȡֵ��ΧΪ65-73." + "\n65-" + BARCODE_TYPE_UPC_A + "\n66-"
							+ BARCODE_TYPE_UPC_E + "\n67-" + BARCODE_TYPE_EAN13
							+ "\n68-" + BARCODE_TYPE_EAN8 + "\n69-"
							+ BARCODE_TYPE_CODE39 + "\n70-" + BARCODE_TYPE_ITF
							+ "\n71-" + BARCODE_TYPE_CODEBAR + "\n72-"
							+ BARCODE_TYPE_CODE93 + "\n73-"
							+ BARCODE_TYPE_CODE128, Toast.LENGTH_SHORT).show();
			return false;
		}

		if ((nWidthX < 2) || (nWidthX > 6)) {
			Toast.makeText(getApplicationContext(),
					"������ȡֵ��Χ2-6�������ȹ��������볬����ӡ��Χ���򲻻��ӡ��", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		if ((nHeight < 1) || (nHeight > 255)) {
			Toast.makeText(getApplicationContext(), "����߶�ȡֵ��Χ1-255",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		if ((nHriFontType < 0) || (nHriFontType > 1)) {
			Toast.makeText(getApplicationContext(),
					"��������ȡֵ��Χ0-1.\n0--��׼����.\n1--ѹ������", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		if ((nHriFontPosition < 0) || (nHriFontPosition > 3)) {
			Toast.makeText(getApplicationContext(),
					"��������λ��ȡֵ��Χ0-3.\n0--����ӡ.\n1--�����Ϸ�.\n2--�����·�.\n3--�������¶���ӡ",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * ���ñ�����֮ǰ������checkparam
	 * 
	 * @param nOrgx
	 * @param nType
	 * @param nWidthX
	 * @param nHeight
	 * @param nHriFontType
	 * @param nHriFontPosition
	 */
	void setParam(int nOrgx, int nType, int nWidthX, int nHeight,
			int nHriFontType, int nHriFontPosition) {
		TextPrintActivity.barcodeOrgx = nOrgx;
		TextPrintActivity.barcodeType = nType;
		TextPrintActivity.barcodeWidthX = nWidthX;
		TextPrintActivity.barcodeHeight = nHeight;
		TextPrintActivity.barcodeFontType = nHriFontType;
		TextPrintActivity.barcodeFontPosition = nHriFontPosition;
	}
}
