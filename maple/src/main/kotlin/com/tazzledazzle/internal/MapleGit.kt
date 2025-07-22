package com.tazzledazzle.internal

import com.github.syari.kgit.KCommitCommand
import com.github.syari.kgit.KGit
import org.eclipse.jgit.lib.TextProgressMonitor
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

class MapleGit {
    var username = "your-username"
    var password = "your-password"
    var repoUrl = ""
    var branch = "main"
    var directoryFile = File("path/to/clone/repo")



    var git = KGit.cloneRepository {
        setURI("depName")
        setDirectory(directoryFile)
        setBranch("main")
        setProgressMonitor (TextProgressMonitor())
        setCredentialsProvider (UsernamePasswordCredentialsProvider(username, password))
    }

    companion object {
        private var instance: MapleGit? = null

        fun getInstance(): MapleGit {
            if (instance == null) {
                instance = MapleGit()
            }
            return instance!!
        }

        fun setCredentials(username: String, password: String) {
            getInstance().username = username
            getInstance().password = password
        }

        fun commitChanges(message: String) {
            getInstance().git
        }
    }
    // build the project
    // commit changes
    // create a branch
    // create a tag
    // push changes
}