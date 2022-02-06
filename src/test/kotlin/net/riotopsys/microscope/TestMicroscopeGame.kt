package net.riotopsys.microscope

import org.junit.Test
import java.io.File

internal class TestMicroscopeGame{

    @Test
    fun canBuildGame(){

        val Addie = "Addie"
        val Bors = "Bors"
        val Cat = "Cat"

        val game = microscopeGame("Doom of the Gods") {

            setup {
                player("Addie", "addie@addie.io")
                player("Bors", "Bors@bors.com")
                player("Cat", "cat@cat.aww")
            }

            rounds {
                //setup round[0]
                focus( "Romance of Goorash and Svetka" )
                round( lens = player(Addie), player = player(Addie) ) //round[1]
                round( lens = player(Addie), player = player(Bors) ) //round[2]
                round( lens = player(Addie), player = player(Cat) ) //round[3]
                round( lens = player(Addie), player = player(Addie) ) //round[4]
                legacy( player(Cat), "Sword of Storms" )
                round( lens = player(Addie), player = player(Cat) ) //round[5]
                focus( "Well of Fate" )
                round( lens = player(Bors), player = player(Bors) ) //round[6]

            }

            period("Allfather creates mortal world",
                Tone.LIGHT, round[0]){
                    description("""
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc laoreet tristique pharetra.
                        Suspendisse vitae erat finibus, volutpat ligula in, placerat urna. Donec quis sollicitudin 
                        purus. Aliquam mollis tincidunt enim, et sodales magna condimentum nec. 
                    """.trimIndent())
            }


            period("Flourishing Kingdoms of Men",
                Tone.LIGHT, round[1]){
                event("Goorash saves Svetka from Black Beast",
                    Tone.LIGHT, round[1]){
                }
                event("Marriage of Svetka Interrupted",
                    Tone.DARK, round[4]){
                    scene(
                        "What dowry does Svetka bring?",
                        "",
                        "Storm of Swords",
                        Tone.DARK, round[4]
                    ) {}
                    scene(
                        "",
                        "",
                        "",
                        Tone.DARK, round[5]
                    ) {}
                }
                event("Goorash wins Sword of Storms",
                    Tone.LIGHT, round[2]){
                }
                event("Svetka mourns death of Goorash",
                    Tone.DARK, round[3]){
                }

            }

            period("Death of the Gods",
                Tone.DARK, round[0]){

                event( "Allfather hides Last Flame, Reveals plans to crow",
                    Tone.LIGHT, round[6]){

                    scene(
                        question = "Why does the Allfather hide the LAst Flame?",
                        setting = "High Fane of the Last Flame",
                        answer = "So the mortal carry their own fate",
                        Tone.LIGHT,
                        round[6]
                    ) {}

                }

            }

        }

        game.outputHtml(".")

    }

}
