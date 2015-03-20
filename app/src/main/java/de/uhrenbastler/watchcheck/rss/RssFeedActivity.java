package de.uhrenbastler.watchcheck.rss;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.pkmmte.pkrss.Article;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.uhrenbastler.watchcheck.R;
import de.uhrenbastler.watchcheck.WatchCheckActionBarActivity;

/**
 * Created by clorenz on 19.03.15.
 */
public class RssFeedActivity extends WatchCheckActionBarActivity implements AsyncRssResponse {

    private AsyncRssLoaderForActivity asyncRssLoaderForActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_rssfeed);
        setTitle(getString(R.string.new_rss));
        asyncRssLoaderForActivity = new AsyncRssLoaderForActivity(getApplicationContext(), this, 50);
        asyncRssLoaderForActivity.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void processFinish(List<Article> articles) {
        ListView articleListView = (ListView) findViewById(R.id.rssListView);
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.rss_date_format));

        List<HashMap<String,String>> articleList = new ArrayList<HashMap<String,String>>();
        for ( Article article : articles) {
            HashMap<String,String> hm = new HashMap<String,String>();
            hm.put("image",article.getImage().toString());
            hm.put("title",article.getTitle());
            hm.put("body",article.getDescription());
            hm.put("source",article.getSource().toString());
            hm.put("date", sdf.format(new Date(article.getDate())));
            articleList.add(hm);
        }
        String[] fieldsFrom = new String[]{"image","title","body","date"};
        int[] fieldsTo = new int[]{R.id.rss_image_article, R.id.rss_headline_article, R.id.rss_body_article, R.id.rss_date_article};


        // Maybe use the new introduced view here?
        UhrenbastlerRssFeedDisplayAdapter articleAdapter = new UhrenbastlerRssFeedDisplayAdapter(this, articleList, R.layout.rssfeed_layout, fieldsFrom, fieldsTo);

        articleListView.setAdapter(articleAdapter);
        articleListView.setBackgroundColor(this.getResources().getColor(R.color.background_material_light));
    }
}
