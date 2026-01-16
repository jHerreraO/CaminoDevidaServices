package com.template.securityTemplate.config.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class CachedBodyServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream buffer;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.buffer = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        // no-op
    }

    @Override
    public int read() throws IOException {
        return buffer.read();
    }
}