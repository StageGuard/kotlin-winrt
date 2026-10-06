package io.github.composefluent.winrt.runtime

internal class WinRTObjectSupport<K : Any, TReference : AutoCloseable>(
    private val queryInterfaceCacheFor: (K) -> ConcurrentCacheMap<WinRTTypeHandle, TReference>,
    private val additionalTypeDataFor: (K) -> ConcurrentCacheMap<WinRTTypeHandle, Any>,
    private val closeReference: (TReference) -> Unit,
) {
    fun queryInterfaceCache(instance: K): ConcurrentCacheMap<WinRTTypeHandle, TReference> =
        queryInterfaceCacheFor(instance)

    fun additionalTypeData(instance: K): ConcurrentCacheMap<WinRTTypeHandle, Any> =
        additionalTypeDataFor(instance)

    fun isInterfaceImplemented(
        instance: K,
        primaryTypeHandle: WinRTTypeHandle?,
        interfaceType: WinRTTypeHandle,
        nativeObject: TReference,
        throwIfNotImplemented: Boolean = false,
        tryQueryInterface: (Guid) -> TReference?,
        missingInterfaceError: (WinRTTypeHandle) -> Throwable,
    ): Boolean {
        if (primaryTypeHandle == interfaceType) {
            return true
        }

        val queryInterfaceCache = queryInterfaceCache(instance)
        if (queryInterfaceCache.containsKey(interfaceType)) {
            return true
        }

        val queried = tryQueryInterface(interfaceType.interfaceId)
        if (queried != null) {
            val existing = queryInterfaceCache.putIfAbsent(interfaceType, queried)
            if (existing != null) {
                closeReference(queried)
            }
            return true
        }

        if (throwIfNotImplemented) {
            throw missingInterfaceError(interfaceType)
        }

        return false
    }

    fun getObjectReferenceForType(
        instance: K,
        primaryTypeHandle: WinRTTypeHandle?,
        interfaceType: WinRTTypeHandle,
        nativeObject: TReference,
        tryQueryInterface: (Guid) -> TReference?,
        missingInterfaceError: (WinRTTypeHandle) -> Throwable,
    ): TReference {
        if (primaryTypeHandle == interfaceType) {
            return nativeObject
        }

        val queryInterfaceCache = queryInterfaceCache(instance)
        queryInterfaceCache[interfaceType]?.let { return it }

        val queried = tryQueryInterface(interfaceType.interfaceId)
        if (queried != null) {
            val existing = queryInterfaceCache.putIfAbsent(interfaceType, queried)
            if (existing != null) {
                closeReference(queried)
                return existing
            }
            return queried
        }

        throw missingInterfaceError(interfaceType)
    }

    fun <T : Any> getOrAddAdditionalTypeData(
        instance: K,
        type: WinRTTypeHandle,
        factory: () -> T,
    ): T {
        val additionalTypeData = additionalTypeData(instance)
        val existing = additionalTypeData[type]
        if (existing != null) {
            @Suppress("UNCHECKED_CAST")
            return existing as T
        }

        val created = factory()
        val raced = additionalTypeData.putIfAbsent(type, created)
        @Suppress("UNCHECKED_CAST")
        return (raced ?: created) as T
    }
}

// CsWinRT IInspectable.net5.cs and SingleInterfaceOptimizedObject.net5.cs own these
// caches on each RCW. A global weak-key table with strong values can retain its key
// through cached callbacks or tracker edges. Each cached reference already owns its
// native-only ComPtr cleaner; no cleaner may capture this managed cache graph.
internal class WinRTObjectState<TReference : AutoCloseable> {
    val queryInterfaceCache = ConcurrentCacheMap<WinRTTypeHandle, TReference>()
    val additionalTypeData = ConcurrentCacheMap<WinRTTypeHandle, Any>()
}

internal object WinRTObjectStateInitialization {
    val lock = PlatformLock()
}
