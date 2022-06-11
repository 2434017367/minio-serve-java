package com.example.minio.common.xss;


import cn.hutool.http.HtmlUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 *
 * @ClassName:  XssStringJsonSerializer
 * @Description:(实现过滤json类型)
 *
 */
public class XssStringJsonSerializer extends JsonSerializer<String> {

    @Override
    public Class<String> handledType() {
        return String.class;
    }

    /**
     * 假如有有html 代码是自己传来的  需要设定对应的name 不走StringEscapeUtils.escapeHtml4(value) 过滤
     */
    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (value != null) {
            //String encodedValue = StringEscapeUtils.escapeHtml4(value);
            String encodedValue = HtmlUtil.escape(value);
            jsonGenerator.writeString(encodedValue);
        }
    }

}

