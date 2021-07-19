# Mastermind
A Discord bot for the code cracking game 'Mastermind' from 1972

## Links
- [Invite Bot](https://discord.com/api/oauth2/authorize?client_id=830490572765790220&permissions=10304&scope=bot%20applications.commands)
- [top.gg](https://top.gg/bot/830490572765790220)
- [discordbotlist.com](https://discordbotlist.com/bots/mastermind)
- [Explanation Video](https://youtu.be/5X9_5cavUPw)

## Description
Mastermind originally is a bord game from 1972 played with two players. One player is replaced by the bot. The goal of the user is to find a randomly generated combination of 4 pins (though this number can be changed) by first placing a guess in the first row, then getting feedback by the bot about the number of pins which are fully correct and the number of pins which only have the correct color, and then combining all previous tries and their feedback to find the solution.

## Basic Rules
One player (in this case the bot) thinks of a random combination of four pins. The other player then has to find that solution by placing their guess in the first row of the open field. When he is done the bot places black pins ⭕ and white pins ⚪ next to the row. Each black pin stands for one pin with the correct color and the correct spot. The player just isn’t told which ones. White pins stand for pins that have the correct color, but are in the wrong spot.

## Commands
- `/show` The main command of this bot. Shows your current board or starts a new game if the previous one finished.
- `/newgame` Starts a new game, cancelling the current one, if there is one.
- `/rules` Shows a message explaining the game with an example
- `/help` Alias for `/rules`
- `/config` Displays a config screen to change the number of pins and whether to allow multiple pins of the same color.
- `/invite` Displays an invite link for this bot
