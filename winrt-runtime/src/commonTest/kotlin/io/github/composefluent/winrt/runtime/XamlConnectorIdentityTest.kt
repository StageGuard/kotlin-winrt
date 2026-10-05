package io.github.composefluent.winrt.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** ABI prerequisite only. The actual WinUI IID and LoadComponent integration are tested by Gallery. */
class XamlConnectorIdentityTest {
    // CsWinRT ComWrappersSupport.Init balances a delegating factory reference,
    // while CLR's NativeObjectWrapper owns the nondelegating inner. Verify both
    // collection and a borrowed QI lease against that same ownership boundary.
    @Test
    fun aggregated_page_and_its_inner_are_reclaimed_without_explicit_close() {
        ComWrappersSupport.clearRegistriesForTests()
        val nativeId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda4")
        val overrideId = registerPageForLifetimeTest()
        WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(nativeId, emptyList())),
            defaultInterfaceId = nativeId,
        ).use { native ->
            val abandoned = abandonAggregatedPage(native, overrideId)
            awaitPageCollection(abandoned)
            assertEquals(1u, WinRTInspectableComObject.tryProbeReferenceCount(native.borrowCachedInterfacePointer(nativeId)))
        }
        ComWrappersSupport.clearRegistriesForTests()
    }

    @Test
    fun borrowed_aggregate_interface_keeps_inner_alive_after_owner_close() {
        ComWrappersSupport.clearRegistriesForTests()
        val nativeId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda5")
        val overrideId = registerPageForLifetimeTest()
        WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(nativeId, emptyList())),
            defaultInterfaceId = nativeId,
        ).use { native ->
            val page = createAggregatedPage(native, overrideId)
            val borrowed = requireNotNull(page.reference).instance.queryInterface(nativeId).getOrThrow()
            requireNotNull(page.reference).close()
            assertEquals(2u, WinRTInspectableComObject.tryProbeReferenceCount(native.borrowCachedInterfacePointer(nativeId)))
            borrowed.queryInterface(IID.IInspectable).getOrThrow().close()
            borrowed.close()
            assertEquals(1u, WinRTInspectableComObject.tryProbeReferenceCount(native.borrowCachedInterfacePointer(nativeId)))
        }
        ComWrappersSupport.clearRegistriesForTests()
    }

    @Test
    fun external_com_reference_keeps_aggregated_page_alive_until_release() {
        ComWrappersSupport.clearRegistriesForTests()
        val nativeId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda6")
        val overrideId = registerPageForLifetimeTest()
        WinRTInspectableComObject(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(nativeId, emptyList())),
            defaultInterfaceId = nativeId,
        ).use { native ->
            val (abandoned, external) = abandonExternallyReferencedPage(native, overrideId)
            PlatformFinalization.drain()
            verifyExternalReferenceAndRelease(abandoned, external)
            awaitPageCollection(abandoned)
            assertEquals(1u, WinRTInspectableComObject.tryProbeReferenceCount(native.borrowCachedInterfacePointer(nativeId)))
        }
        ComWrappersSupport.clearRegistriesForTests()
    }

    private fun registerPageForLifetimeTest(): Guid {
        val overrideId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda7")
        ComWrappersSupport.registerStaticCcwDefinition(Page::class, WinRTCcwDefinition(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(overrideId, emptyList())),
            defaultInterfaceId = overrideId, runtimeClassName = "test.AggregatedXamlPage",
        ))
        return overrideId
    }

    private fun createAggregatedPage(native: WinRTInspectableComObject, overrideId: Guid): Page {
        val page = Page()
        val nativePointer = native.borrowCachedInterfacePointer(native.primaryInterfaceId)
        assertEquals(1u, WinRTInspectableComObject.tryProbeReferenceCount(nativePointer), "Native host baseline")
        page.reference = ComWrappersSupport.createComposableCCWForObject(page, overrideId) { outer, innerOut, instanceOut ->
            PlatformAbi.writePointer(innerOut, native.acquireReference(IID.IInspectable))
            // A real aggregation returns a delegating interface with the outer's
            // IUnknown. The separate nondelegating inner owns the native object.
            WinRTPlatformApi.addRefRaw(outer)
            PlatformAbi.writePointer(instanceOut, outer)
            KnownHResults.S_OK.value
        }
        kotlin.test.assertNotNull(page.reference!!.instance.comPtr.support.nativeObjectLifetime)
        assertEquals(2u, WinRTInspectableComObject.tryProbeReferenceCount(nativePointer), "Owned nondelegating inner")
        return page
    }

    private fun abandonAggregatedPage(native: WinRTInspectableComObject, overrideId: Guid): PlatformManagedWeakReference<Page> =
        PlatformManagedWeakReference(createAggregatedPage(native, overrideId))

    private fun abandonExternallyReferencedPage(native: WinRTInspectableComObject, overrideId: Guid):
        Pair<PlatformManagedWeakReference<Page>, ComObjectReference> {
        val page = createAggregatedPage(native, overrideId)
        val external = requireNotNull(page.reference).outer.queryInterface(overrideId).getOrThrow()
        return PlatformManagedWeakReference(page) to external
    }

    // Native debug GC keeps the result of a weak read until its stack frame
    // returns. Prove the external reference's root in a separate frame.
    private fun verifyExternalReferenceAndRelease(page: PlatformManagedWeakReference<Page>, external: ComObjectReference) {
        kotlin.test.assertNotNull(page.get())
        external.close()
    }

    private fun awaitPageCollection(page: PlatformManagedWeakReference<Page>) {
        repeat(30) {
            PlatformFinalization.drain()
            if (page.get() == null) {
                // The page and its resource wrapper can be collected in separate
                // cycles. Drain native-only cleanup after proving the page is gone.
                repeat(3) { PlatformFinalization.drain() }
                return
            }
            val pressure = List(128) { ByteArray(1024) }
            assertEquals(128, pressure.size)
        }
        kotlin.test.assertNull(page.get())
    }

    @Test
    fun connector_shares_composed_outer_identity_and_receives_borrowed_target() {
        // CsWinRT ComWrappersSupport.GetInterfaceTableEntries and code_writers.h
        // write_composable_constructors: generated interfaces belong to the authored outer.
        // XamlCompiler CSharpPagePass2.tt: Connect(int, object), GetBindingConnector(int, object).
        ComWrappersSupport.clearRegistriesForTests()
        val overrideId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda1")
        val connectorId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda2")
        val nativeId = Guid("82297301-90b2-48da-9ac4-3b4d147aeda3")
        val connector = WinRTInspectableInterfaceDefinition(connectorId, listOf(
            WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Int32_Ptr) { managed, arguments ->
                val page = managed as Page
                page.connectionId = arguments[0] as Int
                page.element = WinRTObjectMarshaller.fromAbi(arguments[1] as RawAddress)
                KnownHResults.S_OK.value
            },
            WinRTInspectableMethodDefinition(ComMethodSignatures.HResult_Int32_Ptr_Ptr) { _, arguments ->
                // No template binding scope: returned connector is null, with no reference transferred.
                PlatformAbi.writePointer(arguments[2] as RawAddress, RawAddress.Null)
                KnownHResults.S_OK.value
            },
        ))
        ComWrappersSupport.registerStaticCcwDefinition(Page::class, WinRTCcwDefinition(
            interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(overrideId, emptyList()), connector),
            defaultInterfaceId = overrideId,
            runtimeClassName = "test.XamlPage",
        ))
        val target = Any()
        val targetHost = WinRTInspectableComObject.inspectableBox(target, "test.Element")
        val targetReference = targetHost.createReference(IID.IInspectable)
        val pages = listOf(Page(), Page())
        try {
            for ((index, page) in pages.withIndex()) {
                val native = WinRTInspectableComObject(
                    interfaceDefinitions = listOf(WinRTInspectableInterfaceDefinition(nativeId, emptyList())),
                    defaultInterfaceId = nativeId,
                )
                var factoryInstance = RawAddress.Null
                try {
                    ComWrappersSupport.createComposableCCWForObject(page, nativeId) { outer, innerOut, instanceOut ->
                        // The extra interface is available before the native composable factory returns.
                        IInspectableReference(outer.asRawComPtr(), IID.IInspectable, preventReleaseOnDispose = true).use { base ->
                            base.queryInterface(connectorId).getOrThrow().use { assertTrue(it.sameIdentity(base)) }
                        }
                        // Each factory output needs its own reference. detachReference
                        // transfers the host baseline and cannot be used twice here.
                        PlatformAbi.writePointer(innerOut, native.acquireReference(IID.IInspectable))
                        factoryInstance = native.acquireReference(nativeId)
                        PlatformAbi.writePointer(instanceOut, factoryInstance)
                        KnownHResults.S_OK.value
                    }.use { composed ->
                        page.reference = composed
                        composed.outer.queryInterface(connectorId).getOrThrow().use { connection ->
                            assertTrue(connection.sameIdentity(composed.outer))
                            assertSame(page, ComWrappersSupport.findObject(connection.pointer.asRawAddress(), Page::class))
                            val targetCount = WinRTInspectableComObject.tryProbeReferenceCount(targetReference.pointer.asRawAddress())
                            HResult(ComVtableInvoker.invokeArgs(connection.pointer, 6, index + 1, targetReference.pointer.asRawAddress())).requireSuccess()
                            assertEquals(targetCount, WinRTInspectableComObject.tryProbeReferenceCount(targetReference.pointer.asRawAddress()))
                            assertSame(target, page.element)
                            assertEquals(index + 1, page.connectionId)
                            PlatformAbi.confinedScope().use { scope ->
                                val result = PlatformAbi.allocatePointerSlot(scope)
                                PlatformAbi.writePointer(result, targetReference.pointer.asRawAddress())
                                HResult(ComVtableInvoker.invokeArgs(connection.pointer, 7, 1, targetReference.pointer, result)).requireSuccess()
                                assertTrue(PlatformAbi.isNull(PlatformAbi.readPointer(result)))
                            }
                        }
                    }
                } finally {
                    // This synthetic instance has a separate IUnknown, rather than
                    // WinUI's delegating outer identity and native aggregation lifetime.
                    if (!PlatformAbi.isNull(factoryInstance)) WinRTPlatformApi.releaseRaw(factoryInstance)
                    native.close()
                }
            }
            assertEquals(1, pages[0].connectionId)
            assertEquals(2, pages[1].connectionId)
        } finally {
            targetReference.close()
            targetHost.close()
            ComWrappersSupport.clearRegistriesForTests()
        }
    }

    private class Page : WinRTComposableObject {
        var reference: WinRTComposableObjectReference? = null
        override val winRTComposableObjectReference get() = reference
        var element: Any? = null
        var connectionId: Int = 0
    }
}
