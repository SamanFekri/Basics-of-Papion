package skings.basicsofpapion;

import android.net.Uri;

import java.net.URI;

/**
 * Created by SKings (samanf74@gmail.com) on 4/13/2017.
 */

public class Post {
    private int type;
    private String url;

    /**
     * type = 0 image
     * type = 1 video
     * @param type
     * @param url
     */
    public Post(int type, String url) {
        this.type = type;
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Uri getUri(){
        return Uri.parse(url);
    }
}
