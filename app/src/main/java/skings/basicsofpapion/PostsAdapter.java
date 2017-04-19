package skings.basicsofpapion;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.List;


public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Post> postsList;
    private int currentPlaying = -1;
    private String userAgent;
    private Handler mainHandler;
    private TrackSelection.Factory videoTrackSelectionFactory;
    private TrackSelector trackSelector;
    private LoadControl loadControl;
    private final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private SimpleExoPlayer player;
    private Uri uri;
    private View view;
    private boolean flag=false;
    private DataSource.Factory dataSourceFactory;
    private MediaSource videoSource;

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mainImage;

        public ImageViewHolder(View view) {
            super(view);
            mainImage = (SimpleDraweeView) view.findViewById(R.id.image_post_view);

        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private SimpleExoPlayerView mainVideo;


        public VideoViewHolder(View view) {
            super(view);
            this.view = view;

            mainVideo = (SimpleExoPlayerView) view.findViewById(R.id.video_post_view);
//            userAgent = Util.getUserAgent(view.getContext(),"Basic of papion");
//            createPlayer();
//            attachPlayerView();

        }

        public View getView() {
            return view;
        }

//        public void preparePlayer(String url){
////            player.setVideoSource(url);
//        }
//         Create TrackSelection Factory, Track Selector, Handler, Load Control, and ExoPlayer Instance
//        public void createPlayer(){
//            mainHandler = new Handler();
//            videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//            loadControl = new DefaultLoadControl();
//            player = ExoPlayerFactory.newSimpleInstance(view.getContext(),trackSelector,loadControl);
//        }

//        public void attachPlayerView(){
//            mainVideo.setPlayer(player);
//        }

        int x = 0;
        // Build Data Source Factory, Dash Media Source, and Prepare player using videoSource
        public void preparePlayer(Uri link){
            uri = link;
            dataSourceFactory = buildDataSourceFactory(bandwidthMeter);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            videoSource = new ExtractorMediaSource(uri,
                    dataSourceFactory, extractorsFactory, mainHandler, null);

            LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

//            videoSource = new DashMediaSource(uri,buildDataSourceFactory(null),new DefaultDashChunkSource.Factory(dataSourceFactory),mainHandler,null);
            player.prepare(loopingSource);

        }

        // Build Data Source Factory using DefaultBandwidthMeter and HttpDataSource.Factory
        private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter){
            return new DefaultDataSourceFactory(view.getContext(), bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
        }

        // Build Http Data Source Factory using DefaultBandwidthMeter
        private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter){
            return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
        }
    }

    public PostsAdapter(List<Post> postsList) {

        this.postsList = postsList;
        mainHandler = new Handler();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType){
            case 0:
                itemView =  LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_image_post, parent, false);
                return new ImageViewHolder(itemView);
            case 1:
                itemView =  LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_video_post, parent, false);
                Log.i("creating",""+viewType);
                return new VideoViewHolder(itemView);
            default:
                itemView =  LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_image_post, parent, false);
                Log.i("creating",""+viewType);
                return new ImageViewHolder(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return postsList.get(position).getType();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Post post = postsList.get(position);
        Log.i("bindbind",""+position);

        switch (post.getType()){
            case 0:
                ((ImageViewHolder) holder).mainImage.setImageURI(post.getUri());
                break;
            case 1:
                view=((VideoViewHolder)holder).getView();
//                ((VideoViewHolder) holder).preparePlayer(post.getUri());
                userAgent = Util.getUserAgent(view.getContext(),"Basic of papion");

                if (player!=null){
                    player.stop();
                }

                player = ExoPlayerFactory.newSimpleInstance(view.getContext(),trackSelector,loadControl);
                uri = post.getUri();
                dataSourceFactory =
                new DefaultDataSourceFactory(view.getContext(), bandwidthMeter,new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                videoSource = new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, mainHandler, null);

                LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

//            videoSource = new DashMediaSource(uri,buildDataSourceFactory(null),new DefaultDashChunkSource.Factory(dataSourceFactory),mainHandler,null);
                player.setVideoTextureView(new TextureView(view.getContext()));
                player.prepare(loopingSource);

                Log.i("surfacetex",""+((VideoViewHolder) holder).mainVideo.getVideoSurfaceView());
                ((VideoViewHolder) holder).mainVideo.setPlayer(player);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

}