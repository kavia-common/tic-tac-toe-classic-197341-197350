package org.example.app

/**
 * Simple Tic Tac Toe game engine with running scores and an optional computer player.
 *
 * This is intentionally UI-framework-agnostic: it does not depend on Android classes.
 */
class TicTacToeGame {

    enum class Player { X, O }
    enum class Mode { PLAYER_VS_PLAYER, PLAYER_VS_COMPUTER }

    var mode: Mode = Mode.PLAYER_VS_PLAYER

    val board: Array<Player?> = arrayOfNulls(9)

    var currentPlayer: Player = Player.X
        private set

    var scoreX: Int = 0
        private set
    var scoreO: Int = 0
        private set
    var scoreTies: Int = 0
        private set

    private var winner: Player? = null
    private var winningLine: IntArray? = null
    private var draw: Boolean = false

    // PUBLIC_INTERFACE
    fun resetBoard() {
        /** Clears board state but retains running scores. */
        for (i in board.indices) board[i] = null
        currentPlayer = Player.X
        winner = null
        winningLine = null
        draw = false
    }

    // PUBLIC_INTERFACE
    fun resetAll() {
        /** Clears board state AND resets running scores. */
        resetBoard()
        scoreX = 0
        scoreO = 0
        scoreTies = 0
    }

    // PUBLIC_INTERFACE
    fun playAt(index: Int): Boolean {
        /**
         * Attempts to play at the given board index.
         * @return true if a move was made; false if invalid.
         */
        if (index !in 0..8) return false
        if (board[index] != null) return false
        if (isGameOver()) return false

        board[index] = currentPlayer
        updateOutcome()

        if (!isGameOver()) {
            currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
        }

        return true
    }

    // PUBLIC_INTERFACE
    fun playComputerMove(): Boolean {
        /**
         * Plays a move for the computer (Player O) when in PvC mode.
         * @return true if the computer made a move; false otherwise.
         */
        if (mode != Mode.PLAYER_VS_COMPUTER) return false
        if (isGameOver()) return false
        if (currentPlayer != Player.O) return false

        val move = chooseComputerMove()
        return if (move != null) playAt(move) else false
    }

    // PUBLIC_INTERFACE
    fun getWinner(): Player? {
        /** @return winner (X/O) or null if no winner. */
        return winner
    }

    // PUBLIC_INTERFACE
    fun getWinningLine(): IntArray? {
        /** @return indices that form the winning line, or null if no winner. */
        return winningLine
    }

    // PUBLIC_INTERFACE
    fun isDraw(): Boolean {
        /** @return true if the game ended in a draw. */
        return draw
    }

    // PUBLIC_INTERFACE
    fun isGameOver(): Boolean {
        /** @return true if winner exists or game is a draw. */
        return winner != null || draw
    }

    private fun updateOutcome() {
        val line = findWinningLine()
        if (line != null) {
            winner = board[line[0]]
            winningLine = line
            when (winner) {
                Player.X -> scoreX++
                Player.O -> scoreO++
                null -> { /* impossible */ }
            }
            draw = false
            return
        }

        // Draw if no empty squares
        val anyEmpty = board.any { it == null }
        if (!anyEmpty) {
            draw = true
            winningLine = null
            winner = null
            scoreTies++
        }
    }

    private fun findWinningLine(): IntArray? {
        val lines = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )

        for (line in lines) {
            val a = board[line[0]]
            val b = board[line[1]]
            val c = board[line[2]]
            if (a != null && a == b && b == c) return line
        }
        return null
    }

    private fun chooseComputerMove(): Int? {
        // Strategy:
        // 1) Win if possible (O)
        // 2) Block opponent win (X)
        // 3) Take center
        // 4) Take a corner
        // 5) Take first available

        val winMove = findImmediateWinningMove(Player.O)
        if (winMove != null) return winMove

        val blockMove = findImmediateWinningMove(Player.X)
        if (blockMove != null) return blockMove

        if (board[4] == null) return 4

        val corners = listOf(0, 2, 6, 8)
        for (c in corners) {
            if (board[c] == null) return c
        }

        for (i in board.indices) {
            if (board[i] == null) return i
        }
        return null
    }

    private fun findImmediateWinningMove(forPlayer: Player): Int? {
        for (i in board.indices) {
            if (board[i] != null) continue

            board[i] = forPlayer
            val line = findWinningLine()
            board[i] = null

            if (line != null) return i
        }
        return null
    }
}
