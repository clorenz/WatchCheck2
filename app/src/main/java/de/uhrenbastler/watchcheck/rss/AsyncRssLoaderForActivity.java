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
public class AsyncRssLoaderForActivity extends AsyncTask<Context, Integer, List<Article>> implements Callback {

    Context context;
    SharedPreferences sharedPref;
    AsyncRssResponse responseDelegate;
    List<Article> result = null;
    boolean loadIsInProgress=false;
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    int maxItems=0;

    public AsyncRssLoaderForActivity(Context context, AsyncRssResponse responseDelegate, int maxItems) {
        super();
        this.context = context;
        this.responseDelegate = responseDelegate;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.maxItems = maxItems;
    }

    @Override
    protected List<Article> doInBackground(Context... params) {
        // Fetch from both URLs and mix the data
        loadArticlesFromRSS(context.getString(R.string.rss_url));
        loadArticlesFromRSS(context.getString(R.string.rss_url_alternative));
        Collections.sort(result, new ArticleByPubDateDescSorter());

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
        List<Article> newArticles = filterArticles(articles,maxItems);

        if ( newArticles!=null && !newArticles.isEmpty()) {
            Collections.sort(newArticles, new ArticleByPubDateDescSorter());
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

    private List<Article> filterArticles(List<Article> articles, int maxArticles) {
        List<Article> ret = new ArrayList<Article>();
        for ( Article article  : articles ) {
            ret.add(article);
            if ( ret.size() >= maxArticles ) {
                break;
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
