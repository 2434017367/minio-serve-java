package com.example.minio.common.xss;

import com.example.minio.common.utils.RequestUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/4/17 19:07
 *
 * request包装类用于可以多次读取inputStream的内容
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {
    //保存流
    private byte[] requestBody = null;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        boolean isJson = RequestUtils.isRequestContentTypeJson(request);
        if (isJson) {
            requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (requestBody == null) {
            return super.getInputStream();
        } else {
            final ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
            return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public byte[] getRequestBody() {
        return requestBody;
    }

}
