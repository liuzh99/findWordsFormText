# 统计文本文件中每个英文单词出现的次数 #

此题需要将查找的历史结果保留下来，并且集合大小会增加多次，后续还需要在这个集合中不断查找，所以，``为了查询速度和插入速度，我打算使用二分搜索算法，数据结构使用链表，并用数组链表实例化。``

一：首先定义集合中数组的结构：liuzh.interview.words.WordsCount<br>
&nbsp;&nbsp;主要是以下两个属性<br>
&nbsp;&nbsp;//单词出现的次数<br>
&nbsp;&nbsp;private int count;<br>
&nbsp;&nbsp;//当前单词<br>
&nbsp;&nbsp;private String words;<br>


二：开始设计程序：关键函数，liuzh.interview.words.Calcul.find(String)<br>
### 第一次实现功能： ###
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
### 第二次优化性能： ###
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

