package com.tazzledazzle.maple.cli

import com.tazzledazzle.internal.BomParser
import com.tazzledazzle.maple.build.ShellBuildRunner
import com.tazzledazzle.maple.git.ShellGitDriver
import com.tazzledazzle.maple.orchestrator.DefaultRepoExecutor
import com.tazzledazzle.maple.orchestrator.Orchestrator
import com.tazzledazzle.maple.orchestrator.RepoSpec
import com.tazzledazzle.maple.orchestrator.SqliteStateStore
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(rawArgs: Array<String>) {
    val args = rawArgs.toList()
    if (args.isEmpty()) {
        printUsage()
        return
    }
    
    try {
        when (args[0]) {
            "fixtures" -> FixturesCommand.run(args.drop(1))
            "query" -> QueryCommand.run(args.drop(1))
            "plan" -> PlanCommand.run(args.drop(1))
            "run" -> RunCommand.run(args.drop(1))
            "resume" -> ResumeCommand.run(args.drop(1))
            "--help", "-h" -> printUsage()
            "--version", "-v" -> printVersion()
            else -> {
                println("Unknown command '${args[0]}'")
                printUsage()
                exitProcess(1)
            }
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        if (System.getenv("MAPLE_DEBUG") == "true") {
            e.printStackTrace()
        }
        exitProcess(1)
    }
}

private fun printUsage() {
    println("""
        Maple - Release branching tool for first-party dependencies
        
        Usage: maple <command> [options]
        
        Commands:
          plan      Show what would be done without executing
          run       Execute release branching for all repos in BOM
          resume    Resume a failed run from a specific repo
          fixtures  Manage test fixtures (init, destroy, list)
          query     Query run history and status
          
        Global Options:
          --help, -h     Show this help message
          --version, -v  Show version information
          
        Examples:
          maple plan --bom bom.json --version 2024.1
          maple run --bom bom.json --version 2024.1 --dry-run
          maple resume --run-id abc123 --from-repo my-service
          
        For command-specific help, use: maple <command> --help
    """.trimIndent())
}

private fun printVersion() {
    println("Maple version 1.0.0-SNAPSHOT")
}

object PlanCommand {
    fun run(args: List<String>) {
        val config = parseCommonArgs(args)
        val bomParser = BomParser()
        val bom = bomParser.parseJsonBomFile(config.bomFile)
        
        println("Maple Release Plan")
        println("==================")
        println("BOM File: ${config.bomFile}")
        println("Version: ${config.version}")
        println("Dry Run: ${config.dryRun}")
        println()
        
        val firstPartyComponents = bom.components.filter { it.group.startsWith("com.tableau") }
        println("First-party components to process: ${firstPartyComponents.size}")
        
        firstPartyComponents.forEach { component ->
            println("  - ${component.name} (${component.version})")
            println("    Group: ${component.group}")
            println("    BOM Ref: ${component.bomRef}")
            println()
        }
        
        if (firstPartyComponents.isEmpty()) {
            println("No first-party components found in BOM!")
            exitProcess(1)
        }
    }
}

object RunCommand {
    fun run(args: List<String>) {
        val config = parseCommonArgs(args)
        
        println("Starting Maple run...")
        println("BOM: ${config.bomFile}")
        println("Version: ${config.version}")
        println("Dry run: ${config.dryRun}")
        
        val bomParser = BomParser()
        val bom = bomParser.parseJsonBomFile(config.bomFile)
        
        // Convert BOM components to RepoSpecs
        val repoSpecs = bom.components
            .filter { it.group.startsWith("com.tableau") }
            .map { component ->
                RepoSpec(
                    name = component.name,
                    repoUrl = "https://github.com/tableau/${component.name}.git", // Default pattern
                    bomVersion = component.version,
                    buildCmd = null // Auto-detect
                )
            }
        
        if (repoSpecs.isEmpty()) {
            println("No first-party repositories found in BOM")
            exitProcess(1)
        }
        
        // Setup orchestrator
        val workRoot = Path.of(".maple/work")
        val logRoot = Path.of(".maple/logs")
        Files.createDirectories(workRoot)
        Files.createDirectories(logRoot)
        
        val gitDriver = ShellGitDriver()
        val executor = DefaultRepoExecutor(gitDriver, workRoot, logRoot)
        val orchestrator = Orchestrator(executor)
        
        // Run the orchestrator
        val summary = orchestrator.run(config.version, repoSpecs)
        
        // Print results
        println("\nRun Summary")
        println("===========")
        println("Run ID: ${summary.runId}")
        println("Status: ${summary.status}")
        println("Total repos: ${summary.metrics.totalRepos}")
        println("Succeeded: ${summary.metrics.succeeded}")
        println("Failed: ${summary.metrics.failed}")
        println("Duration: ${summary.metrics.totalDurationMs}ms")
        
        // Save state
        val stateStore = SqliteStateStore(Path.of(".maple/state/maple.db"))
        stateStore.saveRun(summary)
        
        if (summary.status != com.tazzledazzle.maple.orchestrator.model.RunStatus.SUCCEEDED) {
            exitProcess(1)
        }
    }
}

object ResumeCommand {
    fun run(args: List<String>) {
        println("Resume functionality not yet implemented")
        exitProcess(1)
    }
}

data class MapleConfig(
    val bomFile: String,
    val version: String,
    val dryRun: Boolean = false,
    val workDir: String = ".maple",
    val concurrency: Int = 4
)

private fun parseCommonArgs(args: List<String>): MapleConfig {
    var bomFile: String? = null
    var version: String? = null
    var dryRun = false
    var workDir = ".maple"
    var concurrency = 4
    
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--bom", "-b" -> {
                if (i + 1 >= args.size) error("--bom requires a value")
                bomFile = args[++i]
            }
            "--version", "-v" -> {
                if (i + 1 >= args.size) error("--version requires a value")
                version = args[++i]
            }
            "--dry-run" -> dryRun = true
            "--work-dir" -> {
                if (i + 1 >= args.size) error("--work-dir requires a value")
                workDir = args[++i]
            }
            "--concurrency", "-j" -> {
                if (i + 1 >= args.size) error("--concurrency requires a value")
                concurrency = args[++i].toIntOrNull() ?: error("Invalid concurrency value")
            }
            "--help", "-h" -> {
                printUsage()
                exitProcess(0)
            }
            else -> error("Unknown option: ${args[i]}")
        }
        i++
    }
    
    return MapleConfig(
        bomFile = bomFile ?: error("--bom is required"),
        version = version ?: error("--version is required"),
        dryRun = dryRun,
        workDir = workDir,
        concurrency = concurrency
    )
}