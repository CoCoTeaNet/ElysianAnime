package net.cocotea.elysiananime.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 数据结构生成工具
 *
 * @author CoCoTea
 * @version 2.0.0
 */
public class TreeBuilder<T> {

    final String GET = "get";
    final String SET = "set";
    final String PARENT_ID = "ParentId";
    final String ID = "Id";
    final String CHILDREN = "Children";

    /**
     * 生成树结构
     * 属性名称默认（父级parentId、主键id、子节点children）
     *
     * @param ls 列表集合
     * @return 树形map结构
     */
    public Map<String, T> build(List<T> ls) {
        return build(ls, PARENT_ID, ID, CHILDREN);
    }

    /**
     * 获取树结构列表，属性名称默认（父级parentId、主键id、子节点children）
     *
     * @param ls 列表集合
     * @return 树结构列表
     */
    public List<T> get(List<T> ls) {
        Map<String, T> treeMap = build(ls);
        return new ArrayList<>(treeMap.values());
    }

    /**
     * 生成树结构
     * 属性名称必须大写
     *
     * @param ls           列表集合
     * @param parentIdName 父节点属性名称
     * @param mainIdName   主节点属性名称
     * @param childrenName 子节点属性名称
     * @return 树形map结构
     */
    public Map<String, T> build(List<T> ls, String parentIdName, String mainIdName, String childrenName) {
        try {
            return buildTreeCore(ls, GET + parentIdName, GET + mainIdName, childrenName);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    /**
     * 生成树结构
     * 属性名称必须大写
     *
     * @param tList        列表集合
     * @param parentIdName 获取父节点方法名称
     * @param mainIdName   获取主节点方法名称
     * @param childrenName 子节点属性名称
     * @return 树形map结构
     */
    private Map<String, T> buildTreeCore(List<T> tList, String parentIdName, String mainIdName, String childrenName) throws Exception {
        Map<String, T> menuRootMap = new LinkedHashMap<>(tList.size());
        Map<String, T> menuChildMap = new LinkedHashMap<>(tList.size());
        Object root = String.valueOf(0);
        // 1.将根节点元素和子节点元素通过id映射分离
        for (T item : tList) {
            Class<?> itemClass = item.getClass();
            Method getParentIdMethod;
            Method getIdMethod;
            getParentIdMethod = itemClass.getMethod(parentIdName);
            getIdMethod = itemClass.getMethod(mainIdName);
            // 父级是否根节点
            Object parentId = getParentIdMethod.invoke(item);
            boolean rootFlag = root.equals(String.valueOf(parentId));
            if (rootFlag) {
                menuRootMap.put(String.valueOf(getIdMethod.invoke(item)), item);
            } else {
                menuChildMap.put(String.valueOf(getIdMethod.invoke(item)), item);
            }
        }
        // 遍历子节点，通过映射id去给子节点分配父元素
        for (Map.Entry<String, T> item : menuChildMap.entrySet()) {
            Object value = item.getValue();
            Class<?> valueClass = value.getClass();
            Object parentId = String.valueOf(valueClass.getMethod(parentIdName).invoke(value));
            if (menuRootMap.get(parentId) != null) {
                addToSet(childrenName, value, menuRootMap.get(parentId));
            } else if (menuChildMap.get(parentId) != null) {
                addToSet(childrenName, value, menuChildMap.get(parentId));
            } else if(parentId != null) {
                menuRootMap.put(item.getKey(), item.getValue());
            } else {
                throw new RuntimeException();
            }
        }
        return menuRootMap;
    }

    /**
     * 将子元素添加到集合
     *
     * @param childrenName 子节点属性名称
     * @param childNode    子节点
     * @param parentNode   父节点
     */
    private void addToSet(String childrenName, Object childNode, Object parentNode)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method childrenGetMethod = parentNode.getClass().getMethod(GET + childrenName);
        Object children = childrenGetMethod.invoke(parentNode);
        if (children != null) {
            if (children instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> ls = (List<Object>) children;
                ls.add(childNode);
            }
        } else {
            ArrayList<Object> list = new ArrayList<>();
            list.add(childNode);
            parentNode
                    .getClass()
                    .getMethod(SET + childrenName, List.class)
                    .invoke(parentNode, list);
        }
    }

}
