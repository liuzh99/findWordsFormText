package liuzh.words;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * title:Calcul
 * descript:从指定文件中查找
 * @author liuzh
 * @date 2016年4月27日 下午8:56:46
 */
public class Calcul implements Runnable{
	
	private static List<WordsCount> totalWordsList = new ArrayList<WordsCount>(5000);
	
	private String inputFileName;
	private List<WordsCount> wordsList = new ArrayList<WordsCount>();
	
	/**
	 * 将wordsList中的结果汇总到totalWordsList
	 * @param wordsList
	 */
	private synchronized void collect(){
		if(totalWordsList.size() == 0){
			totalWordsList.addAll(wordsList);
			return;
		}
		for(WordsCount wordsCount : wordsList){
			int index = findInsertIndex(totalWordsList, wordsCount.getWords());
			if(totalWordsList.size() == 0){
				totalWordsList.add(wordsCount);
			}
			//words对应的索引位置
			else if(totalWordsList.get(index).getWords().equals(wordsCount.getWords())){
				totalWordsList.get(index).add(wordsCount.getCount());
			}
			//words对应的插入位置
			else{
				totalWordsList.add(index, wordsCount);
			}
		}
	}
	
	/**
	 * 使用二分搜索从wordsList集合中查找words
	 * @param words
	 * @return words对应的索引位置或者插入位置
	 */
	private int findInsertIndex(List<WordsCount> list,String words){
		int left=0,right=list.size() - 1;
		int mid = 0;
		String midWords;
		int compareValue;
		while(left <= right){
			mid = (left+right)/2;
			midWords = list.get(mid).getWords();
			compareValue = midWords.compareTo(words);
			if(compareValue < 0){
				left = mid + 1;
			}
			else if(compareValue > 0){
				right = mid - 1;
			}else{
				//索引位置与当前获取到的单词相同,增加记数
				return mid;
			}
		}
		//插入到索引位置
		return left;
	}
	

	/**
	 * 从文件中查找单词，对于每个单词，都会到wordsList中查找，
	 * 如果返回的索引位置与当前获取到的单词相同，则索引位置计数加1
	 * 如果返回的索引位置与当前获取到的单词不同，则插入到索引位置
	 * @throws IOException
	 */
	private void find() throws IOException{
		InputStream inputStream = new FileInputStream(inputFileName);
		int intValue;
		char ch;
		int insertIndex;
		StringBuilder words = new StringBuilder();
		int byteLength = 1024, readLength;
		byte[] b = new byte[byteLength];
		while((readLength=inputStream.read(b)) != -1){
			for(int i = 0;i < readLength; i++){
				ch = (char)b[i];
				//判断ch是否是字母
				if(Character.isAlphabetic(ch)){
					words.append(ch);
				}
				//如果不是字母，则判断words中是否有内容，如果有，则将其加入到wordsList中
				else if(words.length() != 0){
					//如果集合为空，直接加入
					if(wordsList.size() == 0){
						wordsList.add(new WordsCount(words.toString()));
					}else{
						insertIndex = findInsertIndex(wordsList,words.toString());
						//words对应的索引位置
						if(insertIndex < wordsList.size() && wordsList.get(insertIndex).getWords().equals(words.toString())){
							wordsList.get(insertIndex).add();
						}
						//words对应的插入位置
						else{
							wordsList.add(insertIndex, new WordsCount(words.toString()));
						}
					}
					//清空words，开始一次新的查询
					words.delete(0, words.length());
				}
			}
		}
	}

	public static void output(String filename) throws IOException{
		OutputStream outputStream = new FileOutputStream(filename);
		for(int i=0;i<totalWordsList.size();i++){
			outputStream.write((totalWordsList.get(i).getWords() + " ").getBytes());
			outputStream.write((totalWordsList.get(i).getCount() + "\n").getBytes());
		}
	}
	
	@Override
	public void run() {
		try {
			find();
			collect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void reduce(File[] files,String outputFileName) throws IOException{
		Calcul calcul = null;
		List<Thread> threads = new LinkedList<Thread>();
		//创建线程
		for(File file : files){
			calcul = new Calcul();
			calcul.inputFileName = file.getAbsolutePath();
			threads.add(new Thread(calcul));
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