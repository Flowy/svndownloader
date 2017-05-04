package com.flowyk.svn;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Downloader {
    public void download(File file, URL source, String basicAuth) {
        URLConnection connection;
        try {
            connection = source.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connection.setRequestProperty("Authorization", basicAuth);

        try (InputStream inputStream = connection.getInputStream();
             ReadableByteChannel channel = Channels.newChannel(inputStream);
             FileOutputStream fileOutput = new FileOutputStream(file)) {
            long length = fileOutput.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            LoggerFactory.getLogger(this.getClass()).debug("downloaded file: {} size: {}", file, length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
