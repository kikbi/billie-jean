package derek.tool.sql.util;

import java.util.ArrayList;

/**
 * 满足条件的添加，不满足条件的不添加，结构为ArrayList
 * @author Derek
 * @date 2022/6/10
 */
public class CList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 3113831527214052686L;

    public boolean addIf(boolean expression,E e){
        if (!expression){
            return false;
        }
        return add(e);
    }
}
