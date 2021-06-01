package com.mytlogos.enterprise.tools

import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

/**
 * Copied from [
 * HtmlToPlainText](https://www.programcreek.com/java-api-examples/?code=eclipse/eclipse.jdt.ls/eclipse.jdt.ls-master/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/javadoc/HtmlToPlainText.java):
 *
 *
 *
 *
 * HTML to plain-text. Uses jsoup to convert HTML input to lightly-formatted
 * plain-text. This is a fork of Jsoup's [HtmlToPlainText](https://github.com/jhy/jsoup/blob/842977c381b8d48bf12719e3f5cf6fd669379957/src/main/java/org/jsoup/examples/HtmlToPlainText.java)
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class HtmlToPlainText {
    /**
     * Format an Element to plain-text
     *
     * @param element the root element to format
     * @return formatted text
     */
    fun getPlainText(element: Element): String {
        // walk the DOM, and call .head() and .tail() for each node
        val formatter = FormattingVisitor()
        element.traverse(formatter)
        return formatter.toString()
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private inner class FormattingVisitor : NodeVisitor {
        private val accum = StringBuilder() // holds the accumulated text
        private var listNesting = 0

        // hit when the node is first seen
        override fun head(node: Node, depth: Int) {
            val name = node.nodeName()
            if (node is TextNode) {
                append(node.text()) // TextNodes carry all user-readable text in the DOM.
            } else if (name == "ul") {
                listNesting++
            } else if (name == "li") {
                append("\n ")
                for (i in 1 until listNesting) {
                    append("  ")
                }
                if (listNesting == 1) {
                    append("* ")
                } else {
                    append("- ")
                }
            } else if (name == "dt") {
                append("  ")
            } else if (StringUtil.`in`(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
                append("\n")
            }
        }

        // hit when all of the node's children (if any) have been visited
        override fun tail(node: Node, depth: Int) {
            val name = node.nodeName()
            if (StringUtil.`in`(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
                append("\n")
            } else if (StringUtil.`in`(name, "th", "td")) {
                append(" ")
            } else if (name == "a") {
                append(String.format(" <%s>", node.absUrl("href")))
            } else if (name == "ul") {
                listNesting--
            }
        }

        // appends text to the string builder with a simple word wrap method
        private fun append(text: String) {
            if (text == " " &&
                (accum.length == 0 || StringUtil.`in`(accum.substring(accum.length - 1), " ", "\n"))
            ) {
                return  // don't accumulate long runs of empty spaces
            }
            accum.append(text)
        }

        override fun toString(): String {
            return accum.toString()
        }
    }
}