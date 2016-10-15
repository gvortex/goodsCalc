package com.jvortex.common;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYingUtils {
	private static HanyuPinyinOutputFormat spellFormat=new HanyuPinyinOutputFormat();
	static{
		spellFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);    
        spellFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);    
        spellFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	 public static String chineneToSpell(String chineseStr) throws BadHanyuPinyinOutputFormatCombination{  
	        return PinyinHelper.toHanyuPinyinString(chineseStr , spellFormat ,"");  
	 }  
	 public static void main(String[] args){
		System.out.println("asdf");
	}
	 
}
