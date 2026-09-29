# XAML runtime contract

Ownership follows `.cswinrt/src/WinRT.Runtime/ComWrappersSupport.cs` (`GetInterfaceTableEntries`), `.cswinrt/src/cswinrt/code_writers.h` (`write_composable_constructors`), and `.cswinrt/src/WinRT.Runtime/Interop/EventSource{TDelegate}.cs`.

- Generated `IComponentConnector` is another interface in the page type CCW definition. `createComposableCCWForObject` constructs that outer before invoking the native factory. The connector must never be a separate object substituted for the page.
- `Connect` receives a borrowed inspectable. Use existing `WinRTObjectMarshaller.fromAbi` and the normal typed projection conversion; do not attach ownership to the incoming pointer or release it. Store the resolved control in a generated instance field.
- `GetBindingConnector` returns a caller-owned interface pointer when a binding scope exists. Without such a scope, initialize the out pointer to null and return success. Reuse normal interface marshaling for non-null scopes.
- The actual native Page aggregation and `Application.LoadComponent` call must be verified in Gallery. An ABI-shaped synthetic host only proves outer interface identity and callback mechanics.
- Generated event code uses projected event add/remove APIs, `EventSource` and existing delegate/reference tracking. It must not maintain an independent global callback registry. Any generated unsubscribe hook must retain the same handler instance used for subscription.
- Named controls and initialization state are fields on each page. Kotlin constructor calls are lowered to `initializeWinRTXamlComponent(constructorCall)` after the complete constructor returns, including secondary constructor bodies. Delegating base-constructor calls are not wrapped. Kotlin 2.4 rich constructor references contain invocation bodies and follow the same lowering. The managed `WinRTXamlComponent` interface carries the contract across compilation boundaries; it has no IID or CCW entry.
- User code overrides `initializeComponent()`, calls `super.initializeComponent()` to execute the generated guarded load, and then accesses named controls. There is no separate initialized callback. Repeated base calls preserve the loaded tree and subscriptions; arbitrary user override statements are ordinary Kotlin code and are not automatically idempotent. Constructor exceptions skip initialization; initialization exceptions propagate before the call returns an instance.
- A separate generated construction guard owns the automatic call to the user override. Authoring's activation boundary ensures completion before marshaling the result; if a compiled constructor call already completed it, the activation check does nothing. This also covers factories supplied by callers that do not use compiler lowering. Failure is retained and no partially initialized result is published by the activation factory.
- This is the user-requested C++/WinRT lifecycle alignment: `strings/base_implements.h` owns `create_and_initialize`, and `nuget/readme.md#initializecomponent` documents construction followed by initialization. CsWinRT remains the ABI/composition reference. The plugin must be enabled in consuming Kotlin modules; reflection bypassing compiled Kotlin call sites must explicitly use `initializeWinRTXamlComponent` after construction. Native target validation remains a separate gate.
- XamlCompiler `CSharpPagePass1.tt` guards reentrant loading before `LoadComponent`; Kotlin keeps this guard. To satisfy the approved failure contract, final generated code distinguishes loading, loaded and failed: successful repeat and synchronous reentry do nothing; failure propagates and subsequent attempts fail with the original cause rather than duplicate partially installed subscriptions. This is a narrow documented difference from the C# boolean guard. No thread-safe background XAML loading is implied.

`XamlConnectorIdentityTest` verifies same outer IUnknown, same managed receiver, per-instance dispatch, borrowed target ownership and null connector output using a synthetic connector-shaped interface (its GUID is not the WinUI IID). `EventRuntimeInfrastructureCommonTest` verifies existing token/handler ownership. Native execution and real WinUI loading remain separate integration gates.

## Authored XAML types

`WinRTXamlTypeDefinition` and `WinRTXamlMemberDefinition` correspond to the
`XamlUserType` activator and `XamlMember` getter/setter delegates emitted by
XamlCompiler's `CSharpTypeInfoPass2.tt`. CsWinRT owns the underlying CCW/RCW
identity and inspectable marshaling; Kotlin reuses those runtime responsibilities.
Generated accessors perform direct Kotlin calls. The runtime does not reflect
over properties, interpret markup, or duplicate value-type classification.
The generated registration includes Kotlin `KClass` values for the native base
and member types. When the SDK metadata provider has no IXamlType for one of
those system types, Kotlin supplies the `XamlSystemBaseType` identity and
underlying type that XamlCompiler's C# output would put in its type table.

Registered definitions extend the existing authored identity provider with
`IsConstructible`, `ActivateInstance`, `ContentProperty`, and `GetMember`.
Activation completes the two-phase XAML construction contract before publishing
the owned inspectable result. Getter outputs are owned by the caller; setter
inputs are borrowed. Members not declared on the type delegate to its base.
Identity-only registrations retain their existing nonconstructible behavior.

The Kotlin source scanner now emits an application-only WinMD before
XamlCompiler pass 1. It recognizes adjacent same-basename Kotlin/XAML classes,
their WinRT bases, no-argument constructors, and explicitly typed public or
internal instance properties. An explicitly declared type is required for a
property exposed to XAML because the first compiler pass precedes Kotlin IR.
The scanner emits Kotlin registration sources with direct constructors and
getter/setter calls. The page's generated load method registers all application
types before invoking `Application.LoadComponent`. Kotlin semantic compilation
then emits the authoritative application WinMD for pass 2.

Gallery `ControlExample` migration, compiled bindings, and end-to-end Native
support remain incomplete. Existing six Gallery XAML pages now pass the
pre-analysis WinMD, semantic compilation, final XAML compilation, and main
Kotlin compilation stages.

The semantic compilation now exports declared public/internal instance
properties into the application-only `KotlinXaml.winmd`. It uses Kotlin IR
types and accessor visibility, excludes generated `x:Name` properties and
private state, and preserves nullable value types as `IReference<T>`. This
schema is separate from authored component ABI export. The metadata writer
owns Property/MethodSemantics encoding, following CsWinRT
`WinRTTypeWriter.AddPropertyDefinition`. The initial XAML analysis consumes
the scanner's application declaration WinMD. `@WinRTXamlContentProperty` maps
implicit child content to an explicitly typed property in both passes and in
the generated runtime metadata.
