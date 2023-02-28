package com.example.minio.common.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.graphics.PdfImageType;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * pdf工具类
 *
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2023/2/27 11:02
 */
@Log4j2
public class PdfUtil {

    /**
     * 拆分pdf文件
     * @param pdfFilePath  pdf文件地址
     * @param pdfSplitDirPath  拆分的pdf存放目录
     * @param splitNum  拆分页数
     * @return
     */
    public static List<String> pdfSplit(String pdfFilePath, String pdfSplitDirPath, int splitNum) throws Exception {
        PDDocument document = PDDocument.load(new File(pdfFilePath));

        // instantiating Splitter
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(splitNum);

        // split the pages of a PDF document
        List<PDDocument> pages = splitter.split(document);

        // Creating an iterator
        Iterator<PDDocument> iterator = pages.listIterator();

        File pdfSplitDirFile = new File(pdfSplitDirPath);
        if (!pdfSplitDirFile.exists()){
            pdfSplitDirFile.mkdirs();
        }
        pdfSplitDirPath = pdfSplitDirFile.getAbsolutePath();

        // saving splits as pdf
        List<String> list = new ArrayList<>();
        int i = 0;
        while(iterator.hasNext()) {
            PDDocument pd = iterator.next();

            // provide destination path to the PDF split
            String splitPdfFilPath = String.format("%s%s%d.pdf", pdfSplitDirPath, File.separator, ++i);
            pd.save(splitPdfFilPath);
            pd.close();

            list.add(splitPdfFilPath);
        }

        document.close();

        return list;
    }

    /**
     * pdf转图片
     * @param pdfFilePath  pdf文件地址
     * @param imagesDirPath  图片存放文件夹路径
     * @param isBase64  是否转为base64
     * @return
     */
    public static List<String> pdfToImages(String pdfFilePath, String imagesDirPath, boolean isBase64) throws Exception {
        // 判断目录是否存证，不存在则创建
        File imagesDirFile = new File(imagesDirPath);
        if (!imagesDirFile.exists()) {
            imagesDirFile.mkdirs();
        }
        final String finalImagesDirPath = imagesDirFile.getAbsolutePath();

        // 创建pdf拆分目录
        String pdfSplitDirPath = finalImagesDirPath + File.separator + "pdfs";
        // 因为冰蓝pdf转图片免费版仅支持3页所已这里分割页数最大只能设置成3
        final Integer spiltNum = 1;
        // 分割pdf为多个小文件
        List<String> pdfSpiltFilePathList = pdfSplit(pdfFilePath, pdfSplitDirPath, spiltNum);
        int pdfSpiltFileNum = pdfSpiltFilePathList.size();

        // 使用多线程将pdf转为图片
        Map<Integer, String> imageFilePathMap = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(pdfSpiltFileNum);
        for (int i = 0; i < pdfSpiltFileNum; i++) {
            // pdf文件路径
            final String pdfSpiltFilePath = pdfSpiltFilePathList.get(i);
            // 开始页数
            final int startPage = i * spiltNum + 1;
            new Thread(() -> {
                try {
                    //实例化PdfDocument类的对象
                    PdfDocument pdf = new PdfDocument();
                    //加载PDF文档
                    pdf.loadFromFile(pdfSpiltFilePath);
                    //遍历PDF每一页，保存为图片
                    for (int j = 0; j < pdf.getPages().getCount(); j++) {
                        //将页面保存为图片，并设置DPI分辨率
                        BufferedImage image = pdf.saveAsImage(j, PdfImageType.Bitmap,250,250);
                        String result = null;
                        if (isBase64) {
                            result = ImgUtil.toBase64(image, "PNG");
                        } else {
                            // 将图片保存为png格式
                            String imageFilePath = String.format(("%s%s%d.png"), finalImagesDirPath, File.separator, startPage + j);
                            File file = new File(imageFilePath);
                            ImageIO.write(image, "PNG", file);
                            result = imageFilePath;
                        }
                        if (result != null) {
                            imageFilePathMap.put(startPage, result);
                        }
                    }
                    pdf.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();

        // 删除pdfdir下的pdf文件
        FileUtil.del(pdfSplitDirPath);

        // 图片更具文件名进行排序
        Set<Integer> keySet = imageFilePathMap.keySet();
        List<Integer> keyList = keySet.stream().sorted(Integer::compareTo).collect(Collectors.toList());
        List<String> imageFileList = new ArrayList<>(keyList.size());
        for (Integer key : keyList) {
            String s = imageFilePathMap.get(key);
            if (s != null) {
                imageFileList.add(s);
            }
        }
        return imageFileList;
    }

}
