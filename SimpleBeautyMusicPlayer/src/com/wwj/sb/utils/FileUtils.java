package com.wwj.sb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Environment;

import com.wwj.sb.domain.Mp3Info;

/**
 * 2013/5/7
 * @author wwj
 *
 */
public class FileUtils {
	private String SDCardRoot;

	public FileUtils() {
		// 获得外部存储设备的目录
		SDCardRoot = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator;
	}

	/**
	 * 在SD卡上创建文件
	 * 
	 * @param filename
	 * @param dir
	 * @return
	 */
	public File createFileInSDCard(String filename, String dir)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + filename);
		file.createNewFile();
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param dir
	 * @return
	 */
	public File createDirInSDCard(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		dirFile.mkdir();
		return dirFile;
	}

	/**
	 * 判断文件在SD卡上是否存在
	 * 
	 * @param filename
	 * @param path
	 * @return
	 */
	public boolean isFileExist(String filename, String path) {
		File file = new File(SDCardRoot + path + File.separator + filename);
		return file.exists();
	}

	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 * @param path
	 * @param filename
	 * @param inStream
	 * @return
	 */
	public File writeToSDCardInput(String path, String filename,
			InputStream inStream) {
		File file = null;
		OutputStream outStrem = null;
		try {
			createDirInSDCard(path); // 创建目录
			file = createFileInSDCard(filename, path); // 创建文件
			outStrem = new FileOutputStream(file); // 得到文件输出流
			byte[] buffer = new byte[4 * 1024]; // 定义一个缓冲区
			int len;
			while ((len = inStream.read(buffer)) != -1) {
				outStrem.write(buffer, 0, len);
			}
			outStrem.flush(); // 确保数据写入到磁盘当中
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outStrem.close(); // 关闭输出流
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	
	public List<Mp3Info> getMp3Infos(String path) {
		List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		File file = new File(SDCardRoot +  File.separator + path);
		File[] files = file.listFiles();
		FileUtils fileUtils = new FileUtils();
		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith("mp3")) {
				Mp3Info mp3Info = new Mp3Info();
				mp3Info.setTitle(files[i].getName());
				mp3Info.setSize(files[i].length());
				String temp[] = mp3Info.getTitle().split("\\.");
				String lrcName = temp[0] + ".lrc";
				if(fileUtils.isFileExist(lrcName, "/mp3")){
					mp3Info.setLrcTitle(lrcName);
				}
				mp3Infos.add(mp3Info);
			}
		}
		return mp3Infos;
	}
	
}
