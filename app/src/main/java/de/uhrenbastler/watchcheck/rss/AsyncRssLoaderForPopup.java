package de.uhrenbastler.watchcheck.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.PkRSS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 18.03.15.
 */
public class AsyncRssLoaderForPopup extends AsyncTask<Context, Integer, List<Article>> implements Callback {

    public static final String LAST_RSS_TIMESTAMP = "last_rss_timestamp";
    public static final String LATEST_ARTICLE_TIMESTAMP = "latest_article_timestamp";
    private static final String LAST_SUCCESSFUL_RSS_TIMESTAMP = "last_successful_rss_timestamp";
    Context context;
    SharedPreferences sharedPref;
    AsyncRssResponse responseDelegate;
    List<Article> result = null;
    boolean loadIsInProgress=false;
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    int maxItems=0;

    public AsyncRssLoaderForPopup(Context context, AsyncRssResponse responseDelegate, int maxItems) {
        super();
        this.context = context;
        this.responseDelegate = responseDelegate;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.maxItems = maxItems;
    }

    @Override
    protected List<Article> doInBackground(Context... params) {
        // Check, if feature is enabled in settings. If not, exit!
        Boolean rssEnabled = sharedPref.getBoolean("pref_rss", false);
        if (!rssEnabled) {
            return null;
        }


        // Check, if RSS was fetched today. If yes, exit
        Long lastFetchTimestamp = sharedPref.getLong(LAST_RSS_TIMESTAMP, 0);
        String strToday = sdf.format(new Date());
        String strLastFetch = sdf.format(new Date(lastFetchTimestamp));
        if (strToday.equals(strLastFetch)) {
            Logger.info("Feed was already fetched today!");
            return null;
        }

        // Only fetch when on WLAN!
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork == null || !wifiNetwork.isConnected()) {
            Logger.debug("Not on a connected WIFI");
            return null;
        }


        // Feature is enabled, RSS was fetched long enough ago, so fetch now! And if successful, save timestamp in settings
        loadArticlesFromRSS(context.getString(R.string.rss_url));

        if ( result.isEmpty() ) {
            Long lastSuccessfulFetchTimestamp = sharedPref.getLong(LAST_SUCCESSFUL_RSS_TIMESTAMP, 0);
            if ( System.currentTimeMillis() - lastSuccessfulFetchTimestamp > 86400000L*7) {
                Logger.info("Last time, we had articles, was more than 7 days ago. Fetching from fallback");
                // Last successful fetch was more than one week ago! Load the fallback data now!
                loadArticlesFromRSS(context.getString(R.string.rss_url_alternative));
                Collections.sort(result, new ArticleByPubDateDescSorter());
            } else {
                Logger.info("Last time, we had articles, was less than 7 days ago.");
            }
        } else {
            // Set, that we had a successful fetch right now
            sharedPref.edit().putLong(LAST_SUCCESSFUL_RSS_TIMESTAMP, System.currentTimeMillis()).apply();
        }

        // In any case, we mark, that right now, we fetched the articles to avoid a re-fetch today
        sharedPref.edit().putLong(LAST_RSS_TIMESTAMP, System.currentTimeMillis()).apply();

        // The Callback retrieves all articles, whose pubDate is newer than the pubDate saved in the settings
        // and saves the latest pubDate in the settings
        return result;
    }

    private void loadArticlesFromRSS(String rssUrl) {
        loadIsInProgress=true;
        new PkRSS.Builder(context).parser(new UhrenbastlerParser()).build().load(rssUrl).callback(this).async();

        while ( loadIsInProgress ) {
            try {
                Logger.debug("Load is in progress");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(List<Article> articles) {
        responseDelegate.processFinish(articles);
    }

    @Override
    public void OnPreLoad() {
        loadIsInProgress=true;
    }

    @Override
    public void OnLoaded(List<Article> articles) {
        List<Article> newArticles = filterArticles(articles,sharedPref.getLong(LATEST_ARTICLE_TIMESTAMP,0),maxItems);

        if ( newArticles!=null && !newArticles.isEmpty()) {
            Collections.sort(newArticles, new ArticleByPubDateDescSorter());
            sharedPref.edit().putLong(LATEST_ARTICLE_TIMESTAMP, newArticles.get(0).getDate()).apply();
        }

        if ( result == null) {
            result = newArticles;
        } else {
            for ( Article article : newArticles ) {
                boolean yetFound=false;
                for ( Article existingArticle: result ) {
                    if (article.getSource().equals(existingArticle.getSource())) {
                        yetFound = true;
                        break;
                    }
                }
                if ( !yetFound ) {
                    result.add(article);
                }
            }
        }
        loadIsInProgress=false;
    }

    private List<Article> filterArticles(List<Article> articles, long latestArticleTimestamp, int maxArticles) {
        List<Article> ret = new ArrayList<Article>();
        for ( Article article  : articles ) {
            if ( article.getDate() > latestArticleTimestamp ) {
                ret.add(article);
                if ( ret.size() >= maxArticles ) {
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public void OnLoadFailed() {
        Logger.error("Could not load RSS feed");
    }

    /**
     * Sort by latest articles first
     */
    private class ArticleByPubDateDescSorter implements java.util.Comparator<Article> {
        @Override
        public int compare(Article lhs, Article rhs) {
            return new Long(rhs.getDate()).compareTo(new Long(lhs.getDate()));
        }
    }
}
