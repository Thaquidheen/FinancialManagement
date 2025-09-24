package com.company.erp.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

@Component
public class ResponseCompressionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String acceptEncoding = httpRequest.getHeader("Accept-Encoding");

        // Check if client supports GZIP compression
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            // Check if response is compressible (JSON, HTML, CSS, JS)
            String contentType = httpResponse.getContentType();
            if (isCompressible(contentType)) {
                GZIPResponseWrapper gzipResponse = new GZIPResponseWrapper(httpResponse);
                chain.doFilter(request, gzipResponse);
                gzipResponse.close();
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isCompressible(String contentType) {
        if (contentType == null) return false;

        return contentType.contains("application/json") ||
                contentType.contains("text/html") ||
                contentType.contains("text/css") ||
                contentType.contains("application/javascript") ||
                contentType.contains("text/xml");
    }

    // GZIP Response Wrapper implementation
    private static class GZIPResponseWrapper extends HttpServletResponseWrapper {
        private GZIPServletOutputStream gzipOutputStream;
        private PrintWriter printWriter;

        public GZIPResponseWrapper(HttpServletResponse response) {
            super(response);
            response.setHeader("Content-Encoding", "gzip");
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (gzipOutputStream == null) {
                gzipOutputStream = new GZIPServletOutputStream(getResponse().getOutputStream());
            }
            return gzipOutputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (printWriter == null) {
                printWriter = new PrintWriter(getOutputStream());
            }
            return printWriter;
        }

        public void close() throws IOException {
            if (printWriter != null) {
                printWriter.close();
            }
            if (gzipOutputStream != null) {
                gzipOutputStream.close();
            }
        }
    }

    private static class GZIPServletOutputStream extends ServletOutputStream {
        private GZIPOutputStream gzipOutputStream;

        public GZIPServletOutputStream(ServletOutputStream servletOutputStream) throws IOException {
            this.gzipOutputStream = new GZIPOutputStream(servletOutputStream);
        }

        @Override
        public void write(int b) throws IOException {
            gzipOutputStream.write(b);
        }

        @Override
        public void close() throws IOException {
            gzipOutputStream.close();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // Not implemented for this simple example
        }
    }
}
