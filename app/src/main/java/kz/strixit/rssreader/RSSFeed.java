package kz.strixit.rssreader;

import java.util.List;

public class RSSFeed {

    private String _title;
    private String _description;
    private String _link;
    private String _rss_link;
    private String _language;
    private List<RSSItem> _items;

    public RSSFeed(String title, String description, String link, String rss_link, String language) {

        this._title = title;
        this._description = description;
        this._link = link;
        this._rss_link = rss_link;
        this._language = language;
    }

    public void setItems(List<RSSItem> items) {
        this._items = items;
    }

    public List<RSSItem> getItems() {
        return this._items;
    }

    public String getTitle() {
        return this._title;
    }

    public String getDescription() {
        return this._description;
    }

    public String getLink() {
        return this._link;
    }

    public String getRSSLink() {
        return this._rss_link;
    }

    public String getLanguage() {
        return this._language;
    }
}
