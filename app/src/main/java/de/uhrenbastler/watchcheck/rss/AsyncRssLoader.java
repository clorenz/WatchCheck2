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

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.SettingsActivity;
import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 18.03.15.
 */
public class AsyncRssLoader extends AsyncTask<Context, Integer, List<Article>> implements Callback {

    public static final String LAST_RSS_TIMESTAMP = "last_rss_timestamp";
    public static final String LATEST_ARTICLE_TIMESTAMP = "latest_article_timestamp";
    Context context;
    SharedPreferences sharedPref;
    AsyncRssResponse responseDelegate;
    List<Article> result = null;
    boolean loadIsInProgress=false;
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public AsyncRssLoader(Context context, AsyncRssResponse responseDelegate) {
        super();
        this.context = context;
        this.responseDelegate = responseDelegate;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected List<Article> doInBackground(Context... params) {
        // BEGIN FAKE
        sharedPref.edit().putLong(LAST_RSS_TIMESTAMP,0).putLong(LATEST_ARTICLE_TIMESTAMP,0).apply();
        // END FAKE
        
        
        // Check, if feature is enabled in settings. If not, exit!
        Boolean rssEnabled = sharedPref.getBoolean("pref_rss", false);
        if ( !rssEnabled ) {
            return null;
        }

        // Check, if RSS was fetched today. If yes, exit
        Long lastFetchTimestamp = sharedPref.getLong(LAST_RSS_TIMESTAMP, 0);
        String strToday = sdf.format(new Date());
        String strLastFetch = sdf.format(new Date(lastFetchTimestamp));
        if ( strToday.equals(strLastFetch)) {
            Logger.info("Feed was already fetched today!");
            return null;
        }

        // Only fetch when on WLAN!
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork == null || !wifiNetwork.isConnected() ) {
            Logger.debug("Not on a connected WIFI");
        }

        // Feature is enabled, RSS was fetched long enough ago, so fetch now! And if successful, save timestamp in settings
        loadIsInProgress=true;
        new PkRSS.Builder(context).parser(new UhrenbastlerParser()).build().load(context.getString(R.string.rss_url)).callback(this).async();

        while ( loadIsInProgress ) {
            try {
                Logger.debug("Load is in progress");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // The Callback retrieves all articles, whose pubDate is newer than the pubDate saved in the settings
        // and saves the latest pubDate in the settings
        return result;
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
        if ( articles!=null && !articles.isEmpty()) {
            sharedPref.edit().putLong(LAST_RSS_TIMESTAMP, System.currentTimeMillis()).apply();
        }

        List<Article> newArticles = filterArticles(articles, sharedPref.getLong(LATEST_ARTICLE_TIMESTAMP,0));

        if ( newArticles!=null && !newArticles.isEmpty()) {
            Collections.sort(newArticles, new ArticleByPubDateSorter());
            sharedPref.edit().putLong(LATEST_ARTICLE_TIMESTAMP, newArticles.get(0).getDate()).apply();
        }

        result = newArticles;
        loadIsInProgress=false;
    }

    private List<Article> filterArticles(List<Article> articles, long latestArticleTimestamp) {
        List<Article> ret = new ArrayList<Article>();
        for ( Article article  : articles ) {
            if ( article.getDate() > latestArticleTimestamp ) {
                ret.add(article);
            }
        }
        return ret;
    }

    @Override
    public void OnLoadFailed() {
        Logger.error("Could not load RSS feed");
    }

    private class ArticleByPubDateSorter implements java.util.Comparator<Article> {
        @Override
        public int compare(Article lhs, Article rhs) {
            return new Long(lhs.getDate()).compareTo(new Long(rhs.getDate()));
        }
    }
}
