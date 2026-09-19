package io.github.composefluent.winrt.gallery.shell

import io.github.composefluent.winrt.gallery.*
import io.github.composefluent.winrt.runtime.await
import microsoft.ui.xaml.Thickness
import windows.foundation.Uri
import windows.ui.startscreen.JumpList
import windows.ui.startscreen.JumpListItem

@GalleryPage(route = "JumpList", title = "JumpList", group = "Shell", order = 2)
internal fun jumpListPage() = ExamplePage {
    val tasks = GalleryPageTasks(this)
    val supported = JumpList.isSupported()
    children.add(packageRequirement("JumpList", supported))
    children.add(label("Kotlin WinUI Gallery populates its jump list with your recently visited and favorited items. Restarting the app will restore these entries after any changes made on this page.").apply { margin = Thickness(0.0, 12.0, 0.0, 0.0) })
    example("Adding tasks to the JumpList.", stack(8.0) {
        children.add(label("Tasks are items with an empty GroupName. They appear in the built-in 'Tasks' section at the bottom of the jump list. Use tasks for common app-wide actions that are always relevant, such as composing a new message or opening settings. Each task launches the app with a specific argument string that your app can handle on startup."))
        children.add(stack(8.0, true) {
            children.add(Button("Add sample tasks") { jumpListTasksSample(tasks) }
                .apply { isEnabled = supported; style = controlStyle("AccentButtonStyle") })
            children.add(Button("Clear all items") { tasks.launch {
                val list = JumpList.loadCurrentAsync().await()
                list.items.clear()
                list.saveAsync().await()
            } }.apply { isEnabled = supported })
        })
    })
    example("Adding items to a custom group.", stack(8.0) {
        children.add(label("Custom groups let you organize jump list items into named sections. Set the GroupName property to a non-empty string and all items sharing the same GroupName will appear together under that heading. This is useful for grouping related items like recent projects, pinned documents, or user-defined categories."))
        children.add(Button("Add custom group items") { jumpListGroupSample(tasks) }
            .apply { isEnabled = supported })
    })
}

@GallerySample(route = "JumpList", title = "Adding tasks to the JumpList.")
internal fun jumpListTasksSample(tasks: GalleryPageTasks) = tasks.launch {
    val list = JumpList.loadCurrentAsync().await()
    list.items.add(JumpListItem.createWithArguments("/compose", "New Message").apply {
        description = "Compose a new message"
        groupName = ""
        logo = Uri("ms-appx:///Assets/AppList.targetsize-48.png")
    })
    list.items.add(JumpListItem.createWithArguments("/search", "Search").apply {
        description = "Search for items"
        groupName = ""
        logo = Uri("ms-appx:///Assets/AppList.targetsize-48.png")
    })
    list.saveAsync().await()
}

@GallerySample(route = "JumpList", title = "Adding items to a custom group.")
internal fun jumpListGroupSample(tasks: GalleryPageTasks) = tasks.launch {
    val list = JumpList.loadCurrentAsync().await()
    list.items.add(JumpListItem.createWithArguments("/project-alpha", "Project Alpha").apply {
        description = "Open Project Alpha"
        groupName = "Projects"
        logo = Uri("ms-appx:///Assets/AppList.targetsize-48.png")
    })
    list.items.add(JumpListItem.createWithArguments("/project-beta", "Project Beta").apply {
        description = "Open Project Beta"
        groupName = "Projects"
        logo = Uri("ms-appx:///Assets/AppList.targetsize-48.png")
    })
    list.saveAsync().await()
}
