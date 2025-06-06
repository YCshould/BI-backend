package com.wu.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    public static String exceltocsv(MultipartFile multipartFile) throws FileNotFoundException {
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:test_excel.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (Exception e) {
            log.error("表格处理错误", e);
            throw new RuntimeException(e);

        }
        System.out.println(list);

        if(CollUtil.isEmpty(list)){
            return "empty";
        }

        StringBuilder stringBuilder = new StringBuilder();

        // 表头
        LinkedHashMap<Integer, String> integerStringMap = (LinkedHashMap)list.get(0);
        List<String> collect = integerStringMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(StringUtils.join(collect, ",")).append("\n");

        for (int i=1;i<list.size();i++){
            LinkedHashMap<Integer, String> map = (LinkedHashMap)list.get(i);
            List<String> maplist = map.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(maplist, ",")).append("\n");
        }
        System.out.println(stringBuilder);
        return stringBuilder.toString();
    }


}
