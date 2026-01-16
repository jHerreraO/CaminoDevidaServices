package com.template.securityTemplate.config.filter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

// clase para leer el cuerpo de solicitud más de una vez con la finalidad de escribirlo sobre el log.

@Getter
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private final boolean isMultipart;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.isMultipart = isMultipart(request);
        if (!this.isMultipart) {
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
        } else {
            this.cachedBody = new byte[0]; // Initialize with an empty byte array for multipart requests
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.isMultipart) {
            return super.getInputStream();
        }
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.isMultipart) {
            return super.getReader();
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    private boolean isMultipart(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }
}
