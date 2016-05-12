# 统计文本文件中每个英文单词出现的次数


此题需要将查找的历史结果保留下来，并且集合大小会增加多次，后续还需要在这个集合中不断查找，所以，``为了查询速度和插入速度，我打算使用二分搜索算法，数据结构使用链表，并用数组链表实例化。``


一：首先定义集合的结构：WordsCount，主要是以下两个属性<br>
```java
    //单词出现的次数
    private int count;
    //当前单词
    private String words;
```


二：开始设计程序：关键函数，find(String)<br>
## 第一次实现功能
```java
	public void find(String filename) throws IOException {
		InputStream inputStream = new FileInputStream(filename);
		int intValue;
		char ch;
		int insertIndex;
		StringBuilder words = new StringBuilder();
		while ((intValue = inputStream.read()) != -1) {
			ch = (char) intValue;
			// 判断ch是否是字母
			if (Character.isAlphabetic(ch)) {
				words.append(ch);
			}
			// 如果不是字母，则判断words中是否有内容，如果有，则将其加入到wordsList中
			else if (words.length() != 0) {
				insertIndex = findInsertIndex(words.toString());
				// 如果集合为空，直接加入
				if (wordsList.size() == 0) {
					wordsList.add(new WordsCount(words.toString()));
				}
				// words对应的索引位置
				else if (wordsList.get(insertIndex).getWords()
						.equals(words.toString())) {
					wordsList.get(insertIndex).add();
				}
				// words对应的插入位置
				else {
					wordsList.add(insertIndex, new WordsCount(words.toString()));
				}
				// 清空words，开始一次新的查询
				words.delete(0, words.length());
			}
		}
	}
```
发现这段代码执行的时间比较长，于是我猜想可能是从文件读取字符导致的。于是我将while里面的代码注释掉，发现大部分时间都是从文件读取字符，于是我打算定义一个字节数组，一次性读取多个字符。改为如下代码之后，速度果然有了巨大的提升，原来需要24s，现在只需要415ms
## 第二次优化IO性能
```java
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
```
最后：由于我使用的测试数据是13.6M，经过计算，如果数据是4G，所需时间大约为2分钟。<br>
对于内存，除了使用到1024byte进行读取加速以外，没有申请其它不必要的内存，所以内存的使用是很低的。

## 第三次优化计算方式
- 此次优化来源于google论文MapReduce的思想，将任务分配到多个节点进行计算，最终把各节点的计算结果汇总。
- 这项技术被广泛应用于分布式系统，我用多线程来模拟多个计算机实现并行计算。
- 此次优化测试结果：我将106M的文件平均分成7份，计算时长为755ms，如果是4G文件，约需要30秒。但这不重要，因为如果用hadoop实现分布式，总时长只需要一份的时间。

### 使用线程模拟分配任务
```java
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
```

### 汇总结果

```java
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
```

    最后：感谢qida_wu对程序提出的意见！
