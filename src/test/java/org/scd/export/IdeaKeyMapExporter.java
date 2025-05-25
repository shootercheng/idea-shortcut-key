package org.scd.export;

import com.alibaba.fastjson.JSONObject;
import org.scd.jdbc.DuckDbJdbc;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IdeaKeyMapExporter {

    public static void main(String[] args) {
        try (var connection = DuckDbJdbc.getFileConnection("database/shortcut-key.duck");
             var statement = connection.prepareStatement("""
                        select id, text, metadata from idea_plugin_openai
                     """)) {
            var resultSet = statement.executeQuery();
            Map<String, IdeaKeyMapData> idMap = new HashMap<>();
            Map<String, IdeaKeyMapData> originIdMap = new HashMap<>();
            while (resultSet.next()) {
                var text = resultSet.getString("text");
                var metadataJson = resultSet.getString("metadata");
                JSONObject jsonObject = JSONObject.parseObject(metadataJson);
                String originId = jsonObject.getString("originId");
                if (originId != null) {
                    IdeaKeyMapData ideaKeyMapData = new IdeaKeyMapData();
                    ideaKeyMapData.setCnId(text);
                    originIdMap.put(originId, ideaKeyMapData);
                } else {
                    IdeaKeyMapData ideaKeyMapData = new IdeaKeyMapData();
                    ideaKeyMapData.setEnId(text);
                    int i = 1;
                    String shortCutKey;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((shortCutKey = jsonObject.getString("keyboard-shortcut-" + i)) != null) {
                        stringBuilder.append(shortCutKey).append(",");
                        i++;
                    }
                    if (!stringBuilder.isEmpty()) {
                        ideaKeyMapData.setShortcutKey(stringBuilder.substring(0, stringBuilder.length() - 1));
                    }
                    idMap.put(text, ideaKeyMapData);
                }
            }
            idMap.forEach((key, value) -> {
                IdeaKeyMapData cnKeyMap = originIdMap.get(key);
                if (cnKeyMap != null) {
                    value.setCnId(cnKeyMap.getCnId());
                }
            });
            String res = MarkdownTableExporter.exportToMarkdown(new ArrayList<>(idMap.values()));
            System.out.println(res);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
