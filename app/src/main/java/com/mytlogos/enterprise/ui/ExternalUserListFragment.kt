package com.mytlogos.enterprise.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.ExternalUser
import com.mytlogos.enterprise.tools.externalUserTypeToName
import com.mytlogos.enterprise.viewmodel.ExternalUserViewModel
import kotlinx.coroutines.flow.Flow

class ExternalUserListFragment : BasePagingFragment<ExternalUser, ExternalUserViewModel>() {
    override val viewModelClass: Class<ExternalUserViewModel>
        get() = ExternalUserViewModel::class.java

    override fun createAdapter(): BaseAdapter<ExternalUser, *> = UserAdapter()

    override fun createPaged(model: ExternalUserViewModel): Flow<PagingData<ExternalUser>> {
        return viewModel.externalUser
    }

    private class UserDiff : DiffUtil.ItemCallback<ExternalUser>() {
        override fun areItemsTheSame(oldItem: ExternalUser, newItem: ExternalUser): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: ExternalUser, newItem: ExternalUser): Boolean {
            return oldItem == newItem
        }
    }

    private class UserAdapter : BaseAdapter<ExternalUser, NewMetaViewHolder>(UserDiff()) {

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                holder.topLeftText.text = externalUserTypeToName(item.type)
                holder.mainText.text = item.identifier
            }
        }

        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)
    }

}