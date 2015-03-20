package de.uhrenbastler.watchcheck.rss;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.gc.materialdesign.widgets.Dialog;
import com.pkmmte.pkrss.Article;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.uhrenbastler.watchcheck.R;

/**
 * Created by clorenz on 18.03.15.
 */
public class UhrenbastlerRssFeedDisplayDialog  {

    MaterialDialog rssDialog;

    public UhrenbastlerRssFeedDisplayDialog(Context context, List<Article> articles) {

        ListView articleListView = new ListView(context);
        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.rss_date_format));

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
        UhrenbastlerRssFeedDisplayAdapter articleAdapter = new UhrenbastlerRssFeedDisplayAdapter(context, articleList, R.layout.rssfeed_layout, fieldsFrom, fieldsTo);

        articleListView.setAdapter(articleAdapter);
        articleListView.setBackgroundColor(context.getResources().getColor(R.color.background_material_light));

        rssDialog = new MaterialDialog.Builder(context)
                .title(R.string.new_rss)
                .titleGravity(GravityEnum.CENTER)
                .theme(Theme.LIGHT)
                .adapter(articleAdapter)
                .positiveText(R.string.ok)
                .positiveColorRes(R.color.colorAccent)
                .iconRes(R.drawable.icon)
                .maxIconSize(50)
                .build();
        View title = rssDialog.getTitleFrame();
        title.setPadding(16,16,16,6);

        rssDialog.show();
    }

    public MaterialDialog getDialog() {
        return rssDialog;
    }
}
