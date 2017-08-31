package org.reactome.server.service.utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Generates the header and the footer every MINUTES defined below.
 * The header.jsp and footer.jsp are placed under jsp folder in WEB-INF
 * <p>
 * IMPORTANT
 * ---------
 * We assume the war file runs exploded, because there is no way of writing
 * a file in a none-exploded war and the jsp template needs the templates
 * to be in the defined resources to parse the content (and this is used
 * to keep the species and other filtering options)
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class HeaderFooterCacher extends Thread {

    private static Logger logger = Logger.getLogger(HeaderFooterCacher.class.getName());

    private static final String TITLE_OPEN = "<title>";
    private static final String TITLE_CLOSE = "</title>";
    private static final String TITLE_REPLACE = "<title>Reactome | Content Service API</title>";

    private static final String HEADER_CLOSE = "</head>";
    private static final String HEADER_CLOSE_REPLACE = "<jsp:include page=\"additional.jsp\"/>\n</head>";

    // Name of the template page in Joomla
    private static final String TEMPLATE_PAGE = "template-swagger";

    private static final Integer MINUTES = 15;

    private final String server;

    @Autowired
    public HeaderFooterCacher(@Value("${template.server}") String server) {
        this.server = server;
        start();
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            getHeaderAndFooter(getTemplate());
            try {
                Thread.sleep(1000 * 60 * MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void writeFile(String fileName, String content) {
        try {
            //noinspection ConstantConditions
            String path = getClass().getClassLoader().getResource("").getPath();
            //HACK!
            if (path.contains("WEB-INF")) {
                //When executing in a deployed war file in tomcat, the WEB-INF folder is just one bellow the classes
                path += "../pages/";
            } else {
                //When executing in local we need to write the files in the actual resources
                path += "../../src/main/webapp/WEB-INF/pages/";
            }
            String file = path + fileName;
            FileOutputStream out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();
            logger.debug(file + " updated successfully");
        } catch (NullPointerException | IOException e) {
            logger.error("Error updating " + fileName, e);
        }
    }

    private String getTemplate() {
        try {
            URL url = new URL(this.server + TEMPLATE_PAGE);
            String rtn = IOUtils.toString(url.openConnection().getInputStream());

            rtn = getReplaced(rtn, TITLE_OPEN, TITLE_CLOSE, TITLE_REPLACE);
            rtn = getReplaced(rtn, HEADER_CLOSE, HEADER_CLOSE, HEADER_CLOSE_REPLACE);

            rtn = rtn.replaceFirst("<base.*/>", "");
            rtn = rtn.replaceAll("(http|https)://", "//");

            // remove joomla template default class
            rtn = rtn.replaceAll("favth-content-block", "");

            return rtn;
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("<span style='color:red'>%s</span>", e.getMessage());
        }
    }

    private void getHeaderAndFooter(String file) {
        if (file != null && !file.isEmpty()) {
            String[] parts = StringUtils.split(file, "<!-- template-placeholder -->");
            writeFile("header.jsp", parts[0]);
            writeFile("footer.jsp", parts[1]);
        }
    }

    private String getReplaced(String target, String open, String close, String replace) {
        try {
            String pre = target.substring(0, target.indexOf(open));
            String suf = target.substring(target.indexOf(close) + close.length(), target.length());
            return pre + replace + suf;
        } catch (StringIndexOutOfBoundsException e) {
            return target;
        }
    }
}
