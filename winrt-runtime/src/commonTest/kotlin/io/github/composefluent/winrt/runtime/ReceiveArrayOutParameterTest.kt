package io.github.composefluent.winrt.runtime

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@WinRTProjectionAbiType(name = "Sample.ReceiveArrayOut<kotlin.Int>", kind = WinRTProjectionAbiTypeKind.ARRAY)
private object ReceiveArrayOutCodec {
    var storage: WinRTAbiArray? = null
    var disposals = 0
    var failDecode = false

    @WinRTProjectionAbiCodec(role = WinRTProjectionAbiCodecRole.FROM_ABI, type = "Sample.ReceiveArrayOut<kotlin.Int>")
    fun decode(length: RawAddress, data: RawAddress): Array<Int> {
        check(!failDecode) { "array decode failed" }
        assertEquals(1, PlatformAbi.readInt32(length))
        return arrayOf(PlatformAbi.readInt32(PlatformAbi.readPointer(data)))
    }

    @WinRTProjectionAbiCodec(role = WinRTProjectionAbiCodecRole.DISPOSE_ABI, type = "Sample.ReceiveArrayOut<kotlin.Int>")
    fun dispose(length: RawAddress, data: RawAddress) {
        assertEquals(1, PlatformAbi.readInt32(length))
        assertEquals(PlatformAbi.pointerKey(checkNotNull(storage).data), PlatformAbi.pointerKey(PlatformAbi.readPointer(data)))
        storage?.close(); storage = null; disposals++
    }
}

@WinRTProjectionCallSite
private fun receiveArrayAndResult(
    receiver: ComObjectReference,
    slot: Int,
    @WinRTProjectionParameter(direction = WinRTCallSiteParameterDirection.OUT, abiType = "Sample.ReceiveArrayOut<kotlin.Int>") values: WinRTOut<Array<Int>>,
): Int = TODO("compiler lowers the array output and independent return")

class ReceiveArrayOutParameterTest {
    @Test
    fun array_output_uses_two_pointers_and_releases_storage_on_success_and_failure() {
        // CsWinRT code_writers.h write_projection_parameter_type and the receive-array
        // marshaler preserve length/data outputs separately from the method retval.
        val iid = Guid("285e4646-5391-4f70-8860-e74eaa4ba9c2")
        var failCall = false
        val method = WinRTInspectableMethodDefinition(ComMethodSignature.of(
            ComAbiValueKind.Pointer, ComAbiValueKind.Pointer, ComAbiValueKind.Pointer,
        )) { args ->
            val array = WinRTAbiArray.allocateInput(1, 4, 4)
            ReceiveArrayOutCodec.storage = array
            PlatformAbi.writeInt32(array.data, 41)
            PlatformAbi.writeInt32(args[0] as RawAddress, 1)
            PlatformAbi.writePointer(args[1] as RawAddress, array.data)
            PlatformAbi.writeInt32(args[2] as RawAddress, 7)
            if (failCall) KnownHResults.E_FAIL.value else 0
        }
        ReceiveArrayOutCodec.disposals = 0
        WinRTInspectableComObject(listOf(WinRTInspectableInterfaceDefinition(iid, listOf(method))), defaultInterfaceId = iid).use { host ->
            host.createPrimaryReference().use { receiver ->
                try {
                    val values = WinRTOut<Array<Int>>()
                    assertEquals(7, receiveArrayAndResult(receiver, 6, values))
                    assertContentEquals(arrayOf(41), values.value)
                    assertEquals(1, ReceiveArrayOutCodec.disposals)
                    ReceiveArrayOutCodec.failDecode = true
                    assertFailsWith<IllegalStateException> { receiveArrayAndResult(receiver, 6, WinRTOut()) }
                    assertEquals(2, ReceiveArrayOutCodec.disposals)
                    ReceiveArrayOutCodec.failDecode = false
                    failCall = true
                    assertFailsWith<WinRTRuntimeException> { receiveArrayAndResult(receiver, 6, WinRTOut()) }
                    assertEquals(3, ReceiveArrayOutCodec.disposals)
                } finally {
                    ReceiveArrayOutCodec.failDecode = false
                    ReceiveArrayOutCodec.storage?.close(); ReceiveArrayOutCodec.storage = null
                }
            }
        }
    }
}
