package io.github.composefluent.winrt.runtime

import kotlin.reflect.KClass

// Reserve Native class-key collision headroom from the existing descriptor count.
internal actual fun intrinsicClassKeyMapInitialCapacity(keyCount: Int): Int = keyCount * 2

// KClassImpl is final. Obtain its implementation class once through public reflection,
// instead of creating a runtime ::class carrier during every type-name lookup.
private val standardKClassImplementation = (Any::class)::class

internal actual fun isErasedReferenceArrayType(
    type: KClass<*>,
    erasedArrayType: KClass<*>,
): Boolean =
    if (standardKClassImplementation.isInstance(type)) {
        // Native Array<T> has one TypeInfo; its element type remains erased. Match
        // TypeNameSupport.cs's fail-closed array name/boxing policy without spelling a name.
        erasedArrayType == type
    } else {
        // Preserve custom and unsupported KClass name access, including its failures.
        type.qualifiedName == "kotlin.Array"
    }
