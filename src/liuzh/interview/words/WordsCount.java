package liuzh.interview.words;

/**
 * title:WordsCount
 * descript:
 * @author liuzh
 * @date 2016年4月27日 下午8:45:37
 */
public class WordsCount {
	//单词出现的次数
	private int count;
	//当前单词
	private String words;
	
	public WordsCount(String words) {
		super();
		count = 1;
		this.words = words;
	}
	
	public int getCount() {
		return count;
	}

	public String getWords() {
		return words;
	}

	//计数加1
	public void add(){
		count++;
	}

}
