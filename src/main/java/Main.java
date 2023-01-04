import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static String domain = "https://www.nikoartgallery.com/";
    public static List<String> printUrls = new ArrayList<>();

    public static void main(String[] args) {
        Logger logger = LogManager.getRootLogger();
        Node root = new Node(domain, domain);
        SiteMapCrawler.getUniqueUrls().add(domain);
        ForkJoinPool pool = new ForkJoinPool();
        SiteMapCrawler siteMapCrawler = new SiteMapCrawler(root);
        pool.invoke(siteMapCrawler);
        for (int i = 0; i <= SiteMapCrawler.getMaxLevel(); i++){
            int level = i;
            SiteMapCrawler.getUniqueUrls().forEach(url ->{
                if(getLevel(url) == level){
                    addPrintUrls(url);
                }
            });
        }
        Collections.reverse(printUrls);
        printUrls.forEach(System.out::println);

        try {
            Files.write(Paths.get("Data/file.txt"), printUrls);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    public static void addPrintUrls(String url) {
        for (int i = 0; i < printUrls.size(); i++) {
            String checkedUrl = printUrls.get(i).trim();
            if (url.contains(checkedUrl)) {
                printUrls.add(i, "\t".repeat(getLevel(url) - 1).concat(url));
                return;
            }
        }
        if (!ifPresent(url)) {
            printUrls.add("\t".repeat(getLevel(url) - 1).concat(url));
        }
    }

    public static Boolean ifPresent(String url) {
        return printUrls.stream().anyMatch(s -> s.equals(url.trim()));
    }

    public static int getLevel(String url) {
        return url.replaceAll("[^/]", "").length() - 2;
    }

}