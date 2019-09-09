package com.itheima.musicproject.manager;


import com.itheima.musicproject.domain.Song;
import com.itheima.musicproject.listener.PlayListListener;

import java.util.List;

/**
 * Created by smile on 2018/6/23.
 */

public interface PlayListManager {
    List<Song> getPlayList();

    void setPlayList(List<Song> datum);

    void play(Song song);

    void pause();

    void resume();

    void delete(Song song);

    Song getPlayData();

    Song next();

    Song previous();

    int getLoopModel();

    int changeLoopModel();

    void addPlayListListener(PlayListListener listener);

    void removePlayListListener(PlayListListener listener);

    void destroy();

    /**
     * 下一首播放
     * @param song
     */
    void nextPlay(Song song);
}
