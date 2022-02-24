package net.riotopsys.microscope

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.io.Writer


fun MicroscopeGame.outputHtml(path: String) {
    val root = File(  "$path/${this.name.replace(" ","_")}" )
        .apply { if ( !exists() ) mkdirs() }

    File( root,"index.html").apply {
        if ( !exists() ) createNewFile()
    }.bufferedWriter().use { out ->
        this.printSummary(out)
    }

    File( root,"rounds.html").apply {
        if ( !exists() ) createNewFile()
    }.bufferedWriter().use { out ->
        this.printRoundSummary(out)
    }
}

private fun MicroscopeGame.printRoundSummary(out: Writer) {
    out.appendHTML().html {
        head {
            title { +"$name Rounds"  }
            style {
                unsafe {
                    +loadResource("microscope.css")
                }
            }
        }
        body {
            h1 { +"$name Rounds" }

            playRounds.forEachIndexed { _, round ->
                when ( round ) {
                    is Round -> round.print(this, this@printRoundSummary.round.indexOf(round))
                    is FocusChange -> round.print(this)
                    is LegacyChange -> round.print(this)
                }
            }

            script {
                unsafe {
                    +loadResource("collapsible.js")
                }
            }
        }
    }
}

private fun LegacyChange.print(tag: HtmlBlockTag) {
    tag.h2 { +"New Legacy: $legacy"  }
    tag.div {
        player?.let {
            +"Player: ${player?.name} "
        }
    }
}

private fun FocusChange.print(tag: HtmlBlockTag) {
    tag.h2 { +"Focus change: $name"  }
    tag.div {
        lens?.let {
            +"Player: ${lens?.name} "
        }
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

private fun Round.print(tag: HtmlBlockTag, index: Int) {

    tag.h2 { +"Round ${index} " }

    tag.div {
        player?.let {
            +"Player: ${player?.name} "
            br
        }
        lens?.let {
            +"Lens: ${lens?.name}"
        }
    }

    actions.forEach { action ->
        tag.div {
            when (action) {
                is Period -> action.periodHeader(tag)
                is Event -> action.printHeader(tag)
                is Scene -> action.print(tag)
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
        periodHeader(this)
        div {
            classes = setOf("content")
            events.forEach {
                it.print(this)
            }
        }
    }
}

fun Period.periodHeader(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("header", "period")
        h3 { +name }
        printDescriptions(this)
    }
}

fun Event.print(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("card")
        printHeader(this)
        div {
            classes = setOf("content", "event")
            scenes.forEach {
                it.print(this)
            }
        }
    }
}

private fun Event.printHeader(tag: HtmlBlockTag) {
    tag.div {
        classes = setOf("header", "event")
        h3 { +name }
        printDescriptions(this)
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
        descriptions.forEach {
            p {
                +it
            }
        }
    }
}

fun loadResource(file: String) = File(ClassLoader.getSystemResource(file).file).readText()
