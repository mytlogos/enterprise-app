package com.mytlogos.enterprise.ui

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.model.NotificationItem
import com.mytlogos.enterprise.viewmodel.NotificationViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible

class NotificationFragment : BaseListFragment<NotificationItem, NotificationViewModel>() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setTitle("Notification History")
        return view
    }

    override val viewModelClass: Class<NotificationViewModel>
        get() = NotificationViewModel::class.java

    override fun createPagedListLiveData(): LiveData<PagedList<NotificationItem>> {
        return viewModel!!.notifications
    }

    override fun createFlexible(value: NotificationItem): IFlexible<*> {
        return FlexibleNotification(value)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.notification_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_notifications) {
            viewModel!!.clearNotifications()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private class FlexibleNotification(item: NotificationItem) :
        AbstractFlexibleItem<MetaViewHolder>() {
        private val item: NotificationItem?
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as FlexibleNotification
            return item == that.item
        }

        override fun hashCode(): Int {
            return item?.hashCode() ?: 0
        }

        override fun getLayoutRes(): Int {
            return R.layout.meta_item
        }

        override fun createViewHolder(
            view: View,
            adapter: FlexibleAdapter<IFlexible<*>?>?
        ): MetaViewHolder {
            return MetaViewHolder(view, adapter)
        }

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<*>?>?,
            holder: MetaViewHolder,
            position: Int,
            payloads: List<Any>
        ) {
            holder.mainText.text = item!!.title
            holder.topLeftText.text = item.dateTime.toString("dd.MM.yyyy HH:mm:ss")
        }

        init {
            this.item = item
        }
    }
}