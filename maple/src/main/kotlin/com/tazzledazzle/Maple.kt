package com.tazzledazzle

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.nio.file.Path

class Maple {
    val greeting: String
        get() {
            return "Hello World!"
        }
    fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
            println("Command line arguments: ${args.joinToString(", ")}")
        } else {
            println("No command line arguments provided.")
        }
        ArgParser(args).parseInto(::MapleArgs).run {
            println("Bom file: $bom")
            println("Version flag is set: $version")
            if (!liveRun) {
                runMaple(args)
            }
        }
    }
}
class MapleArgs(parser: ArgParser) {
    val bom by parser.storing(  "-b","--bom", help = "The bom file used")
    val version by parser.storing( "-v","--ver", help = "Show version")
    val liveRun by parser.flagging( "-e","--execute", help = "Default to false, set to true to run Maple in live mode").default(false)
}

fun runMaple(args: Array<String>) {
    println("Running Maple with arguments: ${args.joinToString(", ")}")
    // Add your Maple logic here
}

interface FixtureDriver {
    fun createRepo(name: String, version: String, buildCmd: String): RepoMeta
    fun deleteRepo(name: String)
}

class RepoMeta {

}

data class FixtureConfig(
    val remote: RemoteMode, val root: Path, val org: String?, val prefix: String,
    val count: Int, val version: String, val buildCmd: String
)

class RemoteMode {

}

// val git: GitDriver = KGitDriver() // or ShellGitDriver()
//    git.cloneOrFetch(entry.repoUrl, checkoutDir)
//    git.checkout(checkoutDir, entry.version)
//
// val spec = BuildRunnerFactory.detect(
//    checkoutDir,
//    entry.buildCmd,
//    logDir,
//    enableScan = true,
//    docker = flags.docker
// )
// val result = ShellBuildRunner().run(spec)
// val (success, scan) = when (result) {
//    is BuildResult.Success -> true to result.scanUrl
//    is BuildResult.Failure -> false to result.scanUrl
// }
// repoStatus.scanUrl = scan

