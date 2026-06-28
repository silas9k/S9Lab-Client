# S9Lab Player Emote Template

Open `S9Lab_Player_Emote_Template.bbmodel` in Blockbench and select the
`Animate` workspace.

## Important orientation rules

- `right_arm` is the player's right arm. From a front view it appears on the
  left side of your screen.
- `left_arm` is the player's left arm. From a front view it appears on the
  right side of your screen.
- The face of the dummy is the front. Do not animate from an accidental rear
  view.
- Animate the named bone groups, never individual cubes.
- Do not rename `head`, `body`, `right_arm`, `left_arm`, `right_leg`, or
  `left_leg`.
- Do not change bone pivots/origins. They match Minecraft's player model.

## Included animations

- `animation.player.my_emote`: clean animation track for your new emote.
- `animation.player.axis_check`: reference pose. At the end of the timeline,
  the player's right arm points forward/up and the left arm points sideways.
  Use it to confirm that you are looking at the front of the model.

## Export

1. Build the animation in `animation.player.my_emote`.
2. Use `Animation > Export Animations`.
3. Keep the exact bone names.
4. Put the exported file in
   `src/main/resources/assets/s9labclient/geckolib/animations/emotes/`.
5. Register the animation name and file in the existing emote catalog.

The skin is embedded in the model and also provided as
`s9lab_player_dummy_skin.png`, so the template still opens correctly after it
is moved to another folder.
