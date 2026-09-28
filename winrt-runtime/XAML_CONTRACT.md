# XAML runtime contract

Ownership follows `.cswinrt/src/WinRT.Runtime/ComWrappersSupport.cs` (`GetInterfaceTableEntries`), `.cswinrt/src/cswinrt/code_writers.h` (`write_composable_constructors`), and `.cswinrt/src/WinRT.Runtime/Interop/EventSource{TDelegate}.cs`.

- Generated `IComponentConnector` is another interface in the page type CCW definition. `createComposableCCWForObject` constructs that outer before invoking the native factory. The connector must never be a separate object substituted for the page.
- `Connect` receives a borrowed inspectable. Use existing `WinRTObjectMarshaller.fromAbi` and the normal typed projection conversion; do not attach ownership to the incoming pointer or release it. Store the resolved control in a generated instance field.
- `GetBindingConnector` returns a caller-owned interface pointer when a binding scope exists. Without such a scope, initialize the out pointer to null and return success. Reuse normal interface marshaling for non-null scopes.
- The actual native Page aggregation and `Application.LoadComponent` call must be verified in Gallery. An ABI-shaped synthetic host only proves outer interface identity and callback mechanics.
- Generated event code uses projected event add/remove APIs, `EventSource` and existing delegate/reference tracking. It must not maintain an independent global callback registry. Any generated unsubscribe hook must retain the same handler instance used for subscription.
- Named controls and initialization state are fields on each page. Initialization runs explicitly after the projected base/composition construction. Named-element getters fail clearly before the field is connected.
- XamlCompiler `CSharpPagePass1.tt` guards reentrant loading before `LoadComponent`; Kotlin keeps this guard. To satisfy the approved failure contract, final generated code distinguishes loading, loaded and failed: successful repeat and synchronous reentry do nothing; failure propagates and subsequent attempts fail with the original cause rather than duplicate partially installed subscriptions. This is a narrow documented difference from the C# boolean guard. No thread-safe background XAML loading is implied.

`XamlConnectorIdentityTest` verifies same outer IUnknown, same managed receiver, per-instance dispatch, borrowed target ownership and null connector output using a synthetic connector-shaped interface (its GUID is not the WinUI IID). `EventRuntimeInfrastructureCommonTest` verifies existing token/handler ownership. Native execution and real WinUI loading remain separate integration gates.
