package com.hongyan.study.springbootcsv.csv.bean;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zy
 * @date Created in 2023/10/30 5:18 PM
 * @description csv bean
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CsvFile implements Serializable {

    @CsvBindByName(column = "nickName")
    private String name;

    @CsvBindByName(column = "age")
    private Integer age;

    @CsvBindByName(column = "sex")
    private String sex;

}
