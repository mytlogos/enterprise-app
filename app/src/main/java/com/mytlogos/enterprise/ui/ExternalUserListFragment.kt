package com.mytlogos.enterprise.ui

import android.view.*
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ExternalUser
import com.mytlogos.enterprise.tools.externalUserTypeToName
import com.mytlogos.enterprise.viewmodel.ExternalUserViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible

class ExternalUserListFragment : BaseListFragment<ExternalUser, ExternalUserViewModel>() {
    override val viewModelClass: Class<ExternalUserViewModel>
        get() = ExternalUserViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<ExternalUser>> {
        return viewModel.externalUser
    }

    override fun createFlexible(value: ExternalUser): IFlexible<*> {
        return UserItem(value)
    }

    private class UserItem(private val externalUser: ExternalUser) :
        AbstractFlexibleItem<MetaViewHolder>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val userItem = other as UserItem
            return externalUser == userItem.externalUser
        }

        override fun hashCode(): Int {
            return externalUser.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.meta_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>>
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            holder.topLeftText.text = externalUserTypeToName(
                externalUser.type)
            holder.mainText.text = externalUser.identifier
        }
    }
}