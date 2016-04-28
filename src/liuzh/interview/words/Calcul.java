package liuzh.interview.words;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * title:Calcul
 * descript:从指定文件中查找
 * @author liuzh
 * @date 2016年4月27日 下午8:56:46
 */
public class Calcul {
	
	private List<WordsCount> wordsList = new ArrayList<WordsCount>(5000);
	
	/**
	 * 使用二分搜索从wordsList集合中查找words
	 * @param words
	 * @return words对应的索引位置或者插入位置
	 */
	private int findInsertIndex(String words){
		int left=0,right=wordsList.size() - 1;
		int mid = 0;
		String midWords;
		int compareValue;
		while(left < right){
			mid = (left+right)/2;
			midWords = wordsList.get(mid).getWords();
			compareValue = midWords.compareTo(words);
			if(compareValue < 0){
				left = mid + 1;
			}
			else if(compareValue > 0){
				right = mid - 1;
			}else{
				return mid;
			}
		}
		
		return left;
	}

	/**
	 * 从文件中查找单词，对于每个单词，都会到wordsList中查找，
	 * 如果返回的索引位置与当前获取到的单词相同，则索引位置计数加1
	 * 如果返回的索引位置与当前获取到的单词不同，则插入到索引位置
	 * @param filename
	 * @throws IOException
	 */
	public void find(String filename) throws IOException{
		InputStream inputStream = new FileInputStream(filename);
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
					insertIndex = findInsertIndex(words.toString());
					//如果集合为空，直接加入
					if(wordsList.size() == 0){
						wordsList.add(new WordsCount(words.toString()));
					}
					//words对应的索引位置
					else if(wordsList.get(insertIndex).getWords().equals(words.toString())){
						wordsList.get(insertIndex).add();
					}
					//words对应的插入位置
					else{
						wordsList.add(insertIndex, new WordsCount(words.toString()));
					}
					//清空words，开始一次新的查询
					words.delete(0, words.length());
				}
			}
		}
	}

	public void output(String filename) throws IOException{
		OutputStream outputStream = new FileOutputStream(filename);
		for(int i=0;i<wordsList.size();i++){
			outputStream.write((wordsList.get(i).getWords() + " ").getBytes());
			outputStream.write((wordsList.get(i).getCount() + "\n").getBytes());
		}
	}

	public static void main(String[] args) {
		Calcul calcul = new Calcul();
		try {
			calcul.find("e:/input.txt");
			calcul.output("e:/output.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}