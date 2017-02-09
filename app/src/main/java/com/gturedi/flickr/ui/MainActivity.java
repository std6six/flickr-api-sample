package com.gturedi.flickr.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gturedi.flickr.R;
import com.gturedi.flickr.adapter.PhotoAdapter;
import com.gturedi.flickr.model.PhotoModel;
import com.gturedi.flickr.model.event.SearchEvent;
import com.gturedi.flickr.service.FlickrService;
import com.gturedi.flickr.util.AppUtil;
import com.gturedi.flickr.util.RowClickListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import timber.log.Timber;

public class MainActivity
        extends BaseActivity implements RowClickListener<PhotoModel> {

    private FlickrService flickrService = FlickrService.INSTANCE;
    private PhotoAdapter adapter;
    private boolean isLoading;
    private int page = 1;

    @BindView(R.id.recycler) protected RecyclerView recycler;

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PhotoAdapter();
        adapter.setRowClickListener(this);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        setScrollListener();
        sendRequest();
    }

    @Override
    public void onRowClicked(int row, PhotoModel item) {
        startActivity(DetailActivity.createIntent(this, row, adapter.getAll()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SearchEvent event) {
        //dismissLoadingDialog();
        isLoading = false;
        if (event.exception == null) {
            //remove progress item
            adapter.remove(adapter.getItemCount()-1);
            adapter.addAll(event.item);
        } else {
            showGeneralError();
        }
    }

    private void sendRequest() {
        Timber.i("sendRequest: "+page);
        if (AppUtil.isConnected()) {
            //showLoadingDialog();
            // add null , so the adapter will check view_type and show progress bar at bottom
            adapter.addAll(null);
            isLoading = true;
            flickrService.searchAsync(page++);
        } else {
            showConnectionError();
        }
    }

    private void setScrollListener() {
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= FlickrService.PAGE_SIZE) {
                        sendRequest();
                    }
                }
            }
        });
    }

}