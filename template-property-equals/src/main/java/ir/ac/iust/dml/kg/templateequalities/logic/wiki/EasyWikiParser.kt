package ir.ac.iust.dml.kg.templateequalities.logic.wiki

import org.sweble.wikitext.engine.PageId
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.WtEngineImpl
import org.sweble.wikitext.engine.nodes.EngProcessedPage
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp
import org.sweble.wikitext.parser.nodes.WtNode
import org.sweble.wikitext.parser.nodes.WtText

object EasyWikiParser {
    private val config = DefaultConfigEnWp.generate()
    private val engine = WtEngineImpl(config)

    fun parse(wikiText: String): EngProcessedPage {
        val pageTitle = PageTitle.make(config, "Test")// Retrieve a page
        val pageId = PageId(pageTitle, -1)
        // Compile the retrieved page
        return engine.postprocess(pageId, wikiText, null)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : WtNode> getAllNode(clazz: Class<T>, node: WtNode, list: MutableList<T>) {
        if (clazz.isInstance(node)) {
            list.add(node as T)
            return
        }
        for (child in node)
            getAllNode(clazz, child, list)
    }

    fun getPlainText(node: WtNode): String {
        val texts = mutableListOf<WtText>()
        getAllNode(WtText::class.java, node, texts)
        val builder = StringBuilder()
        for (t in texts) builder.append(t.content.trim()).append(' ')
        if (builder.isNotEmpty()) builder.setLength(builder.length - 1)
        return builder.toString()
    }

    val linkAndTitleRegex = Regex("([^\\[]*)\\[+([^|]+)\\|([^\\]]+)\\]+([^\\]]*)")
    val linkRegex = Regex("([^\\[]*)\\[+([^\\]]+)\\]+([^\\]]*)")
    val textAndColon = Regex("([^:]*)\\s+:")
    val elementOpenRegex = Regex("([^<]*)<[^>]+>([^>]*)")
    val elementCloseRegex = Regex("([^<]*)</[^>]+>([^>]*)")
    val escapedRegex = Regex("([^&]*)&[^;]+;([^;]*)")
    val sharpIfRegex = Regex("([^#]*)#if: [^\\s]+ ([^\\s]+) (.+)")

    fun clear(text: String): String {
        val cleared = clearOne(text)
        if (cleared.length != text.length) return clear(cleared)
        return text
    }

    private fun groups(text: String, regex: Regex, vararg groupNumbers: Int): String {
        val groups = regex.matchEntire(text)!!.groups
        val builder = StringBuilder()
        for (i in groupNumbers) builder.append(groups[i]!!.value.trim()).append(" ")
        if (builder.isNotEmpty()) builder.setLength(builder.length - 1)
        return builder.toString()
    }

    private fun clearOne(text: String): String {
        if (text.matches(escapedRegex))
            return groups(text, escapedRegex, 1, 2)
        if (text.matches(elementOpenRegex))
            return groups(text, elementOpenRegex, 1, 2)
        if (text.matches(elementCloseRegex))
            return groups(text, elementCloseRegex, 1, 2)
        if (text.matches(linkAndTitleRegex))
            return groups(text, linkAndTitleRegex, 1, 3, 4)
        if (text.matches(linkRegex))
            return groups(text, linkRegex, 1, 2, 3)
        if (text.matches(sharpIfRegex))
            return groups(text, sharpIfRegex, 1, 3)
        if (text.matches(textAndColon))
            return groups(text, textAndColon, 1)
        return text.trim()
    }
}