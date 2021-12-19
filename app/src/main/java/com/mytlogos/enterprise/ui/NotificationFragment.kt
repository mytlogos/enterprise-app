package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.NotificationItem
import com.mytlogos.enterprise.viewmodel.NotificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NotificationFragment : BasePagingFragment<NotificationItem, NotificationViewModel>() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setTitle("Notification History")
        return view
    }

    override val viewModelClass: Class<NotificationViewModel>
        get() = NotificationViewModel::class.java

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notification_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_notifications) {
            lifecycleScope.launch { viewModel.clearNotifications() }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    internal class NotificationDiff : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem,
        ): Boolean {
            return oldItem == newItem
        }
    }

    internal class NotificationAdapter :
        BaseAdapter<NotificationItem, NewMetaViewHolder>(NotificationDiff()) {
        override val layoutId = R.layout.meta_item

        override fun createViewHolder(root: View, viewType: Int) = NewMetaViewHolder(root)

        override fun onBindViewHolder(holder: NewMetaViewHolder, position: Int) {
            val item = getItem(position)
            holder.mainText.text = item?.title ?: "Not available"
            holder.topLeftText.text =
                item?.dateTime?.toString("dd.MM.yyyy HH:mm:ss") ?: "Not available"
        }
    }

    override fun createAdapter(): BaseAdapter<NotificationItem, *> = NotificationAdapter()

    override fun createPaged(model: NotificationViewModel): Flow<PagingData<NotificationItem>> {
        return viewModel.notifications
    }
}