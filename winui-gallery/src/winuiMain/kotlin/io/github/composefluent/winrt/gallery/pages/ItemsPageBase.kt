package io.github.composefluent.winrt.gallery.pages

import io.github.composefluent.winrt.gallery.GalleryNavigationHost
import io.github.composefluent.winrt.gallery.models.ControlInfoDataItem
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.data.*

/** Shared business base; its final pages keep one composed SDK Page identity. */
internal abstract class ItemsPageBase : Page(), INotifyPropertyChanged {
    private val handlers = mutableListOf<PropertyChangedEventHandler>()
    var Items: List<ControlInfoDataItem>? = null
        protected set(value) { if (field !== value) { field = value; handlers.toList().forEach { it(this, PropertyChangedEventArgs("Items")) } } }
    override fun addPropertyChanged(handler: PropertyChangedEventHandler) { handlers.add(handler) }
    override fun removePropertyChanged(handler: PropertyChangedEventHandler) { handlers.remove(handler) }
    protected fun OnItemClick(sender: Any?, args: ItemClickEventArgs) { (args.clickedItem as? ControlInfoDataItem)?.let { GalleryNavigationHost.navigate(it.UniqueId) } }
    protected fun OnItemContainerChanged(sender: ListViewBase, args: ContainerContentChangingEventArgs) {
        val item = args.item as? ControlInfoDataItem ?: return
        args.itemContainer?.isEnabled = item.IncludedInBuild
    }
}
