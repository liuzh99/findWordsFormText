package liuzh.words;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Test {


	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		
		WordsCount wordsCount = new WordsCount();
		try {
			wordsCount.reduce("D:/test/input1.txt", "D:/test/output.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}

}
