package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Adapter for subscriptions list.
 * 
 * @author bgamard
 */
public class SubscriptionAdapter extends BaseAdapter {

    /**
     * Items in list.
     */
    private List<SubscriptionItem> items = new ArrayList<SubscriptionItem>();

    /**
     * Context.
     */
    private Context context;
    
    /**
     * Header item type.
     */
    private static final int HEADER_ITEM = 0;
    
    /**
     * Category item type.
     */
    private static final int CATEGORY_ITEM = 1;
    
    /**
     * Subscription item type.
     */
    private static final int SUBSCRIPTION_ITEM = 2;
    
    /**
     * Auth token used to download favicons.
     */
    private String authToken;
    
    /**
     * AQuery.
     */
    private AQuery aq;
    
    /**
     * Constructor.
     * @param context
     * @param input
     */
    public SubscriptionAdapter(Context context, JSONObject input) {
        this.context = context;
        this.aq = new AQuery(context);
        this.authToken = PreferenceUtil.getAuthToken(context);
        SubscriptionItem item = null;
        
        // Adding fixed items
        item = new SubscriptionItem();
        item.type = HEADER_ITEM;
        item.title = context.getString(R.string.latest);
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.unread);
        item.url = "/all";
        item.unread = true;
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.all);
        item.url = "/all";
        item.unread = false;
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = SUBSCRIPTION_ITEM;
        item.title = context.getString(R.string.starred);
        item.url = "/starred";
        items.add(item);
        
        item = new SubscriptionItem();
        item.type = HEADER_ITEM;
        item.title = context.getString(R.string.subscriptions);
        items.add(item);
        
        // Adding categories and subscriptions
        JSONObject rootCategory = input.optJSONArray("categories").optJSONObject(0);
        JSONArray categories = rootCategory.optJSONArray("categories");
        for (int i = 0; i < categories.length(); i++) {
            JSONObject category = categories.optJSONObject(i);

            item = new SubscriptionItem();
            item.type = CATEGORY_ITEM;
            item.id = category.optString("id");
            item.title = category.optString("name");
            item.url = "/category/" + item.id;
            items.add(item);
            
            JSONArray subscriptions = category.optJSONArray("subscriptions");
            for (int j = 0; j < subscriptions.length(); j++) {
                JSONObject subscription = subscriptions.optJSONObject(j);

                item = new SubscriptionItem();
                item.type = SUBSCRIPTION_ITEM;
                item.id = subscription.optString("id");
                item.title = subscription.optString("title");
                item.url = "/subscription/" + item.id;
                items.add(item);
            }
        }
        
        // Root subscriptions
        JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
        for (int j = 0; j < subscriptions.length(); j++) {
            JSONObject subscription = subscriptions.optJSONObject(j);

            item = new SubscriptionItem();
            item.type = SUBSCRIPTION_ITEM;
            item.id = subscription.optString("id");
            item.title = subscription.optString("title");
            item.url = "/subscription/" + item.id;
            item.root = true;
            items.add(item);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        SubscriptionItem item = getItem(position);
        
        // Inflating the right layout
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout = R.layout.drawer_list_item_header;
            if (item.type == SUBSCRIPTION_ITEM) layout = R.layout.drawer_list_item_subscription;
            if (item.type == CATEGORY_ITEM) layout = R.layout.drawer_list_item_category;
            view = vi.inflate(layout, null);
        }
        
        // Recycling AQuery
        aq.recycle(view);
        
        // Type specific layout data
        switch (item.type) {
        case HEADER_ITEM:
            break;
        case SUBSCRIPTION_ITEM:
            if (item.id != null) {
                String faviconUrl = Constants.READER_API_URL + "/subscription/" + item.id + "/favicon";
                Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
                aq.id(R.id.imgFavicon)
                    .image(new BitmapAjaxCallback()
                        .url(faviconUrl)
                        .fallback(R.drawable.ic_launcher)
                        .preset(placeHolder)
                        .animation(AQuery.FADE_IN_NETWORK)
                        .cookie("auth_token", authToken))
                    .margin(item.root ? 16 : 32, 0, 0, 0);
            } else {
                aq.id(R.id.imgFavicon).image(0);
            }
            break;
        case CATEGORY_ITEM:
            break;
        }
        
        // Common layout data
        aq.id(R.id.content).text(item.title);
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }
    
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    
    @Override
    public SubscriptionItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean isEnabled(int position) {
        int type = getItem(position).type;
        return type == SUBSCRIPTION_ITEM || type == CATEGORY_ITEM;
    }
    
    /**
     * Item in subscription list.
     * 
     * @author bgamard
     */
    public class SubscriptionItem {
        
        private int type;
        private String id;
        private String title;
        private String url;
        private boolean unread = false;
        private boolean root = false;
        
        /**
         * Getter of url.
         * @return url
         */
        public String getUrl() {
            return url;
        }
        
        /**
         * Getter of unread.
         * @return unread
         */
        public boolean isUnread() {
            return unread;
        }
    }
}
