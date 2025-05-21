package org.scd.translate;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

public interface KeyIdTranslate {

    String answer(String question);


    @SystemMessage(value = "请将输入的idea快捷键的id翻译成中文," +
            "中文应该符合原有的idea快捷键含义。" +
            "输入信息是多个idea的快捷键id,用逗号隔开的，输出结果按照json格式," +
            "key为idea快捷键id,value为翻译之后的中文。" +
            "请严格按照json格式输出,程序可以直接解析的json,不要附带其它字符")
    String ideaKeyMapTranslate(String input);


    @SystemMessage(value = "请将输入的idea快捷键的id翻译成中文," +
            "中文应该符合原有的idea快捷键含义。" +
            "输入信息是多个idea的快捷键id,用逗号隔开的，输出结果按照json格式," +
            "key为idea快捷键id,value为翻译之后的中文。" +
            "请严格按照json格式输出,程序可以直接解析的json,不要附带其它字符")
    TokenStream ideaKeyMapTranslateStream(String input);
}
