package Exp01;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;



public class Analysis {
    //map保存单词和编码之间的映射关系
    private static HashMap<String, Integer> map;
    //缓冲区字符流
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    //待解析代码路径
    private static String inputFilePath;
    //输出结果路径
    private static String outputFilePath;
    //当前正在分析的单词
    private static StringBuffer curString;
    //当前字符在正好字符串中的下标
    private static int curIndex;
    //当前在NFA中的状态
    private static int state;
    //记录行号
    private static int cntLine = 0;
    //写入文件时每列的列宽
    private static final int N = 25;

    /**
     * 获取bufferedReader
     */
    public static void initBufferReader(String path) throws FileNotFoundException {
        FileInputStream fin = new FileInputStream(path);
        InputStreamReader reader = new InputStreamReader(fin);
        bufferedReader = new BufferedReader(reader);
    }

    /**
     * 获取bufferedWriter
     */
    public static void initBufferedWriter(String path) throws IOException {
        clearFile(path);
        FileOutputStream fileOutputStream = new FileOutputStream(path, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        bufferedWriter = new BufferedWriter(outputStreamWriter);
        initFileTitle();
    }

    /**
     * 开始写入前先清空文件
     */
    public static void clearFile(String path) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        bufferedWriter.close();
    }

    public static void writerCenter(String str, int n) throws IOException {
        int len = str.length();
        for(int i = 0 ; i < (n - len) / 2 ; i++){
            bufferedWriter.write(' ');
        }
        bufferedWriter.write(str);
        for(int i = 0 ; i < (n - len) - (n - len) / 2 ; i++){
            bufferedWriter.write(' ');
        }
        bufferedWriter.write('|');
    }

    public static void writeError() throws IOException {
        writerCenter("Error", N * 3 + 2);
        writerCenter(String.valueOf(cntLine), N);
        bufferedWriter.write('\n');
        writeLine();
    }


    public static void writeLine() throws IOException {
        for (int i = 0; i < N * 4 + 4; i++) {
            bufferedWriter.write('-');
        }
        bufferedWriter.write('\n');
    }

    public static void initFileTitle() throws IOException {
        writerCenter("code", N);
        writerCenter("word", N);
        writerCenter("type", N);
        writerCenter("lineNo", N);
        bufferedWriter.write('\n');
        writeLine();
        bufferedWriter.flush();
    }

    public static void writeToken(List<Token> tokenList, int lineNo) throws IOException {
        for(Token token : tokenList){
            writerCenter(String.valueOf(token.getCode()), N);
            writerCenter(token.getValue(), N);
            writerCenter(token.getType(), N);
            writerCenter(String.valueOf(cntLine), N);
            bufferedWriter.write('\n');
        }
        if(tokenList.size() > 0){
            writeLine();
        }
        bufferedWriter.flush();
    }


    /**
     * 初始化Map，映射字符与其编号的对应
     */
    public static void initMap() throws IOException {
        map = new HashMap<>();
        //读入单词和编号对应的json文件
        File jsonFile = new File("src/Exp01/codetable.json");
        JSONArray jsonArray = JSON.parseArray(FileUtils.readFileToString(jsonFile,"utf-8"));
        //将对应关系存入map中
        jsonArray.forEach(object -> {
            JSONObject jsonObject = (JSONObject) object;
            map.put(jsonObject.getString("word"), jsonObject.getInteger("index"));
        });
    }

    /**
     * 超前搜索后回退一步
     */
    public static void moveBack(){
        curString.deleteCharAt(curString.length() - 1);
        curIndex--;
    }

    /**
     * 分析完一个单词后将curString和state重置
     */
    public static void reSet(){
        curString.setLength(0);
        state = 0;
    }

