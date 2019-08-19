package com.mytlogos.enterprise.tools;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String getDomain(String url) {
        String host = URI.create(url).getHost();
        if (host == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(www\\.)?(.+?)/?").matcher(host);

        String domain;
        if (matcher.matches()) {
            domain = matcher.group(2);
            int index = domain.indexOf("/");

            if (index >= 0) {
                domain = domain.substring(0, index);
            }
        } else {
            domain = host;
        }
        return domain;
    }

    public static String externalUserTypeToName(int type) {
        switch (type) {
            case 0:
                return "NovelUpdates";
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }

    public static <E> LiveData<PagedList<E>> transform(LiveData<List<E>> listLiveData) {
        return Transformations.switchMap(
                listLiveData,
                input -> new LivePagedListBuilder<>(
                        new DataSource.Factory<Integer, E>() {
                            @NonNull
                            @Override
                            public DataSource<Integer, E> create() {
                                return new StaticDataSource<>(input);
                            }
                        }, 1000
                ).build()
        );
    }

    private static class StaticDataSource<E> extends PageKeyedDataSource<Integer, E> {
        private final List<E> data;

        private StaticDataSource(List<E> data) {
            this.data = data;
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, E> callback) {
            callback.onResult(data, null, null);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, E> callback) {

        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, E> callback) {

        }
    }
}
