package org.b3log.symphony.util;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Zephyr on 2016/12/14.
 */
public class ToCodePoint {
    public static void main(String[] args){
        String output = readFileByLines("D:/index.html");
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(new File("D:\\index2.html"));
            byte b[] = output.getBytes();
            fs.write(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFileByLines(String fileName)  {
        File file = new File(fileName);
        BufferedReader reader = null;
        String output = "";
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                String search = StringUtils.substringBetween(tempString,"<img src=\"graphics/emojis/",".png\">");
                String codePoint = Integer.toHexString(EmojiParser.parseToUnicode(":"+search+":").codePointAt(0));
//                System.out.println(search + ": " + codePoint);
                if(null != search)
                    tempString = StringUtils.replaceOnce(tempString,search,codePoint);
                output += tempString + "\n";
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
            return output;
        }
    }
}