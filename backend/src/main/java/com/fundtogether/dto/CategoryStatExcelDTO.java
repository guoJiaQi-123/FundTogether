package com.fundtogether.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 项目分类统计Excel导出数据传输对象
 * <p>
 * 用于按项目分类维度汇总统计数据，并支持通过EasyExcel导出为Excel文件。
 * 包含分类名称、该分类下的项目数量以及累计筹资金额等统计指标。
 * </p>
 */
@Data
public class CategoryStatExcelDTO {

    /** 分类名称，对应项目分类表中的分类名称 */
    @ExcelProperty("分类名称")
    private String categoryName;

    /** 该分类下的项目总数量 */
    @ExcelProperty("项目数量")
    private Long projectCount;

    /** 该分类下所有项目的累计筹资金额（单位：元） */
    @ExcelProperty("累计金额")
    private BigDecimal totalAmount;
}
