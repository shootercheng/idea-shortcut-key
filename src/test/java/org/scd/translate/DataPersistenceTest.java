package org.scd.translate;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataPersistenceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPersistenceTest.class);

    @Test
    public void testInit() {
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        LOGGER.info("execute ok");
    }

    @Test
    public void testInsertOne() {
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        int res = dataPersistence.insertData("test", "测试");
        Assert.assertEquals(1, res);
        dataPersistence.deleteByEn("test");
    }

    @Test
    public void testQueryAll() {
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        List<TranslateItem> dataList = new ArrayList<>();
        dataList.add(new TranslateItem("test1", "测试1"));
        dataList.add(new TranslateItem("test2", "测试2"));
        dataList.add(new TranslateItem("test3", "测试3"));
        dataPersistence.insertDataList(dataList);
        List<TranslateItem> dataPersistenceList = dataPersistence.queryAllData();
        dataPersistenceList.forEach(item -> {
            LOGGER.info("data {}", item);
        });
        dataPersistence.dropTable();
    }

    @Test
    public void testPrintAll() {
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        List<TranslateItem> dataPersistenceList = dataPersistence.queryAllData();
        LOGGER.info("data persistence size {}", dataPersistenceList.size());
    }

    @Test
    public void testDeleteByEn() {
        DataPersistence dataPersistence = new DataPersistence("database/shortcut-key.duck");
        dataPersistence.deleteByEn("FindInPath");
    }
}
