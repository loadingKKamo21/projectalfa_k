package com.project.alfa.config.redis

import java.io.IOException
import java.net.ServerSocket
import kotlin.random.Random

class RandomPort {
    
    companion object {
        
        fun getRandomAvailablePort(minPort: Int, maxPort: Int): Int {
            var port: Int
            do {
                port = Random.nextInt((maxPort - minPort) + 1) + minPort
            } while (!isPortAvailable(port))
            return port
        }
        
        private fun isPortAvailable(port: Int): Boolean {
            return try {
                ServerSocket(port).use { serverSocket: ServerSocket ->
                    serverSocket.reuseAddress = true
                    true
                }
            } catch (e: IOException) {
                false
            }
        }
        
    }
    
}