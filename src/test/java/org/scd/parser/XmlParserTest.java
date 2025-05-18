package org.scd.parser;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParserTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlParserTest.class);

    @Test
    public void testParseByPath() {
        XmlParser xmlParser = new XmlParser("C:\\Users\\Administrator\\Desktop\\idea-plugin\\keymap");
        xmlParser.parse();
        var res = xmlParser.getResult();
        LOGGER.info("parse result {}", res);
    }
}