    /**
     * 解析一行代码
     * @param str 代码行
     */
    public static void analysisLine(String str) throws IOException {
        cntLine++;
        List<Token> tokenList = new ArrayList<>();
        state = 0;
        curIndex = 0;
        curString = new StringBuffer();
        while(curIndex < str.length()){
            char ch = str.charAt(curIndex++);
            curString.append(ch);
            switch (state){
                case 0:
                    if(Character.isLetter(ch) | ch == '_')
                        state = 1;
                    else if(ch == '0')
                        state = 10;
                    else if(Character.isDigit(ch))
                        state = 3;
                    else if(ch == '+')
                        state = 14;
                    else if(ch == '-')
                        state = 18;
                    else if(ch == '*')
                        state = 22;
                    else if(ch == '/')
                        state = 25;
                    else if(ch == '=')
                        state = 28;
                    else if(ch == '%')
                        state = 31;
                    else if(ch == '^')
                        state = 34;
                    else if(ch == '!')
                        state = 37;
                    else if(ch == '&')
                        state = 40;
                    else if(ch == '|')
                        state = 44;
                    else if(ch == '>')
                        state = 48;
                    else if(ch == '<')
                        state = 54;
                    else if(   ch == '(' || ch == ')' || ch == '[' || ch == ']'
                            || ch == '{' ||ch == '}' || ch == '?' || ch == ':'
                            || ch == ';' ||ch == ',' || ch == '.' || ch == '#')
                        state = 60;
                    else if(ch == '"')
                        state = 61;
                    else if(ch == '\'')
                        state = 63;
                    else if(ch == ' ')
                        curString.setLength(0);
                    else{
                        state = 100;
                    }
                    break;
                case 1:
                    if(Character.isDigit(ch) || Character.isLetter(ch) || ch == '_')
                        state = 1;
                    else{
                        moveBack();
                        state = 2;
                    }
                    break;
                case 2://标识符或关键字类型
                    moveBack();
                    if(map.containsKey(curString.toString())){
                        tokenList.add(new Token(curString.toString(), "key", map.get(curString.toString())));
                    }else{
                        tokenList.add(new Token(curString.toString(), "identifier",0));
                    }
                    reSet();
                    break;
                case 3:
                    if(Character.isDigit(ch)){
                        state = 3;
                    }else if(ch == '.'){
                        state = 4;
                    }else if(ch == 'e' || ch == 'E'){
                        state = 6;
                    }else{
                        moveBack();
                        state = 9;
                    }
                    break;
                case 4:
                    if(Character.isDigit(ch))
                        state = 5;
                    else
                        state = 100;
                    break;
                case 5:
                    if(ch == 'E' || ch == 'e')
                        state = 6;
                    else if(Character.isDigit(ch))
                        state = 5;
                    else{
                        moveBack();
                        state = 9;
                    }
                    break;
                case 6:
                    if(ch == '+' || ch == '-')
                        state = 7;
                    else if(Character.isDigit(ch))
                        state = 8;
                    else
                        state = 100;
                    break;
                case 7:
                    if(Character.isDigit(ch))
                        state = 8;
                    else
                        state = 100;
                    break;
                case 8:
                    if(Character.isDigit(ch))
                        state = 8;
                    else{
                        moveBack();
                        state = 9;
                    }
                    break;
                case 9:
                    moveBack();
                    tokenList.add(new Token(curString.toString(), "const", 1));
                    reSet();
                    break;
                case 10:
                    if(ch == 'x')
                        state = 11;
                    else if(Character.isDigit(ch))
                        state = 3;
                    else{
                        moveBack();
                        state = 9;
                    }
                    break;
                case 11:
                    if(Character.isDigit(ch) || (ch >= 'a' && ch <= 'f'))
                        state = 12;
                    else
                        state = 100;
                    break;
                case 12:
                    if(Character.isDigit(ch) || (ch >= 'a' && ch <= 'f'))
                        state = 12;
                    else{
                        moveBack();
                        state = 9;
                    }
                    break;
                case 14:
                    if(!(ch == '+' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 15:
                    moveBack();
                    tokenList.add(new Token(curString.toString(), "operator", map.get(curString.toString())));
                    reSet();
                    break;
                case 18:
                    if(!(ch == '-' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 22:
                case 25:
                case 28:
                case 31:
                case 34:
                case 37:
                    if(!(ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 40:
                    if(!(ch == '&' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 44:
                    if(!(ch == '|' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 48:
                    if(!(ch == '>' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 54:
                    if(!(ch == '<' || ch == '=')){
                        moveBack();
                    }
                    state = 15;
                    break;
                case 60:
                    moveBack();
                    tokenList.add(new Token(curString.toString(), "bound", map.get(curString.toString())));
                    reSet();
                    break;
                case 61:
                    if(ch == '"')
                        state = 62;
                    else
                        state = 61;
                    break;
                case 62:
                    tokenList.add(new Token(curString.toString(), "const", 1));
                    reSet();
                    break;
                case 63:
                    state = 64;
                    break;
                case 64:
                    if(ch == '\'')
                        state = 62;
                    else
                        state = 100;
                    break;
                case 100:
                    writeError();
                    return;

                default:
                    break;
            }
        }
        writeToken(tokenList, cntLine);
    }


    public static void analysisFile() throws IOException {
        String strTmp;
        //记录多行注释是否已经开始
        boolean curLineIsComment = false;
        while((strTmp = bufferedReader.readLine())!=null){
            //去掉一个以上的空白符，用一个空白代替
            strTmp = strTmp.trim();
            strTmp = strTmp.replaceAll("\\s+", " ");
            //判断该行是否为注释
            if(strTmp.length() >= 2){
                //如果以/*开头：说明多行注释开始，curLineIsComment更新为true;
                if(strTmp.charAt(0) == '/' && strTmp.charAt(1) == '*'){
                    curLineIsComment = true;
                    continue;
                }
                //如果以*/开头：说明多行注释结束，curLineIsComment更新为false;
                if(strTmp.charAt(0) == '*' && strTmp.charAt(1) == '/'){
                    curLineIsComment = false;
                    continue;
                }
                //如果多行注释已经开始，直接continue;
                if(curLineIsComment){
                    continue;
                }
                //如果以//开头：说明当前行是单行注释
                if(strTmp.charAt(0) == '/' && strTmp.charAt(1) == '/'){
                    continue;
                }
            }
            strTmp += " ";
            // 使用DFA状态转换识别该行单词
            analysisLine(strTmp);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        //获取输入文件路径(待解析的代码文件)和输出路径
        System.out.println("please enter the inputPath");
        inputFilePath = scanner.nextLine();
        System.out.println("please enter the outputPath");
        outputFilePath = scanner.nextLine();
        //初始化编码表
        initMap();
        //获取输入输出流
        initBufferReader(inputFilePath);
        initBufferedWriter(outputFilePath);
        //分析
        analysisFile();
        //关闭流
        bufferedWriter.close();
        bufferedReader.close();
        System.out.println("completed!");
    }
}

