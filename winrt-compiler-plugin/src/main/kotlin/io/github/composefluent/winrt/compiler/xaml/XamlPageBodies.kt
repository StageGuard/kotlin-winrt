package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.WinRTXamlDeclarationIndex
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*

/** Bodies execute on the original page; no generated superclass or second COM identity. */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class XamlPageBodies(private val index: WinRTXamlDeclarationIndex, private val semanticOnly: Boolean) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val classes = moduleFragment.files.flatMap { it.declarations }.filterIsInstance<IrClass>()
            .associateBy { it.fqNameWhenAvailable?.asString() }
        for (page in index.pages) {
            val klass = requireNotNull(classes[page.className]) { "Missing Kotlin XAML class ${page.className}" }
            fun function(name: Name) = klass.declarations.filterIsInstance<IrSimpleFunction>().single { it.name == name }.also {
                check((it.origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == XamlDeclarationKey)
            }
            val properties = klass.declarations.filterIsInstance<IrProperty>().filter {
                (it.origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == XamlDeclarationKey
            }.associateBy { it.name.asString() }
            // FIR appends generated declarations. Keep instance state ahead of user initializers;
            // loading itself occurs only at the completed construction call boundary.
            klass.declarations.removeAll(properties.values.toSet())
            klass.declarations.addAll(0, properties.values)
            val functions = listOf(xamlInitializeName, xamlConstructionName, xamlLoadName, xamlConnectName, xamlBindingName).map(::function)
            if (semanticOnly) {
                val error = pluginContext.referenceFunctions(CallableId(FqName("kotlin"), Name.identifier("error"))).single()
                for (method in functions + properties.values.mapNotNull { it.getter }) {
                    method.body = DeclarationIrBuilder(pluginContext, method.symbol).irBlockBody {
                        +irCall(error).apply { arguments[0] = irString("XAML semantic-only artifact must not be executed") }
                    }
                }
                continue
            }
            fun runtime(name: String) = pluginContext.referenceFunctions(
                CallableId(FqName("io.github.composefluent.winrt.runtime"), Name.identifier(name))).single()
            fun projection(name: String) = requireNotNull(pluginContext.referenceClass(xamlProjectionClassId(name))) {
                "XAML requires generated projection $name"
            }.owner
            val requireElement = runtime("requireXamlNamedElement")
            for (connection in page.connections.filter { it.fieldName != null }) {
                val property = properties.getValue(connection.fieldName!!)
                val field = requireNotNull(property.backingField)
                val getter = requireNotNull(property.getter)
                field.type = getter.returnType.makeNullable()
                field.isFinal = false
                field.initializer = DeclarationIrBuilder(pluginContext, field.symbol).run { irExprBody(irNull(field.type)) }
                getter.body = DeclarationIrBuilder(pluginContext, getter.symbol).irBlockBody {
                    +irReturn(irCall(requireElement).apply {
                        type = getter.returnType
                        typeArguments[0] = getter.returnType
                        arguments[0] = irGetField(irGet(requireNotNull(getter.dispatchReceiverParameter)), field)
                        arguments[1] = irString(page.className); arguments[2] = irString(requireNotNull(connection.fieldName))
                    })
                }
            }
            val state = requireNotNull(properties.getValue(xamlStateName.asString()).backingField)
            val stateClass = requireNotNull(pluginContext.referenceClass(xamlStateId)).owner
            state.initializer = DeclarationIrBuilder(pluginContext, state.symbol).run {
                irExprBody(irCallConstructor(stateClass.constructors.single().symbol, emptyList()))
            }
            val stateGetter = requireNotNull(properties.getValue(xamlStateName.asString()).getter)
            stateGetter.body = DeclarationIrBuilder(pluginContext, stateGetter.symbol).irBlockBody {
                +irReturn(irGetField(irGet(requireNotNull(stateGetter.dispatchReceiverParameter)), state))
            }
            val constructionProperty = properties.getValue(xamlConstructionStateName.asString())
            val constructionState = requireNotNull(constructionProperty.backingField)
            constructionState.initializer = DeclarationIrBuilder(pluginContext, constructionState.symbol).run {
                irExprBody(irCallConstructor(stateClass.constructors.single().symbol, emptyList()))
            }
            val constructionGetter = requireNotNull(constructionProperty.getter)
            constructionGetter.body = DeclarationIrBuilder(pluginContext, constructionGetter.symbol).irBlockBody {
                +irReturn(irGetField(irGet(requireNotNull(constructionGetter.dispatchReceiverParameter)), constructionState))
            }
            val load = function(xamlLoadName)
            val applicationDefinitions = requireNotNull(pluginContext.referenceClass(ClassId.topLevel(
                FqName("io.github.composefluent.winrt.generated.xaml.KotlinXamlApplicationDefinitions")))) {
                "XAML application definitions were not generated before Kotlin compilation"
            }.owner
            val registerApplicationTypes = applicationDefinitions.functions.single { it.name.asString() == "registerAll" }
            val applicationMetadata = requireNotNull(projection("Microsoft.UI.Xaml.Application").companionObject())
            val loadComponent = applicationMetadata.functions.single { it.name.asString() == "loadComponent" &&
                it.parameters.count { p -> p.kind == IrParameterKind.Regular } == 2 }
            val uri = projection("Windows.Foundation.Uri").constructors.single {
                it.parameters.filter { p -> p.kind == IrParameterKind.Regular }.let { p ->
                    p.size == 1 && p[0].type.classFqName?.asString() == "kotlin.String"
                }
            }
            load.body = DeclarationIrBuilder(pluginContext, load.symbol).irBlockBody {
                +irCall(registerApplicationTypes.symbol).apply {
                    dispatchReceiver = irGetObject(applicationDefinitions.symbol)
                }
                +irCall(loadComponent.symbol).apply {
                    dispatchReceiver = irGetObject(applicationMetadata.symbol)
                    arguments[1] = irGet(requireNotNull(load.dispatchReceiverParameter))
                    arguments[2] = irCallConstructor(uri.symbol, emptyList()).apply {
                        arguments[0] = irString("ms-appx:///" + page.resourcePath)
                    }
                }
            }
            val initialize = function(xamlInitializeName)
            val stateLoad = stateClass.functions.single { it.name.asString() == "load" }
            initialize.body = DeclarationIrBuilder(pluginContext, initialize.symbol).irBlockBody {
                +irCall(stateLoad.symbol).apply {
                    dispatchReceiver = irGetField(irGet(requireNotNull(initialize.dispatchReceiverParameter)), state)
                    arguments[1] = boundReference(pluginContext, load, irGet(requireNotNull(initialize.dispatchReceiverParameter)))
                }
            }
            val complete = function(xamlConstructionName)
            val userInitialize = klass.functions.single { it.name.asString() == "initializeComponent" }
            complete.body = DeclarationIrBuilder(pluginContext, complete.symbol).irBlockBody {
                +irCall(stateLoad.symbol).apply {
                    dispatchReceiver = irGetField(irGet(requireNotNull(complete.dispatchReceiverParameter)), constructionState)
                    arguments[1] = boundReference(pluginContext, userInitialize, irGet(requireNotNull(complete.dispatchReceiverParameter)))
                }
            }
            val binding = function(xamlBindingName)
            check(binding.returnType.isNullable()) { "IComponentConnector projection must have nullable GetBindingConnector" }
            binding.body = DeclarationIrBuilder(pluginContext, binding.symbol).irBlockBody { +irReturn(irNull(binding.returnType)) }

            val connect = function(xamlConnectName)
            val parameters = connect.parameters.filter { it.kind == IrParameterKind.Regular }
            val cast = runtime("asWinRT")
            connect.body = DeclarationIrBuilder(pluginContext, connect.symbol).irBlockBody {
                +irWhen(pluginContext.irBuiltIns.unitType, mutableListOf()).apply {
                    for (connection in page.connections.filter { it.fieldName != null || it.events.isNotEmpty() }) {
                        branches += irBranch(irEquals(irGet(parameters[0]), irInt(connection.id)), irBlock {
                            val targetType = classes[connection.typeName]?.defaultType
                                ?: projection(connection.typeName).defaultType
                            val target = irTemporary(irCall(cast).apply {
                                type = targetType; typeArguments[0] = targetType; arguments[0] = irGet(parameters[1])
                            })
                            connection.fieldName?.let { fieldName ->
                                +irSetField(irGet(requireNotNull(connect.dispatchReceiverParameter)),
                                    requireNotNull(properties.getValue(fieldName).backingField), irGet(target))
                            }
                            for (event in connection.events) {
                                val handler = klass.functions.single { it.name.asString() == event.handlerName }
                                val add = projection(event.declaringTypeName).functions.single {
                                    it.name.asString() == "add${event.name}" && it.parameters.count { p -> p.kind == IrParameterKind.Regular } == 1
                                }
                                val delegateType = add.parameters.single { it.kind == IrParameterKind.Regular }.type
                                val delegateClass = requireNotNull(delegateType.classOrNull) {
                                    "XAML requires projected delegate ${event.delegateTypeName}"
                                }.owner
                                val invoke = delegateClass.functions.single { it.name.asString() == "invoke" }
                                val handlerParameters = handler.parameters.filter { it.kind == IrParameterKind.Regular }
                                val delegateParameters = invoke.parameters.filter { it.kind == IrParameterKind.Regular }
                                val typeArguments = (delegateType as? IrSimpleType)?.arguments.orEmpty()
                                fun delegateParameterClassName(type: IrType): FqName? {
                                    val parameterIndex = delegateClass.typeParameters.indexOfFirst { parameter ->
                                        parameter.symbol == (type as? IrSimpleType)?.classifier
                                    }
                                    return (typeArguments.getOrNull(parameterIndex)?.typeOrNull ?: type).classFqName
                                }
                                require(!handler.isSuspend && handler.typeParameters.isEmpty() &&
                                    handler.returnType.classFqName == invoke.returnType.classFqName &&
                                    handlerParameters.map { it.type.classFqName } ==
                                    delegateParameters.map { delegateParameterClassName(it.type) }) {
                                    "${page.resourcePath}:${event.location.line}:${event.location.column}: " +
                                        "handler ${event.handlerName} no longer matches ${event.delegateTypeName}; rebuild XAML semantic symbols"
                                }
                                +irCall(add.symbol).apply {
                                    dispatchReceiver = irGet(target)
                                    arguments[1] = irSamConversion(boundReference(pluginContext, handler,
                                        irGet(requireNotNull(connect.dispatchReceiverParameter))), delegateType)
                                }
                            }
                            +irUnit()
                        })
                    }
                }
            }
        }
    }

    private fun boundReference(context: IrPluginContext, function: IrSimpleFunction, receiver: IrExpression): IrExpression {
        val signature = function.parameters.filter { it.kind == IrParameterKind.Regular }.map { it.type } + function.returnType
        return IrFunctionReferenceImpl(function.startOffset, function.endOffset,
            context.irBuiltIns.functionN(signature.size - 1).symbol.typeWith(signature), function.symbol, 0).apply {
            dispatchReceiver = receiver
        }
    }
}
