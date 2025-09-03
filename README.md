# PlayerDistributor
Reads the player-list from an Excel File and distributes all players across factions

---

## Instructions
- Build or download .jar File
- Place into empty Directory
- copy Excel File into Directory
- run from Terminal
- (optional) edit config.properties
- take note of all errors or warnings
- finished list is in output directory

## Config
| **Property**      | **value**                                                         |
|-------------------|-------------------------------------------------------------------|
| sheet-name        | name of the excel sheet to be processed                           |
| mc-name-col       | index of the column with the minecraft name                       |
| role-col          | index of the column with the players role                         |
| roles             | comma-separates list of possible roles                            |
| faction-col       | index of the column with the players faction                      |
| factions          | comma-separates list of possible factions                         |
| friends-col       | index of the column where the friends list starts                 |
| max-friends       | how many columns to check for friends to the right of friends-col |
| friends-blacklist | Discord Ids than are blacklisted from being friends               |
| start-row         | what row to start processing                                      |
| end-row           | what row to stop processing                                       |

## How it works
The Program scans its directory and processes the first Excel file found
(alternatively, a File-Path can be set using flags).  
Players with no faction are distributed as evenly as possible across all factions
while keeping friends in the same faction and balancing roles. 
Players with a preassigned faction are kept in that faction, and all their friends 
with no faction are forced into the same faction.
The result is written to a copy of the original Excel file in the output directory.

## How to run the .jar
Open a Terminal in the same Directory as the .jar File and enter: 
**java -jar FILE_NAME.jar**

You can also add the following optional flags, separated by spaces, at the end of the above line:

  | Flag               | Description                                                   |
  |--------------------|---------------------------------------------------------------|
  | `--checkMCNames`   | Checks if the Name belongs to a valid Minecraft account       |
  | `--delUnchecked`   | Deletes entries where either Twitch or Discord is not checked |
  | `--requireMutual`  | Friendships only count if mutual                              |
  | `--inputFile=PATH` | Specifies the input Excel file to use                         |
  | `--groupLimit=CAP` | Sets the maximum size of friendship groups (Default: 32)      |

