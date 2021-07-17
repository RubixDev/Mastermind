# Mastermind
A Discord bot for the puzzle game 'Mastermind' from 1972

## Links
- [Invite Bot](https://discord.com/api/oauth2/authorize?client_id=830490572765790220&permissions=10304&scope=bot%20applications.commands)
- [top.gg](https://top.gg/bot/830490572765790220)
- [discordbotlist.com](https://discordbotlist.com/bots/mastermind)
- [Explanation Video](https://youtu.be/5X9_5cavUPw)

## Basic Rules
Mastermind originally is a bord game from 1972. One player (in this case the bot) thinks of a random combination of four pins. The other player then has to find that solution by placing their guess in the first row of the open field. When he is done the bot places black pins ⭕ and white pins ⚪ next to the row. Each black pin stands for one pin with the correct color and the correct spot. The player just isn't told which ones. White pins stand for pins that have the correct color, but are in the wrong spot.

## Commands
- `/show` The main command of this bot. Shows your current board or starts a new game if the previous one finished.
- `/newgame` Starts a new game, cancelling the current one, if there is one.
- `/rules` Shows a message explaining the game with an example
- `/help` Alias for `/rules`
- `/setpins <amount>` Sets the number of pins the game is played with. Is 4 by default and must be between 3 and 6.
- `/allowmultiples <allow>` Allows or prevents multiple pins of the same color to exist in the solutions. Is on by default.
- `/invite` Displays an invite link for this bot
