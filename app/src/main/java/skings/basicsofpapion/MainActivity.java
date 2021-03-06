package skings.basicsofpapion;

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Post> posts;
    private PostsAdapter adapter;
    private LinearLayoutManager llm;
    int oldItem = 0, newItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.text);
        RecyclerView posts_view = (RecyclerView) findViewById(R.id.posts_container);
        llm = new LinearLayoutManager(getApplicationContext());
        posts_view.setLayoutManager(llm);

        posts = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                posts.add(new Post(0, "https://www.tarafdari.com/sites/default/files/styles/slider/public/contents/user22475/news/c9sn4txxgaarnvy.jpg?itok=Ma4LP9Jg"));
            } else {

                posts.add(new Post(1, "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"));
            }
        }

        adapter = new PostsAdapter(posts, this);

        posts_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        posts_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
                textView.setText("" + llm.findFirstCompletelyVisibleItemPosition());
//                Log.i("salam",""+adapter.getItemViewType(llm.findFirstCompletelyVisibleItemPosition()));
                newItem = llm.findFirstVisibleItemPosition();
                if (oldItem != newItem && adapter.getItemViewType(newItem) == 1) {
                    Log.i("exoppp", "releasing");
                    adapter.bindViewHolder(recyclerView.findViewHolderForAdapterPosition(newItem), newItem);
//                    adapter.release();
                }
                oldItem = newItem;
//                adapter.release();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked(v);
            }
        });

    }

    public void clicked(View view) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getType() == 1) {
                LinearLayout ll = (LinearLayout) llm.findViewByPosition(i);
                if (ll != null) {
//                    SimpleExoPlayerView sep = (SimpleExoPlayerView) ll.findViewById(R.id.video_post_view);
//                    sep.getPlayer().stop();
                }
                Log.i("salam", ll + "");
            }
        }

    }
}
