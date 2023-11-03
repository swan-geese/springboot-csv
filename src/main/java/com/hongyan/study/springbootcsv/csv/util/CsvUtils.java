package com.hongyan.study.springbootcsv.csv.util;

import cn.hutool.core.io.FileUtil;
import com.hongyan.study.springbootcsv.csv.bean.CsvFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zy
 * @date Created in 2023/10/30 5:19 PM
 * @description
 */
@Slf4j
public class CsvUtils {


    /**
     * 导出 csv 文件 （方法一）
     * @param fileName
     * @param csvFileList
     */
    @SneakyThrows
    public static void exportCsvMethod1(String fileName, List<CsvFile> csvFileList, HttpServletResponse response) {
        Writer writer = new FileWriter(fileName);
//        Writer writer = new OutputStreamWriter(new FileOutputStream(fileName));
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(csvFileList);
        FileUtil.writeToStream(new File(fileName), response.getOutputStream());
        csvWriter.close();
        writer.close();
    }


    /**
     * 导出 csv 文件 （方法二）
     * @param fileName
     * @param response
     * @param csvFileList
     */
    @SneakyThrows
    public static void exportCsvMethod2(String fileName, List<CsvFile> csvFileList, HttpServletResponse response) {
        String[] header = new String[]{"name","age","sex"};
        Writer writer = new OutputStreamWriter(new FileOutputStream(fileName));
        //如果设置 ColumnPositionMappingStrategy 则表明剔除 header 头信息，只输出list数据，
        // 如果不设置 strategy 则按照 @CsvBindByName 注解来输出 header 头信息
        ColumnPositionMappingStrategy<CsvFile> strategy = new ColumnPositionMappingStrategy();
        strategy.setColumnMapping(header);
        strategy.setType(CsvFile.class);
        StatefulBeanToCsv<CsvFile> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                .withMappingStrategy(strategy)
                .build();

        FileUtil.writeToStream(new File(fileName), response.getOutputStream());
        beanToCsv.write(csvFileList);
        writer.close();
    }

    /**
     * 解析csv文件并转成bean（方法一）
     * 手动解析CSV文件
     * @param file
     * @return
     */
    public static List<CsvFile> getCsvDataToBeanMethod1(MultipartFile file) {
        List<CsvFile> csvFileList = new ArrayList<>();
        InputStreamReader in = null;
        String s = null;
        try {
            in = new InputStreamReader(file.getInputStream(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(in);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(",");
                CsvFile csvFile = new CsvFile();
                csvFile.setName(splitResult(split[0]));
                csvFile.setAge(Integer.parseInt(splitResult(split[1])));
                csvFile.setSex(splitResult(split[2]));
                csvFileList.add(csvFile);
            }
        } catch (IOException e) {
            log.error("getCsvDataToBeanMethod1 error:", e);
            e.printStackTrace();
        }
        return csvFileList;
    }

    /**
     * 手动解析csv文件（方法一）
     * 手动解析CSV文件
     * @param file
     * @return
     */
    public static List<String[]> getCsvDataMethod1(MultipartFile file) {
        List<String[]> csvFileList = new ArrayList<>();
        InputStreamReader in = null;
        String s = null;
        try {
            in = new InputStreamReader(file.getInputStream(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(in);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(",");
                csvFileList.add(split);
            }
        } catch (IOException e) {
            log.error("getCsvDataMethod1 error:", e);
            e.printStackTrace();
        }
        return csvFileList;
    }

    /**
     * 解析csv文件并转成bean（方法二）
     * openCSV解析CSV文件
     * @param file csv文件
     * @return 数组
     */
    public static List<String[]> getCsvDataMethod2(MultipartFile file) {
        List<String[]> list = new ArrayList<>();
        int i = 0;
        try {
            CSVReader csvReader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(file.getInputStream(), "utf-8"))).build();
            Iterator<String[]> iterator = csvReader.iterator();
            while (iterator.hasNext()) {
                String[] next = iterator.next();
                //去除第一行的表头，从第二行开始
                if (i >= 1) {
                    list.add(next);
                }
                i++;
            }
            return list;
        } catch (Exception e) {
            log.error("CSV文件读取异常:", e);
            return list;
        }
    }


    /**
     * 解析csv文件并转成bean（方法三）
     * openCSV解析CSV文件(结果为实体类)
     * @param file  csv文件
     * @param clazz 类
     * @param <T>   泛型
     * @return 泛型bean集合
     */
    public static <T> List<T> getCsvDataMethod3(MultipartFile file, Class<T> clazz) {
        InputStreamReader in = null;
        CsvToBean<T> csvToBean = null;
        try {
            in = new InputStreamReader(file.getInputStream(), "utf-8");
            HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(clazz);
            csvToBean = new CsvToBeanBuilder<T>(in).withMappingStrategy(strategy).build();
        } catch (Exception e) {
            log.error("数据转化失败:", e);
            return null;
        }
        return csvToBean.parse();
    }


    private static String splitResult(String once) {
        String result = "";
        for (int i = 0; i < once.length(); i++) {
            if (once.charAt(i) != '"') {
                result += once.charAt(i);
            }
        }
        return result;
    }
}
