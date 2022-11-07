# Introduction
generate creation SQL according to class javadoc

right click in a class file and select "Gen Creation SQL" menu

choose copying to clip board or exporting to specified file

## view

![gen creation sql](https://github.com/kikbi/billie-jean/blob/main/creation_sql.png?raw=true)

![custom dialect](https://github.com/kikbi/billie-jean/blob/main/custom_dialect.jpg?raw=true)



# version 1 

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

 

- support importing custom dialect,  Settings->Custom Dialect
- custom dialect xml file needs to pass xsd verification
  
  xsd: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xsd

  example xml: https://gitee.com/Enoxacin/billie-jean/blob/master/custom_dialect.xml

## supporting list

|   |  default  |  ext supporting  |
| ------------ | ------------ | ------------ |
| language | java |  |
| dialect | * | custom dialect |



