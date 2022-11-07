# 简介
本插件作用是根据java 实体类生成建表语句

在要导出的实体类中右键，选择 Gen Creation SQL菜单
弹出对话框操作复制到剪切板或导出到文件

## 导出界面

![gen creation sql](https://github.com/kikbi/billie-jean/blob/main/creation_sql.png?raw=true)

![custom dialect](https://github.com/kikbi/billie-jean/blob/main/custom_dialect.jpg?raw=true)



# v1 

- 选定要复制或导出的方言类型，可多选 
- 指定列名大小写转换和sql美化
- 支持字段上java文档注释标签
  * @scale 1
  * @length 500000
  * @default 0
  * @nonnull
  * @primaryKey
  * @index
  * @unique
- 支持类上java文档注释标签
  * @index
  * @table

例：



    /**
     * 表1
     * @index column1,column2
     * @table p_value
     */
    public class MyValue {
            /**  
             * 值
             * @default 0
             * @nonnull
             * @length 500
             * @index
             */ 
            String value;
    }

 


- 支持多方言同时选择
- 支持自定义方言导入，导入设置菜单在 Settings->Custom Dialect
- 导入xml需引入指定约束，格式不正确将无法识别
  
  xsd: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xsd

  example xml: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xml

## 支持项列表

|   |  默认支持  |  额外(配置)支持  |
| ------------ | ------------ | ------------ |
| 语言 | java |  |
| 方言 | * | 国产数据库方言 |



# v2 TODO
- 支持连接数据库生成修改脚本
- 支持项目级全实体扫描和建表语句
- 数据库转义字段支持


