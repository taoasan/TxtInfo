package com.founder.storage.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.founder.storage.bean.TextInfo;

/**
 * 
 * 该类的主要功能是获取不同编码格式的txt文档里面的title time detail信息。根据txt文档里面的特定字符提取出需要的信息。封装成TextInfo对象。
 * @author taokl
 *
 */
public class TxtUtil {
	
	/**
	 * 该方法用于获取法新社 AFP 和 美联社  AP 文件下的txt文档里面的信息
	 * 需求:
	 * 获取文档中STX控制字符后面的内容作为title
	 * 获取文档中r i后面有日期格式的内容作为time
	 * 获取全部的文档信息，作为detail
	 * 
	 * 思路：
	 * 判断文件某一行是否含有STX控制字符   如果有就用time记录下来 留作后面处理
	 * 判断某一行是否含有r i头部，如果有用title记录下来，留作后面处理
	 * 获取整个文档的全部信息 用detail记录下来
	 * 处理前面保存的time字符串，用" "切割，循环判断每一个子串是否包含00-00格式的字符。是的话更新time
	 * 处理titile 取title从1到最后字符之间的字符，并且去掉头尾的空格 更新title
	 * 封装对象放回。
	 * @param filepath  文件路径
	 * @return   TextInfo对象
	 */

	public static TextInfo getTxtInfoFromAFPandAP(String filepath) {
		BufferedReader br=null;
		String title="";
		String time="";
		String detail="";
		String st=null;
		String charset=getFileCharset(filepath);//获取文件保存的编码格式
		try {
			br=new BufferedReader(new InputStreamReader(new FileInputStream(filepath),charset));
			StringBuilder sb=new StringBuilder();
			boolean timeflag=false;//定义一个标记，用来判断是否拿到了
			boolean titleflag=false;
			while((st=br.readLine())!=null){	
				//拿time 以a-z开头，之后是空格，在之后是a-z 如果标记为true那么就说明已经拿到了就不再拿
				if( !timeflag &&  (!("".equals(st))) && st.matches("^[a-z] [a-z].*")){
					time=st;
					timeflag=true;					
				}
				//拿title 如果标记为true就拿到了，就不在拿
				if(!titleflag && (!("".equals(st))) && (int)st.charAt(0)==2){
					title=st;
					titleflag=true;
				}
				//判断完了之后将st中的控制字符替换为' '
				char [] ch=st.toCharArray();
				for(char c:ch){
					int asciicode=(int)c;
					if(asciicode >=0 && asciicode<=32){
						st=st.replace(c,' ');
					}
				}
				sb.append(st).append("\n");				
			}
			//可以拿到detail
			detail=sb.toString();	
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭流
			closedReaderStream(br);
		}
		//再对time和title进行处理，先用" "切割，然后匹配
		if(!("".equals(time))){
		    String [] array=time.split(" ");
			for(String tmp:array){
				//正则匹配时间
				if(tmp.matches("^(([0]?[1-9])|([1]?[0-2]))[-](([0]?[1-9])|([1-2]?[0-9])|([3]?[0-1]))?$")){
					time=tmp;
					break;
				}
			}
		}
		//将前面的控制字符切掉，前面的控制字符占了一个字符的长度。并且切掉首尾的空白
		if(!("".equals(title))){
			title=title.substring(1,title.length()).trim();
		}		
		//封装到对象返回
		return packageTextInfo(title,time,detail);		
	}
	
