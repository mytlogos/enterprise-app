package com.mytlogos.enterprise.ui;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.ExternalUser;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.ExternalUserViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class ExternalUserListFragment extends BaseListFragment<ExternalUser, ExternalUserViewModel> {
    @Override
    ExternalUserViewModel createViewModel() {
        return ViewModelProviders.of(this).get(ExternalUserViewModel.class);
    }

    @Override
    LiveData<PagedList<ExternalUser>> createPagedListLiveData() {
        return getViewModel().getExternalUser();
    }

    @Override
    List<IFlexible> convertToFlexibles(Collection<ExternalUser> list) {
        List<IFlexible> flexibles = new ArrayList<>();

        for (ExternalUser user : list) {
            if (user == null) {
                break;
            }
            flexibles.add(new UserItem(user));
        }
        return flexibles;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // TODO: 02.08.2019 implement add external user
    }

    private static class UserItem extends AbstractFlexibleItem<MetaViewHolder> {
        private final ExternalUser externalUser;

        private UserItem(@NonNull ExternalUser externalUser) {
            this.externalUser = externalUser;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserItem userItem = (UserItem) o;

            return externalUser.equals(userItem.externalUser);
        }

        @Override
        public int hashCode() {
            return externalUser.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.meta_item;
        }

        @Override
        public MetaViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new MetaViewHolder(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MetaViewHolder holder, int position, List<Object> payloads) {
            holder.topLeftText.setText(Utils.externalUserTypeToName(this.externalUser.getType()));
            holder.mainText.setText(this.externalUser.getIdentifier());
        }
    }
}
