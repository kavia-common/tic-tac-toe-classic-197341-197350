package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

/**
 * Main single-screen Activity for the Tic Tac Toe game.
 *
 * UI responsibilities:
 * - Score display at top
 * - 3x3 board in the center
 * - Mode selection + reset buttons at bottom
 *
 * Game responsibilities:
 * - Alternating turns
 * - Detect wins and draws
 * - Maintain running scores
 * - Optional simple AI for Player vs Computer mode
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Our XML uses MaterialComponents theme; this must be set before calling setContentView.
        setContentView(R.layout.activity_main)

        bindViews()
        renderAll()

        // Default mode: PvP
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

        for (i in cellButtons.indices) {
            val idx = i
            cellButtons[i].setOnClickListener {
                onCellTapped(idx)
            }
        }

        modePvpButton.setOnClickListener {
            setMode(TicTacToeGame.Mode.PLAYER_VS_PLAYER)
        }
        modePvcButton.setOnClickListener {
            setMode(TicTacToeGame.Mode.PLAYER_VS_COMPUTER)
        }
        resetButton.setOnClickListener {
            game.resetBoard()
            renderAll()
            maybeTriggerComputerMoveIfNeeded()
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
        if (!game.isGameOver() && game.mode == TicTacToeGame.Mode.PLAYER_VS_COMPUTER && game.currentPlayer == TicTacToeGame.Player.O) {
            // Tiny delay-like behavior without using handlers; just post to UI queue for smoother feel.
            statusText.post {
                val moved = game.playComputerMove()
                if (moved) {
                    renderAll()
                }
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
