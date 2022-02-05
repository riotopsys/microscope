package net.riotopsys.microscope

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.Writer

fun MicroscopeGame.printSummary(out: Writer) {
    out.appendHTML().html {
        head {
            title { +name }
            style {
                unsafe {
                    +"""
                            .collapsible {
                              background-color: #777;
                              color: white;
                              cursor: pointer;
                              padding: 18px;
                              width: 100%;
                              border: none;
                              text-align: left;
                              outline: none;
                              font-size: 15px;
                            }

                            .active, .collapsible:hover {
                              background-color: #555;
                            }

                            .content {
                              padding: 0 18px;
                              display: none;
                              overflow: hidden;
                              background-color: #f1f1f1;
                            }
                        """.trimIndent()
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
                    +"""
                            var coll = document.getElementsByClassName("collapsible");
                            var i;

                            for (i = 0; i < coll.length; i++) {
                              coll[i].addEventListener("click", function() {
                                this.classList.toggle("active");
                                var content = this.nextElementSibling;
                                if (content.style.display === "block") {
                                  content.style.display = "none";
                                } else {
                                  content.style.display = "block";
                                }
                              });
                            }
                        """.trimIndent()
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
        button(type = ButtonType.button, classes = "collapsible") {
            +this@print.name
        }
        div("content") {
            printDescriptions()
            events.forEach {
                it.print(this)
            }
        }
    }
}

fun Event.print(tag: HtmlBlockTag) {
    tag.div {
        button(type = ButtonType.button, classes = "collapsible") {
            +this@print.name
        }
        div("content") {
            printDescriptions()
            scenes.forEach {
                it.print(this)
            }
        }
    }
}

fun Scene.print(tag: HtmlBlockTag) {
    tag.div {
        button(type = ButtonType.button, classes = "collapsible") {
            +this@print.question
        }
        div("content") {
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