package de.uhrenbastler.watchcheck.rss;

import android.net.Uri;
import android.text.Html;

import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.parser.Parser;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 16.01.16.
 */
public class UhrwerksarchivSAXParser extends Parser {

    List<Article> articles = new ArrayList<>();
    private final DateFormat dateFormat;
    private final Pattern imgPattern = Pattern.compile("^.*?<img src=\"(.*?)\".*");

    public UhrwerksarchivSAXParser() {
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        dateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    @Override
    public List<Article> parse(String rssStream) {
        // Hack to fix broken Joomla RSS feed
        rssStream = fixBrokenJoomlaRSSData(rssStream);

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            XMLReader xr = sp.getXMLReader();

            xr.setContentHandler(new UhrwerksArchivRssHandler());

            xr.parse(new InputSource(new ByteArrayInputStream(rssStream.getBytes())));
        } catch ( ParserConfigurationException | SAXException | IOException e) {
            Logger.error("Cannot parse String "+rssStream);
            Logger.error("Cause : "+e,e);
        }

        return articles;
    }

    private class UhrwerksArchivRssHandler extends DefaultHandler {

        Article article;
        StringBuilder sb;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("item".equals(qName)) {
                article = new Article();
            } else if ( "link".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "title".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "description".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "description".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "content:encoded".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "wfw:commentRss".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "category".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "dc:creator".equals(qName)) {
                sb = new StringBuilder();
            } else if ( "pubDate".equals(qName)) {
                sb = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ( article!=null && "link".equals(qName)) {
                article.setSource(Uri.parse(sb.toString()));
            } else if ( article!=null && "title".equals(qName)) {
                article.setTitle(sb.toString());
            } else if ( article!=null && "description".equals(qName)) {
                String encoded = sb.toString();
                String imageLink = extractImageLink(encoded);
                if ( imageLink!=null ) {
                    article.setImage(Uri.parse(imageLink));
                }
                String description = Html.fromHtml(encoded.replaceAll("<img.+?>", "")).toString();
                description = StringUtils.abbreviate(description.replaceAll("\\n\\s*\\n", "\n").replaceAll("\\s\\s+", " ").trim(), 150);
                article.setDescription(description);
            } else if ( article!=null && "content:encoded".equals(qName)) {
                article.setContent(sb.toString().replaceAll("[<](/)?div[^>]*[>]", ""));
            } else if ( article!=null && "wfw:commentRss".equals(qName)) {
                article.setComments(sb.toString());
            } else if ( article!=null && "category".equals(qName)) {
                article.setNewTag(sb.toString());
            } else if ( article!=null && "dc:creator".equals(qName)) {
                article.setAuthor(sb.toString());
            } else if ( article!=null && "pubDate".equals(qName)) {
                article.setDate(getParsedDate(sb.toString()));
            } else if ( article!=null && "item".equals(qName)) {
                articles.add(article);
                article=null;
            }

            sb=null;
        }


        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (sb != null)  {
                sb.append(ch, start, length);
            }
        }
    }


    private String fixBrokenJoomlaRSSData(String rssStream) {
        return rssStream.replaceAll("<atom.*?/>","")
                        .replaceAll("<guid.*?</guid>","");
    }

    /**
     * Converts a date in the "EEE, d MMM yyyy HH:mm:ss Z" format to a long value.
     * @param encodedDate The encoded date which to convert.
     * @return A long value for the passed date String or 0 if improperly parsed.
     */
    private long getParsedDate(String encodedDate) {
        try {
            return dateFormat.parse(dateFormat.format(dateFormat.parseObject(encodedDate))).getTime();
        }
        catch (ParseException e) {
            Logger.warn("Error parsing date " + encodedDate);
            e.printStackTrace();
            return 0;
        }
    }


    private String extractImageLink(String src) {
        Matcher m = imgPattern.matcher(src);

        if ( m.find()) {
            return m.group(1);
        } else {
            Logger.warn("No image match for "+src);
        }

        return null;
    }
}
