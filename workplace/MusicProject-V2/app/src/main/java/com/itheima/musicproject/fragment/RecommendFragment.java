package com.itheima.musicproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.itheima.musicproject.R;

import com.itheima.musicproject.activity.BaseWebViewActivity;
import com.itheima.musicproject.activity.ListDetailActivity;
import com.itheima.musicproject.activity.MusicPlayerActivity;
import com.itheima.musicproject.adapter.BaseRecyclerViewAdapter;
import com.itheima.musicproject.adapter.RecommendAdapter;
import com.itheima.musicproject.api.Api;
import com.itheima.musicproject.domain.Advertisement;
import com.itheima.musicproject.domain.List;
import com.itheima.musicproject.domain.Song;
import com.itheima.musicproject.domain.response.ListResponse;
import com.itheima.musicproject.reactivex.HttpListener;
import com.itheima.musicproject.util.ImageUtil;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * @author glsite.com
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class RecommendFragment extends BaseCommonFragment implements OnBannerListener {
    private LRecyclerView rv;
    private GridLayoutManager layoutManager;
    private RecommendAdapter adapter;
    private LRecyclerViewAdapter adapterWrapper;
    private Banner banner;
    private LinearLayout ll_day_container;
    private TextView tv_day;
    private java.util.List<Advertisement> bannerData;

    public static RecommendFragment newInstance() {

        Bundle args = new Bundle();

        RecommendFragment fragment = new RecommendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        super.initViews();
        rv=findViewById(R.id.rv);
        rv.setHasFixedSize(true);



        layoutManager = new GridLayoutManager(getActivity(), 3);
        rv.setLayoutManager(layoutManager);

        //layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
        //    @Override
        //    public int getSpanSize(int position) {
        //        //                ?????????ItemType
        //        int itemViewType = getItemViewType(position);
        //        if (getHeaderView(itemViewType) != null || getFooterView(itemViewType) != null) {
        //            //                    ???????????????Item???header???????????????spanCount??????
        //            return ((GridLayoutManager) layoutManager).getSpanCount();
        //        }
        //        //????????????????????????????????????Item???????????????
        //        return adapter.setSpanSizeLookup(getItemRealPosition(position));
        //    }
        //});


    }

    private void fetchData() {
        //???????????????????????????????????????????????????????????????
        //??????????????????RecyclerView?????????ItemType???????????????
        //????????????????????????????????????????????????RecyclerView??????????????????

        Observable<ListResponse<List>> list = Api.getInstance().lists();
        final Observable<ListResponse<Song>> songs = Api.getInstance().songs();
        final Observable<ListResponse<Advertisement>> advertisements = Api.getInstance().advertisements();

        final ArrayList<Object> d = new ArrayList<>();
        d.add("????????????");

        //?????????????????????????????????RxJava???????????????
        list.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpListener<ListResponse<List>>(getMainActivity()) {
                    @Override
                    public void onSucceeded(final ListResponse<List> data) {
                        super.onSucceeded(data);
                        d.addAll(data.getData());

                        songs.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new HttpListener<ListResponse<Song>>(getMainActivity()) {
                                    @Override
                                    public void onSucceeded(ListResponse<Song> data) {
                                        super.onSucceeded(data);
                                        d.add("????????????");
                                        d.addAll(data.getData());

                                        advertisements.subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new HttpListener<ListResponse<Advertisement>>(getMainActivity()){
                                                    @Override
                                                    public void onSucceeded(ListResponse<Advertisement> data) {
                                                        super.onSucceeded(data);
                                                        d.addAll(data.getData());

                                                        adapter.setData(d);
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    protected void initDatas() {
        super.initDatas();


        adapter = new RecommendAdapter(getActivity());
        adapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerViewAdapter.ViewHolder holder, int position) {
                startActivity(MusicPlayerActivity.class);
//                Object data =  adapter.getData(position);
//                if (data instanceof Song) {
//                    //??????
//                    ArrayList<Song> list = new ArrayList<>();
//                    list.add((Song) data);
//                    playListManager.setPlayList(list);
//                    playListManager.play((Song) data);
//                    startActivity(MusicPlayerActivity.class);
//                } else if (data instanceof List) {
//                    //??????
//                    startActivityExtraId(ListDetailActivity.class,((List) data).getId());
//                } else if (data instanceof Advertisement) {
//                    //??????
//                    BaseWebViewActivity.start(getMainActivity(),((Advertisement) data).getTitle(),((Advertisement) data).getUri());
//                }
            }
        });


        adapterWrapper = new LRecyclerViewAdapter(adapter);
        adapterWrapper.setSpanSizeLookup(new LRecyclerViewAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                //                ?????????ItemType
                int itemViewType = adapterWrapper.getItemViewType(position);
                if (position<adapterWrapper.getHeaderViewsCount() || position>(adapterWrapper.getHeaderViewsCount()+adapter.getItemCount())) {
                    //                    f???????????????Item???header???????????????spanCount??????
                    return ((GridLayoutManager) layoutManager).getSpanCount();
                }
                return adapter.setSpanSizeLookup(position);
            }
        });


        adapterWrapper.addHeaderView(createHeaderView());

        rv.setAdapter(adapterWrapper);
        rv.setPullRefreshEnabled(false);
        rv.setLoadMoreEnabled(false);




        fetchData();

        //?????????????????????
        banner.setImageLoader(new GlideImageLoader());
        fetchBannerData();

    }
    private void fetchBannerData() {
        Api.getInstance().advertisements().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpListener<ListResponse<Advertisement>>(getMainActivity()){
                    @Override
                    public void onSucceeded(ListResponse<Advertisement> data) {
                        super.onSucceeded(data);
                        showBanner(data.getData());
                    }
                });

    }
    private void showBanner(java.util.List<Advertisement> data) {
        //            //??????????????????
        this.bannerData=data;
        banner.setImages(data);
        banner.start();
    }


    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommend,null);
    }
    @Override
    public void onStart() {
        super.onStart();
        //????????????
        banner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        //????????????
        banner.stopAutoPlay();
    }

    private View createHeaderView() {
        View top = getLayoutInflater().inflate(R.layout.header_music_recommend, (ViewGroup) rv.getParent(), false);
        banner = top.findViewById(R.id.banner);
        banner.setOnBannerListener(this);

        ll_day_container = top.findViewById(R.id.ll_day_container);
        tv_day = top.findViewById(R.id.tv_day);
        //rl_day_container = top.findViewById(R.id.rl_day_container);

        //????????????
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        tv_day.setText(String.valueOf(day));

        //????????????3D???????????????????????????????????????????????????????????????Animation?????????
        //ll_day_container.setOnClickListener(this);

        return top;
    }

    @Override
    public void OnBannerClick(int position) {
        Advertisement advertisement = bannerData.get(position);
        //BaseWebViewActivity.start(getMainActivity(),"????????????",banner.getUri());
        BaseWebViewActivity.start(getMainActivity(),"????????????","http://www.ixuea.com");
    }
    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            //?????????????????????Banner???????????????????????????????????????
            Advertisement banner = (Advertisement) path;
            ImageUtil.show(getMainActivity(), imageView, banner.getBanner());
        }
    }
}
