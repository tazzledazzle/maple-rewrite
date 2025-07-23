package com.tazzledazzle.maple.build

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.LogContainerResultCallback
import java.time.Duration
import java.util.concurrent.TimeUnit

object Docker {
    fun client(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        return DockerClientBuilder.getInstance(config).build()
    }

    fun ensureImage(client: DockerClient, image: String) {
        val images = client.listImagesCmd().withImageNameFilter(image).exec()
        if (images.isEmpty()) {
            client.pullImageCmd(image).start().awaitCompletion()
        }
    }

    fun createContainer(
        client: DockerClient,
        image: String,
        entrypoint: String,
        cmd: List<String>,
        mounts: List<DockerMount>
    ): String {
        val binds = mounts.map {
            Bind(it.host.toAbsolutePath().toString(), Volume(it.container.toString()), !it.readOnly)
        }

        val hc = HostConfig.newHostConfig().withBinds(binds)
        val resp: CreateContainerResponse = client.createContainerCmd(image)
            .withHostConfig(hc)
            .withWorkingDir("/workspace")
            .withEntrypoint(entrypoint)
            .withCmd(cmd)
            .exec()
        return resp.id
    }

    fun start(client: DockerClient, containerId: String) {
        client.startContainerCmd(containerId).exec()
    }

    fun streamLogs(client: DockerClient, containerId: String, timeout: Duration, consume: (String) -> Unit) {
        val callback = object : LogContainerResultCallback() {
            override fun onNext(item: com.github.dockerjava.api.model.Frame) {
                consume(String(item.payload).trimEnd())
            }
        }
        client.logContainerCmd(containerId)
            .withStdErr(true)
            .withStdOut(true)
            .withFollowStream(true)
            .exec(callback)
        callback.awaitCompletion(timeout.toMillis(), TimeUnit.MILLISECONDS)
    }

    fun wait(client: DockerClient, containerId: String, timeout: Duration): Long {
        val res = client.waitContainerCmd(containerId).start()
        val s = res.awaitStatusCode(timeout.toMillis(), TimeUnit.MILLISECONDS)
        return s?.toLong() ?: -1
    }

    fun cleanup(client: DockerClient, containerId: String) {
        runCatching { client.removeContainerCmd(containerId).withForce(true).exec() }
    }
}
