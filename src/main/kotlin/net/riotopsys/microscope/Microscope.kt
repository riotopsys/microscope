package net.riotopsys.microscope

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.Writer


fun microscopeGame(name: String, lambda: MicroscopeGame.()->Unit ): MicroscopeGame{
    val game = MicroscopeGame(name)
    game.lambda()
    return game
}

class MicroscopeGame(val name: String) {

    private val _periods = mutableListOf<Period>()
    private var setup: Setup? = null
    private var rounds: Rounds? = null

    val players: Map<String, Player>
        get() = setup?.players?.toMap() ?: emptyMap()
    val round: List<Round>
        get() = rounds?.rounds ?: emptyList()
    val periods: List<Period>
        get() = _periods.toList()
    val foci: List<String>
        get() = rounds?.foci ?: emptyList()

    @Synchronized fun setup(lambda: Setup.() -> Unit) {
        if ( setup != null ) throw SequenceException("only one setup block allowed")
        val setup = Setup()
        setup.lambda()
        this.setup = setup
    }

    @Synchronized fun rounds(lambda: Rounds.() -> Unit) {
        if ( rounds != null ) throw SequenceException("only one rounds block allowed")
        val rounds = Rounds(this)
        rounds.lambda()
        this.rounds = rounds
    }

    fun period(name: String, tone: Tone, round: Round, lambda: Period.() -> Unit): Period {
        val period = Period( this, tone, name )
        round.attach( period )
        _periods.add( period )
        period.lambda()
        return period
    }
}


class Period(val game: MicroscopeGame, tone: Tone, name: String) : Action(tone, name) {

    private val _events = mutableListOf<Event>()
    val events: List<Event>
        get() = _events.toList()

    fun event(name: String, tone: Tone, round: Round, lambda: Event.() -> Unit): Event {
        val event = Event(this, tone, name)
        round.attach(event)
        _events.add(event)
        event.lambda()
        return event
    }

}

class Event(val period: Period, tone: Tone, name: String ):Action(tone, name) {

    private val _scenes = mutableListOf<Scene>()
    val scenes: List<Scene>
        get() = _scenes.toList()

    fun scene(question: String,
              setting: String,
              answer: String,
              tone: Tone,
              round: Round,
              lambda: Scene.() -> Unit ): Scene {
        val scene = Scene(this, question, setting, answer, tone, name)
        round.attach(scene)
        _scenes.add(scene)
        scene.lambda()
        return scene
    }

}

class Scene(val event: Event,
            val question: String,
            val setting: String,
            val answer: String,
            tone: Tone, name: String): Action( tone, name) {

}

class Rounds(private val game:MicroscopeGame) {

    private val focusList = mutableListOf<String>()
    private val activeLegacies = mutableMapOf<Player, String>()

    private val _rounds: MutableList<Round> = mutableListOf( Round(null, null, "", activeLegacies.toMap()) )
    val rounds: List<Round>
        get() = _rounds.toList()
    val foci: List<String>
        get() = focusList.toList()

    fun round(lens: Player?, player: Player?):Round {
        if ( lens == null ) throw InvalidArgumentException("lens is required")
        if ( player == null ) throw InvalidArgumentException("player is required")
        return Round( lens, player, focusList.last(), activeLegacies.toMap() ).also { _rounds.add(it)  }
    }

    fun player(name: String): Player? {
        return game.players[name]
    }

    fun focus(name: String) {
        focusList.add(name)
    }

    fun legacy(player: Player?, legacy: String) {
        player?.let{

        }
    }

}

class Setup {
    val players = mutableMapOf<String, Player>()

    fun player(name: String, email: String): Player {
        return Player(name, email).also { players[it.name]=it }
    }
}

data class Player(val name: String, val email: String)

class Round(val lens: Player?, val player: Player?, val focus: String, val toMap: Map<Player, String>){

    private val actions = mutableListOf<Action>()

    fun attach(action: Action) {
        actions.add(action)
    }

}

open class Action(val tone: Tone, val name: String) {

    private val _descriptions = mutableListOf<String>()
    val descriptions: List<String>
        get() = _descriptions.toList()

    fun description(text: String) {
        _descriptions.add(text)
    }

    fun HtmlBlockTag.printDescriptions() {
        +"Tone:${tone.name}"
        descriptions.forEach {
            p {
                +it
            }
        }
    }
}

enum class Tone {
    LIGHT, DARK
}

open class SequenceException(message: String) : MicroscopeException(message)
open class InvalidArgumentException(message: String) : MicroscopeException(message)
open class MicroscopeException(message: String): Exception(message)

