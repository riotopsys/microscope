package net.riotopsys.microscope

fun microscopeGame(name: String, lambda: MicroscopeGame.()->Unit ): MicroscopeGame{
    val game = MicroscopeGame(name)
    game.lambda()
    return game
}

class MicroscopeGame(val name: String) {

    private val _periods = mutableListOf<Period>()
    internal var setup: Setup? = null
    private var rounds: Rounds? = null

    val players: Map<String, Player>
        get() = setup?.players ?: emptyMap()
    val round: Rounds
        get() = rounds ?: throw SequenceException("rounds not defined")
    val periods: List<Period>
        get() = _periods.toList()
    val foci: List<String>
        get() = rounds?.foci ?: emptyList()
    val pallet: Palette?
        get() = setup?.palette
    val legacies: Map<Player, String>
        get() = rounds?.legacies ?: emptyMap()

    val playRounds: List<PlayRounds>
        get() = rounds?.playRounds ?: emptyList()

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
    val legacies: Map<Player, String>
        get() = activeLegacies.toMap()

    private val _rounds: MutableList<PlayRounds> = mutableListOf(
        Round(null, null, "", activeLegacies.toMap())
    )
    val rounds: List<Round>
        get() = _rounds.toList().filterIsInstance(Round::class.java)
    val foci: List<String>
        get() = focusList.toList()

    val playRounds: List<PlayRounds>
        get() = _rounds.toList()

    private fun roundCount(): Int = _rounds.count { it is Round }

    private fun calcLensPlayer(index: Int): Pair<Player, Player> {
        val players = game.setup?.playerOrder ?: emptyList()
        if (players.isEmpty()) throw SequenceException("no players defined")

        val actualIndex = index - 1
        val cycle = players.size + 1
        val lensIndex = ((actualIndex) / cycle) % players.size
        val pos = actualIndex % cycle
        val playerIndex = if (pos < players.size) {
            (lensIndex + pos) % players.size
        } else {
            lensIndex
        }
        return players[lensIndex] to players[playerIndex]
    }

    operator fun get(index: Int): Round {
        while (roundCount() <= index) {
            val count = roundCount()
            if (count == 0) {
                // setup round already exists
                continue
            }
            val (lens, player) = calcLensPlayer(count)
            val focus = focusList.lastOrNull() ?: ""
            _rounds.add(Round(lens, player, focus, activeLegacies.toMap()))
        }
        return _rounds.filterIsInstance<Round>()[index]
    }

    fun player(name: String): Player? {
        return game.players[name]
    }

    fun focus(lens: Player?, name: String) {
        focusList.add(name)
        _rounds.add(FocusChange( lens, name ))
    }

    fun legacy(player: Player?, legacy: String) {
        player?.let{
            activeLegacies[it] = legacy
        }
        _rounds.add(LegacyChange(player, legacy))
    }

}

interface PlayRounds

class LegacyChange(val player: Player?, val legacy: String) : PlayRounds {

}

class FocusChange(val lens: Player?,val  name: String) : PlayRounds {

}

class Round(val lens: Player?, val player: Player?, val focus: String, val activeLegacies: Map<Player, String>):PlayRounds{

    val actions = mutableListOf<Action>()

    fun attach(action: Action) {
        actions.add(action)
    }

}

class Setup {
    private val _players = LinkedHashMap<String, Player>()
    val players: Map<String, Player>
        get() = _players
    val playerOrder: List<Player>
        get() = _players.values.toList()
    var palette: Palette? = null

    fun player(name: String, email: String): Player {
        return Player(name, email).also { _players[it.name]=it }
    }

    fun palette(lambda: Palette.() -> Unit) {
        val palette = Palette()
        palette.lambda()
        this.palette = palette
    }
}

class Palette {

    private val _yesList = mutableListOf<String>()
    val yesList: List<String>
        get() = _yesList.toList()

    private val _noList = mutableListOf<String>()
    val noList: List<String>
        get() = _noList.toList()

    fun yes(item: String) {
        _yesList.add(item)
    }

    fun no(item: String) {
        _noList.add(item)
    }

}

data class Player(val name: String, val email: String)

open class Action(val tone: Tone, val name: String) {

    private val _descriptions = mutableListOf<String>()
    val descriptions: List<String>
        get() = _descriptions.toList()

    fun description(text: String) {
        _descriptions.add(text)
    }

}

enum class Tone {
    LIGHT, DARK
}

open class SequenceException(message: String) : MicroscopeException(message)
open class InvalidArgumentException(message: String) : MicroscopeException(message)
open class MicroscopeException(message: String): Exception(message)

