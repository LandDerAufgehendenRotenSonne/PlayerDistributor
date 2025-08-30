# PlayerDistributor
Reads the playerlist from an excel File and distributes all players across factions

---

### Instructions
- Build or download .jar File
- Place into empty Directory
- copy Excel File into Directory
- run from Terminal
- (optional) edit config.properties, then clear output and run again
- finished list is in output directory

### Config
| **Property** | **value**                                    |
|--------------|----------------------------------------------|
| sheet-name   | name of the excel sheet to be processed      |
| mc-name-col  | index of the column with the minecraft name  |
| role-col     | index of the column with the players role    |
| roles | comma-seperates list of possible roles       |
| faction-col | index of the column with the players faction |
| factions | comma-seperates list of possible factions    |
| friends-col | index of the column where the friends list starts |
| max-friends | how many columns to check for friends to the right of friends-col |
| friends-blacklist | names than are blacklisted from being friends |
| start-row | what row to start processing |
| end-row | what row to stop processing |

### How it works
Players with an empty faction cell are distributed evenly across all factions
while keeping friends in the same faction. The result is written to a copy of the
original excel file in the output directory
