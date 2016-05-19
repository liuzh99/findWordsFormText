package liuzh.words;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Task implements Callable<Map<String,Integer>>{
	
	//当前节点的文件输入流
	private String inputFileName;
	public Task(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	
	
	//存储计算结果
	private Map<String,Integer> wordsMap = new HashMap<String,Integer>();
	
	private void addWords(String words){
		if(words.isEmpty()){
			return;
		}
		//wordsMap中某个单词的记数
		String key = words.toString();
		Integer value = wordsMap.get(key);

		//如果wordsMap中有，则将记数加1
		if(wordsMap.containsKey(key)){
			wordsMap.put(key, value + 1);
		}
		//如果wordsMap中没有，将些单词加入Map中，记数为1
		else{
			wordsMap.put(key, 1);
		}
	}
	
	@Override
	public Map<String, Integer> call() throws Exception {
		InputStream inputStream = new FileInputStream(inputFileName);
		
		char ch;
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
					addWords(words.toString());
					words.delete(0, words.length());
				}
			}
			addWords(words.toString());
		}
		return wordsMap;
	}
}
