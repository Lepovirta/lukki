package io.lepo.lukki.testserver

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

internal class PageGenerator constructor(val seed: Long? = null) {
    private val random: Random = if (seed == null) Random() else Random(seed)

    fun generate(
            validPageCount: Int
    ): Map<String, List<String>> {
        val validPaths = randomPaths(validPageCount - 1)
        val pages = HashMap<String, List<String>>(validPageCount)
        var remainingPicks: List<String> = ArrayList(validPaths)
        validPaths.add(0, "/") // make sure root is included in the paths

        // build the pages such that all paths are reachable by starting the crawl from root
        for (path in validPaths) {
            val childPaths = ArrayList<String>(10)

            if (!remainingPicks.isEmpty()) {
                val picks = randomInt(1, remainingPicks.size)
                val pickedLinks = remainingPicks.subList(0, picks)
                remainingPicks = remainingPicks.subList(picks, remainingPicks.size)
                childPaths.addAll(pickedLinks)
            }

            // add additional paths
            childPaths.addAll(pickRandom(validPaths))
            pages.put(path, childPaths)
        }

        return pages
    }

    private fun randomPaths(count: Int): MutableList<String> {
        return Stream
                .generate({ randomPath() })
                .distinct()
                .limit(count.toLong())
                .collect(Collectors.toList())
    }

    private fun randomPath(): String {
        val sb = StringBuilder("/")
        val dirDepth = randomInt(1, MAX_DIR_DEPTH)
        for (i in 0 until dirDepth) {
            randomFileName(sb)
            sb.append('/')
        }
        return sb.toString()
    }

    private fun randomFileName(sb: StringBuilder) {
        val fileNameLength = randomInt(1, MAX_NAME_LENGTH)
        for (i in 0 until fileNameLength) {
            val index = randomInt(RAND_STRING_CHARS.length - 1)
            sb.append(RAND_STRING_CHARS[index])
        }
    }

    private fun <A> pickRandom(from: List<A>): List<A> {
        val numberOfElements = randomInt(from.size + 1)
        return random
                .ints(numberOfElements.toLong(), 0, from.size)
                .mapToObj({ from[it] })
                .collect(Collectors.toList())
    }

    private fun randomInt(low: Int, high: Int): Int {
        if (high < low) {
            throw IllegalArgumentException(String.format("%d < %d", high, low))
        }
        return random.nextInt(high - low + 1) + low
    }

    private fun randomInt(high: Int): Int {
        return random.nextInt(high)
    }

    companion object {
        private val RAND_STRING_CHARS = "abcdefghijklmnopqrstuvwxyz"
        private val MAX_NAME_LENGTH = 20
        private val MAX_DIR_DEPTH = 5
    }
}
