package com.example.minio.common.utils.office;

import cn.hutool.core.util.StrUtil;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @email 2434017367@qq.com
 * @author: zhy
 * @date: 2020/7/17
 * @time: 10:34
 */
public class WordUtils {

    public static void creatWordByModel(String tmpFile, Map<String, String> contentMap, String exportFile){

        // 获取模板文件
        Document document = new Document(tmpFile);

        // 根据map替换文本信息
        for (Map.Entry<String, String> entry : contentMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isEmpty(value)){
                String s = "";
                for (int i = 0; i < key.length() + 3; i++) {
                    s += " ";
                }
                value = s;
            }
//            document.replace("${" + key + "}", value, false, true);
            document.replace(Pattern.compile("\\$\\{" + key + "\\}"), value);
        }

        // 保存文本
        document.saveToFile(exportFile, FileFormat.Docx_2013);

    }

    /**
     *
     * @param docFilePath doc文件地址
     * @param pdfFilePath pdf文件地址
     * @throws Exception
     */
    public static void docToPdf(String docFilePath, String pdfFilePath) throws Exception {
        //加载word示例文档
        Document document = new Document();
        document.loadFromFile(docFilePath);
        int pageCount = document.getPageCount();
        if (pageCount > 3){
            OfficeToPdf.word2Pdf(docFilePath, pdfFilePath);
        }else{
            //保存结果文件
            document.saveToFile(pdfFilePath, FileFormat.PDF);
        }
    }

}
