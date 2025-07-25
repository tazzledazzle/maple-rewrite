package com.tazzledazzle

import com.tazzledazzle.maple.git.ShellGitDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class GitTest: FunSpec({
    val gitDriver = ShellGitDriver()
    
    test("git driver interface compliance") {
        // Test that our git driver implements the interface correctly
        val tempDir = Files.createTempDirectory("maple-git-test")
        
        try {
            // Initialize a test repo
            ProcessBuilder("git", "init")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
                
            // Configure git for testing
            ProcessBuilder("git", "config", "user.email", "test@example.com")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
                
            ProcessBuilder("git", "config", "user.name", "Test User")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
            
            // Create initial commit
            Files.write(tempDir.resolve("README.md"), "# Test Repo".toByteArray())
            ProcessBuilder("git", "add", ".")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
                
            ProcessBuilder("git", "commit", "-m", "Initial commit")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
            
            // Test branch creation
            gitDriver.createBranch(tempDir, "test-branch")
            
            // Test tag creation  
            gitDriver.createTag(tempDir, "v1.0.0")
            
            // Verify branch exists
            val branchResult = ProcessBuilder("git", "branch", "--list", "test-branch")
                .directory(tempDir.toFile())
                .start()
            branchResult.waitFor() shouldBe 0
            
            // Verify tag exists
            val tagResult = ProcessBuilder("git", "tag", "--list", "v1.0.0")
                .directory(tempDir.toFile())
                .start()
            tagResult.waitFor() shouldBe 0
            
        } finally {
            // Cleanup
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("checkout specific ref") {
        val tempDir = Files.createTempDirectory("maple-checkout-test")
        
        try {
            // Initialize repo with multiple commits
            ProcessBuilder("git", "init")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
                
            ProcessBuilder("git", "config", "user.email", "test@example.com")
                .directory(tempDir.toFile())
                .start()
                .waitFor()
                
            ProcessBuilder("git", "config", "user.name", "Test User")
                .directory(tempDir.toFile())
                .start()
                .waitFor()
            
            // First commit
            Files.write(tempDir.resolve("file1.txt"), "content1".toByteArray())
            ProcessBuilder("git", "add", ".")
                .directory(tempDir.toFile())
                .start()
                .waitFor()
                
            ProcessBuilder("git", "commit", "-m", "First commit")
                .directory(tempDir.toFile())
                .start()
                .waitFor() shouldBe 0
            
            // Create and checkout main branch
            gitDriver.checkout(tempDir, "main")
            
            tempDir.resolve("file1.txt").exists() shouldBe true
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
