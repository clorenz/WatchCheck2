package de.uhrenbastler.watchcheck.rss;

import com.pkmmte.pkrss.Article;

import java.util.List;

/**
 * Created by clorenz on 18.03.15.
 */
public interface AsyncRssResponse {

    void processFinish(List<Article> articles);
}
