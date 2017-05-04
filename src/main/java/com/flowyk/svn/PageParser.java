package com.flowyk.svn;

import com.google.common.io.Files;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

public class PageParser {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void parse(String baseFolder, String basicAuth) {
        parse(baseFolder, "", basicAuth);
    }

    private void parse(String baseFolder, String interPath, String basicAuth) {
        try {
            String address = URLDecoder.decode(baseFolder + interPath, "UTF-8");
            Document doc = Jsoup
                    .connect(address)
                    .header("Authorization", basicAuth)
                    .get();
            Elements links = doc.select("a");
            for (Element link : links) {
                String reference = link.attr("href");
//                String reference = URLDecoder.decode(href, "UTF-8");
                if (isBack(reference) || isForbidden(reference)) {
                    continue;
                } else if (isFile(reference)) {
                    File file = new File(URLDecoder.decode(interPath + reference, "UTF-8"));
                    if (file.exists()) {
                        continue;
                    }
                    Files.createParentDirs(file);
                    new Downloader().download(file, new URL(baseFolder + interPath + reference), basicAuth);
                } else if (isFolder(reference)) {
                    logger.debug("continuing to page: {}", interPath + reference);
                    new PageParser().parse(baseFolder, interPath + reference, basicAuth);
                } else {
                    logger.warn("unhandled url {}", reference);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isBack(String reference) {
        return "../".equals(reference);
    }

    public boolean isFile(String reference) {
        return !isFolder(reference);
    }

    public boolean isFolder(String reference) {
        return reference.endsWith("/");
    }

    private boolean isForbidden(String reference) {
        return reference.contains("tags") || reference.contains("branches");
    }

}
