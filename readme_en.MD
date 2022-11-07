# 简介
generate creation SQL according to class javadoc

Right click in a class file to export, then select "Gen Creation SQL" menu

choose copy to clip board or export to specified file

## view

![gen creation sql](https://github.com/kikbi/billie-jean/blob/main/creation_sql.png?raw=true)

![custom dialect](https://github.com/kikbi/billie-jean/blob/main/custom_dialect.jpg?raw=true)



# v1 

- supported javadoc tag
  * @scale 1
  * @length 500000
  * @default 0
  * @nonnull
  * @primaryKey
  * @index
  * @unique
- supported class javadoc tag
  * @index
  * @table

e.g.



    /**
     * table_1
     * @index column1,column2
     * @table p_value
     */
    public class MyValue {
            /**  
             * this is value
             * @default 0
             * @nonnull
             * @length 500
             * @index
             */ 
            String value;
    }

 

- support import custom dialect,  Settings->Custom Dialect
- custom dialect xml needs to pass xsd verification
  
  xsd: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xsd

  example xml: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xml

## 支持项列表

|   |  default  |  ext supporting  |
| ------------ | ------------ | ------------ |
| language | java |  |
| dialect | * | custom dialect |



