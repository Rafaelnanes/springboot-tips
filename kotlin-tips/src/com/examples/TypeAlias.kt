package com.examples

typealias NodeId = Int

class TypeAliasExample{

    fun myFunction(value:Int): List<Int>{
        return listOf(value)
    }

    fun usingAlias(value:NodeId): List<NodeId>{
        return listOf(value)
    }

}

fun main() {
    println(TypeAliasExample().myFunction(1) == TypeAliasExample().usingAlias(1))
}