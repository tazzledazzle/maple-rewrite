package com.tazzledazzle.maple.cli

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

/**
 * Thin wrapper that shells out to scripts for now.
 * Later we can replace with a pure Kotlin implementation.
 */
object FixturesCommand {
        fun run(args: List<String>) {
                if (args.isEmpty()) {
                        usageAndExit()
                    }
                when (args[0]) {
                        "init" -> init(args.drop(1))
                        "destroy" -> destroy(args.drop(1))
                        "list" -> list(args.drop(1))
                        else -> usageAndExit()
                    }
            }

        private fun init(args: List<String>) {
                // naive parse: mode default local
                val remote = argValue(args, "--remote") ?: "local"
                when (remote) {
                        "local" -> execScript("scripts/fixtures_local.sh", listOf("init") + args)
                        "github" -> execScript("scripts/fixtures_github.sh", listOf("init") + args)
                        else -> {
                                System.err.println("Unknown --remote $remote")
                                exitProcess(1)
                            }
                    }
            }

        private fun destroy(args: List<String>) {
                val remote = argValue(args, "--remote") ?: "local"
                when (remote) {
                        "local" -> execScript("scripts/fixtures_local.sh", listOf("destroy") + args)
                        "github" -> execScript("scripts/fixtures_github.sh", listOf("destroy") + args)
                        else -> {
                                System.err.println("Unknown --remote $remote")
                                exitProcess(1)
                            }
                    }
            }

        private fun list(args: List<String>) {
                // For now we just print the BOM path if present
                val root = argValue(args, "--root") ?: "/tmp/maple-fixtures"
                val bom = Path.of(root).resolve("fixtures-bom.json")
                if (bom.exists()) {
                        println(bom.toAbsolutePath())
                    } else {
                        println("No BOM found at $bom")
                    }
            }

        private fun argValue(args: List<String>, key: String): String? {
                val i = args.indexOf(key)
                return if (i >= 0 && i + 1 < args.size) args[i + 1] else null
            }

        private fun execScript(script: String, args: List<String>) {
                val pb = ProcessBuilder(listOf(script) + args)
                    .inheritIO()
                val p = pb.start()
                val code = p.waitFor()
                if (code != 0) exitProcess(code)
            }

        private fun usageAndExit(): Nothing {
                println("Usage: maple fixtures <init|destroy|list> [--remote local|github] [...]")
                exitProcess(1)
            }
    }