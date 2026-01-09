package org.example.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicTacToeGameTest {

    @Test
    fun playsAlternatingTurns() {
        val game = TicTacToeGame()
        assertEquals(TicTacToeGame.Player.X, game.currentPlayer)

        assertTrue(game.playAt(0))
        assertEquals(TicTacToeGame.Player.O, game.currentPlayer)

        assertTrue(game.playAt(1))
        assertEquals(TicTacToeGame.Player.X, game.currentPlayer)
    }

    @Test
    fun detectsWinRow() {
        val game = TicTacToeGame()

        // X: 0, O: 3, X: 1, O: 4, X: 2 -> X wins top row
        game.playAt(0)
        game.playAt(3)
        game.playAt(1)
        game.playAt(4)
        game.playAt(2)

        assertEquals(TicTacToeGame.Player.X, game.getWinner())
        assertNotNull(game.getWinningLine())
        assertTrue(game.isGameOver())
        assertEquals(1, game.scoreX)
    }

    @Test
    fun detectsDraw() {
        val game = TicTacToeGame()

        // Draw end state (no 3-in-a-row):
        // X O X
        // X X O
        // O X O
        //
        // Moves below are ordered so no earlier winning line occurs mid-sequence.
        val moves = listOf(0, 1, 2, 5, 3, 8, 7, 6, 4)
        for (m in moves) {
            assertTrue(game.playAt(m))
        }

        assertNull(game.getWinner())
        assertTrue(game.isDraw())
        assertTrue(game.isGameOver())
        assertEquals(1, game.scoreTies)
    }

    @Test
    fun computerBlocksImmediateWin() {
        val game = TicTacToeGame()
        game.mode = TicTacToeGame.Mode.PLAYER_VS_COMPUTER

        // X plays 0, O (computer) plays; X plays 1 => X threatens 2
        assertTrue(game.playAt(0))
        assertTrue(game.playComputerMove())

        assertTrue(game.playAt(1))
        assertTrue(game.playComputerMove())

        // Cell 2 should be blocked by O OR the game ended earlier.
        val isBlockedOrGameOver = game.board[2] == TicTacToeGame.Player.O || game.isGameOver()
        assertTrue(isBlockedOrGameOver)
    }

    @Test
    fun cannotPlayOnOccupiedCell() {
        val game = TicTacToeGame()
        assertTrue(game.playAt(0))
        assertFalse(game.playAt(0))
    }
}