	/**
	 * 该方法用于获取印尼星洲日报Indo / Indo(1)里面的txt文件里面的标题，作者，文件创建时间，以及整个文件的全部信息。。
	 * 需求：
	 * 获取含有“标题”两个字的那一行的标题信息，
	 * 获取含有"作者"两个字的那一行的作者信息
	 * 获取文件创建的时间，
	 * 获取文件的全部信息。
	 * 
	 * 思路：
	 * 现将每一行读取的字符串去掉头尾空格然后
	 * 根据读取的每一行的字符串的开头的信息来判断是否是标题，后面再进行切割，注意标题有可能有两行，或者多行，这里只判断两行，如果后面有多行，那么就需要使用while循环判断。后面碰到在加。
	 * 同理作者也是根据字符串的开头来判断是否是作者。后面切割。
	 * 时间是根据文件创建的时间来获取的，使用CMD命令行的dir命令来获取文件创建的时间。
	 * 文本直接使用字符缓冲流来获取、
	 * 最后封装对象返回。
	 * @param filepath
	 * @return  返回TextInfo对象
	 */
	public static TextInfo getTxtInfoFromIndo(String filepath){
		BufferedReader br=null;
		String title="";
		String time="";
		String author="";
		String detail="";
		String st=null;
		String charset=getFileCharset(filepath);//获取文件保存的编码格式
			try {
				br=new BufferedReader(new InputStreamReader(new FileInputStream(filepath),charset));
				while((st=br.readLine())!=null){
					//title为""的时候才需要读，判断，如果title已经读到了就不在往下判断了。
					st=cutHeadAndTailNullString(st);
					if(("".equals(title)) && (st.startsWith("標題") || st.startsWith("标题"))){
						title=st;
						String nextLine=br.readLine();
						//有的标题有两行，那么就再判断下下一行不是空格也还算做标题
						if(!"".equals(nextLine) || nextLine!=null){
							title+=nextLine;
						}
					}
					//author为""的时候才需要判断，不然的话author已经取到了就不判断了。
					if(("".equals(author)) && st.startsWith("作者") ){
						author=st;
					}	
					//如果两个都不为空就跳出循环
					if(!"".equals(title) && !"".equals(author)) {
						break;
					}
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  finally{
				closedReaderStream(br);
			}
			//调用getcutString来截取title跟author
			title=getCutString(title);
			author=getCutString(author);
			//获取文件时间
			time=getFileCreateTime(filepath);
			//获取文件内容
			detail=getFileDetail(filepath,charset);		
			return packageTextInfo(title,time,detail);
	}	
	/**
	 * 获取香港明报的txt文件中的 title time 以及文本信息
	 * 需求：
	 * 获取文件第一行非空格行作为标题 title
	 * 获取文件创建时间作为time
	 * 获取文件的全部信息 作为detail
	 * 
	 * 思路：
	 * 使用字符流读取文本每一行的信息，如果读到的字符里面有文本信息，那么就作为title
	 * 使用CMD获取文件创建时间
	 * 用字符流读取整个文件的信息
	 * @param filepath 文件路径
	 * @return
	 */
	public static TextInfo getTxtInfoFromHKMP(String filepath){
		BufferedReader br=null;
		StringBuilder sb=new StringBuilder();
		String st=null;
		String title="";
		String time="";
		String detail="";
		String charset=getFileCharset(filepath);	
		try {
			br=new BufferedReader(new InputStreamReader(new FileInputStream(filepath),charset));
			while((st=br.readLine())!=null){
				//将得到的st首尾的空串切割掉。
				st=cutHeadAndTailNullString(st);
				//如果切割完了之后的串不为空，并且
				if(st!=null && st.length()!=0 ){
					title=st;
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			closedReaderStream(br);
		}
		
		//拿时间
		time=getFileCreateTime(filepath);
		//拿内容
		detail=getFileDetail(filepath, charset);		
		//封装成对象
		return packageTextInfo(title, time, detail);		
	}	
	/**
	 * 该方法主要用于将每次从文本中读出来的每一行字符串去掉头部和尾部的空格
	 * @param st   需要去掉首尾空格的字符串
	 * @return     返回切割之后的串
	 */
	private static String cutHeadAndTailNullString(String st){
		if(st!=null && st.length()!=0){
			st=st.trim();
		}
		return st;
	}
	/**
	 * 该方法主要用户获取txt文件的全部内容。可以被getTxtInfofromIndo以及getTxtInfofromHKMP使用
	 * @param filepath 文件路径
	 * @param charset  文件编码格式
	 * @return    返回文件的内容。
	 */
	private  static String getFileDetail(String filepath,String charset){
		BufferedReader br=null;
		StringBuilder sb=new StringBuilder();
		String st=null;
		
		try {
			br=new BufferedReader(new InputStreamReader(new FileInputStream(filepath),charset));
			while((st=br.readLine())!=null){
				sb.append(st).append("\n");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			closedReaderStream(br);
		}
		return sb.toString();		
	}
		
	/**
	 * 该方法主要用于获取某一个文件的创建时间,因为印尼新洲日报和香港明报txt文件中没有时间，所以选择文件创建时间作为time
	 * @param filepath   文件具体的路径
	 * @return 返回文件创建时间字符串  具体到年月日 小时 分钟
	 */
	private static String getFileCreateTime(String filepath){
		Process p=null;
		BufferedReader br=null;
		String time="";
		try {
			p=Runtime.getRuntime().exec("cmd /C dir \""+filepath+"\"/tc");
			br=new BufferedReader(new InputStreamReader(p.getInputStream()));
			//读取第六行的信息 第六行就是时间行
			String st=null;
			int count=0;
			while((st=br.readLine())!=null){
				count++;
				if(count==6){
					//将时间保存起来。后面处理
					time=st;
					break;
				}
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			closedReaderStream(br);
		}
		//处理时间
		time=time.substring(0,17);
		return time;	
	}
	
	/**
	 * 该方法主要用于获取Indo和Indo(1) txt文件里面的标题和作者。
	 * 在使用：切割的时候title和author都需要使用:切割，代码复用，这里定义一个getCutString方法供getTxtDetailfromIndo调用
	 * @param st
	 * @return
	 */
	private static String getCutString(String st){
		//处理title跟作者以及time
		//处理title 处理前先判断下tiltle是否为"" 还要判断标题有可能为标题：后面没有值的情况
		String result="";
		if(!"".equals(st)  && st.length()>3){
		String [] st_array=st.split(":");
		//如果切不出来，也就是使用的是中文的冒号那么  title_array的长度应该是1
		if(st_array.length==1 ){
			//换一种方式切,换成中文的方式切割
			st_array=st.split("：");
		}
		result=st_array[st_array.length-1];//取最后的一部分。
		}
		return result;
	}
	
	/**
	 * 这个方法主要用于对txt文件的编码格式进行判断
	 * 这种判断方式并不严谨，只适用于特定情况。
	 * 使用两级判断，第一级判断文件开头三个字符。
	 * 获取文件保存时候的编码格式。一般与文件的前三个字节的数值有关。
	 * 编码对应的前三个字节的表是
	 * Big-5 对应的编码是:   -64  -55  -90    0xC0   0xC9  0xA6
	 * utf-8 对应的编码是：         -17  -69  -65    0xEF   0xBB  0xBF
	 * GBK   对应的编码是：       -73  -61  -50    0xB7   0xC3  0xCE
	 * UTF-16LE(也就是unicode) 对应编码是：                                     -1 -2  0xFF  0xFE  只判断前两个字节就可以了
	 * UTF-16BE(也就是Unicode big endian)对应编码是：  -2 -1  0xFE  0xFF  也是只判断两个字节
	 * 如果不是上面的编码那么就进行第二级判断。第二级判断只判断gbk与big5的区别，因为gbk可以读uft-8不会出现乱码，
	 * 如果后面有其他的编码可以在这里加上其他的判断方式。
	 * @param sourceFile 文件路径
	 * @return  返回文件的编码格式
	 */
	private static String getFileCharset(String filepath) {
        String charset = "GBK";           //默认是GBK
        BufferedInputStream bis=null;
        byte[] headbyte = new byte[3];    //取前三个字节来判断    
        boolean flag=false;  	         //标记字符编码是否被判断出来了。
        //第一层编码过滤。
            try {
				bis = new BufferedInputStream(new FileInputStream(filepath));
				bis.mark(0);//将这里的位置先置为0如果第一层没有判断出来进行第二次判断之前将读取流重新置为开始状态
	            int read = bis.read(headbyte, 0, 3);//只读前三个字节
	            if (read == -1) {
	                return charset; //如果文件为空就返回默认编码为 GBK
	            } else if (headbyte[0] == (byte) 0xFF
	                    && headbyte[1] == (byte) 0xFE) {
	                charset = "UTF-16LE"; //编码格式为 Unicode little endian
	                flag=true; 
	            } else if (headbyte[0] == (byte) 0xFE
	                    && headbyte[1] == (byte) 0xFF) {
	                charset = "UTF-16BE"; //编码格式为 Unicode big endian
	                flag=true; 
	            } else if (headbyte[0] == (byte) 0xEF
	                    && headbyte[1] == (byte) 0xBB
	                    && headbyte[2] == (byte) 0xBF) {
	                charset = "UTF-8";  //编码格式为 UTF-8
	                flag=true; 
	            } else if (headbyte[0] == (byte) 0xC0 
	            		&& headbyte[1] == (byte) 0xC9 
	            		&& headbyte[2] == (byte) 0xA6) {
	            	charset="Big5";//  编码格式为big5
	            	flag=true; 
	            } else if (headbyte[0] == (byte) 0xB7 
	                  	&& headbyte[1] == (byte) 0xC3
	                    && headbyte[2] == (byte) 0xCE) {
		          	charset="gbk";  
		          	flag=true;
	            }
	            bis.reset();//重置读取流，也就是从头读
	            // 如果第一层判断不出来，就进行第二层编码过滤。
	            if(!flag){
	            	int readnext =0;         	
	            	//一个字节一个的读，读高位，在读地位。这里给定一个界限，如果连续的读取超过10次都是big5的编码，那么就认为是big5的编码
	            	int count=0;//记录下字节的范围在big5以内的有多少次
	    			while(readnext!=-1){					
			    		readnext=bis.read();//第一个字节
			    		int next=bis.read();//第二个字节
			    		if(readnext!=-1 && next!=-1){
						// Big5的编码范围比gb2312要广。所以这里可以将big5判断出来。但是他们之间有交集，有一些字符无法用交集判断，所以这里用一个数字记录下来有多少
			    		//是Big5范围的，如果超过10个那么就认定是Big5。
							if(((readnext >=161 && readnext <=247) && (next>=64 &&next <=126))){
							   count++;
							}
			    		}
						else{
						//文件读到了末尾了就跳出循环。
							break;
						}
		    			//如果判断得到的count数大于等于10就认定为big5
						if(count>=10){
						charset="Big5";
						flag=true;
						break;		
						}
	    			}			
	            }         
            } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				closedInputStream(bis);
			}  
            //如果两层都判断不出来，就返回默认值，
        return charset;
      }
	/**
	 * 该方法用于将获取的time title detail注入到textinfo对象里面，这个操作多次使用，所以抽离成一个方法。
	 * @param title
	 * @param time
	 * @param detail
	 * @return  返回封装好的TextInfo对象
	 */
	private static TextInfo packageTextInfo(String title,String time,String detail){
		TextInfo textinfo=new TextInfo();
		textinfo.setTitle(title);
		textinfo.setTime(time);
		textinfo.setDetail(detail);
		return textinfo;
	}
	/**
	 * 该方法用于关闭字符输入流资源
	 * @param reader 字符流Reader的某一个实现类
	 */
	private static void closedReaderStream(Reader reader){
		if(reader!=null){
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 该方法用于关闭字节输入流资源
	 * @param stream
	 */
	private static void closedInputStream(InputStream stream){
		if(stream!=null){
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
