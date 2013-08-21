package btManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * 
 * @author Administrator �����̣߳���ξͲ�Ū��ô��ѭ����ɶ���߰˰˵�looper�ˡ�
 *         ���ӣ��͵��յ��㲥����getInputStream��getOutputStream����ȡ���������
 *         ���ӵ�ʱ��Ҳ��Ҫ�����쳣�ˣ��Ͼ�����յ��˹㲥���������쳣��Ҳ���������ˡ� �ܽ����Ķ���Connected=false��
 * 
 */
public class ConnectThread extends Thread {

	private UUID uuid2 = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private UUID uuid4 = UUID
	// .fromString("0000fff0-0000-1000-8000-00805F9B34FB");
	private UUID uuid = uuid2;
	private BluetoothAdapter myBTAdapter = BluetoothAdapter.getDefaultAdapter();
	private String btAddress;

	public ConnectThread(String btAddress) {
		this.btAddress = btAddress;
	}

	@Override
	public void run() {

		Pos.mContext.sendBroadcast(new Intent(Pos.ACTION_BEGINCONNECTING));

		if (myBTAdapter.isDiscovering())
			myBTAdapter.cancelDiscovery();

		BluetoothDevice rmDevice = myBTAdapter.getRemoteDevice(btAddress);
		try {
			Pos.cSocket = rmDevice.createRfcommSocketToServiceRecord(uuid);
			// Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
			// String tmp = "uuids: \n";
			// for (int i = 0; i < rmDevice.getUuids().length; i++)
			// tmp += (rmDevice.getUuids())[i].toString() + "\n";
			// intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, tmp);
			// Pos.mContext.sendBroadcast(intent);
		} catch (IOException e) {
			Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
			intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
					"createRfcommSocket ERROR!");
			Pos.mContext.sendBroadcast(intent);
			Pos.POS_Close();
		}

		if (Pos.cSocket != null) {
			try {
				Pos.cSocket.connect();
			} catch (IOException e1) {
				switch (rmDevice.getBondState()) {
				case BluetoothDevice.BOND_NONE:
				case BluetoothDevice.BOND_BONDED:
					Pos.POS_Close();
					break;
				default:
					// Pos.POS_Close();
					break;
				}
			}
		}

		if (Pos.cSocket != null) {
			try {
				Pos.os = Pos.cSocket.getOutputStream();
				Pos.bis = new BufferedInputStream(Pos.cSocket.getInputStream());

				/**
				 * �ж�һ�£��Ƿ���ȷ��������������
				 */
				Pos.Connected = true;
				Pos.mContext.sendBroadcast(new Intent(
						Pos.ACTION_CONNECTEDUNTEST));
			} catch (IOException e) {
				Pos.Connected = false;
				Pos.POS_Close();
				Pos.mContext.sendBroadcast(new Intent(
						Pos.ACTION_CONNECTINGFAILED));
			}
		} else {
			Pos.mContext.sendBroadcast(new Intent(Pos.ACTION_CONNECTINGFAILED));
		}

		Pos.Connecting = false;
	}
}
