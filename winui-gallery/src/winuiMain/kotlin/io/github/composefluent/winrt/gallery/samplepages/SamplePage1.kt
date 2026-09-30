package io.github.composefluent.winrt.gallery.samplepages

import microsoft.ui.xaml.controls.Page
import microsoft.ui.xaml.media.animation.*

internal class SamplePage1 : Page() {
    fun PrepareConnectedAnimation(configuration: ConnectedAnimationConfiguration?) {
        ConnectedAnimationService.getForCurrentView().prepareToAnimate("ForwardConnectedAnimation", SourceElement).apply { if (configuration != null) this.configuration = configuration }
    }
    override fun initializeComponent() { super.initializeComponent(); loaded.add { _, _ -> ConnectedAnimationService.getForCurrentView().getAnimation("BackwardConnectedAnimation")?.tryStart(SourceElement) } }
}
