package btmanager;

public class TimeUtils {

	/**
	 * �ȴ�delay ms ��һ������������������˯��
	 * 
	 * @param delay
	 */
	public static void waitTime(int delay) {
		long time = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - time > delay) {
				return;
			}
		}
	}
	

}
