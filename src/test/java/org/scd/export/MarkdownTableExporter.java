package org.scd.export;


import java.lang.reflect.Field;
import java.util.List;

public class MarkdownTableExporter {

    /**
     * 将对象列表转换为Markdown表格
     * @param dataList 数据列表
     * @param <T> 泛型类型
     * @return markdown格式的表格字符串
     */
    public static <T> String exportToMarkdown(List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }

        Class<?> clazz = dataList.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();

        // 构建表头
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (Field field : fields) {
            sb.append(" ").append(field.getName()).append(" |");
        }
        sb.append("\n");

        // 构建分隔线
        sb.append("|");
        for (int i = 0; i < fields.length; i++) {
            sb.append(" --- |");
        }
        sb.append("\n");

        // 填充数据行
        for (T item : dataList) {
            sb.append("|");
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(item);
                    sb.append(" ").append(value != null ? value.toString() : "").append(" |");
                } catch (IllegalAccessException e) {
                    sb.append("  |");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}

