import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class SiteMapCrawler extends RecursiveTask<Node> {
    private final Node node;
    private static final Set<String> uniqueUrls = new TreeSet<>();
    private static int maxLevel;
    private static final Logger logger = LogManager.getLogger(SiteMapCrawler.class);
    private final List<SiteMapCrawler> tasks = new ArrayList<>();

    public SiteMapCrawler(Node node) {
        this.node = node;
    }

    public List<SiteMapCrawler> getNewUrlsForTasks() {
        try {
            Thread.sleep(300);
            String rootUrl = node.getUrlToCrawl();
            String domain = node.getDomain();
            Document document = Jsoup.connect(rootUrl).ignoreHttpErrors(true).get();
            Elements elements = document.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (childUrl.contains(domain) && !isUniqueUrl(childUrl) && childUrl.endsWith("/")) {
                    if (!childUrl.matches("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php)$") && !childUrl.contains("?") && !childUrl.contains("#")) {
                        synchronized (uniqueUrls) {
                            uniqueUrls.add(childUrl);
                            if (maxLevel < getLevel(childUrl)){
                                setMaxLevel(getLevel(childUrl));
                            }
                        }
                        Node child = new Node(childUrl, domain);
                        SiteMapCrawler siteMapCrawler = new SiteMapCrawler(child);
                        siteMapCrawler.fork();
                        tasks.add(siteMapCrawler);
                        System.out.println("in progress");
                    }
                }
            });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return tasks;
    }

    public static boolean isUniqueUrl(String url) {
        return getUniqueUrls().contains(url);
    }

    public static Set<String> getUniqueUrls() {
        return uniqueUrls;
    }

    public static int getLevel(String url) {
        return url.replaceAll("[^/]", "").length() - 2;
    }

    public static int getMaxLevel() {
        return maxLevel;
    }

    public static void setMaxLevel(int maxLevel) {
        SiteMapCrawler.maxLevel = maxLevel;
    }

    @Override
    protected Node compute() {
        List<SiteMapCrawler> allTasks = getNewUrlsForTasks();
        for (SiteMapCrawler task : allTasks) {
            task.join();
        }
        return node;
    }
}
