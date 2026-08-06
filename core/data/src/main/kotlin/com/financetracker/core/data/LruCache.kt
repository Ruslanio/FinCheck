package com.financetracker.core.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ExchangeRate(
    val from: String,
    val to: String,
    val rate: Double,
    val fetchedAt: Long = System.currentTimeMillis()
)

class LruCache(
    private val capacity: Int,
    private val ttlMs: Long = 60_000L
) {

    private data class Node(
        val key: String,
        var exchangeRate: ExchangeRate
    ) {
        var prev: Node? = null
        var next: Node? = null
    }

    private val mutex = Mutex()

    private val map: HashMap<String, Node> = hashMapOf()

    private val head = Node("HEAD", ExchangeRate("", "", 0.0))
    private val tail = Node("TAIL", ExchangeRate("", "", 0.0))

    init {
        head.next = tail
        tail.prev = head
    }

    suspend fun get(key: String): ExchangeRate? = mutex.withLock {
        val node = map[key] ?: return null

        if (node.exchangeRate.isExpired()) {
            removeNode(node)
            map.remove(key)
            return null
        }

        moveToFront(node)
        return node.exchangeRate
    }

    suspend fun put(key: String, exchangeRate: ExchangeRate) = mutex.withLock {
        val existing = map[key]
        if (existing != null) {
            existing.exchangeRate = exchangeRate
            moveToFront(existing)
        } else {
            val node = Node(key, exchangeRate)
            map[key] = node
            addToFront(node)
            if (map.size > capacity) {
                evictLatest()
            }
        }
    }


    private fun removeNode(node: Node) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
        node.next = null
        node.prev = null
    }

    private fun addToFront(node: Node) {
        node.next = head.next
        node.prev = head
        head.next?.prev = node
        head.next = node
    }

    private fun moveToFront(node: Node) {
        removeNode(node)
        addToFront(node)
    }

    private fun evictLatest() {
        val latest = tail.prev
        if (latest == head || latest == null) return
        removeNode(latest)
        map.remove(latest.key)
    }

    private fun ExchangeRate.isExpired() =
        (System.currentTimeMillis() - fetchedAt) >= ttlMs
}
