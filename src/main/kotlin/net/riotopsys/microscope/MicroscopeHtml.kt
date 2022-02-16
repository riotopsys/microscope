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
            printPallet( this )
            printFocus(this)
            printLegacies( this )
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

private fun MicroscopeGame.printLegacies(tag: HtmlBlockTag) {
    tag.div {
        h2 { +"Legacies" }
        ul {
            legacies.entries.forEach {
                li {
                    +"${it.value} (${it.key.name})"
                }
            }
        }
    }
}

private fun MicroscopeGame.printPallet(tag: HtmlBlockTag) {
    tag.div {
        h2 { +"Pallet" }
        div {
            strong { +"Yes" }
            ul{
                pallet?.yesList?.forEach {
                    li { +it }
                }
            }
        }
        div {
            strong { +"No" }
            ul{
                pallet?.noList?.forEach {
                    li { +it }
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
        div {
            classes = setOf("header", "period")
            h3 { +this@print.name }
            printDescriptions(this)
        }
        div {
            classes = setOf("content")
            events.forEach {
                it.print(this)
            }
        }
    }
}

fun Event.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        div {
            classes = setOf("header", "event")
            h3{+this@print.name}
            printDescriptions(this)
        }
        div {
            classes = setOf("content", "event")
            scenes.forEach {
                it.print(this)
            }
        }
    }
}

fun Scene.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        div {
            classes = setOf("header", "scene")
            h4 { +this@print.question }
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
//        p {
            descriptions.forEach {
                p {
                    +it
                }
            }
//        }
    }
}

fun loadResource(file: String) = File(ClassLoader.getSystemResource(file).file).readText()
