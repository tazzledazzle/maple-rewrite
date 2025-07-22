package com.tazzledazzle.internal

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.core.DockerClientBuilder

class LocalProjectBuilder {
    // This class is currently empty, but can be used for building local projects in the future.
    // For example, you could add methods to compile code, run tests, package applications, etc.
    // Placeholder for future project building code.
    // This could include methods for:
    // - Compiling source code
    // - Running tests
    // - change version numbers in Gradle files
    // - Generating build notes or run notes
    fun startContainer() {
        val dockerClient: DockerClient = DockerClientBuilder.getInstance().build()

        // Pull image if needed
        dockerClient.pullImageCmd("maple-execution-environ:latest").start().awaitCompletion()

        // Create container
        val container: CreateContainerResponse = dockerClient.createContainerCmd("maple-execution-environ:latest")
            .withName("maple-execution-environ")
            .exec()

        // Start container
        dockerClient.startContainerCmd(container.id).exec()

        println("Started container with id: ${container.id}")
    }
}