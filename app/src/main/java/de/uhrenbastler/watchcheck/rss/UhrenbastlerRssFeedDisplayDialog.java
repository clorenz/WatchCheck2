package de.uhrenbastler.watchcheck.rss;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gc.materialdesign.widgets.Dialog;
import com.pkmmte.pkrss.Article;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 18.03.15.
 */
public class UhrenbastlerRssFeedDisplayDialog  {

    Dialog rssDialog;

    public UhrenbastlerRssFeedDisplayDialog(Context context, List<Article> articles) {
        rssDialog = new Dialog(context,"...",null);
        rssDialog.show();

        ListView articleListView = new ListView(context);

        List<HashMap<String,String>> articleList = new ArrayList<HashMap<String,String>>();
        for ( Article article : articles) {
            HashMap<String,String> hm = new HashMap<String,String>();
            //hm.put("image",article.getImage().toString());
            hm.put("title",article.getTitle());
            hm.put("body",article.getDescription());
            articleList.add(hm);
        }
        String[] fieldsFrom = new String[]{/*"image",*/"title","body"};
        int[] fieldsTo = new int[]{/*R.id.rss_image_article,*/ R.id.rss_headline_article, R.id.rss_body_article};
        SimpleAdapter articleAdapter = new SimpleAdapter(context, articleList, R.layout.rssfeed_layout, fieldsFrom, fieldsTo);

        articleListView.setAdapter(articleAdapter);
        articleListView.setBackgroundColor(context.getResources().getColor(R.color.background_material_light));

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(200,
                200);
        rssDialog.setContentView(articleListView, layoutParams);
    }

    public Dialog getDialog() {
        return rssDialog;
    }
}
