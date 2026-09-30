package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*

/** Typed binding calls live in the owning page so private Kotlin members remain private. */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class XamlCompiledBindingBodies(
    private val pluginContext: IrPluginContext,
    private val classes: Map<String?, IrClass>,
) {
    private fun projection(name: String): IrClass = if (name == "System.Object") pluginContext.irBuiltIns.anyClass.owner
        else classes[name] ?: requireNotNull(pluginContext.referenceClass(
        winRTFundamentalTypeForName(name)?.toKotlinProjectionTypeName()?.let { ClassId.topLevel(FqName("kotlin.$it")) }
            ?: xamlProjectionClassId(name))) {
        "Compiled XAML binding requires projection $name"
    }.owner
    private fun runtime(name: String) = pluginContext.referenceFunctions(
        CallableId(FqName("io.github.composefluent.winrt.runtime"), Name.identifier(name))).single()
    private fun method(klass: IrClass, name: Name) = klass.functions.single { it.name == name }
    private fun property(klass: IrClass, name: String): IrProperty? = xamlIrProperty(klass, name)
    private fun functions(klass: IrClass, name: String): List<IrSimpleFunction> = xamlIrFunctions(klass, name)

    private fun emitBinding(builder: IrStatementsBuilder<*>, klass: IrClass, page: WinRTXamlPageDeclaration,
        connection: WinRTXamlConnectionDeclaration, binding: WinRTXamlBindingDeclaration,
        root: () -> IrExpression, converterRoot: () -> IrExpression, target: () -> IrExpression,
        state: () -> IrExpression, observerClass: IrClass, observer: () -> IrExpression,
        changed: IrSimpleFunction, initial: IrExpression, back: IrSimpleFunction? = null,
        namedTargets: ((String, IrBuilderWithScope) -> IrExpression?)? = null,
        backDelegate: ((IrBuilderWithScope, IrType) -> IrExpression)? = null,
        targetPresent: ((IrBuilderWithScope) -> IrExpression)? = null,
        loadAssignment: ((IrBuilderWithScope, IrExpression) -> IrExpression)? = null,
    ) = with(builder) {
        val location = "${page.resourcePath}:${binding.location.line}:${binding.location.column}"
        val owner = projection(binding.declaringTypeName)
        val staticOwner = if (owner.kind == ClassKind.OBJECT) owner else owner.companionObject()
        val setter = if (binding.isLoad) null else if (binding.isAttachable)
            functions(requireNotNull(staticOwner), "Set${binding.name}").single()
        else requireNotNull(property(owner, binding.name)?.setter) {
            "$location: no setter for ${binding.declaringTypeName}.${binding.name}"
        }
        val targetType = if (binding.isLoad) pluginContext.irBuiltIns.booleanType else requireNotNull(setter).parameters.last { it.kind == IrParameterKind.Regular }.type
        val body = irBlock {
            val stateClass = requireNotNull(pluginContext.referenceClass(xamlBindingStateId)).owner
            +irCall(stateClass.functions.single { it.name.asString() == "trackPhase" }).apply {
                dispatchReceiver = state(); arguments[1] = irInt(binding.phase)
            }
            val source = irTemporary(evaluate(binding.expression, this, klass, root, targetType, namedTargets))
            val alternate = binding.targetNullValue ?: binding.fallbackValue
            val converted = if (binding.converter == null) irGet(source) else
                converter(this, klass, converterRoot(), binding, irGet(source), targetType)
            val value = if (alternate != null && source.type.isNullable()) irIfThenElse(targetType,
                irEquals(irGet(source), irNull(source.type)),
                convert(evaluate(alternate, this, klass, root, targetType, namedTargets), targetType, this, location),
                convert(converted, targetType, this, location)) else convert(converted, targetType, this, location)
            val skipNull = source.type.isNullable() && !targetType.isNullable() && alternate == null && binding.converter == null
            val assignedValue = irTemporary(if (skipNull) irGet(source) else value)
            fun IrBuilderWithScope.assignment(): IrExpression = if (binding.isLoad)
                requireNotNull(loadAssignment)(this, irGet(assignedValue)) else irCall(requireNotNull(setter).symbol).apply {
                    if (binding.isAttachable) {
                        dispatchReceiver = irGetObject(requireNotNull(staticOwner).symbol)
                        arguments[1] = target(); arguments[2] = convert(irGet(assignedValue), targetType, this@assignment, location)
                    } else { dispatchReceiver = target(); arguments[1] = convert(irGet(assignedValue), targetType, this@assignment, location) }
            }
            val assignment = if (targetPresent == null || binding.isLoad) assignment() else irIfThenElse(
                pluginContext.irBuiltIns.unitType, targetPresent(this), assignment(),
                irCall(stateClass.functions.single { it.name.asString() == "defer" }).apply {
                    dispatchReceiver = state(); arguments[1] = irInt(connection.id); arguments[2] = irString(binding.name)
                    arguments[3] = lambda(this@irBlock, emptyList()) { nested, _ -> nested.assignment() }
                })
            if (skipNull)
                +irIfThen(pluginContext.irBuiltIns.unitType, irNotEquals(irGet(source), irNull(source.type)), assignment)
            else +assignment
            if (binding.mode != "OneTime") {
                for (expression in members(binding.expression)) {
                    val watched = evaluate(requireNotNull(expression.receiver), this, klass, root, namedTargets = namedTargets)
                    val sourceClass = watched.type.classOrNull?.owner ?: continue
                    val sourceObject = irTemporary(watched)
                    val dp = dependencyProperty(sourceClass, requireNotNull(expression.name))
                    val notifier = projection("Microsoft.UI.Xaml.Data.INotifyPropertyChanged")
                    val subscribe = if (dp != null) subscribeProperty(this, observerClass, observer(),
                        state(), irGet(sourceObject), dp, changed)
                    else if (sourceClass.defaultType.isSubtypeOfClass(notifier.symbol)) subscribeEvent(this, observerClass,
                        observer(), state(), irGet(sourceObject), notifier, "PropertyChanged", changed) else continue
                    if (sourceObject.type.isNullable()) +irIfThen(pluginContext.irBuiltIns.unitType,
                        irNotEquals(irGet(sourceObject), irNull(sourceObject.type)), subscribe)
                    else +subscribe
                }
                if (binding.mode == "TwoWay") +irBlock {
                    val targetObject = irTemporary(target())
                    val dp = dependencyProperty(owner, binding.name)
                    if (binding.updateSourceTrigger == "LostFocus") +subscribeEvent(this, observerClass, observer(),
                        state(), irGet(targetObject), projection("Microsoft.UI.Xaml.UIElement"), "LostFocus", back ?: changed, backDelegate)
                    else {
                        requireNotNull(dp) { "$location: TwoWay target ${binding.name} requires a dependency property or LostFocus trigger" }
                        +subscribeProperty(this, observerClass, observer(), state(), irGet(targetObject), dp, back ?: changed, backDelegate)
                    }
                }.let { subscriptions -> if (targetPresent == null) subscriptions else
                    irIfThen(pluginContext.irBuiltIns.unitType, targetPresent(this), subscriptions) }
            }
            +irUnit()
        }
        if (binding.mode == "OneTime") +irIfThen(pluginContext.irBuiltIns.unitType, initial, body) else +body
    }

    private fun generateTemplateScopes(klass: IrClass, page: WinRTXamlPageDeclaration) {
        val scopeClass = requireNotNull(pluginContext.referenceClass(xamlBindingScopeId)).owner
        val scopeId = requireNotNull(property(scopeClass, "scopeId")?.getter)
        val dataRoot = requireNotNull(property(scopeClass, "dataRoot")?.getter)
        val bindingState = requireNotNull(property(scopeClass, "state")?.getter)
        val changed = scopeClass.functions.single { it.name.asString() == "changed" }
        val getTarget = scopeClass.functions.single { it.name.asString() == "target" }
        val getOptionalTarget = scopeClass.functions.single { it.name.asString() == "targetOrNull" }
        val isPhaseActive = scopeClass.functions.single { it.name.asString() == "isPhaseActive" }
        val registerPhase = scopeClass.functions.single { it.name.asString() == "registerPhase" }
        val groups = page.connections.filter { it.isTemplateChild }.groupBy { it.scopeId }
            .filterValues { it.any { connection -> connection.isScopeRoot } }
        val writeBackIds = groups.values.flatten().flatMap { connection ->
            connection.bindings.filter { it.mode == "TwoWay" && !it.isEvent }.map { connection to it }
        }.sortedWith(compareBy({ it.first.id }, { it.second.name })).withIndex()
            .associate { (index, pair) -> (pair.first.id to pair.second.name) to index + 1 }

        fun IrBuilderWithScope.source(scope: IrValueParameter, connections: List<WinRTXamlConnectionDeclaration>): IrExpression {
            val typeName = connections.first { it.isScopeRoot }.dataTypeName ?: "System.Object"
            val value = irCall(dataRoot.symbol).apply { dispatchReceiver = irGet(scope) }
            return convert(value, projection(typeName).defaultType, this, page.resourcePath)
        }
        fun IrBuilderWithScope.target(scope: IrValueParameter, connection: WinRTXamlConnectionDeclaration): IrExpression =
            convert(irCall(getTarget.symbol).apply {
                dispatchReceiver = irGet(scope); arguments[1] = irInt(connection.id)
            }, projection(connection.typeName).defaultType, this, page.resourcePath)

        val update = method(klass, xamlScopeUpdateName)
        update.body = DeclarationIrBuilder(pluginContext, update.symbol).irBlockBody {
            val scope = update.parameters.first { it.kind == IrParameterKind.Regular }
            val initial = update.parameters.last { it.kind == IrParameterKind.Regular }
            +irWhen(pluginContext.irBuiltIns.unitType, mutableListOf()).apply {
                for ((id, connections) in groups) branches += irBranch(irEquals(irCall(scopeId.symbol).apply {
                    dispatchReceiver = irGet(scope)
                }, irInt(id)), irBlock {
                    val named: (String, IrBuilderWithScope) -> IrExpression? = { name, builder ->
                        connections.firstOrNull { it.elementName == name || it.fieldName == name }?.let { builder.target(scope, it) }
                    }
                    for (connection in connections) for (binding in connection.bindings.filterNot { it.isEvent }.sortedByDescending { it.isLoad }) {
                        +irIfThen(pluginContext.irBuiltIns.unitType, irCall(isPhaseActive.symbol).apply {
                            dispatchReceiver = irGet(scope); arguments[1] = irInt(binding.phase)
                        }, irBlock { emitBinding(this, klass, page, connection, binding,
                            { source(scope, connections) }, { irGet(requireNotNull(update.dispatchReceiverParameter)) },
                            { target(scope, connection) },
                            { irCall(bindingState.symbol).apply { dispatchReceiver = irGet(scope) } },
                            scopeClass, { irGet(scope) }, changed, irGet(initial), namedTargets = named,
                            backDelegate = if (binding.mode != "TwoWay") null else { builder, delegateType -> with(builder) {
                                irSamConversion(irCall(runtime("weakXamlBindingScopeWriteBack")).apply {
                                    arguments[0] = irGet(scope)
                                    arguments[1] = irInt(writeBackIds.getValue(connection.id to binding.name))
                                }, delegateType)
                            } },
                            targetPresent = if (!connection.canBeInstantiatedLater) null else { nested -> with(nested) {
                                irNotEquals(irCall(getOptionalTarget.symbol).apply { dispatchReceiver = irGet(scope); arguments[1] = irInt(connection.id) }, irNull())
                            } },
                            loadAssignment = { nested, value -> loadElement(nested, connection, value,
                                { nested.target(scope, connections.first { it.isScopeRoot }) },
                                { nested.irCall(getOptionalTarget.symbol).apply { dispatchReceiver = nested.irGet(scope); arguments[1] = nested.irInt(connection.id) } }) { cleanup ->
                                for (id in connection.children + connection.id) with(cleanup) {
                                    +irCall(scopeClass.functions.single { it.name.asString() == "disconnect" }).apply {
                                        dispatchReceiver = irGet(scope); arguments[1] = irInt(id)
                                    }
                                }
                            } })
                            +irUnit()
                        })
                    }
                    +irUnit()
                })
            }
        }
        val writeBack = method(klass, xamlScopeWriteBackName)
        writeBack.body = DeclarationIrBuilder(pluginContext, writeBack.symbol).irBlockBody {
            val parameters = writeBack.parameters.filter { it.kind == IrParameterKind.Regular }
            val scope = parameters[0]
            +irWhen(pluginContext.irBuiltIns.unitType, mutableListOf()).apply {
                for ((_, connections) in groups) for (connection in connections)
                    for (binding in connection.bindings.filter { it.mode == "TwoWay" && !it.isEvent }) {
                        branches += irBranch(irEquals(irGet(parameters[1]), irInt(writeBackIds.getValue(connection.id to binding.name))), irBlock {
                            val getter = requireNotNull(property(projection(binding.declaringTypeName), binding.name)?.getter)
                            val value = irCall(getter.symbol).apply { dispatchReceiver = target(scope, connection) }
                            +writeBack(binding, value, this, klass, { source(scope, connections) },
                                converterRoot = { irGet(requireNotNull(writeBack.dispatchReceiverParameter)) },
                                namedTargets = { name, builder -> connections.firstOrNull { it.elementName == name }?.let { builder.target(scope, it) } })
                            +irUnit()
                        })
                    }
            }
        }
        val connect = method(klass, xamlScopeConnectName)
        connect.body = DeclarationIrBuilder(pluginContext, connect.symbol).irBlockBody {
            val parameters = connect.parameters.filter { it.kind == IrParameterKind.Regular }
            +irCall(method(klass, xamlConnectName).symbol).apply {
                dispatchReceiver = irGet(requireNotNull(connect.dispatchReceiverParameter))
                arguments[1] = irGet(parameters[1]); arguments[2] = irGet(parameters[2])
            }
            for ((id, connections) in groups) +irIfThen(pluginContext.irBuiltIns.unitType,
                irEquals(irGet(parameters[1]), irInt(id)), irBlock {
                    for (connection in connections.filter { it.phase != 0 }) +irCall(registerPhase.symbol).apply {
                        dispatchReceiver = irGet(parameters[0]); arguments[1] = irInt(connection.id); arguments[2] = irInt(connection.phase)
                    }
                    +irUnit()
                })
            +irWhen(pluginContext.irBuiltIns.unitType, mutableListOf()).apply {
                for (connections in groups.values) for (connection in connections.filter { it.bindings.any { it.isEvent } }) {
                    branches += irBranch(irEquals(irGet(parameters[1]), irInt(connection.id)), irBlock {
                        for (binding in connection.bindings.filter { it.isEvent }) {
                            +eventSubscription(this, klass, binding, irGet(requireNotNull(connect.dispatchReceiverParameter)),
                                target(parameters[0], connection), irGet(parameters[0]),
                                projection(connections.first { it.isScopeRoot }.dataTypeName ?: "System.Object").defaultType)
                        }
                        +irUnit()
                    })
                }
            }
        }
        val create = method(klass, xamlScopeCreateName)
        create.body = DeclarationIrBuilder(pluginContext, create.symbol).irBlockBody {
            val parameters = create.parameters.filter { it.kind == IrParameterKind.Regular }
            +irReturn(irCall(method(klass, xamlBindingName).symbol).apply {
                dispatchReceiver = irGet(requireNotNull(create.dispatchReceiverParameter))
                arguments[1] = irGet(parameters[0]); arguments[2] = irGet(parameters[1])
            })
        }
    }

    fun generate(klass: IrClass, page: WinRTXamlPageDeclaration, properties: Map<String, IrProperty>) {
        val bindings = page.connections.filter { !it.isTemplateChild && it.bindings.isNotEmpty() }
        val state = requireNotNull(properties.getValue(xamlBindingStateName.asString()).backingField)
        val stateClass = requireNotNull(pluginContext.referenceClass(xamlBindingStateId)).owner
        val update = method(klass, xamlUpdateBindingsName)
        update.body = DeclarationIrBuilder(pluginContext, update.symbol).irBlockBody {
            val receiver = requireNotNull(update.dispatchReceiverParameter)
            val initial = update.parameters.single { it.kind == IrParameterKind.Regular }
            for (connection in bindings) for (binding in connection.bindings.filterNot { it.isEvent }.sortedByDescending { it.isLoad }) {
                emitBinding(this, klass, page, connection, binding,
                    { irGet(receiver) }, { irGet(receiver) },
                    { irCall(requireNotNull(properties.getValue(requireNotNull(connection.storageName())).getter).symbol).apply { dispatchReceiver = irGet(receiver) } },
                    { irGetField(irGet(receiver), state) }, klass, { irGet(receiver) }, method(klass, xamlBindingsChangedName),
                    irGet(initial),
                    if (binding.mode == "TwoWay") method(klass, binding.bindBackName(connection)) else null,
                    targetPresent = if (!connection.canBeInstantiatedLater) null else { nested -> with(nested) {
                        val field = requireNotNull(properties.getValue(requireNotNull(connection.storageName())).backingField)
                        irNotEquals(irGetField(irGet(receiver), field), irNull(field.type))
                    } },
                    loadAssignment = { nested, value -> loadElement(nested, connection, value,
                        { nested.irGet(receiver) },
                        { nested.irGetField(nested.irGet(receiver), requireNotNull(properties.getValue(requireNotNull(connection.storageName())).backingField)) }) { cleanup ->
                        for (element in page.connections.filter { it.id == connection.id || it.id in connection.children }) {
                            element.storageName()?.let { name -> with(cleanup) {
                                val field = requireNotNull(properties.getValue(name).backingField)
                                +irSetField(irGet(receiver), field, irNull(field.type))
                            } }
                        }
                    } })
            }
        }

        fun lifecycle(name: Name, stateMethod: String) {
            val function = method(klass, name)
            function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
                +irCall(stateClass.functions.single { it.name.asString() == stateMethod }).apply {
                    val receiver = requireNotNull(function.dispatchReceiverParameter)
                    dispatchReceiver = irGetField(irGet(receiver), state)
                    if (stateMethod != "stopTracking") arguments[1] = xamlFunctionReference(pluginContext, update, irGet(receiver))
                }
            }
        }
        lifecycle(xamlBindingsLoadingName, "initialize")
        lifecycle(xamlBindingsChangedName, "update")
        lifecycle(xamlBindingsUnloadedName, "stopTracking")
        lifecycle(xamlRefreshBindingsName, "updateAll")
        for (connection in bindings) for (binding in connection.bindings.filter { it.mode == "TwoWay" }) {
            val callback = method(klass, binding.bindBackName(connection))
            callback.body = DeclarationIrBuilder(pluginContext, callback.symbol).irBlockBody {
                val receiver = requireNotNull(callback.dispatchReceiverParameter)
                val target = properties.getValue(requireNotNull(connection.storageName()))
                val getter = requireNotNull(property(projection(binding.declaringTypeName), binding.name)?.getter)
                val value = irCall(getter.symbol).apply {
                    dispatchReceiver = irCall(requireNotNull(target.getter).symbol).apply { dispatchReceiver = irGet(receiver) }
                }
                val change = lambda(this, emptyList()) { builder, _ ->
                    writeBack(binding, value, builder, klass, { builder.irGet(receiver) })
                }
                +irCall(stateClass.functions.single { it.name.asString() == "changeTarget" }).apply {
                    dispatchReceiver = irGetField(irGet(receiver), state)
                    arguments[1] = change; arguments[2] = xamlFunctionReference(pluginContext, update, irGet(receiver))
                }
            }
        }
        if (page.hasTemplateScopes()) generateTemplateScopes(klass, page)
    }

    private fun loadElement(builder: IrBuilderWithScope, connection: WinRTXamlConnectionDeclaration,
        value: IrExpression, root: () -> IrExpression, current: () -> IrExpression,
        disconnect: (IrStatementsBuilder<*>) -> Unit): IrExpression = with(builder) {
        val framework = projection("Microsoft.UI.Xaml.FrameworkElement")
        val helper = requireNotNull(projection("Microsoft.UI.Xaml.Markup.XamlMarkupHelper").companionObject())
        val dependencyObject = projection("Microsoft.UI.Xaml.DependencyObject")
        irIfThenElse(pluginContext.irBuiltIns.unitType, value,
            irBlock {
                +irCall(framework.functions.single { it.name.asString() == "findName" }).apply {
                    dispatchReceiver = root(); arguments[1] = irString(requireNotNull(connection.elementName))
                }
                +irUnit()
            }, irBlock {
                val element = irTemporary(current())
                disconnect(this)
                +irIfThen(pluginContext.irBuiltIns.unitType, irNotEquals(irGet(element), irNull(element.type)),
                    irCall(helper.functions.single { it.name.asString() == "unloadObject" }).apply {
                        dispatchReceiver = irGetObject(helper.symbol)
                        arguments[1] = convert(irGet(element), dependencyObject.defaultType, this@irBlock, "x:Load")
                    })
                +irUnit()
            })
    }

    fun lifecycleSubscription(builder: IrBuilderWithScope, klass: IrClass, page: WinRTXamlPageDeclaration,
        callback: IrSimpleFunction, receiver: IrExpression, loading: Boolean): IrExpression = with(builder) {
        val window = klass.defaultType.isSubtypeOfClass(projection("Microsoft.UI.Xaml.Window").symbol)
        val owner = projection(if (window) "Microsoft.UI.Xaml.Window" else "Microsoft.UI.Xaml.FrameworkElement")
        val event = if (window) { if (loading) "Activated" else "Closed" } else { if (loading) "Loading" else "Unloaded" }
        val add = owner.functions.single { it.name.asString() == "add$event" }
        irCall(add.symbol).apply {
            dispatchReceiver = receiver
            arguments[1] = weakCallback(builder, klass, callback, receiver, add.parameters.single { it.kind == IrParameterKind.Regular }.type)
        }
    }

    private fun weakCallback(builder: IrBuilderWithScope, klass: IrClass, callback: IrSimpleFunction,
        receiver: IrExpression, delegateType: IrType): IrExpression = with(builder) {
        irSamConversion(irCall(runtime("weakXamlBindingCallback")).apply {
            typeArguments[0] = klass.defaultType
            typeArguments[1] = pluginContext.irBuiltIns.anyNType
            typeArguments[2] = pluginContext.irBuiltIns.anyNType
            type = pluginContext.irBuiltIns.functionN(2).symbol.typeWith(pluginContext.irBuiltIns.anyNType, pluginContext.irBuiltIns.anyNType, pluginContext.irBuiltIns.unitType)
            arguments[0] = receiver
            arguments[1] = xamlFunctionReference(pluginContext, callback)
        }, delegateType)
    }

    private fun dependencyProperty(klass: IrClass, name: String, visited: MutableSet<IrClass> = mutableSetOf()): IrSimpleFunction? {
        if (!visited.add(klass)) return null
        (if (klass.kind == ClassKind.OBJECT) klass else klass.companionObject())?.let { companion ->
            (property(companion, "${name}Property") ?: property(companion, "${name.replaceFirstChar(Char::lowercase)}Property"))?.getter?.let { return it }
        }
        return klass.superTypes.mapNotNull { it.classOrNull?.owner }.firstNotNullOfOrNull { dependencyProperty(it, name, visited) }
    }

    private fun subscribeProperty(builder: IrBuilderWithScope, klass: IrClass, page: IrExpression,
        state: IrExpression, source: IrExpression, property: IrSimpleFunction,
        changed: IrSimpleFunction = method(klass, xamlBindingsChangedName),
        callback: ((IrBuilderWithScope, IrType) -> IrExpression)? = null): IrExpression = with(builder) {
        val dependencyObject = projection("Microsoft.UI.Xaml.DependencyObject")
        val dependencyProperty = projection("Microsoft.UI.Xaml.DependencyProperty")
        val register = dependencyObject.functions.single { it.name.asString() == "registerPropertyChangedCallback" }
        val unregister = dependencyObject.functions.single { it.name.asString() == "unregisterPropertyChangedCallback" }
        val stateClass = requireNotNull(pluginContext.referenceClass(xamlBindingStateId)).owner
        irBlock {
            val dp = irTemporary(irCall(property.symbol).apply { dispatchReceiver = irGetObject(requireNotNull(property.parentClassOrNull).symbol) })
            val delegateType = register.parameters.filter { it.kind == IrParameterKind.Regular }[1].type
            val handler = callback?.invoke(this, delegateType) ?: weakCallback(this, klass, changed, page, delegateType)
            val token = irTemporary(irCall(register.symbol).apply {
                dispatchReceiver = irImplicitCast(source, dependencyObject.defaultType)
                arguments[1] = irGet(dp); arguments[2] = handler
            })
            +irCall(stateClass.functions.single { it.name.asString() == "trackProperty" }).apply {
                dispatchReceiver = state
                typeArguments[0] = dependencyObject.defaultType; typeArguments[1] = dependencyProperty.defaultType
                arguments[1] = irImplicitCast(source, dependencyObject.defaultType)
                arguments[2] = irGet(dp); arguments[3] = irGet(token)
                arguments[4] = xamlFunctionReference(pluginContext, unregister)
            }
        }
    }

    private fun subscribeEvent(builder: IrBuilderWithScope, klass: IrClass, page: IrExpression,
        state: IrExpression, source: IrExpression, owner: IrClass, event: String,
        changed: IrSimpleFunction, callback: ((IrBuilderWithScope, IrType) -> IrExpression)? = null): IrExpression = with(builder) {
        val add = owner.functions.single { it.name.asString() == "add$event" }
        val remove = owner.functions.single { it.name.asString() == "remove$event" }
        val delegateType = add.parameters.single { it.kind == IrParameterKind.Regular }.type
        irBlock {
            val handler = irTemporary(callback?.invoke(this, delegateType) ?: weakCallback(this, klass, changed, page, delegateType))
            +irCall(add.symbol).apply { dispatchReceiver = source; arguments[1] = irGet(handler) }
            +irCall(requireNotNull(pluginContext.referenceClass(xamlBindingStateId)).owner.functions.single {
                it.name.asString() == "trackEvent" }).apply {
                dispatchReceiver = state
                typeArguments[0] = owner.defaultType; typeArguments[1] = delegateType
                arguments[1] = irImplicitCast(source, owner.defaultType); arguments[2] = irGet(handler)
                arguments[3] = xamlFunctionReference(pluginContext, remove)
            }
        }
    }

    private fun lambda(builder: IrBuilderWithScope, parameterTypes: List<IrType>,
        body: (IrBuilderWithScope, List<IrValueParameter>) -> IrExpression): IrExpression {
        val function = pluginContext.irFactory.buildFun {
            name = SpecialNames.ANONYMOUS
            visibility = DescriptorVisibilities.LOCAL
            returnType = pluginContext.irBuiltIns.unitType
            origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        }.apply { parent = builder.scope.scopeOwnerSymbol.owner as IrDeclarationParent }
        val parameters = parameterTypes.mapIndexed { index, type -> function.addValueParameter("p$index", type) }
        function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            +body(this, parameters)
        }
        return IrFunctionExpressionImpl(builder.startOffset, builder.endOffset,
            pluginContext.irBuiltIns.functionN(parameterTypes.size).symbol.typeWith(parameterTypes + pluginContext.irBuiltIns.unitType),
            function, IrStatementOrigin.LAMBDA)
    }

    fun eventSubscription(builder: IrBuilderWithScope, klass: IrClass, binding: WinRTXamlBindingDeclaration,
        receiver: IrExpression, target: IrExpression, scope: IrExpression? = null,
        dataType: IrType? = null): IrExpression = with(builder) {
        val add = projection(binding.declaringTypeName).functions.single { it.name.asString() == "add${binding.name}" }
        val delegateType = add.parameters.single { it.kind == IrParameterKind.Regular }.type
        val delegateClass = requireNotNull(delegateType.classOrNull).owner
        val invoke = delegateClass.functions.single { it.name.asString() == "invoke" }
        val types = invoke.parameters.filter { it.kind == IrParameterKind.Regular }.map { parameter ->
            val typeParameter = delegateClass.typeParameters.indexOfFirst { it.symbol == (parameter.type as? IrSimpleType)?.classifier }
            (delegateType as? IrSimpleType)?.arguments?.getOrNull(typeParameter)?.typeOrNull ?: parameter.type
        }
        require(types.size == 2) { "Compiled event binding ${binding.name} requires a two-parameter WinRT delegate" }
        val callbackClass = if (scope == null) klass else requireNotNull(pluginContext.referenceClass(xamlBindingScopeId)).owner
        val dispatch = lambda(builder, listOf(callbackClass.defaultType) + types) { body, parameters ->
            val path = binding.expression
            require(path.kind == "member") { "Compiled event ${binding.name} requires a method path" }
            val source = evaluate(requireNotNull(path.receiver), body, klass, {
                if (scope == null) body.irGet(parameters[0]) else convert(body.irCall(requireNotNull(property(callbackClass, "dataRoot")?.getter).symbol).apply {
                    dispatchReceiver = body.irGet(parameters[0])
                }, requireNotNull(dataType), body, binding.name)
            })
            val functions = functions(requireNotNull(source.type.classOrNull).owner, requireNotNull(path.name))
            val handler = functions.singleOrNull { it.parameters.count { p -> p.kind == IrParameterKind.Regular } == types.size }
                ?: functions.singleOrNull { it.parameters.none { p -> p.kind == IrParameterKind.Regular } }
                ?: error("Compiled event ${binding.name}: missing or ambiguous method ${path.name}")
            nullableCall(source, pluginContext.irBuiltIns.unitType, body) { objectValue -> body.irCall(handler.symbol).apply {
                dispatchReceiver = objectValue
                handler.parameters.filter { it.kind == IrParameterKind.Regular }.forEachIndexed { index, parameter ->
                    arguments[index + 1] = convert(body.irGet(parameters[index + 1]), parameter.type, body, binding.name)
                }
            } }
        }
        val handler = irSamConversion(irCall(runtime("weakXamlBindingCallback")).apply {
            typeArguments[0] = callbackClass.defaultType; typeArguments[1] = types[0]; typeArguments[2] = types[1]
            type = pluginContext.irBuiltIns.functionN(2).symbol.typeWith(types + pluginContext.irBuiltIns.unitType)
            arguments[0] = scope ?: receiver; arguments[1] = dispatch
        }, delegateType)
        irCall(add.symbol).apply { dispatchReceiver = target; arguments[1] = handler }
    }

    private fun converter(builder: IrBuilderWithScope, klass: IrClass, receiver: IrExpression,
        binding: WinRTXamlBindingDeclaration, value: IrExpression, targetType: IrType,
        back: Boolean = false): IrExpression = with(builder) {
        val framework = projection("Microsoft.UI.Xaml.FrameworkElement")
        val resources = requireNotNull(property(framework, "Resources")?.getter)
        val dictionary = projection("Microsoft.UI.Xaml.ResourceDictionary")
        // ResourceDictionary is projected as MutableMap, as in CsWinRT's
        // IDictionary mapping. Its Kotlin surface uses get/containsKey.
        val lookup = dictionary.functions.single { it.name.asString() == "get" }
        val hasKey = dictionary.functions.single { it.name.asString() == "containsKey" }
        val converters = projection("Microsoft.UI.Xaml.Data.IValueConverter")
        val call = converters.functions.single { it.name.asString() == if (back) "convertBack" else "convert" }
        irBlock(resultType = call.returnType) {
            val local = irTemporary(irCall(resources.symbol).apply { dispatchReceiver = receiver })
            val application = requireNotNull(projection("Microsoft.UI.Xaml.Application").companionObject())
            val current = requireNotNull(property(application, "Current")?.getter)
            val appResources = requireNotNull(property(projection("Microsoft.UI.Xaml.Application"), "Resources")?.getter)
            val found = irTemporary(irIfThenElse(pluginContext.irBuiltIns.anyNType,
                irCall(hasKey.symbol).apply { dispatchReceiver = irGet(local); arguments[1] = irString(requireNotNull(binding.converter)) },
                irCall(lookup.symbol).apply { dispatchReceiver = irGet(local); arguments[1] = irString(requireNotNull(binding.converter)) },
                irCall(lookup.symbol).apply {
                    dispatchReceiver = irCall(appResources.symbol).apply {
                        dispatchReceiver = irCall(current.symbol).apply { dispatchReceiver = irGetObject(application.symbol) }
                    }
                    arguments[1] = irString(requireNotNull(binding.converter))
                }))
            +irCall(call.symbol).apply {
                dispatchReceiver = convert(irGet(found), converters.defaultType, this@with, binding.name)
                arguments[1] = value
                arguments[2] = IrClassReferenceImpl(startOffset, endOffset,
                    pluginContext.irBuiltIns.kClassClass.starProjectedType, requireNotNull(targetType.classOrNull), targetType.makeNotNull())
                arguments[3] = binding.converterParameter?.let(::irString) ?: irNull(pluginContext.irBuiltIns.anyNType)
                arguments[4] = irString(binding.converterLanguage.orEmpty())
            }
        }
    }

    private fun writeBack(binding: WinRTXamlBindingDeclaration, target: IrExpression, builder: IrBuilderWithScope,
        klass: IrClass, root: () -> IrExpression, converterRoot: () -> IrExpression = root,
        namedTargets: ((String, IrBuilderWithScope) -> IrExpression?)? = null): IrExpression = with(builder) {
        val path = binding.bindBack ?: binding.expression
        val expression = requireNotNull(path.receiver) { "TwoWay ${binding.name} requires a writable path" }
        val receiver = evaluate(expression, builder, klass, root, namedTargets = namedTargets)
        val owner = requireNotNull(receiver.type.classOrNull).owner
        val setter = if (binding.bindBack != null) owner.functions.single {
            it.name.asString() == path.name && it.parameters.count { p -> p.kind == IrParameterKind.Regular } == 1
        } else requireNotNull(property(owner, requireNotNull(path.name))?.setter) { "TwoWay ${binding.name}: ${path.name} is read-only" }
        val expected = setter.parameters.single { it.kind == IrParameterKind.Regular }.type
        val value = if (binding.converter == null) target else converter(builder, klass, converterRoot(), binding, target, expected, back = true)
        nullableCall(receiver, pluginContext.irBuiltIns.unitType, builder) { objectValue -> irCall(setter.symbol).apply {
            dispatchReceiver = objectValue; arguments[1] = convert(value, expected, builder, binding.name)
        } }
    }

    private fun members(expression: WinRTXamlBindingExpression): List<WinRTXamlBindingExpression> =
        (if (expression.kind == "member") listOf(expression) else emptyList()) +
            expression.receiver?.let(::members).orEmpty() + expression.arguments.flatMap(::members)

    private fun evaluate(expression: WinRTXamlBindingExpression, builder: IrBuilderWithScope, klass: IrClass,
        root: () -> IrExpression, expected: IrType? = null,
        namedTargets: ((String, IrBuilderWithScope) -> IrExpression?)? = null): IrExpression = with(builder) {
        when (expression.kind) {
            "root" -> root()
            "static" -> {
                val owner = projection(requireNotNull(expression.typeName))
                irGetObject((if (owner.kind == ClassKind.OBJECT) owner else requireNotNull(owner.companionObject())).symbol)
            }
            "member" -> {
                val receiver = evaluate(requireNotNull(expression.receiver), builder, klass, root, namedTargets = namedTargets)
                val getter = receiver.type.classOrNull?.owner?.let { property(it, requireNotNull(expression.name)) }?.getter
                val named = if (getter == null && expression.receiver?.kind == "root") namedTargets?.invoke(requireNotNull(expression.name), builder) else null
                if (named != null) named
                else if (getter == null && expression.name == "Value" && receiver.type.isNullable()) receiver
                else {
                    requireNotNull(getter) { "No compiled XAML property ${receiver.type.classFqName}.${expression.name}" }
                    nullableCall(receiver, getter.returnType, builder) { target -> irCall(getter.symbol).apply { dispatchReceiver = target } }
                }
            }
            "call", "index" -> {
                val receiver = evaluate(requireNotNull(expression.receiver), builder, klass, root, namedTargets = namedTargets)
                val name = if (expression.kind == "index") "get" else requireNotNull(expression.name).replaceFirstChar(Char::lowercase)
                val function = requireNotNull(receiver.type.classOrNull).owner.functions.filter {
                    (it.name.asString() == name || it.name.asString() == expression.name) &&
                        it.parameters.count { p -> p.kind == IrParameterKind.Regular } == expression.arguments.size
                }.singleOrNull() ?: error("Missing or ambiguous compiled XAML method ${receiver.type.classFqName}.$name")
                val parameters = function.parameters.filter { it.kind == IrParameterKind.Regular }
                nullableCall(receiver, function.returnType, builder) { target -> irCall(function.symbol).apply {
                    dispatchReceiver = target
                    expression.arguments.forEachIndexed { index, argument -> arguments[index + 1] =
                        convert(evaluate(argument, builder, klass, root, parameters[index].type, namedTargets), parameters[index].type, builder, name) }
                } }
            }
            "cast" -> {
                val target = projection(requireNotNull(expression.typeName)).defaultType
                val value = evaluate(requireNotNull(expression.receiver), builder, klass, root, namedTargets = namedTargets)
                if (winRTFundamentalTypeForName(requireNotNull(expression.typeName)) != null || expression.typeName in classes)
                    convert(value, target, builder, expression.typeName.orEmpty())
                else irCall(runtime("asWinRT")).apply { type = target; typeArguments[0] = target; arguments[0] = value }
            }
            "attached" -> {
                val owner = requireNotNull(projection(requireNotNull(expression.typeName)).companionObject())
                val getter = owner.functions.single { it.name.asString() == "get${expression.name}" }
                val receiver = evaluate(requireNotNull(expression.receiver), builder, klass, root, namedTargets = namedTargets)
                nullableCall(receiver, getter.returnType, builder) { target -> irCall(getter.symbol).apply {
                    dispatchReceiver = irGetObject(owner.symbol); arguments[1] = target
                } }
            }
            "literal" -> when {
                expression.typeName == "null" -> irNull(expected?.makeNullable() ?: pluginContext.irBuiltIns.anyNType)
                expression.typeName == "System.Boolean" || expected?.isBoolean() == true -> irBoolean(expression.value == "true")
                expression.typeName == "System.String" || expected?.isString() == true -> irString(expression.value.orEmpty())
                expected?.isInt() == true -> irInt(requireNotNull(expression.value).toInt())
                expected?.isLong() == true -> irLong(requireNotNull(expression.value).toLong())
                else -> IrConstImpl.double(startOffset, endOffset, pluginContext.irBuiltIns.doubleType, requireNotNull(expression.value).toDouble())
            }
            else -> error("Compiled XAML expression ${expression.kind} needs its capability slice")
        }
    }

    private fun nullableCall(receiver: IrExpression, resultType: IrType, builder: IrBuilderWithScope,
        call: (IrExpression) -> IrExpression): IrExpression = with(builder) {
        if (!receiver.type.isNullable()) call(receiver) else irBlock(resultType = resultType.makeNullable()) {
            val source = irTemporary(receiver)
            +irIfThenElse(resultType.makeNullable(), irNotEquals(irGet(source), irNull(source.type)),
                irImplicitCast(call(irImplicitCast(irGet(source), source.type.makeNotNull())), resultType.makeNullable()),
                irNull(resultType.makeNullable()))
        }
    }

    private fun convert(value: IrExpression, expected: IrType, builder: IrBuilderWithScope, location: String): IrExpression = with(builder) {
        if (value.type.classOrNull == expected.classOrNull || expected.isAny() || expected.isNullableAny()) return irImplicitCast(value, expected)
        val sourceType = value.type.classOrNull?.owner
        val conversionName = if (expected.isString()) "toString" else "to${expected.classOrNull?.owner?.name?.asString()}"
        val conversion = sourceType?.functions?.firstOrNull { it.name.asString() == conversionName &&
            it.parameters.none { parameter -> parameter.kind == IrParameterKind.Regular } && it.returnType.classOrNull == expected.classOrNull }
        if (conversion != null) return irCall(conversion.symbol).apply { dispatchReceiver = irImplicitCast(value, value.type.makeNotNull()) }
        if (value.type.isAny() || value.type.isNullableAny()) {
            if (expected.classOrNull?.owner?.kind == ClassKind.INTERFACE) return irCall(runtime("asWinRT")).apply {
                type = expected; typeArguments[0] = expected.makeNotNull(); arguments[0] = value
            }
            return irAs(value, expected)
        }
        if (expected.classOrNull?.let { value.type.isSubtypeOfClass(it) } == true) return irImplicitCast(value, expected)
        val helper = requireNotNull(projection("Microsoft.UI.Xaml.Markup.XamlBindingHelper").companionObject())
        val convertValue = helper.functions.single { it.name.asString() == "convertValue" }
        irAs(irCall(convertValue.symbol).apply {
            dispatchReceiver = irGetObject(helper.symbol)
            arguments[1] = IrClassReferenceImpl(startOffset, endOffset,
                pluginContext.irBuiltIns.kClassClass.starProjectedType, requireNotNull(expected.classOrNull), expected.makeNotNull())
            arguments[2] = value
        }, expected)
    }
}
