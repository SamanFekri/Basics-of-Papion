package skings.basicsofpapion;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;


/**
 * Created by SKings (samanf74@gmail.com) on 4/13/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
