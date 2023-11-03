package com.hongyan.study.springbootcsv.csv.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.hongyan.study.springbootcsv.csv.bean.CsvFile;
import com.hongyan.study.springbootcsv.csv.util.CsvUtils;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/csv")
@Slf4j
public class CsvController {

    private List<String[]> dataList = new ArrayList<>();

    private List<CsvFile> csvDataList = new ArrayList<>();
    @PostConstruct
    public void generateCsvData() {
        // 生成CSV文件
        dataList = List.of(
                new String[]{"Name","Age","Email"},
                new String[]{"John","30","john@example.com"},
                new String[]{"Alice","25","alice@example.com"}
        );

        csvDataList = List.of(
                CsvFile.builder().name("zhangsan").age(20).sex("M").build(),
                CsvFile.builder().name("lisi").age(21).sex("F").build(),
                CsvFile.builder().name("wangwu").age(22).sex("M").build()
        );
    }

    @GetMapping("/exportCsvOpenCsv")
    public void exportCsvOpenCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            //采用LocalDate实现 XXXX年XX月XX日 输出
            LocalDate localDate = LocalDate.now();
            String current = localDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            String fileName = String.format("【截止%s】CSV文件导出.csv", current);

            //获取csv文件数据
            //计算csv文件数据hash值
            String csvDataHash = calculateCsvDataHash(dataList);
            log.info("exportCsvOpenCsv-csv文件数据hash值为:{}", csvDataHash);
            //截取 csvDataHash 后8位 作为文件名
            if (csvDataHash.length() > 8) {
                csvDataHash = csvDataHash.substring(csvDataHash.length() - 8);
            }
            // 添加哈希值到文件名中
            fileName = fileName.replace(".csv", "-" + csvDataHash + ".csv");

            log.info("exportCsvOpenCsv-fileName:{}", fileName);
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            //输出csv文件
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));

            writer.writeAll(dataList);
            writer.close();
        } catch (IOException e) {
            log.error("exportCsvOpenCsv error:", e);
            e.printStackTrace();
        }
    }

    @GetMapping("/exportCsvToBean")
    public void exportCsvToBean(HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            //采用LocalDate实现 XXXX年XX月XX日 输出
            LocalDate localDate = LocalDate.now();
            String current = localDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            String fileName = String.format("exportCsvToBean-%s.csv", current);

            //获取csv文件数据
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            //输出csv文件
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            CsvUtils.exportCsvMethod1(fileName, csvDataList, response);
        } catch (IOException e) {
            log.error("exportCsvOpenCsv error:", e);
            e.printStackTrace();
        }
    }

    /**
     * 导入 CSV 文件-手动解析CSV文件(方法一)
     * @param file
     * @return
     */
    @PostMapping("/importCsv")
    public Boolean importCsv(@RequestParam("file") MultipartFile file) {
        List<String[]> csvFileList = CsvUtils.getCsvDataMethod1(file);
        csvFileList.stream().forEach(System.out::println);
        return true;
    }

    /**
     * 导入 CSV 文件-手动解析CSV文件(方法一)
     * @param file
     * @return
     */
    @PostMapping("/importCsvBean")
    public Boolean importCsvBean(@RequestParam("file") MultipartFile file) {
        List<CsvFile> csvFileList = CsvUtils.getCsvDataToBeanMethod1(file);
        csvFileList.stream().forEach(System.out::println);
        return true;
    }


    /**
     * 导入 CSV 文件-openCSV解析CSV文件(方法二)
     * @param file
     * @return
     */
    @PostMapping("/importCsvForOpenCsv")
    public Boolean importCsvForOpenCsv(@RequestParam("file") MultipartFile file) {
        List<String[]> csvFileList = CsvUtils.getCsvDataMethod2(file);
        csvFileList.stream().forEach(System.out::println);
        return true;
    }

    /**
     * 导入 CSV 文件并转成 bean (方法三)
     * @param file
     * @return
     */
    @PostMapping("/importCsvToBean")
    public Boolean importCsvToBean(@RequestParam("file") MultipartFile file) {
        List<CsvFile> csvFileList = CsvUtils.getCsvDataMethod3(file, CsvFile.class);
        csvFileList.stream().forEach(System.out::println);
        return true;
    }

    /**
     * 计算CSV数据的哈希值
     * 由于转化成List<String[]>后，原始数据的\n 和 , 符号都被剔除了，所以需要补充回来，这样才能对上原始数据 hash256 后结果
     * @param dataList
     * @return
     */
    private String calculateCsvDataHash(List<String[]> dataList) {
        StringBuilder dataStringBuilder = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            String[] row = dataList.get(i);
            for (int j = 0; j < row.length; j ++) {
                String value = row[j];
                dataStringBuilder.append(value);
                if (j != row.length - 1) {
                    dataStringBuilder.append(",");
                }
            }
            if (i != dataList.size() - 1) {
                dataStringBuilder.append("\n");
            }
        }

        String dataToHash = dataStringBuilder.toString();

        // 使用SHA-256计算CSV数据的哈希值
        return SecureUtil.sha256(dataToHash);
    }
}
