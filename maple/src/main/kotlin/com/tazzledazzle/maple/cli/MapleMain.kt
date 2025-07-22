package com.tazzledazzle.maple.cli

fun main(rawArgs: Array<String>) {
    val args = rawArgs.toList()
    if (args.isEmpty()) {
        println("Usage: maple <command> [args]")
        println("Commands: fixtures, plan, run, resume (others TBD)")
        return
    }
    when (args[0]) {
        "fixtures" -> FixturesCommand.run(args.drop(1))
        else -> println("Unknown command ${args[0]}")
    }
}