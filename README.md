CoreWars8086-Debugger
=====================

An advanced debugger for the CodeGuru Xtreme competition.
This debugger was developed by the Zorg team, which got to the 1st place in the CodeGuru Xtreme competition of 2012.

The debugger displays the current values of the regusters, the next commands to be executed by the engine.
It also draws a purple dot at the current IP register of the player.

Notes:
- This debugger is meant to be used for developing players and therefore will run MANY rounds, in order to get
  a better statistical marking of the player. We removed the GUI setting of the rounds number, and hopefully
  someone will add it again soon.
- There's a known bug, which is quite annoying: Many times the debugger opens and the left panel
  where all the names and registers are written is just blank. There isn't any exception etc.
  The temporary solution is to re-open the debugger until the problem ceases. You may need to re-open it as many
  as 20 times until is gone. We found that updating Java on the PC may make the problem a bit smaller.
- When running at high speed, the purple dot of the IP will be drawn only every few turns, since it is only drawn
  whenever the registers text is updated.

This tool was of crucial help to us during the development of out player for the comptetion.
We hope it will be of help to you. We also hope that you'll develop it further and share your developments
with everyone.

ToDo:
- Maybe add a sub-step. Currently the Step button runs a single turn for all the players and then halts. We found
  that it may be useful to have the turn of each player run at a time and not all of them together. Maybe an additional
  button for such a functionality will help you.
- Fix the problem of the blank panel. This is the most important thing.
- Maybe add an option to tape the moves. It may be very frustrating to step through a long player and just miss with
  a single turn the move that you wanted to catch...
- Any ideas for new features? Share with us!

Best regards, and Good luck!

Zorg Team

צוות זורג

2012
