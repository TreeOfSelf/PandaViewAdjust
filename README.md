![icon](https://github.com/user-attachments/assets/0cf4f4b5-eae6-4b8b-9150-b17d9c567498)

# PandaViewAdjust

## Description

Fabric server-side mod to automatically adjust view-distance and simulation-distanced based on player count & MSPT
Set at what amount of players/MSPT will the simulation/view distance change.

This is useful for keeping server MSPT at a reasonable level, even with a lot of players. It also allows you to have very high view distances when there are very little players on.

## Configuring

config/PandaViewConfig.json generated at runtime

```
[
  //The best acceptble view & simulation distance can be at the top
  { // 5 or less players, 20 or less MSPT
    "maxPlayerCount": 5, //Max amount of players for this configuration (can be 0 to ignore)
    "maxMSPT": 20,    //Max amount of MSPT for this configuration (can be 0 to ignore)
    "viewDistance": 32, //What to set the view distance to at this configuration
    "simulationDistance": 6 //What to set the simulation distance to at this configuration
  },

  // ... Steps between

  {  //The worst acceptable view & simulation distances can be at the bottom
    "maxPlayerCount": 0, 
    "maxMSPT": 0,
    "viewDistance": 3,
    "simulationDistance": 3
  }
]
```

## Support

[Support discord here!]( https://discord.gg/3tP3Tqu983)

## Try it out 

Demo server at **hardcoreanarchy.gay**


## License

[CC0](https://creativecommons.org/public-domain/cc0/)
