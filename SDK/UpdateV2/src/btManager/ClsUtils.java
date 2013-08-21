package btManager;

/************************************ ������Ժ��� * **************/
import java.lang.reflect.Method;
import android.bluetooth.BluetoothDevice;

public class ClsUtils {

	/**
	 * ���豸��� �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean createBond(Class<? extends BluetoothDevice> btClass,
			BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	/**
	 * ���豸������ �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean removeBond(Class<?> btClass, BluetoothDevice btDevice)
			throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPin(Class<? extends BluetoothDevice> btClass,
			BluetoothDevice btDevice, String str) throws Exception {
		try {
			Method removeBondMethod = btClass.getDeclaredMethod("setPin",
					new Class[] { byte[].class });
			removeBondMethod.invoke(btDevice, new Object[] { str.getBytes() });
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}
		return true;
	}

	// ȡ���û�����
	static public boolean cancelPairingUserInput(Class<?> btClass,
			BluetoothDevice device) throws Exception {
		Method cancelPairingUserInput = btClass
				.getMethod("cancelPairingUserInput");
		Boolean returnValue = (Boolean) cancelPairingUserInput.invoke(device);
		return returnValue.booleanValue();
	}

	// ȡ�����
	static public boolean cancelBondProcess(Class<?> btClass,
			BluetoothDevice device)

	throws Exception {
		Method createBondMethod = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

}
