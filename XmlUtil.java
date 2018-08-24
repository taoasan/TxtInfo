package com.founder.storage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.founder.storage.bean.TextInfo;

/**
 * xml工具类 
 * 通过XmlUtil对象的getTxtInfo方法读取xml中指定节点的文本内容， 以及全部的xml信息内容 
 * 
 * 思路：
 * 1，使用Dom4j的xml解析来获取我们需要的节点的信息，
 * 2，将信息存放在map集合里面
 * 3，从map集合里面取出信息，封装到TextInfo对象
 * 4，使用字节流读取xml整个文本的信息，封装到TextInfo对象里面。
 * 5，返回封装好的TextInfo对象
 * 
 */
public class XmlUtil {
	/**
	 * 读取xml文件的指定节点的文本信息以及全部的xml文件信息 参数 文件名和节点名称 返回值 TextInfo对象
	 * @param filepath  需要被读取的xml文件的路径。
	 * @param node1       需要从xml文档中获取的标题title的节点的名字
	 * @param node2       需要从xml文档中截取的时间的节点的名字
	 * @return       返回封装好的TextInfo对象
	 */
	public static TextInfo getTextInfo(String filepath, String node1, String node2) {
		Document doc = null;
		FileInputStream fis = null;
		String title = "";// 由于存放标题
		String time = ""; // 用于存放时间
		String detail = "";// 用于存放所有的xml详细信息
		TextInfo textinfo = new TextInfo();// new 一个TextInfo对象用于将得到的title time text装到对象中。

		// 根据节点来获取节点的信息。
		try {
			doc = new SAXReader().read(new File(filepath));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = doc.getRootElement();
		HashMap<String, String> hm = new HashMap<>();
		getTitleAndTime(root, node1, node2, hm);
		Set<String> set = hm.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String st = it.next();
			if (st.equals(node1)) {
				title = hm.get(st);
			} else if (st.equals(node2)) {
				time = hm.get(st);
			}
		}
		// 使用字节流来获取全部的xml文件信息
		try {
			fis = new FileInputStream(new File(filepath));
			StringBuilder sb = new StringBuilder();
			byte[] buffer = new byte[2 * 1024 * 1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, len));
			}
			detail = sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		textinfo.setTitle(title);
		textinfo.setTime(time);
		textinfo.setDetail(detail);

		return textinfo;
	}
	/**
	 * 用于获取xml格式文件的title和time
	 * @param root  xml文件的根节点
	 * @param node1       需要从xml文档中获取的标题title的节点的名字
	 * @param node2       需要从xml文档中截取的时间的节点的名字
	 * @param hm     用于保存title和time的hashmap
	 * 
	 */
	private static void getTitleAndTime(Element root, String node1, String node2, HashMap<String, String> hm) {
		if (root == null) {
			return;
		}
		// 定义一个临时变量记录下root.getName()的值。
		String tmp = root.getName();
		if (node1.equals(tmp)) {
			hm.put(node1,root.getText());
		} else if (node2.equals(tmp)) {
			hm.put(node2,root.getText());
		}
		List<Element> elementList = root.elements();
		if (elementList != null  && (hm.get(node1)==null  ||  hm.get(node2)==null)) {
			for (Element sonelement : elementList) {
				// 递归调用
				getTitleAndTime(sonelement, node1, node2, hm);
			}
		}
	}

}
