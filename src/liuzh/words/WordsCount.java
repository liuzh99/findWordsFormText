package liuzh.words;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * title:Calcul
 * descript:从指定文件中查找
 * @author liuzh
 * @date 2016年4月27日 下午8:56:46
 */
public class WordsCount implements Runnable{
	//所有节点的结果集
	private static Map<String,Integer> totalWordsMap = new HashMap<String,Integer>();
	
	//当前节点的文件输入流
	private String inputFileName;
	//单个节点的计算结果
	private Map<String,Integer> wordsMap = new HashMap<String,Integer>();
	
	/**
	 * 将wordsList中的结果汇总到totalWordsList
	 * @param wordsList
	 */
	private synchronized void collect(){
		if(totalWordsMap.size() == 0){
			totalWordsMap.putAll(wordsMap);
			return;
		}
		Iterator<String> keys = wordsMap.keySet().iterator();
		String key;
		Integer value;
		while(keys.hasNext()){
			key = keys.next();
			value = wordsMap.get(key);
			//如果totalWords中有，则将记数合并到totalWords中
			if(totalWordsMap.containsKey(key)){
				Integer valuet = totalWordsMap.get(key);
				totalWordsMap.put(key, value + valuet);
			}
			//否则将这个Map元素放入totalWords
			else{
				totalWordsMap.put(key, value);
			}
		}
	}

	/**
	 * 从文件中查找单词，对于每个单词，都会到wordsList中查找，
	 * 如果返回的索引位置与当前获取到的单词相同，则索引位置计数加1
	 * 如果返回的索引位置与当前获取到的单词不同，则插入到索引位置
	 * @throws IOException
	 */
	private void find() throws IOException{
		
		InputStream inputStream = new FileInputStream(inputFileName);
		
		char ch;
		StringBuilder words = new StringBuilder();
		
		int byteLength = 1024, readLength;
		byte[] b = new byte[byteLength];
		
		//wordsMap中某个单词的记数
		String key;
		Integer value;
		
		while((readLength=inputStream.read(b)) != -1){
			for(int i = 0;i < readLength; i++){
				ch = (char)b[i];
				//判断ch是否是字母
				if(Character.isAlphabetic(ch)){
					words.append(ch);
				}
				//如果不是字母，则判断words中是否有内容，如果有，则将其加入到wordsList中
				else if(words.length() != 0){
					key = words.toString();
					value = wordsMap.get(key);
					
					if(wordsMap.containsKey(key)){
						wordsMap.put(key, value + 1);
					}else{
						wordsMap.put(key, 1);
					}
					words.delete(0, words.length());
				}
			}
		}
	}

	/**
	 * 结果输出到文件
	 * @param filename
	 * @throws IOException
	 */
	public static void output(String filename) throws IOException{
		OutputStream outputStream = new FileOutputStream(filename);

		//按顺序输出
		Object[] keys = totalWordsMap.keySet().toArray();
		Arrays.sort(keys);
		for(Object key : keys){
			Integer value = totalWordsMap.get(key);
			outputStream.write((key + " ").getBytes());
			outputStream.write((value + "\n").getBytes());
		}
		//无序输出
//		Iterator<String> keys = totalWordsMap.keySet().iterator();
//		String key;
//		Integer value;
//		while(keys.hasNext()){
//			key = keys.next();
//			value = totalWordsMap.get(key);
//			outputStream.write((key + " ").getBytes());
//			outputStream.write((value + "\n").getBytes());
//		}
	}
	
	/**
	 * 每个节点的任务
	 */
	@Override
	public void run() {
		try {
			find();
			collect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 分派任务
	 * @param files
	 * @param outputFileName
	 * @throws IOException
	 */
	public static void reduce(File[] files,String outputFileName) throws IOException{
		WordsCount wordsCount = null;
		List<Thread> threads = new LinkedList<Thread>();
		//创建线程
		for(File file : files){
			wordsCount = new WordsCount();
			wordsCount.inputFileName = file.getAbsolutePath();
			threads.add(new Thread(wordsCount));
		}
		//开启线程
		for(Thread thread : threads){
			thread.start();
		}
		//等待所有线程结束
		for(Thread thread : threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		output(outputFileName);
	}

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		
		File dir = new File("D:/input");
		File[] files = dir.listFiles();
		try {
			reduce(files, "d:/output/output.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}
	
}