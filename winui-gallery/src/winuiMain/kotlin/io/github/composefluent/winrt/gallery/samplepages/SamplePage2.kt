package io.github.composefluent.winrt.gallery.samplepages

import microsoft.ui.xaml.controls.Page
import microsoft.ui.xaml.media.animation.*

internal class SamplePage2 : Page() {
    fun PrepareConnectedAnimation(configuration: ConnectedAnimationConfiguration?) {
        ConnectedAnimationService.getForCurrentView().prepareToAnimate("BackwardConnectedAnimation", DestinationElement).apply { if (configuration != null) this.configuration = configuration }
    }
    override fun initializeComponent() { super.initializeComponent(); ContentPanel.transitions = TransitionCollection().apply { add(EntranceThemeTransition()) }; loaded.add { _, _ -> ConnectedAnimationService.getForCurrentView().getAnimation("ForwardConnectedAnimation")?.tryStart(DestinationElement) } }
}
