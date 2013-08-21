package btmanager;

import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
public class ClipboardUtils {

	private Context context;
	private ClipboardManager oldClipboard;
	private String clipText;

	public ClipboardUtils(Context mContext) {
		context = mContext;

		oldClipboard = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	// ���û�У��ͻ᷵��""��
	// ����clipboard�����ݵ�ȫ��
	private void updateClipBoard() {
		if (oldClipboard.hasText())
			clipText = oldClipboard.getText().toString();
	}

	public String getText() {

		updateClipBoard();

		if (clipText != null)
			return clipText;
		else
			return "";
	}
}
