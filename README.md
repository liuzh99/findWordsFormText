## 统计文本文件中每个英文单词出现的次数

- Task继承Callable接口，可在线程结束后获取到返回值
```java
public class Task implements Callable<Map<String,Integer>>{
	
	//当前节点的文件输入流
	private String inputFileName;
	public Task(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	//存储计算结果
	private Map<String,Integer> wordsMap = new HashMap<String,Integer>();
	
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

```
- 将文件分离成多个文件，就能分配给多个线程查找单词的记数，当前默认大小为4M
```java
/**
 * title:WordsCount
 * descript:从指定文件中查找
 * @author liuzh
 * @date 2016年4月27日 下午8:56:46
 */
public class WordsCount{
	//所有结果
	private Map<String,Integer> totalWordsMap = new HashMap<String,Integer>();
	//各个线程的结果
	private List<Future<Map<String,Integer>>> wordsMapList = new LinkedList<Future<Map<String,Integer>>>();
	private synchronized void addToWordsMap(Future<Map<String,Integer>> map){
		wordsMapList.add(map);
	}
	
	
	//定义每个文件的大小为4M
	private final int fileBufferSize = 4*1024*1024;
	
	//输入的文件名
	private String inputFileName;
	//输入文件的路径
	String outputPath;
	//存储分离的文件
	private List<String> fileList = new LinkedList<String>();
	
	//用数据作为分离后的各个文件名
	private Integer fileNameCount = 0;
	public String getNewFileName(){
		fileList.add(outputPath + fileNameCount.toString());
		return outputPath + (fileNameCount++).toString();
	}
	
	/**
	 * 分离文件
	 * 先读取fileBufferSize个字节的数据，再找到后面不是字母的位置，从此位置分离文件
	 * 所以分离后的文件可能会比fileBufferSize多几个字节
	 * @throws IOException 
	 */
	private void splitFile() throws IOException{
		byte[] b = new byte[fileBufferSize];
		InputStream inputStream = new FileInputStream(inputFileName);
		
		outputPath = inputFileName.substring(0,inputFileName.lastIndexOf("/") + 1);

		//当前读取的长度
		OutputStream outputStream = new FileOutputStream(getNewFileName());
		int readLength=inputStream.read(b);
		if(readLength == -1){
			outputStream.close();
			inputStream.close();
			return;
		}
		outputStream.write(b);
		while((readLength=inputStream.read(b)) != -1){
			//把前面是字母的输入到上一个文件
			int i;
			for(i=0;i<readLength;i++){
				if(Character.isAlphabetic(b[i])){
					outputStream.write(b[i]);
				}else{
					break;
				}
			}
			//表示readLength全都是字母
			if(i == readLength){
				continue;
			}
			//打开一个新的文件，将剩余的字母输入到新文件中
			outputStream.close();
			outputStream = new FileOutputStream(getNewFileName());
			outputStream.write(b, i, readLength - i);
		}
		inputStream.close();
		outputStream.close();
	}
	/**
	 * 将wordsMapList中的结果汇总到totalWordsMap
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void collect() throws InterruptedException, ExecutionException{
		for (Future<Map<String, Integer>> map : wordsMapList) {

			if (totalWordsMap.size() == 0) {
				totalWordsMap.putAll(map.get());
				continue;
			}
			Iterator<String> keys = map.get().keySet().iterator();
			String key;
			Integer value;
			while (keys.hasNext()) {
				key = keys.next();
				value = map.get().get(key);
				// 找到，则加入记数
				if (totalWordsMap.containsKey(key)) {
					Integer valuet = totalWordsMap.get(key);
					totalWordsMap.put(key, value + valuet);
				}
				// 没找到，加入一个元素
				else {
					totalWordsMap.put(key, value);
				}
			}
		}
	}
	
	/**
	 * 分派任务
	 * @param files
	 * @param outputFileName
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	public void reduce(String inputFileName,String outputFileName) throws IOException, InterruptedException, ExecutionException{
		this.inputFileName = inputFileName;
		//分离文件
		splitFile();
		
		WordsCount wordsCount = null;
		
		int maximumPoolSize = 5;
		
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, maximumPoolSize, 0, TimeUnit.SECONDS,
				new LinkedBlockingDeque<Runnable>(),
				new ThreadPoolExecutor.CallerRunsPolicy());
		
		for(int i=0;i<fileList.size();i++){
			Task task = new Task(fileList.get(i));
			Future<Map<String,Integer>> result = tpe.submit(task);
			addToWordsMap(result);
		}
		tpe.shutdown();
		
		//1天，模拟永远等待
		System.out.println(tpe.awaitTermination(1, TimeUnit.DAYS));
		
		collect();

		output(outputFileName);
	}
	
}
```


    最后：感谢qida_wu对程序提出的意见！
