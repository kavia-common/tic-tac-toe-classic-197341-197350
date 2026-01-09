package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

/**
 * Main single-screen Activity for the Tic Tac Toe game.
 *
 * UX goals:
 * - Clear status messaging (turn / win / draw)
 * - Mode switching (PvP / PvC) resets board, retains scores
 * - Reset Game resets board only; long-press resets scores too
 * - Accessible cell labels that reflect current state
 */
class MainActivity : Activity() {

    private lateinit var scoreXText: TextView
    private lateinit var scoreOText: TextView
    private lateinit var scoreTiesText: TextView
    private lateinit var statusText: TextView

    private lateinit var modePvpButton: Button
    private lateinit var modePvcButton: Button
    private lateinit var resetButton: Button

    // 9 board buttons, index 0..8
    private lateinit var cellButtons: Array<Button>

    private val game = TicTacToeGame()

    // Static base descriptions from XML (e.g., "Cell 1") so we can append state for accessibility.
    private lateinit var baseCellDescriptions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        bindViews()

        // Default mode: PvP (resets board as part of mode set)
        setMode(TicTacToeGame.Mode.PLAYER_VS_PLAYER)
    }

    private fun bindViews() {
        scoreXText = findViewById(R.id.scoreXText)
        scoreOText = findViewById(R.id.scoreOText)
        scoreTiesText = findViewById(R.id.scoreTiesText)
        statusText = findViewById(R.id.statusText)

        modePvpButton = findViewById(R.id.modePvpButton)
        modePvcButton = findViewById(R.id.modePvcButton)
        resetButton = findViewById(R.id.resetButton)

        cellButtons = arrayOf(
            findViewById(R.id.cell0),
            findViewById(R.id.cell1),
            findViewById(R.id.cell2),
            findViewById(R.id.cell3),
            findViewById(R.id.cell4),
            findViewById(R.id.cell5),
            findViewById(R.id.cell6),
            findViewById(R.id.cell7),
            findViewById(R.id.cell8),
        )

        // Capture the base descriptions defined in XML so we can preserve localization.
        baseCellDescriptions = Array(cellButtons.size) { i ->
            cellButtons[i].contentDescription?.toString()?.trim().orEmpty()
        }

        for (i in cellButtons.indices) {
            val idx = i
            cellButtons[i].setOnClickListener { onCellTapped(idx) }
        }

        modePvpButton.setOnClickListener { setMode(TicTacToeGame.Mode.PLAYER_VS_PLAYER) }
        modePvcButton.setOnClickListener { setMode(TicTacToeGame.Mode.PLAYER_VS_COMPUTER) }

        // Reset Game: board only (scores remain).
        resetButton.setOnClickListener {
            game.resetBoard()
            renderAll()
            maybeTriggerComputerMoveIfNeeded()
        }

        // Long-press Reset Game: full reset (board + scores).
        resetButton.setOnLongClickListener {
            game.resetAll()
            renderAll()
            maybeTriggerComputerMoveIfNeeded()
            true
        }
    }

    private fun setMode(mode: TicTacToeGame.Mode) {
        game.mode = mode
        game.resetBoard()
        renderAll()
        updateModeButtons()
        maybeTriggerComputerMoveIfNeeded()
    }

    private fun updateModeButtons() {
        // Simple visual selection state (relies on Material style).
        modePvpButton.isSelected = game.mode == TicTacToeGame.Mode.PLAYER_VS_PLAYER
        modePvcButton.isSelected = game.mode == TicTacToeGame.Mode.PLAYER_VS_COMPUTER
    }

    private fun onCellTapped(index: Int) {
        val moveResult = game.playAt(index)
        if (!moveResult) return

        renderAll()

        // If after player's move the game continues and we're in PvC, let computer play.
        maybeTriggerComputerMoveIfNeeded()
    }

    private fun maybeTriggerComputerMoveIfNeeded() {
        if (!game.isGameOver() &&
            game.mode == TicTacToeGame.Mode.PLAYER_VS_COMPUTER &&
            game.currentPlayer == TicTacToeGame.Player.O
        ) {
            // Post to UI queue for a slightly smoother feel (no extra dependencies).
            statusText.post {
                val moved = game.playComputerMove()
                if (moved) renderAll()
            }
        }
    }

    private fun renderAll() {
        renderScores()
        renderBoard()
        renderStatus()
        updateModeButtons()
    }

    private fun renderScores() {
        scoreXText.text = getString(R.string.score_x, game.scoreX)
        scoreOText.text = getString(R.string.score_o, game.scoreO)
        scoreTiesText.text = getString(R.string.score_ties, game.scoreTies)
    }

    private fun renderBoard() {
        val winner = game.getWinner()
        val winningLine = game.getWinningLine()

        for (i in cellButtons.indices) {
            val button = cellButtons[i]
            val cell = game.board[i]

            button.text = when (cell) {
                TicTacToeGame.Player.X -> "X"
                TicTacToeGame.Player.O -> "O"
                null -> ""
            }

            button.isEnabled = cell == null && !game.isGameOver()

            // Reset selection styling first
            button.isSelected = false

            // Highlight winning line with selected state (selector drawable handles coloring)
            if (winner != null && winningLine != null && winningLine.contains(i)) {
                button.isSelected = true
            }

            // Accessibility: Update cell content descriptions with the current value/state.
            // We keep the base "Cell N" string from XML and append state.
            val base = baseCellDescriptions.getOrNull(i).takeUnless { it.isNullOrBlank() } ?: "Cell ${i + 1}"
            val state = when (cell) {
                TicTacToeGame.Player.X -> "X"
                TicTacToeGame.Player.O -> "O"
                null -> if (game.isGameOver()) "empty, game over" else "empty"
            }
            button.contentDescription = "$base, $state"
        }
    }

    private fun renderStatus() {
        val winner = game.getWinner()
        statusText.visibility = View.VISIBLE

        when {
            winner == TicTacToeGame.Player.X -> statusText.text = getString(R.string.status_x_wins)
            winner == TicTacToeGame.Player.O -> statusText.text = getString(R.string.status_o_wins)
            game.isDraw() -> statusText.text = getString(R.string.status_draw)
            else -> {
                val next = if (game.currentPlayer == TicTacToeGame.Player.X) "X" else "O"
                statusText.text = getString(R.string.status_turn, next)
            }
        }
    }
}
