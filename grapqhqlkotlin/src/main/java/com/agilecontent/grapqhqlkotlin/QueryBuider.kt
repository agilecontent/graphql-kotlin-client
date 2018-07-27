package com.agilecontent.grapqhqlkotlin

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class QueryBuilder {
    private val queryField = QueryField("query")
    fun query(block: QueryField.() -> Unit): QueryBuilder {
        block(queryField)
        return this
    }

    fun build(): String {
        return queryField.toString()
    }
}

class QueryField(val name: String, val args: Map<String, Any>? = null, val alias: String? = null) {
    private val objects = mutableListOf<QueryField>()
    private val scalars = mutableListOf<Any>()
    private var inlineFragment: String? = null

    fun on(destClass: KClass<*>, block: (QueryField.() -> Unit)? = null) {
        field("") {
            inlineFragment = destClass.simpleName
            block?.invoke(this)
        }
    }

    fun field(fieldProperty: KProperty<*>, fieldArgs: Map<String, Any>? = null, alias: String? = null, block: (QueryField.() -> Unit)? = null) =
            field(fieldProperty.name, fieldArgs, alias, block)

    fun field(fieldProperty: String, fieldArgs: Map<String, Any>? = null, alias: String? = null, block: (QueryField.() -> Unit)? = null) {
        if (block == null) {
            scalars.add(fieldProperty)
        } else {
            val sub = QueryField(fieldProperty, fieldArgs, alias)
            objects.add(sub)
            block.invoke(sub)
        }
    }

    fun paginatedField(fieldProperty: String, first: Int? = null, after: String? = null, last: Int? = null, before: String? = null, fieldArgs: Map<String, Any>? = null, alias: String? = null, block: (QueryField.() -> Unit)? = null) =
            field(fieldProperty, mutableMapOf<String, Any>().apply {
                first?.let { put("first", it) }
                after?.let { put("after", it) }
                last?.let { put("last", it) }
                before?.let { put("before", it) }
                fieldArgs?.forEach { put(it.key, it.value) }
            }.toMap(), alias) {
                field("edges") {
                    field("node", block = block)
                }
            }

    fun paginatedField(fieldProperty: KProperty<*>, first: Int? = null, after: String? = null, last: Int? = null, before: String? = null, fieldArgs: Map<String, Any>? = null, alias: String? = null, block: (QueryField.() -> Unit)? = null) =
            paginatedField(fieldProperty.name, first, after, last, before, fieldArgs, alias, block)


    private fun serialize(): String {
        val serializedObjects = objects.joinToString(", ") { it.toString() }
        val serializedScalars = scalars.joinToString(", ")
        val fields = listOf(serializedObjects, serializedScalars).filter { it.isNotEmpty() }
                .joinToString(", ")
        return "${serializeAlias()} ${serializeInlineFragment()} ${serializeArgs()} { $fields }"
    }

    private fun serializeArgs(): String {
        return if (args == null || args.isEmpty()) {
            name
        } else {
            val argsString = "(${args.map {
                "${it.key}:${
                it.value.let { value ->
                    if (value is String)
                        "\\\"$value\\\""
                    else
                        "$value"
                }
                }"
            }.joinToString(",")})"
            "$name $argsString"
        }
    }

    private fun serializeInlineFragment(): String = if (inlineFragment == null) "" else "... on $inlineFragment"

    private fun serializeAlias(): String = if (alias == null) "" else "$alias : "

    override fun toString(): String = serialize()
}
