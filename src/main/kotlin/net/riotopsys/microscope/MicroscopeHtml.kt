package net.riotopsys.microscope

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.io.Writer


fun MicroscopeGame.outputHtml(path: String) {
    File("$path/${this.name.replace(" ","_")}/index.html").apply {
        parentFile.apply { if ( !exists() ) mkdirs() }
        if ( !exists() ) createNewFile()
    }.bufferedWriter().use { out ->
        this.printSummary(out)
    }
}

fun MicroscopeGame.printSummary(out: Writer) {
    out.appendHTML().html {
        head {
            title { +name }
            style {
                unsafe {
                    +loadResource("microscope.css")
                }
            }
        }
        body {
            h1 { +name }
            printPlayers(this)
            printFocus(this)
            periods.forEach {
                it.print(this)
            }
            script {
                unsafe {
                    +loadResource("collapsible.js")
                }
            }
        }
    }
}

private fun MicroscopeGame.printFocus(tag: HtmlBlockTag) {
    tag.div{
        h2 { +"Focus"}
        ol {
            foci.forEach {
                li { +it }
            }
        }
    }
}

private fun MicroscopeGame.printPlayers(tag: HtmlBlockTag) {
    tag.div {
        h2 { +"Players" }
        ul {
            players.values.forEach {
                li { a("mailto:${it.email}") { +it.name } }
            }
        }
    }
}

fun Period.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        button(type = ButtonType.button) {
            classes = setOf("collapsible", "period", "active")
            +this@print.name
        }
        div {
            classes = setOf("content", "period")
            style = "display: block;"
            printDescriptions()
            events.forEach {
                it.print(this)
            }
        }
    }
}

fun Event.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        button(type = ButtonType.button) {
            classes = setOf("collapsible", "event", "active")
            +this@print.name
        }
        div {
            classes = setOf("content", "event")
            style = "display: block;"
            printDescriptions()
            scenes.forEach {
                it.print(this)
            }
        }
    }
}

fun Scene.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        button(type = ButtonType.button) {
            classes = setOf("collapsible", "scene", "active")
            +this@print.question
        }
        div {
            classes = setOf("content", "scene")
            style = "display: block;"
            div {
                p { +"Setting: $setting" }
                p { +"Answer: $answer" }
            }
            printDescriptions(this)
        }
    }
}

fun Action.printDescriptions(tag: HtmlBlockTag) {
    tag.apply {
        +"Tone:${tone.name}"
        descriptions.forEach {
            p {
                +it
            }
        }
    }
}

fun loadResource(file: String) = File(ClassLoader.getSystemResource(file).file).readText()
