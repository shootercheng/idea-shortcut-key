package org.scd.translate;

import org.scd.jdbc.DuckDbJdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DataPersistence {
    private String CREATE_TABLE_SQL = """
              create table if not exists t_key_en_cn (
                 en varchar primary key,
                 cn varchar
              )\s
            \s""";

    private String INSERT_DATA = "insert into t_key_en_cn(en, cn) values(?, ?)";

    private String QUERY_BY_EN = "select * from t_key_en_cn where en = ?";

    private String QUERY_ALL = "select * from t_key_en_cn";

    private String DEL_BY_EN = "delete from t_key_en_cn where en = ?";

    private String UPDATE_BY_EN = "update t_key_en_cn set cn = ? where en = ?";

    private String DROP_TABLE = "drop table t_key_en_cn";


    private String dbPath;

    public DataPersistence(String dbPath) {
        this.dbPath = dbPath;
        init();
    }

    private void init() {
        try (Connection connection = DuckDbJdbc.getFileConnection(dbPath);
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insertData(String en, String cn) {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(INSERT_DATA)) {
            statement.setString(1, en);
            statement.setString(2, cn);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insertDataList(List<TranslateItem> translateItemList) {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(INSERT_DATA)) {
            for (TranslateItem translateItem : translateItemList) {
                statement.setString(1, translateItem.getEn());
                statement.setString(2, translateItem.getCn());
                statement.addBatch();
            }
            int[] res = statement.executeBatch();
            return res.length;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TranslateItem queryDataByEn(String en) {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(QUERY_BY_EN)) {
            statement.setString(1, en);
            var resultSet = statement.executeQuery();
            TranslateItem translateItem = new TranslateItem();
            while (resultSet.next()) {
                var resEn = resultSet.getString("en");
                var resCn = resultSet.getString("cn");
                translateItem.setEn(resEn);
                translateItem.setCn(resCn);
            }
            return translateItem;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TranslateItem> queryAllData() {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(QUERY_ALL)) {
            var resultSet = statement.executeQuery();
            List<TranslateItem> translateItemList = new ArrayList<>();
            while (resultSet.next()) {
                var resEn = resultSet.getString("en");
                var resCn = resultSet.getString("cn");
                TranslateItem translateItem = new TranslateItem(resEn, resCn);
                translateItemList.add(translateItem);
            }
            return translateItemList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByEn(String en) {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(DEL_BY_EN)) {
            statement.setString(1, en);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateDataByEn(String en, String cn) {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(UPDATE_BY_EN)) {
            statement.setString(1, en);
            statement.setString(2, cn);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dropTable() {
        try (var connection = DuckDbJdbc.getFileConnection(dbPath);
             var statement = connection.prepareStatement(DROP_TABLE)) {
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
