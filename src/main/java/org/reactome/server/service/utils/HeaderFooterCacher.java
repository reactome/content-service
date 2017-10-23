package org.reactome.server.service.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HeaderFooterCacher extends Thread {

    private static Logger logger = LoggerFactory.getLogger("threadLogger");

    private static final String TITLE_OPEM = "<title>";
    private static final String TITLE_CLOSE = "</title>";
    private static final String TITLE_REPLACE = "<title>Reactome | Content Service API</title>";

    private static final String HEADER_CLOSE = "</head>";
    private static final String HEADER_CLOSE_REPLACE = "<jsp:include page=\"script.jsp\"/>\n</head>";

    private static final Integer MINUTES = 15;

    private String server;
    private boolean active = true;

    public HeaderFooterCacher(String server) {
        super("CS-HeaderFooter");
        if (server != null && !server.isEmpty()) {
            this.server = server;
            logger.debug("Thread to keep the header/footer updated started");
            start();
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        try {
            while (active) {
                writeFile("header.jsp", getHeader());
                writeFile("footer.jsp", getFooter());
                if (active) Thread.sleep(1000 * 60 * MINUTES);
            }
        } catch (InterruptedException e) {
            logger.warn("The header/footer updater has been stop for the content-service");
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
            logger.debug(file + " updated succesfully");
        } catch (Exception e) {
            logger.error("Error updating " + fileName, e);
            interrupt();
        }
    }

    private String getHeader() {
        try {
            URL url = new URL(this.server + "common/header.php");
            String rtn = IOUtils.toString(url.openConnection().getInputStream());
            rtn = getReplaced(rtn, TITLE_OPEM, TITLE_CLOSE, TITLE_REPLACE);
            rtn = getReplaced(rtn, HEADER_CLOSE, HEADER_CLOSE, HEADER_CLOSE_REPLACE);
            rtn = rtn.replaceAll("(http|https)://", "//");
            return rtn;
        } catch (IOException e) {
            return String.format("<span style='color:red'>%s</span>", e.getMessage());
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

    private String getFooter() {
        try {
            URL url = new URL(this.server + "common/footer.php");
            String rtn = IOUtils.toString(url.openConnection().getInputStream());
            rtn = rtn.replaceAll("(http|https)://", "//");
            return rtn;
        } catch (IOException e) {
            return String.format("<span style='color:red'>%s</span>", e.getMessage());
        }
    }

    @Override
    public void interrupt() {
        this.active = false;
        super.interrupt();
        logger.info("Content-Service HeaderFooterCacher stopped");
    }
}
