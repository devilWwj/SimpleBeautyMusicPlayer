package com.wwj.sb.service;

import java.util.ArrayList;
import java.util.List;

/***
 * 前期开发的用来数据填充的类
 * @author wwj
 *
 */
public class DataService {
	public static List<String> getData(int offset, int maxResult) {
		List<String> data = new ArrayList<String>();
		for(int i = 0; i < 20; i++) {
			data.add("我是歌手" + i);
		}
		return data;
	}
}
