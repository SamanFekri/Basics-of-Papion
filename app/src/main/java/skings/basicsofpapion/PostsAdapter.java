package skings.basicsofpapion;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
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
    private Activity activity;
    private int currentPlaying = -1;
    private String userAgent;
    private Handler mainHandler;
    private TrackSelectionHelper trackSelectionHelper;
    private TrackSelection.Factory videoTrackSelectionFactory;
    private DefaultTrackSelector trackSelector;
    private LoadControl loadControl;
    private final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private SimpleExoPlayer player;
    private Uri uri;
    private View view;
    private boolean flag=false;
    private DataSource.Factory dataSourceFactory;
    private MediaSource videoSource;
    private TrackGroupArray lastSeenTrackGroupArray;

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mainImage;

        public ImageViewHolder(View view) {
            super(view);
            mainImage = (SimpleDraweeView) view.findViewById(R.id.image_post_view);

        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,PlaybackControlView.VisibilityListener, ExoPlayer.EventListener {
        private View view;
        private SimpleExoPlayerView mainVideo;

        private LinearLayout debugRootView;
        private TextView debugTextView;
        private Button retryButton;

        private boolean needRetrySource=false;


        public VideoViewHolder(View view) {
            super(view);
            this.view = view;
            mainVideo = (SimpleExoPlayerView) view.findViewById(R.id.video_post_view);

            mainVideo.setControllerVisibilityListener(this);

            View rootView = view.findViewById(R.id.root);
            rootView.setOnClickListener(this);
            debugRootView = (LinearLayout) view.findViewById(R.id.controls_root);
            debugTextView = (TextView) view.findViewById(R.id.debug_text_view);
            retryButton = (Button) view.findViewById(R.id.retry_button);
            retryButton.setOnClickListener(this);

        }

        public View getView() {
            return view;
        }

        @Override
        public void onClick(View view) {
            if (view == retryButton) {
                initializePlayer();
            } else if (view.getParent() == debugRootView) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    Log.d("exoexoe",activity.toString());
                    Log.d("exoexoe",((Button) view).getText().toString());
                    Log.d("exoexoe",trackSelectionHelper.toString());
                    Log.d("exoexoe",activity.toString());
                    trackSelectionHelper.showSelectionDialog(activity, ((Button) view).getText(),
                            trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
                }
            }
        }

        private void updateButtonVisibilities() {
            Log.d("exoexo","updating vis");
            debugRootView.removeAllViews();

            retryButton.setVisibility(needRetrySource ? View.VISIBLE : View.GONE);
            debugRootView.addView(retryButton);

            if (player == null) {
                return;
            }

            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo == null) {
                return;
            }

            for (int i = 0; i < mappedTrackInfo.length; i++) {
                TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
                if (trackGroups.length != 0) {
                    Button button = new Button(view.getContext());
                    int label;
                    switch (player.getRendererType(i)) {
                        case C.TRACK_TYPE_AUDIO:
                            label = R.string.audio;
                            break;
                        case C.TRACK_TYPE_VIDEO:
                            label = R.string.video;
                            break;
                        case C.TRACK_TYPE_TEXT:
                            label = R.string.text;
                            break;
                        default:
                            continue;
                    }
                    button.setText(label);
                    button.setTag(i);
                    button.setOnClickListener(this);
                    debugRootView.addView(button, debugRootView.getChildCount() - 1);
                }
            }
        }


        @Override
        public void onVisibilityChange(int i) {
            Log.d("exoexo","vis chnaged");
            debugRootView.setVisibility(i);
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object o) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelectionArray) {
            updateButtonVisibilities();
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
//                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
//                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }

        @Override
        public void onLoadingChanged(boolean b) {

        }

        @Override
        public void onPlayerStateChanged(boolean b, int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED) {
                showControls();
            }
            updateButtonVisibilities();
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {

        }

        @Override
        public void onPositionDiscontinuity() {



        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }


        private void showControls() {
            debugRootView.setVisibility(View.VISIBLE);
        }

    }

    private void initializePlayer(){

        mainHandler = new Handler();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();

    }

    private void releasePlayer() {
        if (player != null) {
//            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
        }
    }

    public PostsAdapter(List<Post> postsList, Activity activity) {
        this.activity=activity;
        this.postsList = postsList;
       initializePlayer();

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
                player.addListener((VideoViewHolder)holder);


                //we can use this later to implement "mute"
                player.setVolume(0f);

                uri = post.getUri();
                dataSourceFactory =
                new DefaultDataSourceFactory(view.getContext(), bandwidthMeter,new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                videoSource = new HlsMediaSource(uri,
                        dataSourceFactory, mainHandler, null);

//                trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

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