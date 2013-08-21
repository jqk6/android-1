package com.lvrenyang.kcusb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 还有很多需要优化的地方，载入文件需要开线程，异步加载图片缩略图需要缓存
 * 
 * @author Administrator
 * 
 */
public class FileUtils {

	private List<String> filePaths = new ArrayList<String>();

	/**
	 * 
	 * @param dir
	 * @param extension
	 *            必须为小写字母，会文件后缀名不论大小写都可以匹配
	 * @return
	 */
	public List<String> getFiles(File dir, String extension) {
		if (!dir.isDirectory())
			return filePaths;

		File[] allFile = dir.listFiles();
		if (allFile == null)
			return filePaths;

		for (int i = 0; i < allFile.length; i++) {
			if (allFile[i].isDirectory()) {
				String tmp = allFile[i].getName();
				if (tmp != null) {
					if (tmp.length() > 0) {
						if (tmp.charAt(0) != '.') {
							getFiles(allFile[i], extension);
						}
					}
				}
			} else if (allFile[i].isFile()) {
				String path = allFile[i].getAbsolutePath();
				if (checkExtension(
						path.substring(path.length() - extension.length()),
						extension))
					filePaths.add(path);
			} else
				return filePaths;
		}

		return filePaths;

	}

	/**
	 * 多个extension
	 * 
	 * @param dir
	 * @param extensions
	 * @return
	 */
	public List<String> getFiles(File dir, String[] extensions) {
		if (!dir.isDirectory())
			return filePaths;

		File[] allFile = dir.listFiles();
		if (allFile == null)
			return filePaths;

		for (int i = 0; i < allFile.length; i++) {
			if (allFile[i].isDirectory()) {
				String tmp = allFile[i].getName();
				if (tmp != null) {
					if (tmp.length() > 0) {
						if (tmp.charAt(0) != '.') {
							getFiles(allFile[i], extensions);
						}
					}
				}

			} else if (allFile[i].isFile()) {
				String path = allFile[i].getAbsolutePath();
				for (int j = 0; j < extensions.length; j++)
					if (checkExtension(
							path.substring(path.length()
									- extensions[j].length()), (extensions[j])))
						filePaths.add(path);
			} else
				return filePaths;
		}

		return filePaths;

	}

	/**
	 * 
	 * @param extensions1
	 * @param extensions2
	 * @return
	 */
	private boolean checkExtension(String extension1, String extension2) {
		if (extension1.length() != extension2.length())
			return false;
		else {
			String tmp = "";
			char tmpc;

			for (int i = 0; i < extension1.length(); i++) {
				tmpc = extension1.charAt(i);
				if (tmpc >= 'A' && tmpc <= 'Z')
					tmp += (tmpc + 32);
				else
					tmp += tmpc;
			}
			if (tmp.equals(extension2))
				return true;
			else
				return false;
		}
	}

}